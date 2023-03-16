import { useCookies } from "react-cookie";
import routes from "../../enums/routes";
import { useNavigate } from "react-router";
import { useEffect } from "react";

const ProtectedRoutes = ({ children }) => {
    const [cookies] = useCookies(["LoggedIn"]);
    const navigate = useNavigate();

    useEffect(() => {
        if (!cookies.LoggedIn) {
            navigate(routes.ROOT);
        }
    }, [cookies, navigate]);

    return <>{!!cookies.LoggedIn && children}</>;
};

export default ProtectedRoutes;
