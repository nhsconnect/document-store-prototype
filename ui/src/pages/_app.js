import "nhsuk-frontend/dist/nhsuk.css";
import Authenticator from "../components/Authenticator/Authenticator";
import React from "react";
import FeatureToggleProvider from "../providers/FeatureToggleProvider";
import Layout from "../components/layout";

function MyApp({ Component, pageProps }) {
    return (
        <FeatureToggleProvider>
            {/*<Authenticator>*/}
                <Layout>
                    <Authenticator.Errors />
                    <Component {...pageProps} />
                </Layout>
            {/*</Authenticator>*/}
        </FeatureToggleProvider>);
}

export default MyApp