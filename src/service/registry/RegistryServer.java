package service.registry;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RegistryServer extends UnicastRemoteObject implements RegistryServerInterface  {

	
	public static String[] fileServers=new String[100];
	public static int serverNumber=0;
	
	protected RegistryServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length<2)
		{
			System.out.println("Invalid Arguments ! <IP ADDRS OF REGISTRY> <PORT NO> ");
			System.exit(0);
		}
		String hostname=args[0];
		int port=Integer.parseInt(args[1]);
		
		System.out.println("::: Registery Server Started :::");
		System.out.println("-- Registery Server Registering With RMI SERVER -- ");
		
		try
		{
			
			RegistryServer regServer= new  RegistryServer();
			
			System.out.println("Registery Server Registerd at host "+hostname+" port "+port);
			Registry registry=LocateRegistry.createRegistry(port);
			registry.rebind("registryServer",regServer);
			
			System.out.println(" DONE WITH REGISTRATION ");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean RegisterServer(String name) 
	{
		// TODO Auto-generated method stub
		
		System.out.println("Called by FileServer "+name+" for Registration with Registery Server");
		fileServers[serverNumber]=name;
		serverNumber++;
		if(serverNumber==1)
			return true;
		else
			return false;
	}

	@Override
	public String[] GetFileServers() 
	{
		// TODO Auto-generated method stub
		return fileServers;
	}

}
