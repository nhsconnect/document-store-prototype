import { render, screen, waitFor } from "@testing-library/react";
import DocumentsInput from "./DocumentsInput";
import { useForm } from "react-hook-form";
import userEvent from "@testing-library/user-event";
import { formatSize } from "../utils/utils";

jest.mock("../utils/utils", () => ({
    ...jest.requireActual("../utils/utils"),
    toFileList: () => [],
}));

const FormWrapper = () => {
    const { control, handleSubmit } = useForm();
    return (
        <form onSubmit={handleSubmit()}>
            <DocumentsInput control={control} />
            <button type="submit">Submit</button>
        </form>
    );
};

describe("DocumentsInput", () => {
    it("renders the choose file input", () => {
        render(<FormWrapper />);
        expect(screen.getByLabelText("Select files")).toBeInTheDocument();
    });

    it("renders a list of the selected documents", async () => {
        render(<FormWrapper />);

        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["document two"], "two.txt", {
            type: "text/plain",
        });

        userEvent.upload(screen.getByLabelText("Select files"), [documentOne, documentTwo]);

        expect(await screen.findByText(documentOne.name)).toBeInTheDocument();
        expect(screen.getByText(documentTwo.name)).toBeInTheDocument();
        expect(screen.getByText(formatSize(documentOne.size))).toBeInTheDocument();
        expect(screen.getByText(formatSize(documentTwo.size))).toBeInTheDocument();
    });

    it("validates that a file has been selected", async () => {
        render(<FormWrapper />);

        userEvent.click(screen.getByText("Submit"));

        expect(await screen.findByText("Please select a file")).toBeInTheDocument();
    });

    it("validates that all of the selected files are less than 5GB", async () => {
        render(<FormWrapper />);

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

        userEvent.upload(screen.getByLabelText("Select files"), [documentOne, documentTwo, documentThree]);

        userEvent.click(screen.getByText("Submit"));

        expect(await screen.findByText("Please ensure that all files are less than 5GB in size"));
    });
    it("should be able to remove selected file", async () => {
        render(<FormWrapper />);
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });
        userEvent.upload(screen.getByLabelText("Select files"), [document]);

        expect(screen.getByText(document.name)).toBeInTheDocument();
        userEvent.click(
            screen.getByRole("button", {
                name: `Remove ${document.name} from selection`,
            })
        );
        await waitFor(() => expect(screen.queryByText(document.name)).not.toBeInTheDocument());
    });

    it("adds new file selections to the existing selection", async () => {
        render(<FormWrapper />);

        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["document two"], "two.txt", {
            type: "text/plain",
        });

        userEvent.upload(screen.getByLabelText("Select files"), [documentOne]);

        expect(await screen.findByText(documentOne.name)).toBeInTheDocument();

        userEvent.upload(screen.getByLabelText("Select files"), [documentTwo]);

        expect(screen.getByText(documentOne.name)).toBeInTheDocument();

        expect(await screen.findByText(documentTwo.name)).toBeInTheDocument();
    });

    it("warns the user if they have added the same file twice", async () => {
        render(<FormWrapper />);
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });
        const duplicateDocument = new File(["test"], "test.txt", {
            type: "text/plain",
        });
        userEvent.upload(screen.getByLabelText("Select files"), [document, duplicateDocument]);

        await waitFor(() =>
            expect(screen.queryByText("There are two or more documents with the same name.")).toBeInTheDocument()
        );

        userEvent.click(
            screen.getAllByRole("button", {
                name: `Remove ${duplicateDocument.name} from selection`,
            })[1]
        );

        await waitFor(() =>
            expect(screen.queryByText("There are two or more documents with the same name.")).not.toBeInTheDocument()
        );
    });

    it("allows the user to add the same file again if they remove it", async () => {
        render(<FormWrapper />);
        const document = new File(["test"], "test.txt", {
            type: "text/plain",
        });

        userEvent.upload(screen.getByLabelText("Select files"), document);
        expect(await screen.findByText(document.name)).toBeInTheDocument();

        userEvent.click(
            screen.getByRole("button", {
                name: `Remove ${document.name} from selection`,
            })
        );
        await waitFor(() => expect(screen.queryByText(document.name)).not.toBeInTheDocument());

        userEvent.upload(screen.getByLabelText("Select files"), document);
        expect(await screen.findByText(document.name)).toBeInTheDocument();
    });
});
