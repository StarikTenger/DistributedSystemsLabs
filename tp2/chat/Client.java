package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
    static Server_itf server;
    static Boolean isConnected;

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Usage: java Client <rmiregistry host>");
                return;}
            Registry registry = LocateRegistry.getRegistry(Integer.parseInt(args[0]));
            server = (Server_itf) registry.lookup("ServerService");

            isConnected = false;

            System.out.println("Welcome to chat!");
            System.out.println("Commands:");
            System.out.println("c - connect");
            System.out.println("w - write a message");
            System.out.println("d - disconnect");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            String command = reader.readLine();
            Client_itf clientImpl = null;

            switch (command) {
                case "c":
                    if (!isConnected) {
                        HashMap<Integer, Client_itf> clients = server.getClientList();
                        LinkedList<Message> chat = server.getChat();
                        clientImpl = new ClientImpl(clients, chat);
                        Client_itf serv_stub = (Client_itf) UnicastRemoteObject.exportObject(clientImpl, 0);
                        registry.bind("ClientService", serv_stub);
                        server.connect(serv_stub);
                        isConnected = true;
                        System.out.println("Connected!");
                    } else {
                        System.out.println("Already connected");
                    }
                    break;
                case "w":
                    if (isConnected){
                        System.out.println("Write a message:");
                        Scanner in = new Scanner(System.in);
                        String mes = in.nextLine();
                        System.out.println("Message is" + mes);
                        clientImpl.sendMessage(mes);
                    } else {
                        System.out.println("You aren't connected");
                    }
                    break;
                case "d":
                    break;
                default:
                    System.out.println("idk this command");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error on client: " + e) ;
            e.printStackTrace();
        }
    }
}
