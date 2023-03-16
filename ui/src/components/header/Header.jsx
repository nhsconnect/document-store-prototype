import React from "react";
import { Header as HeaderComponent } from "nhsuk-react-components";
import { useFeatureToggle } from "../../providers/ConfigurationProvider";
import { CookiesProvider } from "react-cookie";
import NavLinks from "../../auth/navLinks/NavLinks";
import OidcAuthenticator from "../../auth/oidcAuthenticator/OidcAuthenticator";
import routes from "../../enums/routes";

const Header = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return (
        <HeaderComponent transactional>
            <HeaderComponent.Container>
                <HeaderComponent.Logo href={routes.ROOT} />
                <HeaderComponent.ServiceName href={routes.ROOT}>
                    Inactive Patient Record Administration
                </HeaderComponent.ServiceName>
            </HeaderComponent.Container>
            <HeaderComponent.Nav>
                {isOIDCAuthActive ? (
                    <OidcAuthenticator.NavLinks />
                ) : (
                    <CookiesProvider>
                        <NavLinks />
                    </CookiesProvider>
                )}
            </HeaderComponent.Nav>
        </HeaderComponent>
    );
};

export default Header;
