package codotos.tags;


import codotos.utils.CompilerUtils;
import codotos.utils.CacheUtils;
import codotos.Constants;
import codotos.tags.TagTranslator;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;


public class TagCompiler {

	/*
		Compiles the tag from a raw file to a tag class file
		
		@param sFullClassName String Full classname for this tag
		@param sRawFileName String File location of the raw uncompiled file
		
		@return void
	*/
	public void compile(String sFullClassName,String sRawFileLoc) throws codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		CompilerUtils.createPackageFolders(sFullClassName);
		
		String sTranslatedFileName = CompilerUtils.getClassFileLocation(sFullClassName);
		
		TagTranslator oTagTranslator = new TagTranslator();
		oTagTranslator.translate(sFullClassName,sRawFileLoc,sTranslatedFileName);
		
		// TODO - Is there a better way?
		// In the event one of the taglibs this depends on fails
		// we should delete our currently translated file?
		try{
		
			// Compile our used taglib/dirs
			TagCompiler.compileUsedTaglibs(oTagTranslator.getUsedTagLibs());
		
		}catch(codotos.exceptions.TagCompilerException e){
			
			File oCompiledFile = new File(sTranslatedFileName);
			oCompiledFile.delete();
			
			// rethrow the error
			throw e;
		
		}
		
		System.out.println("Compiling "+ sRawFileLoc);
		
		// If all has gone well, compile the actual file
		com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
		String[] args = new String[]{ "-d", Constants.SRC_BASE_DIR, "-classpath", CompilerUtils.buildClasspathFromFolder(Constants.LIB_ROOT), "-sourcepath", Constants.SRC_BASE_DIR, sTranslatedFileName };
		int status = javac.compile(args);
		
		// Status of 1 means error compiling
		// TODO - Output the errors?
		if(status!=0){
			
			// Error compiling the file ... delete the .java file so it tries to rebuild it next time around (so the user can correct the .tag?)
			// TODO - When TagCompiler logic is locked down & solid, this wont be needed anymore!
			File oCompiledFile = new File(sTranslatedFileName);
			oCompiledFile.delete();

			throw new codotos.exceptions.TagCompilerException("Error compiling tag file '"+ sTranslatedFileName +"'");
			
		}
	
	}
	
	
	public static void compileTagDirFile(String sRelTagDirFile) throws codotos.exceptions.TagCompilerException, codotos.exceptions.TagRuntimeException, codotos.exceptions.TagInterpreterException {
		
		// Get the raw file name
		String sRawFileLoc = Constants.ROOT + sRelTagDirFile;
		
		// Get the compiled file location
		// TODO - Fix this, there is WAY too much going on here.
		String sFullClassName = TagCompilerLibItem.getCompiledFileNameFromRawFileName(sRelTagDirFile);
		sFullClassName = CompilerUtils.getPackageName(sFullClassName) +"."+ TagCompilerLibItem.getFixedTagDirClassName(CompilerUtils.getClassName(sFullClassName));
		
		String sCompiledFileLoc = CompilerUtils.getClassFileLocation(sFullClassName);
		
		// TagCompiler, used to compile each tag, reusable
		TagCompiler oTagCompiler = null;
		
		try{
			
			// If the compiled version is old (or does not exist)
			if(!CacheUtils.isCacheCurrent(sRawFileLoc,sCompiledFileLoc)){
				
				if(oTagCompiler == null){
					oTagCompiler = new TagCompiler();
				}
				
				// Compile the tag
				oTagCompiler.compile(sFullClassName,sRawFileLoc);
				
			}
		
		}catch(codotos.exceptions.FileNotFoundException e){
			
			codotos.exceptions.TagRuntimeException oException = new codotos.exceptions.TagRuntimeException("Tag file '"+ sRelTagDirFile +" included but not found.");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
	}
	
	
	/*
		Given a map of used tag elements, make sure each taglib file is compiled & up-to-date
		
		@param mUsedTagLibs Map of used tags
		
		@return void
	*/
	private static void compileUsedTaglibs(HashMap<String,TagCompilerLibItem> mUsedTagLibs) throws codotos.exceptions.TagInterpreterException, codotos.exceptions.TagCompilerException { 
		
		// TagCompiler, used to compile each tag, reusable
		TagCompiler oTagCompiler = null;
		
		// Loop through the map of used tag libs
		Iterator oIterator = mUsedTagLibs.values().iterator();
		while(oIterator.hasNext()){
		
			TagCompilerLibItem oTagLib = (TagCompilerLibItem) oIterator.next();
			
			// If the file is a tagLib then it is already compiled
			if(!oTagLib.isTagDir()){
				continue;
			}
			
			// Get the base package for this taglib
			String sBasePackageFolder = oTagLib.getPackageFolder();
			String sBasePackageName = oTagLib.getPackageName();
			
			// Loop through each of the elements
			for(String sElementName : oTagLib.getElements()){
				
				String sRawFileLoc = Constants.ROOT + sBasePackageFolder +"/"+ sElementName +".tag";
				String sCompiledFileLoc = CompilerUtils.getClassFileLocation(sBasePackageName +"."+ oTagLib.getElementName(sElementName));
				
				try{
				
					// If the compiled version is old (or does not exist)
					if(!CacheUtils.isCacheCurrent(sRawFileLoc,sCompiledFileLoc)){
					
						if(oTagCompiler == null){
							oTagCompiler = new TagCompiler();
						}
						
						String sFullClassName = oTagLib.getElementFullClassName(sElementName);
						
						// Compile the tag
						oTagCompiler.compile(sFullClassName,sRawFileLoc);
						
					}
				
				}catch(codotos.exceptions.FileNotFoundException e){
				
					codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Tag '"+ oTagLib.getPrefix() +":"+ sElementName +"' included but file '"+ sRawFileLoc  +"' not found.");
			
					oException.initCause(e);
					
					throw oException;
				
				}
			
			}
			
		}
	
	}


}

