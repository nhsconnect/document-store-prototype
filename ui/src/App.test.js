import { render, screen, within } from '@testing-library/react';
import App from './App';
import config from "./config";

jest.mock("aws-amplify");

test('renders application title', () => {
  render(<App />);
  const linkElement = within(screen.getByTestId("header-service-name")).getByText("Document Store");
  expect(linkElement).toBeTruthy();
});

describe('App tests', () => {

  let defaultFeaturesConfig;

  beforeAll(() => {
    process.env.NODE_ENV = "test";
    defaultFeaturesConfig = config.features;
  });

  afterAll(() => {
    config.features = defaultFeaturesConfig;
  });

  it('when CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED is enabled then use CIS authenticator', () => {
    config.features = { test: { CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true } };
    render(<App/>);
    expect(screen.getByTestId("CIS2Authenticator")).toBeTruthy();
  });

  it('when CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED is enabled then use amplify authenticator', () => {
    config.features = { test: { CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false } };
    render(<App/>);
    expect(screen.getByTestId("AmplifyAuthenticator")).toBeTruthy();
  });

});