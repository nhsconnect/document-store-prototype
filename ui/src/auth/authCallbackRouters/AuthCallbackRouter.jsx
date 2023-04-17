import { useBaseAPIUrl } from "../../providers/configProvider/ConfigProvider";
import { useEffect } from "react";
import routes from "../../enums/routes";
import ProgressBar from "../../components/progressBar/ProgressBar";
import { useSessionContext } from "../../providers/sessionProvider/SessionProvider";
import { useNavigate } from "react-router";

const AuthCallbackRouter = () => {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const [session, setSession] = useSessionContext();
    const navigate = useNavigate();

    useEffect(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const code = urlSearchParams.get("code");
        const state = urlSearchParams.get("state");
        const redirectUri = new URL(routes.AUTH_SUCCESS, window.location.href);
        const tokenRequestUrl = `${baseAPIUrl}/Auth/TokenRequest?code=${code}&state=${state}&redirect_uri=${redirectUri}`;
        fetch(tokenRequestUrl, {
            headers: {
                "Content-Type": "application/fhir+json",
                "Access-Control-Allow-Origin": `${baseAPIUrl}`,
                "Access-Control-Allow-Credentials": "true",
                "Access-Control-Allow-Methods": "GET"
            },
            mode: "cors",
            method: "GET"
        })
            .then((res) => {
                res.json().then((json) => {
                    setSession({
                        ...session,
                        subjectClaim: json.subjectClaim,
                        sessionId: json.sessionId,
                        isLoggedIn: true
                    });

                    navigate(routes.HOME);
                });
            })
            .catch(() => {
                console.error("computer said no");
                navigate(routes.HOME);
            });
        // window.location.replace(tokenRequestUrl);
    }, [baseAPIUrl, setSession, session, navigate]);

    return <ProgressBar status="Logging in..." />;
};

export default AuthCallbackRouter;
