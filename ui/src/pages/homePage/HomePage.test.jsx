import { render, screen, waitFor, within } from "@testing-library/react";
import HomePage from "./HomePage";
import userEvent from "@testing-library/user-event";
import { useNavigate } from "react-router";

jest.mock("react-router");

describe("<HomePage />", () => {
    it("renders the page", () => {
        render(<HomePage />);

        expect(screen.getByText("Back")).toBeInTheDocument();
        const form = within(screen.getByRole("group", { name: "How do you want to use the service?" }));
        expect(form.getByRole("heading", { name: "How do you want to use the service?" })).toBeInTheDocument();
        expect(form.getByText("Select an option")).toBeInTheDocument();
        expect(form.getByRole("radio", { name: "Upload documents" })).toBeInTheDocument();
        expect(form.getByRole("radio", { name: "Download and/or delete documents" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
    });

    it("no radio buttons selected by default", () => {
        render(<HomePage />);

        expect(screen.getByRole("radio", { name: "Upload documents" })).not.toBeChecked();
        expect(screen.getByRole("radio", { name: "Download and/or delete documents" })).not.toBeChecked();
    });

    it.each([
        ["upload", "Upload documents"],
        ["search", "Download and/or delete documents"],
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
