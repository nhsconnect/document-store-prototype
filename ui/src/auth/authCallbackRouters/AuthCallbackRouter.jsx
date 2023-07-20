import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import axios from "axios";
import { useNavigate } from "react-router";
import Spinner from "../../components/spinner/Spinner";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";

const AuthCallbackRouter = () => {
    const navigate = useNavigate();
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [session, setSession] = useSessionContext();

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        axios
            .get(`${baseAPIUrl}/Auth/TokenRequest`, {
                params: { code, state },
                withCredentials: true,
            })
            .then((res) => {
                const { sessionId, organisations } = res.data;
                setSession({
                    ...session,
                    sessionId,
                    organisations,
                    isLoggedIn: true,
                });
                if (session.orgs) {
                    navigate(routes.ORG_SELECT);
                } else {
                    navigate(routes.HOME);
                }
            })
            .catch(() => {
                navigate(routes.AUTH_ERROR);
            });
    }, [baseAPIUrl, navigate, session, setSession]);

    return <Spinner status="Logging in..." />;
};

export default AuthCallbackRouter;
