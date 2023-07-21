import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import axios from "axios";
import { useNavigate } from "react-router";
import Spinner from "../../components/spinner/Spinner";

const AuthCallbackRouter = () => {
    const navigate = useNavigate();
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        console.log(code, state);
        axios
            .get(`${baseAPIUrl}/Auth/TokenRequest`, {
                params: { code, state },
                withCredentials: true,
            })
            .then((res) => {
                const { SessionId, Organisations } = res.data;
                setSession({
                    ...session,
                    sessionId: SessionId,
                    organisations: Organisations,
                    isLoggedIn: true,
                });
                if (session.organisations) {
                    navigate(routes.ORG_SELECT);
                } else {
                    navigate(routes.HOME);
                }
            })
            .catch((err) => {
                if (err.response.status === 401) {
                    navigate(routes.NO_VALID_ORGANISATION);
                } else if (err.response.status === 403) {
                    navigate(routes.AUTH_ERROR);
                }
            });
    }, [baseAPIUrl, navigate]);

    return <Spinner status="Logging in..." />;
};

export default AuthCallbackRouter;
