package uk.nhs.digital.docstore.testHarness;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.testHarness.helpers.AuthorizedRequestBuilderFactory;
import uk.nhs.digital.docstore.testHarness.helpers.UserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.amazonaws.services.cognitoidp.model.AuthFlowType.ADMIN_NO_SRP_AUTH;
import static com.amazonaws.services.cognitoidp.model.AuthFlowType.ADMIN_USER_PASSWORD_AUTH;
import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.requireNonNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

public class DocumentStoreJourneyTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final String INTERNAL_DOCKER_HOST = "172.17.0.2";

    private static final String endpoint = System.getenv("DOCUMENT_STORE_BASE_URI");
    private static final String userPoolId = JsonPath.read(System.getenv("COGNITO_USER_POOL_IDS"), "$[0]");

    private static String getHost() {
        String host = System.getenv("DS_TEST_HOST");
        return (host != null) ? host : DEFAULT_HOST;
    }

    private AWSCognitoIdentityProvider cognitoClient;
    private String username;

    @BeforeEach
    private void setUp() {
        cognitoClient = AWSCognitoIdentityProviderClientBuilder
                .standard().withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion("eu-west-2")
                .build();
        username = UUID.randomUUID().toString();
    }

    @AfterEach
    private void tearDown() {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUsername(username)
                .withUserPoolId(userPoolId);
        cognitoClient.adminDeleteUser(deleteUserRequest);
    }

    @Test
    void documentsCanBeDownloadedWhenTheyHaveBeenSuccessfullyUploaded() throws IOException, InterruptedException, URISyntaxException {
        String idToken = createAuthenticatedSession();

        String documentContent = "hello";
        String documentReference = createDocumentReference(idToken);

        URI documentUploadUri = extractDocumentUri(documentReference);
        uploadDocument(documentContent, documentUploadUri);

        String id = JsonPath.read(documentReference, "$.id");
        String updatedDocumentReference = fetchUpdatedDocumentReference(id);

        URI documentDownloadUri = extractDocumentUri(updatedDocumentReference);
        String downloadedDocument = downloadDocument(documentDownloadUri);
        assertThat(downloadedDocument).isEqualTo(documentContent);
    }

    private String createAuthenticatedSession() {
        UserFactory userFactory = new UserFactory(cognitoClient, userPoolId);
        String password = generatePassword();
        String clientId = JsonPath.read(System.getenv("COGNITO_CLIENT_IDS"), "$[0]");
        userFactory.createUser(username + "@example.com", username, password);
        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(ADMIN_USER_PASSWORD_AUTH)
                .withAuthParameters(Map.of("USERNAME", username, "PASSWORD", password))
                .withClientId(clientId)
                .withUserPoolId(userPoolId);
        AdminInitiateAuthResult adminInitiateAuthResult = cognitoClient.adminInitiateAuth(authRequest);
        AuthenticationResultType authenticationResult =
                adminInitiateAuthResult.getAuthenticationResult();
        return authenticationResult.getIdToken();
    }

    private String createDocumentReference(String idToken) throws URISyntaxException, IOException, InterruptedException {
        String expectedDocumentReference = getContentFromResource("CreatedDocumentReference.json");
        String content = getContentFromResource("CreateDocumentReferenceRequest.json");

        var createDocumentReferenceRequest = newPostBuilder("DocumentReference", content)
                .header("Content-Type", "application/fhir+json")
                .header("Accept", "application/fhir+json")
                .header("Authorization", "Bearer " + idToken)
                .build();

        var createdDocumentReferenceResponse = newHttpClient().send(createDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));

        var documentReference = createdDocumentReferenceResponse.body();
        assertThat(createdDocumentReferenceResponse.statusCode())
                .isEqualTo(201);
        String id = JsonPath.read(documentReference, "$.id");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Location"))
                .hasValue("DocumentReference/" + id);
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url", "$.meta")
                .isEqualTo(expectedDocumentReference);
        return documentReference;
    }

    public static HttpRequest.Builder newPostBuilder(String path, String content) throws URISyntaxException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(new URI(endpoint + "/" + path));
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(content));
        return requestBuilder;
    }

    private void uploadDocument(String content, URI documentUploadUri) throws IOException, InterruptedException {
        var documentUploadRequest = HttpRequest.newBuilder(documentUploadUri)
                .PUT(BodyPublishers.ofString(content))
                .build();
        var documentUploadResponse = newHttpClient().send(documentUploadRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);
    }

    private String fetchUpdatedDocumentReference(String id) {
        HttpResponse<String> documentReferenceResponse = waitAtMost(30, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> getDocumentResponse(id), documentIsFinal());

        assertThat(documentReferenceResponse.statusCode()).isEqualTo(200);
        String documentReference = documentReferenceResponse.body();
        assertThatJson(documentReference)
                .inPath("$.indexed")
                .asString()
                .satisfies(indexed -> {
                    var indexedAsInstant = Instant.parse(indexed);
                    assertThat(indexedAsInstant).isAfter(Instant.now().minus(30, SECONDS));
                });
        return documentReference;
    }

    private String downloadDocument(URI documentDownloadUri) throws IOException, InterruptedException {
        var documentRequest = HttpRequest.newBuilder(documentDownloadUri)
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        return newHttpClient()
                .send(documentRequest, BodyHandlers.ofString(UTF_8))
                .body();
    }

    private HttpResponse<String> getDocumentResponse(String id) throws URISyntaxException, IOException, InterruptedException {
        var retrieveDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newGetBuilder("DocumentReference/" + id).build();

        return newHttpClient().send(retrieveDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));
    }

    private Predicate<HttpResponse<String>> documentIsFinal() {
        return response -> response.statusCode() == 200
                && "final".equals(JsonPath.read(response.body(), "$.docStatus"));
    }

    private URI extractDocumentUri(String documentReference) throws URISyntaxException {
        return new URI(JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost()));
    }

    private String getContentFromResource(String resourcePath) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream stream = requireNonNull(classLoader.getResourceAsStream(resourcePath))) {
            return new String(stream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("unable to load resource '%s'", resourcePath), e);
        }
    }

    private String generatePassword() {
        int length = 20;
        String digits = "0123456789";
        String specials = "~=+%^*/()[]{}/!@#$?|";
        String all = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz"
                + digits + specials;
        Random rnd = new Random();
        List<String> result = new ArrayList<>();
        Consumer<String> appendChar = s ->
                result.add("" + s.charAt(rnd.nextInt(s.length())));
        appendChar.accept(digits);
        appendChar.accept(specials);
        while (result.size() < length)
            appendChar.accept(all);
        Collections.shuffle(result, rnd);
        return String.join("", result);
    }
}
