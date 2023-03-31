import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Controller.SocialMediaController;
import Model.Message;
import Util.ConnectionUtil;
import io.javalin.Javalin;

public class RetrieveAllMessagesForUserTest {
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
     * Sending an http request to GET localhost:8080/accounts/1/messages (messages exist for user) 
     * 
     * Expected Response:
     *  Status Code: 200
     *  Response Body: JSON representation of a list of messages
     */
    @Test
    public void getAllMessagesFromUserMessageExists() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/1/messages"))
                .build();
        HttpResponse response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Assert.assertEquals(200, status);

        List<Message> expectedResult = new ArrayList<>();
        expectedResult.add(new Message(1, 1, "test message 1", 1669947792));
        List<Message> actualResult = objectMapper.readValue(response.body().toString(), new TypeReference<List<Message>>(){});
        Assert.assertEquals(expectedResult, actualResult);
    }

    /**
     * Sending an http request to GET localhost:8080/accounts/1/messages (messages does NOT exist for user) 
     * 
     * Expected Response:
     *  Status Code: 200
     *  Response Body:
     */
    @Test
    public void getAllMessagesFromUserNoMessagesFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/2/messages"))
                .build();
        HttpResponse response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Assert.assertEquals(200, status);

        List<Message> actualResult = objectMapper.readValue(response.body().toString(), new TypeReference<List<Message>>(){});
        Assert.assertTrue(actualResult.isEmpty());
    }
}
