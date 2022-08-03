async function uploadDocument(s3_url, document, token) {
    const requestHeaders = {
        'Accept': 'application/fhir+json',
        'Authorization': `Bearer ${token}`,
    }
    return fetch(s3_url, {method: "PUT", headers: requestHeaders, body: document})
}

export default uploadDocument