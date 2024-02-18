package chat;
import java.rmi.*;
import java.util.*;

public interface Server_itf extends Client_itf {
	public int genId(); // New id for new user
	public HashMap<Integer, Client_itf> getClientList();
	public LinkedList getChatHistory();
}
