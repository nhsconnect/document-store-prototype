import { Button, Input } from "nhsuk-react-components";
import React, {useState} from "react";
import { useForm } from "react-hook-form";

const states = {
    IDLE: "idle",
    UPLOADING: "uploading",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
    FILE_SIZE_ERROR: "file-size-error"
}

const UploadPage = ({ client }) => {
    const { register, handleSubmit } = useForm();
    const { ref: documentInputRef, ...documentInputProps } =
        register("document");
    const [submissionState, setSubmissionState] = useState(states.IDLE);

    const doSubmit = async (data) => {
        try{
            const fileSize = data.document[0].size
            if (fileSize < 5*107374184){
                setSubmissionState(states.UPLOADING);
                await client.uploadDocument(data.document[0]);
                setSubmissionState(states.SUCCEEDED);
            }
            else {
                setSubmissionState(states.FILE_SIZE_ERROR);
            }
        }
        catch (e) {
            setSubmissionState(states.FAILED);
        }
    }

    return (
        <>
            <h2>Upload Patient Records</h2>
            <div>
                <form onSubmit={handleSubmit(doSubmit)}>
                    <Input
                        id={"nhs-number-input"}
                        label="Enter NHS number"
                        type="text"
                        placeholder="012 345 6789"
                    />
                    <Input
                        id={"document-input"}
                        label="Choose document"
                        type="file"
                        multiple={false}
                        name="document"
                        {...documentInputProps}
                        inputRef={documentInputRef}
                    />
                    <Button type="submit">Upload</Button>
                    {submissionState === states.UPLOADING && (
                        <p>
                            <progress aria-label={"Loading..."}>
                            </progress>
                        </p>
                    )}
                    {submissionState === states.SUCCEEDED && (
                        <p>Document uploaded successfully</p>
                    )}
                    {submissionState === states.FAILED && (
                        <p>File upload failed - please retry</p>
                    )}
                    {submissionState === states.FILE_SIZE_ERROR && (
                        <p>File size greater than 5GB - upload a smaller file</p>
                    )}
                </form>
            </div>
        </>
    );
};

export default UploadPage;
