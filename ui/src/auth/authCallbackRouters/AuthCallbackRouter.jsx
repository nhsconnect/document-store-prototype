import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import ProgressBar from "../../components/progressBar/ProgressBar";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const redirectUri = new URL(routes.AUTH_SUCCESS, window.location.href);
        const tokenRequestUrl = `${baseAPIUrl}/Auth/TokenRequest?code=${code}&state=${state}&redirect_uri=${redirectUri}`;

        window.location.replace(tokenRequestUrl);
    }, [baseAPIUrl]);

    return <ProgressBar status="Logging in..." />;
};

export default AuthCallbackRouter;
