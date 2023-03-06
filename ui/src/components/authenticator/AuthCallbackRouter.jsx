import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import { useEffect } from "react";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const redirectUri = new URL("/home", window.location.href);

        window.location.replace(
            `${baseAPIUrl}/Auth/TokenRequest?code=${code}&state=${state}&redirect_uri=${redirectUri}`
        );
    }, [baseAPIUrl]);

    return "Loading...";
};

export default AuthCallbackRouter;
