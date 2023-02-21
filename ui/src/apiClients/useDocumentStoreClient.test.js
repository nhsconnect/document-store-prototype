import { renderHook } from "@testing-library/react-hooks";
import { useDocumentStoreClient } from "./useDocumentStoreClient";
import { useAuth } from "react-oidc-context";
import { useDocumentStoreAuthErrorInterceptor } from "./useDocumentStoreAuthErrorInterceptor";
import { useBaseAPIUrl } from "../providers/ConfigurationProvider";

jest.mock("react-oidc-context");
jest.mock("./useDocumentStoreAuthErrorInterceptor");
jest.mock("../providers/ConfigurationProvider");

describe("useDocumentStoreClient()", () => {
    it("returns a configured request object", () => {
        const user = { id_token: "foo" };
        const apiName = "doc-store-api";
        const baseAPIURL = "https://api.url";

        useAuth.mockImplementation(() => ({ user }));
        useDocumentStoreAuthErrorInterceptor.mockReturnValue(jest.fn());
        useBaseAPIUrl.mockReturnValue(baseAPIURL);
        const { result } = renderHook(() => useDocumentStoreClient());

        expect(useBaseAPIUrl).toHaveBeenCalledWith(apiName);
        expect(result.current.defaults.baseURL).toEqual(baseAPIURL);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/fhir+json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${user.id_token}`);
    });
});
