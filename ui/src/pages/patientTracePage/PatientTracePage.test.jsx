import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PatientTracePage } from "./PatientTracePage";
import { useNavigate } from "react-router";
import PatientDetailsProvider from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";

jest.mock("react-router");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");

describe("<PatientTracePage/>", () => {
    const getPatientDetailsMock = jest.fn();

    beforeEach(() => {
        useAuthorisedDocumentStore.mockReturnValue({ getPatientDetails: getPatientDetailsMock });
    });

    describe("initial rendering", () => {
        it("renders the page", () => {
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

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "9000000009");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            await waitFor(() => {
                expect(screen.getByRole("progressbar")).toBeInTheDocument();
            });
            expect(screen.queryByRole("progressbar")).not.toBeInTheDocument();
        });

        it("displays a message when no patient details are found", async () => {
            const errorResponse = {
                response: {
                    status: 404,
                    message: "404 Patient not found.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "0987654321");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
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
            expect(await screen.queryByText("There is a problem")).not.toBeInTheDocument();
            expect(await screen.queryByText("Enter a valid patient NHS number")).not.toBeInTheDocument();
            // expect(await screen.findByText("Enter patient's 10 digit NHS number")).not.toBeInTheDocument();
        });

        it("displays a message when NHS number is invalid", async () => {
            const errorResponse = {
                response: {
                    status: 400,
                    message: "400 Invalid NHS number.",
                },
            };

            getPatientDetailsMock.mockRejectedValue(errorResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "9000000000");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
            expect(await screen.findAllByText("Enter a valid patient NHS number")).toHaveLength(2);
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
