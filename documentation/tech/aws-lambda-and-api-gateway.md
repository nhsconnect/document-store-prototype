#### API Gateway
* Use HTTP API
* Use the lambda integration pattern with different invocations for each of the combination of HTTP method and path  
* Use AWS IAM authentication  allowing only the dev users to invoke the lambda

#### Lambda
* One repository with 1 lambda handler class/method for each different combinations of HTTP method and path. Build 1 artifact that contains all code and is deployed with different handlers for each individual lambda
* Use the `APIGatewayV2HTTPEvent` as an input event and `APIGatewayV2HTTPResponse` as the return type for the lambda
* Start with no extra support libraries and add as needed

#### Design
API endpoints - 1 lambda for each:
* `GET /DocumentReference/:id` - get metadata by id
* `GET /DocumentReference?subject.id=nhs_no|{nhsNumber}` - search metadata by nhs number
* `PUT /DocumentReference` - upload new document reference
* `GET /PatientDetails?subject.id=nhs_no|{nhsNumber}` - search Patient Demographic Service by nhs number