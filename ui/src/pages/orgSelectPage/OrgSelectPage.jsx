import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";

import axios from "axios";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import ErrorBox from "../../components/errorBox/ErrorBox";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import Spinner from "../../components/spinner/Spinner";

const OrgSelectPage = () => {
    const [session] = useSessionContext();
    const navigate = useNavigate();
    const [inputError, setInputError] = useState("");
    const [loading, setLoading] = useState(false);
    const { register, handleSubmit, formState, getFieldState } = useForm();

    const { ref: organisationRef, ...organisationProps } = register("organisation");
    const { isDirty: isOrganisationDirty } = getFieldState("organisation", formState);

    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        if (!session.organisations) {
            navigate(routes.ROOT);
        }
    });

    const submit = (organisation) => {
        if (!isOrganisationDirty) {
            setInputError("Select one organisation you would like to view");
            return;
        }

        setLoading(true);
        console.log(organisation);

        axios
            .get(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                withCredentials: true,
                params: { odsCode: organisation },
            })
            .then(() => {
                navigate(routes.HOME);
            })
            .catch(() => {
                navigate(routes.AUTH_ERROR);
            });
    };

    const toSentenceCase = (string) => {
        return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
    };

    return !loading ? (
        <div style={{ maxWidth: 730 }}>
            {inputError && (
                <ErrorBox
                    messageTitle={"There is a problem"}
                    messageLinkBody={inputError}
                    errorInputLink={"#select-org-input"}
                    errorBoxSummaryId={"error-box-summary"}
                />
            )}
            <form onSubmit={handleSubmit(submit)}>
                <Fieldset>
                    <Fieldset.Legend headingLevel="h1" isPageHeading>
                        Select an organisation
                    </Fieldset.Legend>
                    <Radios
                        id="#select-org-input"
                        error={inputError}
                        hint="You are associated to more than one organisation, select an organisation you would like to view."
                    >
                        {session.organisations?.map((item, key) => (
                            <Radios.Radio
                                {...organisationProps}
                                key={key}
                                value={item.odsCode}
                                inputRef={organisationRef}
                            >
                                <p style={{ margin: 0, fontWeight: "bold" }}>{toSentenceCase(item.orgName)}</p>
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
    ) : (
        <Spinner status="Logging in..." />
    );
};

export default OrgSelectPage;
