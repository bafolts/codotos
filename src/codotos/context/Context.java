package codotos.context;


import codotos.navigation.Navigator;
import codotos.controllers.Controller;
import codotos.tags.TagLoader;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
	The purpose of this class is to contain the current context. It holds server, environment, request, response, mvc data.
	
	TODO: Things to implement
		- Method to return Server Data
		- Method to return environment data
		- Should mvc controller/params/variables be part of the navigator object?
*/
public class Context {

	/*
		Map of server data (key/value pairs)
	*/
	private HashMap<String,String> mServerData = new HashMap<String,String>();
	
	/*
		Map of environment data (key/value pairs)
	*/
	private HashMap<String,String> mEnvData = new HashMap<String,String>();
	
	/*
		Request Object
	*/
	private HttpServletRequest oRequest = null;
	
	/*
		Response Object
	*/
	private HttpServletResponse oResponse = null;
	
	/*
		Generated Class Loader
	*/
	private TagLoader oGeneratedClassLoader = null;
	
	/*
		MVC Navigator Route Controller Object
	*/
	private Controller oController = null;
	
	/*
		MVC Navigator Route Parameter Map (key/value pairs)
		Passed in from map.xml as Controller <param>'s
	*/
	private HashMap<String,String> mParams = new HashMap<String,String>();
	
	/*
		MVC Navigator Tag/Page Variable Map (key/value pairs)
	*/
	private HashMap<String,Object> mVariables = new HashMap<String,Object>();
	
	/*
		Attributes that can be set/retrieved from the Context
	*/
	private HashMap<String,Object> mAttributes = new HashMap<String,Object>();
	
	
	/*
		Setup the context object
		
		@param oControllerObject ControllerObject Controller that is to be populated	
		
		@constructor
	*/
	public Context(){
		
		

	}
	
	
	/*
		Get the request object
		
		@return oRequest RequestObject Request Object
	*/
	public HttpServletRequest getRequest(){
		return this.oRequest;
	}
	
	
	/*
		Set the request object
		
		@param HttpServletRequest oRequest The Request Object
	*/
	public void setRequest(HttpServletRequest oRequest){
		this.oRequest = oRequest;
	}
	
	
	/*
		Get the response object
		
		@return oResponse ResponseObject Response Object
	*/
	public HttpServletResponse getResponse(){
		return this.oResponse;
	}
	
	
	/*
		Set the response object
		
		@param HttpServletResponse oResponse The Response Object
	*/
	public void setResponse(HttpServletResponse oResponse){
		this.oResponse = oResponse;
	}
	
	
	/*
		Get the generated class loader instance
		
		@return TagLoader Generated Class Loader instance
	*/
	public TagLoader getGeneratedClassLoader(){
		return this.oGeneratedClassLoader;
	}
	
	
	/*
		Set the generated class loader instance
		
		@param TagLoader oGeneratedClassLoader The Generated Class Loader
	*/
	public void setGeneratedClassLoader(TagLoader oGeneratedClassLoader){
		this.oGeneratedClassLoader = oGeneratedClassLoader;
	}
	
	
	/*
		Set the controller object
		Sets the tag/page variable of "model" equal to the supplied controller object
		
		@param oController ControllerObject Controller Object to be set
		
		@return null
	*/
	public void setController(Controller oController){
		this.oController = oController;
		this.setVariable("model",oController);
	}
	
	
	/*
		Get the controller object
		
		@return oController ControllerObject Controller Object
	*/
	public Controller getController(){
		return this.oController;
	}
	
	
	/*
		Clear all of the navigator route <param>'s (specified in the map.xml file)
		
		@return null
	*/
	public void clearParams(){
		this.mParams.clear();
	}
	
	
	/*
		Add a navigator route <param> key/value pair
		
		@param sKey String Param key name
		@param sValue String Param key value
		
		@return null
	*/
	public void addParam(String sKey,String sValue){	
		this.mParams.put(sKey,sValue);
	}
	
	
	/*
		Get the value of a specific <param>, given a key value
		
		@param sKey String Param key name
		
		@return String Param key value
	*/
	public String getParam(String sKey){	
		return this.mParams.get(sKey);	
	}
	
	
	/*
		Get a map of the <param> key/value pairs
		
		@return Map Param key values
	*/
	public HashMap<String,String> getParams(){	
		return this.mParams;	
	}
	
	
	/*
		Set a page/tag variable, given a name & value
		
		@param sName String Variable name
		@param oValue Object Variable value
		
		@return null
	*/
	public void setVariable(String sName,Object oValue){
		this.mVariables.put(sName,oValue);
	}
	
	
	/*
		Determine if a page/tag variable exists
		
		@param sName String Variable name
		
		@return Boolean True if variable exists, False if it does not
	*/
	public Boolean hasVariable(String sName){
		return this.mVariables.containsKey(sName);
	}
	
	
	/*
		Get a page/tag variable, given its name
		
		@param sName String Variable name
		
		@return Object Variable value
	*/
	public Object getVariable(String sName){
		return this.mVariables.get(sName);
	}
	
	
	/*
		Set a context attribute, given a name & value
		
		@param sName String Attribute name
		@param oValue Object Attribute value
		
		@return null
	*/
	public void setAttribute(String sName,Object oValue){
		this.mAttributes.put(sName,oValue);
	}
	
	
	/*
		Determine if a context attribute exists
		
		@param sName String Attribute name
		
		@return Boolean True if attribute exists, False if it does not
	*/
	public Boolean hasAttribute(String sName){
		return this.mAttributes.containsKey(sName);
	}
	
	
	/*
		Get a context attribute, given its name
		
		@param sName String Attribute name
		
		@return Object Attribute value
	*/
	public Object getAttribute(String sName){
		return this.mAttributes.get(sName);
	}
	

}

