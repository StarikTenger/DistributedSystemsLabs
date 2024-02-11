// Client handler of number messages

import java.rmi.*;

public  class AccountingImpl implements Accounting_itf {
	private int id;

	public void numberOfCalls(int number) throws RemoteException {
		System.out.println(number);
	}

	public void setId(int number) throws RemoteException {
		id = number;
	}

	public int getId() throws RemoteException {
		return id;
	}
}

