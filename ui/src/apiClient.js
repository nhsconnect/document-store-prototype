class ApiClient {
  constructor(api) {
    this.api = api;
  }

  async findByNhsNumber(nhsNumber) {
    const data = await this.api.get('doc-store-api', '/DocumentReference', {
      headers: {
        'Accept': 'application/fhir+json'
      },
      queryStringParameters: {
        'subject.identifier': `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
      },
    });
    return data.entry.map(entry => ({
      description: entry.description,
      type: entry.type.coding.map(coding => coding.code).join(', '),
      url: entry.docStatus === 'final' ? entry.content[0].attachment.url : '',
    }));
  }
}

export default ApiClient;
