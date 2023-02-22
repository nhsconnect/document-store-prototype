import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { act } from "react-dom/test-utils";
import "../../apiClients/documentStore";
import { documentUploadStates } from "../../enums/documentUploads";
import { usePatientDetailsProviderContext } from "../../providers/PatientDetailsProvider";
import UploadDocumentPage from "./UploadDocumentPage";

const mockDocumentStore = {
    uploadDocument: () => null,
};

jest.mock("../../apiClients/documentStore", () => {
    return {
        useDocumentStore: () => mockDocumentStore,
    };
});

jest.mock("../../providers/PatientDetailsProvider", () => ({
    usePatientDetailsProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

describe("UploadDocumentPage", () => {
    const nextPagePath = "/next";

    describe("when there is an NHS number", () => {
        const nhsNumber = "9000000009";
        const patientData = {
            birthDate: "2010-10-22",
            familyName: "Smith",
            givenName: ["Jane"],
            nhsNumber: nhsNumber,
            postalCode: "LS1 6AE",
        };

        beforeEach(() => {
            jest.resetAllMocks();
            usePatientDetailsProviderContext.mockReturnValue([patientData, jest.fn()]);
        });

        it("renders the page", () => {
            render(<UploadDocumentPage nextPagePath={nextPagePath} />);

            expect(screen.getByRole("heading", { name: "Upload documents" })).toBeInTheDocument();
            expect(screen.getByText(nhsNumber)).toBeInTheDocument();
            expect(screen.getByLabelText("Select file(s)")).toBeInTheDocument();
            expect(uploadButton()).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("uploads documents and displays the progress", async () => {
            const uploadStateChangeTriggers = {};
            const resolvers = {};

            mockDocumentStore.uploadDocument = async (document, uploadNhsNumber, onUploadStateChange) => {
                expect(uploadNhsNumber).toBe(nhsNumber);
                uploadStateChangeTriggers[document.name] = onUploadStateChange;

                return new Promise((resolve) => {
                    resolvers[document.name] = resolve;
                });
            };

            render(<UploadDocumentPage nextPagePath={nextPagePath} />);

            const documentOne = makeTextFile("one", 100);
            const documentTwo = makeTextFile("two", 200);
            const documentThree = makeTextFile("three", 100);

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

            chooseDocuments([documentOne, documentTwo, documentThree]);
            uploadDocument();

            await waitFor(() => {
                expect(uploadButton()).toBeDisabled();
            });

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 0);

            await waitFor(() => {
                expect(uploadForm()).not.toBeInTheDocument();
            });

            expect(
                screen.getByText("Do not close or navigate away from this browser until upload is complete.")
            ).toBeInTheDocument();

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(0);
                expect(getProgressBarMessage(documentOne).textContent).toContain("Uploading");
            });

            triggerUploadStateChange(documentTwo, documentUploadStates.UPLOADING, 0);

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(0);
                expect(getProgressBarMessage(documentTwo).textContent).toContain("Uploading");
            });

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 10);

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(10);
                expect(getProgressBarMessage(documentOne).textContent).toContain("Uploading");
            });

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 70);

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(70);
                expect(getProgressBarMessage(documentOne).textContent).toContain("Uploading");
            });

            triggerUploadStateChange(documentTwo, documentUploadStates.UPLOADING, 20);

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(20);
                expect(getProgressBarMessage(documentTwo).textContent).toContain("Uploading");
            });

            triggerUploadStateChange(documentTwo, documentUploadStates.SUCCEEDED, 100);

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(100);
                expect(getProgressBarMessage(documentTwo).textContent).toContain("Uploaded");
            });

            // Make sure document three is waiting, otherwise the "upload step" will move to complete once one and two have succeeded and failed
            triggerUploadStateChange(documentThree, documentUploadStates.UPLOADING, 0);
            triggerUploadStateChange(documentOne, documentUploadStates.FAILED, 0);

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(0);
                expect(getProgressBarMessage(documentOne).textContent).toContain("failed");
            });

            // Now we want to complete document three to move to the "complete" upload step
            triggerUploadStateChange(documentThree, documentUploadStates.SUCCEEDED, 100);

            resolveDocumentUploadPromise(documentOne);
            resolveDocumentUploadPromise(documentTwo);
            resolveDocumentUploadPromise(documentThree);

            await waitFor(() => {
                expect(screen.getByText("Upload Summary")).toBeInTheDocument();
            });

            userEvent.click(screen.getByLabelText("View successfully uploaded documents"));

            expect(await screen.findByText(documentTwo.name)).toBeInTheDocument();
            expect(screen.getByText(documentThree.name)).toBeInTheDocument();

            expect(screen.getByText("Some of your documents failed to upload")).toBeInTheDocument();
            expect(screen.getByRole("table", { name: "Failed uploads" })).toBeInTheDocument();
            expect(screen.getByText("If you want to upload another patient's health record")).toBeInTheDocument();

            userEvent.click(screen.getByRole("button", { name: "Start Again" }));

            expect(mockNavigate).toHaveBeenCalledWith(nextPagePath);
        });
    });

    describe("when there is NOT an NHS number", () => {
        beforeEach(() => {
            usePatientDetailsProviderContext.mockReturnValue([undefined, jest.fn()]);
        });
        it("redirects to patient trace page when the NHS number is NOT available", () => {
            render(<UploadDocumentPage />);

            expect(mockNavigate).toHaveBeenCalledWith("/upload/patient-trace");
        });
    });
});

function makeTextFile(name, size) {
    const file = new File(["test"], `${name}.txt`, {
        type: "text/plain",
    });
    if (size) {
        Object.defineProperty(file, "size", {
            value: size,
        });
    }
    return file;
}

function chooseDocuments(documents) {
    userEvent.upload(screen.getByLabelText("Select file(s)"), documents);
}

function uploadDocument() {
    userEvent.click(uploadButton());
}

function uploadForm() {
    return screen.queryByTestId("upload-document-form");
}

function uploadButton() {
    return screen.getByText("Upload");
}

const getProgressBar = (document) => screen.getByRole("progressbar", { name: `Uploading ${document.name}` });
const getProgressBarMessage = (document) => screen.getByRole("status", { name: `${document.name} upload status` });
const getProgressBarValue = (document) => parseInt(getProgressBar(document).value);