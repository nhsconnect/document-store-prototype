import React from "react";
import { BrowserRouter as Router, Outlet, Route, Routes } from "react-router-dom";
import "./App.scss";
import config from "./config";
import Authenticator from "./components/authenticator/Authenticator";
import HomePage from "./pages/homePage/HomePage";
import Layout from "./components/layout/Layout";
import { PatientDetailsProvider } from "./providers/PatientDetailsProvider";
import { PatientTracePage } from "./pages/patientTracePage/PatientTracePage";
import UploadDocumentsPage from "./pages/uploadDocumentsPage/UploadDocumentsPage";
import SearchResultsPage from "./pages/searchResultsPage/SearchResultsPage";
import StartPage from "./pages/startPage/StartPage";
import OIDCAuthCallbackRouter from "./components/authenticator/OIDCAuthCallbackRouter";
import AuthProvider from "./components/authenticator/AuthProvider";
import DeleteDocumentsPage from "./pages/deleteDocumentsPage/DeleteDocumentsPage";
import ConfigurationProvider, { useFeatureToggle } from "./providers/ConfigurationProvider";
import SessionAuthCallbackRouter from "./components/authenticator/SessionAuthCallbackRouter";
import DocumentStoreProvider from "./providers/DocumentStoreProvider";

const AppRoutes = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return (
        <Routes>
            <Route element={<StartPage />} path="/" />
            <Route
                element={isOIDCAuthActive ? <OIDCAuthCallbackRouter /> : <SessionAuthCallbackRouter />}
                path="cis2-auth-callback"
            />
            <Route
                element={
                    isOIDCAuthActive ? (
                        <Authenticator.Protected>
                            <DocumentStoreProvider>
                                <Outlet />
                            </DocumentStoreProvider>
                        </Authenticator.Protected>
                    ) : (
                        <DocumentStoreProvider>
                            <Outlet />
                        </DocumentStoreProvider>
                    )
                }
            >
                <Route path="/home" element={<HomePage />} />
                <Route
                    path="/search"
                    element={
                        <PatientDetailsProvider>
                            <Outlet />
                        </PatientDetailsProvider>
                    }
                >
                    <Route path="/search/patient-trace" element={<PatientTracePage nextPage="/search/results" />} />
                    <Route path="/search/results" element={<SearchResultsPage />} />
                    <Route path="/search/results/delete-documents-confirmation" element={<DeleteDocumentsPage />} />
                </Route>
                <Route
                    path="/upload"
                    element={
                        <PatientDetailsProvider>
                            <Outlet />
                        </PatientDetailsProvider>
                    }
                >
                    <Route path="/upload/patient-trace" element={<PatientTracePage nextPage="/upload/submit" />} />
                    <Route path="/upload/submit" element={<UploadDocumentsPage nextPagePath="/home" />} />
                </Route>
            </Route>
        </Routes>
    );
};

const AuthenticatorErrors = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");
    return isOIDCAuthActive ? <Authenticator.Errors /> : null;
};

const App = () => {
    return (
        <ConfigurationProvider config={config}>
            <Router>
                <AuthProvider>
                    <Layout>
                        <AuthenticatorErrors />
                        <AppRoutes />
                    </Layout>
                </AuthProvider>
            </Router>
        </ConfigurationProvider>
    );
};

export default App;
