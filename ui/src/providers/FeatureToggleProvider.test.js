import { render, screen } from "@testing-library/react";
import FeatureToggleProvider, {
    useFeatureToggle,
} from "./FeatureToggleProvider";

const TestComponent = () => {
    const featureToggle = useFeatureToggle("TEST");
    if (featureToggle === true) return "active";
    else if (featureToggle === false) return "inactive";
    else return "undefined";
};

describe("The feature toggle provider", () => {
    let env;
    beforeAll(() => {
        env = process.env.REACT_APP_ENV;
    });
    afterAll(() => {
        process.env.REACT_APP_ENV = env;
    });

    it("allows the consuming component to detect when a feature is active", () => {
        process.env.REACT_APP_ENV = "development";
        const config = { features: { development: { TEST: true } } };
        render(
            <FeatureToggleProvider config={config}>
                <TestComponent />
            </FeatureToggleProvider>
        );

        expect(screen.getByText("active")).toBeInTheDocument();
    });

    it("allows the consuming component to detect when a feature is inactive", () => {
        process.env.REACT_APP_ENV = "development";
        const config = { features: { development: { TEST: false } } };
        render(
            <FeatureToggleProvider config={config}>
                <TestComponent />
            </FeatureToggleProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when a toggle is not defined", () => {
        process.env.REACT_APP_ENV = "development";
        const config = { features: { development: { TEST: undefined } } };
        render(
            <FeatureToggleProvider config={config}>
                <TestComponent />
            </FeatureToggleProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false when feature toggles are not defined for the current environment", () => {
        process.env.REACT_APP_ENV = "non-existent-env";
        render(
            <FeatureToggleProvider>
                <TestComponent />
            </FeatureToggleProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when REACT_APP_ENV is not defined", () => {
        process.env.REACT_APP_ENV = undefined;
        const config = { features: { development: { TEST: undefined } } };
        render(
            <FeatureToggleProvider config={config}>
                <TestComponent />
            </FeatureToggleProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });
});
