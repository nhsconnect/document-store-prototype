import React from "react";
import { BrowserRouter as Router, Outlet, Route, Routes } from "react-router-dom";
import "./App.scss";
import config from "./config";
import Authenticator from "./components/authenticator/Authenticator";
import HomePage from "./pages/homePage/HomePage";
import Layout from "./components/layout/Layout";
import { PatientDetailsProvider } from "./providers/PatientDetailsProvider";
import { PatientTracePage } from "./pages/patientTracePage/PatientTracePage";
import UploadDocumentPage from "./pages/uploadDocumentPage/UploadDocumentPage";
import SearchResultsPage from "./pages/searchResultsPage/SearchResultsPage";
import StartPage from "./pages/startPage/StartPage";
import AuthCallbackRouter from "./components/authenticator/AuthCallbackRouter";
import AuthProvider from "./components/authenticator/AuthProvider";
import DeleteDocumentsConfirmationPage from "./pages/deleteDocumentsConfirmationPage/DeleteDocumentsConfirmationPage";
import ConfigurationProvider from "./providers/ConfigurationProvider";

const AppRoutes = () => {
    return (
        <Routes>
            <Route element={<StartPage />} path="/" />
            <Route element={<AuthCallbackRouter />} path="cis2-auth-callback" />
            <Route
                element={
                    <Authenticator.Protected>
                        <Outlet />
                    </Authenticator.Protected>
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
                    <Route
                        path="/search/results/delete-documents-confirmation"
                        element={<DeleteDocumentsConfirmationPage />}
                    />
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
                    <Route path="/upload/submit" element={<UploadDocumentPage nextPagePath="/home" />} />
                </Route>
            </Route>
        </Routes>
    );
};

const App = () => {
    return (
        <ConfigurationProvider config={config}>
            <Router>
                <AuthProvider>
                    <Layout>
                        <Authenticator.Errors />
                        <AppRoutes />
                    </Layout>
                </AuthProvider>
            </Router>
        </ConfigurationProvider>
    );
};

export default App;
