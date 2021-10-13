package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldE2eTest {
    private static final String BASE_URI_PATH = "http://localhost:4566/restapis/";
    private static final String USER_REQUEST_PATH = "_user_request_/";

    private static URI getBaseUri() {
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

    @Test
    void returnsHelloWorldResponse() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(getBaseUri().resolve("hello"))
                .GET()
                .build();

        var response = newHttpClient().send(request, BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World!");
    }
}
