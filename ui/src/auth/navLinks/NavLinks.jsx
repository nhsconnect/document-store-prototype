import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import routes from "../../enums/routes";
import { Header } from "nhsuk-react-components";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

const NavLinks = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [session, setSession] = useSessionContext();
    const { isLoggedIn } = session;
    const redirectUri = new URL(routes.ROOT, window.location.href);
    const logoutUrl = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirectUri}`;

    const handleLogOut = () => {
        setSession({
            ...session,
            isLoggedIn: false,
        });
    };

    return isLoggedIn ? (
        <>
            <Header.NavItem href={routes.ROOT}>Home</Header.NavItem>
            <Header.NavItem href={logoutUrl} onClick={handleLogOut}>
                Log Out
            </Header.NavItem>
        </>
    ) : null;
};

export default NavLinks;
