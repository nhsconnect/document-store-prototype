import { Factory } from "fishery";
import { documentUploadStates } from "../enums/documentUploads";
import { nanoid } from "nanoid/non-secure";

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

const buildTextFile = (name, size) => {
    const file = new File(["test"], `${name}.txt`, {
        type: "text/plain",
    });

    if (size) {
        Object.defineProperty(file, "size", {
            value: size,
        });
    }

    return file;
};

const buildDocument = (file, uploadStatus) => {
    return {
        file,
        state: uploadStatus ?? documentUploadStates.SUCCEEDED,
        progress: 0,
        id: nanoid(),
    };
};

const searchResultFactory = Factory.define(() => ({
    id: "some-id",
    description: "Some description",
    type: "some type",
    indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
}));

export { buildPatientDetails, buildTextFile, buildDocument, searchResultFactory };
