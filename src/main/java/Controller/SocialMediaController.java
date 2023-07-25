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

    private Map<String, Account> accounts = new HashMap<>();
    private AccountDAO accountDAO = new AccountDAO();
    private MessageDAO messageDAO = new MessageDAO();

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerHandler);
        app.post("/login", this::loginHandler);
        app.post("/messages", this::createMessageHandler);


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
        // Get the JSON payload from the request body and parse it as a Message object
        Message newMessage = context.bodyAsClass(Message.class);
    
        // Check if the message_text is not blank and is under 255 characters
        String messageText = newMessage.getMessage_text();
        if (messageText == null || messageText.trim().isEmpty() || messageText.length() > 255) {
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
        Message addedMessage = messageDAO.insertMessage(newMessage);
        if (addedMessage != null) {
            context.status(201).json(addedMessage);
        } else {
            context.status(500).json(""); // Something went wrong on the server side
        }
    }
    
    
}
