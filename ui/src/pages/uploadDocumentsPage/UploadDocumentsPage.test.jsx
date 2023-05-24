import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { act } from "react-dom/test-utils";
import { documentUploadStates } from "../../enums/documentUploads";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import UploadDocumentsPage from "./UploadDocumentsPage";
import { useNavigate } from "react-router";
import { buildPatientDetails, buildTextFile } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");
jest.mock("../../providers/patientDetailsProvider/PatientDetailsProvider");
jest.mock("react-router");

describe("<UploadDocumentsPage />", () => {
    describe("with NHS number", () => {
        it("renders the page", () => {
            const navigateMock = jest.fn();
            const nhsNumber = "9000000009";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const session = { isLoggedIn: true };
            const setSessionMock = jest.fn();

            useSessionContext.mockReturnValue([session, setSessionMock]);
            useNavigate.mockImplementation(() => navigateMock);
            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);

            renderUploadDocumentsPage();

            expect(screen.getByRole("heading", { name: "Upload documents" })).toBeInTheDocument();
            expect(screen.getByText(nhsNumber)).toBeInTheDocument();
            expect(screen.getByLabelText("Select file(s)")).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Upload" })).toBeInTheDocument();
            expect(navigateMock).not.toHaveBeenCalled();
        });

        it("uploads documents and displays the progress", async () => {
            const nhsNumber = "9000000009";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const navigateMock = jest.fn();
            const uploadDocumentMock = jest.fn();
            const documentOne = buildTextFile("one", 100);
            const documentTwo = buildTextFile("two", 200);
            const documentThree = buildTextFile("three", 100);
            const uploadStateChangeTriggers = {};
            const resolvers = {};
            const triggerUploadStateChange = (document, state, progress) => {
                act(() => {
                    uploadStateChangeTriggers[document.name](state, progress);
                });
            };
            const resolveDocumentUploadPromise = (document) => {
                act(() => {
                    resolvers[document.name]();
                });
            };

            useNavigate.mockImplementation(() => navigateMock);
            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            useAuthorisedDocumentStore.mockReturnValue({ uploadDocument: uploadDocumentMock });
            uploadDocumentMock.mockImplementation(async (document, uploadNhsNumber, onUploadStateChange) => {
                expect(uploadNhsNumber).toBe(nhsNumber);
                uploadStateChangeTriggers[document.name] = onUploadStateChange;

                return new Promise((resolve) => {
                    resolvers[document.name] = resolve;
                });
            });

            renderUploadDocumentsPage({ nextPagePath: "/next" });
            userEvent.upload(screen.getByLabelText("Select file(s)"), [documentOne, documentTwo, documentThree]);
            userEvent.click(screen.getByRole("button", { name: "Upload" }));

            expect(await screen.findByRole("button", { name: "Upload" })).toBeDisabled();

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 0);

            expect(screen.queryByTestId("upload-document-form")).not.toBeInTheDocument();
            expect(
                screen.getByText("Do not close or navigate away from this browser until upload is complete.")
            ).toBeInTheDocument();
            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentOne.name}` }).value)).toEqual(
                0
            );
            expect(screen.getByRole("status", { name: `${documentOne.name} upload status` }).textContent).toContain(
                "Uploading"
            );

            triggerUploadStateChange(documentTwo, documentUploadStates.UPLOADING, 0);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentTwo.name}` }).value)).toEqual(
                0
            );
            expect(screen.getByRole("status", { name: `${documentTwo.name} upload status` }).textContent).toContain(
                "Uploading"
            );

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 10);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentOne.name}` }).value)).toEqual(
                10
            );
            expect(screen.getByRole("status", { name: `${documentOne.name} upload status` }).textContent).toContain(
                "Uploading"
            );

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 70);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentOne.name}` }).value)).toEqual(
                70
            );
            expect(screen.getByRole("status", { name: `${documentOne.name} upload status` }).textContent).toContain(
                "Uploading"
            );

            triggerUploadStateChange(documentTwo, documentUploadStates.UPLOADING, 20);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentTwo.name}` }).value)).toEqual(
                20
            );
            expect(screen.getByRole("status", { name: `${documentTwo.name} upload status` }).textContent).toContain(
                "Uploading"
            );

            triggerUploadStateChange(documentTwo, documentUploadStates.SUCCEEDED, 100);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentTwo.name}` }).value)).toEqual(
                100
            );
            expect(screen.getByRole("status", { name: `${documentTwo.name} upload status` }).textContent).toContain(
                "Uploaded"
            );

            triggerUploadStateChange(documentThree, documentUploadStates.UPLOADING, 0);
            triggerUploadStateChange(documentOne, documentUploadStates.FAILED, 0);

            expect(parseInt(screen.getByRole("progressbar", { name: `Uploading ${documentOne.name}` }).value)).toEqual(
                0
            );
            expect(screen.getByRole("status", { name: `${documentOne.name} upload status` }).textContent).toContain(
                "failed"
            );

            triggerUploadStateChange(documentThree, documentUploadStates.SUCCEEDED, 100);
            resolveDocumentUploadPromise(documentOne);
            resolveDocumentUploadPromise(documentTwo);
            resolveDocumentUploadPromise(documentThree);

            expect(await screen.findByRole("heading", { name: "Upload Summary" })).toBeInTheDocument();

            userEvent.click(screen.getByLabelText("View successfully uploaded documents"));

            expect(screen.getByText(documentTwo.name)).toBeInTheDocument();
            expect(screen.getByText(documentThree.name)).toBeInTheDocument();
            expect(screen.getByRole("heading", { name: "There is a problem" })).toBeInTheDocument();
            expect(screen.getByText(`1 of 3 files failed to upload`));
            expect(screen.getByText("If you want to upload another patient's health record")).toBeInTheDocument();

            userEvent.click(screen.getByRole("button", { name: "Start Again" }));

            expect(navigateMock).toHaveBeenCalledWith("/next");
        });
    });

    describe("without NHS number", () => {
        it("redirects to patient trace page when the NHS number is unavailable", () => {
            const navigateMock = jest.fn();
            const unavailableNhsNumber = undefined;

            usePatientDetailsContext.mockReturnValue([unavailableNhsNumber, jest.fn()]);
            useNavigate.mockImplementation(() => navigateMock);

            renderUploadDocumentsPage();

            expect(navigateMock).toHaveBeenCalledWith(routes.UPLOAD_SEARCH_PATIENT);
        });

        it("redirects to the start page when the user is unauthorized to upload", async () => {
            const nhsNumber = "9000000009";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const navigateMock = jest.fn();
            const uploadDocumentMock = jest.fn();
            const documentOne = buildTextFile("one", 100);
            const uploadStateChangeTriggers = {};
            const resolvers = {};
            const triggerUploadStateChange = (document, state, progress) => {
                act(() => {
                    uploadStateChangeTriggers[document.name](state, progress);
                });
            };

            const setSessionMock = jest.fn();
            const session = { isLoggedIn: true };
            useNavigate.mockReturnValue(navigateMock);
            useSessionContext.mockReturnValue([session, setSessionMock]);
            usePatientDetailsContext.mockReturnValue([patientDetails, jest.fn()]);
            useAuthorisedDocumentStore.mockReturnValue({ uploadDocument: uploadDocumentMock });

            uploadDocumentMock.mockImplementation(async (document, uploadNhsNumber, onUploadStateChange) => {
                expect(uploadNhsNumber).toBe(nhsNumber);
                uploadStateChangeTriggers[document.name] = onUploadStateChange;

                return new Promise((resolve) => {
                    resolvers[document.name] = resolve;
                });
            });

            renderUploadDocumentsPage({ nextPagePath: "/next" });
            userEvent.upload(screen.getByLabelText("Select file(s)"), [documentOne]);
            userEvent.click(screen.getByRole("button", { name: "Upload" }));

            expect(await screen.findByRole("button", { name: "Upload" })).toBeDisabled();

            triggerUploadStateChange(documentOne, documentUploadStates.UNAUTHORISED, 0);
            await waitFor(() => {
                expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
            });
        });
    });
});

const renderUploadDocumentsPage = (propsOverride) => {
    const props = {
        nextPagePath: "some/path",
        ...propsOverride,
    };

    render(<UploadDocumentsPage {...props} />);
};
