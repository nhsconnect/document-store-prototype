import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import { useEffect } from "react";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const queryParams = window.location.search;
        window.location.assign(`${baseAPIUrl}/Auth/TokenRequest${queryParams}`);
    }, [baseAPIUrl]);

    return "Loading...";
};

export default AuthCallbackRouter;
