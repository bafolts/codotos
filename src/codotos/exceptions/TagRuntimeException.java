package codotos.exceptions;


public class TagRuntimeException extends Exception {

	private static final long serialVersionUID = 1;


	String sMessage;
	String sResourceName = "UNKNOWN";
	String sRawXML = "";


	public TagRuntimeException()
	{
		super();	
		this.sMessage = "unknown";	
	}
	
	
	public TagRuntimeException(String sMessage)
	{
		super(sMessage);
		this.sMessage = sMessage;
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
	

