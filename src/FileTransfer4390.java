package computernetwork;

import java.io.File;
import java.util.*;

/**
 * 
 * @author Gian Brazzini
 *
 */
class FileTransfer4390
{
	private static final boolean DEBUG = true;
	public static final int DEFAULT_PORT = 10001;
	public static final String SEND = "send", RECEIVE = "receive", EMPTY = "";

	static Scanner scanner = new Scanner(System.in); 
	private static Logger logs;
	private static TransferHandler transfer;
	private static File inputFile;
	private static int port = DEFAULT_PORT;
	private static String fileName = "",
			action = "",
			address = "localhost",
			protocol = "tcp";
	
	/**
	 * @constructor
	 */
	public static void main(String args[]) {
		logs = new Logger(args);
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("--"+SEND) || args[i].equalsIgnoreCase("--"+RECEIVE)) {
				if (action.equals(EMPTY)) {
					action = args[i].substring(2);
				} else {
					logs.warn("Ignoring option '" + args[i] + "'. Action was already set to '" + action + "'.");
				}
			}
			
			if (args[i].equals("--help") || args[i].equals("-h")) {
				printUserHelp();
				return;
			}
			
			if (args[i].equals("--protocol") || args[i].equals("-p")) {
				protocol = args[i+1];
				i++;
			}
			
			if (args[i].equals("--address") || args[i].equals("-a")) {
				address = args[i+1];
				i++;
			}
			
			if (args[i].equals("--filename")) {
				fileName = args[i+1];
				i++;
			}
				
			if (args[i].equals("--port") || args[i].equals("-b")) {
				port = Integer.parseInt(args[i+1]);
				i++;
			}
		}
		
		if (!validateInitialization())
			return;
		
		try {
			transfer = new TransferHandler(logs, port, protocol);
			if (action.equals(SEND)) {
				transfer.sendFile(inputFile);
			} else if (action.equals(RECEIVE)) {
				transfer.receiveFile(address);
			} else {
				logs.error("There was an error performing operation. Reason= No action named" + action);
			}
		} catch (Exception e) {
			logs.error(e.toString());
		}
	}	
	
	private static boolean validateInitialization()
	{
		try  {	
			if (action.equalsIgnoreCase(SEND))  {
				if (fileName.equals(EMPTY))
					getFilePathFromConsole();
				
				inputFile = new File(fileName);
				if (!inputFile.exists()) {
					logs.error("File '"+ fileName +"' does not exist.");
				}
				
			}
			else if (action.equalsIgnoreCase(RECEIVE)) {
				
			} else {
				logs.error("No command line option for which action was given.");
				return false;
			}
			
			if (DEBUG)
				return true;
			
			System.out.print("Continue (y/n)? ");
			String userAgreesToContinue = scanner.nextLine();
			
			if ( userAgreesToContinue.length() < 0 || String.valueOf(Character.toUpperCase(userAgreesToContinue.charAt(0))).equals("Y") )
				return false;
			else 
				return true;
			
		} catch (Exception e) {
			logs.error(e.toString());
			return false;
		}
	}
	
	private static void getFilePathFromConsole() {
		try {
			System.out.print("No command line option was given for filename. "
					+ "Please enter the file path of the file you'd like to send now: ");
			
			boolean valid = true;
			do {
				fileName = scanner.nextLine();
			}
			while(!valid);
			
		} catch(Exception e) {
			logs.error(e.toString());
		}
	}
	
	private static void printUserHelp() {
		System.out.print("\nWelcome to the CE4390 semester project for Gian Brazzini and \n");
		System.out.print("Usage: java main [-options]");
		System.out.print("\nCommand Line Options:\n");
		
		System.out.print("--send");
		System.out.print("\t\tTells the program that it will be sending a file.\n");
		
		System.out.print("--receive");
		System.out.print("\tTells the program that it will be receiving a file.\n");
		
		System.out.print("--address, -a");
		System.out.print("\tThe protocol that we will be using. Options: udp, tcp. Default: tcp\n");
		
		System.out.print("--protocol, -p");
		System.out.print("\tThe protocol that we will be using. Options: udp, tcp. Default: tcp\n");
		
		System.out.print("--port, -b");
		System.out.print("\t\tThe port that will be used to initialize the socket. Default="+DEFAULT_PORT+"\n");

		System.out.print("--filename");
		System.out.print("\tThe name of the file that will be sent. When using --receive, this will be ignored.\n");
		
		System.out.print("--help, -h");
		System.out.print("\tDisplays user help and instructions on how to use this program.\n");
		
		System.out.print("\n You will be given a chance to update these before the program executes.\n");
	}
	
}
