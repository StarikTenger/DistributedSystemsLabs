package chat;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

public class Server {
    public static void main(String [] args) {
        try {
            // Register the remote object in RMI registry with a given identifier
            Registry registry;
            if (args.length>0)
                registry= LocateRegistry.getRegistry(Integer.parseInt(args[0]));
            else
                registry = LocateRegistry.getRegistry();

            Server_itf serv = new ServerImpl();
            Server_itf serv_stub = (Server_itf) UnicastRemoteObject.exportObject(serv, 0);
            registry.bind("ServerService", serv_stub);

            System.out.println ("Server ready");

        } catch (Exception e) {
            System.err.println("Error on server: " + e) ;
            e.printStackTrace();
        }
    }
}
