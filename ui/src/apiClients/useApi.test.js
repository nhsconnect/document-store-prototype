import { renderHook } from "@testing-library/react-hooks";
import { useApiRequest } from "./useApi";
import { useAuth } from "react-oidc-context";
import "../utils/useConfig";

jest.mock("react-oidc-context");

const mockConfig = {
    API: {
        endpoints: [
            {
                name: "doc-store-api",
                endpoint: "http://test.url",
            },
        ],
    },
};
jest.mock("../utils/useConfig", () => {
    return {
        useConfig: () => mockConfig,
    };
});

describe("useApiRequest", () => {
    it("returns a configured request object", () => {
        const user = { id_token: "foo" };
        useAuth.mockImplementation(() => ({ user }));

        const { result } = renderHook(() => useApiRequest(mockConfig.API.endpoints[0].name));

        expect(result.current.defaults.baseURL).toEqual(mockConfig.API.endpoints[0].endpoint);
        expect(result.current.defaults.headers["Accept"]).toEqual("application/fhir+json");
        expect(result.current.defaults.headers["Authorization"]).toEqual(`Bearer ${user.id_token}`);
    });
});
