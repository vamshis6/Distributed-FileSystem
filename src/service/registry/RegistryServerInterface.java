package service.registry;

import java.io.IOException;
import java.rmi.*;

public interface RegistryServerInterface extends Remote
{

	public boolean RegisterServer(String name) throws IOException,RemoteException;
	public String[] GetFileServers() throws IOException,RemoteException;
	
}
