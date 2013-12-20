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


/*
	This class is a representation of a resource bundle, which is a group of resource objects. Bundles can be quickly swapped for other bundles, which makes internationalization easier
*/	
public class ResourceBundle {


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
		Last modified time of the resource bundle file
	*/
	private long lLastModified = 0L;
	
	
	/*
		Given the resource name, attempt to load the resource objects from cache. If not, parse the resource file & create resource objects.
		
		@param sResourceBundleName String Name of the resource bundle
		
		@return null
	*/
	public void load(String sResourceBundleName) throws codotos.exceptions.ResourceRuntimeException {
		
		this.sResourceBundleName = sResourceBundleName;
		
		this.loadResources();
		
	}
	
	
	private void loadResources() throws codotos.exceptions.ResourceRuntimeException {
		
		System.out.println("Processing resource "+ sResourceBundleName +".txt");
		
		try{
			
			File oFile = new File(this.getResourceFileName());
			FileInputStream fstream = new FileInputStream(oFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			// Update our last modified value
			lLastModified = oFile.lastModified();
			
			String sLine;
			while ((sLine = br.readLine()) != null){
				this.loadResource(sLine);
			}
			
			in.close();
		
		}catch (java.lang.Exception e){//Catch exception if any
			
			codotos.exceptions.ResourceRuntimeException oException = new codotos.exceptions.ResourceRuntimeException("Unable to open resource bundle file '"+ this.getResourceFileName() +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
	}
	
	
	/*
		Returns a specific resource object
		
		@param sResourceName String Resource name
		
		@return ResourceObject Resource Object
	*/
	public ResourceItem getResource(String sResourceName) throws codotos.exceptions.ResourceRuntimeException {
		
		// Check if the resource exists
		if(!this.mResources.containsKey(sResourceName)){
			
			throw new codotos.exceptions.ResourceRuntimeException("Resource '"+ sResourceName +"' does not exist in the '"+ this.sResourceBundleName +"' resource bundle");
		
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
	
	
	
	
	public final void checkCache() throws codotos.exceptions.ResourceRuntimeException {
		
		// If the file was modified since our last check
		if(new File(this.getResourceFileName()).lastModified() > lLastModified){
			
			// To prevent issues when another request comes in at the same time and resources dont exist
			synchronized(this){
			
				// Reset parameters
				this.mResources.clear();
				
				// load the resources again
				this.loadResources();
			
			}
		
		}

	}
	
	
}

