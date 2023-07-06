import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import { useNavigate } from "react-router";
import Spinner from "../../components/spinner/Spinner";
import axiosService from "../../services/axiosService";

const AuthCallbackRouter = () => {
    const navigate = useNavigate();
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const error_uri = new URL(routes.AUTH_ERROR, window.location.href);
        const redirect_uri = new URL(routes.AUTH_SUCCESS, window.location.href);

        const fetchTokenRequest = async () => {
            try {
                const response = await axiosService.get(`/Auth/TokenRequest`, {
                    params: { code, state, redirect_uri, error_uri },
                });
                console.log(response);
                navigate(routes.AUTH_SUCCESS);
            } catch (err) {
                console.log(err);
                navigate(routes.AUTH_ERROR);
            }
        };
        fetchTokenRequest();
    }, [baseAPIUrl, navigate]);

    return <Spinner status="Logging in..." />;
};

export default AuthCallbackRouter;
