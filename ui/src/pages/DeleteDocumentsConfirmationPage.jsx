import React, {useEffect, useState} from "react";
import {Button, Fieldset, Radios} from "nhsuk-react-components";
import BackButton from "../components/BackButton";
import {usePatientDetailsProviderContext} from "../providers/PatientDetailsProvider";
import useApi from "../apiClients/useApi";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {useDeleteDocumentsResponseProviderContext} from "../providers/DeleteDocumentsResponseProvider";

const DeleteDocumentsConfirmationPage = () => {
    const client = useApi();
    const { register, handleSubmit } = useForm();
    let navigate = useNavigate();
    const [patientDetails] = usePatientDetailsProviderContext();
    const [patientName, setPatientName] = useState([]);
    const { ref: trxRef, ...trxProps } = register("trx");
    const [, setDeleteDocumentsResponseState] = useDeleteDocumentsResponseProviderContext();

    useEffect(() => {
        async function fetchPatientDetails() {
            if (patientDetails?.nhsNumber) {
                const response = await client.getPatientDetails(patientDetails.nhsNumber);
                setPatientName([response.result.patientDetails.givenName, response.result.patientDetails.familyName]);
            }
        }

        fetchPatientDetails().catch((error) => console.error(error));

        // Todo: Remove the suppression when we provide a client to the dependency array that remains stable between renders
        // eslint-disable-next-line
    }, [patientDetails, setPatientName]);

    const doSubmit = async (data) => {
        if (data.trx === "yes") {
            try {
                const response = await client.deleteAllDocuments(patientDetails?.nhsNumber);
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
                        Are you sure you want to permanently delete all files for patient {patientName[1]}{" "}
                        {patientName[0]} NHS number {patientDetails?.nhsNumber} ?
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
