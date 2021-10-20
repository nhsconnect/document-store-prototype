package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;

import java.net.URI;
import java.util.List;

public class BaseUriHelper {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;

    @SuppressWarnings("HttpUrlsUsage")
    public static final String BASE_URI_TEMPLATE = "http://%s:%d";
    public static final String BASE_PATH_TEMPLATE = "/restapis/%s/%s/_user_request_/";

    public static URI getBaseUri() {
        var baseUri = String.format(BASE_URI_TEMPLATE, getHost(), DEFAULT_PORT);

        AmazonApiGateway amazonApiGatewayClient = AmazonApiGatewayClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION))
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

        return URI.create(baseUri)
                .resolve(String.format(BASE_PATH_TEMPLATE, restApiId, stageName));
    }

    private static String getHost() {
        String host = System.getenv("DS_TEST_HOST");
        return (host != null) ? host : DEFAULT_HOST;
    }
}
