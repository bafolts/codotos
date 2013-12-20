package codotos.navigation;


import codotos.config.ConfigManager;
import codotos.Constants;
import codotos.navigation.Route;
import codotos.context.Context;
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
public final class Navigator {


	/*
		Whether or not the Navigator has been loaded
	*/
	private static Boolean bLoaded = false;


	/*
		Array of Navigator Route objects
		This is serialized and cached until map.xml is modified
	*/
	private static ArrayList<Route> aRoutes = new ArrayList<Route>();
	
	
	/*
		Route used to process all errors
	*/
	private static Route oErrorRoute = null;


	/*
		Filename of the map XML file
	*/
	private static String MAP_FILE = "map.xml";


	/*
		Last modified time of the map.xml file
	*/
	private static long lLastModified = 0L;
	
	
	/*
		Setup the navigator object
		
		@constructor
	*/
	public Navigator(){
	
	}
	
	
	/*
		Load the navigators data from the map XML file or serialized cache.
		Cache the data when complete
		
		@return void
	*/
	static public final void load() throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// If it's already been loaded
		if(bLoaded){
			
			// We have runtime navigator cache checks
			if(ConfigManager.getBoolean("runtimeNavigatorCacheChecks")){
			
				// If the file was modified since our last check
				if(new File(Constants.MAP_RESOURCES_DIR + MAP_FILE).lastModified() > lLastModified){
					
					// To prevent issues when another request comes in at the same time and routes dont exist
					synchronized(Navigator.class){
					
						// Reset parameters
						oErrorRoute = null;
						aRoutes.clear();
						
						// load the routes again
						loadRoutes();
					
					}
				
				}
			
			}
		
		}else{
		
			synchronized(Navigator.class){
			
				// load the routes
				loadRoutes();
			
				bLoaded=true;
			
			}
		
		}
	
	}
	
	
	static private final void loadRoutes() throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// If we cannot load from the cache, let's load the map XML file
		File oMapFile = new File(Constants.MAP_RESOURCES_DIR + MAP_FILE);
		
		// Save our maps last modified time
		lLastModified = oMapFile.lastModified();
		
		System.out.println("Processing navigator map "+ MAP_FILE);
		
		try{
		
			Document oMapDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(oMapFile);
		
			// Load our routes from the map xml document
			// If we failed to load routes, return false
			loadRoutes(oMapDocument);
			
		}catch(javax.xml.parsers.ParserConfigurationException e){
		
			codotos.exceptions.NavigatorMapInterpreterException oException = new codotos.exceptions.NavigatorMapInterpreterException("Error opening map '"+ oMapFile.getPath() +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}catch(org.xml.sax.SAXException e){
		
			codotos.exceptions.NavigatorMapInterpreterException oException = new codotos.exceptions.NavigatorMapInterpreterException("Error opening map '"+ oMapFile.getPath() +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}catch(java.io.IOException e){
		
			codotos.exceptions.NavigatorMapInterpreterException oException = new codotos.exceptions.NavigatorMapInterpreterException("Error opening map '"+ oMapFile.getPath() +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
	}
	
	
	/*
		Given a Navigator Node, load all of the routes
		
		@param oNavigatorDocument Document Navigator Map Document
		
		@return Boolean True if routes loaded successfully, False if a problem occured
	*/
	static private final void loadRoutes(Document oNavigatorDocument) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Get all the <route> nodes
		NodeList aRouteNodes = oNavigatorDocument.getElementsByTagName("route");
			
		// For each <route> node
		for(int i=0,len=aRouteNodes.getLength(); i<len; i++){
			
			Element oRouteNode = (Element) aRouteNodes.item(i);
			
			// Attempt to load the route, if it fails, return false
			loadRoute(oRouteNode);
			
		}
		
		// Get the the <error> node
		NodeList aErrorNodes = oNavigatorDocument.getElementsByTagName("error");
		
		if(aErrorNodes.getLength() != 1){
		
			// Throw an error, this is a show stopper
				throw new codotos.exceptions.NavigatorMapInterpreterException("One error route must be defined in "+ MAP_FILE);
		
		}
		
		// Create the new error route
		Route oNewErrorRoute = new Route();
		
		// Pass the XML node to the route objects load() method
		oNewErrorRoute.load((Element) aErrorNodes.item(0));
		
		// Save it for later
		oErrorRoute = oNewErrorRoute;
		
	}
	
	
	/*
		Given a Route Node, create a Navigator Route object and append it to the list of Navigator routes
		
		@param oRouteNode Node Route XML Node
		
		@return void
	*/
	static private final void loadRoute(Element oRouteNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
		// Create the new route
		Route oRoute = new Route();
		
		// Pass the XML node to the route objects load() method
		oRoute.load(oRouteNode);
			
		// Push it into our routes array
		aRoutes.add(oRoute);
		
	}
	
	
	/*
		Get the requested URI then navigate to the correct route
		
		@return Boolean True if navigated successfully, False if a problem occured
	*/
	static public final void navigate(Context oContext) throws codotos.exceptions.NavigatorRuntimeException {
		
		// Get the requested filename from the request object
		String sUrl = oContext.getRequest().getServletPath();
		
		// Navigate to the correct route given the requested URL
		navigateUrl(oContext,sUrl);
	
	}
	
	
	/*
		Using the supplied URI, attempt to find a route that matches the URI
		If a match is found, call the Route Object's follow() method
		
		@param oContext ContextObject Context Object
		@param sUrl String URI to match against a route object
		
		@return void
	*/
	static public final void navigateUrl(Context oContext,String sUrl) throws codotos.exceptions.NavigatorRuntimeException {
		
		// Find a route match based on the URI
		Route oRoute = findRouteMatch(sUrl);
		
		// If no route is found that matches the URI
		if(oRoute == null){
		
			// Throw an error, this is a show stopper
			throw new codotos.exceptions.NavigatorRuntimeException("Route for '"+ sUrl +"' not defined");
			
		}
		
		try{
		
			// Call the follow() method on the object
			oRoute.follow(oContext);
		
		// Catch all controller/page/tag/template/run-time errors
		}catch(java.lang.Exception e){
			
			e.printStackTrace();
			
			// Put the error into the oContext object
			oContext.setAttribute("ERROR",e);
			
			try{
			
				// Call the follow() method on the object
				oErrorRoute.follow(oContext);
				
			// Catch all controller/page/tag/template errors
			}catch(java.lang.Exception e2){
				
				e2.printStackTrace();
				
				// If you have an error on your error handler page ...
				codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Error occured on the error handling page");
					
				try{
				
					// Keep track of our errors
					e2.initCause(e);
				
					// Don't lose our error page error
					oException.initCause(e2);
					
					throw oException;
				
				// This occurs when we get a 'Can't overwrite cause' exception, when both of the e & e2 have similar causes
				}catch(java.lang.IllegalStateException e3){
				
					oException.initCause(e);
					
				}
				
				throw oException;
			
			}
			
		}
	
	}
	
	
	/*
		Based on the provided URI, find a route that matches. If no match found, returns null.
		
		@param sUrl String URI to match against a route object
		
		@return RouteObject|Null Route Object that matches the provided URI, if no match found, returns null
	*/
	static private final Route findRouteMatch(String sUrl){
		
		// Loop through each route
		for(Route oRoute : aRoutes){
			
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

