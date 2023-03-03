import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import UploadSummary from "./UploadSummary";
import { documentUploadStates } from "../../enums/documentUploads";
import { formatSize, getFormattedDate } from "../../utils/utils";
import { buildDocument, buildPatientDetails, buildTextFile } from "../../utils/testBuilders";

describe("<UploadSummary />", () => {
    it("renders the page", () => {
        const nhsNumber = "9000000009";
        const patientDetails = buildPatientDetails({ nhsNumber });

        renderUploadSummary({ patientDetails, documents: [] });

        expect(screen.getByRole("heading", { name: "Upload Summary" })).toBeInTheDocument();
        expect(
            screen.getByRole("heading", { name: /All documents have been successfully uploaded on/ })
        ).toBeInTheDocument();
        expect(screen.getByText(nhsNumber)).toBeInTheDocument();
        expect(screen.getByText("Before you close this page")).toBeInTheDocument();
        expect(screen.queryByText("Some of your documents failed to upload")).not.toBeInTheDocument();
        expect(screen.queryByText("View successfully uploaded documents")).not.toBeInTheDocument();
    });

    it("displays successfully uploaded docs", () => {
        const files = [buildTextFile("one", 100), buildTextFile("two", 101)];
        const documents = files.map((file) => buildDocument(file, documentUploadStates.SUCCEEDED));

        renderUploadSummary({ documents });

        expect(screen.getByText(/All documents have been successfully uploaded on/)).toBeInTheDocument();
        expect(screen.getByText("View successfully uploaded documents")).toBeInTheDocument();
        const uploadedDocsTable = screen.getByRole("table", { name: "Successfully uploaded documents" });
        files.forEach(({ name, size }) => {
            expect(within(uploadedDocsTable).getByText(name)).toBeInTheDocument();
            expect(within(uploadedDocsTable).getByText(formatSize(size))).toBeInTheDocument();
        });
    });

    it("displays a collapsible list of successfully uploaded docs", () => {
        const files = [buildTextFile(), buildTextFile()];
        const documents = files.map((file) => buildDocument(file, documentUploadStates.SUCCEEDED));

        renderUploadSummary({ documents });

        expect(screen.queryByRole("table", { name: "Successfully uploaded documents" })).not.toBeVisible();

        userEvent.click(screen.getByText("View successfully uploaded documents"));

        expect(screen.getByRole("table", { name: "Successfully uploaded documents" })).toBeVisible();

        userEvent.click(screen.getByText("View successfully uploaded documents"));

        expect(screen.queryByRole("table", { name: "Successfully uploaded documents" })).not.toBeVisible();
    });

    it("does not include docs that failed to upload in the successfully uploaded docs list", () => {
        const uploadedFileName = "one";
        const failedToUploadFileName = "two";
        const documents = [
            buildDocument(buildTextFile(uploadedFileName, 100), documentUploadStates.SUCCEEDED),
            buildDocument(buildTextFile(failedToUploadFileName, 101), documentUploadStates.FAILED),
        ];

        renderUploadSummary({ documents });

        expect(
            screen.queryByRole("heading", { name: /All documents have been successfully uploaded on/ })
        ).not.toBeInTheDocument();
        const uploadedDocsTable = screen.getByRole("table", { name: "Successfully uploaded documents" });
        expect(within(uploadedDocsTable).getByText(`${uploadedFileName}.txt`)).toBeInTheDocument();
        expect(within(uploadedDocsTable).queryByText(`${failedToUploadFileName}.txt`)).not.toBeInTheDocument();
    });

    it("does not display the successfully uploads docs list when all of the docs failed to upload", () => {
        const documents = [buildDocument(buildTextFile(), documentUploadStates.FAILED)];

        renderUploadSummary({ documents });

        expect(
            screen.queryByRole("heading", { name: /All documents have been successfully uploaded on/ })
        ).not.toBeInTheDocument();
        expect(screen.queryByText("View successfully uploaded documents")).not.toBeInTheDocument();
    });

    it("displays message and does not display an alert if all the docs were uploaded successfully", () => {
        const files = [buildTextFile("one", 100)];
        const documents = [buildDocument(files[0], documentUploadStates.SUCCEEDED)];

        renderUploadSummary({ documents });

        expect(
            screen.getByRole("heading", {
                name: "All documents have been successfully uploaded on " + getFormattedDate(new Date()),
            })
        ).toBeInTheDocument();
        expect(
            screen.queryByRole("alert", { name: "Some of your documents failed to upload" })
        ).not.toBeInTheDocument();
    });

    it("displays an alert if some of the docs failed to upload", () => {
        const documents = [
            buildDocument(buildTextFile(), documentUploadStates.SUCCEEDED),
            buildDocument(buildTextFile(), documentUploadStates.FAILED),
        ];

        renderUploadSummary({ documents });

        expect(screen.getByRole("alert", { name: "Some of your documents failed to upload" })).toBeInTheDocument();
        expect(
            screen.getByText(
                "You can try to upload the documents again if you wish and/or make a note of the failures for future reference."
            )
        ).toBeInTheDocument();
        expect(
            screen.getByText(/Please check your internet connection. If the issue persists please contact the/)
        ).toBeInTheDocument();
        expect(screen.getByRole("link", { name: "NHS Digital National Service Desk" })).toBeInTheDocument();
    });

    it("displays each doc that failed to upload in a table", () => {
        const files = [buildTextFile("one", 100), buildTextFile("two", 101)];
        const documents = files.map((file) => buildDocument(file, documentUploadStates.FAILED));

        renderUploadSummary({ documents });

        const failedToUploadDocsTable = screen.getByRole("table", { name: "Failed uploads" });
        files.forEach(({ name, size }) => {
            expect(within(failedToUploadDocsTable).getByText(name)).toBeInTheDocument();
            expect(within(failedToUploadDocsTable).getByText(formatSize(size))).toBeInTheDocument();
        });
    });
});

const renderUploadSummary = (propsOverride) => {
    const props = {
        patientDetails: buildPatientDetails(),
        documents: [],
        ...propsOverride,
    };

    render(<UploadSummary {...props} />);
};
