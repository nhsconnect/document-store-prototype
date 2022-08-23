import { render, screen } from "@testing-library/react";
import HomePage from "./HomePage";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";
import { BrowserRouter } from "react-router-dom";

jest.mock("../providers/FeatureToggleProvider");

describe("The home page", () => {
    describe("the PDS_TRACE_ENABLED toggle is active", () => {
        it("provides a link to the multi step upload path", () => {
            useFeatureToggle.mockImplementation((toggleName) => {
                if (toggleName === "PDS_TRACE_ENABLED") {
                    return true;
                }
            });
            render(
                <BrowserRouter>
                    <HomePage />
                </BrowserRouter>
            );

            expect(screen.getByText("Upload Patient Record")).toHaveAttribute(
                "href",
                "/upload/patient-trace"
            );
        });
    });

    describe("the PDS_TRACE_ENABLED toggle is inactive", () => {
        it("provides a link to the single step upload path", () => {
            useFeatureToggle.mockImplementation((toggleName) => {
                if (toggleName === "PDS_TRACE_ENABLED") {
                    return false;
                }
            });
            render(
                <BrowserRouter>
                    <HomePage />
                </BrowserRouter>
            );

            expect(screen.getByText("Upload Patient Record")).toHaveAttribute(
                "href",
                "/upload"
            );
        });
    });
});
