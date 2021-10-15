package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;

import java.net.URI;
import java.util.List;

public class BaseUriHelper {
    private static final String BASE_URI_PATH = "http://localhost:4566/restapis/";
    private static final String USER_REQUEST_PATH = "_user_request_/";

    static URI getBaseUri() {
        AmazonApiGateway amazonApiGatewayClient = AmazonApiGatewayClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "eu-west-2"))
                .build();

        List<RestApi> apis = amazonApiGatewayClient
                .getRestApis(new GetRestApisRequest())
                .getItems();
        String restApiId = apis.get(0).getId();

        List<Deployment> deployments = amazonApiGatewayClient
                .getDeployments(new GetDeploymentsRequest().withRestApiId(restApiId))
                .getItems();
        String deploymentId = deployments.get(0).getId();

        List<Stage> stages = amazonApiGatewayClient
                .getStages(new GetStagesRequest()
                        .withRestApiId(restApiId)
                        .withDeploymentId(deploymentId))
                .getItem();
        String stageName = stages.get(0).getStageName();

        return URI.create(BASE_URI_PATH).resolve(restApiId + "/").resolve(stageName + "/").resolve(USER_REQUEST_PATH);
    }
}
