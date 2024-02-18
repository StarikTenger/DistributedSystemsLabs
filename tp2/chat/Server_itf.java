package chat;
import java.rmi.*;
import java.util.*;

public interface Server_itf extends Client_itf {

	public HashMap<Integer, Client_itf> getClients() throws RemoteException;
	public LinkedList<Message> getChat() throws RemoteException;

	public int genId() throws RemoteException; // New id for new user
	public HashMap<Integer, Client_itf> getClientList() throws RemoteException;
}
