import React from "react";
import { Link } from "react-router-dom";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";

const HomePage = () => {
    const isPdsTraceEnabled = useFeatureToggle("PDS_TRACE_ENABLED");
    const uploadPathHref = isPdsTraceEnabled
        ? "/upload/patient-trace"
        : "/upload";
    const searchPathHref = isPdsTraceEnabled
        ? "/search/patient-trace"
        : "/search";
    return (
        <>
            <h3>Document Store</h3>
            <p>Use this service to:</p>
            <ul>
                <li>
                    <Link to={searchPathHref}>View Stored Patient Record</Link>
                </li>
                <li>
                    <Link to={uploadPathHref}>Upload Patient Record</Link>
                </li>
            </ul>
        </>
    );
};

export default HomePage;
