import React from "react";
// eslint-disable-next-line import/namespace
import { useIdleTimer } from "react-idle-timer";
import { useBaseAPIUrl } from "../configProvider/ConfigProvider";
import routes from "../../enums/routes";

function IdleProvider({ children }) {
    const baseAPIUrl = useBaseAPIUrl("doc-store-api");
    const redirect_uri = new URL(routes.INACTIVITY_ERROR, window.location.href);
    const url = `${baseAPIUrl}/Auth/Logout?redirect_uri=${redirect_uri}`;
    const fifteenMinutes = 900000;
    useIdleTimer({
        onIdle: () => {
            window.location.replace(url);
        },
        onActive: () => null,
        onAction: () => null,
        timeout: fifteenMinutes,
        throttle: 500,
    });
    return <div>{children}</div>;
}

export default IdleProvider;
