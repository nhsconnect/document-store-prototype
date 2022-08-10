import awsConfig from "../config";
import {useEffect, useState} from "react";
import {Auth, Hub} from "aws-amplify";

const getToken = async () => {
    const session = await Auth.currentSession();
    return session.idToken.jwtToken;
}

const CIS2Authenticator = ({ children, autologin = true }) => {
    const [token, setToken] = useState();
    const [isAutologin] = useState(() => !token && autologin);

    const authHandler = async ({payload: {event, data}}) => {
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
                throw "cognito login failed";
        }
    };

    useEffect(() => {

        if(isAutologin){
            (async () => {
                await Auth.federatedSignIn({
                    provider: awsConfig.Auth.providerId,
                });
            })();
        }

        Hub.listen("auth", authHandler);

        return () => {
            Hub.remove("auth", authHandler);
        }

    }, []);

    return <div data-testid={'CIS2Authenticator'}>
        { token && children }
    </div>;
};

export default CIS2Authenticator;