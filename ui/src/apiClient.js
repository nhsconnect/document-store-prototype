import {Auth} from "aws-amplify";

class ApiClient {
  constructor(api) {
    this.api = api;
  }

  async findByNhsNumber(nhsNumber) {
    const data = await this.api.get('doc-store-api', '/DocumentReference', {
      headers: {
        'Accept': 'application/fhir+json',
        'Authorization': `Bearer ${(await Auth.currentSession()).getIdToken().getJwtToken()}`,
      },
      queryStringParameters: {
        'subject.identifier': `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });
    return data.entry.map(entry => ({
      id: entry.resource.id,
      description: entry.resource.description,
      type: entry.resource.type.coding.map(coding => coding.code).join(', '),
      url: entry.resource.docStatus === 'final' ? entry.resource.content[0].attachment.url : '',
    }));
  }
}

export default ApiClient;
