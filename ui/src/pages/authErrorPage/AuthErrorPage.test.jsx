import AuthErrorPage from "./AuthErrorPage";
import { render, screen } from "@testing-library/react";

describe("AuthErrorPage", () => {
    it("renders the error page", () => {
        render(<AuthErrorPage />);

        expect(screen.getByRole("heading", { name: "Authorisation Error" })).toBeInTheDocument();
    });
});
