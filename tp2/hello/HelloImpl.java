
import java.rmi.*;

public  class HelloImpl implements Hello {

	private String message;
//	private Info_itf client;
 
	public HelloImpl(String s) {
		message = s ;
	}

	public String sayHello(Info_itf c) throws RemoteException {
//		String client = c.getName();

		return "Name of the client: " + c.getName();
	}
}

