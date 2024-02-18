package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


public class Client {
    static Server_itf server;
	static Integer id;
	static ClientImpl client;

	public static Optional<Integer> loadId(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line != null) {
                int id = Integer.parseInt(line.trim());
                return Optional.of(id);
            } else {
                System.out.println("File is empty: " + filename);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing id from file: " + filename);
        }

        return Optional.empty();
    }

	public static void saveId(int id, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(Integer.toString(id));
            System.out.println("Id saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving id to file: " + filename);
        }
    }

    public static void main(String[] args) {
        try {
			if (args.length < 3) {
				System.out.println("Usage: java Client <rmiregistry host> <rmiregistry port> <id file>");
				return;
			}

			String host = args[0];
			int port = Integer.parseInt(args[1]);
			String filename = args[2];

			Registry registry = LocateRegistry.getRegistry(host, port);
            server = (Server_itf) registry.lookup("ServerService");

            client = new ClientImpl ();
			Client_itf itf_stub = (Client_itf) UnicastRemoteObject.exportObject(client, 0);

			// Getting the id
			Optional<Integer> id_opt = loadId(filename);
			if (id_opt.isPresent()) {
				id = id_opt.get();
			} else {
				id = server.genId();
				saveId(id, filename);
			}
			System.out.println(id);

			// Connection
			server.connect(id, itf_stub);
			client.connected = server.getClientList();
			for (Map.Entry<Integer, Client_itf> entry : client.connected.entrySet()) {
				entry.getValue().connect(id, itf_stub);
				System.out.println(entry.getKey() + "/" + entry.getValue());
			}

			// Getting history
			client.chatHistory = new ChatHistory(server.getChat());
			client.showHistory();
			

			Scanner scanner = new Scanner(System.in);

			while (true) {
				System.out.print("Enter a message: ");
				String message = scanner.nextLine();


				if(!serverAlive()) {
					System.err.println("Server is offline");
					return;
				}

				if (message.startsWith("#")) {
					processCommand(message);
				} else {
					sendAll(message);
				}
			}
        

        } catch (Exception e) {
            System.err.println("Error on client: " + e) ;
            e.printStackTrace();
			return;
        }
    }

	private static void sendAll(String message) throws RemoteException {
		server.sendMessage(message);
        for (Map.Entry<Integer, Client_itf> entry : client.connected.entrySet()) {
			entry.getValue().sendMessage(message);
		}
    }

    private static void processCommand(String command) {
        switch (command) {
            case "#quit":
                quit();
                break;
            case "#help":
                printHelp();
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    private static void quit() {
        System.out.println("Quitting the program.");
        System.exit(0);
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("#quit - Quit the program");
        System.out.println("#help - Display help information");
    }

	private static boolean serverAlive() {
		try {
			return server.ping();
		} catch (Exception e) {
            return false;
        }
	}
}
