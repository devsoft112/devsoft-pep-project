package Controller;

import io.javalin.http.Context;
import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Service.AccountService;
import Service.MessageService;

import DAO.AccountDAO;
import DAO.MessageDAO;

import Model.Account;
import Model.Message;


public class SocialMediaController {

    private AccountService accountService;
    private MessageService messageService;

    private Map<String, Account> accounts = new HashMap<>();
    private AccountDAO accountDAO = new AccountDAO();
    private MessageDAO messageDAO = new MessageDAO();

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerHandler);
        app.post("/login", this::loginHandler);
        app.post("/messages", this::createMessageHandler);
        app.get("/messages/{id}", this::getMessageById);
        app.get("/messages", this::getAllMessages);
        app.get("/accounts/{id}/messages", this::getAllMessagesFromUser);
        app.delete("/messages/{id}", this::deleteMessageById);
        app.patch("/messages/{id}", this::updateMessageById);


        // Initialize the AccountService with the AccountDAO
        accountService = new AccountService(new AccountDAO());

        return app;
    }

    private void registerHandler(Context context) {
        Account newAccount = context.bodyAsClass(Account.class);

        // Check if the username is blank or password length is less than 4 characters
        if (newAccount.getUsername().isBlank() || newAccount.getPassword().length() < 4) {
            context.status(400).json("");
            return;
        }

        // Create the new account using the AccountService
        Account createdAccount = accountService.addAccount(newAccount);

        if (createdAccount != null) {
            context.status(200).json(createdAccount);
        } else {
            context.status(400).json(""); // Registration failed
        }
    }

    private void loginHandler(Context context) {
        Account loginAccount = context.bodyAsClass(Account.class);

        // Find the account with the provided username and password
        Account matchedAccount = accountService.findAccountByUsernameAndPassword(loginAccount.getUsername(), loginAccount.getPassword());

        if (matchedAccount != null) {
            context.status(200).json(matchedAccount); // Login successful  asdwqd
        } else {
            context.status(401).json(""); // Login failed
        }
    }
    
    private void createMessageHandler(Context context) {
        messageService = new MessageService();


        // Get the JSON payload from the request body and parse it as a Message object
        Message newMessage = context.bodyAsClass(Message.class);

        System.out.println(context);
    
        // Check if the message_text is not blank and is under 255 characters
        String messageText = newMessage.getMessage_text();
        if (messageText == null || messageText.trim().isEmpty() || messageText.length() > 254) {
            context.status(400).json("");
            return;
        }
    
        // Check if the posted_by refers to a real, existing user (account) asdw  asd qwd 
        int postedByUserId = newMessage.getPosted_by();
        Account postedByUser = accountDAO.getAccountById(postedByUserId);
        if (postedByUser == null) {
            context.status(400).json("");
            return;
        }
    
        // Insert the new message into the database
        Message addedMessage = messageService.addMessage(newMessage);

        if (addedMessage != null) {
            context.status(200).json(addedMessage);
        } else {
            context.status(500).json(""); // Something went wrong on the server side
        }
    } 

    private void getMessageById(Context context) {
        messageService = new MessageService();

        int messageId = Integer.parseInt(context.pathParam("id"));
        Message messageFound = messageService.getMessageById(messageId);

        if(messageFound != null){
            context.status(200).json(messageFound);
        }else{
            context.status(200).json("");
        }
        
    }

    private void getAllMessages(Context context){
        messageService = new MessageService();

        List<Message> messagesFound = messageService.getAllMessages();

        if(messagesFound != null){
            context.status(200).json(messagesFound);
        }else{
            context.status(200).json("");
        }
    }

    private void getAllMessagesFromUser(Context context){
        messageService = new MessageService();

        int messageId = Integer.parseInt(context.pathParam("id"));
        List<Message> messages = messageService.getAllMessagesFromUser(messageId);

        if(messages != null){
            context.status(200).json(messages);
        }else{
            context.status(200).json("");
        }
    }

    private void deleteMessageById(Context context) {
        messageService = new MessageService();

        int messageId = Integer.parseInt(context.pathParam("id"));
        Message messageFound = messageService.deleteMessageById(messageId);

        if(messageFound != null){
            context.status(200).json(messageFound);
        }else{
            context.status(200).json("");
        }
    }

    private void updateMessageById(Context context){
        messageService = new MessageService();

        int messageId = Integer.parseInt(context.pathParam("id"));
        Message newMessage = context.bodyAsClass(Message.class);
        newMessage.message_id = messageId;

        Message messageFound = messageService.updateMessageById(newMessage);

        if (messageFound == null || messageFound.message_text.trim().isEmpty() || messageFound.message_text.length() > 254) {
            context.status(400).json("");
            return;
        }
        
        if (messageFound != null) {
            context.status(200).json(messageFound);
        }
    }

}
    