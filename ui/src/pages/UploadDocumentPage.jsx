import { Button, Fieldset, Input } from "nhsuk-react-components";
import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import BackButton from "../components/BackButton";
import DocumentsInput from "../components/DocumentsInput";

const states = {
    IDLE: "idle",
    UPLOADING: "uploading",
    FAILED: "failed",
};
const oneGigaByte = 107374184;

const UploadDocumentPage = ({ client }) => {
    const { register, handleSubmit, formState, control } = useForm();
    const { ref: documentsInputRef, ...documentsInputProps } = register(
        "documents",
        {
            validate: {
                isFile: (value) =>{
                    return value.length > 0 || "Please attach a file"
                },
                isLessThan5GB: (value) =>{
                    for(let i=0;i<value.length;i++){
                        if(value.item(i).size > 5 * oneGigaByte){
                            return "One or more documents have a size greater than 5GB - please upload a smaller file"
                        }
                    }
                }
            },
        }
    );
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [failedUploads, setFailedUploads] = useState([]);
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/upload/patient-trace");
        }
    }, [nhsNumber, navigate]);

    const doSubmit = async (data) => {
        setSubmissionState(states.UPLOADING);
        setFailedUploads([]);
        const uploadPromises = []

        data.documents.forEach((document) => {
            uploadPromises.push(
                client.uploadDocument(
                    document,
                    data.nhsNumber
                ).catch((e) => {
                    setFailedUploads(current => [...current, document])
                    console.error(e);
                    throw e
                })
            )
        })

        try {
            await Promise.all(uploadPromises)
            navigate("/upload/success");
        } catch (e) {
            setSubmissionState(states.FAILED)
        }
    };

    return (
        <>
            <BackButton />
            <form onSubmit={handleSubmit(doSubmit)} noValidate>
                <Fieldset>
                    <Fieldset.Legend headingLevel={"h1"} isPageHeading>
                        Upload a document
                    </Fieldset.Legend>
                    <Input
                        id={"nhs-number-input"}
                        label="NHS number"
                        name="nhsNumber"
                        type="text"
                        {...nhsNumberProps}
                        inputRef={nhsNumberRef}
                        value={nhsNumber}
                        readOnly
                    />
                    <DocumentsInput control = {control}/>
                </Fieldset>
                <Button
                    type="submit"
                    disabled={submissionState === states.UPLOADING}
                >
                    Upload
                </Button>
                {submissionState === states.UPLOADING && (
                    <p>
                        <progress aria-label={"Loading..."} />
                    </p>
                )}
                {submissionState === states.FAILED && (
                    <>
                        <p data-testid="failure-message">
                            Some of your documents failed to upload
                        </p>
                        <ul>{failedUploads.map(failedUpload => <li key={failedUpload.name}>
                            Upload of {failedUpload.name} failed - please retry
                            </li>)
                        }</ul>

                    </>
                )}
            </form>
        </>
    );
};

export default UploadDocumentPage;
