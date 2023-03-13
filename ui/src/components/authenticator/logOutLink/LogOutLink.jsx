import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import { Header } from "nhsuk-react-components";
import routes from "../../../enums/routes";

const LogOutLink = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const redirectUri = new URL(routes.ROOT, window.location.href);

    return (
        <Header.ServiceName href={`${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`}>Log Out</Header.ServiceName>
    );
};

export default LogOutLink;
