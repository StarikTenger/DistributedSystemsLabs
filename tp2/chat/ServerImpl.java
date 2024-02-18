package chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ServerImpl  implements Server_itf {

        public void connect(Integer id, Client_itf itf) {

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
        public int genId() {
            return 0;
        }

        @Override
        public HashMap<Integer, Client_itf> getClientList() {
            return null;
        }

        @Override
        public LinkedList getChatHistory() {
            return null;
        }
}
