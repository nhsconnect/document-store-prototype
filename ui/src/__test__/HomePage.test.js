import { render, screen } from "@testing-library/react";
import Home from "../pages/home";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";
// import { BrowserRouter } from "react-router-dom";

jest.mock("../providers/FeatureToggleProvider");

describe("The home page", () => {
        it("provides a link to the multi step upload path", () => {
            render(
                // <BrowserRouter>
                    <Home />
                // </BrowserRouter>
            );

            expect(screen.getByText("Upload a document")).toBeInTheDocument()
        });

        it("provides a link to the multi step search path", () => {
            render(
                // <BrowserRouter>
                    <Home />
                // </BrowserRouter>
            );

            expect(
                screen.getByText("Download and view a stored document")
            ).toBeInTheDocument()
        });
    });

