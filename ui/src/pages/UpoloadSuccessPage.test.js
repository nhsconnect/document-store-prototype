import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import UploadSuccessPage from "./UploadSuccessPage";

const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

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

    it("redirects to home page when done button is clicked", () => {
        render(<UploadSuccessPage />);

        userEvent.click(screen.getByRole("button", { name: "Done" }));

        expect(mockNavigate).toHaveBeenCalledWith("/home");
    });
});
