import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import { useMultiStepUploadProviderContext } from "../providers/MultiStepUploadProvider";
import { PatientTracePage } from "./PatientTrace";

jest.mock("../apiClients/apiClient");
const mockSetNhsNumber = jest.fn();
jest.mock("../providers/MultiStepUploadProvider", () => ({
    useMultiStepUploadProviderContext: () => ["1112223334", mockSetNhsNumber],
}));

describe("PatientTracePage", () => {
    it("renders the page", () => {
        render(<PatientTracePage />);

        expect(
            screen.getByRole("heading", { name: "Upload Patient Records" })
        ).toBeInTheDocument();
        expect(screen.queryByLabelText("Enter NHS number")).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Next" })
        ).toBeInTheDocument();
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

        userEvent.type(
            screen.getByRole("textbox", { name: "Enter NHS number" }),
            nhsNumber
        );
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

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

        userEvent.type(
            screen.getByRole("textbox", { name: "Enter NHS number" }),
            "0987654321"
        );
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

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

        userEvent.type(
            screen.getByRole("textbox", { name: "Enter NHS number" }),
            nhsNumber
        );
        userEvent.click(screen.queryByRole("button", { name: "Next" }));
        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.name}`)
            ).toBeInTheDocument();
        });
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

        await waitFor(() => {
            expect(mockSetNhsNumber).toBeCalledWith(nhsNumber);
        });
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
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
        });
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

        userEvent.type(screen.getByLabelText("Enter NHS number"), "0987654321");
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

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

        userEvent.type(screen.getByLabelText("Enter NHS number"), "0987654321");
        userEvent.click(screen.queryByRole("button", { name: "Next" }));

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
