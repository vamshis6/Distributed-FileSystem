package service.client;

import java.io.RandomAccessFile;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import service.server.FileServerInterface;

public class ClientThreads extends Thread {
	
	private FileServerInterface fs = null;
	private RandomAccessFile rs=null;
	Thread thread;
	String filename="Output/"+Client.relative_filename;
	private long numChunks=Client.numberChunks;
	private int numThreads=Client.numThreads;
	private int id;
	private int server_id;
	private long lastChunkread=-1;
	
	public ClientThreads(FileServerInterface fileSer, int i,int ser_id) 
	{
		// TODO Auto-generated constructor stub
		this.fs=fileSer;
		this.id=i;
		this.server_id=ser_id;
		
		Client.lastChunkInfo.put(ser_id,-1);
		thread = new Thread(this, ""+i);
		thread.start();
	}
	
	public void run() {
		// TODO Auto-generated method stub
		if(!Client.failed)
		{
			System.out.println("Thread "+this.id+"Reading Data From Server"+this.server_id);
			try 
			{
				rs=new RandomAccessFile(this.filename, "rw");
				long offset;
				for(int i=this.id;i<numChunks;i+=numThreads)
				{
					offset=(long)(i*65536);	
					byte[] b=new byte[65536];
					b=fs.FileRead64K(Client.relative_filename,offset);
					rs.seek(offset);
					rs.writeBytes(new String(b));
					if(Client.lastChunkInfo.containsKey(this.id))
						Client.lastChunkInfo.put(this.id,i);
				}
				} catch (AccessException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (Exception e)
			{
				e.printStackTrace();
			}
		}/*
		Code to Handle Failures
		*/
		else
		{
			//String name=this.getName();
			System.out.println("Thread "+this.id+"Reading Data From Server"+this.server_id);
			try 
			{
				rs=new RandomAccessFile(this.filename, "rw");
				long offset;
				for(int i=this.id;i<Client.remChunks.size();i+=numThreads)
				{
					offset=(long)(Client.remChunks.get(i)*65536);	
					byte[] b=new byte[65536];
					b=fs.FileRead64K(Client.relative_filename,offset);
					rs.seek(offset);
					rs.writeBytes(new String(b));
					if(Client.lastChunkInfo.containsKey(this.server_id))
						Client.lastChunkInfo.put(this.server_id,Client.remChunks.get(i));
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
		}
	}
}


