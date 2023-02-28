import { Factory } from "fishery";

const buildPatientDetails = (patientDetailsOverride) => {
    return {
        birthDate: "1970-01-01",
        familyName: "Default Surname",
        givenName: ["Default Given Name"],
        nhsNumber: "0000000000",
        postalCode: "AA1 1AA",
        superseded: false,
        ...patientDetailsOverride,
    };
};

const searchResultFactory = Factory.define(() => ({
    id: "some-id",
    description: "Some description",
    type: "some type",
    indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

export { buildPatientDetails, searchResultFactory };
