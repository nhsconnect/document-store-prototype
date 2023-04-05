import { renderHook } from "@testing-library/react-hooks";
import { useDocumentStoreClient } from "./useDocumentStoreClient";
import { useBaseAPIUrl, useFeatureToggle } from "../providers/configProvider/ConfigProvider";

jest.mock("react-oidc-context");
jest.mock("./useDocumentStoreAuthErrorInterceptor");
jest.mock("../providers/configProvider/ConfigProvider");

describe("useDocumentStoreClient()", () => {
    it("returns a configured request object with a bearer token when OIDC auth is true", () => {
        useFeatureToggle.mockReturnValue(true);

        const bearerToken = "token";

        const apiName = "doc-store-api";
        const baseAPIURL = "https://api.url";
        const interceptor = jest.fn();

        useBaseAPIUrl.mockReturnValue(baseAPIURL);
        const { result } = renderHook(() => useDocumentStoreClient(bearerToken, interceptor));

        expect(useBaseAPIUrl).toHaveBeenCalledWith(apiName);
        expect(result.current.defaults.baseURL).toEqual(baseAPIURL);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${bearerToken}`);
        expect(result.current.defaults.withCredentials).toEqual(false);
    });

    it("returns a configured request object without a bearer token and withCredentials when OIDC auth is false ", () => {
        useFeatureToggle.mockReturnValue(false);

        const bearerToken = "token";

        const apiName = "doc-store-api";
        const baseAPIURL = "https://api.url";
        const interceptor = jest.fn();

        useBaseAPIUrl.mockReturnValue(baseAPIURL);
        const { result } = renderHook(() => useDocumentStoreClient(bearerToken, interceptor));

        expect(useBaseAPIUrl).toHaveBeenCalledWith(apiName);
        expect(result.current.defaults.baseURL).toEqual(baseAPIURL);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(undefined);
        expect(result.current.defaults.withCredentials).toEqual(true);
    });
});
