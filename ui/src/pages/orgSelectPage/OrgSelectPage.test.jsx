import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import OrgSelectPage from "./OrgSelectPage";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import { useNavigate } from "react-router";
import React from "react";
import routes from "../../enums/routes";
import axios from "axios";

jest.mock("../../providers/configProvider/ConfigProvider");
jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("react-router");
jest.mock("axios");

describe("OrgSelectPage", () => {
    const session = {
        isLoggedIn: false,
        organisations: [
            { orgType: "GP Practice", orgName: "PORTWAY LIFESTYLE CENTRE", odsCode: "A9A5A" },
            { orgType: "Primary Care Support England", orgName: "PCSE DARLINGTON", odsCode: "B1B1B" },
        ],
    };

    const setSessionMock = jest.fn();
    useSessionContext.mockReturnValue([session, setSessionMock]);

    afterEach(() => {
        jest.clearAllMocks();
    });

    it("renders the page", () => {
        renderOrgSelectPage();

        const form = within(screen.getByRole("group", { name: "Select an organisation" }));
        expect(form.getByRole("heading", { name: "Select an organisation" })).toBeInTheDocument();
        expect(
            form.getByText(
                "You are associated to more than one organisation, select an organisation you would like to view."
            )
        ).toBeInTheDocument();
        expect(form.getByRole("radio", { name: "Portway lifestyle centre [A9A5A] GP Practice" })).toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Continue" })).toBeInTheDocument();
    });

    it("has no radio buttons selected by default", () => {
        renderOrgSelectPage();

        expect(screen.getByRole("radio", { name: "Portway lifestyle centre [A9A5A] GP Practice" })).not.toBeChecked();
        expect(
            screen.getByRole("radio", { name: "Pcse darlington [B1B1B] Primary Care Support England" })
        ).not.toBeChecked();
    });

    it("displays a loading spinner when an org has been selected and the Continue button clicked", async () => {
        const mockNavigate = jest.fn();
        const responseData = { org: "test" };

        axios.get.mockResolvedValue(responseData);
        useNavigate.mockImplementation(() => mockNavigate);

        renderOrgSelectPage();

        userEvent.click(screen.getByRole("radio", { name: "Portway lifestyle centre [A9A5A] GP Practice" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(screen.getByRole("Spinner", { name: "Verifying organisation..." })).toBeInTheDocument();
        });
    });

    it("displays error box when no org has been selected and the Continue button clicked", async () => {
        renderOrgSelectPage();

        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(screen.getByRole("heading", { name: "There is a problem" })).toBeInTheDocument();
            expect(
                screen.getByRole("radio", { name: "Portway lifestyle centre [A9A5A] GP Practice" })
            ).not.toBeChecked();
            expect(
                screen.getByRole("radio", { name: "Pcse darlington [B1B1B] Primary Care Support England" })
            ).not.toBeChecked();
            expect(screen.queryByRole("Spinner")).not.toBeInTheDocument();
        });
    });

    it("navigates to the Download page when a PCSE org has been selected and the Continue button clicked", async () => {
        const mockNavigate = jest.fn();
        const responseData = {
            data: {
                UserType: "Primary Care Support England",
            },
        };

        axios.get.mockResolvedValue(responseData);
        useNavigate.mockImplementation(() => mockNavigate);

        renderOrgSelectPage();

        userEvent.click(screen.getByRole("radio", { name: "Pcse darlington [B1B1B] Primary Care Support England" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith(routes.SEARCH_PATIENT);
        });
    });

    it("navigates to the Upload page when a GPP org has been selected and the Continue button clicked", async () => {
        const mockNavigate = jest.fn();
        const responseData = {
            data: {
                UserType: "GP Practice",
            },
        };

        axios.get.mockResolvedValue(responseData);
        useNavigate.mockImplementation(() => mockNavigate);

        renderOrgSelectPage();

        userEvent.click(screen.getByRole("radio", { name: "Portway lifestyle centre [A9A5A] GP Practice" }));
        userEvent.click(screen.getByRole("button", { name: "Continue" }));

        await waitFor(() => {
            expect(axios.get).toHaveBeenCalledTimes(1);
            expect(mockNavigate).toHaveBeenCalledWith(routes.UPLOAD_SEARCH_PATIENT);
        });
    });
});

const renderOrgSelectPage = () => {
    render(<OrgSelectPage />);
};
