import { renderHook } from "@testing-library/react-hooks";
import ApiClient from "./apiClient";
import useApi, { useApiRequest } from "./useApi";
import { API } from "aws-amplify";
import { useAuth } from "react-oidc-context";
import config from "../config";

jest.mock("./apiClient");
jest.mock("aws-amplify");
jest.mock("react-oidc-context");

describe("useApi", () => {
    it("returns an instance of the API client", () => {
        const user = "foo";
        useAuth.mockImplementation(() => ({ user }));

        const { result } = renderHook(() => useApi());
        expect(result.current).toBeInstanceOf(ApiClient);

        expect(ApiClient).toHaveBeenCalledWith(API, user);
    });
});

describe("useApiRequest", () => {
    it("returns a configured request object", () => {
        const user = { id_token: "foo" };
        useAuth.mockImplementation(() => ({ user }));

        const { result } = renderHook(() => useApiRequest(config.API.endpoints[0].name));

        expect(result.current.defaults.baseURL).toEqual(config.API.endpoints[0].endpoint);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/fhir+json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${user.id_token}`);
    });
});
