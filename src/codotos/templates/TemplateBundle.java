package codotos.templates;


import codotos.Constants;
import codotos.templates.TemplateItem;
import codotos.templates.Template;
import codotos.utils.CacheUtils;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;


/*
	This class represents and individual template file
*/	
public class TemplateBundle {


	/*
		Name of this template bundle
	*/
	private String sTemplateBundleName = null;


	/*
		Map of template names to template files
	*/
	private HashMap<String, String> mTemplateNameToFiles = new HashMap<String, String>();


	/*
		Map of template files to template objects
	*/
	private HashMap<String, TemplateItem> mTemplateFilesToObject = new HashMap<String, TemplateItem>();
	
	
	/*
		Given the template bundle name, attempt to load the template objects from cache. If not, parse the template file & create template objects.
		
		@param sTemplateBundleName String Name of the template bundle
		
		@return Boolean True if successful, False if error occured
	*/
	public Boolean load(String sTemplateBundleName) throws java.lang.Exception {
		
		this.sTemplateBundleName = sTemplateBundleName;
		
		// If we can load from the cache, we are done
		if(this.loadFromCache()){
			return true;
		}
		
		try{
		
			FileInputStream fstream = new FileInputStream(this.getTemplateFileName());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String sLine;
			while ((sLine = br.readLine()) != null){
				this.processTemplateLine(sLine);
			}
			
			in.close();
		
		}catch (java.lang.Exception e){//Catch exception if any
			
			System.err.println("Error reading template bundle file '"+ this.getTemplateFileName() +"'");
			// TODO
			//throw new java.lang.Exception("Error reading template bundle file '"+ this.getTemplateFileName() +"'");
			return false;
			
		}
		
		// Save into our cache
		this.saveCache();
		
		return true;	
	}
	
	
	/*
		Returns a specific template object
		
		@param sTemplateName String Template name
		
		@return TemplateObject Template Object
	*/
	public Template getTemplate(String sTemplateName) throws java.lang.Exception {
		
		// Check if the template exists
		if(!this.mTemplateNameToFiles.containsKey(sTemplateName)){
			throw new java.lang.Exception("Template '"+ sTemplateName +"' does not exist in the '"+ this.sTemplateBundleName +"' template bundle'");
		}
		
		/*
			NOTE: The code here does not directly correlate template names & template objects, since you can have multiple template names that point to the same template objects.
			This is optimized to support those scenarios & speed up the code.
		*/
		
		// Look up the template file location based on the template name
		String sTemplateFileLoc = this.mTemplateNameToFiles.get(sTemplateName);
		
		// If the template object has not been created yet for this template
		if(!this.mTemplateFilesToObject.containsKey(sTemplateFileLoc)){
			
			// Create the template and add it to our map of templates
			this.mTemplateFilesToObject.put(sTemplateFileLoc,new TemplateItem(sTemplateFileLoc));
			
		}
		
		// Grab our already-created template object
		TemplateItem oTemplate = this.mTemplateFilesToObject.get(sTemplateFileLoc);
		
		// TODO - Re-evaluate this
		return oTemplate.getInstance();
	
	}
	
	
	/*
		Get the template file location
		
		@return String Template file location
	*/
	private String getTemplateFileName(){
		return Constants.TEMPLATE_RESOURCES_DIR + this.sTemplateBundleName +".txt";
	}
	
	
	/*
		Get the template cache file location
		
		@return String Template cache file location
	*/
	private String getTemplateCacheFileName(){
		return Constants.TEMPLATE_CACHE_DIR + this.sTemplateBundleName +".cache";
	}
	
	
	/*
		Given a line from a template bundle file, add its template name and file to a map
		
		@param sTemplateLine String Template Bundle Line
		
		@return null
	*/
	private void processTemplateLine(String sTemplateLine){
		
		// Comment, or not in the right format
		Pattern oPattern = Pattern.compile("^[\\s]*((?!#).*?)[\\s]*=[\\s]*(.*?)[\\s]*$");
		Matcher oMatcher = oPattern.matcher(sTemplateLine);
		
		// Ignore comments, or files not in the correct format
		if(!oMatcher.matches()){
			return;
		}
		
		// Add a template name/file entry to the map
		this.mTemplateNameToFiles.put(oMatcher.group(1),oMatcher.group(2));
		
	}
	
	
	/*
		Attempt to load the template bundle from cache
		
		@return Boolean True if successful, False if could not load from cache
	*/
	@SuppressWarnings("unchecked")
	private Boolean loadFromCache() throws java.lang.Exception{
		
		// if cache is not current, abort
		if(!CacheUtils.isCacheCurrent(this.getTemplateFileName(),this.getTemplateCacheFileName()))
			return false;
		
		try{
		
			this.mTemplateNameToFiles = (HashMap<String, String>) CacheUtils.getCachedObject(this.getTemplateCacheFileName());
			
		}catch(java.lang.Exception e){
			
			// TODO - warning
			//throw new java.lang.Exception("Error opening template bundle cache");
			return false;
		
		}
		
		// Let them know it was loaded successfully
		return true;
		
	}
	
	
	/*
		Save the template bundle to a cache
		
		@return null
	*/
	private void saveCache(){
		
		try{
			
			CacheUtils.setCachedObject(this.mTemplateNameToFiles,this.getTemplateCacheFileName());
			
		}catch(java.lang.Exception e){
		
			// TODO - warning
			//throw new java.lang.Exception("Error saving template bundle cache");
		
		}
		
	}
	
	
}

