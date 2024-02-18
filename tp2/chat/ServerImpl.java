package chat;

import java.rmi.RemoteException;
import java.util.*;

public class ServerImpl implements Server_itf {

		private Set<Integer> knownIds = new HashSet<>();
		private HashMap<Integer, Client_itf> connected = new HashMap<>();
		private int cur_id = 0;

        public void connect(Integer id, Client_itf itf) throws RemoteException {
			if (knownIds.contains(id)) {
				System.out.println("Connected user with ID" + String.valueOf(id));
				connected.put(id, itf);
			} else {
				System.out.println("ERROR: Non-registered ID" + String.valueOf(id));
			}
        }

        @Override
        public void disconnect(Integer id) throws RemoteException {
			connected.remove(id);
        }

        @Override
        public void sendMessage(String s) throws RemoteException {
			
        }

        @Override
        public void resMessage(Message m) throws RemoteException {

        }

        @Override
        public int genId() throws RemoteException {
            cur_id++;
			knownIds.add(cur_id);
			System.out.println(cur_id);
			return cur_id;
        }

        @Override
        public HashMap<Integer, Client_itf> getClientList() throws RemoteException {
            return connected;
        }

        @Override
        public LinkedList getChatHistory() throws RemoteException {
            return null;
        }

		@Override
		public boolean ping() throws RemoteException {
			return true;
		}
}
