package chat;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;

public class Server {
    public static void  main(String [] args) {
        try {
            // Register the remote object in RMI registry with a given identifier
            Registry registry = null;
            if (args.length>0)
                registry= LocateRegistry.getRegistry(Integer.parseInt(args[0]));
            else
                registry = LocateRegistry.getRegistry();

            Server_itf serv = new ServerItf();
            Server_itf serv_stub = (Server_itf) UnicastRemoteObject.exportObject(serv, 0);

            registry.bind("ServerService", serv_stub);

            System.out.println ("Server ready");

        } catch (Exception e) {
            System.err.println("Error on server: " + e) ;
            e.printStackTrace();
        }
    }
}
