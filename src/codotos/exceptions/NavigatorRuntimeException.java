package codotos.exceptions;


public class NavigatorRuntimeException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public NavigatorRuntimeException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public NavigatorRuntimeException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

