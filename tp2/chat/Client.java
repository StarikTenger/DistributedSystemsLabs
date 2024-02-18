package chat;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Client {
    static Server_itf server;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(Integer.parseInt(args[0]));
            server = (Server_itf) registry.lookup("ServerService");
            Client_itf cl = new ClientItf();
            Server_itf serv_stub = (Server_itf) UnicastRemoteObject.exportObject(cl, 0);
            registry.bind("ClientService", serv_stub);
        } catch (Exception e) {
            System.err.println("Error on client: " + e) ;
            e.printStackTrace();
        }
    }
}
