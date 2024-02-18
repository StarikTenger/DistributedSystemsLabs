package chat;

import java.rmi.RemoteException;
import java.util.HashMap;

public class ClientImpl implements Client_itf {
    String id;
    ChatHistory chatHistory;
    ClientList clientList;
	private HashMap<Integer, Client_itf> connected = new HashMap<>();

	@Override
    public void connect(Integer id, Client_itf itf) throws RemoteException {
		connected.put(id, itf);
    }

    @Override
    public void disconnect(Integer id) throws RemoteException {
		connected.remove(id);
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
