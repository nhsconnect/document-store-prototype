import React from "react";
import { Header as HeaderComponent } from "nhsuk-react-components";
import Authenticator from "../authenticator/Authenticator";
import LogoutLink from "../authenticator/logoutLink/LogoutLink";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";
import { CookiesProvider } from "react-cookie";

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
                    {isOIDCAuthActive ? (
                        <Authenticator.LogOut />
                    ) : (
                        <CookiesProvider>
                            <LogoutLink />
                        </CookiesProvider>
                    )}
                </HeaderComponent.Content>
            </HeaderComponent.Container>
        </HeaderComponent>
    );
};

export default Header;
