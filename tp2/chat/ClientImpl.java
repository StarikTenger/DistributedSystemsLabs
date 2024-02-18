package chat;

import java.rmi.RemoteException;

public class ClientImpl implements Client_itf {
    String id;
    ChatHistory chatHistory;
    ClientList clientList;

	@Override
    public void connect(Integer id, Client_itf itf) throws RemoteException {
    }

    @Override
    public void disconnect(Integer id) throws RemoteException {

    }

    @Override
    public void sendMessage(String s) throws RemoteException {
        // Message m = new Message(id, s);
        // chatHistory.sendMessage(clientList, m);
    }

    @Override
    public void resMessage(Message m) throws RemoteException {
        chatHistory.loadNewMessage(m);
    }
}
