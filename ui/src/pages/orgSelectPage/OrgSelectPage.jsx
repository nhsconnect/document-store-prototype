import React, { useEffect } from "react";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";

import axios from "axios";
import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";

const OrgSelectPage = () => {
    const [session] = useSessionContext();
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

        axios
            .get(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                withCredentials: true,
                params: { organisation },
            })
            .then((res) => {
                console.log(JSON.stringify(res.data, null, 4));
                navigate(routes.HOME);
            })
            .catch(() => {
                navigate(routes.AUTH_ERROR);
            });
    };

    return (
        <>
            <form onSubmit={handleSubmit(submit)}>
                <Fieldset>
                    <Fieldset.Legend headingLevel="h1" isPageHeading>
                        Select an organisation
                    </Fieldset.Legend>
                    <Radios hint="You are associated to more than one organisation, select an organisation you would like to view">
                        {session.organisations.map((item, key) => (
                            <Radios.Radio
                                {...organisationProps}
                                key={key}
                                value={item.orgName}
                                inputRef={organisationRef}
                            >
                                {item.orgType}: {item.odsCode}
                            </Radios.Radio>
                        ))}
                    </Radios>
                </Fieldset>
                <Button type="submit" disabled={!isOrganisationDirty}>
                    Continue
                </Button>
            </form>
        </>
    );
};

export default OrgSelectPage;
