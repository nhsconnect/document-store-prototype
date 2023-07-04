import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import Spinner from "../../components/spinner/Spinner";
import axios from "axios";
import { useNavigate } from "react-router";

const AuthCallbackRouter = () => {
    const navigate = useNavigate();
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const error_uri = new URL(routes.AUTH_ERROR, window.location.href);
        const redirect_uri = new URL(routes.AUTH_SUCCESS, window.location.href);
        axios
            .get(`${baseAPIUrl}/Auth/TokenRequest`, {
                params: { code, state, redirect_uri, error_uri },
            })
            .then((res) => {
                console.log(res);
                navigate(routes.AUTH_SUCCESS);
            })
            .catch((err) => {
                console.log(err);
                navigate(routes.AUTH_ERROR);
            });
    }, [baseAPIUrl, navigate]);

    return <Spinner status="Logging in..." />;
};

export default AuthCallbackRouter;
