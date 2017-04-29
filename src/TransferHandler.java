/**
 * @author Gian Brazzini
 *
 */

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;


public class TransferHandler {
	
	private static final int PACKET_SIZE = 2048;
	private static final String ENCODING = "UTF-8";
	
	private Logger log;
	private Random rand = new Random();
	private static int port;
	private static Socket socket;
	byte[] fileData = null;
	long fileLength, check, index = 0, lastIndex;
	String fileName = "";
	OutputStream out = null;
	InputStream in = null;
	
	public TransferHandler(Logger _log, int _port) {
		log = _log;
		port = _port;
	}
	
	/**
	 * 
	 * @param address
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void receiveFile(String address) throws UnknownHostException, IOException
	{	
		log.log("\nAttemting to connect to "+ address +":"+ port + "...");
		socket = new Socket(address, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		
        boolean headerReceived = false;
        byte response[] = new byte[PACKET_SIZE],
    		 request[];
        
        long req_check, req_index, req_lastIndex, req_status;
        int ack = 0;
        byte[] req_data;
        while (true)
        {
        	if (!headerReceived)
        	{
    		    in.read(response, 0, PACKET_SIZE);
        		fileName = stringFromResponse(response, 0, 16);
        		fileLength = longFromResponse(response, 16);
        		check = longFromResponse(response, 2039);
        		
        		request = new byte[PACKET_SIZE];
        		request = populatePacket(request, check + fileLength, 0);
        		out.write(request, 0, PACKET_SIZE);
        		
        		log.log("Success!\nStarting download... file=" + fileName + 
        				", size=" + fileSize(fileLength) + "\n");
        		lastIndex = (int) ((fileLength/1024) + 1);
        		index = 0;
        		fileData = new byte[(int)fileLength];
        		headerReceived = true;
        	}
        	else
        	{
    		    in.read(response, 0, PACKET_SIZE);
        		req_check = longFromResponse(response, 0);
        		req_index = longFromResponse(response, 8);
        		req_lastIndex = longFromResponse(response, 16);
        		req_status = longFromResponse(response, 24);
        		req_data = getFileData(response);
        		
        		request = new byte[PACKET_SIZE];
    		    if (index == lastIndex && req_status == 0)
        		{
    		    	//System.out.println("RECEIVED EXIT PACKET");
    		    	request = populatePacket(request, 1, 0);
    		    	out.write(request, 0, PACKET_SIZE);
        			break;
        		}
    		    else if (req_status>0 && req_check == index + check)
    		    {	
    		    	//System.out.println("RECEIVED DATA PACKET");
    		    	fileData = appendFileData(fileData, req_data, index);
    		    	request = populatePacket(request, 1, 0);
    		    	request = populatePacket(request, index, 8);
    		    	request = populatePacket(request, req_check, 16);
    		    	out.write(request, 0, PACKET_SIZE);
    		    	System.out.println("success. index: " + index);
    		    	index++;
    		    }
    		    else
    		    {
    		    	System.out.print("status: " + req_status);
    		    	System.out.print(", req_index: " + req_index);
    		    	System.out.print(", index: " + index + ", ");
    		    	System.out.println();
    		    	
    		    	if (req_status == -1)
    		    		log.error("There was a problem sending the file.");
    		    	
    		    	//System.out.println("SOMETHING WENT WRONG RESEND");
    		    	request = populatePacket(request, 0, 0);
    		    	request = populatePacket(request, index, 8);
    		    	request = populatePacket(request, req_check, 16);
    		    	out.write(request, 0, PACKET_SIZE);
    		    }
        	}
        }
        int val = 0;
        System.out.println("data len: " + fileData.length);
        for (int i=0; i<fileData.length; i++)
        {
        	val += (int)fileData[i];
        }
        System.out.println(val);
        downloadFile(fileName, fileData, "received");
        log.log("File successfully downloaded.\n\n");
        close();
	}
	
	/**
	 * 
	 * @param file
	 */
	public void sendFile(File file) throws UnknownHostException, IOException
	{
		InetAddress iAddress = InetAddress.getLocalHost();
		String currentIp = iAddress.getHostAddress();
        
        fileData = fileToBytes(file);
		fileLength = fileData.length;
		check = Math.abs(rand.nextLong()%15 + 1);
		fileName = file.getName();
		
		ServerSocket serverSocket = new ServerSocket(port);
		log.log("\nWaiting for connection at "+ currentIp +":"+ port + "\n");
        socket = serverSocket.accept();
        
        log.log("Connection established.\n");
        out = socket.getOutputStream();
        in = socket.getInputStream();
        
        boolean headerSent = false;
		lastIndex = (fileLength / 1024) + 1;
        byte inputData[] = new byte[PACKET_SIZE],
    		 outputData[];
        int tries = 0;
        long response_status, response_index, response_check;
        while (true) 
        { 	
        	if (!headerSent) {
        		outputData = new byte[PACKET_SIZE];
        		outputData = populatePacket(outputData, fileName, 0, 15);
        		outputData = populatePacket(outputData, fileLength, 16);
        		outputData = populatePacket(outputData, check, 2039);
        		out.write(outputData, 0, PACKET_SIZE);
        		
        		in.read(inputData, 0, PACKET_SIZE);
        		if (isLong(inputData, 0, check+fileLength))
        		{
        			log.log("Uploading file...\n");
        			headerSent = true;
        		}
        		else
        		{
        			log.log("Failure trying to connect, retrying...");
        			out.write(outputData, 0, PACKET_SIZE);
        		}
        	}
        	//else if (index <= lastIndex)
        	else if (index < lastIndex)
        	{
        		//System.out.println(index + "<" + lastIndex);
        		//System.out.println("SENDING DATA; INDEX: " + index + ", SIZE: " + lastIndex + ", CHECK:" + check);
        		outputData = new byte[PACKET_SIZE];
        		outputData = populatePacket(outputData, check+index, 0);
        		outputData = populatePacket(outputData, index, 8);
        		outputData = populatePacket(outputData, lastIndex, 16);
        		outputData = populatePacket(outputData, 1, 24);
        		
        		outputData = populatePacket(outputData, fileData, index);
        		
				out.write(outputData, 0, PACKET_SIZE);
				
				in.read(inputData, 0, PACKET_SIZE);
				response_status = longFromResponse(inputData, 0);
				response_index = longFromResponse(inputData, 8);
				response_check = longFromResponse(inputData, 16);
				if (response_status == 1)
				{
					System.out.println("Successful packet. index: " + index);
					tries = 0;
					index++;
				}
				else if (tries < 3)
				{
					System.out.print("index: "+ index +", tries: " + tries + ", ");
					System.out.println("Resending packet.");
					
					if (response_check == check+index) {
						index = response_index;
					}
					
					tries++;
				}
				else 
				{
					outputData = new byte[2048];
	        		outputData = populatePacket(outputData, -1, 0);
					out.write(outputData, 0, PACKET_SIZE);
					
					log.error("Hit max number of tries.");
				}
        	}
        	else
        	{
        		System.out.println("SENDING EXIT PACKET");
        		outputData = new byte[PACKET_SIZE];
        		outputData = populatePacket(outputData, check+index, 0);
        		outputData = populatePacket(outputData, index, 8);
        		outputData = populatePacket(outputData, lastIndex, 16);
        		outputData = populatePacket(outputData, 0, 24);
        		
				out.write(outputData, 0, PACKET_SIZE);
				
				in.read(inputData, 0, PACKET_SIZE);
				if (!isLong(inputData, 0, 1))
					log.log("There was an error while sending this file. Double check the download.");
				
				break;
        	}
        } 
        int val = 0;
        System.out.println("data len: " + fileData.length);
        for (int i=0; i<fileData.length; i++)
        {
        	val += (int)fileData[i];
        }
        System.out.println(val);
		downloadFile(fileName, fileData, "preSend");
        log.log("File sent successfully!\n");
        
        close();
        serverSocket.close(); 
   
    }
	
	private byte[] populatePacket(byte[] packet, String str, int from, int to) throws UnsupportedEncodingException {
		int length = to - from;
		byte[] byteData = str.getBytes(ENCODING);
		for (int i=0; i<length && i<str.length(); i++)
		{
			packet[from+i] = byteData[i];
		}
		return packet;
	}
	
	private byte[] populatePacket(byte[] packet, long data, int index) {
		byte[] byteData = longToByte(data);
		for (int i=0; i<8; i++)
		{
			packet[index+i] = byteData[i];
		}
		return packet;
	}
	
	private byte[] populatePacket(byte[] packet, byte[] data, int from, int to) {
		int length = to - from;
		for(int i=0; i< data.length && i<length; i++) {
			packet[from+i] = data[i];
		}
		return packet;
	}
	
	private byte[] populatePacket(byte[] packet, byte[] data, long index) {
		long len = data.length;
		int from = (int)(index)*1024,
			to = Math.min((int)((index+1)*1024)-1, (int)len);
		//System.out.println("index: " + index + " from: " + from + " to: " + to + " len: " + len);
		byte[] subData = Arrays.copyOfRange(data, from, to);
		packet = populatePacket(packet, subData, 1023, 2047);
		return packet;
	}
	
	private void downloadFile(String name, byte[] data, String pre) {
		try {
			
			if (!pre.equals(""))
				name = pre + "-" + name;
        	
        	File file = new File(name);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileData);
            fos.flush();
            fos.close();
        	
        	file.createNewFile();
        }
        catch(Exception e)
        {
        	log.error("There was an error creating the file. Reason=" + e.toString());
        }
	}
	
	private byte[] fileToBytes(File file) {
		
		byte[] fileBytes = null;
		String err = "Could not get bytes from file.";
		
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
	        byte[] buf = new byte[1024];
	        for (int readNum; (readNum = fis.read(buf)) != -1;)
			    bos.write(buf, 0, readNum);
	        
	        fileBytes = bos.toByteArray();
	        fis.close();
	        bos.close();
	        
	        return fileBytes;
			
		} catch (FileNotFoundException e) {
			log.error(err + " Reason=" + e.toString());
		} catch (IOException e) {
			log.error(err + " Reason=" + e.toString());
		} catch (Exception e) {
			log.error(err + " Reason=" + e.toString());
		}
		
        return fileBytes;
	}
	
	private byte[] appendFileData(byte[] data, byte[] newData, long i) {
		
		int offset = (int)i * 1024,
			len = (int)newData.length,
			len2 = (int)data.length;
		
		for (int j=0; j<len && j<1024 && j+offset<len2; j++) {
			data[offset+j] = newData[j];
		}
		
		return data;
	}
	
	private byte[] longToByte(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	private long byteToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();
	    return buffer.getLong();
	}
	
	private String stringFromResponse(byte[] bytes, int index, int len) {
		try {
			return new String(Arrays.copyOfRange(bytes, index, index+len), ENCODING).trim();
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	private long longFromResponse(byte[] bytes, int index) {
		return byteToLong(Arrays.copyOfRange(bytes, index, index+8));
	}
	
	private boolean isLong(byte[] bytes, int index, long compare) {
		return new Long(byteToLong(Arrays.copyOfRange(bytes, index, 8))).equals(compare);
	}
	
	private byte[] getFileData(byte[] response) {
		return Arrays.copyOfRange(response, 1023, 2047);
	}
	
	private String fileSize(long bytes) {
		return bytes + " bytes";
	}
	
	private void close() {
		try {
	        out.close();
	        in.close();
	        socket.close();
		} catch (Exception e) {
			log.error(e.toString());
			System.exit(-1);
		}
	}
	
}
