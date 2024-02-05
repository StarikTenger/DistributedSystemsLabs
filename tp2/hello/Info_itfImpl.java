
import java.rmi.*;

public  class Info_itfImpl implements Info_itf {

	private String message;
 
	public Info_itfImpl(String s) {
		message = s ;
	}

	public String getName() throws RemoteException {
		return message;
	}
}

