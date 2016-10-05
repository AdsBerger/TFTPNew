import java.net.*;
import java.awt.List;
import java.io.IOException;
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

@SuppressWarnings("unused")
public class TFTPServer {
	
	private boolean writeReq,readReq;
	
	public static final byte[] readByte ={0,3,0,1};
	public static final byte[] writeByte={0,4,0,0};
	
	private DatagramSocket receivingSocket,sendingSocket;
	private DatagramPacket receivingPacket,sendingPacket;
	
	
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
					}
					else if(keyPress.equals("E")){
						System.out.println("SERVER EXITING");
						System.exit(0);
						
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
				
			}
			
		}
	}
	
	
	
	

}
