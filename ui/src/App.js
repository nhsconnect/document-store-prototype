import React from "react";
import Amplify, { API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "./config";
import Search from "./Search";
import ApiClient from "./apiClient";
import "./App.scss"

Amplify.configure(awsConfig);

const client = new ApiClient(API);

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