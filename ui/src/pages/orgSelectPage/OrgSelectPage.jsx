import React, { useEffect } from "react";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import { useForm } from "react-hook-form";

const OrgSelectPage = () => {
    const [session] = useSessionContext();
    const navigate = useNavigate();
    // const { register, handleSubmit, formState, getFieldState } = useForm();
    const { handleSubmit } = useForm();

    useEffect(() => {
        if (!session.organisations) {
            navigate(routes.HOME);
        }
    });

    const submit = (organisation) => {
        console.log(organisation);
    };
    return (
        <>
            <form onSubmit={handleSubmit(submit)}>
                <Fieldset>
                    <Fieldset.Legend headingLevel="h1" isPageHeading>
                        How do you want to use the service?
                    </Fieldset.Legend>
                    <Radios hint="Select an option"></Radios>
                </Fieldset>
                <Button type="submit">Continue</Button>
            </form>
        </>
    );
};

export default OrgSelectPage;
