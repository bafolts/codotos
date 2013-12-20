package codotos.utils;


import codotos.Constants;

import java.io.File;
import java.lang.StringBuilder;


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
		
		if(sFullClassName.contains(".")){
		
			return sFullClassName.substring(0,sFullClassName.lastIndexOf('.'));
		
		}else{
		
			return "";
		
		}
	
	}
	
	
	public static String getClassName(String sFullClassName){
	
		return sFullClassName.substring(sFullClassName.lastIndexOf('.')+1);
	
	}


	public static String getClassFileLocation(String sFullClassName){
	
		return Constants.SRC_BASE_DIR + sFullClassName.replaceAll("\\.","/") +".java";
	
	}


	public static String getCompiledClassFileLocation(String sFullClassName){
	
		return Constants.SRC_BASE_DIR + sFullClassName.replaceAll("\\.","/") +".class";
	
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
	
	
	public static String buildClasspathFromFolder(String sFolder){
		
		File oClassPathFolder = new File(sFolder);
		File[] aFiles = oClassPathFolder.listFiles();
		StringBuilder sToReturn = new StringBuilder();
		
		for(int i=0,len=aFiles.length; i<len; i++){
			if(aFiles[i].getName().endsWith(".jar")){
				sToReturn.append(aFiles[i].getAbsoluteFile());
				sToReturn.append(File.pathSeparatorChar);
			}
		}
		
		return sToReturn.toString();
		
	}


}


