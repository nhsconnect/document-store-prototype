import { render, screen, waitFor,within} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { act } from "react-dom/test-utils";

import ApiClient from "../apiClients/apiClient";
import { documentUploadStates } from "../enums/documentUploads";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import UploadDocumentPage from "./UploadDocumentPage";

jest.mock("../apiClients/apiClient");
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: jest.fn(),
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

beforeEach(() => {
    ApiClient.mockReset()
})

describe("UploadDocumentPage", () => {
    const nextPagePath = "/next"
    

    describe("when there is an NHS number", () => {
        const nhsNumber = "1112223334";
        beforeEach(() => {
            jest.resetAllMocks()
            useNhsNumberProviderContext.mockReturnValue([nhsNumber, jest.fn()]);
        });

        it("renders the page", () => {
            render(<UploadDocumentPage nextPagePath={nextPagePath} />);

            expect(
                screen.getByRole("heading", { name: "Upload a document" })
            ).toBeInTheDocument();
            expect(nhsNumberField()).toBeInTheDocument();
            expect(nhsNumberField()).toHaveValue(nhsNumber);
            expect(nhsNumberField()).toHaveAttribute("readonly");
            expect(
                screen.getByLabelText("Choose documents")
            ).toBeInTheDocument();
            expect(uploadButton()).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("uploads documents and displays the progress", async () => {
            const apiClientMock = new ApiClient();

            const uploadStateChangeTriggers = {};
            const resolvers = {}
            
            apiClientMock.uploadDocument = async (document, nhsNumber, onUploadStateChange) => {
                uploadStateChangeTriggers[document.name] = onUploadStateChange

                return new Promise((resolve) => {
                    resolvers[document.name] = resolve
                })
            };

            render(<UploadDocumentPage client={apiClientMock} nextPagePath={nextPagePath} />);

            const documentOne = makeTextFile("one", 100);
            const documentTwo = makeTextFile("two", 200);
            const documentThree = makeTextFile("three", 100);

            const triggerUploadStateChange = (document, state, progress) => {
                act(() => {
                    uploadStateChangeTriggers[document.name](state, progress)
                })
            }

            const resolveDocumentUploadPromise = (document) => {
                act(() => {
                    resolvers[document.name]()
                })
            }

            chooseDocuments([documentOne, documentTwo, documentThree]);
            uploadDocument();

            await waitFor(() => {
                expect(uploadButton()).toBeDisabled()
            })

            triggerUploadStateChange(documentOne, documentUploadStates.WAITING, 0)

            await waitFor(() => {
                expect(uploadForm()).not.toBeInTheDocument();
            });

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(0)
                expect(getProgressBarMessage(documentOne).textContent).toContain("Waiting")
            })

            triggerUploadStateChange(documentTwo, documentUploadStates.WAITING, 0);

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(0)
                expect(getProgressBarMessage(documentTwo).textContent).toContain("Waiting")
            })

            triggerUploadStateChange(documentTwo, documentUploadStates.STORING_METADATA, 0);

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(0)
                expect(getProgressBarMessage(documentTwo).textContent).toContain("metadata")
            })

            triggerUploadStateChange(documentOne, documentUploadStates.STORING_METADATA, 0)

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(0)
                expect(getProgressBarMessage(documentOne).textContent).toContain("metadata")
            })

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 10)

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(10)
                expect(getProgressBarMessage(documentOne).textContent).toContain("Uploading")
            })

            triggerUploadStateChange(documentOne, documentUploadStates.UPLOADING, 70)

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(70)
                expect(getProgressBarMessage(documentOne).textContent).toContain("Uploading")
            })

            triggerUploadStateChange(documentTwo, documentUploadStates.UPLOADING, 20)

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(20)
                expect(getProgressBarMessage(documentTwo).textContent).toContain("Uploading")
            })

            triggerUploadStateChange(documentTwo, documentUploadStates.SUCCEEDED, 100)

            await waitFor(() => {
                expect(getProgressBarValue(documentTwo)).toEqual(100)
                expect(getProgressBarMessage(documentTwo).textContent).toContain("successful")
            })

            // Make sure document three is waiting, otherwise the "upload step" will move to complete once one and two have succeeded and failed
            triggerUploadStateChange(documentThree, documentUploadStates.WAITING, 0)
            triggerUploadStateChange(documentOne, documentUploadStates.FAILED, 0)

            await waitFor(() => {
                expect(getProgressBarValue(documentOne)).toEqual(0)
                expect(getProgressBarMessage(documentOne).textContent).toContain("failed")
            })

            // Now we want to complete document three to move to the "complete" upload step
            triggerUploadStateChange(documentThree, documentUploadStates.SUCCEEDED, 100)

            resolveDocumentUploadPromise(documentOne)
            resolveDocumentUploadPromise(documentTwo)
            resolveDocumentUploadPromise(documentThree)

            await waitFor(() => {
                expect(screen.getByText("Upload Summary")).toBeInTheDocument();
            });

            expect(screen.getByText(`Summary of uploaded documents for patient number ${nhsNumber}`))

            userEvent.click(screen.getByText("Successfully uploaded documents"))

            expect(await screen.findByText(documentTwo.name)).toBeInTheDocument()
            expect(screen.getByText(documentThree.name)).toBeInTheDocument()

            expect(screen.getByText("Some of your documents could not be uploaded")).toBeInTheDocument()
            expect(within(screen.getByRole("alert")).getByText(documentOne.name)).toBeInTheDocument()

            userEvent.click(screen.getByRole("button", { name: "Finish" }))

            expect(mockNavigate).toHaveBeenCalledWith(nextPagePath)

        })
    });

    describe("when there is NOT an NHS number", () => {
        beforeEach(() => {
            useNhsNumberProviderContext.mockReturnValue([undefined, jest.fn()]);
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
        })
    }
    return file;
}

function chooseDocuments(documents) {
    userEvent.upload(screen.getByLabelText("Choose documents"), documents);
}



function uploadDocument() {
    userEvent.click(uploadButton());
}

function uploadForm() {
    return screen.queryByTestId("upload-document-form")
}

function uploadButton() {
    return screen.getByText("Upload");
}


function nhsNumberField() {
    return screen.getByLabelText("NHS number");
}

const getProgressBar = (document) => screen.getByRole("progressbar", { name: `Uploading ${document.name}` })
const getProgressBarMessage = (document) => screen.getByRole("status", { name: `${document.name} upload status` })
const getProgressBarValue = (document) => parseInt(getProgressBar(document).value);
