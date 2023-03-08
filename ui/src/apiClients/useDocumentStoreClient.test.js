import { renderHook } from "@testing-library/react-hooks";
import { useDocumentStoreClient } from "./useDocumentStoreClient";
import { useBaseAPIUrl } from "../providers/ConfigurationProvider";

jest.mock("react-oidc-context");
jest.mock("./useDocumentStoreAuthErrorInterceptor");
jest.mock("../providers/ConfigurationProvider");

describe("useDocumentStoreClient()", () => {
    it("returns a configured request object with bearer token if one is provided", () => {
        const apiName = "doc-store-api";
        const baseAPIURL = "https://api.url";
        const bearerToken = "token";
        const interceptor = jest.fn();
        useBaseAPIUrl.mockReturnValue(baseAPIURL);
        const { result } = renderHook(() => useDocumentStoreClient(bearerToken, interceptor));

        expect(useBaseAPIUrl).toHaveBeenCalledWith(apiName);
        expect(result.current.defaults.baseURL).toEqual(baseAPIURL);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/fhir+json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${bearerToken}`);
    });
});
