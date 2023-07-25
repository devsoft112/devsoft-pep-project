package Service;



import java.util.List;
import Model.Account;
import DAO.AccountDAO;




public class AccountService{
    private AccountDAO accountDAO;

    public AccountService(){
        accountDAO = new AccountDAO();
    }
    /**
     * Constructor for a AccountService when a AccountDAO is provided.
     * This is used for when a mock AccountDAO that exhibits mock behavior is used in the test cases.
     * This would allow the testing of AccountService independently of AccountDAO.
     * There is no need to modify this constructor.
     * @param AccountDAO
     */
    public AccountService(AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }
    /**
     * TODO: Use the AccountDAO to retrieve all authors.
     *
     * @return all authors
     */
    public List<Account> getAllAccounts() {
        List<Account> accounts = accountDAO.getAllaccounts();
        return accounts;
    }
    /**
     * TODO: Use the AccountDAO to persist an author. The given Author will not have an id provided.
     *
     * @param author an author object.
     * @return The persisted author if the persistence is successful.
     */
    public Account addAccount(Account account) {
        Account addAccount = accountDAO.insertAccount(account);
        return addAccount;
    }
    public Account findAccountByUsernameAndPassword(String username, String password) {
        Account account = accountDAO.findAccountByUsernameAndPassword(username, password);
        return account;
    }
    public Account findAccountById(int accountId) {
        return accountDAO.getAccountById(accountId);
    }

}