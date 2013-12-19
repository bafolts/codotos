package codotos.resources;


import codotos.resources.ResourceBundle;

import java.util.HashMap;


/*
	This class is a static object that handles all resource bundles
	
	@static
*/	
final public class ResourceBundleManager {
	
	
	/*
		Map of resource bundles that have already been loaded		
		@static
	*/
	static HashMap<String,ResourceBundle> mResourceBundles = new HashMap<String,ResourceBundle>();
	
	
	/*
		Retrieves a resource bundle object corresponding to the supplied resource bundle name
		
		@param sResourceBundleName String Name of Resource Bundle to retrieve
		
		@static
		
		@return ResourceBundleObject Resource Bundle object
	*/
	static final public ResourceBundle getBundle(String sResourceBundleName) throws java.lang.Exception {
		
		// If we have not already loaded this resource bundle
		if(!ResourceBundleManager.mResourceBundles.containsKey(sResourceBundleName)){
		
			// Load the requested resource bundle
			ResourceBundleManager.load(sResourceBundleName);
			
		}
		
		// Return the resource bundle object
		return ResourceBundleManager.mResourceBundles.get(sResourceBundleName);
		
	}
	
	
	/*
		Creates a resource bundle object and adds it to the map
		
		@param sResourceBundleName String Name of Resource Bundle to load & add
		
		@static
		
		@return null
	*/
	static final public void load(String sResourceBundleName) throws java.lang.Exception {
		
		// Create a new resource bundle
		ResourceBundle oBundle = new ResourceBundle();
		
		// Load the resources
		oBundle.load(sResourceBundleName);
		
		// Add it to the map
		ResourceBundleManager.mResourceBundles.put(sResourceBundleName,oBundle);
	
	}
	
	
}

