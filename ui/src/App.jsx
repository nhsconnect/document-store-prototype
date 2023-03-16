import React from "react";
import { BrowserRouter, Outlet, Route, Routes } from "react-router-dom";
import "./App.scss";
import config from "./config";
import OidcAuthenticator from "./auth/oidcAuthenticator/OidcAuthenticator";
import HomePage from "./pages/homePage/HomePage";
import Layout from "./components/layout/Layout";
import PatientDetailsProvider from "./providers/PatientDetailsProvider";
import { PatientTracePage } from "./pages/patientTracePage/PatientTracePage";
import UploadDocumentsPage from "./pages/uploadDocumentsPage/UploadDocumentsPage";
import SearchResultsPage from "./pages/searchResultsPage/SearchResultsPage";
import StartPage from "./pages/startPage/StartPage";
import OIDCAuthCallbackRouter from "./auth/authCallbackRouters/OIDCAuthCallbackRouter";
import AuthProvider from "./providers/AuthProvider";
import DeleteDocumentsPage from "./pages/deleteDocumentsPage/DeleteDocumentsPage";
import ConfigurationProvider, { useFeatureToggle } from "./providers/ConfigurationProvider";
import SessionAuthCallbackRouter from "./auth/authCallbackRouters/SessionAuthCallbackRouter";
import DocumentStoreProvider from "./providers/DocumentStoreProvider";
import routes from "./enums/routes";
import ProtectedRoutes from "./auth/protectedRoutes/ProtectedRoutes";

const AppRoutes = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    const {
        ROOT,
        AUTH_CALLBACK,
        HOME,
        UPLOAD,
        UPLOAD_SEARCH_PATIENT,
        UPLOAD_SUBMIT,
        SEARCH,
        SEARCH_PATIENT,
        SEARCH_RESULTS,
        SEARCH_RESULTS_DELETE,
    } = routes;

    const ProtectedAuthRoutes = ({ children }) => {
        return isOIDCAuthActive ? (
            <OidcAuthenticator.Protected>{children}</OidcAuthenticator.Protected>
        ) : (
            <ProtectedRoutes>{children}</ProtectedRoutes>
        );
    };

    return (
        <Routes>
            <Route element={<StartPage />} path={ROOT} />
            <Route
                element={isOIDCAuthActive ? <OIDCAuthCallbackRouter /> : <SessionAuthCallbackRouter />}
                path={AUTH_CALLBACK}
            />
            <Route
                element={
                    <ProtectedAuthRoutes>
                        <DocumentStoreProvider>
                            <Outlet />
                        </DocumentStoreProvider>
                    </ProtectedAuthRoutes>
                }
            >
                <Route path={HOME} element={<HomePage />} />
                <Route
                    path={SEARCH}
                    element={
                        <PatientDetailsProvider>
                            <Outlet />
                        </PatientDetailsProvider>
                    }
                >
                    <Route path={SEARCH_PATIENT} element={<PatientTracePage nextPage={SEARCH_RESULTS} />} />
                    <Route path={SEARCH_RESULTS} element={<SearchResultsPage />} />
                    <Route path={SEARCH_RESULTS_DELETE} element={<DeleteDocumentsPage />} />
                </Route>
                <Route
                    path={UPLOAD}
                    element={
                        <PatientDetailsProvider>
                            <Outlet />
                        </PatientDetailsProvider>
                    }
                >
                    <Route path={UPLOAD_SEARCH_PATIENT} element={<PatientTracePage nextPage={UPLOAD_SUBMIT} />} />
                    <Route path={UPLOAD_SUBMIT} element={<UploadDocumentsPage nextPagePath={HOME} />} />
                </Route>
            </Route>
        </Routes>
    );
};

const AuthenticatorErrors = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");
    return isOIDCAuthActive ? <OidcAuthenticator.Errors /> : null;
};

const App = () => {
    return (
        <ConfigurationProvider config={config}>
            <BrowserRouter>
                <AuthProvider>
                    <Layout>
                        <AuthenticatorErrors />
                        <AppRoutes />
                    </Layout>
                </AuthProvider>
            </BrowserRouter>
        </ConfigurationProvider>
    );
};

export default App;
