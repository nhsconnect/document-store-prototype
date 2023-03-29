import routes from "../../enums/routes";
import { useNavigate } from "react-router";
import { useEffect } from "react";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

const ProtectedRoutes = ({ children }) => {
    const navigate = useNavigate();

    const [session] = useSessionContext();
    const { isLoggedIn } = session;

    useEffect(() => {
        if (!isLoggedIn) {
            navigate(routes.ROOT);
        }
    }, [isLoggedIn, navigate]);

    return <>{isLoggedIn && children}</>;
};

export default ProtectedRoutes;
