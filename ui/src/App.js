import React from "react";
import Amplify, { Auth, API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "./config";
import Search from "./Search";
import ApiClient from "./apiClient";
import "./App.scss"
import UploadDocument from "./UploadDocument";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);

const App = () => (
    <AmplifyAuthenticator>
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
    </AmplifyAuthenticator>
);

export default App;