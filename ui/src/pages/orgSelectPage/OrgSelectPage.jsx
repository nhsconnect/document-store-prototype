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
    // const { register, handleSubmit, formState, getFieldState } = useForm();
    const { handleSubmit } = useForm();
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        if (!session.organisations) {
            navigate(routes.HOME);
        }
    });

    const submit = (organisation) => {
        console.log(organisation);
        const org = {
            organisation: organisation,
        };
        axios
            .get(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                params: { org },
                withCredentials: true,
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
                        How do you want to use the service?
                    </Fieldset.Legend>
                    <Radios hint="Select an option">
                        {session.organisations.map((item, key) => (
                            <Radios.Radio key={key} value={item.orgName}>
                                {item.orgType}: {item.odsCode}
                            </Radios.Radio>
                        ))}
                    </Radios>
                </Fieldset>
                <Button type="submit">Continue</Button>
            </form>
        </>
    );
};

export default OrgSelectPage;
