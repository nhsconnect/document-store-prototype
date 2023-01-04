import React, {useEffect, useState} from "react";
import {Fieldset, Radios, Button} from "nhsuk-react-components";
import { useNavigate } from "react-router";
import BackButton from "../components/BackButton";
import {useNhsNumberProviderContext} from "../providers/NhsNumberProvider";

const DeleteDocumentsConfirmationPage = () => {
<<<<<<< HEAD
    return <></>;
=======
    const [nhsNumber] = useNhsNumberProviderContext();

    return (
        <>
            <BackButton />
             <Fieldset>
                 <Fieldset.Legend isPageHeading>Delete health records and attachments</Fieldset.Legend>
                    <Fieldset.Legend size="m">Are you sure you want to permanently delete all files for patient NHS number {nhsNumber} ?</Fieldset.Legend>
                 <Radios
                     name="delete-documents-action"
                 >
                        <Radios.Radio
                            id="yes"
                            value="yes"
                        >
                        Yes
                        </Radios.Radio>
                         <Radios.Radio
                            id="no"
                            value="no"
                         >
                        No
                        </Radios.Radio>
                </Radios>
            </Fieldset>
            <Button
            >
             Continue
            </Button>
        </>
    );
>>>>>>> da7e860 ([PRMT-2805] Added DeleteDocumentsConfirmationPage to delete all documents attached to the patient NHS number)
};

export default DeleteDocumentsConfirmationPage;
