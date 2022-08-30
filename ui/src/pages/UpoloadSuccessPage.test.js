import { render, screen } from "@testing-library/react";
import UploadSuccessPage from "./UploadSuccessPage";

describe("UploadSuccessPage", () => {
    it("renders the page", () => {
        render(<UploadSuccessPage />);

        expect(
            screen.getByRole("heading", { name: "NHS Digital DocStore" })
        ).toBeInTheDocument();
        expect(
            screen.getByText("File uploaded successfully")
        ).toBeInTheDocument();
        expect(
            screen.getByRole("button", { name: "Done" })
        ).toBeInTheDocument();
    });
});
