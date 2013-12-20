package codotos.exceptions;


public class TemplateCompilerException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public TemplateCompilerException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TemplateCompilerException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

