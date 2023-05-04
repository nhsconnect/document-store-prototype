import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useNavigate } from "react-router";
import PatientDetailsProvider from "../../providers/patientDetailsProvider/PatientDetailsProvider";
import { buildPatientDetails } from "../../utils/testBuilders";
import { useAuthorisedDocumentStore } from "../../providers/documentStoreProvider/DocumentStoreProvider";
import {PatientSummaryPage} from "./patientSummaryPage";
import {createContext} from "react";

jest.mock("react-router");
jest.mock("../../providers/documentStoreProvider/DocumentStoreProvider");
jest.mock("../../providers/patientDetailsProvider/PatientDetailsProvider"), () => ({
    __esModule: true,
    PatientDetailsContext: createContext(null)
});

describe("<PatientSummaryPage/>", () => {
    const getPatientDetailsMock = jest.fn();

    beforeEach(() => {
        useAuthorisedDocumentStore.mockReturnValue({getPatientDetails: getPatientDetailsMock});
    });


    describe("render the page when patient details are found", () => {
        it("displays the patient details when their data is found", async () => {
            const nhsNumber = "9000000000";
            const familyName = "Smith";
            const patientDetails = buildPatientDetails({familyName, nhsNumber});

            useNavigate.mockImplementation(() => jest.fn());
            renderPatientSummaryPage();

            expect(await screen.findByRole("heading", {name: "Verify patient details"})).toBeInTheDocument();
            expect(screen.getByText(familyName)).toBeInTheDocument();
            expect(screen.getByRole("button", {name: "Next"})).toBeInTheDocument();
        });

        it("displays text specific to upload path if user has selected upload", async () => {
            const nhsNumber = "9000000000";
            const patientDetails = buildPatientDetails({nhsNumber});
            const patientDetailsResponse = {result: {patientDetails}};
            const expectedNextPage = "upload";

            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);
            useNavigate.mockImplementation(() => jest.fn());

            renderPatientSummaryPage({nextPage: expectedNextPage});
            userEvent.click(await screen.findByRole("button", {name: "Next"}));

            expect(
                screen.getByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            );
        });

        it("doesn't display text specific to upload path if user has selected download", async () => {
            const nhsNumber = "9000000000";
            const patientDetailsResponse = {result: {patientDetails: buildPatientDetails({nhsNumber})}};
            const expectedNextPage = "download";

            useNavigate.mockImplementation(() => jest.fn());
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientSummaryPage({nextPage: expectedNextPage});
            userEvent.type(screen.getByRole("textbox", {name: "Enter NHS number"}), nhsNumber);
            userEvent.click(screen.getByRole("button", {name: "Search"}));
            userEvent.click(await screen.findByRole("button", {name: "Next"}));

            expect(
                screen.queryByText(
                    "Ensure these patient details match the electronic health records and attachments you are about to upload."
                )
            ).not.toBeInTheDocument();
        });


        it("displays a message when NHS number is superseded", async () => {
            const nhsNumber = "9000000000";
            const supersededNhsNumber = "9000000012";
            const patientDetails = buildPatientDetails({superseded: true, nhsNumber});
            const patientDetailsResponse = {result: {patientDetails}};

            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientSummaryPage();
            userEvent.type(screen.getByRole("textbox", {name: "Enter NHS number"}), supersededNhsNumber);
            userEvent.click(screen.getByRole("button", {name: "Search"}));

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

            renderPatientSummaryPage();
            userEvent.type(screen.getByRole("textbox", {name: "Enter NHS number"}), "0987654321");
            userEvent.click(screen.getByRole("button", {name: "Search"}));

            expect(await screen.findByText("There is a problem")).toBeInTheDocument();
        });


        it("displays a message when patient is sensitive", async () => {
            const nhsNumber = "9124038456";
            const restrictedPatientDetails = {
                result: {patientDetails: buildPatientDetails({nhsNumber, postalCode: null, restricted: true})},
            };

            getPatientDetailsMock.mockResolvedValue(restrictedPatientDetails);

            renderPatientTracePage();

            userEvent.type(screen.getByRole("textbox", {name: "Enter NHS number"}), nhsNumber);
            userEvent.click(screen.getByRole("button", {name: "Search"}));

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
            const patientDetails = buildPatientDetails({nhsNumber});
            const patientDetailsResponse = {result: {patientDetails}};
            const mockNavigate = jest.fn();
            const expectedNextPage = "test/submit";

            useNavigate.mockImplementation(() => mockNavigate);
            getPatientDetailsMock.mockResolvedValue(patientDetailsResponse);

            renderPatientTracePage({nextPage: expectedNextPage});
            userEvent.click(await screen.findByRole("button", {name: "Next"}));

            expect(mockNavigate).toHaveBeenCalledWith(expectedNextPage);
        });
    });
});

const renderPatientSummaryPage = (propsOverride) => {
    const props = {
        expectedNextPage: "download",
        ...propsOverride,
    };

    render(
        <PatientDetailsProvider>
            <PatientSummaryPage {...props} />
        </PatientDetailsProvider>
    );
};