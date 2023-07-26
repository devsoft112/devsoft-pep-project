package Service;

import Model.Message;
import DAO.MessageDAO;
import java.util.List;

public class MessageService {

    private MessageDAO messageDAO;

    public MessageService() {
        messageDAO = new MessageDAO();
    }

    public Message addMessage(Message message) {
        Message addedMessage = messageDAO.insertMessage(message);
        return addedMessage;
    }

    public Message getMessageById(int id){
        return messageDAO.getMessageById(id);
    }

    public List<Message> getAllMessages(){
        return messageDAO.getAllMessages();
    }

    public List<Message> getAllMessagesFromUser(int id){
        return messageDAO.getMessageByUserId(id);
    }

    public Message deleteMessageById(int messageId){
        return messageDAO.deleteMessageById(messageId);
    }

    public Message updateMessageById(Message message){
        return messageDAO.updateMessageById(message);
    }
}