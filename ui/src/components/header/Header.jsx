import React from "react";
import { Header as HeaderComponent } from "nhsuk-react-components";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";
import { CookiesProvider } from "react-cookie";
import LogoutLink from "../authenticator/logoutLink/LogoutLink";
import Authenticator from "../authenticator/Authenticator";

const Header = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return (
        <HeaderComponent transactional>
            <HeaderComponent.Container>
                <HeaderComponent.Logo href="/" />
                <HeaderComponent.ServiceName href="/">
                    Inactive Patient Record Administration
                </HeaderComponent.ServiceName>
            </HeaderComponent.Container>
            <HeaderComponent.Nav>
                {isOIDCAuthActive ? (
                    <Authenticator.LogOut />
                ) : (
                    <CookiesProvider>
                        <LogoutLink />
                    </CookiesProvider>
                )}
            </HeaderComponent.Nav>
        </HeaderComponent>
    );
};

export default Header;
