import { render, screen, waitFor } from "@testing-library/react";
import DocumentsInput from "./DocumentsInput";
import { useForm } from "react-hook-form";
import userEvent from "@testing-library/user-event";
import { formatSize } from "../../utils/utils";

jest.mock("../../utils/utils", () => ({
    ...jest.requireActual("../../utils/utils"),
    toFileList: () => [],
}));

describe("<DocumentsInput />", () => {
    it("renders the choose file input", () => {
        render(<DocumentsInputFormWrapper />);

        expect(screen.getByLabelText("Select file(s)")).toBeInTheDocument();
        expect(
            screen.getByText("A patient's full electronic health record including attachments must be uploaded.")
        ).toBeInTheDocument();
        expect(screen.getByText("You can select multiple files to upload at once.")).toBeInTheDocument();
        expect(screen.getByText("Primary Care Support England")).toBeInTheDocument();
    });

    it("renders a list of the selected documents", async () => {
        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["document two"], "two.txt", {
            type: "text/plain",
        });

        render(<DocumentsInputFormWrapper />);
        userEvent.upload(screen.getByLabelText("Select file(s)"), [documentOne, documentTwo]);

        expect(await screen.findByText(documentOne.name)).toBeInTheDocument();
        expect(screen.getByText(documentTwo.name)).toBeInTheDocument();
        expect(screen.getByText(formatSize(documentOne.size))).toBeInTheDocument();
        expect(screen.getByText(formatSize(documentTwo.size))).toBeInTheDocument();
    });

    it("validates that a file has been selected", async () => {
        render(<DocumentsInputFormWrapper />);
        userEvent.click(screen.getByText("Submit"));

        expect(await screen.findByText("Please select a file")).toBeInTheDocument();
    });

    it("validates that all of the selected files are less than 5GB", async () => {
        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["two"], "two.txt", {
            type: "text/plain",
        });
        Object.defineProperty(documentTwo, "size", {
            value: 5 * Math.pow(1024, 3) + 1,
        });
        const documentThree = new File(["three"], "three.txt", {
            type: "text/plain",
        });

        render(<DocumentsInputFormWrapper />);
        userEvent.upload(screen.getByLabelText("Select file(s)"), [documentOne, documentTwo, documentThree]);
        userEvent.click(screen.getByText("Submit"));

        expect(await screen.findByText("Please ensure that all files are less than 5GB in size"));
    });

    it("removes selected file", async () => {
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });

        render(<DocumentsInputFormWrapper />);
        userEvent.upload(screen.getByLabelText("Select file(s)"), [document]);
        userEvent.click(
            await screen.findByRole("link", {
                name: `Remove ${document.name} from selection`,
            })
        );

        expect(screen.queryByText(document.name)).not.toBeInTheDocument();
    });

    it("adds new file selections to the existing selection", async () => {
        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["document two"], "two.txt", {
            type: "text/plain",
        });

        render(<DocumentsInputFormWrapper />);
        const selectFilesLabel = screen.getByLabelText("Select file(s)");
        userEvent.upload(selectFilesLabel, [documentOne]);
        const documentOneName = await screen.findByText(documentOne.name);

        expect(documentOneName).toBeInTheDocument();

        userEvent.upload(selectFilesLabel, [documentTwo]);

        expect(documentOneName).toBeInTheDocument();
        expect(await screen.findByText(documentTwo.name)).toBeInTheDocument();
    });

    it("warns the user if they have added the same file twice", async () => {
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });
        const duplicateDocument = new File(["test"], "test.txt", {
            type: "text/plain",
        });
        const duplicateFileWarning = "There are two or more documents with the same name.";

        render(<DocumentsInputFormWrapper />);
        userEvent.upload(screen.getByLabelText("Select file(s)"), [document, duplicateDocument]);

        expect(await screen.findByText(duplicateFileWarning)).toBeInTheDocument();

        userEvent.click(
            screen.getAllByRole("link", {
                name: `Remove ${duplicateDocument.name} from selection`,
            })[1]
        );

        await waitFor(() => {
            expect(screen.queryByText(duplicateFileWarning)).not.toBeInTheDocument();
        });
    });

    it("allows the user to add the same file again if they remove it", async () => {
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });

        render(<DocumentsInputFormWrapper />);
        const selectFilesLabel = screen.getByLabelText("Select file(s)");
        userEvent.upload(selectFilesLabel, document);
        userEvent.click(await screen.findByRole("link", { name: `Remove ${document.name} from selection` }));
        userEvent.upload(selectFilesLabel, document);

        expect(await screen.findByText(document.name)).toBeInTheDocument();
    });

    it("renders link to PCSE that opens in a new tab", () => {
        render(<DocumentsInputFormWrapper />);

        const pcseLink = screen.getByRole("link", { name: "Primary Care Support England" });
        expect(pcseLink).toHaveAttribute("href", "https://secure.pcse.england.nhs.uk/");
        expect(pcseLink).toHaveAttribute("target", "_blank");
    });
});

const DocumentsInputFormWrapper = () => {
    const { control, handleSubmit } = useForm();

    return (
        <form onSubmit={handleSubmit(undefined, undefined)}>
            <DocumentsInput control={control} />
            <button type="submit">Submit</button>
        </form>
    );
};
