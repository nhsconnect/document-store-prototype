import React from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";

const HomePage = () => {
    const uploadPathHref = "/upload/patient-trace";
    const searchPathHref = "/search/patient-trace";
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const { ref: trxRef, ...trxProps } = register("trx");

    const doSubmit = async (data) => {
        const location = data.trx === "download" ? searchPathHref : uploadPathHref;
        navigate(location, { replace: false });
    };

    return (
        <>
            <BackButton />
            <form onSubmit={handleSubmit(doSubmit)}>
                <Fieldset>
                    <Fieldset.Legend headingLevel={"h1"} isPageHeading>
                        How do you want to use the Document Store?
                    </Fieldset.Legend>
                    <Radios name="document-store-action" hint="Select an option">
                        <Radios.Radio id="download" value="download" inputRef={trxRef} {...trxProps}>
                            Download and view a stored document
                        </Radios.Radio>
                        <Radios.Radio id="upload" value="upload" inputRef={trxRef} {...trxProps}>
                            Upload a document
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                <Button type={"submit"}>Continue</Button>
            </form>
        </>
    );
};

export default HomePage;
