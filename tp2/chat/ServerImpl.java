package chat;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;

public class ServerImpl  implements Server_itf {
        private Integer count;
        ChatHistory chatHistory;
        ClientList clientList;


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
        public void connect(Client_itf itf) throws RemoteException {
                clientList.addClientToList(itf);
        }

        @Override
        public void disconnect(Integer id) {

        }

        @Override
        public void sendMessage(String s) {

        }

        @Override
        public void resMessage(Message m) {

        }

        @Override
        public void showHistory() throws RemoteException {

        }

        @Override
        public HashMap<Integer, Client_itf> getClients() {
                return clientList.getClients();
        }

        @Override
        public LinkedList<Message> getChat() {
                return chatHistory.getMessages();
        }

        @Override
        public int genId() {
            return count++;
        }

        @Override
        public HashMap<Integer, Client_itf> getClientList() {
            return null;
        }

}
