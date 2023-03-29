import { render, screen } from "@testing-library/react";
import ConfigProvider from "../configProvider/ConfigProvider";
import DocumentStoreProvider, { useAuthorisedDocumentStore } from "./DocumentStoreProvider";
import { useAuth } from "react-oidc-context";
import { useDocumentStore } from "../../apiClients/documentStore";
import { useDocumentStoreAuthErrorInterceptor } from "../../apiClients/useDocumentStoreAuthErrorInterceptor";

jest.mock("react-oidc-context");
jest.mock("../../apiClients/documentStore");
jest.mock("../../apiClients/useDocumentStoreAuthErrorInterceptor");

describe("The document store context provider", () => {
    const reactAppEnv = process.env.REACT_APP_ENV;
    process.env.REACT_APP_ENV = "dev";

    afterAll(() => {
        process.env.REACT_APP_ENV = reactAppEnv;
    });

    beforeEach(() => {
        useAuth.mockReset();
        useDocumentStore.mockReset();
        useDocumentStoreAuthErrorInterceptor.mockReset();
    });

    const TestComponent = () => {
        const documentStore = useAuthorisedDocumentStore();
        return <p>{documentStore}</p>;
    };

    it("renders the oidc authorised document store provider when using OIDC authentication", () => {
        const token = "token";
        useAuth.mockReturnValue({
            user: {
                id_token: token,
            },
        });

        const documentStore = "foo";
        useDocumentStore.mockReturnValue(documentStore);

        const errorInterceptor = "interceptor";
        useDocumentStoreAuthErrorInterceptor.mockReturnValue(errorInterceptor);

        const config = {
            features: {
                dev: { OIDC_AUTHENTICATION: true },
            },
        };

        render(
            <ConfigProvider config={config}>
                <DocumentStoreProvider>
                    <p>Using OIDC</p>
                    <TestComponent />
                </DocumentStoreProvider>
            </ConfigProvider>
        );

        expect(screen.getByText("Using OIDC")).toBeVisible();
        expect(screen.getByText(documentStore)).toBeVisible();
        expect(useDocumentStore).toHaveBeenCalledWith(token, errorInterceptor);
        expect(useAuth).toHaveBeenCalled();
    });

    it("renders the session authorised document store provider when not using OIDC authentication", () => {
        const config = {
            features: {
                dev: { OIDC_AUTHENTICATION: false },
            },
        };
        const documentStore = "bar";
        useDocumentStore.mockReturnValue(documentStore);

        render(
            <ConfigProvider config={config}>
                <DocumentStoreProvider>
                    <p>Using sessions</p>
                    <TestComponent />
                </DocumentStoreProvider>
            </ConfigProvider>
        );

        expect(screen.getByText("Using sessions")).toBeVisible();
        expect(screen.getByText(documentStore)).toBeVisible();
        expect(useDocumentStore).toHaveBeenCalledWith();
        expect(useAuth).not.toHaveBeenCalled();
    });
});
