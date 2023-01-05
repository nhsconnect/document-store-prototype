import React, { useEffect, useState } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";

import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";
import useApi from "../apiClients/useApi";

const DeleteDocumentsConfirmationPage = () => {
    const client = useApi();
    const [nhsNumber] = useNhsNumberProviderContext();
    const [patientName, setPatientName] = useState([]);

    useEffect(async () => {
        try {
            if (nhsNumber) {
                const response = await client.getPatientDetails(nhsNumber);
                setPatientName([
                    response.result.patientDetails.givenName,
                    response.result.patientDetails.familyName,
                ]);
            }
        } catch (e) {
            console.log(e);
        }
    }, [nhsNumber, setPatientName]);

    return (
        <>
            <BackButton />
            <Fieldset>
                <Fieldset.Legend isPageHeading>
                    Delete health records and attachments
                </Fieldset.Legend>
                <Fieldset.Legend size="m">
                    Are you sure you want to permanently delete all files for
                    patient {patientName[1]} {patientName[0]} NHS number{" "}
                    {nhsNumber} ?
                </Fieldset.Legend>
                <Radios name="delete-documents-action">
                    <Radios.Radio id="yes" value="yes">
                        Yes
                    </Radios.Radio>
                    <Radios.Radio id="no" value="no">
                        No
                    </Radios.Radio>
                </Radios>
            </Fieldset>
            <Button>Continue</Button>
        </>
    );
};

export default DeleteDocumentsConfirmationPage;
