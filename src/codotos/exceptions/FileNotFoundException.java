package codotos.exceptions;


public class FileNotFoundException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public FileNotFoundException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public FileNotFoundException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

