package codotos.context;


import codotos.context.Request;
import codotos.navigation.Navigator;
import codotos.controllers.Controller;
//import codotos.context.Response; TODO

import java.util.HashMap;


/*
	The purpose of this class is to contain the current context. It holds server, environment, request, response, mvc data.
	
	TODO:
		- Method to return Server Data
		- Method to return response object
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
	private Request oRequest = null;
	
	/*
		Response Object
	*/
	// TODO
	//private Response oResponse = null;
	
	/*
		MVC Navigator Object
	*/
	private Navigator oNavigator = null;
	
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
		Setup the context object
		
		@param oControllerObject ControllerObject Controller that is to be populated	
		
		@constructor
	*/
	public Context(){
		
		// TODO TRANSLATOR
		//this.mServerData = &$_SERVER;
		
		// TODO TRANSLATOR
		//this.mEnvData = &$_ENV;
		
		this.oRequest = new Request();

	}
	
	
	/*
		Get the request object
		
		@return oRequest RequestObject Request Object
	*/
	public Request getRequest(){
		return this.oRequest;
	}
	
	
	/*
		Set the navigator object
		
		@param oNavigator NavigatorObject Navigator Object to be set
		
		@return null
	*/
	public void setNavigator(Navigator oNavigator){
		this.oNavigator = oNavigator;
	}
	
	
	/*
		Get the navigator object
		
		@return oNavigator NavigatorObject Navigator Object
	*/
	public Navigator getNavigator(){
		return this.oNavigator;
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
	

}

