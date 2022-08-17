import React from "react";
import { Link } from "react-router-dom";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";

const HomePage = () => {
    const isMultiStepUploadPathEnabled = useFeatureToggle(
        "PDS_TRACE_FOR_UPLOAD_ENABLED"
    );
    const uploadPathHref = isMultiStepUploadPathEnabled
        ? "/upload/patient-trace"
        : "/upload";
    return (
        <>
            <h3>Document Store</h3>
            <p>Use this service to:</p>
            <ul>
                <li>
                    <Link to="/search">View Stored Patient Record</Link>
                </li>
                <li>
                    <Link to={uploadPathHref}>Upload Patient Record</Link>
                </li>
            </ul>
        </>
    );
};

export default HomePage;
