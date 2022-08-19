import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useNavigate } from "react-router";

import ApiClient from "../apiClients/apiClient";
import { useMultiStepUploadProviderContext } from "../providers/MultiStepUploadProvider";
import { PatientTracePage } from "./PatientTrace";

jest.mock("../apiClients/apiClient");
const mockSetNhsNumber = jest.fn();
jest.mock("../providers/MultiStepUploadProvider", () => ({
    useMultiStepUploadProviderContext: () => ["1112223334", mockSetNhsNumber],
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

describe("PatientTracePage", () => {
    it("renders the page", () => {
        render(<PatientTracePage />);

        expect(
            screen.getByRole("heading", { name: "Upload Patient Records" })
        ).toBeInTheDocument();
        expect(screen.queryByLabelText("Enter NHS number")).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Search" })
        ).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Next" })
        ).not.toBeInTheDocument();
    });

    it("gets the patient's data when the NHS number is submitted", async () => {
        const patientData = {
            name: "Joe Bloggs",
            dateOfBirth: "05/10/2001",
            postcode: "AB1 2CD",
        };
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockReturnValue([patientData]),
            };
        });
        const apiClientMock = new ApiClient();
        const nhsNumber = "0987654321";
        render(<PatientTracePage client={apiClientMock} />);

        enterNhsNumber(nhsNumber);
        startSearch();

        await waitFor(() => {
            expect(apiClientMock.getPatientDetails).toHaveBeenCalledWith(
                nhsNumber
            );
        });
    });

    it("displays the patient's details when their demographic data is found", async () => {
        const patientData = {
            name: "Fred Smith",
            dateOfBirth: "05/10/2099",
            postcode: "AB1 2CD",
        };
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockReturnValue([patientData]),
            };
        });
        render(<PatientTracePage client={new ApiClient()} />);

        enterNhsNumber("0987654321");
        startSearch();

        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.name}`)
            ).toBeInTheDocument();
        });
        expect(
            screen.queryByText(`${patientData.dateOfBirth}`)
        ).toBeInTheDocument();
        expect(
            screen.queryByText(`${patientData.postcode}`)
        ).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Next" })
        ).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Search" })
        ).not.toBeInTheDocument();
        expect(
            screen.getByRole("textbox", { name: "Enter NHS number" })
        ).toBeDisabled();
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });

    it("stores the patient's details in context when moving to next page", async () => {
        const patientData = {
            name: "Fred Smith",
            dateOfBirth: "05/10/2099",
            postcode: "AB1 2CD",
        };
        const nhsNumber = "0987654321";
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockReturnValue([patientData]),
            };
        });
        render(<PatientTracePage client={new ApiClient()} />);

        enterNhsNumber(nhsNumber);
        startSearch();
        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.name}`)
            ).toBeInTheDocument();
        });
        clickNext();

        await waitFor(() => {
            expect(mockSetNhsNumber).toBeCalledWith(nhsNumber);
        });
        expect(mockNavigate).toHaveBeenCalledWith("/upload/submit");
    });

    it("displays a loading spinner when the patient's details are being requested", async () => {
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockReturnValue({
                    name: "",
                    dateOfBirth: "",
                    postcode: "",
                }),
            };
        });
        render(<PatientTracePage client={new ApiClient()} />);

        userEvent.type(screen.getByLabelText("Enter NHS number"), "0987654321");
        startSearch();

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
        });
    });

    it("displays an error message when the form is submitted and the NHS number is missing", async () => {
        const apiClientMock = new ApiClient();
        render(<PatientTracePage client={apiClientMock} />);

        startSearch();

        await waitFor(() => {
            expect(
                screen.getByText("Please enter an NHS number")
            ).toBeInTheDocument();
        });
        expect(apiClientMock.getPatientDetails).not.toHaveBeenCalled();
    });

    it("displays an error message when there is a problem retrieving the patient's details", async () => {
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockImplementation(() => {
                    throw Error("Technical failure!");
                }),
            };
        });
        render(<PatientTracePage client={new ApiClient()} />);

        enterNhsNumber("0987654321");
        startSearch();

        await waitFor(() => {
            expect(
                screen.getByText("Technical Failure - Please retry.")
            ).toBeInTheDocument();
        });
    });

    it("displays a message when no patient details are found", async () => {
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockImplementation(() => {
                    return [];
                }),
            };
        });
        render(<PatientTracePage client={new ApiClient()} />);

        enterNhsNumber("0987654321");
        startSearch();

        await waitFor(() => {
            expect(screen.getByText("Patient Not Found")).toBeInTheDocument();
        });
        expect(
            screen.getByText(
                "Please verify NHS number again. However, if you are sure it's correct you can proceed."
            )
        ).toBeInTheDocument();
    });
});

function clickNext() {
    userEvent.click(screen.queryByRole("button", { name: "Next" }));
}

function enterNhsNumber(nhsNumber) {
    userEvent.type(screen.getByLabelText("Enter NHS number"), nhsNumber);
}

function startSearch() {
    userEvent.click(screen.queryByRole("button", { name: "Search" }));
}
