package codotos.pages;


import codotos.config.ConfigManager;
import codotos.Constants;
import codotos.context.Context;
import codotos.tags.Tag;
import codotos.tags.TagTranslator;
import codotos.tags.TagCompiler;
import codotos.tags.TagLoader;
import codotos.utils.CompilerUtils;
import codotos.utils.CacheUtils;

import java.io.File;


/*
	This class is responsible for managing the different user-created pages	
	It is in charge of:
		- Loading a page file
		- Compiling the page file to a class
		- Creating the page object
		- Returning the page object
	
	Note: A page is just a glorified tag
	
	@static
*/	
public final class PageManager {

	
	/*
		Given a page name, return an instance of that page object.  Compile the page object if necessary.
		
		@param sPageFileLoc String Location of the page file to retrieve
		
		@return TagObject Page that was requested, null if problem occured
	*/	
	public static Tag get(String sPageFileLoc, Context oContext) throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		// Create a safe className out of the file location (remove slashes)
		String sTempClass = "codotos.tags.generated.pages." + CompilerUtils.getClassNameFromFileLocation(sPageFileLoc);
			
		// Add the prefix
		sTempClass = CompilerUtils.getPackageName(sTempClass) +"."+ TagTranslator.PREFIX + CompilerUtils.getClassName(sTempClass);
		
		// Generate the full class name for this page
		String sFullClassName = sTempClass;		
		
		// See if the config manager dictates that the file should be compiled
		if(!ConfigManager.getBoolean("preCompiledTags")){
		
			// Convert the name of the class to a folder structure
			String sCompiledFileName = CompilerUtils.getClassFileLocation(sFullClassName);
		
			// Get the filename for the raw page file and the compiled page file
			// TODO - sPageFileLoc starts with a "/", page resources dir ends with a "/"
			String sRawFileName = Constants.PAGE_RESOURCES_DIR + sPageFileLoc;
		
			try{
				
				Boolean bDoCompile = false;
				
				// If we are doing runtime tag cache checks
				if(ConfigManager.getBoolean("runtimeTagCacheChecks")){
					
					// Check the cache, recompile if its old
					if(!CacheUtils.isCacheCurrent(sRawFileName,sCompiledFileName)){
						
						bDoCompile=true;
						
					}
				
				// If we are not doing runtime tag cache checks & the compiled file does not exist
				}else if(!(new File(sCompiledFileName)).exists()){
					
					bDoCompile=true;
				
				}
				
				if(bDoCompile){
				
					// Create our tag compiler
					TagCompiler oTagCompiler = new TagCompiler();
					
					// Compile
					oTagCompiler.compile(sFullClassName,sRawFileName);
				
				}
				
			}catch(codotos.exceptions.FileNotFoundException e){
			
				codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Page file '"+ sRawFileName +"' not found.");
				
				oException.initCause(e);
				
				throw oException;
			
			}
		
		}
		
		Tag oPage = null;
		
		try{
			
			// Look at config manager to figure out if we are precompiling tags or runtime compilation
			if(ConfigManager.getBoolean("preCompiledTags")){
				
				// Grab it using our default class loader
				oPage = (Tag) Class.forName(sFullClassName).newInstance();
			
			}else{
				
				// Grab it using our generated class loader (for run-time compilations)
				Class oTagClass = oContext.getGeneratedClassLoader().loadClass(sFullClassName);
				oPage = (Tag) oTagClass.newInstance();
			
			}
		
		}catch(java.lang.Exception e){
			
			codotos.exceptions.TagRuntimeException oException = new codotos.exceptions.TagRuntimeException("Could not create instance of page '"+ sFullClassName +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
		// return the page object
		return oPage;
	}


}

