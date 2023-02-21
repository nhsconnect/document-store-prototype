import { render, screen } from "@testing-library/react";
import ConfigurationProvider, { useBaseAPIUrl, useFeatureToggle } from "./ConfigurationProvider";

const TestComponent = () => {
    const featureToggle = useFeatureToggle("TEST");
    const baseAPIUrl = useBaseAPIUrl("test");
    return (
        <>
            {featureToggle !== undefined && <p>{featureToggle ? "active" : "inactive"}</p>}
            <p>{baseAPIUrl}</p>
        </>
    );
};

describe("The feature toggle provider", () => {
    let env;
    beforeAll(() => {
        env = process.env.REACT_APP_ENV;
    });
    afterAll(() => {
        process.env.REACT_APP_ENV = env;
    });

    const baseConfig = {
        API: {
            endpoints: [
                {
                    name: "test",
                    endpoint: "https://test.api/",
                },
            ],
        },
        features: { development: { TEST: undefined } },
    };

    it("allows the consuming component to detect when a feature is active", () => {
        process.env.REACT_APP_ENV = "development";
        const config = {
            ...baseConfig,
            features: { development: { TEST: true } },
        };
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );

        expect(screen.getByText("active")).toBeInTheDocument();
    });

    it("allows the consuming component to detect when a feature is inactive", () => {
        process.env.REACT_APP_ENV = "development";
        const config = {
            ...baseConfig,
            features: { development: { TEST: false } },
        };
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when a toggle is not defined", () => {
        process.env.REACT_APP_ENV = "development";
        const config = {
            ...baseConfig,
            features: { development: { TEST: undefined } },
        };
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false when feature toggles are not defined for the current environment", () => {
        process.env.REACT_APP_ENV = "non-existent-env";
        render(
            <ConfigurationProvider config={baseConfig}>
                <TestComponent />
            </ConfigurationProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });

    it("provides a default value of false to the consuming component when REACT_APP_ENV is not defined", () => {
        process.env.REACT_APP_ENV = undefined;
        const config = { ...baseConfig };
        render(
            <ConfigurationProvider config={config}>
                <TestComponent />
            </ConfigurationProvider>
        );
        expect(screen.getByText("inactive")).toBeInTheDocument();
    });
});
