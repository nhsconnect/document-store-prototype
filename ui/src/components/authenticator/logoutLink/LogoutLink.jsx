import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";
import { useCookies } from "react-cookie";

const LogoutLink = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [cookies] = useCookies(["LoggedIn"]);

    const redirectUri = new URL(routes.ROOT, window.location.href);
    const logoutUrl = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`;

    return cookies.LoggedIn ? (
        <a href={logoutUrl} style={{ color: "white" }}>
            Log Out
        </a>
    ) : null;
};

export default LogoutLink;
