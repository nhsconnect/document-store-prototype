import React from "react";
import { Header as HeaderComponent } from "nhsuk-react-components";
import Authenticator from "../authenticator/Authenticator";
import LogoutLink from "../authenticator/logoutLink/LogoutLink";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";

const Header = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return (
        <HeaderComponent transactional>
            <HeaderComponent.Container>
                <HeaderComponent.Logo href="/" />
                <HeaderComponent.ServiceName href="/">
                    Inactive Patient Record Administration
                </HeaderComponent.ServiceName>
                <HeaderComponent.Content>
                    {isOIDCAuthActive ? <Authenticator.LogOut /> : <LogoutLink />}
                </HeaderComponent.Content>
            </HeaderComponent.Container>
        </HeaderComponent>
    );
};

export default Header;
