import { render } from "@testing-library/react";
import { Navigate } from "react-router";

import { useQuery } from "./CIS2Authenticator";
import CIS2AuthenticationResultNavigator from "./CIS2AuthenticationResultNavigator";

jest.mock("react-router");
jest.mock("./CIS2Authenticator", () => ({ useQuery: jest.fn() }));

describe("CIS2AuthenticationResultNavigator", () => {
    it("navigates to home page when authentication is successful", () => {
        useQuery.mockReturnValue(new URLSearchParams());
        Navigate.mockImplementation(() => null);
        render(<CIS2AuthenticationResultNavigator />);

        expect(Navigate).toBeCalledWith(
            expect.objectContaining({ to: "/home", replace: true }),
            expect.anything()
        );
    });

    it("navigates to start page when authentication is unsuccessful", () => {
        const queryString = "error=some_error&some_other_key=some_value";
        useQuery.mockReturnValue(new URLSearchParams(queryString));
        Navigate.mockImplementation(() => null);
        render(<CIS2AuthenticationResultNavigator />);

        expect(Navigate).toBeCalledWith(
            expect.objectContaining({
                to: `/?${queryString}`,
                replace: true,
            }),
            expect.anything()
        );
    });
});
