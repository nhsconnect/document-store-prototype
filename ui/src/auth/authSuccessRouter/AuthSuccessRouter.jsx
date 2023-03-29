import { useNavigate } from "react-router";
import routes from "../../enums/routes";
import { useEffect } from "react";

const AuthSuccessRouter = () => {
    const navigate = useNavigate();

    useEffect(() => {
        sessionStorage.setItem("LoggedIn", "true");
        navigate(routes.HOME);
    }, [navigate]);

    return null;
};

export default AuthSuccessRouter;
