package codotos.templates;


import codotos.Constants;
import codotos.utils.CompilerUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/*
	This class is responsible for compiling raw template files into template java files
	
	@static
*/	
final public class TemplateTranslator {
	
	
	public static final String PREFIX = "tpl_";
	
	
	// Used to convert template to java in runtime
	public static void main(String args[]) throws codotos.exceptions.TemplateInterpreterException {
	
		if(args.length<4 || !args[0].equals("-baseDir") || args[1].isEmpty() || !args[2].equals("-destDir") || args[3].isEmpty()){
			System.out.println("File destination must be specified (eg '-baseDir C:/templates/  -destDir C:/translatedTemplates/ C:/templates/my/favorite/template.tpl')");
			return;
		}
		
		if(args.length<5 || args[4].isEmpty()){
			System.out.println("File to translate needs to be supplied");
			return;
		}
		
		
		
		
		File oBaseDirectory = new File(args[1]);
		
		if(!oBaseDirectory.exists()){
			System.out.println("Base directory does not exist");
			return;
		}
		
		File oDestDirectory = new File(args[3]);
		
		if(!oDestDirectory.exists()){
			System.out.println("Destination directory does not exist");
			return;
		}
		
		for(int i=4,len=args.length; i<len; i++){
			
			File oFileToTranslate = new File(args[i]);
			
			if(!oFileToTranslate.exists()){
				System.out.println("File to translate does not exist");
				return;
			}
			
			String sTemplateClassName = "codotos.templates.generated." + (oFileToTranslate.getAbsolutePath().substring(oBaseDirectory.getAbsolutePath().length()+1,oFileToTranslate.getAbsolutePath().lastIndexOf("."))).replaceAll("\\\\",".");
			
			// Add the prefix
			sTemplateClassName = CompilerUtils.getPackageName(sTemplateClassName) +"."+ TemplateTranslator.PREFIX + CompilerUtils.getClassName(sTemplateClassName);
			
			// Generate the class name
			String sRelativeFileLoc = sTemplateClassName.replaceAll("\\.","\\\\");
			
			// Get the translated file location
			File oTranslatedTemplateFile = new File(oDestDirectory.getAbsolutePath() +"/"+ sRelativeFileLoc +".java");
			
			// Make the parent directories
			oTranslatedTemplateFile.getParentFile().mkdirs();
			
			System.out.println("Translating "+ sTemplateClassName);
			
			translate(sTemplateClassName,oFileToTranslate.getAbsolutePath(),oTranslatedTemplateFile.getAbsolutePath());
		
		}
		
	}
	
	
	public static void translate(String sTemplateClassName,String sRawTemplateFileLoc,String sTranslatedTemplateFileLoc) throws codotos.exceptions.TemplateInterpreterException {
		
		try{
		
			// Open the compiled file for writing
			FileWriter oOutputStream = new FileWriter(sTranslatedTemplateFileLoc);
			BufferedWriter oBufferedOutput = new BufferedWriter(oOutputStream);
		
			try{
			
				// Output the compiled template header
				header(sTemplateClassName,oBufferedOutput);
				
				// Open the raw file for reading
				FileInputStream oInputStream = new FileInputStream(sRawTemplateFileLoc);
				DataInputStream oDataInputStream = new DataInputStream(oInputStream);
				BufferedReader oBufferedReader = new BufferedReader(new InputStreamReader(oDataInputStream));
			
				String sLine;
				while ((sLine = oBufferedReader.readLine()) != null){
					compileLine(sLine,oBufferedOutput);
					oBufferedOutput.write("\t\t");
					oBufferedOutput.write("sToReturn.append(\"\\n\");");
					oBufferedOutput.write("\n");
				}
				
				// Close the raw file
				oDataInputStream.close();
			
				// Output the compiled template footer
				footer(oBufferedOutput);
				
				// Close the compiled file		
				oBufferedOutput.close();
				
			// if an error occured while compiling ...
			// NOTE: add this when we start throwing this exception
			}catch(codotos.exceptions.TemplateInterpreterException e){
			
				// Close the compiled file, otherwise we can't delete it
				oBufferedOutput.close();
				
				// TODO ?
				//e.setResourceName(sRawTemplateFileLoc);
				
				// NOTE: No need to delete the .java file here, when the user updates their .tpl file a new .java file will be created
				
				// Rethrow our error so it trickles up
				throw e;
				
			}
		
		}catch(java.io.IOException e){
		
			codotos.exceptions.TemplateInterpreterException oException = new codotos.exceptions.TemplateInterpreterException("I/O error creating/writing template file '"+ sTranslatedTemplateFileLoc +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
	}
	
	
	/*
		Write the header of the template class to the template class file
		
		@param sFullClassName String Template Class Name		
		@param oFpCompiled FilePointer Pointer to compiled template file
		
		@return null
		
		@static
	*/
	private static void header(String sFullClassName,BufferedWriter oBufferedOutput) throws java.io.IOException {
		
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
		oBufferedOutput.write("public String getText(HashMap<String,Object> mTemplateData) throws codotos.exceptions.TemplateRuntimeException, codotos.exceptions.ResourceRuntimeException {");
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
	private static void footer(BufferedWriter oBufferedOutput) throws java.io.IOException {
		
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
	private static void compileLine(String sRawLine,BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TemplateInterpreterException {
		
		// Check for #[resources] in the line
		Pattern oPattern = Pattern.compile("(.*?)#\\[[\\s]*(.*?)\\](.*)");
		Matcher oMatcher = oPattern.matcher(sRawLine);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the #[ ] resource
			if(oMatcher.group(1).length()!=0){
				compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the resource request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("sToReturn.append("+ getResourceGetter(oMatcher.group(2)) +");");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the #[ ] resource
			if(oMatcher.group(3).length()!=0){
				compileLine(oMatcher.group(3),oBufferedOutput);
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
				compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("sToReturn.append(codotos.templates.Expression.evaluate(\""+ CompilerUtils.sanitizeString(oMatcher.group(2)) +"\",mTemplateData));");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ${ } variable
			if(oMatcher.group(3).length()!=0){
				compileLine(oMatcher.group(3),oBufferedOutput);
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
				compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("}");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ((/section)) variable
			if(oMatcher.group(3).length()!=0){
				compileLine(oMatcher.group(3),oBufferedOutput);
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
				compileLine(oMatcher.group(1),oBufferedOutput);
			}
			
			// Compile the variable request
			oBufferedOutput.write("\t\t");
			oBufferedOutput.write("if(mTemplateData.containsKey(\""+ oMatcher.group(2) +"\") && ((Boolean)mTemplateData.get(\""+ oMatcher.group(2) +"\"))==true){");
			oBufferedOutput.write("\n");
			
			// Compile the text that occured after the ((/section)) variable
			if(oMatcher.group(3).length()!=0){
				compileLine(oMatcher.group(3),oBufferedOutput);
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
	private static String getResourceGetter(String sResourceCall) throws codotos.exceptions.TemplateInterpreterException {
	
		// Split the resource call on commas
		String[] aResourceData = sResourceCall.split(",");
		
		// First variable is the name of the resource
		// NOTE: We shift this first value out of the array so we can call getText() with just the resource data (resource name not included)
		String sResourceName = aResourceData[0].trim();
		
		// Return the compiled text
		return "this.getResourceBundle().getResource("+ compileSingleString(sResourceName) +").getText("+ convertResourceArrayToCSL(aResourceData) +")";
		
	}
	
	
	private static String compileSingleString(String sString) throws codotos.exceptions.TemplateInterpreterException {
	
		StringBuilder sToReturn = new StringBuilder();
		
		Pattern oPattern = Pattern.compile("(.*?)(\\$\\{.*?\\})(.*)");
		Matcher oMatcher = oPattern.matcher(sString);
		
		// If it exists
		if(oMatcher.matches()){
		
			// Compile the text that occured before the ${ } variable
			if(oMatcher.group(1).length()!=0){
				sToReturn.append(compileSingleString(oMatcher.group(1)) +"+");
			}
			
			// Compile the variable request
			sToReturn.append("(String) codotos.templates.Expression.evaluate(\""+ CompilerUtils.sanitizeString(oMatcher.group(2)) +"\",mTemplateData)");
		
			// Compile the text that occured after the ${ } variable
			if(oMatcher.group(3).length()!=0){
				sToReturn.append("+"+ compileSingleString(oMatcher.group(3)));
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
	private static String convertResourceArrayToCSL(String[] aStringArray) throws codotos.exceptions.TemplateInterpreterException {
		
		// Array to hold strings
		StringBuilder sToReturn = new StringBuilder();
		
		// Loop through each item in the array
		for(int i=1,len=aStringArray.length; i<len; i++){
			
			if(i!=1){
				sToReturn.append(',');
			}
			
			sToReturn.append(compileSingleString(aStringArray[i]));
			
		}
		
		// Turn the array into a comma seperated list
		return sToReturn.toString();
	
	}
	

}