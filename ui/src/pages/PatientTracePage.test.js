import {render, screen, waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import {PatientTracePage} from "./PatientTracePage";
import * as ReactRouter from "react-router";
import useApi from "../apiClients/useApi";

jest.mock("../apiClients/useApi");
const mockSetNhsNumber = jest.fn();
jest.mock("../providers/NhsNumberProvider", () => ({
    useNhsNumberProviderContext: () => ["1112223334", mockSetNhsNumber],
}));
const fakeNhsNumber = "9000000009";
const patientData = {
    birthDate: "2010-10-22",
    familyName: "Smith",
    givenName: ["Jane"],
    nhsNumber: fakeNhsNumber,
    postalCode: "LS1 6AE"
};
const patientDetailsResponse = {
    result: {
        patientDetails: patientData
    }
};

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
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return patientDetailsResponse
                    }
                }
            };
        });
        
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();

        await waitFor(() => {
            expect(
                screen.queryByText(patientData.postalCode)
            ).toBeInTheDocument();
        });
    });

    it("displays the patient's details when their demographic data is found", async () => {
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return patientDetailsResponse
                    }
                }
            };
        });
        
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();

        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.givenName[0]}`)
            ).toBeInTheDocument();
        });
        expect(
            screen.queryByText(`${patientData.familyName}`)
        ).toBeInTheDocument();
        expect(
            screen.queryByText(
                `${patientData.birthDate}`
            )
        ).toBeInTheDocument();
        expect(
            screen.queryByText(`${patientData.postalCode}`)
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
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return patientDetailsResponse
                    }
                }
            };
        });

        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();
        await waitFor(() => {
            expect(
                screen.queryByText(`${patientData.givenName[0]}`)
            ).toBeInTheDocument();
        });
        clickNext();

        await waitFor(() => {
            expect(mockSetNhsNumber).toBeCalledWith(fakeNhsNumber);
        });
    });

    it("navigates to specified page when moving to next page", async () => {
        const expectedNextPage = "test/submit";
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: (nhsNumber) => {
                    if (nhsNumber === fakeNhsNumber) {
                        return patientDetailsResponse
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
        const errorResponse = {
            status: 404,
            message: "404 Patient not found."
        };
        useApi.mockImplementation(() => {
            return {
                getPatientDetails: () => {
                    throw {response: errorResponse}
                }
            };
        });
        render(<PatientTracePage />);

        enterNhsNumber(fakeNhsNumber);
        startSearch();

        await waitFor(() => {
            expect(screen.getByText("Patient Not Found")).toBeInTheDocument();
        });
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
