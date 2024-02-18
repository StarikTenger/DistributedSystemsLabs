package chat;
import java.rmi.*;
import java.util.*;

public interface Server_itf extends Client_itf {
	public int genId(); // New id for new user
	public ArrayList<Client> getClientList();
	public LinkedList getChatHistory();
}
