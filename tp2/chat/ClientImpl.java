package chat;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

public class ClientImpl implements Client_itf {
    Integer id;
    ChatHistory chatHistory;
    ClientList clientList;

    public ClientImpl( HashMap<Integer, Client_itf> c, LinkedList<Message> m) {
        id = 0;
        clientList = new ClientList(c);
        chatHistory = new ChatHistory(m);
    }

    public void connect(Integer id, Client_itf itf, Server_itf server) throws RemoteException {
        chatHistory.loadChatHistory(server);
        clientList.loadClientList(server);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void connect(Client_itf itf) {

    }

    @Override
    public void disconnect(Integer id) {

    }

    @Override
    public void sendMessage(String s) {
        Message m = new Message(id, s);
        chatHistory.sendMessage(clientList, m);
    }

    @Override
    public void resMessage(Message m) {
        chatHistory.loadNewMessage(m);
    }
}
