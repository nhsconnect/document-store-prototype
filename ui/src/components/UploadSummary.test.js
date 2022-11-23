import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import UploadSummary from "./UploadSummary";
import { documentUploadStates } from "../enums/documentUploads";
import { formatSize } from "../utils/utils";

describe("The upload summary component", () => {
    it("confirms the NHS number that the records have been uploaded for", () => {
        const nhsNumber = "12345"
        render(<UploadSummary nhsNumber={nhsNumber} documents={[]} documentUploadStates={[]}></UploadSummary>)

        expect(screen.getByText(`Summary of uploaded documents for patient number ${nhsNumber}`))
    })

    it("displays a collapsible list of the successfully uploaded files", async () => {
        const documents = [makeTextFile("one", 100), makeTextFile("two", 101)]
        const states = documents.map(() => ({ state: documentUploadStates.SUCCEEDED }))

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        toggleSuccessfulUploads()

        expect(await screen.findByRole("table")).toBeInTheDocument()
        documents.forEach((document) => {
            expect(screen.getByText(document.name)).toBeVisible()
            expect(screen.getByText(formatSize(document.size))).toBeVisible()
        })

        toggleSuccessfulUploads()

        await waitFor(() => {
            expect(screen.getByRole("table")).not.toBeVisible()
        })
    })

    it("does not include failed uploads in the successful uploads list", async () => {
        const documents = [makeTextFile("one", 100), makeTextFile("two", 101)]
        const states = [{ state: documentUploadStates.SUCCEEDED }, { state: documentUploadStates.FAILED }]

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        toggleSuccessfulUploads()

        expect(await screen.findByRole("table")).toBeVisible()

        expect(within(screen.getByRole("table")).getByText(documents[0].name)).toBeVisible()

        expect(within(screen.getByRole("table")).queryByText(documents[1].name)).not.toBeInTheDocument()
    })

    it("does not display the successful uploads list when all of the documents failed to upload", () => {
        const documents = [makeTextFile("one", 100)]
        const states = [{ state: documentUploadStates.FAILED }]

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        expect(screen.queryByText("Successfully uploaded documents")).not.toBeInTheDocument()
    })

    it("does not display an alert if all the documents were uploaded successfully", () => {
        const documents = [makeTextFile("one", 100)]
        const states = [{ state: documentUploadStates.SUCCEEDED }]

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        expect(screen.queryByRole("alert")).not.toBeInTheDocument()
    })

    it("displays an alert if some of the documents failed to upload successfully", () => {
        const documents = [makeTextFile("one", 100), makeTextFile("two", 200)]
        const states = [{ state: documentUploadStates.SUCCEEDED }, { state: documentUploadStates.FAILED }]

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        expect(screen.getByRole("alert")).toBeInTheDocument()
    })

    it("renders the name of each document that failed to upload inside an alert", () => {
        const documents = [makeTextFile("one", 100), makeTextFile("two", 200)]
        const states = documents.map(() => ({ state: documentUploadStates.FAILED }))

        render(<UploadSummary nhsNumber={"12345"} documents={documents} documentUploadStates={states}></UploadSummary>)

        documents.forEach((document) => {
            expect(within(screen.getByRole("alert")).getByText(document.name)).toBeInTheDocument()
        })
    })
})

const toggleSuccessfulUploads = () => {
    userEvent.click(screen.getByText("Successfully uploaded documents"))
}

const makeTextFile = (name, size) => {
    const file = new File(["test"], `${name}.txt`, {
        type: "text/plain",
    });
    if (size) {
        Object.defineProperty(file, "size", {
            value: size,
        })
    }
    return file;
}