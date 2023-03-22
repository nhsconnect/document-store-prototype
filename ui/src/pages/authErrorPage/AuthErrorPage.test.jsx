import AuthErrorPage from "./AuthErrorPage";
import { render } from "@testing-library/react";

describe("authErrorPage", () => {
    it("renders the error page", () => {
        render(<AuthErrorPage />);
    });
});
