package codotos.templates;


import codotos.Constants;
import codotos.context.Context;
import codotos.templates.TemplateCompiler;
import codotos.templates.TemplateTranslator;
import codotos.utils.CacheUtils;
import codotos.utils.CompilerUtils;
import codotos.config.ConfigManager;

import java.io.File;


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
		Last modified time of the template file
	*/
	private long lLastModified = 0L;
	
	
	/*
		Initialize the Template
		
		@param String File Location of the raw template file
	*/
	public TemplateItem(String sFileLoc){
	
		this.sFileLoc = sFileLoc;
	
	}
	
	
	/*
		Retrieve an instance of the template object
		
		@param Context oContext A context object that contains the generated class loader
		
		@return TemplateObject Template Object;
	*/
	public Template getInstance(Context oContext) throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException, codotos.exceptions.TemplateRuntimeException {
		
		// If the template has not been loaded yet, load it now
		if(!this.bIsLoaded){
			this.load();
			this.bIsLoaded=true;
		}
		
		try{
			
			// Create & return a Template object
			Template oTemplate = null;
		
			// Look at config manager to figure out if we are precompiling tags or runtime compilation
			if(ConfigManager.getBoolean("preCompiledTemplates")){
			
				oTemplate = (Template) Class.forName(this.sTemplateClassName).newInstance();
				
			}else{
			
				Class oClass = oContext.getGeneratedClassLoader().loadClass(this.sTemplateClassName);
				oTemplate = (Template) oClass.newInstance();
			
			}
			
			return oTemplate;
			
		}catch(java.lang.Exception e){ // Class.forName().newInstance() errors
			
			codotos.exceptions.TemplateRuntimeException oException = new codotos.exceptions.TemplateRuntimeException("Could not create Template Instance '"+ this.sTemplateClassName);
			
			oException.initCause(e);
			
			throw oException;
		
		}
	
	}
	
	private String getRawFileName(){
	
		return Constants.TEMPLATE_RESOURCES_DIR + this.sFileLoc +".tpl";
	
	}
	
	
	/*
		Load the the template.  If template has already been used, re-use the template object, otherwise create a new one. If no template class file has been created, or its cache is old, recompile a new template.
		
		@return null
	*/
	private void load() throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
	
		// Create a safe className out of the file location (remove slashes)
		String sTempClass = "codotos.templates.generated." + CompilerUtils.getClassNameFromFileLocation(this.sFileLoc+".tpl");
			
		// Add the prefix
		sTempClass = CompilerUtils.getPackageName(sTempClass) +"."+ TemplateTranslator.PREFIX + CompilerUtils.getClassName(sTempClass);
		
		this.sTemplateClassName = sTempClass;
		
		// Look at config manager to figure out if we are precompiling templates or runtime compilation
		if(!ConfigManager.getBoolean("preCompiledTemplates")){
		
			System.out.println("Processing template item "+ this.sFileLoc +".tpl");
			
			// Get the raw template file & compiled template file locations
			String sRawFileName = this.getRawFileName();
			String sCompiledFileName = CompilerUtils.getClassFileLocation(this.sTemplateClassName);
			
			// Update the last modified date
			this.lLastModified = (new File(sRawFileName)).lastModified();
			
			try {
			
				// If the compiled version is old
				if(!CacheUtils.isCacheCurrent(sRawFileName,sCompiledFileName)){
				
					// Recompile this template
					TemplateCompiler.compile(this.sTemplateClassName,sRawFileName);
				
				}
			
			}catch(codotos.exceptions.FileNotFoundException e){
			
				codotos.exceptions.TemplateInterpreterException oException = new codotos.exceptions.TemplateInterpreterException(e.getMessage());
				
				oException.initCause(e);
				
				throw oException;
			
			}
		
		}
		
	}
	
	
	public final void checkCache() throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
		
		// If the file was modified since our last check
		if(new File(this.getRawFileName()).lastModified() > this.lLastModified){
			
			// To prevent issues when another request comes in at the same time and resources dont exist
			synchronized(this){
				
				// load/compile the template again
				this.load();
			
			}
		
		}
	
	}
	
	
}

