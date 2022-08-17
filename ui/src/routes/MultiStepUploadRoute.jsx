import { Outlet } from "react-router";
import { MultiStepUploadProvider } from "../providers/MultiStepUploadProvider";

export const MultiStepUploadRoute = () => {
    return (
        <MultiStepUploadProvider>
            <Outlet />
        </MultiStepUploadProvider>
    );
};
