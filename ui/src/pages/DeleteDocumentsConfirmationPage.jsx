import React from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";
import { useNhsNumberProviderContext } from "../providers/NhsNumberProvider";

const DeleteDocumentsConfirmationPage = () => {
    const [nhsNumber] = useNhsNumberProviderContext();

    return (
        <>
            <BackButton />
            <Fieldset>
                <Fieldset.Legend isPageHeading>
                    Delete health records and attachments
                </Fieldset.Legend>
                <Fieldset.Legend size="m">
                    Are you sure you want to permanently delete all files for
                    patient NHS number {nhsNumber} ?
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
