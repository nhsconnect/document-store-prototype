import { Navigate } from "react-router";
import { useQuery } from "./CIS2Authenticator";

const CIS2AuthenticationResultNavigator = () => {
    const query = useQuery();

    if (query.has("error")) {
        return <Navigate to={`/?${query.toString()}`} replace />;
    }
    return <Navigate to={"/home"} replace />;
};

export default CIS2AuthenticationResultNavigator;
