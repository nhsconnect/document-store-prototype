class ApiClient {
  constructor(api, auth) {
    this.api = api;
    this.auth = auth;
  }

  async findByNhsNumber(nhsNumber) {
    const data = await this.api.get('doc-store-api', '/DocumentReference', {
      headers: {
        'Accept': 'application/fhir+json',
        'Authorization': `Bearer ${(await this.auth.currentSession()).getIdToken().getJwtToken()}`,
      },
      queryStringParameters: {
        'subject.identifier': `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });

    return data.total > 0 ? data.entry.map(({ resource }) => ({
      description: resource.description,
      type: resource.type.coding.map(coding => coding.code).join(', '),
      url: resource.docStatus === 'final' ? resource.content[0].attachment.url : '',
    })) : [];
  }
}

export default ApiClient;
