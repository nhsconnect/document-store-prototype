import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Factory } from "fishery";

import ApiClient from "../apiClients/apiClient";
import SearchPage from "./SearchPage";

jest.mock("../apiClients/apiClient");

const searchResultFactory = Factory.define(({ sequence }) => ({
  description: "Some description",
  type: "some type",
  url: `https://some.${sequence}.url`,
  indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

describe("Search page", () => {
  it("displays a loading spinner when a document search is in progress", async () => {
    const apiClientMock = new ApiClient();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      return [];
    });
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), "123456789");
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });
  });

  it("displays search results when there are results", async () => {
    const apiClientMock = new ApiClient();
    const searchResult = searchResultFactory.build();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      return [searchResult];
    });
    const nhsNumber = "123456789";
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), nhsNumber);
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(apiClientMock.findByNhsNumber).toHaveBeenCalledWith(nhsNumber);
    });
    expect(screen.getByText("Documents")).toBeInTheDocument();
    const documentDescriptionElement = screen.getByText(
      searchResult.description
    );
    expect(documentDescriptionElement).toBeInTheDocument();
    expect(documentDescriptionElement).toHaveAttribute(
      "href",
      searchResult.url
    );
    expect(screen.getByText(searchResult.type)).toBeInTheDocument();
    expect(
      screen.getByText(searchResult.indexed.toLocaleString())
    ).toBeInTheDocument();
    expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
  });

  it("displays search results in descending order by 'uploaded at' value when there are multiple results", async () => {
    const apiClientMock = new ApiClient();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      return [
        searchResultFactory.build({
          description: "oldest",
          indexed: new Date(Date.UTC(2022, 7, 9, 10)),
        }),
        searchResultFactory.build({
          description: "latest",
          indexed: new Date(Date.UTC(2022, 7, 11, 10)),
        }),
        searchResultFactory.build({
          description: "middle",
          indexed: new Date(Date.UTC(2022, 7, 10, 10)),
        }),
      ];
    });
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), "123456789");
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(screen.getByText("Documents")).toBeInTheDocument();
    });

    const tableBody = document.querySelector("tbody");
    const resultRows = within(tableBody).getAllByRole("row");
    expect(resultRows).toHaveLength(3);
    expect(resultRows[0].innerHTML).toContain("latest");
    expect(resultRows[1].innerHTML).toContain("middle");
    expect(resultRows[2].innerHTML).toContain("oldest");
  });

  it("clears previous search results when a new search is triggered", async () => {
    const apiClientMock = new ApiClient();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      return [searchResultFactory.build()];
    });
    const nhsNumber = "123456789";
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), nhsNumber);
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(screen.getByRole("progressbar")).toBeInTheDocument();
    });
    await waitFor(() => {
      expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });
    expect(screen.getByText("Documents")).toBeInTheDocument();

    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(screen.queryByText("Documents")).not.toBeInTheDocument();
    });
  });

  it("displays a message when a document search returns no results", async () => {
    const apiClientMock = new ApiClient();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      return [];
    });
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), "123456789");
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(screen.getByText("No record found")).toBeInTheDocument();
    });
  });

  it("displays a message when a document search fails", async () => {
    const apiClientMock = new ApiClient();
    apiClientMock.findByNhsNumber = jest.fn(() => {
      throw Error("Search error!");
    });
    render(<SearchPage client={apiClientMock} />);

    userEvent.type(screen.getByLabelText("Find by NHS number"), "123456789");
    userEvent.click(screen.getByText("Search"));

    await waitFor(() => {
      expect(
        screen.getByText(
          "Sorry, the search failed due to an internal error. Please try again."
        )
      ).toBeInTheDocument();
    });
  });
});
