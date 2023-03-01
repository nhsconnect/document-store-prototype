import { render, screen, waitFor, within } from "@testing-library/react";
import HomePage from "./HomePage";
import userEvent from "@testing-library/user-event";
import { useNavigate } from "react-router";

jest.mock("react-router");

describe("<HomePage />", () => {
    it("renders the page", () => {
        render(<HomePage />);

        expect(screen.getByText("Back")).toBeInTheDocument();
        const form = within(screen.getByRole("group", { name: "How do you want to use the Document Store?" }));
        expect(form.getByRole("heading", { name: "How do you want to use the Document Store?" })).toBeInTheDocument();
        expect(form.getByText("Select an option")).toBeInTheDocument();
        expect(form.getByRole("radio", { name: "Upload a document" })).toBeInTheDocument();
        expect(form.getByRole("radio", { name: "Download and view a stored document" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
    });

    it("no radio buttons selected by default", () => {
        render(<HomePage />);

        expect(screen.getByRole("radio", { name: "Upload a document" })).not.toBeChecked();
        expect(screen.getByRole("radio", { name: "Download and view a stored document" })).not.toBeChecked();
    });

    it.each([
        ["upload", "Upload a document"],
        ["search", "Download and view a stored document"],
    ])("navigates to %s path when '%s' selected", async (path, radioButtonName) => {
        const navigateMock = jest.fn();

        useNavigate.mockImplementation(() => navigateMock);

        render(<HomePage />);
        userEvent.click(screen.getByRole("radio", { name: radioButtonName }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(navigateMock).toHaveBeenCalledWith(`/${path}/patient-trace`, { replace: false });
        });
    });

    it("disables continue button if nothing is selected", async () => {
        render(<HomePage />);

        expect(screen.getByRole("button", { name: "Continue" })).toBeDisabled();
    });
});
