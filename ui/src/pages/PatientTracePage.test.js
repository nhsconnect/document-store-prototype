import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { PatientTracePage } from "./PatientTracePage";
import * as ReactRouter from "react-router";
import useApi from "../apiClients/useApi"; 

jest.mock("../apiClients/useApi");
const mockSetNhsNumber = jest.fn();
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: () => ["1112223334", mockSetNhsNumber],
}));

describe("PatientTracePage", () => {

    const mockNavigate = jest.fn();
    let useNavigateSpy;

    afterEach(() => {
        jest.clearAllMocks();
        useNavigateSpy.mockRestore();
    });

    beforeEach(() => {
        useNavigateSpy = jest.spyOn(ReactRouter, "useNavigate")
        useNavigateSpy.mockImplementation(() => mockNavigate);
    });

    it("renders the page", () => {
        render(<PatientTracePage title={"My test title"} />);

        expect(
            screen.getByRole("heading", { name: "My test title" })
        ).toBeInTheDocument();
        expect(screen.queryByLabelText("NHS number")).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Search" })
        ).toBeInTheDocument();
        expect(
            screen.queryByRole("button", { name: "Next" })
        ).not.toBeInTheDocument();
    });

    it("gets the patient's data when the NHS number is submitted", async () => {
        const patientData = {
            name: {
                given: ["Fred"],
                family: "Smith",
            },
            dateOfBirth: new Date(Date.UTC(2099, 9, 5)),
            postcode: "AB1 2CD",
        };

        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return [patientData]
                    }
                }
            };
        });
        
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();

        await waitFor(() => {
            expect(
                screen.queryByText(patientData.postcode)
            ).toBeInTheDocument();
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
        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return [patientData]
                    }
                }
            };
        });
        
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
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
            screen.getByRole("textbox", { name: "NHS number" })
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
        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return [patientData]
                    }
                }
            };
        });

        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();
        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.name.family}`)
            ).toBeInTheDocument();
        });
        clickNext();

        await waitFor(() => {
            expect(mockSetNhsNumber).toBeCalledWith(fakeNhsNumber);
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
        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return [patientData]
                    }
                }
            };
        });
        render(
            <PatientTracePage
                nextPage={expectedNextPage}
            />
        );

        enterNhsNumber(fakeNhsNumber);
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
        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return []
                    }
                }
            };
        });
        render(<PatientTracePage />);

        userEvent.type(screen.getByLabelText("NHS number"), fakeNhsNumber);
        startSearch();

        await waitFor(() => {
            expect(screen.getByRole("progressbar")).toBeInTheDocument();
        });
    });

    it("displays an error message when the form is submitted and the NHS number is missing", async () => {
        const fakeNhsNumber = "0987654321";
        const getPatientDetails = jest.fn()
        useApi.mockImplementation(() => {
            return {
                getPatientDetails
            };
        });
        render(<PatientTracePage />);

        startSearch();

        await waitFor(() => {
            expect(
                screen.getByText("Please enter a 10 digit NHS number")
            ).toBeInTheDocument();
        });
        expect(getPatientDetails).not.toHaveBeenCalled();
    });

    it.each([["123456789"], ["12345678901"], ["123456789A"]])(
        "displays an error message when the form is submitted and the NHS number is '%s''",
        async (nhsNumber) => {
            const fakeNhsNumber = "0987654321";
            const getPatientDetails = jest.fn()
            useApi.mockImplementation(() => {
                return {
                    getPatientDetails
                };
            });
            render(<PatientTracePage />);

            enterNhsNumber(nhsNumber);
            startSearch();

            await waitFor(() => {
                expect(
                    screen.getByText("Please enter a 10 digit NHS number")
                ).toBeInTheDocument();
            });
            expect(getPatientDetails).not.toHaveBeenCalled();
        }
    );

    it("displays an error message when there is a problem retrieving the patient's details", async () => {
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => {
                    throw Error("Error")
                }
            };
        });
        render(<PatientTracePage />);

        enterNhsNumber("0987654321");
        startSearch();

        await waitFor(() => {
            expect(
                screen.getByText("Technical error - Please retry.")
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
        const fakeNhsNumber = "0987654321";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return []
                    }
                }
            };
        });
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
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
    userEvent.type(screen.getByLabelText("NHS number"), nhsNumber);
}

function startSearch() {
    userEvent.click(screen.queryByRole("button", { name: "Search" }));
}

function nextButton() {
    return screen.queryByRole("button", { name: "Next" });
}
