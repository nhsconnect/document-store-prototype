import { Button, Input } from "nhsuk-react-components";
import React, {useState} from "react";
import { useForm } from "react-hook-form";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";

const states = {
    IDLE: "idle",
    UPLOADING: "uploading",
    SUCCEEDED: "succeeded",
    FAILED: "failed",
    FILE_SIZE_ERROR: "file-size-error"
}

const UploadPage = ({ client }) => {
    const showMetadataFields = useFeatureToggle("SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE");
    const { register, handleSubmit } = useForm();
    const { ref: documentInputRef, ...documentInputProps } =
        register("document");
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const { ref: documentTitleRef, ...documentTitleProps } = register("documentTitle");
    const [submissionState, setSubmissionState] = useState(states.IDLE);

    const doSubmit = async (data) => {
        try{
            const fileSize = data.document[0].size
            if (fileSize < 5*107374184){
                setSubmissionState(states.UPLOADING);
                await client.uploadDocument(data.document[0], data.nhsNumber, data.documentTitle);
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
                        name = "nhsNumber"
                        type="text"
                        {...nhsNumberProps}
                        inputRef={nhsNumberRef}
                    />
                    <Input
                        id={"document-title-input"}
                        label="Enter Document Title"
                        type="text"
                        name = "documentTitle"
                        placeholder="Document Title"
                        {...documentTitleProps}
                        inputRef = {documentTitleRef}
                    />
                    {showMetadataFields && (
                        <>
                            <Input
                                id={"clinical-code-input"}
                                label="Enter Clinical Code"
                                type="text"
                                placeholder="Clinical Code"
                            />
                        </>
                    )}
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
