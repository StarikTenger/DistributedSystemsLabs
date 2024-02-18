package chat;
import java.rmi.*;
import java.util.*;

public interface Client_itf extends Remote {
//	public void connect(Integer id, Client_itf itf); // Add id to table
	public void disconnect(Integer id); // Remove id from table
	public void sendMessage(String s); // Send message to this client
	public void resMessage(Message m); // Send message to this client
}
