import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import Controller.SocialMediaController;
import Model.Message;
import Util.ConnectionUtil;
import io.javalin.Javalin;

public class RetrieveMessageByMessageIdTest {
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
     * Sending an http request to GET localhost:8080/messages/1 
     * 
     * Expected Response:
     *  Status Code: 200
     *  Response Body: JSON represenation of a message object
     */
    @Test
    public void getMessageGivenMessageIdMessageFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .build();
        HttpResponse response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Assert.assertEquals(200, status);

        Message expectedResult = new Message(1, 1, "test message 1", 1669947792);
        Message actualResult = objectMapper.readValue(response.body().toString(), Message.class);
        Assert.assertEquals(expectedResult, actualResult);
    }


    /**
     * Sending an http request to GET localhost:8080/messages/100 (message id 100 does not exist)
     * 
     * Expected Response:
     *  Status Code: 200
     *  Response Body: 
     */
    @Test
    public void getMessageGivenMessageIdMessageNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/100"))
                .build();
        HttpResponse response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Assert.assertEquals(200, status);
        Assert.assertTrue(response.body().toString().isEmpty());
    }


}
