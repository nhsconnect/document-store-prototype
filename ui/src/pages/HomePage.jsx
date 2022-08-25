import React, {useEffect, useRef, useState} from "react";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";
import {Button, ButtonLink, Fieldset, Radios} from "nhsuk-react-components";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";

const HomePage = () => {
    const isPdsTraceEnabled = useFeatureToggle("PDS_TRACE_ENABLED");
    const uploadPathHref = isPdsTraceEnabled
        ? "/upload/patient-trace"
        : "/upload";
    const searchPathHref = isPdsTraceEnabled
        ? "/search/patient-trace"
        : "/search";
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const { ref: downloadTrxRef, ...downloadTrxProps } = register("downloadTrx");
    const { ref: uploadTrxRef, ...uploadTrxProps } = register("uploadTrx");

    const doSubmit = async (data) => {
        const location = data.downloadTrx ? searchPathHref : uploadPathHref;
        navigate(location, { replace: false });
    };

    return (
        <form onSubmit={handleSubmit(doSubmit)}>
            <Fieldset>
                <Fieldset.Legend headingLevel={'h1'} isPageHeading>How do you want to use the Document Store?</Fieldset.Legend>
                <Radios
                    name="document-store-action"
                    hint="Select an option">
                    <Radios.Radio id="download" value="true" inputRef={downloadTrxRef} {...downloadTrxProps}>
                        Download and view a stored document
                    </Radios.Radio>
                    <Radios.Radio id="upload" value="true" inputRef={uploadTrxRef} {...uploadTrxProps}>
                        Upload a document
                    </Radios.Radio>
                </Radios>
            </Fieldset>
            <Button type={"submit"}>Continue</Button>
        </form>
    );
};

export default HomePage;
