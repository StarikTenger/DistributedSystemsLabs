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

	AccountingImpl acc = new AccountingImpl();
	Accounting_itf acc_stub = (Accounting_itf) UnicastRemoteObject.exportObject(acc, 0);

	Hello h = (Hello) registry.lookup("HelloService");
	Registry_itf r = (Registry_itf) registry.lookup("RegistryService");
	Hello2 h2 = (Hello2) registry.lookup("Hello2Service");

	// Remote method invocation
	// String res = h.sayHello(itf_stub);
	// System.out.println(res);

	// Registration
	r.register(acc_stub);
	int my_id = acc.getId();
	System.out.println("My id is " + String.valueOf(my_id));

	for (int i = 0; i < 3; i++) {
		h2.sayHello(acc_stub, my_id);
	}

	} catch (Exception e)  {
//		System.err.println("Error on client: " + e);
		e.printStackTrace();
	}
  }
}
