package chat;
import java.rmi.*;
import java.util.*;

public interface Client_itf extends Remote {
	public void connect(Integer id, Client_itf itf) throws RemoteException; // Add id to table
	public Integer getId() throws RemoteException;
	public void disconnect(Integer id) throws RemoteException; // Remove id from table
	public void sendMessage(String s) throws RemoteException; // Send message to this client
	public void resMessage(Message m) throws RemoteException; // Send message to this client
}
