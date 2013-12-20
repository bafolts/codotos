package codotos.exceptions;


public class BeanUtilsNoClassMethodException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public BeanUtilsNoClassMethodException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public BeanUtilsNoClassMethodException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

