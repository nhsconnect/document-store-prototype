import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useNavigate } from "react-router";
import PatientDetailsProvider from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import { PatientSummaryPage } from "./patientSummaryPage";

jest.mock("react-router");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");

describe("<PatientSummaryPage/>", () => {
    beforeEach(() => {
        useAuthorisedDocumentStore.mockReturnValue();
    });

    describe("render the page when patient details are found", () => {
        it("renders the patient details page when patient data is found", async () => {
            const nhsNumber = "9000000000";
            const familyName = "Smith";
            const patientDetails = buildPatientDetails({ familyName, nhsNumber });

            useNavigate.mockImplementation(() => jest.fn());
            render(
                <PatientDetailsProvider value={patientDetails}>
                    <PatientSummaryPage />
                </PatientDetailsProvider>
            );
            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(screen.getByText(familyName)).toBeInTheDocument();
            expect(screen.getByRole("button", { name: "Next" })).toBeInTheDocument();
        });

        it("displays text specific to upload path if user has selected upload", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const expectedNextPage = "upload";

            useNavigate.mockImplementation(() => jest.fn());
            render(
                <PatientDetailsProvider value={patientDetails}>
                    <PatientSummaryPage nextPage={expectedNextPage} />
                </PatientDetailsProvider>
            );

            userEvent.click(await screen.findByRole("button", { name: "Next" }));
            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(
                screen.getByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            );
        });

        it("doesn't display text specific to upload path if user has selected download", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const expectedNextPage = "download";

            useNavigate.mockImplementation(() => jest.fn());

            render(
                <PatientDetailsProvider value={patientDetails}>
                    <PatientSummaryPage nextPage={expectedNextPage} />
                </PatientDetailsProvider>
            );

            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(
                screen.queryByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            ).not.toBeInTheDocument();
        });

        it("displays a message when NHS number is superseded", async () => {
            const nhsNumber = "9000000012";
            const patientDetails = buildPatientDetails({ superseded: true, nhsNumber });

            useNavigate.mockImplementation(() => jest.fn());

            render(
                <PatientDetailsProvider value={patientDetails}>
                    <PatientSummaryPage />
                </PatientDetailsProvider>
            );

            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(screen.findByText("The NHS number for this patient has changed."));
        });

        it("displays a message when patient is sensitive", async () => {
            const nhsNumber = "9124038456";
            const restrictedPatientDetails = {
                result: { patientDetails: buildPatientDetails({ nhsNumber, postalCode: null, restricted: true }) },
            };

            render(
                <PatientDetailsProvider value={restrictedPatientDetails}>
                    <PatientSummaryPage />
                </PatientDetailsProvider>
            );

            expect(await screen.findByRole("heading", { name: "Verify patient details" })).toBeInTheDocument();
            expect(
                screen.findByText(
                    "Certain details about this patient cannot be displayed without the necessary access."
                )
            );
        });
    });

    describe("navigation", () => {
        it("navigates to specified page when moving to next page", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({ nhsNumber });
            const mockNavigate = jest.fn();
            const expectedNextPage = "test/submit";

            useNavigate.mockImplementation(() => mockNavigate);

            render(
                <PatientDetailsProvider value={patientDetails}>
                    <PatientSummaryPage nextPage={expectedNextPage} />
                </PatientDetailsProvider>
            );
            userEvent.click(await screen.findByRole("button", { name: "Next" }));

            expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
        });

        it("navigates to home page when no patient details found", async () => {
            const mockNavigate = jest.fn();
            const expectedNextPage = "/home";

            useNavigate.mockImplementation(() => mockNavigate);

            render(
                <PatientDetailsProvider>
                    <PatientSummaryPage />
                </PatientDetailsProvider>
            );

            expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
        });
    });
});
