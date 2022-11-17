import {setUrlHostToLocalHost} from "../utils/utils";
import axios from "axios";

export const documentUploadStates = {
  WAITING: "waiting",
  STORING_METADATA: "storing_metadata",
  UPLOADING: "uploading",
  SUCCEEDED: "succeeded",
  FAILED: "failed",
}

class ApiClient {
  constructor(api, auth) {
    this.api = api;
    this.auth = auth;
  }

  async findByNhsNumber(nhsNumber) {
    const data = await this.api.get("doc-store-api", "/DocumentReference", {
      headers: {
        Accept: "application/fhir+json",
        Authorization: `Bearer ${(await this.auth.currentSession())
          .getIdToken()
          .getJwtToken()}`,
      },
      queryStringParameters: {
        "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });

    return data.total > 0
      ? data.entry.map(({resource}) => ({
        id: resource.id,
        description: resource.description,
        indexed: new Date(resource.indexed),
      }))
      : [];
  }

  async uploadDocument(document, nhsNumber, onUploadStateChange) {
    const requestBody = {
      resourceType: "DocumentReference",
      subject: {
        identifier: {
          system: "https://fhir.nhs.uk/Id/nhs-number",
          value: nhsNumber,
        },
      },
      type: {
        coding: [
          {
            system: "http://snomed.info/sct",
            code: "22151000087106",
          },
        ],
      },
      content: [
        {
          attachment: {
            contentType: document.type,
          },
        },
      ],
      description: document.name,
      created: "2021-07-11T16:57:30+01:00",
    };
    const token = (await this.auth.currentSession())
      .getIdToken()
      .getJwtToken();
    const requestHeaders = {
      Accept: "application/fhir+json",
      Authorization: `Bearer ${token}`,
    };
    onUploadStateChange(documentUploadStates.WAITING, 0)

    try {
      const response = await this.api.post(
        "doc-store-api",
        "/DocumentReference",
        {
          body: requestBody,
          headers: requestHeaders,
          onUploadProgress: () => {
            onUploadStateChange(documentUploadStates.STORING_METADATA, 0)
          }
        }
      );

      const url = response.content[0].attachment.url;
      let s3Url = setUrlHostToLocalHost(url);

      await axios.put(
        s3Url,
        document,
        {
          onUploadProgress: (({ total, loaded }) => {
            onUploadStateChange(documentUploadStates.UPLOADING, (loaded/total)*100)
          })
        }
      )
      onUploadStateChange(documentUploadStates.SUCCEEDED, 100)
    } catch (e) {
      onUploadStateChange(documentUploadStates.FAILED, 0)
    }
  }

  async getPatientDetails(nhsNumber) {
    const data = await this.api.get("doc-store-api", "/PatientDetails", {
      headers: {
        Accept: "application/fhir+json",
        Authorization: `Bearer ${(await this.auth.currentSession())
          .getIdToken()
          .getJwtToken()}`,
      },
      queryStringParameters: {
        "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });
    return data.total > 0
      ? data.entry.map(({resource}) => ({
        dateOfBirth: new Date(resource.birthDate),
        postcode: resource.address[0].postalCode,
        name: resource.name[0],
      }))
      : [];
  }

  async getPresignedUrl(id) {
    const data = await this.api.get("doc-store-api", "/DocumentReference/"+id, {
      headers: {
        Accept: "application/fhir+json",
        Authorization: `Bearer ${(await this.auth.currentSession())
          .getIdToken()
          .getJwtToken()}`,
      },
    });

    if (data?.docStatus === "final") {
      return data.content[0].attachment;
    }
    throw new Error("No url received");
  }

  async getPresignedUrlForZip(nhsNumber) {
    const data = await this.api.get("doc-store-api", "/DocumentManifest", {
      headers: {
        Accept: "application/fhir+json",
        Authorization: `Bearer ${(await this.auth.currentSession())
            .getIdToken()
            .getJwtToken()}`,
      },
      queryStringParameters: {
        "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });

    if (data) {
      return setUrlHostToLocalHost(data);
    }

    throw new Error("No url received");
  }
}

export default ApiClient;
