package chat;

public class ClientImpl implements Client_itf {
    String id;
    ChatHistory chatHistory;
    ClientList clientList;

	@Override
    public void connect(Integer id, Client_itf itf) {
    }

    @Override
    public void disconnect(Integer id) {

    }

    @Override
    public void sendMessage(String s) {
        // Message m = new Message(id, s);
        // chatHistory.sendMessage(clientList, m);
    }

    @Override
    public void resMessage(Message m) {
        chatHistory.loadNewMessage(m);
    }
}
