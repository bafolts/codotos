package codotos.exceptions;


public class BeanUtilsCreateMethodParameterInstanceException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;


	public BeanUtilsCreateMethodParameterInstanceException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public BeanUtilsCreateMethodParameterInstanceException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}


	public String getError()
	{
		return this.sMessage;
	}
	
	
}
	

