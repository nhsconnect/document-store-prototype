import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import ApiClient from "../apiClients/apiClient";
import { PatientTracePage } from "./PatientTracePage";

jest.mock("../apiClients/apiClient");
const mockSetNhsNumber = jest.fn();
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: () => ["1112223334", mockSetNhsNumber],
}));
const mockNavigate = jest.fn();
jest.mock("react-router", () => ({
    useNavigate: () => mockNavigate,
}));

describe("PatientTracePage", () => {
    it("renders the page", () => {
        render(<PatientTracePage title={"My test title"} />);

        expect(
            screen.getByRole("heading", { name: "My test title" })
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
            name: {
                given: ["Fred"],
                family: "Smith",
            },
            dateOfBirth: new Date(Date.UTC(2099, 9, 5)),
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
                screen.queryByText(`${patientData.name.given[0]}`)
            ).toBeInTheDocument();
        });
        expect(
            screen.queryByText(`${patientData.name.family}`)
        ).toBeInTheDocument();
        expect(
            screen.queryByText(
                `${patientData.dateOfBirth.toLocaleDateString()}`
            )
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
        ).toHaveAttribute("readonly");
        expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
    });

    it("stores the patient's details in context when moving to next page", async () => {
        const patientData = {
            name: {
                given: ["Fred"],
                family: "Smith",
            },
            dateOfBirth: new Date(Date.UTC(2099, 9, 5)),
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
                screen.queryByText(`${patientData.name.family}`)
            ).toBeInTheDocument();
        });
        clickNext();

        await waitFor(() => {
            expect(mockSetNhsNumber).toBeCalledWith(nhsNumber);
        });
    });

    it("navigates to specified page when moving to next page", async () => {
        const patientData = {
            name: {
                given: ["Fred"],
                family: "Smith",
            },
            dateOfBirth: new Date(Date.UTC(2099, 9, 5)),
            postcode: "AB1 2CD",
        };
        const expectedNextPage = "test/submit";
        ApiClient.mockImplementation(() => {
            return {
                getPatientDetails: jest.fn().mockReturnValue([patientData]),
            };
        });
        render(
            <PatientTracePage
                nextPage={expectedNextPage}
                client={new ApiClient()}
            />
        );

        enterNhsNumber("0987654321");
        startSearch();
        await waitFor(() => {
            expect(nextButton()).toBeInTheDocument();
        });
        clickNext();

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
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
                screen.getByText("Please enter a 10 digit NHS number")
            ).toBeInTheDocument();
        });
        expect(apiClientMock.getPatientDetails).not.toHaveBeenCalled();
    });

    it.each([["123456789"], ["12345678901"], ["123456789A"]])(
        "displays an error message when the form is submitted and the NHS number is '%s''",
        async (nhsNumber) => {
            const apiClientMock = new ApiClient();
            render(<PatientTracePage client={apiClientMock} />);

            enterNhsNumber(nhsNumber);
            startSearch();

            await waitFor(() => {
                expect(
                    screen.getByText("Please enter a 10 digit NHS number")
                ).toBeInTheDocument();
            });
            expect(apiClientMock.getPatientDetails).not.toHaveBeenCalled();
        }
    );

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
        expect(
            screen.queryByRole("button", { name: "Next" })
        ).not.toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Search" })
        ).toBeInTheDocument();
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

function nextButton() {
    return screen.queryByRole("button", { name: "Next" });
}
