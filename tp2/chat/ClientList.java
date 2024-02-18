package chat;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ClientList {
    HashMap<Integer, Client_itf> clients;

    public ClientList() {
        clients = new HashMap<Integer, Client_itf>();
    }
    public ClientList(HashMap<Integer, Client_itf> c) {
        clients = c;
    }

    public void loadClientList(Server_itf s) throws RemoteException {
        clients = s.getClientList();
    }

    public HashMap<Integer, Client_itf> getClients() {
        return clients;
    }

    public void addClientToList(Client_itf c) throws RemoteException {
        clients.put(c.getId(), c);
    }
}
