package codotos.navigation;


import codotos.pages.PageManager;
import codotos.context.Context;
import codotos.controllers.Controller;
import codotos.tags.Tag;
import codotos.tags.TagLoader;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/*
	This class is an representation of a <destination> node in the Navigator Map XML file.  It is part of the MVC Framework.
	It is in charge of:
		- Reading the attributes of the <destination> node and determining the destination type
		- When called upon, handle the destination based upon its type
	
	@serializable
*/
public class Destination {


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
		
		@return void
	*/
	public void load(Element oDestinationNode) throws codotos.exceptions.NavigatorMapInterpreterException {
		
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
			throw new codotos.exceptions.NavigatorMapInterpreterException(codotos.tags.TagTranslator.getRawXML(oDestinationNode) +" does not contain a 'page', 'route', 'redirect', 'stream' or 'binary' attribute");
			
		}
		
		// Set the destination data equal to the value pulled from the attributes above
		this.sDestinationData = sAttrVal;
		
	}
	
	
	/*
		Called when the Navigator arrived at this destination, responsible for
		examining the destination type and taking appropriate action
		
		@param oContext ContextObject Context Object
		
		@return void
	*/
	public void arrived(Context oContext) throws codotos.exceptions.NavigatorRuntimeException {
		
		// Examine the destination type enum
		switch(this.eDestinationType){
		
			// If type "Page", we will be loading a page object
			case DESTINATION_TYPE_PAGE:
				
				Tag oPage = null;
				
				try {
				
					// Request the specific Page Object from the page manager
					oPage = PageManager.get(this.sDestinationData,oContext);
					
					// Set the context for the page
					oPage.setContext(oContext);
					
					// Execute the Page Object & output data to the user via response object
					oPage.display();
				
				// Error occured while compiling page and/or tags
				}catch(codotos.exceptions.TagCompilerException e){
				
					codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Error occured while compiling page and/or tags");
					
					oException.initCause(e);
					
					throw oException;
				
				// Error occured while parsing page and/or tags
				}catch(codotos.exceptions.TagInterpreterException e){
				
					codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Error occured while parsing page and/or tags");
					
					oException.initCause(e);
					
					throw oException;
				
				// Error occured while instantiating and/or executing page and/or tags
				}catch(codotos.exceptions.TagRuntimeException e){
				
					codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("Error occured while instantiating and/or executing page and/or tags");
					
					oException.initCause(e);
					
					throw oException;
				
				}
				
				break;
			
			// If type "Route", we will be navigating to another route based on the provided URI
			case DESTINATION_TYPE_ROUTE:
				
				// Tell the navigator to follow a new route based on the provided route URI
				Navigator.navigateUrl(oContext,this.sDestinationData);
				
				break;
			
			// If type "Redirect", we will simply redirect the user to the provided URI
			case DESTINATION_TYPE_REDIRECT:
				
				try{
				
					oContext.getResponse().sendRedirect(this.sDestinationData);
					
				}catch(java.io.IOException e){
				
					codotos.exceptions.NavigatorRuntimeException oException = new codotos.exceptions.NavigatorRuntimeException("IOException while attempting to redirect");
					
					oException.initCause(e);
					
					throw oException;
				
				}
				
				break;
			
			// If type "Stream", ...???
			case DESTINATION_TYPE_STREAM:
				
				// TODO - Re-evaluate this feature
				
				break;
			
			
			// If type "Binary", ...???
			case DESTINATION_TYPE_BINARY:
				
				// TODO - Re-evaluate this feature
				
				break;
				
		}
	
	}


}

