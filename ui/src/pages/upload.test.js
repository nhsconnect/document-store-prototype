import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import UploadPage from "./upload";

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

    it("uploads a document when the upload button is clicked", () => {
        const apiClientMock = new ApiClient();
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        render(<UploadPage client={apiClientMock} />);
        userEvent.upload(screen.getByLabelText("Choose document"), document);
        userEvent.click(screen.getByText("Upload"));

        expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(document);
    });
});
