package computernetwork;

import computernetwork.UDPFileTransfer;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {

    private DatagramSocket soc = null;
    private UDPFileTransfer file_transfer = null;

    public UDPServer() {
    }

    public void Socket_Create_Listen() {

        try {
            
            soc = new DatagramSocket(3500);                                     //open socket, port#3500
            byte[] receivedContent = new byte[1024 * 50];                       //byte array size [50*1024]

            while (true) {

                DatagramPacket receivedFile = new DatagramPacket(receivedContent, receivedContent.length);
                soc.receive(receivedFile);
                byte[] content = receivedFile.getData();                        //getData gets content of receivedFile

                ByteArrayInputStream byte_is = new ByteArrayInputStream(content); //accepts byte array as a parameter
                ObjectInputStream obj_is = new ObjectInputStream(byte_is);      //deserializes primitive data & objects

                file_transfer = (UDPFileTransfer) obj_is.readObject();
                if (file_transfer.getUpdate().equalsIgnoreCase("Error")) {       //Error in File transferring 
                    System.out.println("Error in transfering file at UDPClient");
                    System.exit(0);
                }

                FileTransferring();

                InetAddress ip_adrs = receivedFile.getAddress();                //library class DatagramPacket getAddress() gets adrs of receivedFile
                int portNumber = receivedFile.getPort();                        //library class DatagramPacket getPort() gets port# of receivedFile

                String ack = "Acknowledged";                                    //Server sending acknowledgement
                byte[] ackBytes = ack.getBytes();
                DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, ip_adrs, portNumber);
                soc.send(ackPacket);

                Thread.sleep(3000);
                System.exit(0);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void FileTransferring() {

        String received_filename = file_transfer.getLocation_B() + file_transfer.getName();

        if (!new File(file_transfer.getLocation_B()).exists()) {
            new File(file_transfer.getLocation_B()).mkdirs();                   
        }

        File final_file = new File(received_filename);
        FileOutputStream file_os = null;                                        //writes data to File

        try {

            file_os = new FileOutputStream(final_file);
            file_os.write(file_transfer.getContent());                          //file transferring including its content
            file_os.flush();
            file_os.close();

            System.out.println("Final file : " + received_filename + " is created including its content");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.Socket_Create_Listen();
    }
}
