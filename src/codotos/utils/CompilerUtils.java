package codotos.utils;


import codotos.Constants;

import java.io.File;


public final class CompilerUtils {
	
	
	/*
		Create package folders structure
		
		@param sFullClassName String Full name of the class
		
		@return void
	*/
	public static void createPackageFolders(String sFullClassName){
		
		// Make the directory structure if it does not exist
		File oPackageFolder = new File(CompilerUtils.getPackageFolderLocation(sFullClassName));
		oPackageFolder.mkdirs();
		
	}
	
	
	public static String getPackageName(String sFullClassName){
	
		return sFullClassName.substring(0,sFullClassName.lastIndexOf('.'));
	
	}
	
	
	public static String getClassName(String sFullClassName){
	
		return sFullClassName.substring(sFullClassName.lastIndexOf('.')+1);
	
	}


	public static String getClassFileLocation(String sFullClassName){
	
		return Constants.SRC_BASE_DIR + sFullClassName.replaceAll("\\.","/") +".java";
	
	}


	public static String getPackageFolderLocation(String sFullClassName){
	
		return Constants.SRC_BASE_DIR + CompilerUtils.getPackageName(sFullClassName).replaceAll("\\.","/");
	
	}


	public static String getClassNameFromFileLocation(String sRelativeFileLoc){
		
		return sRelativeFileLoc.substring(0,sRelativeFileLoc.lastIndexOf('.')).replaceAll("/",".");
	
	}
	
	
	public static String sanitizeString(String sRawString){
		
		// TODO - Check for others
		return sRawString.replaceAll("([\\\\\"])","\\\\$1");
	
	}


}


