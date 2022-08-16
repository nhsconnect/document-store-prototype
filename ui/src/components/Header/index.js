import React from "react";

import { Header } from "nhsuk-react-components";

export const HeaderContainer = () => (
  <Header transactional>
    <Header.Container>
      <Header.Logo href="/" />
      <Header.ServiceName data-testid="header-service-name" href="/">
        Document Store
      </Header.ServiceName>
    </Header.Container>
  </Header>
);

export default HeaderContainer;
