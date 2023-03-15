import { useBaseAPIUrl } from "../../../providers/ConfigurationProvider";
import routes from "../../../enums/routes";
import { useCookies } from "react-cookie";
import { Header } from "nhsuk-react-components";

const NavLinks = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [cookies] = useCookies(["LoggedIn"]);

    const redirectUri = new URL(routes.ROOT, window.location.href);
    const logoutUrl = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`;

    return cookies.LoggedIn ? (
        <>
            <Header.NavItem href={routes.HOME}>Home</Header.NavItem>
            <Header.NavItem href={logoutUrl}>Log Out</Header.NavItem>
        </>
    ) : null;
};

export default NavLinks;
