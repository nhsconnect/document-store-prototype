import { renderHook } from "@testing-library/react-hooks";
import { useNavigate } from "react-router";
import { useDocumentStoreAuthHandler } from "./useDocumentStoreAuthHandler";

jest.mock("react-router");
describe("useDocumentStoreAuthHandler()", () => {
    it("returns the returned value of the wrapped function if no error is thrown", () => {
        const successfulApiRequest = () => {
            return 200;
        };

        const { result } = renderHook(() => useDocumentStoreAuthHandler(successfulApiRequest));

        expect(result.current()).toEqual(200);
    });

    it("navigates to the start page if an error response is a 401", () => {
        const unauthorizedApiRequest = () => {
            throw { response: { status: 401 } };
        };
        const navigate = jest.fn();

        useNavigate.mockReturnValue(navigate);
        const { result } = renderHook(() => useDocumentStoreAuthHandler(unauthorizedApiRequest));
        result.current();

        expect(navigate).toHaveBeenCalledWith("/");
    });

    it("rethrows any error that is not a 401 response", () => {
        const internalErrorResponse = new Error();
        internalErrorResponse.response = { status: 500 };
        const internalErrorApiRequest = () => {
            throw internalErrorResponse;
        };

        const { result } = renderHook(() => useDocumentStoreAuthHandler(internalErrorApiRequest));

        expect(result.current).toThrow(internalErrorResponse);
    });
});
