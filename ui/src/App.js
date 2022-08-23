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
import SearchPage from "./pages/SearchPage";
import UploadPage from "./pages/UploadPage";
import Layout from "./components/layout";
import FeatureToggleProvider from "./providers/FeatureToggleProvider";
import { NhsNumberProvider } from "./providers/NhsNumberProvider";
import { PatientTracePage } from "./pages/PatientTracePage";
import UploadDocumentPage from "./pages/UploadDocumentPage";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);

const App = () => {
    return (
        <FeatureToggleProvider>
            <Router>
                <Authenticator>
                    <Layout>
                        <Authenticator.Errors />
                        <Authenticator.Protected>
                            <Routes>
                                <Route path="/" element={<HomePage />} />

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
                            </Routes>
                            <div>{/*<AmplifySignOut/>*/}</div>
                        </Authenticator.Protected>
                    </Layout>
                </Authenticator>
            </Router>
        </FeatureToggleProvider>
    );
};

export default App;
