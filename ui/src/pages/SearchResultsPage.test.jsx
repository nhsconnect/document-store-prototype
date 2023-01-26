import { render, screen, waitFor, within } from "@testing-library/react";
import { Factory } from "fishery";
import useApi from "../apiClients/useApi";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import SearchResultsPage from "./SearchResultsPage";
import userEvent from "@testing-library/user-event";
import { downloadFile } from "../utils/utils";
import { useDeleteDocumentsResponseProviderContext } from "../providers/DeleteDocumentsResponseProvider";
import { MemoryRouter } from "react-router";

jest.mock("../apiClients/useApi");
jest.mock("../providers/PatientDetailsProvider", () => ({
    usePatientDetailsProviderContext: jest.fn(),
}));
jest.mock("../providers/DeleteDocumentsResponseProvider", () => ({
    useDeleteDocumentsResponseProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
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
    const renderPage = () => {
        render(<MemoryRouter><SearchResultsPage /></MemoryRouter>)
    }

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
            renderPage()

            expect(
                screen.getByRole("heading", {
                    name: "Download electronic health records and attachments",
                })
            ).toBeInTheDocument();
            expect(searchButton()).not.toBeInTheDocument();
            expect(screen.getByText(patientData.nhsNumber)).toBeInTheDocument();
            expect(screen.getByText(patientData.familyName)).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
            expect(startAgainLink()).toBeInTheDocument();
        });

        it("should go to home page when user clicks on start again button", () => {
            renderPage()
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
            renderPage()

            expect(screen.getByRole("progressbar", { name: "Loading..." })).toBeInTheDocument();
            expect(await screen.findByText("There are no documents available for this patient.")).toBeInTheDocument();
        });

        it("displays search results when there are results", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
            }));
            renderPage()

            expect(await screen.findByText("List of documents available")).toBeInTheDocument();
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
            renderPage()

            await waitFor(() => {
                expect(screen.getByText("List of documents available")).toBeInTheDocument();
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
            renderPage()

            await waitFor(() => {
                expect(screen.getByText("There are no documents available for this patient.")).toBeInTheDocument();
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
            renderPage()

            await waitFor(() => {
                expect(
                    screen.getByRole("alert")
                ).toBeInTheDocument();
            });
        });

        it("calls api client and should download the zip file when user clicks on download all button", async () => {
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => "some-url",
            }));

            renderPage()

            await waitFor(() => {
                expect(screen.getByText("List of documents available")).toBeInTheDocument();
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
            expect(screen.queryByText("All documents have been successfully downloaded.")).toBeInTheDocument();
        });

        it("downloads the file", async () => {
            const preSignedUrl = "some-pre-signed-url";
            const searchResult = searchResultFactory.build();
            useApi.mockImplementation(() => ({
                findByNhsNumber: () => [searchResult],
                getPresignedUrlForZip: () => preSignedUrl,
            }));

            renderPage()
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
            renderPage()
            await waitFor(() => {
                expect(screen.getByText("List of documents available")).toBeInTheDocument();
            });
            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));
            await waitFor(() => {
                expect(
                    screen.getByRole("button", { name: "Download All Documents" })
                ).toBeDisabled();
                expect(screen.getByRole("progressbar")).toBeVisible()
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

            renderPage()

            await waitFor(() => {
                expect(
                    screen.getByRole("button", {
                        name: "Download All Documents",
                    })
                ).toBeInTheDocument();
            });
            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));

            await waitFor(() => {
                expect(screen.getByRole("alert")).toBeInTheDocument();
            });
        });
    });

    describe("when there is NOT an NHS number", () => {
        beforeEach(() => {
            usePatientDetailsProviderContext.mockReturnValue([undefined, jest.fn()]);
            useDeleteDocumentsResponseProviderContext.mockReturnValue(["", jest.fn()]);
        });

        it("redirects to patient trace page when the NHS number is NOT available", () => {
            renderPage()

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
