package codotos.exceptions;


public class TagInterpreterException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;
	String sResourceName = "UNKNOWN";
	String sRawXML = "";


	public TagInterpreterException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TagInterpreterException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
	}
	
	
	public TagInterpreterException(String sMessage,String sRawXML)
	{
		super(sMessage);
		this.sMessage = sMessage;
		this.setRawXML(sRawXML);
	}


	public String getMessage()
	{
		return this.sResourceName +" : "+ this.sMessage + " - " + this.sRawXML;
	}
	
	
	public void setResourceName(String sResourceName){
		if(this.sResourceName.equals("UNKNOWN")){
			this.sResourceName = sResourceName;
		}
	}
	
	
	public void setRawXML(String sRawXML){	
		if(this.sRawXML.isEmpty()){
			this.sRawXML = sRawXML;
		}
	}
	
	
}
	



