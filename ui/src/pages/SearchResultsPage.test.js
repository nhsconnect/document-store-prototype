import { render, screen, waitFor, within } from "@testing-library/react";
import { Factory } from "fishery";

import ApiClient from "../apiClients/apiClient";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import SearchResultsPage from "./SearchResultsPage";

jest.mock("../apiClients/apiClient");
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

const searchResultFactory = Factory.define(({ sequence }) => ({
    description: "Some description",
    type: "some type",
    url: `https://some.${sequence}.url`,
    indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

describe("Search page", () => {
    describe("when there is an NHS number", () => {
        const nhsNumber = "1112223334";

        beforeEach(() => {
            useNhsNumberProviderContext.mockReturnValue([nhsNumber, jest.fn()]);
        });

        it("renders the page", () => {
            render(<SearchResultsPage />);

            expect(
                screen.getByRole("heading", {
                    name: "View Stored Patient Record",
                })
            ).toBeInTheDocument();
            expect(nhsNumberField()).toBeInTheDocument();
            expect(nhsNumberField()).toHaveValue(nhsNumber);
            expect(nhsNumberField()).toHaveAttribute("readonly");
            expect(searchButton()).not.toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("displays a loading spinner when a document search is in progress", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.findByNhsNumber = jest.fn(() => {
                return [];
            });
            render(<SearchResultsPage client={apiClientMock} />);

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
            render(<SearchResultsPage client={apiClientMock} />);

            await waitFor(() => {
                expect(apiClientMock.findByNhsNumber).toHaveBeenCalledWith(
                    nhsNumber
                );
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
            render(<SearchResultsPage client={apiClientMock} />);

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

        it("displays a message when a document search returns no results", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.findByNhsNumber = jest.fn(() => {
                return [];
            });
            render(<SearchResultsPage client={apiClientMock} />);

            await waitFor(() => {
                expect(screen.getByText("No record found")).toBeInTheDocument();
            });
        });

        it("displays a message when a document search fails", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.findByNhsNumber = jest.fn(() => {
                throw Error("Search error!");
            });
            render(<SearchResultsPage client={apiClientMock} />);

            await waitFor(() => {
                expect(
                    screen.getByText(
                        "Sorry, the search failed due to an internal error. Please try again."
                    )
                ).toBeInTheDocument();
            });
        });

        it("open document in new tab when user clicks on document description link", async () => {
            const apiClientMock = new ApiClient();
            const searchResult = searchResultFactory.build();
            apiClientMock.findByNhsNumber = jest.fn(() => {
                return [searchResult];
            });
            window.open = jest.fn();
            render(<SearchResultsPage client={apiClientMock} />);

            await waitFor(() => {
                expect(screen.getByText("Documents")).toBeInTheDocument();
            });
            const documentDescriptionElement = screen.getByText(
                searchResult.description
            );
            expect(documentDescriptionElement.getAttribute("href")).toBe(
                searchResult.url
            );
            expect(documentDescriptionElement.getAttribute("target")).toBe(
                "_blank"
            );
        });
    });

    describe("when there is NOT an NHS number", () => {
        beforeEach(() => {
            useNhsNumberProviderContext.mockReturnValue([undefined, jest.fn()]);
        });

        it("redirects to patient trace page when the NHS number is NOT available", () => {
            render(<SearchResultsPage />);

            expect(mockNavigate).toHaveBeenCalledWith("/search/patient-trace");
        });
    });
});

function nhsNumberField() {
    return screen.getByLabelText("Find by NHS number");
}

function searchButton() {
    return screen.queryByRole("button", { name: "Search" });
}
