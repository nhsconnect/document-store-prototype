import axios from "axios";
import { useMemo } from "react";

export const useStorageClient = () => {
    return useMemo(() => axios.create(), []);
};
