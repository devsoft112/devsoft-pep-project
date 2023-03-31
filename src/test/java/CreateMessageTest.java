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

public class CreateMessageTest {
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
     * Sending an http request to POST localhost:8080/messages with valid message credentials
     * 
     * Expected Response:
     *  Status Code: 200
     *  Response Body: JSON representation of message object
     */
    @Test
    public void createMessageSuccessful() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1, " +
                        "\"message_text\": \"hello message\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        Assert.assertEquals(200, status);        

        ObjectMapper om = new ObjectMapper();
        Message expectedResult = new Message(2, 1, "hello message", 1669947792);
        System.out.println(response.body().toString());
        Message actualResult = om.readValue(response.body().toString(), Message.class);
        Assert.assertEquals(expectedResult, actualResult);
    }

    /**
     * Sending an http request to POST localhost:8080/messages with empty message
     * 
     * Expected Response:
     *  Status Code: 400
     *  Response Body: 
     */
    @Test
    public void createMessageMessageTextBlank() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1, " +
                        "\"message_text\": \"\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();

        Assert.assertEquals(400, status);        
        Assert.assertEquals("", response.body().toString());
    }


    /**
     * Sending an http request to POST localhost:8080/messages with message length greater than 254
     * 
     * Expected Response:
     *  Status Code: 400
     *  Response Body: 
     */
    @Test
    public void createMessageMessageGreaterThan254() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":1, " +
                        "\"message_text\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        
        Assert.assertEquals(400, status);        
        Assert.assertEquals("", response.body().toString());
    }


    /**
     * Sending an http request to POST localhost:8080/messages with a user id that doesnt exist in db
     * 
     * Expected Response:
     *  Status Code: 400
     *  Response Body: 
     */
    @Test
    public void createMessageUserNotInDb() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{"+
                        "\"posted_by\":3, " +
                        "\"message_text\": \"message test\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        
        Assert.assertEquals(400, status);        
        Assert.assertEquals("", response.body().toString());
    }


}
