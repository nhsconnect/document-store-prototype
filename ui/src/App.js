import { Amplify, API } from "aws-amplify";
import React from "react";
import {
    BrowserRouter as Router,
    Outlet,
    Route,
    Routes,
} from "react-router-dom";
import "./App.scss";
import config from "./config";
import ApiClient from "./apiClients/apiClient";
import Authenticator from "./components/Authenticator/Authenticator";
import HomePage from "./pages/HomePage";
import Layout from "./components/layout";
import FeatureToggleProvider from "./providers/FeatureToggleProvider";
import { NhsNumberProvider } from "./providers/NhsNumberProvider";
import { PatientTracePage } from "./pages/PatientTracePage";
import UploadDocumentPage from "./pages/UploadDocumentPage";
import SearchResultsPage from "./pages/SearchResultsPage";
import StartPage from "./pages/StartPage";
import AuthenticationCallbackRouter from "./components/Authenticator/AuthenticationCallbackRouter";
import AuthProvider from "./components/Authenticator/AuthProvider";
import DeleteDocumentsConfirmationPage from "./pages/DeleteDocumentsConfirmationPage";

Amplify.configure({ API: config.API });

const client = new ApiClient(API);
const AppRoutes = () => {
    return (
        <Routes>
            <Route element={<StartPage />} path={"/"} />
            <Route
                element={<AuthenticationCallbackRouter />}
                path={"cis2-auth-callback"}
            />
            <Route
                element={
                    <Authenticator.Protected>
                        <Outlet />
                    </Authenticator.Protected>
                }
            >
                <Route path={"/home"} element={<HomePage />} />
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

                    <Route
                        path="/search/results/delete-documents-confirmation"
                        element={<DeleteDocumentsConfirmationPage />}
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
                        element={
                            <UploadDocumentPage
                                client={client}
                                nextPagePath={"/home"}
                            />
                        }
                    />
                </Route>
            </Route>
        </Routes>
    );
};

const App = () => {
    return (
        <FeatureToggleProvider config={config}>
            <Router>
                <AuthProvider>
                    <Layout>
                        <Authenticator.Errors />
                        <AppRoutes />
                    </Layout>
                </AuthProvider>
            </Router>
        </FeatureToggleProvider>
    );
};

export default App;
