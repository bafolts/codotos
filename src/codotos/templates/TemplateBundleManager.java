package codotos.templates;


import codotos.templates.TemplateBundle;

import java.util.HashMap;
import java.io.File;


/*
	This class is a static object that handles all template bundles
	
	@static
*/
final public class TemplateBundleManager {


	/*
		Map of template bundles that have already been loaded		
		@static
	*/
	static HashMap<String,TemplateBundle> mTemplateBundles = new HashMap<String,TemplateBundle>();
	
	
	/*
		Retrieves a template bundle object corresponding to the supplied template bundle name
		
		@param sTemplateBundleName String Name of Template Bundle to retrieve
		
		@static
		
		@return TemplateBundleObject Template Bundle object
	*/
	static public TemplateBundle getBundle(String sTemplateBundleName) throws java.lang.Exception {
	
		// If we have not already loaded this template bundle
		if(!TemplateBundleManager.mTemplateBundles.containsKey(sTemplateBundleName)){
		
			// Load the requested template bundle
			TemplateBundleManager.load(sTemplateBundleName);
			
		}
		
		// Return the template bundle object
		return TemplateBundleManager.mTemplateBundles.get(sTemplateBundleName);
		
	}
	
	
	/*
		Creates a template bundle object and adds it to the map
		
		@param sTemplateBundleName String Name of Template Bundle to load & add
		
		@static
		
		@return null
	*/
	static public void load(String sTemplateBundleName) throws java.lang.Exception {
		
		// Create a new template bundle
		TemplateBundle oBundle = new TemplateBundle();
		
		// Load the templates
		oBundle.load(sTemplateBundleName);
		
		// Add it to the map
		TemplateBundleManager.mTemplateBundles.put(sTemplateBundleName,oBundle);
	
	}
	
	
}

