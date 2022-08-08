import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import UploadPage from "./UploadPage";

jest.mock("../apiClients/apiClient");

describe("Upload page", () => {
    it("renders the page", () => {
        render(<UploadPage />);

        expect(
            screen.getByRole("heading", { name: "Upload Patient Records" })
        ).toBeInTheDocument();
        screen.getByLabelText("Enter NHS number");
        // expect(screen.getByLabelText("Document Title")).toBeInTheDocument();
        expect(screen.getByLabelText("Choose document")).toBeInTheDocument();
        expect(screen.getByText("Upload")).toBeInTheDocument();
    });

    it("displays success message when a document is successfully uploaded", async () => {
        const apiClientMock = new ApiClient();
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadPage client={apiClientMock} />);
        userEvent.upload(screen.getByLabelText("Choose document"), document);
        userEvent.click(screen.getByText("Upload"));

        await waitFor(() => {
            expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(document);
        });
        expect(
            screen.getByText("Document uploaded successfully")
        ).toBeInTheDocument();
    });

    it("displays an error message when the document fails to upload", async() => {
        const apiClientMock = new ApiClient();
        apiClientMock.uploadDocument = jest.fn((document) => {
            throw new Error;
        })
        const document = new File(["hello"], "hello.txt",{
            type: "text/plain",
        });
        render(<UploadPage client={apiClientMock} />);
        userEvent.upload(screen.getByLabelText("Choose document"), document);
        userEvent.click(screen.getByText("Upload"));
        await waitFor(() => {
            expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(document);
        });
        expect(screen.getByText("File upload failed - please retry")).toBeInTheDocument();
    })

    it("displays a loading spinner when the document is being uploaded", async () => {
        const apiClientMock = new ApiClient();
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadPage client={apiClientMock} />);
        userEvent.upload(screen.getByLabelText("Choose document"), document);
        userEvent.click(screen.getByText("Upload"));
        await waitFor(() => {
            expect(
                screen.getByRole("progressbar")
            ).toBeInTheDocument()
        })
        });
});
