package codotos.templates;


import codotos.Constants;
import codotos.context.Context;
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
import java.util.Iterator;
import java.util.Map.Entry;


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
		Last modified time of the template bundle file
	*/
	private long lLastModified = 0L;
	
	
	/*
		Given the template bundle name, attempt to load the template objects from cache. If not, parse the template file & create template objects.
		
		@param sTemplateBundleName String Name of the template bundle
		
		@return null
	*/
	public void load(String sTemplateBundleName) throws codotos.exceptions.TemplateInterpreterException {
		
		this.sTemplateBundleName = sTemplateBundleName;
		
		this.loadTemplate();
	
	}
	
	
	private void loadTemplate() throws codotos.exceptions.TemplateInterpreterException {
		
		System.out.println("Processing template bundle "+ sTemplateBundleName +".txt");
		
		try{
			
			File oFile = new File(this.getTemplateFileName());
			FileInputStream fstream = new FileInputStream(oFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			this.lLastModified = oFile.lastModified();
			
			String sLine;
			while ((sLine = br.readLine()) != null){
				this.processTemplateLine(sLine);
			}
			
			in.close();
		
		}catch (java.lang.Exception e){ // FileInputStream(), DataInputStream(), BufferedReader() exceptions
			
			codotos.exceptions.TemplateInterpreterException oException = new codotos.exceptions.TemplateInterpreterException("Error reading template bundle file '"+ this.getTemplateFileName() +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
	}
	
	
	/*
		Retrieves a specific template item object, based on the template name
		
		@param sTemplateName String Template name
		
		@return TemplateItem TemplateItem Object
	*/
	private TemplateItem getTemplateItem(String sTemplateName) throws codotos.exceptions.TemplateInterpreterException {
	
		// Check if the template exists
		if(!this.mTemplateNameToFiles.containsKey(sTemplateName)){
		
			throw new codotos.exceptions.TemplateInterpreterException("Template '"+ sTemplateName +"' does not exist in the '"+ this.sTemplateBundleName +"' template bundle");
			
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
		
		// Return our already-created template object
		return this.mTemplateFilesToObject.get(sTemplateFileLoc);
	
	}
	
	
	/*
		Returns a specific template object
		
		@param sTemplateName String Template name
		
		@return TemplateObject Template Object
	*/
	public Template getTemplate(String sTemplateName,Context oContext) throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException, codotos.exceptions.TemplateRuntimeException {		
		
		// Grab our already-created template object
		TemplateItem oTemplate = this.getTemplateItem(sTemplateName);
		
		return oTemplate.getInstance(oContext);
	
	}
	
	
	/*
		Get the template file location
		
		@return String Template file location
	*/
	private String getTemplateFileName(){
		return Constants.TEMPLATE_RESOURCES_DIR + this.sTemplateBundleName +".txt";
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
	
	
	public final void checkCache() throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
		
		// If the file was modified since our last check
		if(new File(this.getTemplateFileName()).lastModified() > this.lLastModified){
			
			// To prevent issues when another request comes in at the same time and resources dont exist
			synchronized(this){
			
				// Reset parameters
				this.mTemplateNameToFiles.clear();
				this.mTemplateFilesToObject.clear();
				
				// load the template again
				this.loadTemplate();
			
			}
		
		// If we didn't fully reload things, check individual .tpl files
		}else{
		
			// Iterate over each templateItem, calling it's checkCache() method
			Iterator oIterator = mTemplateFilesToObject.entrySet().iterator();
			while (oIterator.hasNext()) {
				Entry oPairs = (Entry) oIterator.next();
				((TemplateItem) oPairs.getValue()).checkCache();
			}
		
		}
		
	}
	
	
}

