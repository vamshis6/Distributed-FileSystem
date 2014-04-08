package service.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import service.registry.RegistryServerInterface;
import service.server.FileServerInterface;


public class Client 
{

	public static Registry registery;
	public static String[] fileServers;
	public static String relative_filename;
	public static String filename;
	public static String hostname;
	public static int port;
	public static ArrayList<Integer> remChunks=new ArrayList<Integer>();
	public static boolean failed=false;
	public static int numThreads;
	public static long numberChunks;
	public static HashMap<Integer,Integer> lastChunkInfo=new HashMap<Integer,Integer>();
	private static ArrayList<FileServerInterface> remainingServers = null;
	 
	protected Client() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static synchronized void write_file(String filename,FileServerInterface obj)
	{
		try
		{
			File f=new File(filename);
			InputStream in=new FileInputStream(f);
			byte[] b=new byte[64*1024];
			int len=64*1024;
			int offset=0;
			int rt;
			System.out.println("writing 64 KB Chunk's to Server "+fileServers[0]+" ");
			System.out.println("Size of File :: "+f.length());
			while((rt=in.read(b,0,len))!=-1)
			{
				if(rt==(64*1024))
				{
					
					//System.out.println(rt);
					obj.FileWrite64K(relative_filename, offset,b);
					offset+=rt;
				}
				else
				{
					byte[] data_new=new byte[rt];
					in.read(data_new,0,rt);
					//System.out.println(rt);
					if(obj.FileWrite64K(relative_filename, offset,data_new)==0)
						System.out.println("File Size is less than 64Kb");
					offset+=rt;
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Excpetion in IO");
			e.printStackTrace();
		}
		catch(Exception e)
		{
			System.out.println("Excpetion in Writing File to Server");
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void Initialization()
	{
		try
		{
			int noOfServers=0;
			while(fileServers[noOfServers]!=null)
				noOfServers++;
			if(noOfServers>0)
			{
				String MasterServer=fileServers[0];
				FileServerInterface fs=(FileServerInterface)registery.lookup(MasterServer);
				write_file(filename,fs);
				File oup=new File("Output");
				if(!oup.exists())
				{
					oup.mkdir();
				}
				//f=new File("Output/"+relative_filename);
				numberChunks=fs.NumFileChunks(relative_filename);
				
				System.out.println("Number of Chunks "+numberChunks);
				System.out.println("Number of File Servers Available "+noOfServers);
				
				/*
				 *Giving time for the server to fail !! 
				 */
				System.out.println("Writing successful. Press Enter to continue reading (Time for a server or two to fail! :P)");
				System.in.read();
				
				if(numberChunks<=noOfServers)
					numThreads=(int)numberChunks;
				else
					numThreads=noOfServers;
				// Thread Functionality
				
				System.out.println(numThreads);
				
				ClientThreads[] threads=new ClientThreads[numThreads];
				System.out.println("Creating Threads to read data from servers");
				FileServerInterface fileSer;
				for(int i=0;i<numThreads;i++)
				{
					fileSer=(FileServerInterface)registery.lookup(fileServers[i]);
					threads[i]=new ClientThreads(fileSer,i,i);
				}
				
				for(int i = 0; i < numThreads; i++)
				{	
					threads[i].thread.join();
					
				}
				/*
				 * Code to Handle Failures
				 * 
				 */
				
				boolean isCompleted=false;
				
				int oNc=(int)numberChunks;
				int oNt=numThreads;
				System.out.println("CutOff:"+(oNc-oNt));
				while(isCompleted==false)
				{
					Iterator<Integer> iter=lastChunkInfo.keySet().iterator();
					HashMap<Integer,Integer> failedServers=new HashMap<Integer,Integer>();
					
					while(iter.hasNext())
					{
						int serverID=iter.next();
						int lastChunkWritten=lastChunkInfo.get(serverID);
						System.out.println("Server Id"+serverID+"Last Chunk Written"+lastChunkWritten);
						if(failed == false)
						{
							if(lastChunkWritten<(oNc-oNt))
								failedServers.put(serverID,lastChunkWritten);
						}else if(lastChunkWritten==-1)
						{
							failedServers.put(serverID,lastChunkWritten);
						}
					}
					
					
					
					if(failedServers.size()==0)
					{	
						isCompleted=true;
						break;
					}
					System.out.println("Num of File Servers Failed ::"+failedServers.size());
					lastChunkInfo.clear();
					
					failed=true;
					Iterator<Integer> it=failedServers.keySet().iterator();
					while(it.hasNext())
					{
						int serverId=it.next();
						int chunkNo=failedServers.get(serverId);
						if(chunkNo==-1)
						{	
							chunkNo=serverId;
							remChunks.add(chunkNo);
						}
						for(int i=(chunkNo+numThreads);i<numberChunks;i+=numThreads)
						{
							remChunks.add(i);
						}
					}
					int serRemain=noOfServers-failedServers.size();
					int chunksRem=remChunks.size();
					if(chunksRem<=serRemain)
						numThreads=chunksRem;
					else
						numThreads=serRemain;
					numberChunks=remChunks.size();
					
					int j=0;
					Collections.sort(remChunks);
					ClientThreads[] threads_f=new ClientThreads[numThreads];
					for(int i=0;i<noOfServers;i++)
					{
						if(failedServers.containsKey(i)==false)
						{
							System.out.println(fileServers[i]);
							fileSer=(FileServerInterface)registery.lookup(fileServers[i]);
							threads_f[j]=new ClientThreads(fileSer,j,i);
							j++;
						}
					}
					for(int i = 0; i < numThreads; i++)
					{	
						threads_f[i].thread.join();
					}
					failedServers.clear();
					remChunks.clear();
				}
				System.out.println("Reading Completed SuccesFully");
			
				
			}
			else
				System.out.println("File Servers are not available");
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
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
		
		if(args.length<3)
		{
			System.out.println("Invalid Arguments !");
		}
		else
		{
			filename=args[0];
			hostname=args[1];
			port=Integer.parseInt(args[2]);
			System.out.println("Hostname:: "+hostname+" Port::"+port);
			File f=new File(filename);
			relative_filename=f.getName();
			System.out.println("::: Client Started :::");
			try
			{
				registery=LocateRegistry.getRegistry(hostname,port);
				RegistryServerInterface ri=(RegistryServerInterface)registery.lookup("registryServer");
				fileServers=ri.GetFileServers();
				Initialization();
				
			}catch(Exception e)
			{
				System.out.println("Exception ");
				e.printStackTrace();
			}
			
		}
		
	}

}
