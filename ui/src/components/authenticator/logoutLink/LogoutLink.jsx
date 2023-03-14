import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";

const LogoutLink = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const redirectUri = new URL(routes.ROOT, window.location.href);
    const logoutUrl = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`;

    return (
        <a href={logoutUrl} style={{ color: "white" }}>
            Log Out
        </a>
    );
};

export default LogoutLink;
