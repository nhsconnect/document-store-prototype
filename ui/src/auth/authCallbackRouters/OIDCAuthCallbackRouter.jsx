import { useAuth } from "react-oidc-context";
import { Navigate } from "react-router";

const OIDCAuthCallbackRouter = () => {
    const { isLoading, isAuthenticated, error } = useAuth();

    if (isLoading) {
        return "Loading...";
    }

    if (error) {
        return <Navigate to="/" replace />;
    }

    if (isAuthenticated) {
        return <Navigate to="/home" replace />;
    }

    throw new Error("This should never happen");
};

export default OIDCAuthCallbackRouter;
