package tp2.chat;
import java.rmi.*;
import java.util.*;

public interface Client_itf extends Remote {
	public void connect(Integer id, Client_itf itf); // Add id to table
	public void disconnect(Integer id); // Remove id from table
	public void message(String s); // Send message to this client
}
