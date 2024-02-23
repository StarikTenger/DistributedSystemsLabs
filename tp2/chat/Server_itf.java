package chat;
import java.rmi.*;
import java.util.*;

public interface Server_itf extends Client_itf {
	public int genId() throws RemoteException; // New id for new user
	public HashMap<Integer, Client_itf> getClientList() throws RemoteException;
	public boolean ping() throws RemoteException;

	public HashMap<Integer, Client_itf> getClients() throws RemoteException;
	public LinkedList<Message> getChat() throws RemoteException;
}
