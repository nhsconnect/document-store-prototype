import { render, screen, waitFor, within } from "@testing-library/react";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import SearchResultsPage from "./SearchResultsPage";
import userEvent from "@testing-library/user-event";
import { downloadFile } from "../../utils/utils";
import { MemoryRouter, useNavigate } from "react-router";
import { buildPatientDetails, buildSearchResult } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");
jest.mock("../../providers/patientDetailsProvider/PatientDetailsProvider");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));
jest.mock("../../utils/utils");

describe("<SearchResultsPage />", () => {
    const getPatientDetailsMock = jest.fn();
    const findByNhsNumberMock = jest.fn();
    const getPresignedUrlForZipMock = jest.fn();

    beforeEach(() => {
        useAuthorisedDocumentStore.mockReturnValue({ getPatientDetails: getPatientDetailsMock });
        useAuthorisedDocumentStore.mockReturnValue({
            findByNhsNumber: findByNhsNumberMock,
            getPresignedUrlForZip: getPresignedUrlForZipMock,
        });
    });

    describe("with NHS number", () => {
        it("renders the page", async () => {
            const navigateMock = jest.fn();
            const nhsNumber = "9000000001";
            const familyName = "Smith";
            const patientDetails = buildPatientDetails({ nhsNumber, familyName });
            const session = { isLoggedIn: true };
            const setSessionMock = jest.fn();

            useSessionContext.mockReturnValue([session, setSessionMock]);
            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            useNavigate.mockReturnValue(navigateMock);

            renderSearchResultsPage();

            expect(
                screen.getByRole("heading", {
                    name: "Download electronic health records and attachments",
                })
            ).toBeInTheDocument();
            expect(screen.queryByRole("link", { name: "Start Again" })).not.toBeInTheDocument();
            expect(screen.getByText(nhsNumber)).toBeInTheDocument();
            expect(screen.getByText(familyName)).toBeInTheDocument();
            expect(navigateMock).not.toHaveBeenCalled();
            expect(await screen.findByRole("link", { name: "Start Again" })).toBeInTheDocument();
        });

        it("goes to Start page when user clicks on start again button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);

            renderSearchResultsPage();

            expect(await screen.findByRole("link", { name: "Start Again" })).toHaveAttribute("href", "/");
        });

        it("displays a loading spinner when a document search is in progress", async () => {
            findByNhsNumberMock.mockResolvedValue([]);
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);

            renderSearchResultsPage();

            expect(screen.getByRole("progressbar", { name: "Loading..." })).toBeInTheDocument();
            expect(await screen.findByText("There are no documents available for this patient.")).toBeInTheDocument();
        });

        it("displays search results when there are results", async () => {
            const description = "Doc description.";
            const indexed = new Date();
            const searchResult = buildSearchResult({ description, indexed });

            findByNhsNumberMock.mockResolvedValue([searchResult]);
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);

            renderSearchResultsPage();

            expect(await screen.findByText("List of documents available")).toBeInTheDocument();
            const documentDescriptionElement = screen.getByText(description);
            expect(documentDescriptionElement).toBeInTheDocument();
            expect(screen.getByText(indexed.toLocaleString())).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Download All Documents" })).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Delete All Documents" })).toBeInTheDocument();
            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });

        it("displays search results in descending order by 'uploaded at' when there are multiple results", async () => {
            const oldestDate = new Date(Date.UTC(2022, 7, 9, 10));
            const secondOldestDate = new Date(Date.UTC(2022, 7, 10, 10));
            const newestDate = new Date(Date.UTC(2022, 7, 11, 10));
            const searchResults = [
                buildSearchResult({ indexed: oldestDate }),
                buildSearchResult({ indexed: secondOldestDate }),
                buildSearchResult({ indexed: newestDate }),
            ];

            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue(searchResults);

            renderSearchResultsPage();

            expect(await screen.findByText("List of documents available")).toBeInTheDocument();
            const rows = within(screen.getByRole("table")).getAllByRole("row");
            expect(rows[1]).toHaveTextContent(newestDate.toLocaleString());
            expect(rows[2]).toHaveTextContent(secondOldestDate.toLocaleString());
            expect(rows[3]).toHaveTextContent(oldestDate.toLocaleString());
        });

        it("displays a message when a document search returns no results", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([]);

            renderSearchResultsPage();

            expect(await screen.findByText("There are no documents available for this patient.")).toBeInTheDocument();
            expect(screen.queryByRole("button", { name: "Download All Documents" })).not.toBeInTheDocument();
            expect(screen.queryByRole("button", { name: "Delete All Documents" })).not.toBeInTheDocument();
        });

        it("displays a message when a document search fails", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockRejectedValue(new Error("Failed to search for docs!"));

            renderSearchResultsPage();

            expect(await screen.findByRole("alert")).toBeInTheDocument();
        });

        xit("calls API client and should download the ZIP file when user clicks on download all button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult()]);

            renderSearchResultsPage();

            expect(await screen.findByText("List of documents available")).toBeInTheDocument();

            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));

            expect(
                await screen.findByRole("button", {
                    name: "Download All Documents",
                })
            ).not.toBeDisabled();
            expect(screen.queryByText("Failed to download, please retry.")).not.toBeInTheDocument();
        });

        it("downloads the file", async () => {
            const preSignedUrl = "some-pre-signed-url";
            const nhsNumber = "9000000009";
            const patientDetails = buildPatientDetails({ nhsNumber });

            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            getPresignedUrlForZipMock.mockResolvedValue(preSignedUrl);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult()]);

            renderSearchResultsPage();
            userEvent.click(
                await screen.findByRole("button", {
                    name: "Download All Documents",
                })
            );

            await waitFor(() => {
                expect(downloadFile).toHaveBeenCalledWith(preSignedUrl, `patient-record-${nhsNumber}`);
            });
        });

        it("display spinner on the download all button while waiting to download the zip file", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult()]);

            renderSearchResultsPage();
            userEvent.click(await screen.findByRole("button", { name: "Download All Documents" }));
            expect(screen.getByRole("SpinnerButton")).toBeInTheDocument();

            await waitFor(() => {
                expect(downloadFile).toHaveBeenCalled();
            });
        });

        it("displays error message when download fails after clicking download all button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult()]);
            getPresignedUrlForZipMock.mockRejectedValue(new Error("Failed to download docs!"));

            renderSearchResultsPage();
            userEvent.click(await screen.findByRole("button", { name: "Download All Documents" }));

            expect(await screen.findByRole("alert")).toBeInTheDocument();
        });

        it("disables download all button when there are no uninfected files available", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult({ virusScanResult: "Infected" })]);

            renderSearchResultsPage();

            expect(await screen.findByRole("button", { name: "Download All Documents" })).toBeDisabled();
        });

        it("renders warning message when there is an infected file available", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            const searchResult = [
                buildSearchResult({ virusScanResult: "Infected" }),
                buildSearchResult({ virusScanResult: "Clean" }),
            ];
            findByNhsNumberMock.mockResolvedValue(searchResult);

            renderSearchResultsPage();

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
        });

        it("renders new table with infected filenames when there is an infected file available", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            const infectedFilename = "InfectedFile";
            const cleanFilename = "CleanFile";
            const searchResult = [
                buildSearchResult({ virusScanResult: "Infected", description: infectedFilename }),
                buildSearchResult({ virusScanResult: "Clean", description: cleanFilename }),
            ];
            findByNhsNumberMock.mockResolvedValue(searchResult);

            renderSearchResultsPage();

            const infectedTable = await screen.findByText("List of documents not available");
            expect(infectedTable).toBeInTheDocument();
            expect(within(infectedTable).findByText(infectedFilename));

            const cleanTable = await screen.findByText("List of documents available");
            expect(cleanTable).toBeInTheDocument();
            expect(within(cleanTable).findByText(cleanFilename));
        });

        it("doesn't render clean table if all files available are infected", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            const infectedFilename = "InfectedFile";
            const searchResult = [buildSearchResult({ virusScanResult: "Infected", description: infectedFilename })];
            findByNhsNumberMock.mockResolvedValue(searchResult);

            renderSearchResultsPage();

            expect(await screen.findByText("List of documents not available")).toBeInTheDocument();
            expect(screen.queryByText("List of documents available")).not.toBeInTheDocument();
        });
    });

    describe("without NHS number", () => {
        it("redirects to patient trace page when the NHS number is NOT available", async () => {
            const navigateMock = jest.fn();

            usePatientDetailsContext.mockReturnValue([undefined, jest.fn()]);
            useNavigate.mockReturnValue(navigateMock);

            renderSearchResultsPage();

            await waitFor(() => {
                expect(navigateMock).toHaveBeenCalledWith(routes.SEARCH_PATIENT);
            });
        });
    });

    describe("navigation", () => {
        it("navigates to start page when user is unauthorized to make search request", async () => {
            const nhsNumber = "9000000001";
            const familyName = "Smith";
            const patientDetails = buildPatientDetails({ nhsNumber, familyName });
            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);

            const errorResponse = {
                response: {
                    status: 403,
                    message: "403 Unauthorized.",
                },
            };

            const navigateMock = jest.fn();
            const setSessionMock = jest.fn();
            const session = { isLoggedIn: true };
            useNavigate.mockReturnValue(navigateMock);
            useSessionContext.mockReturnValue([session, setSessionMock]);

            findByNhsNumberMock.mockRejectedValue(errorResponse);

            renderSearchResultsPage();

            await waitFor(() => {
                expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
            });
        });

        it("navigates to start page when user is unauthorized to make download request", async () => {
            const errorResponse = {
                response: {
                    status: 403,
                    message: "403 Unauthorized.",
                },
            };
            const navigateMock = jest.fn();
            const setSessionMock = jest.fn();
            const session = { isLoggedIn: true };
            useNavigate.mockReturnValue(navigateMock);
            useSessionContext.mockReturnValue([session, setSessionMock]);

            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([buildSearchResult()]);
            getPresignedUrlForZipMock.mockRejectedValue(errorResponse);

            renderSearchResultsPage();
            userEvent.click(await screen.findByRole("button", { name: "Download All Documents" }));

            await waitFor(() => {
                expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
            });
        });
    });
});

const renderSearchResultsPage = () => {
    render(
        <MemoryRouter>
            <SearchResultsPage />
        </MemoryRouter>
    );
};
