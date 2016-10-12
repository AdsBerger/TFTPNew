class TFTPTransferHandler implements Runnable {
	/*we could modify the server but that involves a really complicated constructor for the server
	This 'server' does not need to be killable - it will die when the transfer is complete.
	Send this a packet in the constructor so it knows where to send back the data/ack packets */

	public TFTPTransferHandler(DatagramPacket clientRequestPacket){
		
	}

}