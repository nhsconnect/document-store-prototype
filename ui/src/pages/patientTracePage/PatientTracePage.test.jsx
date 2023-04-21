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
        it("displays the patient details when their data is found", async () => {
            const nhsNumber = "9000000000";
            const familyName = "Smith";
            const patientDetails = buildPatientDetails({ familyName, nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };

            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(screen.getByText(familyName)).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Next" })).toBeInTheDocument();
        });

        it("displays text specific to upload path if user has selected upload", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };
            const expectedNextPage = "upload";

            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);
            useNavigate.mockImplementation(() => jest.fn());

            renderPatientTracePage({ nextPage: expectedNextPage });
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            userEvent.click(await screen.findByRole("button", { name: "Next" }));

            expect(
                screen.getByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            );
        });

        it("doesn't display text specific to upload path if user has selected download", async () => {
            const nhsNumber = "9000000000";
            const patientDetailsResponse = { result: { patientDetails: buildPatientDetails({ nhsNumber }) } };
            const expectedNextPage = "download";

            useNavigate.mockImplementation(() => jest.fn());
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage({ nextPage: expectedNextPage });
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));
            userEvent.click(await screen.findByRole("button", { name: "Next" }));

            expect(
                screen.queryByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            ).not.toBeInTheDocument();
        });

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

        it("displays a message when NHS number is superseded", async () => {
            const nhsNumber = "9000000000";
            const supersededNhsNumber = "9000000012";
            const patientDetails = buildPatientDetails({ superseded: true, nhsNumber });
            const patientDetailsResponse = { result: { patientDetails } };

            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), supersededNhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("The NHS number for this patient has changed."));
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

            expect(await screen.findByText("Patient Not Found")).toBeInTheDocument();
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
            expect(
                await screen.findByText("The NHS number provided is invalid. Please check the number you have entered.")
            ).toBeInTheDocument();
        });

        it("displays a message when patient is sensitive", async () => {
            const nhsNumber = "9124038456";
            const restrictedPatientDetails = {
                result: { patientDetails: buildPatientDetails({ nhsNumber, postalCode: null, restricted: true }) },
            };

            getPatientDetailsMock.mockResolvedValue(restrictedPatientDetails);

            renderPatientTracePage();

            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), nhsNumber);
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(
                await screen.findByText(
                    "Certain details about this patient cannot be displayed without the necessary access."
                )
            ).toBeInTheDocument();
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
            userEvent.click(await screen.findByRole("button", { name: "Next" }));

            expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
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

        it("displays an error message when there is a problem retrieving the patient details", async () => {
            getPatientDetailsMock.mockRejectedValue(new Error("Problem retrieving patient details"));

            renderPatientTracePage();
            userEvent.type(screen.getByRole("textbox", { name: "Enter NHS number" }), "0987654321");
            userEvent.click(screen.getByRole("button", { name: "Search" }));

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
            expect(await screen.findByText("Enter patient's 10 digit NHS number")).toBeInTheDocument();
            expect(await screen.findByRole("alert")).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Search" })).toBeInTheDocument();
            expect(screen.queryByRole("button", { name: "Next" })).not.toBeInTheDocument();
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
