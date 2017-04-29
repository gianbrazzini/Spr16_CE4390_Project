
import java.io.*;
import java.net.*;

public class UDPClient {

    private static DatagramSocket soc = null;
    private static UDPFileTransfer transfer = null;
    private static String A_directory = "C:\\Users\\Maisha\\Desktop\\UDP\\A\\fruits.txt";
    private static String B_directory = "C:\\Users\\Maisha\\Desktop\\UDP\\B\\";
    private static String host = "localHost";

    public static UDPFileTransfer getFileTransfer() {

        UDPFileTransfer file_transfer = new UDPFileTransfer();
        String name = A_directory.substring(A_directory.lastIndexOf("\\") + 1, A_directory.length()); // filename from the directory
        String location = A_directory.substring(0, A_directory.lastIndexOf("\\") + 1);                //location from the directory
       
        file_transfer.setLocation_B(B_directory);
        file_transfer.setName(name);
        file_transfer.setLocation_A(A_directory);
        File file = new File(A_directory);

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
                e.printStackTrace();
                file_transfer.setUpdate("Error");
            }
        } else {
            System.out.println("Error in finding the file");
            file_transfer.setUpdate("Error");
        }
        return file_transfer;
    }

    public static void main(String[] args) {
    	try {

            soc = new DatagramSocket();                                         //open socket
            InetAddress ip_adrs = InetAddress.getByName(host);                  //host ip adrs
            byte[] receivedContent = new byte[1024];
            transfer = getFileTransfer();
            
            ByteArrayOutputStream byte_os = new ByteArrayOutputStream();        //creates a ByteArrayOutputStream buffer of 32 byte.
            ObjectOutputStream obj_os = new ObjectOutputStream(byte_os);        //writes primitive data types & graphs of Java objects
            obj_os.writeObject(transfer);
            
            byte[] content = byte_os.toByteArray();
            DatagramPacket sendFile = new DatagramPacket(content, content.length, ip_adrs, 3500);
            soc.send(sendFile);                                                 //Sending file
            System.out.println("File has been sent from UDPClient");            //file sent output
            
            DatagramPacket receivedFile = new DatagramPacket(receivedContent, receivedContent.length); //receivedFile for Server
            soc.receive(receivedFile);
            String ack = new String(receivedFile.getData());                     //library class DatagramPacket getData()
            System.out.println("UDPServer " + ack);
            
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
}
