import React from "react";
import Amplify, { Auth, API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "./config";
import Search from "./Search";
import ApiClient from "./apiClient";
import "./App.scss"

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);

const App = () => (
    <AmplifyAuthenticator>
        <div>
            <Search apiClient={client} />
        </div>
        <div>
            <AmplifySignOut />
        </div>
    </AmplifyAuthenticator>
);

export default App;