import { useNavigate } from "react-router";
import { useCallback } from "react";
import { useAuth } from "react-oidc-context";

export const useDocumentStoreAuthErrorInterceptor = () => {
    const { removeUser } = useAuth();
    const navigate = useNavigate();

    return useCallback(
        async (error) => {
            if (error.response?.status === 401) {
                await removeUser();
                navigate("/");
            } else {
                throw error;
            }
        },
        [navigate, removeUser]
    );
};
