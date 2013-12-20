package codotos.templates;


import codotos.templates.TemplateBundle;
import codotos.config.ConfigManager;

import java.util.HashMap;
import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;


/*
	This class is a static object that handles all template bundles
	
	@static
*/
final public class TemplateBundleManager {
	
	/*
		Map of template bundles that have already been loaded		
		@static
	*/
	private static HashMap<String,TemplateBundle> mTemplateBundles = new HashMap<String,TemplateBundle>();
	
	
	/*
		Retrieves a template bundle object corresponding to the supplied template bundle name
		
		@param sTemplateBundleName String Name of Template Bundle to retrieve
		
		@static
		
		@return TemplateBundleObject Template Bundle object
	*/
	static public TemplateBundle getBundle(String sTemplateBundleName) throws codotos.exceptions.TemplateInterpreterException {
	
		// If we have not already loaded this template bundle
		if(!mTemplateBundles.containsKey(sTemplateBundleName)){
		
			// Load the requested template bundle
			load(sTemplateBundleName);
			
		}
		
		// Return the template bundle object
		return mTemplateBundles.get(sTemplateBundleName);
		
	}
	
	
	/*
		Creates a template bundle object and adds it to the map
		
		@param sTemplateBundleName String Name of Template Bundle to load & add
		
		@static
		
		@return null
	*/
	static private void load(String sTemplateBundleName) throws codotos.exceptions.TemplateInterpreterException {
		
		// Create a new template bundle
		TemplateBundle oBundle = new TemplateBundle();
		
		// Load the templates
		oBundle.load(sTemplateBundleName);
		
		// Add it to the map
		mTemplateBundles.put(sTemplateBundleName,oBundle);
	
	}
	
	
	static final public void checkCache() throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
		
		// Iterate over each resource bundle, calling it's checkCache() method
		Iterator oIterator = mTemplateBundles.entrySet().iterator();
		while (oIterator.hasNext()) {
			Entry oPairs = (Entry) oIterator.next();
			((TemplateBundle) oPairs.getValue()).checkCache();
		}
	
	}
	
	
}

