import { render, screen } from "@testing-library/react";
import FeatureToggleProvider, {
  useFeatureToggle,
} from "./FeatureToggleProvider";
import config from "../config";

const TestComponent = () => {
  const featureToggle = useFeatureToggle("TEST");
  if (featureToggle === true) return "active";
  else if (featureToggle === false) return "inactive";
  else return "undefined";
};

describe("The feature toggle provider", () => {
  let env;
  let defaultFeaturesConfig;
  beforeAll(() => {
    env = process.env.REACT_APP_ENV;
    defaultFeaturesConfig = config.features;
  });
  afterAll(() => {
    process.env.REACT_APP_ENV = env;
    config.features = defaultFeaturesConfig;
  });

  it("allows the consuming component to detect when a feature is active", () => {
    process.env.REACT_APP_ENV = "development";
    config.features = { development: { TEST: true } };
    render(
      <FeatureToggleProvider>
        <TestComponent />
      </FeatureToggleProvider>
    );

    expect(screen.getByText("active")).toBeInTheDocument();
  });

  it("allows the consuming component to detect when a feature is inactive", () => {
    process.env.REACT_APP_ENV = "development";
    config.features = { development: { TEST: false } };
    render(
      <FeatureToggleProvider>
        <TestComponent />
      </FeatureToggleProvider>
    );
    expect(screen.getByText("inactive")).toBeInTheDocument();
  });

  it("provides a default value of false to the consuming component when a toggle is not defined", () => {
    process.env.REACT_APP_ENV = "development";
    config.features = { development: { TEST: undefined } };
    render(
      <FeatureToggleProvider>
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
    config.features = { development: { TEST: undefined } };
    render(
      <FeatureToggleProvider>
        <TestComponent />
      </FeatureToggleProvider>
    );
    expect(screen.getByText("inactive")).toBeInTheDocument();
  });
});
