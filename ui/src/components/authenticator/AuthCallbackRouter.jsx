import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import { useEffect } from "react";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");

        window.location.assign(`${baseAPIUrl}/Auth/TokenRequest?code=${code}&state=${state}`);
    }, [baseAPIUrl]);

    return "Loading...";
};

export default AuthCallbackRouter;
