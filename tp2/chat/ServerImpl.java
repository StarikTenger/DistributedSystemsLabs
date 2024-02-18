package chat;

import java.rmi.RemoteException;
import java.util.*;

public class ServerImpl implements Server_itf {

		private Set<Integer> knownIds = new HashSet<>();
		private HashMap<Integer, Client_itf> connected = new HashMap<>();
		private Integer count;
        ChatHistory chatHistory;
        ClientList clientList;
		private int cur_id = 0;

        public void connect(Integer id, Client_itf itf) throws RemoteException {
			if (knownIds.contains(id)) {
				System.out.println("Connected user with ID" + String.valueOf(id));
				connected.put(id, itf);
			} else {
				System.out.println("ERROR: Non-registered ID" + String.valueOf(id));
			}
		}


        public ServerImpl() {
                count = 0;
                chatHistory = new ChatHistory();
                clientList = new ClientList();
        }

        @Override
        public Integer getId() {
                return null;
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

        public void showHistory() throws RemoteException {
			System.out.println(chatHistory);
        }

        @Override
        public HashMap<Integer, Client_itf> getClients() {
            return connected;
        }

		@Override
		public boolean ping() throws RemoteException {
			return true;
		}

        @Override
        public LinkedList<Message> getChat() {
                return chatHistory.getMessages();
        }

}
