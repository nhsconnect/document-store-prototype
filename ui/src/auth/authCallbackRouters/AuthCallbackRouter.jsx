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

        axios
            .get(`${baseAPIUrl}/Auth/TokenRequest`, {
                params: { code, state },
                withCredentials: true,
            })
            .then((res) => {
                const { Organisations } = res.data;
                setSession({
                    ...session,
                    organisations: Organisations,
                    isLoggedIn: true,
                });
                if (Organisations) {
                    if (Organisations.length > 1) {
                        navigate(routes.ORG_SELECT);
                    } else {
                        axios
                            .get(`${baseAPIUrl}/Auth/VerifyOrganisation`, {
                                withCredentials: true,
                                params: { odsCode: Organisations.odsCode },
                            })
                            .then((res) => {
                                console.log(JSON.stringify(res.data, null, 4));
                                navigate(routes.HOME);
                            })
                            .catch(() => {
                                navigate(routes.AUTH_ERROR);
                            });
                    }
                } else {
                    navigate(routes.NO_VALID_ORGANISATION);
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
