package codotos.resources;


import codotos.resources.ResourceItem;
import codotos.Constants;
import codotos.utils.CacheUtils;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.Serializable;


/*
	This class is a representation of a resource bundle, which is a group of resource objects. Bundles can be quickly swapped for other bundles, which makes internationalization easier
*/	
public class ResourceBundle implements Serializable {


	/*
		Default resource bundle name
	*/
	public static String DEFAULT_BUNDLE = "en_us";
	
	
	/*
		Name of this resource bundle
	*/
	private String sResourceBundleName = null;
	
	
	/*
		Map of resource objects, key is the resource name
	*/
	private HashMap<String,ResourceItem> mResources = new HashMap<String,ResourceItem>();
	
	
	/*
		Given the resource name, attempt to load the resource objects from cache. If not, parse the resource file & create resource objects.
		
		@param sResourceBundleName String Name of the resource bundle
		
		@return Boolean True if successful, False if error occured
	*/
	public Boolean load(String sResourceBundleName) throws java.lang.Exception {
		
		this.sResourceBundleName = sResourceBundleName;
		
		// If we can load from the cache, we are done
		if(this.loadFromCache()){
			return true;
		}
		
		try{
		
			FileInputStream fstream = new FileInputStream(this.getResourceFileName());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String sLine;
			while ((sLine = br.readLine()) != null){
				this.loadResource(sLine);
			}
			
			in.close();
		
		}catch (java.lang.Exception e){//Catch exception if any
			
			System.err.println("Error reading resource bundle file '"+ this.getResourceFileName() +"'");
			// TODO
			//throw new java.lang.Exception("Error reading resource bundle file '"+ this.getResourceFileName() +"'");
			return false;
			
		}
		
		// Save into our cache
		this.saveCache();
		
		return true;	
	}
	
	
	/*
		Returns a specific resource object
		
		@param sResourceName String Resource name
		
		@return ResourceObject Resource Object
	*/
	public ResourceItem getResource(String sResourceName){
		
		// Check if the resource exists
		if(!this.mResources.containsKey(sResourceName)){
		
			//throw new java.lang.Exception("Resource '"+ sResourceName +"' does not exist in the '"+ this.sResourceBundleName +"' resource bundle'");
			return null;
		
		}
		
		return this.mResources.get(sResourceName);
	
	}
	
	
	/*
		Get the resource file location
		
		@return String Resource file location
	*/
	private String getResourceFileName(){
		return Constants.RESOURCE_RESOURCES_DIR + this.sResourceBundleName +".txt";
	}
	
	
	/*
		Get the resource cache file location
		
		@return String Resource cache file location
	*/
	private String getResourceCacheFileName(){
		return Constants.RESOURCE_CACHE_DIR + this.sResourceBundleName +".cache";
	}
	
	
	/*
		Attempt to load the resource bundle from cache
		
		@return Boolean True if successful, False if could not load from cache
	*/
	@SuppressWarnings("unchecked")
	private Boolean loadFromCache() throws java.lang.Exception {
		
		// if cache is not current, abort
		if(!CacheUtils.isCacheCurrent(this.getResourceFileName(),this.getResourceCacheFileName()))
			return false;
		
		try{
		
			this.mResources = (HashMap<String,ResourceItem>) CacheUtils.getCachedObject(this.getResourceCacheFileName());
			
		}catch(java.lang.Exception e){
			
			//throw new java.lang.Exception("Error opening resource bundle cache");
			return false;
		
		}
		
		// Let them know it was loaded successfully
		return true;
		
	}
	
	
	/*
		Save the resource bundle to a cache
		
		@return null
	*/
	private void saveCache(){
		
		try{
			
			CacheUtils.setCachedObject(this.mResources,this.getResourceCacheFileName());
			
		}catch(java.lang.Exception e){
		
			// TODO - WARNING LOG
			//throw new Exception("Error saving resource bundle cache");
		
		}
		
	}
	
	
	/*
		Given a line from a resource bundle file, create and initialize a resource object
		
		@param sResourceLine String Resource Bundle Line
		
		@return null
	*/
	private void loadResource(String sResourceLine){
		
		Pattern oPattern = Pattern.compile("^[\\s]*((?!#).*?)(?:\\.(plural|singular))?[\\s]*=(.*)$");
		Matcher oMatcher = oPattern.matcher(sResourceLine);
		
		// Ignore comments, or files not in the correct format
		if(!oMatcher.matches()){
			return;
		}
		
		// Create the resource based on the resource name
		// NOTE: we removed the .plural & .singular from the end of the resource name (if they existed)
		ResourceItem oResource = this.createResource(oMatcher.group(1));
		
		// If the resource name ended with a .plural, it is a grammatical resource
		if(oMatcher.group(2)!=null && oMatcher.group(2).equals("plural")){
		
			oResource.loadPlural(oMatcher.group(3));
		
		// If the resource name ended with a .singular, it is a grammatical resource
		}else if(oMatcher.group(2)!=null && oMatcher.group(2).equals("singular")){
		
			oResource.loadSingular(oMatcher.group(3));
		
		// Otherwise it is a regular resource
		}else{
		
			oResource.load(oMatcher.group(3));
		
		}
		
	}
	
	
	/*
		Given a resource name, creates a resource object, adds it to the resource bundle map
		
		@param sResourceName String Resource Name
		
		@return ResourceObject Resource Object that was created
	*/
	private ResourceItem createResource(String sResourceName){
		
		// If this resource already exists, does not exist, we need to make it
		// Note: this occurs when a .plural resource was made and then a .singular resource is defined later
		if(!this.mResources.containsKey(sResourceName)){
			
			// Create the resource, insert it into the map, & return it
			ResourceItem oResourceItem = new ResourceItem(sResourceName);
			this.mResources.put(sResourceName,oResourceItem);
			return oResourceItem;
		
		}
		
		// Grab the already-loaded resource
		return this.mResources.get(sResourceName);
	
	}
	
	
}

