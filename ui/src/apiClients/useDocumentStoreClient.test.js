import { renderHook } from "@testing-library/react-hooks";
import { useDocumentStoreClient } from "./useDocumentStoreClient";
import { useAuth } from "react-oidc-context";
import config from "../config";
import { useDocumentStoreAuthErrorInterceptor } from "./useDocumentStoreAuthErrorInterceptor";

jest.mock("react-oidc-context");
jest.mock("./useDocumentStoreAuthErrorInterceptor");

describe("useDocumentStoreClient()", () => {
    it("returns a configured request object", () => {
        const user = { id_token: "foo" };

        useAuth.mockImplementation(() => ({ user }));
        useDocumentStoreAuthErrorInterceptor.mockReturnValue(jest.fn());
        const { result } = renderHook(() => useDocumentStoreClient(config.API.endpoints[0].name));

        expect(result.current.defaults.baseURL).toEqual(config.API.endpoints[0].endpoint);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/fhir+json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${user.id_token}`);
    });
});
