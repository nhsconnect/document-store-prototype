import { render, waitFor, within, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { nanoid } from "nanoid/non-secure";
import UploadSummary from "./UploadSummary";
import { documentUploadStates } from "../enums/documentUploads";
import { formatSize } from "../utils/utils";

describe("The upload summary component", () => {
    it("confirms the NHS number that the records have been uploaded for", () => {
        const nhsNumber = "12345";
        render(<UploadSummary nhsNumber={nhsNumber} documents={[]}></UploadSummary>);

        expect(screen.getByText(`Summary of uploaded documents for patient number ${nhsNumber}`));
    });

    it("displays a collapsible list of the successfully uploaded files", async () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = files.map((file) => makeDocument(file, documentUploadStates.SUCCEEDED));

        render(<UploadSummary nhsNumber={"12345"} documents={documents}></UploadSummary>);

        toggleSuccessfulUploads();

        expect(await screen.findByRole("table")).toBeInTheDocument();
        files.forEach((file) => {
            expect(screen.getByText(file.name)).toBeVisible();
            expect(screen.getByText(formatSize(file.size))).toBeVisible();
        });

        toggleSuccessfulUploads();

        await waitFor(() => {
            expect(screen.getByRole("table")).not.toBeVisible();
        });
    });

    it("does not include failed uploads in the successful uploads list", async () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = [
            makeDocument(files[0], documentUploadStates.SUCCEEDED),
            makeDocument(files[1], documentUploadStates.FAILED),
        ];

        render(<UploadSummary nhsNumber={"12345"} documents={documents}></UploadSummary>);

        toggleSuccessfulUploads();

        expect(await screen.findByRole("table")).toBeVisible();

        expect(within(screen.getByRole("table")).getByText(files[0].name)).toBeVisible();

        expect(within(screen.getByRole("table")).queryByText(files[1].name)).not.toBeInTheDocument();
    });

    it("does not display the successful uploads list when all of the documents failed to upload", () => {
        const files = [makeTextFile("one", 100)];

        render(<UploadSummary nhsNumber={"12345"} documents={files}></UploadSummary>);

        expect(screen.queryByText("Successfully uploaded documents")).not.toBeInTheDocument();
    });

    it("does not display an alert if all the documents were uploaded successfully", () => {
        const files = [makeTextFile("one", 100)];
        const documents = [makeDocument(files[0], documentUploadStates.SUCCEEDED)];

        render(<UploadSummary nhsNumber={"12345"} documents={documents}></UploadSummary>);

        expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });

    it("displays an alert if some of the documents failed to upload successfully", () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = [
            makeDocument(files[0], documentUploadStates.SUCCEEDED),
            makeDocument(files[1], documentUploadStates.FAILED),
        ];

        render(<UploadSummary nhsNumber={"12345"} documents={documents}></UploadSummary>);

        expect(screen.getByRole("alert")).toBeInTheDocument();
    });

    it("renders the name of each document that failed to upload inside an alert", () => {
        const files = [makeTextFile("one", 100), makeTextFile("two", 101)];
        const documents = files.map((file) => makeDocument(file, documentUploadStates.FAILED));

        render(<UploadSummary nhsNumber={"12345"} documents={documents}></UploadSummary>);

        files.forEach((file) => {
            expect(within(screen.getByRole("alert")).getByText(file.name)).toBeInTheDocument();
        });
    });
});

const toggleSuccessfulUploads = () => {
    userEvent.click(screen.getByLabelText("Show successfully uploaded documents"));
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
