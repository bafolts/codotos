package codotos.exceptions;


public class ExpressionRuntimeException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public ExpressionRuntimeException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public ExpressionRuntimeException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

