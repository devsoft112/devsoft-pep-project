package Controller;
import Model.Account;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * TODO: You will need to write your own endpoints and handlers for your
 * controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a
 * controller may be built.
 */
public class SocialMediaController {

    // Add a data structure to store the registered accounts (simulating a database)
    // This should ideally be a database connection, but for simplicity, we'll use a
    // simple data structure here.
    private Map<String, Account> accounts = new HashMap<>();

    /**
     * In order for the test cases to work, you will need to write the endpoints in
     * the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * 
     * @return a Javalin app object which defines the behavior of the Javalin
     *         controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerHandler);

        return app;
    }

    /**
     * This is the handler for the registration endpoint.
     * 
     * @param context The Javalin Context object manages information about both the
     *                HTTP request and response.
     */
    private void registerHandler(Context context) {
        // Get the JSON payload from the request body and parse it as an Account object
        Account newAccount = context.bodyAsClass(Account.class);

        // Validate the JSON payload and check if the account already exists
        if (newAccount.getUsername().isBlank()
                || newAccount.getPassword().length() < 4
                || accounts.containsKey(newAccount.getUsername())) {
            context.status(400).json("Registration failed");
            return;
        }

        // Generate an account_id (you can use any suitable method for this)
        int accountId = generateAccountId();

        // Set the account_id in the newAccount object
        newAccount.setAccount_id(accountId);

        // Add the newAccount to the accounts data structure
        accounts.put(newAccount.getUsername(), newAccount);

        // Return the newAccount object in the response
        context.status(200).json(newAccount);
    }

    /**
     * This method generates a unique account_id (you can replace this with a more
     * suitable approach).
     * 
     * @return a unique account_id integer.
     */
    private int generateAccountId() {
        // Here, you can use any suitable method to generate an integer account_id
        // For example, you can use a random number generator or increment a counter.
        // In this example, I'll use a random number generator.
        return new Random().nextInt(100000); // Generating a random integer between 0 and 99999
    }
}
