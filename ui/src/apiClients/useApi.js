import ApiClient from "./apiClient";
import { API } from "aws-amplify";
import { useAuth } from "react-oidc-context";

const useApi = () => {
    const { user } = useAuth();
    return new ApiClient(API, user);
}

export default useApi;