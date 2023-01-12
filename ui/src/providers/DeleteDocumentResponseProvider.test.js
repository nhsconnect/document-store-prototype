import {render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
    DeleteDocumentsResponseProvider,
    useDeleteDocumentsResponseProviderContext
} from "./DeleteDocumentsResponseProvider";


const TestComponent = ({ newDeleteDocumentsResponse }) => {
    const [deleteDocumentsResponse, setDeleteDocumentsResponse] = useDeleteDocumentsResponseProviderContext();
    return (
        <div>
            <p>Delete Documents Response : { deleteDocumentsResponse || "Null"}</p>
            <button onClick={() => setDeleteDocumentsResponse(newDeleteDocumentsResponse)}>Update Delete Documents Response</button>
        </div>
    );
};
describe("The Delete Documents Response provider", () => {
    it("provides delete documents response value and setter", () => {
        const deleteDocumentsResponse = "successful";
        render(
            <DeleteDocumentsResponseProvider>
                <TestComponent newDeleteDocumentsResponse={deleteDocumentsResponse} />
            </DeleteDocumentsResponseProvider>
        );
        expect(screen.getByText("Delete Documents Response : Null")).toBeInTheDocument();

        userEvent.click(screen.getByText("Update Delete Documents Response"));

        expect(screen.getByText(`Delete Documents Response : ${deleteDocumentsResponse}`)).toBeInTheDocument();
    });
});