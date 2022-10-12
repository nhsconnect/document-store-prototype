import "nhsuk-frontend/dist/nhsuk.css";
import {Amplify, API, Auth} from "aws-amplify";
import awsConfig from "../config";
import Authenticator from "../components/Authenticator/Authenticator";
import React from "react";
import FeatureToggleProvider from "../providers/FeatureToggleProvider";
import Layout from "../components/layout";
import {NhsNumberProvider} from "../providers/NhsNumberProvider";

function MyApp({ Component, pageProps }) {
    Amplify.configure(awsConfig);
    console.log("rendering app")

    return (
        <FeatureToggleProvider>
            {/*<Authenticator>*/}
                <Layout>
                    <Authenticator.Errors />
                    <NhsNumberProvider>
                        <Component {...pageProps} />
                    </NhsNumberProvider>
                </Layout>
            {/*</Authenticator>*/}
        </FeatureToggleProvider>);
}

export default MyApp