async function uploadDocument(s3_url, document, token) {
  return fetch(s3_url, { method: "PUT", body: document });
}

export default uploadDocument;
