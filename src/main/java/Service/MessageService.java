package Service;

import Model.Message;
import DAO.MessageDAO;

public class MessageService {

    private MessageDAO messageDAO;

    public MessageService() {
        messageDAO = new MessageDAO();
    }

    public Message addMessage(Message message) {
        Message addedMessage = messageDAO.insertMessage(message);
        return addedMessage;
    }
}