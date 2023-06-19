import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PatientTracePage } from "./PatientTracePage";
import { useNavigate } from "react-router";
import PatientDetailsProvider from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import routes from "../../enums/routes";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

jest.mock("../../providers/sessionProvider/SessionProvider");
jest.mock("react-router");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");

describe("<PatientTracePage/>", () => {
    const getPatientDetailsMock = jest.fn();

    beforeEach(() => {
        useAuthorisedDocumentStore.mockReturnValue({ getPatientDetails: getPatientDetailsMock });
    });

    describe("initial rendering", () => {
        it("renders the page", () => {
            const session = { isLoggedIn: true };
            const setSessionMock = jest.fn();

            useSessionContext.mockReturnValue([session, setSessionMock]);
            renderPatientTracePage();

            expect(screen.getByRole("heading", { name: "Search for patient" })).toBeInTheDocument();
            expect(screen.getByRole("textbox", { name: "Enter NHS number" })).toBeInTheDocument();
            expect(screen.getByText("A 10-digit number, for example, 485 777 3456")).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Search" })).toBeInTheDocument();
        });
    });

    describe("patient details search", () => {
        it("displays a loading spinner when the patients details are being requested", async () => {
            getPatientDetailsMock.mockResolvedValue([]);
            const session = { isLoggedIn: true };
            const setSessionMock = jest.fn();

            useSessionContext.mockReturnValue([session, setSessionMock]);
            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "9000000009");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            await waitFor(() => {
                expect(screen.getByRole("SpinnerButton")).toBeInTheDocument();
            });
            expect(screen.queryByRole("SpinnerButton")).not.toBeInTheDocument();
        });

        it("displays a message when invalid NHS number provided", async () => {
            const errorResponse = {
                response: {
                    status: 400,
                    message: "400 Patient not found.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "0987654321");
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            expect(await screen.findAllByText("Enter a valid patient NHS number.")).toHaveLength(2);
        });

        it("displays server error message when server is down", async () => {
            const errorResponse = {
                response: {
                    status: 500,
                    message: "500 service is currently unavailable.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "0987654321");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("Sorry, the service is currently unavailable.")).toBeInTheDocument();
        });

        it("displays a message when patient data not found", async () => {
            const errorResponse = {
                response: {
                    status: 404,
                    message: "404 Not found.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "9000000000");
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            expect(await screen.findAllByText("Sorry, patient data not found.")).toHaveLength(2);
        });
    });

    describe("navigation", () => {
        it("navigates to specified page when moving to next page", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };
            const mockNavigate = jest.fn();
            const expectedNextPage = "test/submit";

            useNavigate.mockImplementation(() => mockNavigate);
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage({ nextPage: expectedNextPage });
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
            });
        });

        it("navigates to start page when user is unauthorized to make request", async () => {
            const errorResponse = {
                response: {
                    status: 403,
                    message: "403 Unauthorized.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);
            const homePage = routes.ROOT;
            const mockNavigate = jest.fn();
            const session = { isLoggedIn: true };
            const setSessionMock = jest.fn();

            useSessionContext.mockReturnValue([session, setSessionMock]);
            useNavigate.mockImplementation(() => mockNavigate);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "9000000000");
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith(homePage);
            });
        });
    });

    describe("validation", () => {
        it("allows NHS number with spaces to be submitted", async () => {
            const nhsNumberWithSpaces = "123 456 7891";
            const nhsNumber = "1234567891";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };
            const mockNavigate = jest.fn();
            const expectedNextPage = "test/submit";

            useNavigate.mockImplementation(() => mockNavigate);
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage({ nextPage: expectedNextPage });
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumberWithSpaces);
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
            });
        });

        it("allows NHS number with dashes to be submitted", async () => {
            const nhsNumberWithDashes = "123-456-7891";
            const nhsNumber = "1234567891";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };
            const mockNavigate = jest.fn();
            const expectedNextPage = "test/submit";

            useNavigate.mockImplementation(() => mockNavigate);
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage({ nextPage: expectedNextPage });
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumberWithDashes);
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            await waitFor(() => {
                expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
            });
        });
    });

    describe("errors", () => {
        it("displays an error message when the form is submitted and the NHS number is missing", async () => {
            renderPatientTracePage();
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
            expect(await screen.findAllByText("Enter patient's 10 digit NHS number")).toHaveLength(2);
        });

        it.each([["123456789"], ["12345678901"], ["123456789A"]])(
            "displays an error message when the form is submitted and the NHS number is '%s''",
            async (nhsNumber) => {
                renderPatientTracePage();
                userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
                userEvent.click(screen.getByRole("button", { name: "Search" }));

                expect(await screen.findByText("There is a problem")).toBeInTheDocument();
                expect(await screen.findAllByText("Enter patient's 10 digit NHS number")).toHaveLength(2);
            }
        );
    });

    describe("without a valid backend session", () => {
        it("navigates to the start page when API call to search patient is made without a valid backend session", async () => {
            const errorResponse = {
                response: {
                    status: 403,
                    message: "Unauthorised",
                },
            };

            const navigateMock = jest.fn();

            useNavigate.mockReturnValue(navigateMock);
            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "0987654321");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            await waitFor(() => {
                expect(navigateMock).toHaveBeenCalledWith(routes.ROOT);
            });
        });
    });
});

const renderPatientTracePage = (propsOverride) => {
    const props = {
        expectedNextPage: "download",
        ...propsOverride,
    };

    render(
        <PatientDetailsProvider>
            <PatientTracePage {...props} />
        </PatientDetailsProvider>
    );
};
