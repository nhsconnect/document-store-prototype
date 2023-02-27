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

export { buildPatientDetails };
