import React from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";
import { usePatientDetailsProviderContext } from "../providers/PatientDetailsProvider";
import useApi from "../apiClients/useApi";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useDeleteDocumentsResponseProviderContext } from "../providers/DeleteDocumentsResponseProvider";

const DeleteDocumentsConfirmationPage = () => {
    const client = useApi();
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const [patientDetails] = usePatientDetailsProviderContext();
    const { ref: trxRef, ...trxProps } = register("trx");
    const [, setDeleteDocumentsResponseState] = useDeleteDocumentsResponseProviderContext();

    const doSubmit = async (data) => {
        if (data.trx === "yes") {
            try {
                const response = await client.deleteAllDocuments(patientDetails.nhsNumber);
                if (response === "successfully deleted") {
                    setDeleteDocumentsResponseState("successful");
                    navigate("/search/results");
                }
            } catch (error) {
                setDeleteDocumentsResponseState("unsuccessful");
                navigate("/search/results");
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
                        Are you sure you want to permanently delete all files for patient{" "}
                        {patientDetails.givenName?.join(" ")} {patientDetails.familyName} NHS number{" "}
                        {patientDetails.nhsNumber} ?
                    </Fieldset.Legend>
                    <Radios name="delete-documents-action">
                        <Radios.Radio id="yes" value="yes" inputRef={trxRef} {...trxProps}>
                            Yes
                        </Radios.Radio>
                        <Radios.Radio id="no" value="no" inputRef={trxRef} {...trxProps} defaultChecked>
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
