import React from "react";
import { Hero } from "nhsuk-react-components";
import { Link } from "react-router-dom";
import { usePatientDetailsContext } from "../../providers/patientDetailsProvider/PatientDetailsProvider";

const DownloadConfirmationPage = () => {
    const [patientDetails] = usePatientDetailsContext();

    return (
        <div style={{ maxWidth: 600 }}>
            <Hero style={{ margin: "auto" }}>
                <Hero.Heading>Documents downloaded successfully for</Hero.Heading>
                <Hero.Text>
                    {patientDetails?.givenName?.map((name) => `${name} `)}
                    {patientDetails?.familyName}
                </Hero.Text>
                <Hero.Text>NHS number ({patientDetails?.nhsNumber})</Hero.Text>
            </Hero>
            <p style={{ marginTop: 40 }}>
                When you have finished processing the files for this patient, delete all files from your computer.
            </p>
            <p>
                <Link to="/">Start Again</Link>
            </p>
        </div>
    );
};

export default DownloadConfirmationPage;
