import java.rmi.*;
import java.util.*;

public  class RegistryImpl implements Registry_itf {
	private Map<Integer, Accounting_itf> users = new HashMap<>();
	private Map<Integer, Integer> call_count = new HashMap<>();;
	private int curId = 0;
	private int threshold = 2;

	private int newId() {
		curId++;
		return curId;
	}

	public boolean user_exists(int id) {
		return users.get(id) != null;
	}

	public void bind_id(int id, Accounting_itf client) {
		users.put(id, client);
	}

	public void add_count(int id) throws RemoteException {
		call_count.put(id, call_count.get(id) + 1); // ZHOPA
		if (call_count.get(id) >= threshold) {
			users.get(id).numberOfCalls(call_count.get(id));
		}
	}

	public void register(Accounting_itf client) throws RemoteException {
		int id = newId();
		bind_id(id, client);
		call_count.put(id, 0);
		System.out.println("Registered client with id " + String.valueOf(id));
		client.setId(id);
	}
}

