import java.rmi.*;

public interface Accounting_itf extends Remote {
    public void numberOfCalls(int number) throws RemoteException;
    public void setId(int number) throws RemoteException;
}