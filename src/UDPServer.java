package computernetwork;

import FileTransfer4390.FileTransfer;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {

    private DatagramSocket soc = null;
    private FileTransfer file_transfer = null;

    public UDPServer() {
    }

    public void Socket_Create_Listen() {
        
        try {
            soc = new DatagramSocket(3500);
            byte[] receivedContent = new byte[1024 * 50];

            while (true) {

                DatagramPacket receivedFile = new DatagramPacket(receivedContent, receivedContent.length);
                soc.receive(receivedFile);
                byte[] content = receivedFile.getData();
                ByteArrayInputStream byte_is = new ByteArrayInputStream(content);
                ObjectInputStream obj_is = new ObjectInputStream(byte_is);
                file_transfer = (FileTransfer) obj_is.readObject();

                if (file_transfer.getUpdate().equalsIgnoreCase("Error")) {
                    System.out.println("Error in transfering file content at UDPClient");
                    System.exit(0);
                }

                FileTransferring(); // writing the file to hard disk
                InetAddress ip_adrs = receivedFile.getAddress();    //library class DatagramPacket getAddress()
                int portNumber = receivedFile.getPort();            //library class DatagramPacket getPort()
                String reply = "Thank you for the message";
                byte[] replyBytea = reply.getBytes();
                DatagramPacket replyPacket = new DatagramPacket(replyBytea, replyBytea.length, ip_adrs, portNumber);
                soc.send(replyPacket);
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

        String outputFile = file_transfer.getLocation_B() + file_transfer.getName();

        if (!new File(file_transfer.getLocation_B()).exists()) {
            new File(file_transfer.getLocation_B()).mkdirs();
        }

        File dstFile = new File(outputFile);
        FileOutputStream file_os = null;

        try {
            file_os = new FileOutputStream(dstFile);
            file_os.write(file_transfer.getContent());
            file_os.flush();
            file_os.close();
            System.out.println("Output file : " + outputFile + " is successfully saved ");

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
