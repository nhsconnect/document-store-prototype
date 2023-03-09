import { render, screen, waitFor, within } from "@testing-library/react";
import { usePatientDetailsContext } from "../../providers/PatientDetailsProvider";
import SearchResultsPage from "./SearchResultsPage";
import userEvent from "@testing-library/user-event";
import { downloadFile } from "../../utils/utils";
import { MemoryRouter, useNavigate } from "react-router";
import { buildPatientDetails, searchResultFactory } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/DocumentStoreProvider";

jest.mock("../../providers/DocumentStoreProvider");
jest.mock("../../providers/PatientDetailsProvider");
jest.mock("react-router", () => ({
    ...jest.requireActual("react-router"),
    useNavigate: jest.fn(),
}));
jest.mock("../../utils/utils");

describe("<SearchResultsPage />", () => {
    const findByNhsNumberMock = jest.fn();
    const getPresignedUrlForZipMock = jest.fn();

    beforeEach(() => {
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

        it("goes to home page when user clicks on start again button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);

            renderSearchResultsPage();

            expect(await screen.findByRole("link", { name: "Start Again" })).toHaveAttribute("href", "/home");
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
            const searchResult = searchResultFactory.build({ description, indexed });

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
                searchResultFactory.build({ indexed: oldestDate }),
                searchResultFactory.build({ indexed: secondOldestDate }),
                searchResultFactory.build({ indexed: newestDate }),
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

        it("calls API client and should download the ZIP file when user clicks on download all button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([searchResultFactory.build()]);

            renderSearchResultsPage();

            expect(await screen.findByText("List of documents available")).toBeInTheDocument();

            userEvent.click(screen.getByRole("button", { name: "Download All Documents" }));

            expect(
                await screen.findByRole("button", {
                    name: "Download All Documents",
                })
            ).not.toBeDisabled();
            expect(screen.queryByText("Failed to download, please retry.")).not.toBeInTheDocument();
            expect(screen.queryByText("All documents have been successfully downloaded.")).toBeInTheDocument();
        });

        it("downloads the file", async () => {
            const preSignedUrl = "some-pre-signed-url";
            const nhsNumber = "9000000009";
            const patientDetails = buildPatientDetails({ nhsNumber });

            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            getPresignedUrlForZipMock.mockResolvedValue(preSignedUrl);
            findByNhsNumberMock.mockResolvedValue([searchResultFactory.build()]);

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

        it("disables the download all button while waiting to download the zip file", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([searchResultFactory.build()]);

            renderSearchResultsPage();
            userEvent.click(await screen.findByRole("button", { name: "Download All Documents" }));

            expect(screen.getByRole("button", { name: "Download All Documents" })).toBeDisabled();
            await waitFor(() => {
                expect(downloadFile).toHaveBeenCalled();
            });
        });

        it("displays error message when download fails after clicking download all button", async () => {
            usePatientDetailsContext.mockReturnValue([buildPatientDetails(), jest.fn()]);
            findByNhsNumberMock.mockResolvedValue([searchResultFactory.build()]);
            getPresignedUrlForZipMock.mockRejectedValue(new Error("Failed to download docs!"));

            renderSearchResultsPage();
            userEvent.click(await screen.findByRole("button", { name: "Download All Documents" }));

            expect(await screen.findByRole("alert")).toBeInTheDocument();
        });
    });

    describe("without NHS number", () => {
        it("redirects to patient trace page when the NHS number is NOT available", () => {
            const navigateMock = jest.fn();
            const patientDetails = buildPatientDetails({ nhsNumber: undefined });

            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            useNavigate.mockReturnValue(navigateMock);

            renderSearchResultsPage();

            expect(navigateMock).toHaveBeenCalledWith("/search/patient-trace");
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
