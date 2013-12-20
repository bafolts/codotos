package codotos.resources;


import codotos.resources.ResourceBundle;
import codotos.config.ConfigManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/*
	This class is a static object that handles all resource bundles
	
	@static
*/	
final public class ResourceBundleManager {
	
	
	/*
		Map of resource bundles that have already been loaded		
		@static
	*/
	static protected HashMap<String,ResourceBundle> mResourceBundles = new HashMap<String,ResourceBundle>();
	
	
	/*
		Retrieves a resource bundle object corresponding to the supplied resource bundle name
		
		@param sResourceBundleName String Name of Resource Bundle to retrieve
		
		@static
		
		@return ResourceBundleObject Resource Bundle object
	*/
	static final public ResourceBundle getBundle(String sResourceBundleName) throws codotos.exceptions.ResourceRuntimeException {
		
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
	static final public void load(String sResourceBundleName) throws codotos.exceptions.ResourceRuntimeException {
		
		// Create a new resource bundle
		ResourceBundle oBundle = new ResourceBundle();
		
		// Load the resources
		oBundle.load(sResourceBundleName);
		
		// Add it to the map
		ResourceBundleManager.mResourceBundles.put(sResourceBundleName,oBundle);
	
	}
	
	
	static final public void checkCache() throws codotos.exceptions.ResourceRuntimeException {
		
		// Iterate over each resource bundle, calling it's checkCache() method
		Iterator oIterator = mResourceBundles.entrySet().iterator();
		while (oIterator.hasNext()) {
			Entry oPairs = (Entry) oIterator.next();
			((ResourceBundle) oPairs.getValue()).checkCache();
		}
		
	}
	
	
}

