import {render, screen} from "@testing-library/react";
import DocumentsInput from "./DocumentsInput";
import {useForm} from "react-hook-form";
import userEvent from "@testing-library/user-event";
import {formatSize} from "../utils/utils";

const FormWrapper = () =>{
    const {control, handleSubmit} = useForm();
    return <form onSubmit={handleSubmit()}>
        <DocumentsInput control={control}/>
        <button type="submit">Submit</button>
    </form>;
};

describe("DocumentsInput", () => {
    it("renders the choose file input", () => {
        render(<FormWrapper/>);
        expect(
            screen.getByLabelText("Choose documents")
        ).toBeInTheDocument();
    });

    it("renders a list of the selected documents", async () => {
        render(<FormWrapper/>)

        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });
        const documentTwo = new File(["document two"], "two.txt", {
            type: "text/plain",
        });

        userEvent.upload(screen.getByLabelText("Choose documents"), [documentOne, documentTwo]);

        expect(await screen.findByText(documentOne.name)).toBeInTheDocument()
        expect(screen.getByText(documentTwo.name)).toBeInTheDocument()
        expect(screen.getByText(formatSize(documentOne.size))).toBeInTheDocument()
        expect(screen.getByText(formatSize(documentOne.size))).toBeInTheDocument()
    })

    it("validates that a file has been selected", async () => {
        render(<FormWrapper />)

        userEvent.click(screen.getByText("Submit"))

        expect(await screen.findByText('Please select a file')).toBeInTheDocument()
    })

    it('validates that all of the selected files are less than 5GB', async () => {
        render(<FormWrapper />)

        const documentOne = new File(["one"], "one.txt", {
            type: "text/plain",
        });

        const documentTwo = new File(["two"], "two.txt", {
            type: "text/plain",
        });
        Object.defineProperty(documentTwo, 'size', {
            value: 5 * Math.pow(1024, 3) + 1,
        })

        const documentThree = new File(["three"], "three.txt", {
            type: "text/plain",
        });

        userEvent.upload(screen.getByLabelText("Choose documents"), [documentOne, documentTwo, documentThree]);

        userEvent.click(screen.getByText("Submit"))

        expect(await screen.findByText("Please ensure that all files are less than 5GB in size"))
    })

});