package codotos.templates;


import codotos.Constants;
import codotos.utils.CompilerUtils;
import codotos.templates.TemplateTranslator;

import java.io.File;

/*
	This class is responsible for compiling raw template files into template class definitions
	
	@static
*/	
final public class TemplateCompiler {
	
	
	/*
		Compile a raw template file into a template class definition file
		
		@param sTemplateClassName String Template Class Name		
		@param sRawTemplateFileLoc String Raw Template File
		
		@static
	*/
	public static void compile(String sTemplateClassName,String sRawTemplateFileLoc) throws codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
	
		// Make the directory structure if it does not exist
		CompilerUtils.createPackageFolders(sTemplateClassName);
		
		// Translate the file
		String sTranslatedTemplateFileLoc = CompilerUtils.getClassFileLocation(sTemplateClassName);
		
		TemplateTranslator.translate(sTemplateClassName,sRawTemplateFileLoc,sTranslatedTemplateFileLoc);
		
		System.out.println("Compiling "+ sTranslatedTemplateFileLoc);
		
		com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
		String[] args = new String[]{ "-d", Constants.SRC_BASE_DIR, "-classpath", CompilerUtils.buildClasspathFromFolder(Constants.LIB_ROOT), "-sourcepath", Constants.SRC_BASE_DIR, sTranslatedTemplateFileLoc };
		int status = javac.compile(args);
		
		// Status of 0 means compiling was successful
		if(status!=0){
		
			// Delete the original file, so next time it tries to recreate it.
			// Otherwise it will think the existing .java file was created after the raw file and not try to recompile
			// TODO - When TemplateCompiler logic is locked down & solid, this wont be needed anymore!
			File oCompiledFile = new File(sTranslatedTemplateFileLoc);
			oCompiledFile.delete();
		
			throw new codotos.exceptions.TemplateCompilerException("Error compiling template file '"+ sTranslatedTemplateFileLoc +"'");
		
		}
		
	}
	
	
}

