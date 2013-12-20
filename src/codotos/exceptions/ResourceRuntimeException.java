package codotos.exceptions;


public class ResourceRuntimeException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public ResourceRuntimeException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public ResourceRuntimeException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

