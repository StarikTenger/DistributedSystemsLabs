package chat;

import java.util.*;

public class ServerImpl implements Server_itf {

		private Set<Integer> knownIds = new HashSet<>();
		private HashMap<Integer, Client_itf> connected = new HashMap<>();
		private int cur_id = 0;

        public void connect(Integer id, Client_itf itf) {
			if (knownIds.contains(id)) {
				System.out.println("Connected user with ID" + String.valueOf(id));
				connected.put(id, itf);
			} else {
				System.out.println("ERROR: Non-registered ID" + String.valueOf(id));
			}
        }

        @Override
        public void disconnect(Integer id) {
			connected.remove(id);
        }

        @Override
        public void sendMessage(String s) {
			
        }

        @Override
        public void resMessage(Message m) {

        }

        @Override
        public int genId() {
            cur_id++;
			return cur_id;
        }

        @Override
        public HashMap<Integer, Client_itf> getClientList() {
            return connected;
        }

        @Override
        public LinkedList getChatHistory() {
            return null;
        }
}
