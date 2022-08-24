import { Amplify, Auth, API } from "aws-amplify";
import React from "react";
import {
    BrowserRouter as Router, Navigate,
    Outlet,
    Route,
    Routes,
} from "react-router-dom";
import "./App.scss";
import awsConfig from "./config";
import ApiClient from "./apiClients/apiClient";
import Authenticator from "./components/Authenticator/Authenticator";
import HomePage from "./pages/HomePage";
import SearchPage from "./pages/SearchPage";
import UploadPage from "./pages/UploadPage";
import Layout from "./components/layout";
import FeatureToggleProvider, {useFeatureToggle} from "./providers/FeatureToggleProvider";
import { NhsNumberProvider } from "./providers/NhsNumberProvider";
import { PatientTracePage } from "./pages/PatientTracePage";
import UploadDocumentPage from "./pages/UploadDocumentPage";
import SearchSubmitPage from "./pages/SearchSubmitPage";
import StartPage from "./pages/StartPage";
import {useQuery} from "./components/Authenticator/CIS2Authenticator";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);
const AppRoutes = () => {
    const query = useQuery();
    const isCIS2Enabled = useFeatureToggle("CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED");
    return <Routes>
        {isCIS2Enabled && <>
            <Route element={<StartPage />} path={"/"} />
            <Route element={<Navigate to={"/home"} search={query.toString()} replace/>} path={"cis2-auth-callback"}/>
        </>}
        <Route
            element={
                <Authenticator.Protected>
                    <Outlet />
                </Authenticator.Protected>
            }
        >
            <Route path={isCIS2Enabled? "/home" : "/"} element={<HomePage />} />
            <Route
                path="/search"
                element={
                    <NhsNumberProvider>
                        <Outlet />
                    </NhsNumberProvider>
                }
            >
                <Route
                    path="/search"
                    element={<SearchPage client={client} />}
                />
                <Route
                    path="/search/patient-trace"
                    element={
                        <PatientTracePage
                            client={client}
                            nextPage={"/search/submit"}
                            title={
                                "View Stored Patient Record"
                            }
                        />
                    }
                />
                <Route
                    path="/search/submit"
                    element={
                        <SearchSubmitPage client={client} />
                    }
                />
            </Route>

            <Route
                path="/upload"
                element={
                    <NhsNumberProvider>
                        <Outlet />
                    </NhsNumberProvider>
                }
            >
                <Route
                    path="/upload"
                    exact={true}
                    element={<UploadPage client={client} />}
                />
                <Route
                    path="/upload/patient-trace"
                    element={
                        <PatientTracePage
                            client={client}
                            nextPage={"/upload/submit"}
                            title={"Upload Patient Record"}
                        />
                    }
                />
                <Route
                    path="/upload/submit"
                    element={
                        <UploadDocumentPage
                            client={client}
                        />
                    }
                />
            </Route>
        </Route>
    </Routes>
}

const App = () => {
    return (
        <FeatureToggleProvider>
            <Router>
                <Authenticator>
                    <Layout>
                        <Authenticator.Errors />
                        <AppRoutes />
                    </Layout>
                </Authenticator>
            </Router>
        </FeatureToggleProvider>
    );
};

export default App;
