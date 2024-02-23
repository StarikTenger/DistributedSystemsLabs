package chat;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

public class ClientImpl implements Client_itf {
    public Integer id;
    public ChatHistory chatHistory;
    ClientList clientList;
	public HashMap<Integer, Client_itf> connected = new HashMap<>();

	@Override
    public void connect(Integer id, Client_itf itf) throws RemoteException {
		connected.put(id, itf);
    }

    @Override
    public void disconnect(Integer id) throws RemoteException {
		connected.remove(id);
	}
	

    public void showHistory() throws RemoteException {
        chatHistory.showChatHistory();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void sendMessage(Message m) throws RemoteException {
        chatHistory.loadNewMessage(m);
    }
}
