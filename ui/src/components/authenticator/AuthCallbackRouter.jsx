import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import { useEffect } from "react";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const params = window.location.search;
        window.location.assign(`${baseAPIUrl}/Auth/TokenRequest${params}`);
    }, [baseAPIUrl]);

    return "Loading...";
};

export default AuthCallbackRouter;
