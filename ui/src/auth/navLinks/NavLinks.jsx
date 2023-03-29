import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";
import { Header } from "nhsuk-react-components";

const NavLinks = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    const isLoggedIn = sessionStorage.getItem("LoggedIn") === "true";
    const redirectUri = new URL(routes.ROOT, window.location.href);
    const logoutUrl = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`;

    const handleLogOut = () => {
        sessionStorage.setItem("LoggedIn", "false");
    };

    return isLoggedIn ? (
        <>
            <Header.NavItem href={routes.HOME}>Home</Header.NavItem>
            <Header.NavItem href={logoutUrl} onClick={handleLogOut}>
                Log Out
            </Header.NavItem>
        </>
    ) : null;
};

export default NavLinks;
