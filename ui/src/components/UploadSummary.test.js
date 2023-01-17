import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { nanoid } from "nanoid/non-secure";
import UploadSummary from "./UploadSummary";
import { documentUploadStates } from "../enums/documentUploads";
import { formatSize, getFormattedDate } from "../utils/utils";

describe("The upload summary component", () => {
    const patientData = {
        birthDate: "2003-01-22",
        familyName: "Smith",
        givenName: ["Jane"],
        nhsNumber: "9234567801",
        postalCode: "LS1 6AE",
    };

    it("displays patient details for the records that have been uploaded", () => {
        render(<UploadSummary patientDetails={patientData} documents={[]}></UploadSummary>);

        expect(screen.getByText("Upload Summary")).toBeInTheDocument();
        expect(screen.getByText(`NHS number ${patientData.nhsNumber}`)).toBeInTheDocument();
        expect(screen.getByText("Before you close this page")).toBeInTheDocument();
    });

    it("displays a collapsible list of the successfully uploaded files", async () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = files.map((file) => makeDocument(file, documentUploadStates.SUCCEEDED));

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        toggleSuccessfulUploads();

        expect(await screen.findByRole("table", { name: "Successfully uploaded documents" })).toBeInTheDocument();
        files.forEach((file) => {
            expect(screen.getByText(file.name)).toBeVisible();
            expect(screen.getByText(formatSize(file.size))).toBeVisible();
        });

        toggleSuccessfulUploads();

        await waitFor(() => {
            expect(screen.getByRole("table", { name: "Successfully uploaded documents" })).not.toBeVisible();
        });
    });

    it("does not include failed uploads in the successful uploads list", async () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = [
            makeDocument(files[0], documentUploadStates.SUCCEEDED),
            makeDocument(files[1], documentUploadStates.FAILED),
        ];

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        toggleSuccessfulUploads();

        expect(await screen.findByRole("table", { name: "Successfully uploaded documents" })).toBeVisible();
        expect(screen.queryByText("All documents have been successfully uploaded")).not.toBeInTheDocument();
        expect(
            within(screen.getByRole("table", { name: "Successfully uploaded documents" })).getByText(files[0].name)
        ).toBeVisible();

        expect(
            within(screen.getByRole("table", { name: "Successfully uploaded documents" })).queryByText(files[1].name)
        ).not.toBeInTheDocument();
    });

    it("does not display the successful uploads list when all of the documents failed to upload", () => {
        const files = [makeTextFile("one", 100)];
        const documents = [makeDocument(files[0], documentUploadStates.FAILED)];

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        expect(screen.queryByText("View successfully uploaded documents")).not.toBeInTheDocument();
        expect(screen.getByText("Some of your documents failed to upload")).toBeInTheDocument();
    });

    it("does not display an alert if all the documents were uploaded successfully", () => {
        const files = [makeTextFile("one", 100)];
        const documents = [makeDocument(files[0], documentUploadStates.SUCCEEDED)];

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
        expect(
            screen.getByText("All documents have been successfully uploaded on " + getFormattedDate(new Date()))
        ).toBeInTheDocument();
    });

    it("displays an alert if some of the documents failed to upload successfully", () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = [
            makeDocument(files[0], documentUploadStates.SUCCEEDED),
            makeDocument(files[1], documentUploadStates.FAILED),
        ];

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        expect(screen.getByRole("alert")).toBeInTheDocument();
    });

    it("renders the name of each document that failed to upload inside a table", () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = files.map((file) => makeDocument(file, documentUploadStates.FAILED));

        render(<UploadSummary patientDetails={patientData} documents={documents}></UploadSummary>);

        expect(screen.getByRole("table", { name: "Failed uploads" })).toBeInTheDocument();
        files.forEach((file) => {
            expect(screen.getByText(file.name)).toBeVisible();
            expect(screen.getByText(formatSize(file.size))).toBeVisible();
        });
    });
});

const toggleSuccessfulUploads = () => {
    userEvent.click(screen.getByLabelText("View successfully uploaded documents"));
};

const makeDocument = (file, uploadStatus) => {
    return {
        file,
        state: uploadStatus ?? documentUploadStates.SUCCEEDED,
        progress: 0,
        id: nanoid(),
    };
};

const makeTextFile = (name, size) => {
    const file = new File(["test"], `${name}.txt`, {
        type: "text/plain",
    });
    if (size) {
        Object.defineProperty(file, "size", {
            value: size,
        });
    }
    return file;
};
