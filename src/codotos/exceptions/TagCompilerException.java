package codotos.exceptions;


public class TagCompilerException extends Exception {

	private static final long serialVersionUID = 1;

	String sMessage;


	public TagCompilerException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TagCompilerException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

