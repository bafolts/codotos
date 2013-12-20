package codotos.navigation;


/*
	This class is the part of the MVC framework.
	It is in responsible for containing a <param> variables name/value
	
	@serializable
*/
public class ControllerParam {

	
	/*
		Name of the param
	*/
	private String sName = null;
	
	
	/*
		Value of the param
	*/
	private String sValue = null;

	
	/*
		Load the param values
		
		@param sName String Name of the param
		@param sValue String Value of the param
		
		@constructor
	*/
	public ControllerParam(String sName, String sValue){
	
		this.sName = sName;
		this.sValue = sValue;
	
	}

	
	/*
		Return the param name
		
		@return String Name of the param
	*/
	public String getName(){
	
		return this.sName;
	
	}

	
	/*
		Return the param value
		
		@param sName String Name of the param
		@return String Value of the param
		
		@constructor
	*/
	public String getValue(){
	
		return this.sValue;
	
	}
	
}

