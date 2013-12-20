package codotos.exceptions;


public class TemplateRuntimeException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public TemplateRuntimeException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TemplateRuntimeException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

