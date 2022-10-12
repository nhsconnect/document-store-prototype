import { render, screen } from "@testing-library/react";
import Index from "./pages";
import config from "./config";

jest.mock("aws-amplify");

describe("Index tests", () => {
  let defaultFeaturesConfig;

  beforeAll(() => {
    process.env.REACT_APP_ENV = "test";
    defaultFeaturesConfig = config.features;
  });

  afterAll(() => {
    config.features = defaultFeaturesConfig;
  });

  it("when CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED is enabled then use CIS authenticator", () => {
    config.features = {
      test: { CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true },
    };
    render(<Index />);
    expect(screen.getByTestId("CIS2Authenticator")).toBeTruthy();
  });

  it("when CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED is enabled then use amplify authenticator", () => {
    config.features = {
      test: { CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false },
    };
    render(<Index />);
    expect(screen.getByTestId("AmplifyAuthenticator")).toBeTruthy();
  });
});
