package chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ClientList {
    HashMap<Integer, Client_itf> clients;

    public void ClientList() {
        clients = new HashMap<Integer, Client_itf>();
    }

    public void loadClientList(Server_itf s) {
        clients = s.getClientList();
    }

    public HashMap<Integer, Client_itf> getClients() {
        return clients;
    }

    public void addClientToList(ClientImpl c) {
        clients.put(c.id, c);
    }
}
