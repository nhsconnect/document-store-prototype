import config from "../config";
import {useEffect, useMemo, useState} from "react";
import awsConfig from "../config";
import {Auth, Hub} from "aws-amplify";

const getToken = () => {
    return Auth.currentSession()
        .then(session => session)
        .catch(err => console.log(err));
}

const CIS2Authenticator = ({ children, autologin = true }) => {
    const [token, setToken] = useState();
    const [isAutologin] = useState(() => !token && autologin);

    const authHandler = ({payload: {event, data}}) => {
        switch (event) {
            case "signIn":
            case "cognitoHostedUI":
                setToken("granting...");
                getToken().then(userToken => setToken(userToken.idToken.jwtToken));
                break;
            case "signOut":
                setToken(null);
                break;
            case "signIn_failure":
            case "cognitoHostedUI_failure":
                console.log("Sign in failure", data);
                break;
            default:
                break;
        }
    };

    useEffect(() => {

        if(isAutologin){
            (async () => {
                try {
                    await Auth.federatedSignIn({
                        provider: awsConfig.Auth.providerId,
                    });
                } catch (e) {
                    console.log(e);
                }
            })();
        }

        Hub.listen("auth", authHandler);

        return () => {
            Hub.remove("auth", authHandler);
        }

    }, []);

    return <div data-testid={'CIS2Authenticator'}>{ token && children }</div>;
};

export default CIS2Authenticator;