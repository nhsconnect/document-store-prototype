import {render, screen, waitFor, within} from "@testing-library/react";
import {Factory} from "fishery";
import ApiClient from "../apiClients/apiClient";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";
import SearchResultsPage from "./SearchResultsPage";
import userEvent from "@testing-library/user-event";

jest.mock("../apiClients/apiClient");
jest.mock("../providers/NhsNumberProvider", () => ({
  useNhsNumberProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
  useNavigate: () => mockNavigate,
}));

const searchResultFactory = Factory.define(() => ({
  id: "some-id",
  description: "Some description",
  type: "some type",
  indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

describe("Search page", () => {
  describe("when there is an NHS number", () => {
    const nhsNumber = "1112223334";

    beforeEach(() => {
      useNhsNumberProviderContext.mockReturnValue([nhsNumber, jest.fn()]);
    });

    it("renders the page", () => {
      render(<SearchResultsPage/>);

      expect(
        screen.getByRole("heading", {
          name: "Download and view a stored document",
        })
      ).toBeInTheDocument();
      expect(nhsNumberField()).toBeInTheDocument();
      expect(nhsNumberField()).toHaveValue(nhsNumber);
      expect(nhsNumberField()).toHaveAttribute("readonly");
      expect(searchButton()).not.toBeInTheDocument();
      expect(mockNavigate).not.toHaveBeenCalled();
      expect(startAgainButton()).toBeInTheDocument();
    });

    it("should go to home page when user clicks on start again button", () =>{
      render(<SearchResultsPage/>);
      userEvent.click(startAgainButton());
      expect(mockNavigate).toHaveBeenCalled();
    })

    it("displays a loading spinner when a document search is in progress", async () => {
      const apiClientMock = new ApiClient();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [];
      });
      render(<SearchResultsPage client={apiClientMock}/>);

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
      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(apiClientMock.findByNhsNumber).toHaveBeenCalledWith(
          nhsNumber
        );
      });
      expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
      const documentDescriptionElement = screen.getByText(
        searchResult.description
      );
      expect(documentDescriptionElement).toBeInTheDocument();
      expect(
        screen.getByText(searchResult.indexed.toLocaleString())
      ).toBeInTheDocument();
      expect(screen.getByRole("button", {name: "Download All"})).toBeInTheDocument();
      expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();

    });

    it("displays search results in descending order by 'uploaded at' value when there are multiple results", async () => {
      const apiClientMock = new ApiClient();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [
          searchResultFactory.build({
            id:"some-id1",
            description: "oldest",
            indexed: new Date(Date.UTC(2022, 7, 9, 10)),
          }),
          searchResultFactory.build({
            id:"some-id2",
            description: "latest",
            indexed: new Date(Date.UTC(2022, 7, 11, 10)),
          }),
          searchResultFactory.build({
            id:"some-id3",
            description: "middle",
            indexed: new Date(Date.UTC(2022, 7, 10, 10)),
          }),
        ];
      });
      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
      });

      const tableBody = document.querySelector("tbody");
      const resultRows = within(tableBody).getAllByRole("row");
      expect(resultRows).toHaveLength(3);
      expect(resultRows[0].innerHTML).toContain("latest");
      expect(resultRows[1].innerHTML).toContain("middle");
      expect(resultRows[2].innerHTML).toContain("oldest");
    });

    it("displays a message when a document search returns no results", async () => {
      const apiClientMock = new ApiClient();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [];
      });
      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(screen.getByText("No record found")).toBeInTheDocument();
      });
      expect(screen.queryByRole("button", {name: "Download All"})).not.toBeInTheDocument();
    });

    it("displays a message when a document search fails", async () => {
      const apiClientMock = new ApiClient();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        throw Error("Search error!");
      });
      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(
          screen.getByText(
            "Sorry, the search failed due to an internal error. Please try again."
          )
        ).toBeInTheDocument();
      });
    });

    it("calls api client and should download the zip file when user clicks on download all button", async () => {
      const apiClientMock = new ApiClient();
      const searchResult = searchResultFactory.build();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [searchResult];
      });
      apiClientMock.getPresignedUrlForZip = jest.fn(() => {
        return 'some-url';
      });

      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
      });

      userEvent.click(screen.getByRole("button", {name: "Download All"}));


      expect(apiClientMock.getPresignedUrlForZip).toHaveBeenCalled();

      await waitFor(() => {
        expect(screen.getByRole("button", {name:"Download All"})).not.toBeDisabled();
      });

      expect(screen.queryByText("Failed to download, please retry.")).not.toBeInTheDocument();
    });

    it("should disable the download all button while waiting to download the zip file", async ()=>{
      const apiClientMock = new ApiClient();
      const searchResult = searchResultFactory.build();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [searchResult];
      });
      apiClientMock.getPresignedUrlForZip = jest.fn(() => {
        return 'some-url';
      });
      render(<SearchResultsPage client={apiClientMock}/>);
      await waitFor(() => {
        expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
      });
      userEvent.click(screen.getByRole("button", {name: "Download All"}));
      expect(screen.getByRole("button", {name:"Download All"})).toBeDisabled();
    });

    it("should display error message when download fails after clicking download all button", async () => {
      const apiClientMock = new ApiClient();
      const searchResult = searchResultFactory.build();
      apiClientMock.findByNhsNumber = jest.fn(() => {
        return [searchResult];
      });
      apiClientMock.getPresignedUrlForZip = jest.fn(() => {
        throw new Error("No url received");
      });

      render(<SearchResultsPage client={apiClientMock}/>);

      await waitFor(() => {
        expect(screen.getByRole("button", {name: "Download All"})).toBeInTheDocument();
      });
      userEvent.click(screen.getByRole("button", {name: "Download All"}));


      expect(apiClientMock.getPresignedUrlForZip).toHaveBeenCalled();
      await waitFor(() => {
        expect(screen.getByText("Failed to download, please retry.")).toBeInTheDocument();
      });
    });
  });

  describe("when there is NOT an NHS number", () => {
    beforeEach(() => {
      useNhsNumberProviderContext.mockReturnValue([undefined, jest.fn()]);
    });

    it("redirects to patient trace page when the NHS number is NOT available", () => {
      render(<SearchResultsPage/>);

      expect(mockNavigate).toHaveBeenCalledWith("/search/patient-trace");
    });
  });
});

function nhsNumberField() {
  return screen.getByLabelText("Find by NHS number");
}

function searchButton() {
  return screen.queryByRole("button", {name: "Search"});
}
function startAgainButton(){
  return screen.queryByRole("button", {name:"Start Again"});
}
