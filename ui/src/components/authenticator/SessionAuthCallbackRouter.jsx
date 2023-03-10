import { useBaseAPIUrl } from "../../providers/ConfigurationProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const redirectUri = new URL(routes.HOME, window.location.href);

        window.location.replace(
            `${baseAPIUrl}/Auth/TokenRequest?code=${code}&state=${state}&redirect_uri=${redirectUri}`
        );
    }, [baseAPIUrl]);

    // TODO: [PRMT-2779] Improve the UX of the loading status
    return "Loading...";
};

export default AuthCallbackRouter;
