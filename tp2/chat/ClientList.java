package chat;

import java.util.ArrayList;
import java.util.LinkedList;

public class ClientList {
    ArrayList<Client> clients;

    public void ClientList() {
        clients = new ArrayList();
    }

    public void loadClientList(Server_itf s) {
        clients = s.getClientList();
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void addClientToList(Client c) {
        clients.add(c);
    }
}
