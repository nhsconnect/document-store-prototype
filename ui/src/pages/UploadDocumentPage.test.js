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
                screen.getByLabelText("Choose document")
            ).toBeInTheDocument();
            expect(uploadButton()).toBeInTheDocument();
            expect(mockNavigate).not.toHaveBeenCalled();
        });

        it("navigates to a success page when a document is successfully uploaded", async () => {
            const apiClientMock = new ApiClient();
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocument(document);
            uploadDocument();

            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith("/upload/success");
            });
        });

        it("displays an error message when the document fails to upload", async () => {
            const apiClientMock = new ApiClient();
            apiClientMock.uploadDocument = jest.fn(() => {
                throw new Error();
            });
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocument(document);
            uploadDocument();

            await waitFor(() => {
                expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(
                    document,
                    nhsNumber
                );
            });
            expect(
                screen.getByText("File upload failed - please retry")
            ).toBeInTheDocument();
        });

        it("displays a loading spinner and disables the upload button when the document is being uploaded", async () => {
            const apiClientMock = new ApiClient();
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadDocumentPage client={apiClientMock} />);


            chooseDocument(document);
            uploadDocument();

            await waitFor(() => {
                expect(uploadButton()).toBeDisabled();
                expect(progressBar()).toBeInTheDocument();
            });
        });

        it("does not upload documents of size greater than 5GB and displays an error", async () => {
            const apiClientMock = new ApiClient();
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            Object.defineProperty(document, "size", {
                value: 5 * 107374184 + 1,
            });
            render(<UploadDocumentPage client={apiClientMock} />);

            chooseDocument(document);
            uploadDocument();

            await waitFor(() => {
                expect(
                    screen.getByText(
                        "File size greater than 5GB - upload a smaller file"
                    )
                ).toBeInTheDocument();
            });
            expect(apiClientMock.uploadDocument).not.toHaveBeenCalled();
        });

        it("displays an error message when the form is submitted if the required fields are missing", async () => {
            const apiClientMock = new ApiClient();
            render(<UploadDocumentPage client={apiClientMock} />);

            uploadDocument();

            await waitFor(() => {
                expect(
                    screen.getByText("Please attach a file")
                ).toBeInTheDocument();
            });
            expect(apiClientMock.uploadDocument).not.toHaveBeenCalled();
        });
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

function chooseDocument(document) {
    userEvent.upload(screen.getByLabelText("Choose document"), document);
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
