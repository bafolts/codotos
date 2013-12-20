package codotos.exceptions;


public class BeanUtilsClassMethodInvocationException extends Exception {

	private static final long serialVersionUID = 1;
	

	String sMessage;


	public BeanUtilsClassMethodInvocationException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public BeanUtilsClassMethodInvocationException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

