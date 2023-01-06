import React, { useEffect, useState } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";

import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import useApi from "../apiClients/useApi";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";

const DeleteDocumentsConfirmationPage = () => {
    const client = useApi();
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const [nhsNumber] = useNhsNumberProviderContext();
    const [patientName, setPatientName] = useState([]);
    const { ref: trxRef, ...trxProps } = register("trx");

    useEffect(async () => {
        try {
            if (nhsNumber) {
                const response = await client.getPatientDetails(nhsNumber);
                setPatientName([response.result.patientDetails.givenName, response.result.patientDetails.familyName]);
            }
        } catch (e) {
            console.log(e);
        }
    }, [nhsNumber, setPatientName]);

    const doSubmit = async (data) => {
        if (data.trx === "Yes") {
            try {
                const response = await client.deleteAllDocuments(nhsNumber);
                if (response === "successfully deleted") {
                    navigate("/search/results");
                }
            } catch (e) {
                console.log(e);
            }
        }
        navigate("/search/results");
    };

    return (
        <>
            <BackButton />
            <form onSubmit={handleSubmit(doSubmit)}>
                <Fieldset>
                    <Fieldset.Legend isPageHeading>Delete health records and attachments</Fieldset.Legend>
                    <Fieldset.Legend size="m">
                        Are you sure you want to permanently delete all files for patient {patientName[1]}{" "}
                        {patientName[0]} NHS number {nhsNumber} ?
                    </Fieldset.Legend>
                    <Radios name="delete-documents-action">
                        <Radios.Radio id="yes" value="yes" inputRef={trxRef} {...trxProps}>
                            Yes
                        </Radios.Radio>
                        <Radios.Radio id="no" value="no" inputRef={trxRef} {...trxProps}>
                            No
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                <Button type={"submit"}>Continue</Button>
            </form>
        </>
    );
};

export default DeleteDocumentsConfirmationPage;
