import { Button, Fieldset, Input, Select } from "nhsuk-react-components";
import React, { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import BackButton from "../components/BackButton";

const states = {
    IDLE: "idle",
    UPLOADING: "uploading",
    FAILED: "failed",
};

const UploadDocumentPage = ({ client }) => {
    const { register, handleSubmit, formState } = useForm();
    const { ref: documentInputRef, ...documentInputProps } = register(
        "document",
        {
            validate: {
                isFile: (value) =>
                    value[0] instanceof File || "Please attach a file",
                isLessThan5GB: (value) =>
                    value[0]?.size <= 5 * 107374184 ||
                    "File size greater than 5GB - upload a smaller file",
            },
        }
    );
    const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
    const { ref: documentTitleRef, ...documentTitleProps } = register(
        "documentTitle",
        { required: "Please enter document title" }
    );
    const { ref: clinicalCodeRef, ...clinicalCodeProps } =
        register("clinicalCode");
    const [submissionState, setSubmissionState] = useState(states.IDLE);
    const [nhsNumber] = useNhsNumberProviderContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (!nhsNumber) {
            navigate("/upload/patient-trace");
        }
    }, [nhsNumber, navigate]);

    const doSubmit = async (data) => {
        try {
            setSubmissionState(states.UPLOADING);
            await client.uploadDocument(
                data.document[0],
                data.nhsNumber,
                data.documentTitle,
                data.clinicalCode
            );
            navigate("/upload/success");
        } catch (e) {
            setSubmissionState(states.FAILED);
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
                    <Input
                        id={"document-title-input"}
                        label="Enter Document Title"
                        type="text"
                        name="documentTitle"
                        placeholder="Document Title"
                        error={formState.errors.documentTitle?.message}
                        {...documentTitleProps}
                        inputRef={documentTitleRef}
                    />
                    <Select
                        name="clinicalCode"
                        label="Select Clinical Code"
                        {...clinicalCodeProps}
                        selectRef={clinicalCodeRef}
                    >
                        <Select.Option value="22151000087106" defaultValue>
                            Paper Report (Record Artifact)
                        </Select.Option>
                    </Select>
                    <Input
                        id={"document-input"}
                        label="Choose document"
                        type="file"
                        multiple={false}
                        name="document"
                        error={formState.errors.document?.message}
                        {...documentInputProps}
                        inputRef={documentInputRef}
                    />
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
                    <p data-testid="failure-message">
                        File upload failed - please retry
                    </p>
                )}
            </form>
        </>
    );
};

export default UploadDocumentPage;
