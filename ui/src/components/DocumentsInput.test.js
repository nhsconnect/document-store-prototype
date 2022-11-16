import {render, screen} from "@testing-library/react";
import DocumentsInput from "./DocumentsInput";
import {useForm} from "react-hook-form";
import userEvent from "@testing-library/user-event";
import {formatSize} from "../utils/utils";

const FormWrapper = () =>{
    const {control} = useForm();
    return <DocumentsInput control={control}/>;
};

describe("DocumentsInput", () => {
    it("renders the choose file input", () => {
        render(<FormWrapper/>);
        expect(
            screen.getByLabelText("Choose documents")
        ).toBeInTheDocument();
    });

    it("renders a list of the selected documents",
        async () => {
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

        });