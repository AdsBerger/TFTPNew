import java.net.*;
import java.awt.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.IOException;
import java.awt.*;
import javax.swing.*;
import java.lang.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;


public class TFTPServer {
	
	private boolean writeReq,readReq;
	private DatagramSocket receivingSocket;
	private DatagramPacket receivingPacket;
	
	
	public TFTPServer(){
		try {
			receivingSocket=new DatagramSocket(69);
		} catch (SocketException e) {
			System.out.println("Failure in creating receiving socket");
			e.printStackTrace();
			System.exit(1);
		}
		new Thread(){//each new server will create a new thread that listens for a exit key.
			
			public void run(){
				Scanner exit = new Scanner(System.in);
				String keyPress=null;
				System.out.println("TO EXIT SERVER PRESS E");
				
				while(true){
					keyPress=exit.next();
					if(keyPress.equals("e")){
						System.out.println("SERVER EXITING");
						System.exit(0);
						exit.close();
						
					}
					else if(keyPress.equals("E")){
						System.out.println("SERVER EXITING");
						System.exit(0);
						exit.close();
					}
				}
				
				
				
			}
		}.start();
	}

	
	public void receiveTFTPRequest(){
		int j;
		int i = 0;
		String fileName,mode;
		byte[] temp;
		readReq=false;
		writeReq=false;
		while(true){
			temp = new byte[100];
			receivingPacket= new DatagramPacket(temp,temp.length);
			System.out.println("Server is waiting for request packet ");
			
			try {
				receivingSocket.receive(receivingPacket);
			} catch (IOException e) {
				
				
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Request Packet received...");
			System.out.println("From host: "+ receivingPacket.getAddress());
			System.out.println("Port: " + receivingPacket.getPort());
			System.out.println("Length: "+receivingPacket.getLength());
			System.out.println("Contents: ");
			
			temp = receivingPacket.getData();
			for(Byte z :temp){
				System.out.print(z);
			}
			System.out.println(" ");
			String receivedPortBytes = new String(temp,0,receivingPacket.getLength());
			System.out.println(receivedPortBytes);
			
			if(temp[0]!=0){
				System.out.println("Packet is INVALID");
				System.exit(1);
			}
			else if(temp[1]==2){//write request
				writeReq=true;
				
			}
			else if(temp[1]==1){//read request
				readReq =true;
			}
			else{
				System.out.println("Packet is INVALID");
				System.exit(1);
			}
			
			int packetLen = receivingPacket.getLength();
			if(readReq||writeReq == true){//need to parse through bytes for 0, then everything before that is the filename
				for(i=2;i<packetLen;i++){
					if(temp[i]==0){
						break;//we've found the 0 byte and everything we've read so far has been the filename
					}
					if(i==packetLen){//no luck finding 0byte and we've reached the end of the data
						System.out.println("Couldnt find filename");
						System.exit(1);
					}
					
				}
				fileName = new String(temp,2,i-2);
				
			}
			if(readReq||writeReq == true){//check for type ex. OCTET
				for(j=i+1;j<packetLen;j++){
					if(temp[j]==0){
						break;
					}
					if(j==packetLen){
						System.out.println("Could not find mode");
						System.exit(1);
					}
				}
				
				
				mode = new String(temp,i,j-i-1);
				new clientConnectionThread(receivingPacket,readReq,writeReq,69).start();
			}
			
		}
	}
	
	


	class clientConnectionThread extends Thread{

		
		
		private DatagramSocket sendReceive;
		private String file_Name;
		private boolean read,write;
		private int sendBackPort;
		
		public clientConnectionThread(DatagramPacket dp,boolean read,boolean write,int sendBackPort){
			this.read = read;
			this.write =write;
			this.sendBackPort=sendBackPort;
			byte[] temp = dp.getData();
			
			String tempToString = new String(temp);
			
			
			try {
				this.sendReceive=new DatagramSocket();
				
			} catch (SocketException e) {
				System.out.println("Datagram Socket creation failure");
				e.printStackTrace();
				System.exit(1);
				
			}
			
			file_Name =tempToString.substring(2, temp.length-1);//temp.length-1 isnt the correct length it should stop -1 when it reaches a zero
																//that seperates the mode from the filename
		}
		
		public void run(){//spawns new thread
			
			
			if(read==true){
				InputStream in =null;
				File temp_file=new File(file_Name);
				if(temp_file.exists()){
					try {
						in =new FileInputStream(temp_file);
					} catch (FileNotFoundException e) {
						System.out.println("Failure in read file name");
						e.printStackTrace();
					}
					
				}
				else{//file doesn't exist, so can't do read
					System.out.println("File does not exist");
					System.exit(1);
				}
				ArrayList<Byte> temp;
				byte[] bits = new byte [4];
				byte[] data = new byte[512];
				
				bits[0]=0;
				bits[1]=3;
				bits[2]=0;
				bits[3]=1;
				
				try {
					while(in.read(data)!=-1){//readRequest
						int i=0;
						temp = new ArrayList<Byte>();
						while(i<=4){
						for(byte j: bits){
							temp.add(j);
							
						}
						}
						for(byte j:data){
							temp.add(j);
						}
						Byte[] temp2 = temp.toArray(new Byte[516]);
						
						//test purposes only
						System.out.println("");
						System.out.println("Printing contents of read request: ");
						for(byte j:temp2){
							System.out.println(j);
						}
						System.out.println("");//new line
						sendDataPack(temp2);
						receiveAck();
					}//end while loop
						
					
				} catch (IOException e) {
					System.out.println("Data read failure");
					e.printStackTrace();
					System.exit(1);
				}
				try {
					in.close();
				} catch (IOException e) {
					System.out.println("Input Stream closing failed");
					e.printStackTrace();
				}
				System.out.println("FILE TRANSFERED SUCCESSFULLY");
				this.sendReceive.close();
				
				
			}//end read==true
			
			else if(write==true){//write request
				
				
			}
			
		}//end of run();
						
		private byte [] receiveDataPack(){//returns byte array containing data from packet
			byte[] temp = new byte[516];
			DatagramPacket receivingPacket = null;
			
			try {
				receivingPacket= new DatagramPacket(temp,temp.length,InetAddress.getLocalHost(),sendBackPort);
			} catch (UnknownHostException e) {
				System.out.println("Receiving packet creation failed");
				e.printStackTrace();
			}
			try {
				sendReceive.receive(receivingPacket);
			} catch (IOException e) {
				System.out.println("Receiving Packet failure");
				e.printStackTrace();
				System.exit(1);
			}
			temp = receivingPacket.getData();
			
			System.out.println("Received data packet containing: ");
			for(byte j:temp){
				System.out.print(j);
			}
			System.out.println(" ");
			
			return temp;
			
		}//end receiveDataPack
		
		private byte[] receiveAck(){
			byte[] temp = new byte[4];
			DatagramPacket receivingPacket=null;
			
			
			try {
				receivingPacket=new DatagramPacket(temp,temp.length,InetAddress.getLocalHost(),sendBackPort);
			} catch (UnknownHostException e) {
				System.out.println("Receiving packet creation failure");
				e.printStackTrace();
				System.exit(1);
			}
			
			try {
				sendReceive.receive(receivingPacket);
			} catch (IOException e) {
				System.out.println("Failed to receive packet");
				e.printStackTrace();
				System.exit(1);
			}
			temp=receivingPacket.getData();
			
			System.out.println("Received Ackowledgment containing: ");
			for(byte j : temp){
				System.out.print(j);
			}
			
			
			
			return temp;
		}
	
		private void sendDataPack(Byte[] data){
			
			byte[] temp = new byte[516];
			int i=0;
			
			for(i=0;i<data.length;i++){
				if(data[i] !=null);
				temp[i]=data[i].byteValue();
			}
			DatagramPacket sendingPacket = null;
			
			try {
				sendingPacket = new DatagramPacket(temp,temp.length,InetAddress.getLocalHost(),this.sendBackPort);
			} catch (UnknownHostException e) {
				System.out.println("Failure in creating datagramPacket");				
				e.printStackTrace();
			}
			try {
				System.out.println("Sending packet from:"+InetAddress.getLocalHost()+" to port"+this.sendBackPort+"Containing: ");
			} catch (UnknownHostException e) {
				System.out.println("Failure in locating locaHost address");
				e.printStackTrace();
				System.exit(1);
			}
			for(byte j:temp){
				System.out.print(j);
				
			}
			try {
				sendReceive.send(sendingPacket);
			} catch (IOException e) {
				System.out.println("Could not send packet");
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("Packet was sucesffully sent to port: "+this.sendBackPort);
			
			
				
			}//end sendDataPacket
	
	}//end thread class




public static void main(String args[]){
	
	TFTPServer s = new TFTPServer();
	s.receiveTFTPRequest();
}
}

