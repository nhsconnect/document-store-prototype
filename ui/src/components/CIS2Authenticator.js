import awsConfig from "../config";
import {useEffect, useMemo, useState} from "react";
import {Auth, Hub} from "aws-amplify";
import {ErrorSummary} from "nhsuk-react-components";
import {deleteFromStorage, writeStorage} from '@rehooks/local-storage';
import {useLocation} from "react-router";

const getToken = async () => {
    const session = await Auth.currentSession();
    return session.idToken.jwtToken;
}

const checkIfAuthAttempted = () => parseInt(localStorage.getItem('attempts') ?? 0) > 0;
const setAuthAsAttempted = () => writeStorage('attempts', 1);
const unsetAuthAsAttempted = () => writeStorage('attempts', 0);

function useQuery() {
    const { search } = useLocation();
    return useMemo(() => new URLSearchParams(search), [search]);
}

const CIS2Authenticator = ({ children, autologin = true }) => {
    const [token, setToken] = useState();
    const [error, setError] = useState();
    const [isAutologin] = useState(() => !token && autologin);
    const query = useQuery();

    const authHandler = async ({payload: {event, data}}) => {
        console.log(event, data);
        switch (event) {
            case "signIn":
            case "cognitoHostedUI":
                const userToken = await getToken();
                setToken(userToken);
                break;
            case "signOut":
                setToken(null);
                break;
            case "signIn_failure":
            case "cognitoHostedUI_failure":
            default:
                if(data === undefined){
                    data = { error_description: "There was a problem" };
                }
                setError(data);
                break;
        }
        deleteFromStorage('attempts');
    };

    useEffect(() => {

        if (query.get("error_description"))
        {
            setError({ error_description: query.get("error_description") });
            unsetAuthAsAttempted();
            return;
        }

        if(isAutologin && !checkIfAuthAttempted()){
            (async () => {
                try {
                    await Auth.federatedSignIn({
                        provider: awsConfig.Auth.providerId,
                    });
                } catch(e){
                    setError(e);
                }
                setAuthAsAttempted();
            })();
        }

        return Hub.listen("auth", authHandler);

    }, []);

    return <div data-testid={'CIS2Authenticator'}>
        { error && <ErrorSummary>
            <ErrorSummary.Title id="error-summary-title">There is a problem</ErrorSummary.Title>
            <ErrorSummary.Body>
                <p>{ error.error_description }</p>
            </ErrorSummary.Body>
        </ErrorSummary> }
        { token && children }
    </div>;
};

export default CIS2Authenticator;