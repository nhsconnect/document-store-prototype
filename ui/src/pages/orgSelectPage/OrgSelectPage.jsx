import React, { useState } from "react";
import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";

import axios from "axios";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import ErrorBox from "../../components/errorBox/ErrorBox";

const OrgSelectPage = () => {
    const [session] = useSessionContext();
    const [inputError, setInputError] = useState("");
    const navigate = useNavigate();
    const { register, handleSubmit, formState, getFieldState } = useForm();

    const { ref: organisationRef, ...organisationProps } = register("organisation");
    const { isDirty: isOrganisationDirty } = getFieldState("organisation", formState);

    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        if (!session.organisations) {
            navigate(routes.HOME);
        }
    });

    const submit = (organisation) => {
        console.log(organisation);
        if (!isOrganisationDirty) {
            setInputError("Select one organisation you would like to view");
        }

        axios
            .get(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                withCredentials: true,
                params: { odsCode: organisation.odsCode },
            })
            .then((res) => {
                console.log(JSON.stringify(res.data, null, 4));
                navigate(routes.HOME);
            })
            .catch(() => {
                navigate(routes.AUTH_ERROR);
            });
    };

    const toSentenceCase = (string) => {
        return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
    };

    // const handleEmptySubmit = () => {
    //     setInputError("Select one organisation you would like to view");
    // };

    // for testing
    const organisations = [
        {
            orgName: "ORGANISATION ONEEEE",
            odsCode: "A9A5A",
            orgType: "Primary Care Support England",
        },
        {
            orgName: "ORG TWO",
            odsCode: "A9A6B",
            orgType: "GP Practice",
        },
        {
            orgName: "ORG THREEEEEE",
            odsCode: "B1B1B",
            orgType: "Primary Care Support England",
        },
    ];

    return (
        <div style={{ maxWidth: 720 }}>
            {inputError && (
                <ErrorBox
                    messageTitle={"There is a problem"}
                    messageLinkBody={inputError}
                    errorInputLink={"#select-org-input"}
                    errorBoxSummaryId={"error-box-summary"}
                />
            )}
            <form onSubmit={handleSubmit(submit)} style={{ maxWidth: 720 }}>
                <Fieldset>
                    <Fieldset.Legend headingLevel="h1" isPageHeading>
                        Select an organisation
                    </Fieldset.Legend>
                    <Radios
                        error={inputError}
                        hint="You are associated to more than one organisation, select an organisation you would like to view."
                    >
                        {organisations.map((item, key) => (
                            <Radios.Radio
                                {...organisationProps}
                                key={key}
                                value={item.odsCode}
                                inputRef={organisationRef}
                            >
                                {inputError && (
                                    <p id={"select-org-input"}>Select one organisation you would like to view</p>
                                )}
                                <h4 style={{ margin: 0, padding: 0 }}>{toSentenceCase(item.orgName)}</h4>
                                <p>
                                    [{item.odsCode}] {item.orgType}
                                </p>
                            </Radios.Radio>
                        ))}
                    </Radios>
                </Fieldset>
                <Button type="submit">Continue</Button>
            </form>
        </div>
    );
};

export default OrgSelectPage;
