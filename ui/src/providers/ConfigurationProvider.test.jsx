import { render, screen } from "@testing-library/react";
import ConfigurationProvider, { useFeatureToggle } from "./ConfigurationProvider";

describe("<ConfigurationProvider />", () => {
    const reactAppEnv = process.env.REACT_APP_ENV;
    const baseConfig = {
        API: {
            endpoints: [
                {
                    name: "test",
                    endpoint: "https://test.api/",
                },
            ],
        },
        features: {},
    };

    afterAll(() => {
        process.env.REACT_APP_ENV = reactAppEnv;
    });

    it("allows the consuming component to detect when a feature is active", () => {
        const config = {
            ...baseConfig,
            features: { development: { TEST: true } },
        };

        process.env.REACT_APP_ENV = "development";
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("active")).toBeInTheDocument();
    });

    it("allows the consuming component to detect when a feature is inactive", () => {
        const config = {
            ...baseConfig,
            features: { development: { TEST: false } },
        };

        process.env.REACT_APP_ENV = "development";
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when a toggle is undefined", () => {
        const config = {
            ...baseConfig,
            features: { development: { TEST: undefined } },
        };

        process.env.REACT_APP_ENV = "development";
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false when feature toggle are undefined for the current env", () => {
        process.env.REACT_APP_ENV = "non-existent-env";
        render(
            <ConfigurationProvider config={baseConfig}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when REACT_APP_ENV is undefined", () => {
        process.env.REACT_APP_ENV = undefined;
        render(
            <ConfigurationProvider config={baseConfig}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("inactive")).toBeInTheDocument();
    });
});

const TestComponent = () => {
    const featureToggle = useFeatureToggle("TEST");

    return featureToggle !== undefined && <>{featureToggle ? "active" : "inactive"}</>;
};
