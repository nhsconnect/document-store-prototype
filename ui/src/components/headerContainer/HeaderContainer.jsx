import React from "react";
import { Header } from "nhsuk-react-components";
import Authenticator from "../authenticator/Authenticator";
import LogOutLink from "../authenticator/logOutLink/LogOutLink";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";

export const HeaderContainer = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return (
        <Header transactional>
            <Header.Container>
                <Header.Logo href="/" />
                <Header.ServiceName href="/">Inactive Patient Record Administration</Header.ServiceName>
                <Header.Content>{isOIDCAuthActive ? <Authenticator.LogOut /> : <LogOutLink />}</Header.Content>
            </Header.Container>
        </Header>
    );
};

export default HeaderContainer;
