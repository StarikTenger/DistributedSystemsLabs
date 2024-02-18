package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Client {
    static Server_itf server;
	static Integer id;

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
			String filename = args[3];

			Registry registry = LocateRegistry.getRegistry(host, port);
            server = (Server_itf) registry.lookup("ServerService");

            ClientImpl itf = new ClientImpl ();
			Client_itf itf_stub = (Client_itf) UnicastRemoteObject.exportObject(itf, 0);

			// Getting the id
			Optional<Integer> id_opt = loadId(filename);
			if (id_opt.isPresent()) {
				id = id_opt.get();
			} else {
				server.genId();
				saveId(id, filename);
			}

			// Connection
			server.connect(id, itf_stub);
			HashMap<Integer, Client_itf> clients = server.getClientList();
			for (Map.Entry<Integer, Client_itf> entry : clients.entrySet()) {
				entry.getValue().connect(id, itf_stub);
				System.out.println(entry.getKey() + "/" + entry.getValue());
			}



        

        } catch (Exception e) {
            System.err.println("Error on client: " + e) ;
            e.printStackTrace();
        }
    }
}
