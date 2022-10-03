import React from "react";
import {ButtonLink} from "nhsuk-react-components";
import {useNavigate} from "react-router";

import {Header} from "nhsuk-react-components";


export const HeaderContainer = () => {
  const navigate = useNavigate();
  const handleLogOut = () => {
    navigate("/");
  };
  return (
    <Header transactional>
      <Header.Container>
        <Header.Logo href="/"/>
        <Header.ServiceName data-testid="header-service-name" href="/">
          Document Store
        </Header.ServiceName>
        <Header.Content>
          <ButtonLink onClick={handleLogOut}>Log Out</ButtonLink>
        </Header.Content>
      </Header.Container>
    </Header>
  );
}

export default HeaderContainer;
