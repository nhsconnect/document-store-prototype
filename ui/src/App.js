import React from "react";
import Amplify from "aws-amplify";
import {AmplifyAuthenticator, AmplifySignOut} from "@aws-amplify/ui-react";
import awsconfig from "./aws-export";
import Search from "./Search";

Amplify.configure(awsconfig);

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