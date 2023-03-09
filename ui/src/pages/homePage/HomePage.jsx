import React from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import BackButton from "../../components/backButton/BackButton";
import routes from "../../enums/routes";

const HomePage = () => {
    const { register, handleSubmit, formState, getFieldState } = useForm();
    const navigate = useNavigate();

    const { ref: isDownloadOrUploadRef, ...isDownloadOrUploadProps } = register("isDownloadOrUpload");
    const { isDirty: isDownloadOrUploadDirty } = getFieldState("isDownloadOrUpload", formState);

    const submit = ({ isDownloadOrUpload }) => {
        const { SEARCH_PATIENT, UPLOAD_SEARCH_PATIENT } = routes;
        const path = isDownloadOrUpload === "download" ? SEARCH_PATIENT : UPLOAD_SEARCH_PATIENT;

        navigate(path, { replace: false });
    };

    return (
        <>
            <BackButton />
            <form onSubmit={handleSubmit(submit)}>
                <Fieldset>
                    <Fieldset.Legend headingLevel="h1" isPageHeading>
                        How do you want to use the service?
                    </Fieldset.Legend>
                    <Radios hint="Select an option">
                        <Radios.Radio
                            {...isDownloadOrUploadProps}
                            id="download"
                            value="download"
                            inputRef={isDownloadOrUploadRef}
                        >
                            Download and/or delete documents
                        </Radios.Radio>
                        <Radios.Radio
                            {...isDownloadOrUploadProps}
                            id="upload"
                            value="upload"
                            inputRef={isDownloadOrUploadRef}
                        >
                            Upload documents
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                <Button type="submit" disabled={!isDownloadOrUploadDirty}>
                    Continue
                </Button>
            </form>
        </>
    );
};

export default HomePage;
