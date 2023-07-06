import axios from "axios";
import { useBaseAPIUrl } from "../providers/configProvider/ConfigProvider";

const axiosService = axios.create({
    baseURL: useBaseAPIUrl,
});

axiosService.defaults.withCredentials = true;

export default axiosService;
