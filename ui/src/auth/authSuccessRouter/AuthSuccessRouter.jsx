import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { useEffect } from "react";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import UserRoles from "../../enums/userRoles";

const AuthSuccessRouter = () => {
    const navigate = useNavigate();
    const [session, setSession] = useSessionContext();

    useEffect(() => {
        setSession({
            userRole: UserRoles.user,
            isLoggedIn: true,
        });

        navigate(routes.HOME);
    }, [navigate, session, setSession]);

    return null;
};

export default AuthSuccessRouter;
