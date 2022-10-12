import {Amplify, API, Auth} from "aws-amplify";
import React from "react";
import awsConfig from "../config";
import ApiClient from "../apiClients/apiClient";
import StartPage from "./StartPage";

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
//                 <Route path={homePagePath} element={<Home />} />
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
        <StartPage />
    );
};

export default Index;
