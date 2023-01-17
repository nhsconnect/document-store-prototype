import React from "react";
import { Header } from "nhsuk-react-components";
import Authenticator from "../Authenticator/Authenticator";

export const HeaderContainer = () => {
    return (
        <Header transactional>
            <Header.Container>
                <Header.Logo href="/" />
                <Header.ServiceName href="/">Inactive Patient Record Administration</Header.ServiceName>
                <Header.Content>
                    <Authenticator.LogOut />
                </Header.Content>
            </Header.Container>
        </Header>
    );
};

export default HeaderContainer;
