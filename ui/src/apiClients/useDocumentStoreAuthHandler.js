import { useNavigate } from "react-router";

export const useDocumentStoreAuthHandler = (apiRequest) => {
    const navigate = useNavigate();

    return () => {
        try {
            return apiRequest();
        } catch (error) {
            if (error.response.status === 401) {
                navigate("/");
            } else {
                throw error;
            }
        }
    };
};
