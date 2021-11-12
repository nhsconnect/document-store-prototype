import React from "react";
import Amplify from "aws-amplify";
import {AmplifyAuthenticator, AmplifySignOut} from "@aws-amplify/ui-react";
import awsConfig from "./config";
import Search from "./Search";

Amplify.configure(awsConfig);

const App = () => (
    <AmplifyAuthenticator>
      <div>
        Document Store
        <AmplifySignOut />
      </div>
        <div>
            <Search />
        </div>
    </AmplifyAuthenticator>
);

export default App;