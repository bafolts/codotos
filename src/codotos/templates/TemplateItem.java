package codotos.templates;


import codotos.Constants;
import codotos.templates.TemplateCompiler;
import codotos.utils.CacheUtils;
import codotos.utils.CompilerUtils;

import java.io.File;
import java.lang.Exception;


/*
	This class represents and individual template file
*/	
public class TemplateItem {


	/*
		Unique name for this template (also is the className for the template object)
	*/
	private String sTemplateClassName = null;
	
	
	/*
		Location of the template file
	*/
	private String sFileLoc = null;
	
	
	/*
		Indicates whether this template has been loaded yet
	*/
	private Boolean bIsLoaded = false;
	
	
	/*
		Initialize the Template
		
		@param String File Location of the raw template file
	*/
	public TemplateItem(String sFileLoc){
	
		this.sFileLoc = sFileLoc;
	
	}
	
	
	/*
		Retrieve an instance of the template object
		
		@return TemplateObject Template Object;
	*/
	public Template getInstance() throws java.lang.Exception {
		
		// If the template has not been loaded yet, load it now
		if(!this.bIsLoaded){
			this.load();
		}
		
		try{
			
			// Create & return a Template object
			Template oTemplate = (Template) Class.forName(this.sTemplateClassName).newInstance();
			return oTemplate;
			
		}catch(java.lang.Exception e){
			
			throw new java.lang.Exception("Could not create Template Instance '"+ this.sTemplateClassName);
		
		}
	
	}
	
	
	/*
		Load the the template.  If template has already been used, re-use the template object, otherwise create a new one. If no template class file has been created, or its cache is old, recompile a new template.
		
		@return null
	*/
	private void load() throws java.lang.Exception {
	
		// Create a safe className out of the file location (remove slashes)
		this.sTemplateClassName = "codotos.templates.generated." + CompilerUtils.getClassNameFromFileLocation(this.sFileLoc+".tpl");
		
		// Get the raw template file & compiled template file locations
		String sRawFileName = Constants.TEMPLATE_RESOURCES_DIR + this.sFileLoc +".tpl";
		String sCompiledFileName = CompilerUtils.getClassFileLocation(this.sTemplateClassName);
		
		// If the compiled version is old
		if(!CacheUtils.isCacheCurrent(sRawFileName,sCompiledFileName)){
		
			// Recompile this template
			this.compile(sRawFileName);
		
		}
		
		// Mark this template as loaded so subsequent calls dont need to go through this process
		this.bIsLoaded=true;
		
	}
	
	
	/*
		Compile our raw template into a template class file
		
		@param sRawFileName String File location of the raw template
		@param sCompiledFileName String File location of the compiled template
	*/
	private void compile(String sRawFileName) throws java.lang.Exception {
		
		// Attempt to compile the template
		TemplateCompiler.compile(this.sTemplateClassName,sRawFileName);
		
	}
	
	
}

