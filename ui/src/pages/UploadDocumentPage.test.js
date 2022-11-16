import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
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
    describe("when there is an NHS number", () => {
        const nhsNumber = "1112223334";
        beforeEach(() => {
            useNhsNumberProviderContext.mockReturnValue([nhsNumber, jest.fn()]);
        });

        it("renders the page", () => {
            render(<UploadDocumentPage />);

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

        it('can upload multiple documents', async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(async () => {
                return new Promise((resolve) => {
                    resolve(null);
                })
            });
            const documentOne = new File(["one"], "one.txt", {
                type: "text/plain",
            });
            const documentTwo = new File(["two"], "two.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocuments([documentOne, documentTwo]);
            uploadDocument();

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith("/upload/success");
            });

            expect(apiClientMock.uploadDocument).toHaveBeenCalledTimes(2)
        })

        it("navigates to a success page when a document is successfully uploaded", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(async () => {
                return new Promise((resolve) => {
                    resolve(null);
                })
            });
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocuments(document);
            uploadDocument();

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith("/upload/success");
            });
        });

        it("displays an error message for each document that fails to upload", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(async () => {
                return new Promise ((resolve, reject) => {
                    setTimeout(() => {reject('Something went wrong')}, 10)
                })
            });
            const documentOne = new File(["one"], "one.txt", {
                type: "text/plain",
            });
            const documentTwo = new File(["two"], "two.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocuments([documentOne, documentTwo]);
            uploadDocument();

            expect(
                await screen.findByText(`Upload of ${documentOne.name} failed - please retry`)
            ).toBeInTheDocument();
            expect(
                screen.getByText(`Upload of ${documentTwo.name} failed - please retry`)
            ).toBeInTheDocument();
            expect(
                screen.getByText("Some of your documents failed to upload")
            ).toBeInTheDocument()
        });

        it("displays a loading spinner and disables the upload button when the document is being uploaded", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(async () => {
                return new Promise((resolve) => {
                    resolve(null);
                })
            });
            
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocuments(document);
            uploadDocument();

            await waitFor(() => {
                expect(uploadButton()).toBeDisabled();
                expect(progressBar()).toBeInTheDocument();
            });
        });

        it("clears existing error messages when the form is submitted again", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(async () => {
                return new Promise ((resolve, reject) => {
                    setTimeout(() => {reject('Something went wrong')}, 10)
                })
            });
            const documentOne = new File(["one"], "one.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocuments(documentOne);
            uploadDocument();

            expect(
                await screen.findByText(`Upload of ${documentOne.name} failed - please retry`)
            ).toBeInTheDocument();

            uploadDocument();

            await waitFor(() => {
                expect(uploadButton()).toBeDisabled()
            })

            expect(screen.queryByText(`Upload of ${documentOne.name} failed - please retry`)).toBeNull()

            expect(
                await screen.findByText(`Upload of ${documentOne.name} failed - please retry`)
            ).toBeInTheDocument();
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

function chooseDocuments(documents) {
    userEvent.upload(screen.getByLabelText("Choose documents"), documents);
}



function uploadDocument() {
    userEvent.click(uploadButton());
}


function progressBar() {
    return screen.getByRole("progressbar");
}

function uploadButton() {
    return screen.getByText("Upload");
}


function nhsNumberField() {
    return screen.getByLabelText("NHS number");
}
