import { useNavigate } from "react-router";
import { useDocumentStoreAuthErrorInterceptor } from "./useDocumentStoreAuthErrorInterceptor";
import { renderHook } from "@testing-library/react-hooks";
import { useAuth } from "react-oidc-context";

jest.mock("react-oidc-context");
jest.mock("react-router");

describe("useDocumentStoreAuthErrorInterceptor()", () => {
    it("returns error on any non-401 response status code", async () => {
        const error = new Error();

        useAuth.mockReturnValue({ removeUser: jest.fn() });
        const { result } = renderHook(() => useDocumentStoreAuthErrorInterceptor());

        await expect(() => result.current(error)).rejects.toThrow(error);
    });

    it("navigates to start page on 401 response status code", async () => {
        const removeUser = jest.fn();
        const navigate = jest.fn();
        const error = new Error();
        error.response = { status: 401 };

        useAuth.mockReturnValue({ removeUser });
        useNavigate.mockReturnValue(navigate);
        const { result } = renderHook(() => useDocumentStoreAuthErrorInterceptor());
        await result.current(error);

        expect(removeUser).toHaveBeenCalled();
        expect(navigate).toHaveBeenCalledWith("/");
    });
});
