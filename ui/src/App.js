import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { Amplify, Auth, API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "./config";
import ApiClient from "./apiClients/apiClient";
import "./App.scss";
import Layout from "./components/layout";
import HomePage from "./pages/HomePage";
import SearchPage from "./pages/SearchPage";
import UploadPage from "./pages/UploadPage";
import FeatureToggleProvider, {useFeatureToggle} from "./providers/FeatureToggleProvider";
import CIS2Authenticator from "./components/CIS2Authenticator";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);

const Authenticator = ({ children }) => {
    const isCIS2FederatedIdentityProviderEnabled = useFeatureToggle("CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED");

    return (
        isCIS2FederatedIdentityProviderEnabled
            ? <CIS2Authenticator>{ children }</CIS2Authenticator>
            : <AmplifyAuthenticator data-testid={"AmplifyAuthenticator"}>{ children }</AmplifyAuthenticator>
    );
}

const App = () => {
    return (
        <FeatureToggleProvider>
            <Layout>
                    <Router>
                        <Authenticator>
                        <Routes>
                            <Route path="/" element={<HomePage/>}/>
                            <Route
                                path="/search"
                                element={<SearchPage client={client}/>}
                            />
                            <Route
                                path="/upload"
                                element={<UploadPage client={client}/>}
                            />
                        </Routes>
                        <div>
                            {/*<AmplifySignOut/>*/}
                        </div>
                        </Authenticator>
                    </Router>
            </Layout>
        </FeatureToggleProvider>
    );
}

export default App;
