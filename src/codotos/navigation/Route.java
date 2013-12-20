package codotos.navigation;


import codotos.navigation.Destination;
import codotos.navigation.ControllerParam;
import codotos.controllers.Controller;
import codotos.context.Context;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/*
	This class is the core of the MVC framework.
	It is in charge of:
		- Reading the map XML file
		- Determining the appropriate route for an incoming request
		- Determining any controller <params> specified in the map XML file
		- Calling the appropriate route controller
		- Calling the correct destination based on the controllers response
	
	@serializable
*/
public class Route {
	
	
	/*
		Enum representation of each of the route uri matching types
	*/
	private static final int ROUTE_TYPE_URL_EXACT = 1; // Exact URI Match
	private static final int ROUTE_TYPE_URL_REGEXP = 2; // URI Regular Expression Match
	
	
	/*
		Which uri matching type this route is
	*/
	private int eRouteType = 0;
	
	
	/*
		Route URI Match data (regular expression or exact uri string)
	*/
	private String sRouteMatchCriteria = null;
	
	
	/*
		Name of the controller object that will handle this route
	*/
	private String sController = null;
	
	
	/*
		Map of controller <param>'s as key/value pairs
	*/
	private ArrayList<ControllerParam> aControllerParams = new ArrayList<ControllerParam>();
	
	
	/*
		Map of controller <destination>'s where the key corresponds to the controllers return string & value is a Navigator Destination Object
	*/
	private HashMap<String,Destination> mDestinations = new HashMap<String,Destination>();
	
	
	/*
		Setup the navigator route object
		
		@constructor
	*/
	public Route(){
	
	}
	
	
	/*
		Load the navigator routes data from the map XML's <route> node
		
		@param oRouteNode Element <Route> DOM Node
		
		@return void
	*/
	public void load(Element oRouteNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Load the URI Matching data from the node
		// Do this only for <route> nodes, not <error> or <404> nodes
		if(oRouteNode.getTagName().toUpperCase().equals("ROUTE")){
			this.loadRouteMatch(oRouteNode);
		}
		
		// Load the controller data from the node, return false if unsuccessful
		this.loadRouteController(oRouteNode);
		
		// Load the destination data from the node, return false if unsuccessful
		this.loadRouteDestinations(oRouteNode);
		
	}
	
	
	/*
		Load the navigator routes URI matching data from the map XML's <route> node
		
		@param oRouteNode Element <Route> DOM Node
		
		@return void
	*/
	private void loadRouteMatch(Element oRouteNode) throws codotos.exceptions.NavigatorMapInterpreterException {	
		
		String sAttrVal = "";
		
		// If "match" attribute is provided, we are doing an exact URI match
		if(oRouteNode.hasAttribute("match")){
		
			sAttrVal = oRouteNode.getAttribute("match");
			this.eRouteType = ROUTE_TYPE_URL_EXACT;
		
		// If "regexp" attribute is provided, we are doing a regexp URI match
		}else if(oRouteNode.hasAttribute("regexp")){
			
			sAttrVal = oRouteNode.getAttribute("regexp");
			this.eRouteType = ROUTE_TYPE_URL_REGEXP;
			
		// If required data was not provided
		}else{
			
			// Throw an error, this is a show stopper
			throw new codotos.exceptions.NavigatorMapInterpreterException(codotos.tags.TagTranslator.getRawXML(oRouteNode) +" does not contain 'match' or 'regexp' attribute");
		
		}
		
		// Set the route match data equal to the value pulled from the attributes above
		this.sRouteMatchCriteria = sAttrVal;
		
	}
	
	
	/*
		Load the navigator route controller data from the map XML's <route> node
		
		@param oRouteNode Element <Route> DOM Node
		
		@return void
	*/
	private void loadRouteController(Element oRouteNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Find the first <controller> node
		Element oControllerNode = (Element) oRouteNode.getElementsByTagName("controller").item(0);
		
		if(oControllerNode != null){
			
			// Grab the controllers class name
			this.sController = oControllerNode.getAttribute("src");
			
			// If no "src" attribute is provided, or it is empty
			if(this.sController == null || this.sController.length() == 0){
			
				throw new codotos.exceptions.NavigatorMapInterpreterException(codotos.tags.TagTranslator.getRawXML(oControllerNode) +" does not contain a 'src' attribute");
				
			}
			
			// Load the controller param key/vaue pairs
			this.loadControllerParams(oControllerNode);
		
		// No <controller> specified
		}else{
			
			throw new codotos.exceptions.NavigatorMapInterpreterException(codotos.tags.TagTranslator.getRawXML(oRouteNode) +" does not contain a <controller> node");
		
		}
		
	}
	
	
	/*
		Load the params data from the navigator routes controller node
		
		@param oControllerNode Element <Controller> DOM Node
		
		@return void
	*/
	private void loadControllerParams(Element oControllerNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Grab all the <param>'s
		NodeList aParamNodes = oControllerNode.getElementsByTagName("param");
		
		// Loop through each <param>
		for(int i=0,len=aParamNodes.getLength(); i<len; i++){
			
			Element oParamNode = (Element) aParamNodes.item(i);
			
			// Load the controller <param>
			this.loadControllerParam(oParamNode);
			
		}
		
	}
	
	
	/*
		Given a <param> node, load its key/value pairs into the Navigator Route objects map
		
		@param oParamNode Element <Param> DOM Node
		
		@return void
	*/
	private void loadControllerParam(Element oParamNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Grab the "name" attribute
		String sName = oParamNode.getAttribute("name");
		
		// If no name provided, invalid
		if(sName == null || sName.length() == 0){
		
			throw new codotos.exceptions.NavigatorMapInterpreterException(codotos.tags.TagTranslator.getRawXML(oParamNode) +" requires a 'name' attribute");
		
		}
		
		// Grab the "value" attribute
		String sValue = oParamNode.getAttribute("value");
		
		// Create our controller param object
		ControllerParam oParam = new ControllerParam(sName,sValue);
		
		// Push the controller param into the array
		this.aControllerParams.add(oParam);
		
	}
	
	
	/*
		Load the navigator route destination data from the map XML's <route> node
		
		@param oRouteNode Element <Route> DOM Node
		
		@return void
	*/
	private void loadRouteDestinations(Element oRouteNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Grab the <destination> nodes
		NodeList aDestinationNodes = oRouteNode.getElementsByTagName("destination");
		
		// Loop through each node
		for(int i=0,len=aDestinationNodes.getLength(); i<len; i++){
			
			Element oDestinationNode = (Element) aDestinationNodes.item(i);
		
			// Load the destination, returns false if an error occured
			this.loadRouteDestination(oDestinationNode);
		
		}
		
	}
	
	
	/*
		Load the destination data from the Navigator XML's <destination> node
		
		@param oDestinationNode Element <destination> DOM Node
		
		@return void
	*/
	private void loadRouteDestination(Element oDestinationNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Grab the "key" attribute
		String sDestinationKey = oDestinationNode.getAttribute("key");
		
		// If no key attribute, use a blank string as the key
		// This will allow it to catch all keys unless another key has a more specific match
		if(sDestinationKey==null){
			sDestinationKey="";
		}
		
		// Create a navigator destination object
		Destination oDestination = new Destination();
		
		// Attempt to load the destination object using the <destination> node
		// Returns true on success, false on error
		oDestination.load(oDestinationNode);
			
		// Add our destination object into the map using the key attribute as the map key
		this.mDestinations.put(sDestinationKey,oDestination);
		
	}
	
	
	/*
		Given a URI, determines if this route is a match
		
		@param sURI String URI to match the route against
		
		@return Boolean True if loaded successfully, False if unsuccessful
	*/	
	public Boolean isMatchedUrl(String sURI){
		
		// Look at our route type enum
		switch(this.eRouteType){
			
			// if exact match URI
			case ROUTE_TYPE_URL_EXACT:
				
				// If is an exact match, do an direct string match
				return (sURI.equals(this.sRouteMatchCriteria));
			
			// If it is a regexp match, test it against the compiled regexp
			case ROUTE_TYPE_URL_REGEXP:
				
				// Create a pattern based on the routes supplied regexp
				Pattern oPattern = Pattern.compile(this.sRouteMatchCriteria);
				
				// Create a matcher object from the pattern, using the given URI
				Matcher oMatcher = oPattern.matcher(sURI);
				
				// Return if it is a match
				return oMatcher.matches();
		
		}
		
		// Note: this should not occur since the load() method will throw an error if no match is found in the enum
		return false;
		
	}
	
	
	/*
		Called when we want to follow this <route>
		Responsible for:
			Loading the route controller
			Executing the route controller
			Matching the controllers returned key to a destination object
			Execute the destination object (via arrived())
		
		@param oContext ContextObject Context Object
		
		@return void
	*/	
	public void follow(Context oContext) throws codotos.exceptions.NavigatorRuntimeException {
		
		// Clear the existing controller params we loaded into the context object
		// In case the routes destination routed it to another route (and thus another controller)
		oContext.clearParams();
		
		// Load each controller param into the context object
		for(ControllerParam oParam : this.aControllerParams){
			oContext.addParam(oParam.getName(),oParam.getValue());
		}
		
		Controller oController = null;
		
		try{
		
			// Create the Controller object
			oController = (Controller) Class.forName(this.sController).newInstance(); 
			
		}catch(java.lang.Exception e){
			
			codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Could not create controller '"+ this.sController +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
		// Set the controller for the context
		oContext.setController(oController);
		
		//set the context for the controller
		oController.setContext(oContext);
		
		// Destination key will be returned that we will map to a destination object
		String sDestinationKey = null;
		
		// Tell the controller to take over & do its thing
		// This can throw user defined errors that we need to trickle up, hence the use of java.lang.Exception
		try{
		
			sDestinationKey = oController.control();
			
		}catch(java.lang.Exception e){
			
			// Show stopper, no suitable destination found
			codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Error following route '"+ this.sController +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
		// If we cannot find a matching destination based on the key
		if(!this.mDestinations.containsKey(sDestinationKey)){
			
			// Show stopper, no suitable destination found
			throw new codotos.exceptions.NavigatorRuntimeException("Destination '"+ sDestinationKey +"' does not exist");
			
		}
		
		// Grab our destination object based on our destination key
		Destination oDestination = this.mDestinations.get(sDestinationKey);
		
		// Tell the destination we have arrived
		// Destination will return true if its actions are completed successfully, false otherwise
		oDestination.arrived(oContext);
		
	}
	

}

