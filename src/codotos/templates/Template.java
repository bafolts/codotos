package codotos.templates;


import codotos.resources.ResourceBundleManager;
import codotos.resources.ResourceBundle;

import java.util.HashMap;


/*
	This is the base class for all generated template objects
	
	@abstract
*/	
abstract public class Template {
	
	/*
		Resource Bundle name to use for this template
	*/
	private String sResourceBundleName;
	
	
	/*
		Cached Resource Bundle Object
	*/	
	private ResourceBundle oCachedResourceBundle = null;
	
	
	/*
		Setup our template
	*/
	public Template(){
	
		// Set the default bundle name
		// TODO - Inherit this from the locale
		this.sResourceBundleName = ResourceBundle.DEFAULT_BUNDLE;
	
	}
	

	/*
		Get the output of the template with variable/section/resource placeholders replaced
		
		@param mTemplateData Map of template data
		
		@return String Template Output
		
		@abstract
	*/
	abstract public String getText(HashMap<String,Object> mTemplateData) throws codotos.exceptions.TemplateRuntimeException, codotos.exceptions.ResourceRuntimeException;
	
	
	/*
		Set the resource bundle to use when doing resource placeholder replacement
		
		@param sBundleName String Name of the resource bundle
		
		@return null
		
		@final
	*/
	final public void setResourceBundleName(String sBundleName){
	
		this.sResourceBundleName = sBundleName;
		
	}
	
	
	/*
		Retrieve the resource bundle
		
		@return ResourceBundleObject Templates Resource Bundle Object
		
		@final
	*/
	final protected ResourceBundle getResourceBundle() throws codotos.exceptions.TemplateRuntimeException, codotos.exceptions.ResourceRuntimeException {
		
		// If we have not loaded our resource bundle yet
		if(this.oCachedResourceBundle == null){
		
			// Load it & cache it
			this.oCachedResourceBundle = ResourceBundleManager.getBundle(this.sResourceBundleName);
		
		}
		
		// Return the resource bundle
		return this.oCachedResourceBundle;
	
	}
	

}

