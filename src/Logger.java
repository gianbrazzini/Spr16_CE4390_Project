package FileTransfer4390;

public class Logger {

	
	public Logger(String[] args){
		
	}
	
	public void log(String message)
	{
		System.out.print(message);
	}
	
	public void warn(String message)
	{
		System.out.print("WARNING: ");
		System.out.println(message);
	}
	
	public void error(String error)
	{
		System.out.print("\nERROR: ");
		System.out.println(error);
		if (error.equals(""))
			System.out.print("There was an error with the program.");
		else
			System.out.print("Use --help if you're in need of assistance. Aborting...\n");
		System.exit(1);
	}
	
}
