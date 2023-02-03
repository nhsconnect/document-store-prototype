import axios from "axios";
import { useMemo } from "react";

export const useStorage = () => {
    return useMemo(() => axios.create());
};
