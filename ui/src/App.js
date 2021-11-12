import React from "react";
import Amplify, { API } from "aws-amplify";
import { AmplifyAuthenticator, AmplifySignOut } from "@aws-amplify/ui-react";
import awsConfig from "./config";
import Search from "./Search";
import ApiClient from "./apiClient";

Amplify.configure(awsConfig);

const client = new ApiClient(API);

const App = () => (
    <AmplifyAuthenticator>
      <div>
        Document Store
        <AmplifySignOut />
      </div>
        <div>
            <Search apiClient={client} />
        </div>
    </AmplifyAuthenticator>
);

export default App;