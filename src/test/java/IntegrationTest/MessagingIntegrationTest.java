package IntegrationTest;

import Application.Controller.SocialMediaController;
import Application.Model.Message;
import Application.Util.ConnectionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

/**
 * Integration tests are a separate category from Unit tests. Where Unit tests are for testing an individual piece of
 * functionality, integration tests verify the behavior of multiple parts of the application (in this case,
 * the DAO, Service, and Controller portions should all work correctly in unison.)
 *
 * Because messages rely on existing users, the basic functionality of adding new users via the
 * POST localhost:8080/register endpoint should be functional.
 *
 * DO NOT CHANGE ANYTHING IN THIS CLASS
 */
public class MessagingIntegrationTest {
    SocialMediaController socialMediaController;
    ObjectMapper objectMapper;
    HttpClient webClient;
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
     * Calling GET localhost:8080/messages when there are no messages (empty database) should return an empty list with
     * a 200 OK status code.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void getAllMessagesEmptyTest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .build();
        HttpResponse response = webClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response should have a response code of 200 (this is the default)
        Assert.assertEquals(200, status);
//        the response body should be an empty list of messages.
        List<Message> messages = objectMapper.readValue(response.body().toString(),
                new TypeReference<List<Message>>(){});
        Assert.assertTrue(messages.isEmpty());
    }
    /**
     * Calling POST localhost:8080/messages when there are no users with a body containing a message referencing a
     * non-existent user should respond with a 400 status code.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postMessageNonExistentAccountTest() throws IOException, InterruptedException {
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
//        the response status should be 400 (client error in request)
        Assert.assertEquals(400, status);
    }

    /**
     * Calling POST http://localhost:8080/register with a valid account that does not exist yet in the database
     * should respond with a 200 status code. This test doesn't really need to be here, but exists as a reminder
     * that the ability to add a new user MUST exist before any later tests may succeed.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountTest() throws IOException, InterruptedException {
        HttpRequest postUserRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postUserResponse = webClient.send(postUserRequest, HttpResponse.BodyHandlers.ofString());
        int postUserStatus = postUserResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, postUserStatus);
    }
    /**
     * Calling POST http://localhost:8080/messages with a valid message that does not exist yet in the database
     * should respond with a 200 status code as well as a response body containing the added message.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountAndMessageTest() throws IOException, InterruptedException {
//        the ability to post a new account registration is prerequisite to this test
        postAccountTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"posted_by\":1, " +
                        "\"message_text\": \"hello message\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postMessageResponse = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int postMessageStatus = postMessageResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, postMessageStatus);
        Message expectedMessageFromPostResponseBody = new Message(1, 1, "hello message",
                1669947792);
        Message actualMessageFromPostResponseBody = objectMapper.readValue(postMessageResponse.body().toString(),
                Message.class);
//        the posted message should be in the response body
        Assert.assertEquals(expectedMessageFromPostResponseBody , actualMessageFromPostResponseBody);
    }
    /**
     * Calling POST http://localhost:8080/messages with a message that has a message_text over 255 characters should
     * result in a response status of 400 (client error). The provided message should not be persisted
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountAndTooLongMessage() throws IOException, InterruptedException {
//        the ability to post a new account registration is prerequisite to this test
        postAccountTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"posted_by\":1, " +
                        "\"message_text\": \"Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postMessageResponse = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int postMessageStatus = postMessageResponse.statusCode();
//        the response status should be 400 (client error)
        Assert.assertEquals(400, postMessageStatus);
    }
    /**
     * Calling POST http://localhost:8080/messages with a message that has a blank message_text should
     * result in a response status of 400 (client error). The provided message should not be persisted
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountAndBlankMessage() throws IOException, InterruptedException {
//        the ability to post a new account registration is prerequisite to this test
        postAccountTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"posted_by\":1, " +
                        "\"message_text\": \"\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postMessageResponse = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int postMessageStatus = postMessageResponse.statusCode();
//        the response status should be 400 (client error)
        Assert.assertEquals(400, postMessageStatus);
    }
    /**
     * After a user and message have been posted, the API should then be able to GET http://localhost:8080/messages/1 to
     * retrieve the posted message.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountAndMessageThenGetMessageTest() throws IOException, InterruptedException {
//        reuse the code to post a single user and a message - this test must pass first!
        postAccountAndMessageTest();
        HttpRequest getMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .build();
        HttpResponse getMessageResponse = webClient.send(getMessageRequest, HttpResponse.BodyHandlers.ofString());
        int getMessageStatus = getMessageResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, getMessageStatus);
        Message expectedMessagedFromGetResponseBody = new Message(1, 1, "hello message",
                1669947792);
        Message actualMessageFromGetResponseBody = objectMapper.readValue(getMessageResponse.body().toString(), Message.class);
//        the response body should contain the json of message 1
        Assert.assertEquals(expectedMessagedFromGetResponseBody, actualMessageFromGetResponseBody);
    }
    /**
     * After an account and message have been posted, the API should then be able to GET http://localhost:8080/messages
     * to retrieve all posted messages
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postAccountAndMessageAndGetAllMessagesTest() throws IOException, InterruptedException {
//        reuse the code to post a single user and a message - this test must pass first!
        postAccountAndMessageTest();
        HttpRequest getMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .build();
        HttpResponse getMessageResponse = webClient.send(getMessageRequest, HttpResponse.BodyHandlers.ofString());
        int getMessageStatus = getMessageResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, getMessageStatus);
        Message expectedMessagesFromGetResponseBody = new Message(1, 1, "hello message",
                1669947792);
        List<Message> actualMessagesFromResponseBody =
                objectMapper.readValue(getMessageResponse.body().toString(), new TypeReference<List<Message>>(){});
//        the response body should be a list containing exactly one message (the one that was posted_
        Assert.assertTrue(actualMessagesFromResponseBody.contains(expectedMessagesFromGetResponseBody));
        Assert.assertTrue(actualMessagesFromResponseBody.size() == 1);
    }
    /**
     * The API should be able to register multiple accounts and post new messages that are associated to those users.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postManyAccountsAndMessagesTest() throws IOException, InterruptedException {
//        reuse the code to post a single user and a message - this test must pass first!
        postAccountAndMessageTest();
        HttpRequest postUserRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/register"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"username\": \"user2\", " +
                        "\"password\": \"password\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postUserResponse2 = webClient.send(postUserRequest2, HttpResponse.BodyHandlers.ofString());
        int postUserStatus2 = postUserResponse2.statusCode();
        Assert.assertEquals(200, postUserStatus2);
        HttpRequest postMessageRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"posted_by\":1, " +
                        "\"message_text\": \"message 2\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postMessageResponse2 = webClient.send(postMessageRequest2, HttpResponse.BodyHandlers.ofString());
        int postMessageStatus2 = postMessageResponse2.statusCode();
        Assert.assertEquals(200, postMessageStatus2);
        HttpRequest postMessageRequest3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .POST(HttpRequest.BodyPublishers.ofString("{" +
                        "\"posted_by\":2, " +
                        "\"message_text\": \"message 3\", " +
                        "\"time_posted_epoch\": 1669947792}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse postMessageResponse3 = webClient.send(postMessageRequest3, HttpResponse.BodyHandlers.ofString());
        int postMessageStatus3 = postMessageResponse3.statusCode();
        Assert.assertEquals(200, postMessageStatus3);
    }

    /**
     * When many users and messages exist, all the users and messages should be retrieved in a list from the
     * endpoint GET http://localhost:8080/messages.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postManyUsersAndMessagesThenGetAllMessages() throws IOException, InterruptedException {
//        reuse the code to post multiple users and messages - this test must pass first!
        postManyAccountsAndMessagesTest();
        HttpRequest getMessageByAuthorRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages"))
                .build();
        HttpResponse getMessageByAuthorResponse = webClient.send(getMessageByAuthorRequest,
                HttpResponse.BodyHandlers.ofString());
        int getMessageByAuthorStatus = getMessageByAuthorResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, getMessageByAuthorStatus);
        Message message1 = new Message(1, 1, "hello message", 1669947792);
        Message message2 = new Message(2, 1, "message 2", 1669947792);
        Message message3 = new Message(3, 2, "message 3", 1669947792);
        List<Message> actualMessages = objectMapper.readValue(getMessageByAuthorResponse.body().toString(),
                new TypeReference<List<Message>>(){});
//        the response body should be a list containing all posted messages
        Assert.assertTrue(actualMessages.contains(message1));
        Assert.assertTrue(actualMessages.contains(message2));
        Assert.assertTrue(actualMessages.contains(message3));
    }

//    GET ALL BY AUTHOR ID

    /**
     * When there are no messages and no users, a request to GET http://localhost:8080/accounts/1/messages should
     * respond with an empty list.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void getMessagesByAccountIdEmpty() throws IOException, InterruptedException {
        HttpRequest getMessageByAuthorRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/1/messages"))
                .build();
        HttpResponse getMessageByAuthorResponse = webClient.send(getMessageByAuthorRequest, HttpResponse.BodyHandlers.ofString());
        int getMessageByAuthorStatus = getMessageByAuthorResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, getMessageByAuthorStatus);
        List<Message> messages = objectMapper.readValue(getMessageByAuthorResponse.body().toString(), new TypeReference<List<Message>>(){});
//        the response body should contain an empty list
        Assert.assertTrue(messages.isEmpty());
    }
    /**
     * When many accounts and messages already exist, the API should then be able to
     * GET http://localhost:8080/users/1/messages to retrieve the messages posted by a user, assuming that there exists
     * a user with an ID of 1.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void postManyAccountsAndMessagesThenGetMessagesByAccountIdTest() throws IOException, InterruptedException {
//        reuse the code to post multiple users and messages - this test must pass first!
        postManyAccountsAndMessagesTest();
        HttpRequest getMessageByAuthorRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/accounts/1/messages"))
                .build();
        HttpResponse getMessageByAuthorResponse = webClient.send(getMessageByAuthorRequest, HttpResponse.BodyHandlers.ofString());
        int getMessageByAuthorStatus = getMessageByAuthorResponse.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, getMessageByAuthorStatus);
        Message message1 = new Message(1, 1, "hello message", 1669947792);
        Message message2 = new Message(2, 1, "message 2", 1669947792);
        Message message3 = new Message(3, 2, "message 3", 1669947792);
        List<Message> actualMessages = objectMapper.readValue(getMessageByAuthorResponse.body().toString(), new TypeReference<List<Message>>(){});
//        the response body should be a list containing the messages posted by user 1
        Assert.assertTrue(actualMessages.contains(message1));
        Assert.assertTrue(actualMessages.contains(message2));
//        the response body list should not contain the messages posed by user 2
        Assert.assertFalse(actualMessages.contains(message3));
    }
    /**
     * When many accounts and messages already exist, the API should then be able to
     * GET http://localhost:8080/messages to retrieve all messages.
     * @throws IOException
     * @throws InterruptedException
     */

//    DELETE MESSAGE

    /**
     * Calling DELETE localhost:8080/messages/{message_id} with message_id referencing a non-existent message should
     * respond with a 200 OK status and an empty body. DELETE is intended to be idempotent and to always return 200.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void deleteMessageNonExistentMessageTest() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/101"))
                .DELETE()
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 200 (ok)
        Assert.assertEquals(200, status);
//        the response body should be empty
        Assert.assertTrue(response.body().toString().length()==0);
    }
    /**
     * Deleting an existing message on DELETE http://localhost:8080/messages/1 should respond with a body containing
     * the deleted message.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void deleteMessageTest() throws IOException, InterruptedException {
//        The ability to post an account and a message is prerequisite to this test
        postAccountAndMessageTest();
        HttpRequest deleteMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .DELETE()
                .build();
        HttpResponse deleteMessageResponse = webClient.send(deleteMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = deleteMessageResponse.statusCode();
//        the response should have a response code of 200 (this is the default)
        Assert.assertEquals(200, status);
        Message expectedMessageResponseBody = new Message(1, 1, "hello message", 1669947792);
        Message actualMessageResponseBody = objectMapper.readValue(deleteMessageResponse.body().toString(), Message.class);
//        the response body should contain the expected message
        Assert.assertEquals(expectedMessageResponseBody, actualMessageResponseBody);
    }
    /**
     * Deleting a message should make the API unable to retrieve it by its ID.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void deletedMessageShouldNotExistTest() throws IOException, InterruptedException {
//        The ability to delete an existing message is prerequisite to this test
        deleteMessageTest();
        HttpRequest getMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .build();
        HttpResponse getMessageResponse = webClient.send(getMessageRequest, HttpResponse.BodyHandlers.ofString());
//        the response body should be empty
        Assert.assertTrue(getMessageResponse.body().toString().length()==0);
    }

//    PATCH MESSAGE

    /**
     * Calling PATCH localhost:8080/messages/{message_id} with a body containing a message but the URI referencing a
     * non-existent message should respond with a 400 status code.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void patchMessageNonExistentMessageTest() throws IOException, InterruptedException {
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/101"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{"+
                        "\"message_text\": \"updated message\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 400 (client error in request)
        Assert.assertEquals(400, status);
    }
    /**
     * Calling PATCH localhost:8080/messages with a body containing a message referencing an
     * existing message should respond with a 200 status code and the full updated message.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void patchMessageTest() throws IOException, InterruptedException {
//        the functionality to post accounts and messages is prerequisite for this test
        postAccountAndMessageTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{"+
                        "\"message_text\": \"updated message\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 200 (ok)
        Assert.assertEquals(200, status);
        Message expectedMessage = new Message(1, 1, "updated message", 1669947792);
        Message actualMessage = objectMapper.readValue(response.body().toString(), Message.class);
//        the response body should contain the expected message
        Assert.assertEquals(expectedMessage, actualMessage);
    }
    /**
     * After a message has been patched, the update should be reflected when the API retrieves the message with
     * GET http://localhost:8080/messages/1.
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void getMessageAfterPatchTest() throws IOException, InterruptedException {
//        the functionality to patch message_text is prerequisite to this test
        patchMessageTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 200
        Assert.assertEquals(200, status);
        Message expectedMessage = new Message(1, 1, "updated message", 1669947792);
        Message actualMessage = objectMapper.readValue(response.body().toString(), Message.class);
//        the response body should contain the expected message
        Assert.assertEquals(expectedMessage, actualMessage);
    }
    /**
     * An attempt to patch a message via PATCH http://localhost:8080/messages/{message_id} with a new message over
     * 255 characters should result in a response status of 400 (client error)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void patchMessageTooLongTest() throws IOException, InterruptedException {
//        the functionality to post accounts and messages is prerequisite to this test
        postAccountAndMessageTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{"+
                        "\"message_text\": \"Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters Over 255 characters " +
                        "                Over 255 characters Over 255 characters Over 255 characters\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 400 (Client error)
        Assert.assertEquals(400, status);
    }
    /**
     * An attempt to patch a message via PATCH http://localhost:8080/messages/{message_id} with a blank message_text
     * should result in a response status of 400 (client error)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void patchMessageBlankTest() throws IOException, InterruptedException {
//        the functionality to post accounts and messages is prerequisite to this test
        postAccountAndMessageTest();
        HttpRequest postMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/messages/1"))
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{"+
                        "\"message_text\": \"\" }"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse response = webClient.send(postMessageRequest, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
//        the response status should be 400 (Client error)
        Assert.assertEquals(400, status);
    }
}