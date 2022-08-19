import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import { useMultiStepUploadProviderContext } from "../providers/MultiStepUploadProvider";
import UploadDocumentPage from "./UploadDocumentPage";

jest.mock("../apiClients/apiClient");
jest.mock("../providers/MultiStepUploadProvider", () => ({
    useMultiStepUploadProviderContext: jest.fn(),
}));

describe("UploadDocumentPage", () => {
    const nhsNumber = "1112223334";
    beforeEach(() => {
        useMultiStepUploadProviderContext.mockReturnValue([
            nhsNumber,
            jest.fn(),
        ]);
    });

    it("renders the page when the NHS number is available", () => {
        render(<UploadDocumentPage />);

        expect(
            screen.getByRole("heading", { name: "Upload Patient Records" })
        ).toBeInTheDocument();
        expect(nhsNumberField()).toBeInTheDocument();
        expect(nhsNumberField()).toHaveValue(nhsNumber);
        expect(documentTitleField()).toBeInTheDocument();
        expect(clinicalCodeSelector()).toBeInTheDocument();
        expect(screen.getByLabelText("Choose document")).toBeInTheDocument();
        expect(uploadButton()).toBeInTheDocument();
    });

    it("displays success message when a document is successfully uploaded", async () => {
        const apiClientMock = new ApiClient();
        const documentTitle = "Jane Doe - Patient Record";
        const snomedCode = "22151000087106";
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadDocumentPage client={apiClientMock} />);

        selectClinicalCode(snomedCode);
        enterTitle(documentTitle);
        chooseDocument(document);
        uploadDocument();

        await waitFor(() => {
            expect(
                screen.getByText("Document uploaded successfully")
            ).toBeInTheDocument();
        });
        expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(
            document,
            nhsNumber,
            documentTitle,
            snomedCode
        );
    });

    it("displays an error message when the document fails to upload", async () => {
        const apiClientMock = new ApiClient();
        const documentTitle = "Jane Doe - Patient Record";
        const snomedCode = "22151000087106";
        apiClientMock.uploadDocument = jest.fn(() => {
            throw new Error();
        });
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadDocumentPage client={apiClientMock} />);

        enterTitle(documentTitle);
        selectClinicalCode(snomedCode);
        chooseDocument(document);
        uploadDocument();

        await waitFor(() => {
            expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(
                document,
                nhsNumber,
                documentTitle,
                snomedCode
            );
        });
        expect(
            screen.getByText("File upload failed - please retry")
        ).toBeInTheDocument();
    });

    it("displays a loading spinner when the document is being uploaded", async () => {
        const apiClientMock = new ApiClient();
        const documentTitle = "Jane Doe - Patient Record";
        const snomedCode = "22151000087106";
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadDocumentPage client={apiClientMock} />);

        enterTitle(documentTitle);
        selectClinicalCode(snomedCode);
        chooseDocument(document);
        uploadDocument();

        await waitFor(() => {
            expect(progressBar()).toBeInTheDocument();
        });
    });

    it("does not upload documents of size greater than 5GB and displays an error", async () => {
        const apiClientMock = new ApiClient();
        const documentTitle = "Jane Doe - Patient Record";
        const snomedCode = "22151000087106";
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        Object.defineProperty(document, "size", {
            value: 5 * 107374184 + 1,
        });
        render(<UploadDocumentPage client={apiClientMock} />);

        enterTitle(documentTitle);
        selectClinicalCode(snomedCode);
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
        const snomedCode = "22151000087106";
        render(<UploadDocumentPage client={apiClientMock} />);

        selectClinicalCode(snomedCode);
        uploadDocument();

        await waitFor(() => {
            expect(
                screen.getByText("Please enter document title")
            ).toBeInTheDocument();
            expect(
                screen.getByText("Please attach a file")
            ).toBeInTheDocument();
        });
        expect(apiClientMock.uploadDocument).not.toHaveBeenCalled();
    });
});

function chooseDocument(document) {
    userEvent.upload(screen.getByLabelText("Choose document"), document);
}

function selectClinicalCode(snomedCode) {
    userEvent.selectOptions(clinicalCodeSelector(), snomedCode);
}

function enterTitle(documentTitle) {
    userEvent.type(documentTitleField(), documentTitle);
}

function uploadDocument() {
    userEvent.click(uploadButton());
}

function clinicalCodeSelector() {
    return screen.getByLabelText("Select Clinical Code");
}

function progressBar() {
    return screen.getByRole("progressbar");
}

function uploadButton() {
    return screen.getByText("Upload");
}

function documentTitleField() {
    return screen.getByLabelText("Enter Document Title");
}

function nhsNumberField() {
    return screen.getByLabelText("Enter NHS number");
}
