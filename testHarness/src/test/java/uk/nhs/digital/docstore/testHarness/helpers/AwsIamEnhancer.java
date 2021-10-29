package uk.nhs.digital.docstore.testHarness.helpers;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.http.HttpMethodName;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AwsIamEnhancer implements AuthorizationEnhancer {
    private DefaultRequest<String> getAWSAuthorization(URI endpoint, String resourcePath, HttpMethodName httpMethod, String content) throws URISyntaxException {
        DefaultRequest<String> apiRequest = new DefaultRequest<>("execute-api");
        apiRequest.setHttpMethod(httpMethod);
        apiRequest.setEndpoint(endpoint);
        apiRequest.setResourcePath(resourcePath);

        if (content != null) {
            apiRequest.setContent(new ByteArrayInputStream(content.getBytes()));
        }


        AWSCredentials credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
        AWS4Signer aws4Signer = new AWS4Signer();
        aws4Signer.setServiceName("execute-api");
        aws4Signer.setRegionName("eu-west-2");
        aws4Signer.sign(apiRequest, credentials);
        return apiRequest;
    }

    @Override
    public HttpRequest.Builder enhanceWithAuthorization(HttpRequest.Builder original, URI endpoint, String resourcePath, String content) throws URISyntaxException {
        HttpRequest httpRequest = original.build();
        DefaultRequest<String> awsAuthorization = getAWSAuthorization(endpoint, resourcePath, HttpMethodName.fromValue(httpRequest.method()), content);
        original.header("X-Amz-Date", awsAuthorization.getHeaders().get("x-amz-date"))
                .header("X-Amz-Security-Token", awsAuthorization.getHeaders().get("x-amz-security-token"))
                .header("Authorization", awsAuthorization.getHeaders().get("authorization"));
        return original;

    }
}
