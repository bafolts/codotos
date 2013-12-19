package codotos.templates;


import codotos.Constants;
import codotos.utils.CompilerUtils;
import codotos.tags.TagCompiler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Exception;


/*
	This class is responsible for compiling raw template files into template class definitions
	
	@static
*/	
final public class TemplateCompiler {
	
	
	/*
		Compile a raw template file into a template class definition file
		
		@param sTemplateClassName String Template Class Name		
		@param sRawTemplateFileLoc String Raw Template File
		@param sCompiledTemplateFileLoc String Compiled Template File
		
		@static
	*/
	public static void compile(String sTemplateClassName,String sRawTemplateFileLoc) throws java.lang.Exception {
		
		// Make the directory structure if it does not exist
		CompilerUtils.createPackageFolders(sTemplateClassName);
		
		String sCompiledTemplateFileLoc = CompilerUtils.getClassFileLocation(sTemplateClassName);
		
		try{
		
			// Open the compiled file for writing
			FileWriter oOutputStream = new FileWriter(sCompiledTemplateFileLoc);
			BufferedWriter oBufferedOutput = new BufferedWriter(oOutputStream);
		
			// Output the compiled template header
			TemplateCompiler.header(sTemplateClassName,oBufferedOutput);
			
			// Open the raw file for reading
			FileInputStream oInputStream = new FileInputStream(sRawTemplateFileLoc);
			DataInputStream oDataInputStream = new DataInputStream(oInputStream);
			BufferedReader oBufferedReader = new BufferedReader(new InputStreamReader(oDataInputStream));
		
			String sLine;
			while ((sLine = oBufferedReader.readLine()) != null){
				TemplateCompiler.compileLine(sLine,oBufferedOutput);
			}
			
			// Close the raw file
			oDataInputStream.close();
		
			// Output the compiled template footer
			TemplateCompiler.footer(oBufferedOutput);
			
			// Close the compiled file		
			oBufferedOutput.close();
		
		}catch(java.lang.Exception e){
		
			throw new java.lang.Exception("Error compiling template file '"+ sRawTemplateFileLoc +"'");
		
		}
		
		com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
		String[] args = new String[]{ "-d", Constants.COMPILED_ROOT, sCompiledTemplateFileLoc };
		int status = javac.compile(args);
		
		// Status of 0 means compiling was successful
		if(status!=0){
		
			throw new java.lang.Exception("Error compiling file '"+ sCompiledTemplateFileLoc +"'");
		
		}
		
	}
	
	
	/*
		Write the header of the template class to the template class file
		
		@param sFullClassName String Template Class Name		
		@param oFpCompiled FilePointer Pointer to compiled template file
		
		@return null
		
		@static
	*/
	private static void header(String sFullClassName,BufferedWriter oBufferedOutput) throws java.io.IOException{
		
		oBufferedOutput.write("package "+ CompilerUtils.getPackageName(sFullClassName) +";");
		oBufferedOutput.write("\n");
		
		oBufferedOutput.write("import codotos.templates.Template;");
		oBufferedOutput.write("\n");
		oBufferedOutput.write("import codotos.templates.Expression;");
		oBufferedOutput.write("\n");
		oBufferedOutput.write("import java.util.HashMap;");
		oBufferedOutput.write("\n");
		oBufferedOutput.write("import java.lang.StringBuilder;");
		oBufferedOutput.write("\n");
		
		// Define our template class
		oBufferedOutput.write("public class "+ CompilerUtils.getClassName(sFullClassName) +" extends Template {");
		oBufferedOutput.write("\n");
		
		// Create our @override getText method that will spit out the template
		oBufferedOutput.write("\t");
		oBufferedOutput.write("public String getText(HashMap<String,Object> mTemplateData) throws java.lang.Exception {");
		oBufferedOutput.write("\n");
		
		// Setup our return string
		oBufferedOutput.write("\t\t");
		oBufferedOutput.write("StringBuilder sToReturn = new StringBuilder();");
		oBufferedOutput.write("\n");
	}
	
	
	/*
		Write the footer of the template class to the template class file
			
		@param oFpCompiled FilePointer Pointer to compiled template file
		
		@return null
		
		@static
	*/
	private static void footer(BufferedWriter oBufferedOutput) throws java.io.IOException{
		
		// Return the return string
		oBufferedOutput.write("\n");
		oBufferedOutput.write("\t\t");
		oBufferedOutput.write("return sToReturn.toString();");
		
		oBufferedOutput.write("\n");
		oBufferedOutput.write("\t");
		oBufferedOutput.write("}");
		
		oBufferedOutput.write("\n");
		oBufferedOutput.write("}");
		
	}
	
	
	/*
		Given a raw template line, compile it and write it to the class definition file
		
		@param sRawLine String Template line string
		@param oFpCompiled FilePointer Pointer to compiled template file
		
		@return null
		
		@static
	*/
	private static void compileLine(String sRawLine,BufferedWriter oBufferedOutput) throws java.io.IOException{
		
		// Check for #[resources] in the line
		Pattern oPattern = Pattern.compile("(.*?)#\\[[\\s]*(.*?)\\](.*)");
		Matcher oMatcher = oPattern.matcher(sRawLine);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the #[ ] resource
			if(oMatcher.group(1).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the resource request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("sToReturn.append("+ TemplateCompiler.getResourceGetter(oMatcher.group(2)) +");");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the #[ ] resource
			if(oMatcher.group(3).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(3),oBufferedOutput);
			}
			
			return;
		}
		
		
		// Check for ${variables}
		oPattern = Pattern.compile("(.*?)(\\$\\{.*?\\})(.*)");
		oMatcher = oPattern.matcher(sRawLine);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the ${ } variable
			if(oMatcher.group(1).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("sToReturn.append(codotos.templates.Expression.evaluate(\""+ CompilerUtils.sanitizeString(oMatcher.group(2)) +"\",mTemplateData));");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ${ } variable
			if(oMatcher.group(3).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(3),oBufferedOutput);
			}
			
			return;
		}
		
		
		// Check for ((/section))
		oPattern = Pattern.compile("(.*?)\\(\\([\\s]*\\/[\\s]*(.*?)[\\s]*\\)\\)(.*)");
		oMatcher = oPattern.matcher(sRawLine);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the ((/section)) variable
			if(oMatcher.group(1).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("}");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ((/section)) variable
			if(oMatcher.group(3).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(3),oBufferedOutput);
			}
			
			return;
		}
		
		
		// Check for ((section))
		oPattern = Pattern.compile("(.*?)\\(\\([\\s]*(.*?)[\\s]*\\)\\)(.*)");
		oMatcher = oPattern.matcher(sRawLine);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the ((/section)) variable
			if(oMatcher.group(1).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("if(mTemplateData.containsKey(\""+ oMatcher.group(2) +"\") && ((Boolean)mTemplateData.get(\""+ oMatcher.group(2) +"\"))==true){");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ((/section)) variable
			if(oMatcher.group(3).length()!=0){
				TemplateCompiler.compileLine(oMatcher.group(3),oBufferedOutput);
			}
	
			return;
		}
		
		// Setup our return string
		oBufferedOutput.write("\t\t");
		oBufferedOutput.write("sToReturn.append(\""+ CompilerUtils.sanitizeString(sRawLine) +"\");");
		oBufferedOutput.write("\n");
		
	}
	
	
	/*
		Compile a resource call string
		
		@param sResourceCall String Resource call string
		
		@return String Compiled resource call
		
		@static
	*/
	private static String getResourceGetter(String sResourceCall){
	
		// Split the resource call on commas
		String[] aResourceData = sResourceCall.split(",");
		
		// First variable is the name of the resource
		// NOTE: We shift this first value out of the array so we can call getText() with just the resource data (resource name not included)
		String sResourceName = aResourceData[0].trim();
		
		// Return the compiled text
		return "this.getResourceBundle().getResource("+ TemplateCompiler.compileSingleString(sResourceName) +").getText("+ TemplateCompiler.convertResourceArrayToCSL(aResourceData) +")";
		
	}
	
	
	private static String compileSingleString(String sString){
	
		StringBuilder sToReturn = new StringBuilder();
		
		Pattern oPattern = Pattern.compile("(.*?)(\\$\\{.*?\\})(.*)");
		Matcher oMatcher = oPattern.matcher(sString);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the ${ } variable
			if(oMatcher.group(1).length()!=0){
				sToReturn.append(TemplateCompiler.compileSingleString(oMatcher.group(1)) +"+");
			}
			
			// Compile the variable request
			sToReturn.append("codotos.templates.Expression.evaluate(\""+ CompilerUtils.sanitizeString(oMatcher.group(2)) +"\",mTemplateData)");
		
			// Compile the text that occured after the ${ } variable
			if(oMatcher.group(3).length()!=0){
				sToReturn.append("+"+ TemplateCompiler.compileSingleString(oMatcher.group(3)));
			}
		
		}else{
		
			sToReturn.append("\""+ CompilerUtils.sanitizeString(sString) +"\"");
		
		}
		
		return sToReturn.toString();
		
	}
	
	
	/*
		Output a comma seperated list of values generated using a resource array
		
		@param aArray Array Array of strings
		
		@return String Comma seperated list of values
		
		@static
	*/
	private static String convertResourceArrayToCSL(String[] aStringArray){
		
		// Array to hold strings
		StringBuilder sToReturn = new StringBuilder();
		
		// Loop through each item in the array
		for(int i=1,len=aStringArray.length; i<len; i++){
			
			if(i!=1){
				sToReturn.append(',');
			}
			
			sToReturn.append(TemplateCompiler.compileSingleString(aStringArray[i]));
			
		}
		
		// Turn the array into a comma seperated list
		return sToReturn.toString();
	
	}
	
	
}

