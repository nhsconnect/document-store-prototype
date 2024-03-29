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
import SessionProvider from "./providers/sessionProvider/SessionProvider";
import { PatientSummaryPage } from "./pages/patientSummaryPage/patientSummaryPage";
import OrgSelectPage from "./pages/orgSelectPage/OrgSelectPage";
import NoValidOrgsPage from "./pages/noValidOrgsPage/NoValidOrgsPage";

const AppRoutes = () => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    const {
        ROOT,
        AUTH_CALLBACK,
        AUTH_SUCCESS,
        AUTH_ERROR,
        HOME,
        NO_VALID_ORGANISATION,
        UPLOAD,
        UPLOAD_SEARCH_PATIENT,
        UPLOAD_SEARCH_PATIENT_RESULT,
        UPLOAD_SUBMIT,
        SEARCH,
        SEARCH_PATIENT,
        SEARCH_PATIENT_RESULT,
        SEARCH_RESULTS,
        SEARCH_RESULTS_DELETE,
        ORG_SELECT,
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
            <Route path={ORG_SELECT} element={<OrgSelectPage />} />
            <Route element={<AuthSuccessRouter />} path={AUTH_SUCCESS} />
            <Route element={<AuthErrorPage />} path={AUTH_ERROR} />
            <Route element={<NoValidOrgsPage />} path={NO_VALID_ORGANISATION} />
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
                    <Route path={SEARCH_PATIENT} element={<PatientTracePage nextPage={SEARCH_PATIENT_RESULT} />} />
                    <Route path={SEARCH_PATIENT_RESULT} element={<PatientSummaryPage nextPage={SEARCH_RESULTS} />} />

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
                    <Route
                        path={UPLOAD_SEARCH_PATIENT}
                        element={<PatientTracePage nextPage={UPLOAD_SEARCH_PATIENT_RESULT} />}
                    />
                    <Route
                        path={UPLOAD_SEARCH_PATIENT_RESULT}
                        element={<PatientSummaryPage nextPage={UPLOAD_SUBMIT} />}
                    />

                    <Route path={UPLOAD_SUBMIT} element={<UploadDocumentsPage nextPagePath={ROOT} />} />
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
                    <SessionProvider>
                        <Layout>
                            <AuthenticatorErrors />
                            <AppRoutes />
                        </Layout>
                    </SessionProvider>
                </AuthProvider>
            </BrowserRouter>
        </ConfigProvider>
    );
};

export default App;
