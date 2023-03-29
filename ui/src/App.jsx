import React from "react";
import { BrowserRouter, Outlet, Route, Routes } from "react-router-dom";
import "./App.scss";
import config from "./config";
import OidcAuthenticator from "./auth/oidcAuthenticator/OidcAuthenticator";
import HomePage from "./pages/homePage/HomePage";
import Layout from "./components/layout/Layout";
import PatientDetailsProvider from "./providers/patientDetailsProvider/PatientDetailsProvider";
import { PatientTracePage } from "./pages/patientTracePage/PatientTracePage";
import UploadDocumentsPage from "./pages/uploadDocumentsPage/UploadDocumentsPage";
import SearchResultsPage from "./pages/searchResultsPage/SearchResultsPage";
import StartPage from "./pages/startPage/StartPage";
import OIDCAuthCallbackRouter from "./auth/authCallbackRouters/OIDCAuthCallbackRouter";
import AuthProvider from "./providers/authProvider/AuthProvider";
import DeleteDocumentsPage from "./pages/deleteDocumentsPage/DeleteDocumentsPage";
import ConfigProvider, { useFeatureToggle } from "./providers/configProvider/ConfigProvider";
import AuthCallbackRouter from "./auth/authCallbackRouters/AuthCallbackRouter";
import AuthErrorPage from "./pages/authErrorPage/AuthErrorPage";
import DocumentStoreProvider from "./providers/documentStoreProvider/DocumentStoreProvider";
import routes from "./enums/routes";
import ProtectedRoutes from "./auth/protectedRoutes/ProtectedRoutes";
import AuthSuccessRouter from "./auth/authSuccessRouter/AuthSuccessRouter";

const AppRoutes = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    const {
        ROOT,
        AUTH_CALLBACK,
        AUTH_SUCCESS,
        AUTH_ERROR,
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
                element={isOIDCAuthActive ? <OIDCAuthCallbackRouter /> : <AuthCallbackRouter />}
                path={AUTH_CALLBACK}
            />
            <Route element={<AuthSuccessRouter />} path={AUTH_SUCCESS} />
            <Route element={<AuthErrorPage />} path={AUTH_ERROR} />
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
        <ConfigProvider config={config}>
            <BrowserRouter>
                <AuthProvider>
                    <Layout>
                        <AuthenticatorErrors />
                        <AppRoutes />
                    </Layout>
                </AuthProvider>
            </BrowserRouter>
        </ConfigProvider>
    );
};

export default App;
