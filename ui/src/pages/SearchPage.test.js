import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import SearchPage from "./SearchPage";

jest.mock("../apiClients/apiClient");

describe("Search page", () => {
    it("displays a loading spinner when a document search is in progress", async () => {
        const apiClientMock = new ApiClient();
        apiClientMock.findByNhsNumber = jest.fn(() => {
            return [];
        });
        render(<SearchPage client={apiClientMock} />);

        userEvent.type(
            screen.getByLabelText("Find by NHS number"),
            "123456789"
        );
        userEvent.click(screen.getByText("Search"));

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
        });
    });

    it("displays search results when there are results", async () => {
        const documentDescription = "Some description";
        const documentType = "some type";
        const documentUrl = "https://some.url";
        const apiClientMock = new ApiClient();
        apiClientMock.findByNhsNumber = jest.fn(() => {
            return [
                {
                    description: documentDescription,
                    type: documentType,
                    url: documentUrl,
                },
            ];
        });
        const nhsNumber = "123456789";
        render(<SearchPage client={apiClientMock} />);

        userEvent.type(screen.getByLabelText("Find by NHS number"), nhsNumber);
        userEvent.click(screen.getByText("Search"));

        await waitFor(() => {
            expect(apiClientMock.findByNhsNumber).toHaveBeenCalledWith(
                nhsNumber
            );
        });
        expect(screen.getByText("Documents")).toBeInTheDocument();
        expect(screen.getByText(documentDescription)).toBeInTheDocument();
        expect(screen.getByText(documentType)).toBeInTheDocument();
        const documentLink = screen.getByText("Link");
        expect(documentLink).toBeInTheDocument();
        expect(documentLink).toHaveAttribute("href", documentUrl);
        expect(screen.queryByRole("progressbar")).toBeNull();
    });

    it("clears previous search results when a new search is triggered", async () => {
        const apiClientMock = new ApiClient();
        apiClientMock.findByNhsNumber = jest.fn(() => {
            return [
                {
                    description: "Some description",
                    type: "some type",
                    url: "https://some.url",
                },
            ];
        });
        const nhsNumber = "123456789";
        render(<SearchPage client={apiClientMock} />);

        userEvent.type(screen.getByLabelText("Find by NHS number"), nhsNumber);
        userEvent.click(screen.getByText("Search"));

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
        });
        await waitFor(() => {
            expect(screen.queryByRole("progressbar")).toBeNull();
        });
        expect(screen.getByText("Documents")).toBeInTheDocument();

        userEvent.click(screen.getByText("Search"));

        await waitFor(() => {
            expect(screen.queryByText("Documents")).toBeNull();
        });
    });

    it("displays a message when a document search returns no results", async () => {
        const apiClientMock = new ApiClient();
        apiClientMock.findByNhsNumber = jest.fn(() => {
            return [];
        });
        render(<SearchPage client={apiClientMock} />);

        userEvent.type(
            screen.getByLabelText("Find by NHS number"),
            "123456789"
        );
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

        userEvent.type(
            screen.getByLabelText("Find by NHS number"),
            "123456789"
        );
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
