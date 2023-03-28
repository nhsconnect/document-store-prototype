import routes from "../../enums/routes";
import { useNavigate } from "react-router";
import { useEffect } from "react";

const ProtectedRoutes = ({ children }) => {
    const navigate = useNavigate();

    const isLoggedIn = localStorage.getItem("LoggedIn") === "true";

    useEffect(() => {
        if (!isLoggedIn) {
            navigate(routes.ROOT);
        }
    }, [isLoggedIn, navigate]);

    return <>{isLoggedIn && children}</>;
};

export default ProtectedRoutes;
