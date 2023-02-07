import { useNavigate } from "react-router";
import { useCallback } from "react";
import { useAuth } from "react-oidc-context";

export const useDocumentStoreAuthErrorInterceptor = () => {
    const { removeUser } = useAuth();
    const navigate = useNavigate();

    return useCallback(
        (error) => {
            // TODO: [PRMT-3018] Remove this log once Axios/API Gateway 401 issues are resolved
            console.debug("useDocumentStoreAuthErrorInterceptor", error);
            if (error.response?.status === 401) {
                void removeUser();
                navigate("/");
            } else {
                throw error;
            }
        },
        [navigate, removeUser]
    );
};
