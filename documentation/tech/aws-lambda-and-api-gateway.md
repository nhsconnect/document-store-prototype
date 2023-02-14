# API Gateway

* Use HTTP API
* Use the Lambda integration pattern with different invocations for each of the combination of HTTP method and path
* Use AWS IAM auth, allowing only the dev users to invoke the Lambda

# Lambda

* One repo with 1 Lambda handler class/method for each different combinations of HTTP method and path. Build 1 artifact
  that contains all code and is deployed with different handlers for each individual Lambda.
* Use the `APIGatewayV2HTTPEvent` as an input event and `APIGatewayV2HTTPResponse` as the return type for the Lambda.
* Start with no extra support libraries and add as needed.

# Design

API endpoints - 1 Lambda for each:

* `GET /DocumentReference/:id` - Get metadata by ID.
* `GET /DocumentReference?subject.id=nhs_no|{nhsNumber}` - Search metadata by NHS number.
* `PUT /DocumentReference` - Upload new document reference.
* `GET /PatientDetails?subject.id=nhs_no|{nhsNumber}` - Search PDS by NHS number.