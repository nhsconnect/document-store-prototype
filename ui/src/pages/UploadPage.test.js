import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";
import UploadPage from "./UploadPage";

jest.mock("../apiClients/apiClient");
jest.mock("../providers/FeatureToggleProvider");

describe("Upload page", () => {
    describe("SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE feature toggle is active", () => {
        beforeEach(() => {
            useFeatureToggle.mockImplementation(() => true);
        });
        afterEach(() => {
            useFeatureToggle.mockReset();
        });

        it("renders the page", () => {
            render(<UploadPage />);

            expect(
                screen.getByRole("heading", { name: "Upload Patient Records" })
            ).toBeInTheDocument();
            expect(
                screen.getByLabelText("Enter NHS number")
            ).toBeInTheDocument();
            expect(
                screen.getByLabelText("Enter Document Title")
            ).toBeInTheDocument();
            expect(
                screen.getByLabelText("Select Clinical Code")
            ).toBeInTheDocument();
            expect(
                screen.getByLabelText("Choose document")
            ).toBeInTheDocument();
            expect(screen.getByText("Upload")).toBeInTheDocument();
        });

        it("displays success message when a document is successfully uploaded", async () => {
            const apiClientMock = new ApiClient();
            const nhsNumber = "0987654321";
            const documentTitle = "Jane Doe - Patient Record";
            const snomedCode = "22151000087106";
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadPage client={apiClientMock} />);

            userEvent.type(
                screen.getByLabelText("Enter NHS number"),
                nhsNumber
            );
            userEvent.selectOptions(
                screen.getByLabelText("Select Clinical Code"),
                snomedCode
            );
            userEvent.type(
                screen.getByLabelText("Enter Document Title"),
                documentTitle
            );
            userEvent.upload(
                screen.getByLabelText("Choose document"),
                document
            );
            userEvent.click(screen.getByText("Upload"));

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
            const nhsNumber = "0987654321";
            const documentTitle = "Jane Doe - Patient Record";
            const snomedCode = "22151000087106";
            apiClientMock.uploadDocument = jest.fn((document) => {
                throw new Error();
            });
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadPage client={apiClientMock} />);

            userEvent.type(
                screen.getByLabelText("Enter NHS number"),
                nhsNumber
            );
            userEvent.type(
                screen.getByLabelText("Enter Document Title"),
                documentTitle
            );
            userEvent.selectOptions(
                screen.getByLabelText("Select Clinical Code"),
                snomedCode
            );
            userEvent.upload(
                screen.getByLabelText("Choose document"),
                document
            );
            userEvent.click(screen.getByText("Upload"));

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
            const nhsNumber = "0987654321";
            const documentTitle = "Jane Doe - Patient Record";
            const snomedCode = "22151000087106";
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadPage client={apiClientMock} />);

            userEvent.type(
                screen.getByLabelText("Enter NHS number"),
                nhsNumber
            );
            userEvent.type(
                screen.getByLabelText("Enter Document Title"),
                documentTitle
            );
            userEvent.selectOptions(
                screen.getByLabelText("Select Clinical Code"),
                snomedCode
            );
            userEvent.upload(
                screen.getByLabelText("Choose document"),
                document
            );
            userEvent.click(screen.getByText("Upload"));

            await waitFor(() => {
                expect(screen.getByRole("progressbar")).toBeInTheDocument();
            });
        });

        it("does not upload documents of size greater than 5GB and displays an error", async () => {
            const apiClientMock = new ApiClient();
            const nhsNumber = "0987654321";
            const documentTitle = "Jane Doe - Patient Record";
            const snomedCode = "22151000087106";
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            Object.defineProperty(document, "size", {
                value: 5 * 107374184 + 1,
            });
            render(<UploadPage client={apiClientMock} />);

            userEvent.type(
                screen.getByLabelText("Enter NHS number"),
                nhsNumber
            );
            userEvent.type(
                screen.getByLabelText("Enter Document Title"),
                documentTitle
            );
            userEvent.selectOptions(
                screen.getByLabelText("Select Clinical Code"),
                snomedCode
            );
            userEvent.upload(
                screen.getByLabelText("Choose document"),
                document
            );
            userEvent.click(screen.getByText("Upload"));

            await waitFor(() => {
                expect(
                    screen.getByText(
                        "File size greater than 5GB - upload a smaller file"
                    )
                ).toBeInTheDocument();
            });
            expect(apiClientMock.uploadDocument).not.toHaveBeenCalled();
        });
    });

    describe("SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE feature toggle is inactive", () => {
        beforeEach(() => {
            useFeatureToggle.mockImplementation(() => false);
        });
        afterEach(() => {
            useFeatureToggle.mockReset();
        });

        it("renders the page", () => {
            render(<UploadPage />);

            expect(
                screen.getByRole("heading", { name: "Upload Patient Records" })
            ).toBeInTheDocument();
            expect(
                screen.getByLabelText("Enter NHS number")
            ).toBeInTheDocument();
            expect(
                screen.queryByLabelText("Enter Document Title")
            ).not.toBeInTheDocument();
            expect(
                screen.queryByLabelText("Select Clinical Code")
            ).not.toBeInTheDocument();
            expect(
                screen.getByLabelText("Choose document")
            ).toBeInTheDocument();
            expect(screen.getByText("Upload")).toBeInTheDocument();
        });

        it("displays success message when a document is successfully uploaded", async () => {
            const apiClientMock = new ApiClient();
            const nhsNumber = "0987654321";
            const document = new File(["hello"], "hello.txt", {
                type: "text/plain",
            });
            render(<UploadPage client={apiClientMock} />);

            userEvent.type(
                screen.getByLabelText("Enter NHS number"),
                nhsNumber
            );
            userEvent.upload(
                screen.getByLabelText("Choose document"),
                document
            );
            userEvent.click(screen.getByText("Upload"));

            await waitFor(() => {
                expect(
                    screen.getByText("Document uploaded successfully")
                ).toBeInTheDocument();
            });
            expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(
                document,
                nhsNumber,
                "Jane Doe - Patient Record",
                "22151000087106"
            );
        });
    });
});
