import { Amplify, Auth, API } from "aws-amplify";
import React from "react";
// import {
//     BrowserRouter as Router,
//     Outlet,
//     Route,
//     Routes,
// } from "react-router-dom";
import awsConfig from "../config";
import ApiClient from "../apiClients/apiClient";
import Authenticator from "../components/Authenticator/Authenticator";
import HomePage from "./HomePage";
import Layout from "../components/layout";
import FeatureToggleProvider, {
    useFeatureToggle,
} from "../providers/FeatureToggleProvider";
import { NhsNumberProvider } from "../providers/NhsNumberProvider";
import { PatientTracePage } from "./PatientTracePage";
import UploadDocumentPage from "./UploadDocumentPage";
import SearchResultsPage from "./SearchResultsPage";
import StartPage from "./StartPage";
import CIS2AuthenticationResultNavigator from "../components/Authenticator/CIS2AuthenticationResultNavigator";
import UploadSuccessPage from "./UploadSuccessPage";

Amplify.configure(awsConfig);

const client = new ApiClient(API, Auth);
// const AppRoutes = () => {
//     const isCIS2Enabled = useFeatureToggle(
//         "CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED"
//     );
//     const homePagePath = isCIS2Enabled ? "/home" : "/";
//     return (
//         <Routes>
//             {isCIS2Enabled && (
//                 <>
//                     <Route element={<StartPage />} path={"/"} />
//                     <Route
//                         element={<CIS2AuthenticationResultNavigator />}
//                         path={"cis2-auth-callback"}
//                     />
//                 </>
//             )}
//             <Route
//                 element={
//                     <Authenticator.Protected>
//                         <Outlet />
//                     </Authenticator.Protected>
//                 }
//             >
//                 <Route path={homePagePath} element={<HomePage />} />
//                 <Route
//                     path="/search"
//                     element={
//                         <NhsNumberProvider>
//                             <Outlet />
//                         </NhsNumberProvider>
//                     }
//                 >
//                     <Route
//                         path="/search/patient-trace"
//                         element={
//                             <PatientTracePage
//                                 client={client}
//                                 nextPage={"/search/results"}
//                                 title={"Download and view a stored document"}
//                             />
//                         }
//                     />
//                     <Route
//                         path="/search/results"
//                         element={<SearchResultsPage client={client} />}
//                     />
//                 </Route>
//
//                 <Route
//                     path="/upload"
//                     element={
//                         <NhsNumberProvider>
//                             <Outlet />
//                         </NhsNumberProvider>
//                     }
//                 >
//                     <Route
//                         path="/upload/patient-trace"
//                         element={
//                             <PatientTracePage
//                                 client={client}
//                                 nextPage={"/upload/submit"}
//                                 title={"Upload a document"}
//                             />
//                         }
//                     />
//                     <Route
//                         path="/upload/submit"
//                         element={<UploadDocumentPage client={client} />}
//                     />
//                     <Route
//                         path="/upload/success"
//                         element={
//                             <UploadSuccessPage nextPagePath={homePagePath} />
//                         }
//                     />
//                 </Route>
//             </Route>
//         </Routes>
//     );
// };

const Index = () => {
    return (
        <FeatureToggleProvider>
            {/*<Router>*/}
            {/*    <Authenticator>*/}
                    <Layout>
                        <Authenticator.Errors />
                        {/*<AppRoutes />*/}
                    </Layout>
                {/*</Authenticator>*/}
            {/*</Router>*/}
        </FeatureToggleProvider>
    );
};

export default Index;
