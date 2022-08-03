import React from "react";
import Amplify, { Auth, API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "../config";
import Search from "../components/Search";
import ApiClient from "../apiClients/apiClient";
import "./App.scss"
import UploadDocument from "../components/UploadDocument";
import Layout from "../components/layout";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);

const App = () => (
    <AmplifyAuthenticator>
        <Layout>
            <h3>Document Store</h3>
            <div>
                <Search apiClient={client} />
            </div>
            <div>
                <UploadDocument apiClient={client} />
            </div>
            <div>
                <AmplifySignOut />
            </div>
        </Layout>
    </AmplifyAuthenticator>
);

export default App;