import java.rmi.*;
import java.util.*;

public  class Hello2Impl implements Hello2 {
	private RegistryImpl reg;
	
 
	public Hello2Impl(RegistryImpl r) {
		reg = r;
	}

	public String sayHello(Accounting_itf acc, int id) throws RemoteException {
		if (reg.user_exists(id)) {
			reg.add_count(id);
			return "Hello, user number " + String.valueOf(id);
		}
		return "Cannot find user with id" + String.valueOf(id);
	}
}

