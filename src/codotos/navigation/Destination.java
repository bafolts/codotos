package codotos.navigation;


import codotos.pages.PageManager;
import codotos.context.Context;
import codotos.controllers.Controller;
import codotos.tags.Tag;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;


/*
	This class is an representation of a <destination> node in the Navigator Map XML file.  It is part of the MVC Framework.
	It is in charge of:
		- Reading the attributes of the <destination> node and determining the destination type
		- When called upon, handle the destination based upon its type
	
	@serializable
*/
public class Destination implements Serializable {


	/*
		Enum representation of each of the destination types
	*/
	private static final int DESTINATION_TYPE_PAGE = 1;
	private static final int DESTINATION_TYPE_ROUTE = 2;
	private static final int DESTINATION_TYPE_REDIRECT = 3;
	private static final int DESTINATION_TYPE_STREAM = 4;
	private static final int DESTINATION_TYPE_BINARY = 5;
	
	
	/*
		Which destination type this destination is
	*/
	private int eDestinationType = 0;
	
	
	/*
		Corresponding destination type data (page to load, url to redirect to, etc)
	*/
	private String sDestinationData = null;
	
	
	/*
		Setup the Navigator Destination object
	*/
	public Destination(){
	
	}
	
	
	/*
		Load the navigator destination based on the <destination> XML node.
		Determine the destination type & destination data
		
		@param oDestinationNode Element <destination> DOM Node
		
		@return Boolean True if loaded successfully, False if an error occured
	*/
	public Boolean load(Element oDestinationNode){
		
		String sAttrVal = "";
		
		// If the <destination> node has a 'page' attribute, it is destination type "PAGE"
		if(oDestinationNode.hasAttribute("page")){
		
			sAttrVal = oDestinationNode.getAttribute("page");
			this.eDestinationType = DESTINATION_TYPE_PAGE;
			
		// If the <destination> node has a 'route' attribute, it is destination type "ROUTE"
		}else if(oDestinationNode.hasAttribute("route")){
		
			sAttrVal = oDestinationNode.getAttribute("route");
			this.eDestinationType = DESTINATION_TYPE_ROUTE;
			
		// If the <destination> node has a 'redirect' attribute, it is destination type "REDIRECT"
		}else if(oDestinationNode.hasAttribute("redirect")){
		
			sAttrVal = oDestinationNode.getAttribute("redirect");
			this.eDestinationType = DESTINATION_TYPE_REDIRECT;
			
		// If the <destination> node has a 'stream' attribute, it is destination type "STREAM"
		}else if(oDestinationNode.hasAttribute("stream")){
		
			sAttrVal = oDestinationNode.getAttribute("stream");
			this.eDestinationType = DESTINATION_TYPE_STREAM;
			
		// If the <destination> node has a 'binary' attribute, it is destination type "BINARY"
		}else if(oDestinationNode.hasAttribute("binary")){
			
			this.eDestinationType = DESTINATION_TYPE_BINARY;
		
		// If no valid destination type was supplied
		}else{
		
			// Throw an exception, this is a show stopper
			System.out.println("<Destination> does not contain a 'page', 'route', 'redirect', 'stream' or 'binary' attribute");
			// TODO
			//throw new java.lang.Exception("<Destination> does not contain a 'page', 'route', 'redirect', 'stream' or 'binary' attribute");
			return false;
			
		}
		
		// Set the destination data equal to the value pulled from the attributes above
		this.sDestinationData = sAttrVal;
		
		// If we got here, everything was successful
		return true;
		
	}
	
	
	/*
		Called when the Navigator arrived at this destination, responsible for
		examining the destination type and taking appropriate action
		
		@param oContext ContextObject Context Object
		@param oController ControllerObject Controller Object
		
		@return Boolean True if executed successfully, False if an error occured
	*/
	public Boolean arrived(Context oContext,Controller oController) {
		
		// Examine the destination type enum
		switch(this.eDestinationType){
		
			// If type "Page", we will be loading a page object
			case DESTINATION_TYPE_PAGE:
				
				Tag oPage = null;
				
				try{
				
					// Request the specific Page Object from the page manager
					oPage = PageManager.get(this.sDestinationData);
					
				// Error occured while parsing, compiling, instantiating page and/or tags
				}catch(java.lang.Exception e){
					
					// TODO - Toggle displaying/logging error info depending on which environment you are in?
					System.out.println("TAG COMPILE EXCEPTION: "+ e.getMessage());
					// TODO - Redirect to error route
				
				}
				
				// Set the context for the page
				oPage.setContext(oContext);
				
				try{
				
					// Execute the Page Object & output data to the user via response object
					oPage.display();
					
				}catch(java.lang.Exception e){
					
					// Error occured while executing page/tags contents
					System.out.println("TAG RUNTIME EXCEPTION: "+ e.getMessage());
					// TODO - Redirect to error route
				
				}
				
				return true;
			
			// If type "Route", we will be navigating to another route based on the provided URI
			case DESTINATION_TYPE_ROUTE:
				
				// Tell the navigator to follow a new route based on the provided route URI
				return oContext.getNavigator().navigateUrl(oContext,this.sDestinationData);
			
			// If type "Redirect", we will simply redirect the user to the provided URI
			case DESTINATION_TYPE_REDIRECT:
				
				// TODO TRANSLATOR
				//header("Location: "+ this.sDestinationData);
				return true;
			
			// If type "Stream", ...???
			case DESTINATION_TYPE_STREAM:
				
				// TODO - Re-evaluate this feature
				
				return false;
			
			
			// If type "Binary", ...???
			case DESTINATION_TYPE_BINARY:
				
				// TODO - Re-evaluate this feature
				
				return false;
				
		}
		
		// If we did not match any destination type enums, something went wrong
		// Note: this should not occur since the load() method will throw an error if no match is found in the enum
		System.out.println("<Destination> does not contain a 'page', 'route', 'redirect', 'stream' or 'binary' attribute");
		// TODO
		//throw new java.lang.Exception("<Destination> does not contain a 'page', 'route', 'redirect', 'stream' or 'binary' attribute");
		return false;
	
	}


}

