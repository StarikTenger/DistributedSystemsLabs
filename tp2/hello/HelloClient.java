import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;

public class HelloClient {
  public static void main(String [] args) {
	
	try {
	  if (args.length < 3) {
	   System.out.println("Usage: java HelloClient <rmiregistry host> <rmiregistry port> <client name>");
	   return;}

	String host = args[0];
	int port = Integer.parseInt(args[1]);

	Registry registry = LocateRegistry.getRegistry(host, port);

	Info_itfImpl itf = new Info_itfImpl (args[2]);
	Info_itf itf_stub = (Info_itf) UnicastRemoteObject.exportObject(itf, 0);
	registry.bind("Info_itf", itf_stub);

	Hello h = (Hello) registry.lookup("HelloService");

	// Remote method invocation
	String res = h.sayHello(itf_stub);
	System.out.println(res);

	} catch (Exception e)  {
//		System.err.println("Error on client: " + e);
		e.printStackTrace();
	}
  }
}
