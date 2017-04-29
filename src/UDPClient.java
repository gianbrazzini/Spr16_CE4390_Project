package computernetwork;

import FileTransfer4390.FileTransfer;
import java.io.*;
import java.net.*;

public class UDPClient {

    private DatagramSocket soc = null;
    private FileTransfer transfer = null;
    private String A_directory = "C:\\Users\\Maisha\\Desktop\\A\\fruits.txt";
    private String B_directory = "C:\\Users\\Maisha\\Desktop\\B\\";
    private String host = "localHost";

    public UDPClient() {
    }

    public void ConnectionSetup() {

        try {

            soc = new DatagramSocket();
            InetAddress ip_adrs = InetAddress.getByName(host);
            byte[] receivedContent = new byte[1024];
            transfer = getFileTransfer();
            ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
            ObjectOutputStream obj_os = new ObjectOutputStream(byte_os);
            obj_os.writeObject(transfer);
            byte[] content = byte_os.toByteArray();
            DatagramPacket sendFile = new DatagramPacket(content, content.length, ip_adrs, 3500);
            soc.send(sendFile);
            System.out.println("File has been transfered from UDPClient");
            DatagramPacket receivedFile = new DatagramPacket(receivedContent, receivedContent.length);
            soc.receive(receivedFile);
            String reply = new String(receivedFile.getData());       //library class DatagramPacket getData()
            System.out.println("UDPServer: " + reply);
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

    public FileTransfer getFileTransfer() {

        FileTransfer file_transfer = new FileTransfer();
        String name = A_directory.substring(A_directory.lastIndexOf("\\") + 1, A_directory.length());
        String location = A_directory.substring(0, A_directory.lastIndexOf("\\") + 1);
        file_transfer.setLocation_B(B_directory);
        file_transfer.setName(name);
        file_transfer.setLocation_A(A_directory);
        File file = new File(A_directory);

        if (file.isFile()) {
            try {
                DataInputStream data_is = new DataInputStream(new FileInputStream(file));
                long length = (int) file.length();
                byte[] fileBytes = new byte[(int) length];
                int read = 0;
                int numRead = 0;

                while (read < fileBytes.length && (numRead = data_is.read(fileBytes, read, fileBytes.length - read)) >= 0) {
                    read = read + numRead;
                }

                file_transfer.setSize(length);
                file_transfer.setContent(fileBytes);
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
        UDPClient client = new UDPClient();
        client.ConnectionSetup();
    }
}
