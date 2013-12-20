package codotos.exceptions;


public class NavigatorMapInterpreterException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public NavigatorMapInterpreterException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public NavigatorMapInterpreterException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

