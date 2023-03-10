import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";

const LogOutLink = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const redirectUri = new URL(routes.ROOT, window.location.href);

    return <a href={`${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`}>Log Out</a>;
};

export default LogOutLink;
