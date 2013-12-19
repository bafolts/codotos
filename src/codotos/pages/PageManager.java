package codotos.pages;


import codotos.Constants;
import codotos.tags.Tag;
import codotos.tags.TagCompiler;
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
	public static Tag get(String sPageFileLoc) throws java.lang.Exception {
		
		String sFullClassName = "codotos.pages.generated." + CompilerUtils.getClassNameFromFileLocation(sPageFileLoc+".pg");
		
		// Convert the name of the class to a folder structure
		String sCompiledFileName = CompilerUtils.getClassFileLocation(sFullClassName);
		
		// Get the filename for the raw page file and the compiled page file
		String sRawFileName = Constants.PAGE_RESOURCES_DIR + sPageFileLoc +".pg";
		
		// Check if the compiled version is old
		if(!CacheUtils.isCacheCurrent(sRawFileName,sCompiledFileName)){
		
			// Create our tag compiler
			TagCompiler oTagCompiler = new TagCompiler();
			
			// Compile
			oTagCompiler.compile(sFullClassName,sRawFileName);
		
		}
		
		Tag oPage = null;
		
		try{
		
			// create the page object
			oPage = (Tag) Class.forName(sFullClassName).newInstance(); 
			
		}catch(java.lang.ClassNotFoundException e){
			
			throw new java.lang.Exception("Could not find class for page '"+ sFullClassName +"'");
		
		}catch(java.lang.IllegalAccessException e){
		
			throw new java.lang.Exception("No access to page '"+ sFullClassName +"'");
		
		}catch(java.lang.InstantiationException e){
		
			throw new java.lang.Exception("Could not create instance of page '"+ sFullClassName +"'");
		
		}
		
		// return the page object
		return oPage;
	}


}

