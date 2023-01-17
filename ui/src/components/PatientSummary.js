import React from "react";
import { SummaryList } from "nhsuk-react-components";
import { getFormattedDate } from "../utils/utils";

const PatientSummary = ({ patientDetails }) => {
    return (
        <>
            <p>NHS number {patientDetails?.nhsNumber}</p>
            <SummaryList>
                <SummaryList.Row>
                    <SummaryList.Key>Surname</SummaryList.Key>
                    <SummaryList.Value>{patientDetails?.familyName}</SummaryList.Value>
                </SummaryList.Row>
                <SummaryList.Row>
                    <SummaryList.Key>First name</SummaryList.Key>
                    <SummaryList.Value>{patientDetails?.givenName?.map((name) => `${name} `)}</SummaryList.Value>
                </SummaryList.Row>
                <SummaryList.Row>
                    <SummaryList.Key>Date of birth</SummaryList.Key>
                    <SummaryList.Value>{getFormattedDate(patientDetails?.birthDate)}</SummaryList.Value>
                </SummaryList.Row>
                <SummaryList.Row>
                    <SummaryList.Key>Postcode</SummaryList.Key>
                    <SummaryList.Value>{patientDetails?.postalCode}</SummaryList.Value>
                </SummaryList.Row>
            </SummaryList>
        </>
    );
};

export default PatientSummary;
