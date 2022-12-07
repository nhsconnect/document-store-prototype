import { renderHook } from "@testing-library/react-hooks";
import ApiClient from "./apiClient";
import useApi from "./useApi";
import { API } from "aws-amplify"
import { useAuth } from "react-oidc-context"

jest.mock("./ApiClient")
jest.mock("aws-amplify")
jest.mock("react-oidc-context")

describe("The useApi hook", () => {
    it("returns an instance of the API client", () => {
        const user = "foo"
        useAuth.mockImplementation(() => ({user}))

        const { result } = renderHook(() => useApi())
        expect(result.current).toBeInstanceOf(ApiClient);

        expect(ApiClient).toHaveBeenCalledWith(API, user)
    })
})