
import java.io.*;
import java.net.*;

public class UDPClient {

	private static Logger logs = null;
    private DatagramSocket soc = null;
    private UDPFileTransfer transfer = null;
    private String host = "localHost";

    public static void send(File transferFile, Logger _logs) {
    	logs = _logs;
    	UDPClient client = new UDPClient();
        client.ConnectionSetup(transferFile);
    }

    public void ConnectionSetup(File transferFile) {

        try {

            soc = new DatagramSocket();                                         //open socket
            InetAddress ip_adrs = InetAddress.getByName(host);                  //host ip adrs
            byte[] receivedContent = new byte[1024];
            transfer = getFileTransfer(transferFile);
            
            ByteArrayOutputStream byte_os = new ByteArrayOutputStream();        //creates a ByteArrayOutputStream buffer of 32 byte.
            ObjectOutputStream obj_os = new ObjectOutputStream(byte_os);        //writes primitive data types & graphs of Java objects
            obj_os.writeObject(transfer);
            
            byte[] content = byte_os.toByteArray();
            DatagramPacket sendFile = new DatagramPacket(content, content.length, ip_adrs, 3500);
            soc.send(sendFile);       
        	logs.log("Sending file through UDP...");        
        	
        	//file sent output
            DatagramPacket receivedFile = new DatagramPacket(receivedContent, receivedContent.length); //receivedFile for Server
            soc.receive(receivedFile);
            String ack = new String(receivedFile.getData());                     //library class DatagramPacket getData()
            logs.log("UDPServer " + ack);
            
            Thread.sleep(2000);
            System.exit(0);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public UDPFileTransfer getFileTransfer(File thisFile) {

        UDPFileTransfer file_transfer = new UDPFileTransfer();
        String name = thisFile.getName(); 		// filename from the directory
        String location = thisFile.getPath(); 	// location from the directory

        file_transfer.setLocation_A(location);
        file_transfer.setLocation_B(location);
        file_transfer.setName(name);
        File file = thisFile;

        if (file.isFile()) {
            try {
                DataInputStream data_is = new DataInputStream(new FileInputStream(file)); //gets input bytes from file
                long length = (int) file.length();                              //the entire file length
                byte[] contentBytes = new byte[(int) length];
                
                int readSoFar = 0;
                int readingLeft = 0;

                //read func reads upto 'length' number of bytes starting from readSoFar from the input stream into an array. 
                //it returns the total number of bytes read. If it is the end of the file, -1 will be returned.
                            
                while (readSoFar < contentBytes.length                          
                        && (readingLeft = data_is.read(contentBytes, readSoFar, contentBytes.length - readSoFar)) >= 0)
                {
                    readSoFar = readSoFar + readingLeft;
                }

                file_transfer.setSize(length);
                file_transfer.setContent(contentBytes);
                file_transfer.setUpdate("Success");

            } catch (Exception e) {
            	logs.error("Could not create packet. Reason=" + e.toString());
                e.printStackTrace();
                file_transfer.setUpdate("Error");
            }
        } else {
            logs.error("File could not be found.");
            file_transfer.setUpdate("Error");
        }
        return file_transfer;
    }
}