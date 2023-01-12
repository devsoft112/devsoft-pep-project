

import Controller.SocialMediaController;
import Model.Account;
import Util.ConnectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
/**
 * Integration tests are a separate category from Unit tests. Where Unit tests are for testing an individual piece of
 * functionality, integration tests verify the behavior of multiple parts of the application (in this case,
 * the DAO, Service, and Controller portions should all work correctly in unison.)
 *
 * DO NOT CHANGE ANYTHING IN THIS CLASS
 */
public class AuthIntegrationTest {

    SocialMediaController socialMediaController;
    HttpClient webClient;
    ObjectMapper objectMapper;
    Javalin app;

    /**
     * Before every test, reset the database, restart the Javalin app, and create a new webClient and ObjectMapper
     * for interacting locally on the web.
     * @throws InterruptedException
     */
    @Before
    public void setUp() throws InterruptedException {
        ConnectionUtil.resetTestDatabase();
        socialMediaController = new SocialMediaController();
        app = socialMediaController.startAPI();
        webClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
        app.start(8080);
        Thread.sleep(1000);
    }

    @After
    public void tearDown() {
        app.stop();
    }

    /**
     * Calling POST localhost:8080/register when there are no users (empty database) with a body representing a valid
     * user should register the user and respond with the posted message and a 200 OK status code.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterTest() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, status);
        Account expectedAccount = new Account(1, "user", "password");
        Account actualAccount = objectMapper.readValue(response.body().toString(), Account.class);
//        the response body should contain the full posted account
        Assert.assertEquals(expectedAccount, actualAccount);

    }
    /**
     * Attempting to register a user with a blank username should cause the API to respond with a 400 status code.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterBlankUsernameTest() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response should be 400 (client error)
        Assert.assertEquals(400, status);
    }
    /**
     * Attempting to register a user with a password that is less than 4 characters long should cause the API
     * to respond with a 400 status code.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterShortPasswordTest() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"pw\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response should be 400 (client error)
        Assert.assertEquals(400, status);
    }
    /**
     * Inserting a valid user should return a 200 status code, but then adding another user with the same name should
     * return a 400 status code.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterDuplicateUsernameTest() throws IOException, InterruptedException {
        postRegisterTest();
        HttpRequest postRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response2 = webClient.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        int status2 = response2.statusCode();
//        the response should be 400 (client error)
        Assert.assertEquals(400, status2);
    }
    /**
     * Calling POST localhost:8080/login when there are no users (empty database) with a body representing
     * a non-existent user should respond with a 401 status code.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postLoginEmptyTest() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 401 (unauthorized)
        Assert.assertEquals(401, status);
    }
    /**
     * Calling POST http://localhost:8080/register with a valid user and then POST http://localhost:8080/login
     * with the same user should register an account and then accept the login request with a 200 OK response.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterThenPostLoginTest() throws IOException, InterruptedException {
        postRegisterTest();
        HttpRequest postLoginRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse loginResponse = webClient.send(postLoginRequest, HttpResponse.BodyHandlers.ofString());
        int status = loginResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, status);
        Account expectedAccount = new Account(1, "user", "password");
        Account actualAccount = objectMapper.readValue(loginResponse.body().toString(), Account.class);
//        the response body should contain the full account
        Assert.assertEquals(expectedAccount, actualAccount);
    }
    /**
     * Calling POST http://localhost:8080/register with a valid user and then POST http://localhost:8080/login
     * with different credentials should register an account and then deny the login request with a 401 response.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postRegisterThenPostIncorrectLoginTest() throws IOException, InterruptedException {
        postRegisterTest();
        HttpRequest postLoginRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/login"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"incorrectpassword\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse loginResponse = webClient.send(postLoginRequest, HttpResponse.BodyHandlers.ofString());
        int status = loginResponse.statusCode();
//        the response status should be 401 (unauthorized)
        Assert.assertEquals(401, status);
    }
}
