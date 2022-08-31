import { Amplify, Auth, API } from "aws-amplify";
import React from "react";
import {
    BrowserRouter as Router,
    Outlet,
    Route,
    Routes,
} from "react-router-dom";
import "./App.scss";
import awsConfig from "./config";
import ApiClient from "./apiClients/apiClient";
import Authenticator from "./components/Authenticator/Authenticator";
import HomePage from "./pages/HomePage";
import Layout from "./components/layout";
import FeatureToggleProvider, {
    useFeatureToggle,
} from "./providers/FeatureToggleProvider";
import { NhsNumberProvider } from "./providers/NhsNumberProvider";
import { PatientTracePage } from "./pages/PatientTracePage";
import UploadDocumentPage from "./pages/UploadDocumentPage";
import SearchResultsPage from "./pages/SearchResultsPage";
import StartPage from "./pages/StartPage";
import CIS2AuthenticationResultNavigator from "./components/Authenticator/CIS2AuthenticationResultNavigator";
import UploadSuccessPage from "./pages/UploadSuccessPage";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);
const AppRoutes = () => {
    const isCIS2Enabled = useFeatureToggle(
        "CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED"
    );
    return (
        <Routes>
            {isCIS2Enabled && (
                <>
                    <Route element={<StartPage />} path={"/"} />
                    <Route
                        element={<CIS2AuthenticationResultNavigator />}
                        path={"cis2-auth-callback"}
                    />
                </>
            )}
            <Route
                element={
                    <Authenticator.Protected>
                        <Outlet />
                    </Authenticator.Protected>
                }
            >
                <Route
                    path={isCIS2Enabled ? "/home" : "/"}
                    element={<HomePage />}
                />
                <Route
                    path="/search"
                    element={
                        <NhsNumberProvider>
                            <Outlet />
                        </NhsNumberProvider>
                    }
                >
                    <Route
                        path="/search/patient-trace"
                        element={
                            <PatientTracePage
                                client={client}
                                nextPage={"/search/results"}
                                title={"Download and view a stored document"}
                            />
                        }
                    />
                    <Route
                        path="/search/results"
                        element={<SearchResultsPage client={client} />}
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
                        path="/upload/patient-trace"
                        element={
                            <PatientTracePage
                                client={client}
                                nextPage={"/upload/submit"}
                                title={"Upload a document"}
                            />
                        }
                    />
                    <Route
                        path="/upload/submit"
                        element={<UploadDocumentPage client={client} />}
                    />
                    <Route
                        path="/upload/success"
                        element={<UploadSuccessPage />}
                    />
                </Route>
            </Route>
        </Routes>
    );
};

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
