package service.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import service.registry.RegistryServerInterface;

public class FileServer extends UnicastRemoteObject implements FileServerInterface {

	protected FileServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int PORT = 12345;	
	private static ServerSocket s;
	public static boolean master;
	
	
	
	
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
			
			
		
			boolean sleep=false;
			try
			{
				s=new ServerSocket(PORT,10,InetAddress.getLocalHost());
				
			}catch(UnknownHostException e)
			{
				System.out.println("Unknown Host Exception");
			}catch(IOException e)
			{
				System.out.println("Already a Instance is Running\n");
				sleep=true;
			}catch(Exception e)
			{
				System.out.println("Exception ");
				e.printStackTrace();
			}
			
			try
			{
				if(sleep)
					Thread.sleep(2000);
				RegistryServerInterface regisSer;
				
				System.out.println("::: File Server Started :::");
				Registry re=LocateRegistry.getRegistry(hostname,port);
				System.out.println("File Server Registerd at <RMI REGISTRY> host "+hostname+" port "+port);
				String name="Server_"+System.currentTimeMillis();
				regisSer=(RegistryServerInterface) re.lookup("registryServer");
				FileServer fs=new FileServer();
				re.rebind(name,fs);
				master=regisSer.RegisterServer(name);
				System.out.println("File Server"+name+" Registers");
			
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		
	}

	@Override
	public int FileWrite64K(String filename, long offset, byte[] data)
			throws IOException, RemoteException {
		// TODO Auto-generated method stub
		if(master==true)
		{
				try
				{
					if(data.length==(64*1024))
					{
						int index=(int) (offset/(64*1024));
					//String path=new String();
					if(offset==0)
					{
						File file = new File(filename.trim());
						if (!file.exists()) {
							if (file.mkdir()) {
								System.out.println("Directory is created!");
							} else {
								System.out.println("Failed to create directory!");
							}
						}
					}
					File f=new File(filename+"/"+filename+index);
					if(!f.exists())
					{
						if(!f.createNewFile())
							System.out.println("Error in Creating File");
					}
					
					OutputStream os=new FileOutputStream(f);
					OutputStreamWriter writer=new OutputStreamWriter(os);
					String br=new String(data);
					writer.write(br);
					writer.close();
				}else
				{
					System.out.println("Cant Write File");
				//	writer.close();
					return 0;
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			return 1;
			
		}else
		{
			System.out.println("Iam not allowed to WRITE");
			return 0;
		}
	}

	@Override
	public long NumFileChunks(String filename) throws IOException,
			RemoteException {
		try
		{
		// TODO Auto-generated method stub
			File f=new File(filename);
			System.out.println(filename);
			File[] files=f.listFiles();
			if(files==null)
				return 0;
			return files.length;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}	

	@Override
	public byte[] FileRead64K(String filename, long offset) throws IOException,
			RemoteException {
		// TODO Auto-generated method stub
		long chunk_num=offset/(64*1024);
		System.out.println(chunk_num);
		byte[] data=new byte[64*1024];
		try
		{
			File f=new File(filename+"/"+filename+chunk_num);
			InputStream ios=new FileInputStream(f);
			InputStreamReader isr=new InputStreamReader(ios);
			if(ios.read(data,0,65536) !=65536 )
			{	
				System.out.println("Error in Reading File in the Server Side");
				
			}
			ios.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return data;
	}
}


