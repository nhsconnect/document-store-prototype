import { render, screen, waitFor, within } from "@testing-library/react";
import { Factory } from "fishery";
import useApi from "../apiClients/useApi";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import SearchResultsPage from "./SearchResultsPage";
import userEvent from "@testing-library/user-event";
import { downloadFile } from "../utils/utils";
import { useDeleteDocumentsResponseProviderContext } from "../providers/DeleteDocumentsResponseProvider";

jest.mock("../apiClients/useApi");
jest.mock("../providers/PatientDetailsProvider", () => ({
    usePatientDetailsProviderContext: jest.fn(),
}));
jest.mock("../providers/DeleteDocumentsResponseProvider", () => ({
    useDeleteDocumentsResponseProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));
jest.mock("../utils/utils");

const searchResultFactory = Factory.define(() => ({
    id: "some-id",
    description: "Some description",
    type: "some type",
    indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

describe("<SearchResultsPage />", () => {
    describe("when there is an NHS number", () => {
        const nhsNumber = "1112223334";
        const patientData = {
            birthDate: "2010-10-22",
            familyName: "Smith",
            givenName: ["Jane"],
            nhsNumber: nhsNumber,
            postalCode: "LS1 6AE",
        };
        const deleteDocumentsResponse = "";

        beforeEach(() => {
            usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
            useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);
        });

        it("renders the page", () => {
            render(<SearchResultsPage />);

            expect(
                screen.getByRole("heading", {
                    name: "Download electronic health records and attachments",
                })
            ).toBeInTheDocument();
            expect(searchButton()).not.toBeInTheDocument();
            expect(screen.getByText(`NHS number ${patientData.nhsNumber}`)).toBeInTheDocument();
            expect(screen.getByText(patientData.familyName)).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
            expect(startAgainLink()).toBeInTheDocument();
        });

        it("should go to home page when user clicks on start again button", () => {
            render(<SearchResultsPage />);
            userEvent.click(startAgainLink());
            expect(startAgainLink()).toHaveAttribute("href", "/home");
        });

        it("displays a loading spinner when a document search is in progress", async () => {
            useApi.mockImplementation(() => {
                return {
                    findByNhsNumber: () => {
                        return [];
                    },
                };
            });
            render(<SearchResultsPage />);

            expect(screen.getByRole("progressbar", { name: "Loading..." })).toBeInTheDocument();
            expect(
                await screen.findByText("There are no records associated with this patient to delete or download.")
            ).toBeInTheDocument();
        });

        it("displays search results when there are results", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
            }));
            render(<SearchResultsPage />);

            expect(await screen.findByText("List of documents available to download")).toBeInTheDocument();
            const documentDescriptionElement = screen.getByText(searchResult.description);
            expect(documentDescriptionElement).toBeInTheDocument();
            expect(screen.getByText(searchResult.indexed.toLocaleString())).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Download All Documents" })).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Delete All Documents" })).toBeInTheDocument();

            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });

        it("displays search results in descending order by 'uploaded at' value when there are multiple results", async () => {
            const searchResults = [
                searchResultFactory.build({
                    id: "some-id1",
                    description: "oldest",
                    indexed: new Date(Date.UTC(2022, 7, 9, 10)),
                }),
                searchResultFactory.build({
                    id: "some-id2",
                    description: "latest",
                    indexed: new Date(Date.UTC(2022, 7, 11, 10)),
                }),
                searchResultFactory.build({
                    id: "some-id3",
                    description: "middle",
                    indexed: new Date(Date.UTC(2022, 7, 10, 10)),
                }),
            ];
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => searchResults,
            }));
            render(<SearchResultsPage />);

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
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [],
            }));
            render(<SearchResultsPage />);

            await waitFor(() => {
                expect(
                    screen.getByText("There are no records associated with this patient to delete or download.")
                ).toBeInTheDocument();
            });
            expect(screen.queryByRole("button", { name: "Download All Documents" })).not.toBeInTheDocument();
            expect(screen.queryByRole("button", { name: "Delete All Documents" })).not.toBeInTheDocument();
        });

        it("displays a message when a document search fails", async () => {
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => {
                    throw Error("Error!");
                },
            }));
            render(<SearchResultsPage />);

            await waitFor(() => {
                expect(
                    screen.getByText("Sorry, the search failed due to an internal error. Please try again.")
                ).toBeInTheDocument();
            });
        });

        it("calls api client and should download the zip file when user clicks on download all button", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => "some-url",
            }));

            render(<SearchResultsPage />);

            await waitFor(() => {
                expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
            });

            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));

            await waitFor(() => {
                expect(
                    screen.getByRole("button", {
                        name: "Download All Documents",
                    })
                ).not.toBeDisabled();
            });

            expect(screen.queryByText("Failed to download, please retry.")).not.toBeInTheDocument();
        });

        it("downloads the file", async () => {
            const preSignedUrl = "some-pre-signed-url";
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => preSignedUrl,
            }));

            render(<SearchResultsPage />);
            userEvent.click(
                await screen.findByRole("button", {
                    name: "Download All Documents",
                })
            );

            await waitFor(() => {
                expect(downloadFile).toHaveBeenCalledWith(preSignedUrl, `patient-record-${nhsNumber}`);
            });
        });

        it("should disable the download all button while waiting to download the zip file", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => "some-url",
            }));
            render(<SearchResultsPage />);
            await waitFor(() => {
                expect(screen.getByText("List of documents available to download")).toBeInTheDocument();
            });
            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));
            await waitFor(() => {
                expect(
                    screen.getByRole("button", {
                        name: "Downloading All Documents...",
                    })
                ).toBeDisabled();
            });
        });

        it("should display error message when download fails after clicking download all button", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => {
                    throw Error("Error");
                },
            }));

            render(<SearchResultsPage />);

            await waitFor(() => {
                expect(
                    screen.getByRole("button", {
                        name: "Download All Documents",
                    })
                ).toBeInTheDocument();
            });
            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));

            await waitFor(() => {
                expect(screen.getByText("Failed to download, please retry.")).toBeInTheDocument();
            });
        });

        it("should navigate to confirm deleteAllDocuments page when user clicks on Delete All button", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
            }));

            render(<SearchResultsPage />);

            await waitFor(() => {
                expect(screen.getByRole("button", { name: "Delete All Documents" })).toBeInTheDocument();
            });
            userEvent.click(screen.getByRole("button", { name: "Delete All Documents" }));

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith("/search/results/delete-documents-confirmation");
            });
        });
        describe("when user clicks on Delete All button and successfully navigate to confirmation page and choose Yes", () => {
            it("should display error message next to Delete All Documents Button when failed to delete the documents", async () => {
                const searchResult = searchResultFactory.build();
                const deleteDocumentsResponse = "unsuccessful";
                useApi.mockImplementation(() => ({
                    findByNhsNumber: () => [searchResult],
                }));
                useDeleteDocumentsResponseProviderContext.mockReturnValue([deleteDocumentsResponse, jest.fn()]);

                render(<SearchResultsPage />);
                await waitFor(() => {
                    expect(screen.getByRole("button", { name: "Delete All Documents" })).toBeInTheDocument();
                });
                userEvent.click(screen.getByRole("button", { name: "Delete All Documents" }));

                await waitFor(() => {
                    expect(
                        screen.getByText("There has been an issue deleting these records, please try again later.")
                    ).toBeInTheDocument();
                });
            });
        });
    });

    describe("when there is NOT an NHS number", () => {
        beforeEach(() => {
            usePatientDetailsProviderContext.mockReturnValue([undefined, jest.fn()]);
            useDeleteDocumentsResponseProviderContext.mockReturnValue(["", jest.fn()]);
        });

        it("redirects to patient trace page when the NHS number is NOT available", () => {
            render(<SearchResultsPage />);

            expect(mockNavigate).toHaveBeenCalledWith("/search/patient-trace");
        });
    });
});

function searchButton() {
    return screen.queryByRole("button", { name: "Search" });
}

function startAgainLink() {
    return screen.queryByRole("link", { name: "Start Again" });
}
