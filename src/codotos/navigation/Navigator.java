package codotos.navigation;


import codotos.Constants;
import codotos.navigation.Route;
import codotos.context.Context;
import codotos.context.Request;
import codotos.utils.CacheUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.io.File;


/*
	This class is the core of the MVC framework.
	It is in charge of:
		- Reading the map XML file
		- Determining the appropriate route for an incoming request
		- Determining any controller <params> specified in the map XML file
		- Calling the appropriate route controller
		- Calling the correct destination based on the controllers response
*/	
public class Navigator {


	/*
		Array of Navigator Route objects
		This is serialized and cached until map.xml is modified
	*/
	private ArrayList<Route> aRoutes = new ArrayList<Route>();


	/*
		Filename of the map XML file
	*/
	private static String MAP_FILE = "map.xml";


	/*
		Filename of the serialized map cache
	*/
	private static String MAP_CACHE_FILE = "map.xml.cache";
	
	
	/*
		Setup the navigator object
		
		@constructor
	*/
	public Navigator(){
	
	}
	
	
	/*
		Load the navigators data from the map XML file or serialized cache.
		Cache the data when complete
		
		@return Boolean True if loaded successfully, False if an error occured
	*/
	public Boolean load() throws java.lang.Exception {
		
		// If we successfully loaded from the cache, we are good to go
		if(this.loadFromCache()){
			return true;
		}
		
		// If we cannot load from the cache, let's load the map XML file
		File oMapFile = new File(Constants.MAP_RESOURCES_DIR + this.MAP_FILE);
		
		try{
		
			Document oMapDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(oMapFile);
		
			// Load our routes from the map xml document
			// If we failed to load routes, return false
			if(this.loadRoutes(oMapDocument) == false){
				return false;
			}
			
		}catch(java.lang.Exception e){
			System.out.println("Error opening map.xml");
			return false;
		}
		
		// Save everything into our cache so we can just load from our cache next time
		this.saveCache();
		
		// Everything was loaded successfully, return true
		return true;		
	}
	
	
	/*
		Load navigator data from cache if the cache is current, otherwise return false
		
		@return Boolean True if cache was loaded, False if cache was not loaded
	*/
	@SuppressWarnings("unchecked")
	private Boolean loadFromCache() throws java.lang.Exception {
		
		String sOriginalFile = Constants.MAP_RESOURCES_DIR + this.MAP_FILE;
		String sCachedFile = Constants.MAP_CACHE_DIR + this.MAP_CACHE_FILE;
		
		// if cache is not current, abort
		if(!CacheUtils.isCacheCurrent(sOriginalFile,sCachedFile))
			return false;
		
		try{
		
			this.aRoutes = (ArrayList<Route>) CacheUtils.getCachedObject(sCachedFile);
			
		}catch(java.lang.Exception e){
			
			// TODO
			System.out.println("Error opening map cache");
			return false;
		
		}
		
		// Let them know it was loaded successfully
		return true;
		
	}
	
	
	/*
		Save the current navigator data to a cache
		
		@return null
	*/	
	private void saveCache(){
		
		String sCachedFile = Constants.MAP_CACHE_DIR + this.MAP_CACHE_FILE;
		
		try{
			
			CacheUtils.setCachedObject(this.aRoutes,sCachedFile);
			
		}catch(java.lang.Exception e){
		
			// TODO
			System.out.println("Error saving map cache");
		
		}
		
	}
	
	
	/*
		Given a Navigator Node, load all of the routes
		
		@param oNavigatorDocument Document Navigator Map Document
		
		@return Boolean True if routes loaded successfully, False if a problem occured
	*/
	private Boolean loadRoutes(Document oNavigatorDocument){
		
		// Get all the <route> nodes
		NodeList aRouteNodes = oNavigatorDocument.getElementsByTagName("route");
			
		// For each <route> node
		for(int i=0,len=aRouteNodes.getLength(); i<len; i++){
			
			Element oRouteNode = (Element) aRouteNodes.item(i);
			
			// Attempt to load the route, if it fails, return false
			if(this.loadRoute(oRouteNode) == false){
				return false;
			}
			
		}
		
		// If we got here, all routes loaded successfully
		return true;	
	}
	
	
	/*
		Given a Route Node, create a Navigator Route object and append it to the list of Navigator routes
		
		@param oRouteNode Node Route XML Node
		
		@return Boolean True if route created successfully, False if a problem occured
	*/
	private Boolean loadRoute(Element oRouteNode){
		
		// Create the new route
		Route oRoute = new Route();
		
		// Pass the XML node to the route objects load() method
		// If true is returned, route is loaded successfully
		if(oRoute.load(oRouteNode)){
			
			// Push it into our routes array
			this.aRoutes.add(oRoute);
		
		// Error occured
		}else{
		
			// Throw an exception, this is a show-stopper
			System.out.println("Could not add route");
			// TODO
			//throw new java.lang.Exception("Could not add route");
			
		}
		
		// If we are here, route was successfully loaded
		return true;	
	}
	
	
	/*
		Get the requested URI then navigate to the correct route
		
		@return Boolean True if navigated successfully, False if a problem occured
	*/
	public Boolean navigate(){
		
		// Create a context object
		Context oContext = new Context();
		
		// Set the navigator on the context object
		oContext.setNavigator(this);
		
		// Grab a reference to the request object from the context object
		Request oRequest = oContext.getRequest();
		
		// Get the requested filename from the request object
		String sUrl = oRequest.getRequestedFilename();
		
		// Navigate to the correct route given the requested URL
		return this.navigateUrl(oContext,sUrl);
	
	}
	
	
	/*
		Using the supplied URI, attempt to find a route that matches the URI
		If a match is found, call the Route Object's follow() method
		
		@param oContext ContextObject Context Object
		@param sUrl String URI to match against a route object
		
		@return Boolean True if navigated successfully, False if a problem occured
	*/
	public Boolean navigateUrl(Context oContext,String sUrl){
		
		// Find a route match based on the URI
		Route oRoute = this.findRouteMatch(sUrl);
		
		// If no route is found that matches the URI
		if(oRoute == null){
		
			// Throw an error, this is a show stopper
			System.out.println("Route for '"+ sUrl +"' not defined");
			return false;
			// TODO
			//throw new java.lang.Exception("Route for '"+ sUrl +"' not defined");
			
		}
		
		// Call the follow() method on the object
		return oRoute.follow(oContext);
	
	}
	
	
	/*
		Based on the provided URI, find a route that matches. If no match found, returns null.
		
		@param sUrl String URI to match against a route object
		
		@return RouteObject|Null Route Object that matches the provided URI, if no match found, returns null
	*/
	private Route findRouteMatch(String sUrl){
		
		// Loop through each route
		for(Route oRoute : this.aRoutes){
			
			// Ask the route if the URI matches
			if(oRoute.isMatchedUrl(sUrl)){
			
				// If it does, return the route
				return oRoute;
				
			}
			
		}
		
		// No match found, return null
		return null;		
	}


}

