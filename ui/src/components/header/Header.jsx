import React from "react";
import { Header as HeaderComponent } from "nhsuk-react-components";
import OidcAuthenticator from "../../auth/oidcAuthenticator/OidcAuthenticator";
import routes from "../../enums/routes";

const Header = () => {
    return (
        <HeaderComponent transactional>
            <HeaderComponent.Container>
                <HeaderComponent.Logo href={routes.ROOT} />
                <HeaderComponent.ServiceName href={routes.ROOT}>
                    Inactive Patient Record Administration
                </HeaderComponent.ServiceName>
            </HeaderComponent.Container>
            <HeaderComponent.Nav>
                <OidcAuthenticator.NavLinks />
            </HeaderComponent.Nav>
        </HeaderComponent>
    );
};

export default Header;
