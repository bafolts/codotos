package codotos.exceptions;


public class TemplateInterpreterException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public TemplateInterpreterException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TemplateInterpreterException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

