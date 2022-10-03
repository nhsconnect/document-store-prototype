import React from "react";
import { ButtonLink} from "nhsuk-react-components";

import { Header } from "nhsuk-react-components";

export const HeaderContainer = () => (
  <Header transactional>
    <Header.Container>
      <Header.Logo href="/" />
      <Header.ServiceName data-testid="header-service-name" href="/">
        Document Store
      </Header.ServiceName>
      <Header.Content>
        <ButtonLink>Logout</ButtonLink>
      </Header.Content>
    </Header.Container>
  </Header>
);

export default HeaderContainer;
