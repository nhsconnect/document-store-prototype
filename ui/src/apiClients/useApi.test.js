import { renderHook } from "@testing-library/react-hooks";
import { useApiRequest } from "./useApi";
import { useAuth } from "react-oidc-context";
import config from "../config";

jest.mock("react-oidc-context");

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
