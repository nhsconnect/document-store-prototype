import { Amplify, Auth, API } from "aws-amplify";
import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import "./App.scss";
import awsConfig from "./config";
import ApiClient from "./apiClients/apiClient";
import Authenticator from "./components/Authenticator/Authenticator";
import HomePage from "./pages/HomePage";
import SearchPage from "./pages/SearchPage";
import UploadPage from "./pages/UploadPage";
import Layout from "./components/layout";
import FeatureToggleProvider from "./providers/FeatureToggleProvider";

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
                  element={<SearchPage client={client} />}
                />
                <Route
                  path="/upload"
                  element={<UploadPage client={client} />}
                />
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
