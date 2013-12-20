package codotos.tags;


import codotos.utils.CompilerUtils;
import codotos.utils.CacheUtils;
import codotos.Constants;
import codotos.config.ConfigManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class TagTranslator {
	
	
	public static final String PREFIX = "tag_";

	
	private static final String BUILT_IN_TAG = "fel";
	
	/*
		These are users to increase readability
		TOOD - Remove these eventually
	*/
	private int iVariableCount = 0;
	private int iDepthCount = 0;
	

	/*
		Unique name for the current tag file
	*/
	private String sFullClassName = "";
	

	/*
		Map of used tag libs (so we know which other files to compile)
	*/
	private HashMap<String,TagCompilerLibItem> mUsedTagLibs = new HashMap<String,TagCompilerLibItem>();

	
	// Used to convert tag to java in runtime
	public static void main(String args[]) throws codotos.exceptions.TagInterpreterException {
	
		if(args.length<4 || !args[0].equals("-baseDir") || args[1].isEmpty() || !args[2].equals("-destDir") || args[3].isEmpty()){
			System.out.println("File destination must be specified (eg '-baseDir C:/tags/ -destDir C:/translatedTags/ C:/tag/my/favorite/something.tag')");
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
		
		TagTranslator oTagTranslator = new TagTranslator();
		
		for(int i=4,len=args.length; i<len; i++){
		
			File oFileToTranslate = new File(args[i]);
			
			if(!oFileToTranslate.exists()){
				System.out.println("File does not exist: "+ oFileToTranslate.getAbsolutePath());
				return;
			}
			
			String sTagClassName = "codotos.tags.generated." + (oFileToTranslate.getAbsolutePath().substring(oBaseDirectory.getAbsolutePath().length()+1,oFileToTranslate.getAbsolutePath().lastIndexOf("."))).replaceAll("\\\\",".");
			
			// Add the prefix
			sTagClassName = CompilerUtils.getPackageName(sTagClassName) +"."+ TagTranslator.PREFIX + CompilerUtils.getClassName(sTagClassName);
			
			// Generate the class name
			String sRelativeFileLoc = sTagClassName.replaceAll("\\.","\\\\");
			
			// Get the translated file location
			File oTranslatedFile = new File(oDestDirectory.getAbsolutePath() +"/"+ sRelativeFileLoc +".java");
			
			// Make the parent directories
			oTranslatedFile.getParentFile().mkdirs();
			
			System.out.println("Translating "+ sTagClassName);
			
			oTagTranslator.translate(sTagClassName,oFileToTranslate.getAbsolutePath(),oTranslatedFile.getAbsolutePath());
		
		}
		
	}
	
	
	public HashMap<String,TagCompilerLibItem> getUsedTagLibs(){
		return this.mUsedTagLibs;
	}
	
	
	/*
		translates the tag from a raw file to a java file
		
		@param sFullClassName String Full classname for this tag
		@param sRawFileName String File location of the raw uncompiled file
		
		@return void
	*/
	public void translate(String sFullClassName,String sRawFileLoc,String sTranslatedFileLoc) throws codotos.exceptions.TagInterpreterException {
		
		// Reset these
		this.iVariableCount = 0;
		this.iDepthCount = 0;
		
		try{
		
			// Open the compiled file for writing
			FileWriter oOutputStream = new FileWriter(sTranslatedFileLoc);
			BufferedWriter oBufferedOutput = new BufferedWriter(oOutputStream);
		
			try{
				
				// Get the XML Document Node from the xml file
				Node oNode = this.getNode(sRawFileLoc);
				
				// Add the header
				this.header(sFullClassName,sRawFileLoc,oNode,oBufferedOutput);
				
				// Evaluate the xml nodes & compile them
				this.compileNodeXML(oNode,oBufferedOutput);
				
				// Add the footer
				this.footer(oBufferedOutput);
				
				// Close the compiled file
				oBufferedOutput.close();
			
			// if an error occured while interpreting ...
			}catch(codotos.exceptions.TagInterpreterException e){
			
				// Close the compiled file, otherwise we can't delete it
				oBufferedOutput.close();
				
				e.setResourceName(sRawFileLoc);
				
				// Rethrow our error so it trickles up
				throw e;
				
				// NOTE: No need to delete the .java file here, when the user updates their .tag file a new .java file will be created
			
			}
			
		}catch(java.io.IOException e){
		
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("I/O error creating/writing tag file '"+ sTranslatedFileLoc +"'");
			
			oException.initCause(e);
			
			throw oException;
			
		}
	
	}

	
	/*
		Retrieves the base DOMNode of an XML file, adding the appropriate namespaces based on the taglib/tagdirs provided
		
		@param sRawFileName String File location of the raw uncompiled xml file
		
		@return Node Base Node for the tag
	*/
	private Node getNode(String sRawFileLoc) throws codotos.exceptions.TagInterpreterException {
	
		// Get the list of taglibs this xml file utilizes
		HashMap<String,TagCompilerLibItem> mDefinedTagLibs = this.getIncludedTaglibs(sRawFileLoc);
		
		// Add namespacing to the xml document for our taglibs (otherwise the xml object will throw warnings)
		String sRawXML = this.addXMLNamespaces(sRawFileLoc,mDefinedTagLibs);
		
		try{
		
			// convert String into InputStream
			InputStream oInputStream = new ByteArrayInputStream(sRawXML.getBytes());
			
			// Create our domDocument & load it with our namespaces xml string
			// NOTE: Do not suppress warnings here, any problems we want to throw errors for the user to see
			DocumentBuilderFactory oDocumentBuilderFactory = DocumentBuilderFactory.newInstance();	
			oDocumentBuilderFactory.setNamespaceAware(true);			
			Document oTagDocument = oDocumentBuilderFactory.newDocumentBuilder().parse(oInputStream);
			
			oInputStream.close();
		
			// Get a map of used tag elements so we can compile/include those later
			this.mUsedTagLibs = this.getUsedTagLibs(oTagDocument,mDefinedTagLibs);
			
			// Return the base DOMNode
			return oTagDocument.getFirstChild();
		
		}catch(javax.xml.parsers.ParserConfigurationException e){
			
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("javax.xml.parsers.ParserConfigurationException");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(org.xml.sax.SAXException e){
			
			// Note: taglib prefixes used but not defined up top will throw this error
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("File '"+ sRawFileLoc +"' contains invalid XML ("+  e.getMessage() +")");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(java.io.IOException e){
			
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Error creating a DocumentBuilder from the '"+ sRawFileLoc +"' XML input stream");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
	}

	
	/*
		Open the raw XML file, retrieve a map of included taglibs/tagdirs
		
		@param sRawFileName String File location of the raw uncompiled file
		
		@return Map Map of included taglibs/tagdirs, with the key being the supplied prefix
	*/
	private HashMap<String,TagCompilerLibItem> getIncludedTaglibs(String sRawFileLoc) throws codotos.exceptions.TagInterpreterException {
		
		// Map that will hold the different taglib/tagdirs, key will be based on the provided tag prefix/namespace
		HashMap<String,TagCompilerLibItem> mIncludedTaglibs = new HashMap<String,TagCompilerLibItem>();
		
		// Note: Built in taglib "fel"
		mIncludedTaglibs.put(TagTranslator.BUILT_IN_TAG,new TagCompilerLibItem(TagTranslator.BUILT_IN_TAG,"codotos.tags.builtin",false));
		
		// Add our "fel" taglib to the XML document so we can read the <fel:...> tags
		String sRawXML = this.addXMLNamespaces(sRawFileLoc,mIncludedTaglibs);
		
		try{
		
			// convert String into InputStream
			InputStream oInputStream = new ByteArrayInputStream(sRawXML.getBytes());
		
			// Create our DOMDocument
			// Load our raw XML from a string
			// Note: Supress warnings since we will have namespaces that are not defined
			DocumentBuilderFactory oDocumentBuilderFactory = DocumentBuilderFactory.newInstance();	
			oDocumentBuilderFactory.setNamespaceAware(false);			
			Document oTagDocument = oDocumentBuilderFactory.newDocumentBuilder().parse(oInputStream);
			
			// close the input stream
			oInputStream.close();
			
			// For each <fel:taglib/> node
			NodeList aTagLibNodes = oTagDocument.getElementsByTagName(TagTranslator.BUILT_IN_TAG+":taglib");
			for(int i=0,len=aTagLibNodes.getLength(); i<len; i++){
				
				Element oTagLibElement = (Element) aTagLibNodes.item(i);
				
				// Add the taglib to our map
				mIncludedTaglibs.put(oTagLibElement.getAttribute("prefix"),new TagCompilerLibItem(oTagLibElement.getAttribute("prefix"),oTagLibElement.getAttribute("src"),false));
				
			}
			
			// For each <fel:tagdir/> node
			NodeList aTagDirNodes = oTagDocument.getElementsByTagName(TagTranslator.BUILT_IN_TAG+":tagdir");
			for(int i=0,len=aTagDirNodes.getLength(); i<len; i++){
				
				Element oTagDirElement = (Element) aTagDirNodes.item(i);
				
				// Add the tagdir to our map
				mIncludedTaglibs.put(oTagDirElement.getAttribute("prefix"),new TagCompilerLibItem(oTagDirElement.getAttribute("prefix"),oTagDirElement.getAttribute("src"),true));
			
			}
		
		}catch(javax.xml.parsers.ParserConfigurationException e){
			
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("javax.xml.parsers.ParserConfigurationException");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(org.xml.sax.SAXException e){
		
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("File '"+ sRawFileLoc +"' contains invalid XML ("+  e.getMessage() +")");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(java.io.IOException e){
			
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Error creating a DocumentBuilder from the '"+ sRawFileLoc +"' XML input stream");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
		// Return the map
		return mIncludedTaglibs;		
	}

	
	/*
		Used to add namespaces to a raw XML file and return the namespaced xml as a string
		
		@param sRawFileName String File location of the raw uncompiled file
		@param mNamespaces HashMap<String,TagCompilerLibItem> {0Map of included taglibs/tagdirs, with the key being the supplied prefix
		
		@return String XML in string format
	*/
	private static String addXMLNamespaces(String sRawFileLoc,HashMap<String,TagCompilerLibItem> mNamespaces) throws codotos.exceptions.TagInterpreterException {
		
		// our wrapping tag
		StringBuilder sToReturn = new StringBuilder("<tag");
		
		// Add each namespace "<tag xmlns:monkey='/tag/monkey'/>"
		Iterator oIterator = mNamespaces.values().iterator();
		while(oIterator.hasNext()){
			TagCompilerLibItem oTagLib = (TagCompilerLibItem) oIterator.next();
			sToReturn.append(" xmlns:"+ oTagLib.getPrefix() +"=\""+ oTagLib.getNamespace() +"\"");
		}
		
		sToReturn.append(">");
		
		try{
		
			File oFile = new File(sRawFileLoc);
			FileReader oFileReader = new FileReader(oFile);
			BufferedReader oBufferedReader = new BufferedReader(oFileReader);

			String sLine;
			while ((sLine = oBufferedReader.readLine()) != null){
				sToReturn.append(sLine);
			}
			
			oBufferedReader.close();
		
		}catch(java.io.FileNotFoundException e){
			
			// Tag file not found
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Tag file '"+ sRawFileLoc +"' could not be found.");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(java.io.IOException e){
			
			codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Unable to open tag file '"+ sRawFileLoc +"'.");
			
			oException.initCause(e);
			
			throw oException;
		
		}
	
		sToReturn.append("</tag>");
		
		
		// Output the actual XML
		return sToReturn.toString();
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	/*
		Add a newline & the correct number of tabs to the provided file
		
		@param oBufferedOutput BufferedWriter Writer to output the new lines and tabs to
		
		@return null
	*/
	private void outputNewLineTabs(BufferedWriter oBufferedOutput) throws java.io.IOException{
	
		oBufferedOutput.write("\n");
		
		for(int i=0,len=this.iDepthCount; i<len; i++){
			oBufferedOutput.write("\t");
		}
		
	}
	
	
	/*
		Add a newline & the correct number of tabs to the provided file
		
		@param oStringBuilder StringBuilder String to output the new lines and tabs to
		
		@return null
	*/
	private void outputNewLineTabs(StringBuilder oStringBuilder) {
	
		oStringBuilder.append("\n");
		
		for(int i=0,len=this.iDepthCount; i<len; i++){
			oStringBuilder.append("\t");
		}
		
	}

	
	/*
		Output the header for the class
		
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void header(String sFullClassName, String sRawFileLoc, Node oNode, BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TagInterpreterException {
		
		oBufferedOutput.write("package "+ CompilerUtils.getPackageName(sFullClassName) +";");
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("import java.lang.StringBuilder;");
		
		// Define our tag class
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public final class "+ CompilerUtils.getClassName(sFullClassName) +" extends codotos.tags.Tag {");
		this.iDepthCount++;
		
		// Define our constructor
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public "+ CompilerUtils.getClassName(sFullClassName) +"() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {super();}");
		
		// Define our resource name
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("protected final String getResourceName() { return \""+ CompilerUtils.sanitizeString(sRawFileLoc) +"\"; }");
		
		// Get an array of .tag files we used
		java.util.List<String> aUsedRawTagFiles = this.getUsedRawTags(this.mUsedTagLibs);
		
		// if we have any of them, we need to declare tag dependencies
		if(aUsedRawTagFiles.size()!=0){
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected static java.util.List<String> aTagResources = new java.util.ArrayList<String>("+ Integer.toString(aUsedRawTagFiles.size()) +");");
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected static Boolean bCheckedTagResources = false;");
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("static {");
			this.iDepthCount++;
			
			for(int i=0,len=aUsedRawTagFiles.size(); i<len; i++){
			
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write("aTagResources.add(\""+ aUsedRawTagFiles.get(i) +"\");");
			
			}
			
			this.iDepthCount--;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("}");
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected final java.util.List<String> getTagResources(){");
			oBufferedOutput.write("return aTagResources;");
			oBufferedOutput.write("}");
		
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected Boolean isTagResourcesAlreadyChecked(){");
			oBufferedOutput.write("return this.bCheckedTagResources;");
			oBufferedOutput.write("}");
		
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected void setTagResourcesAlreadyChecked(){");
			oBufferedOutput.write("this.bCheckedTagResources=true;");
			oBufferedOutput.write("}");
			
		}	
	
	
		// Define our attributes
		StringBuilder oAttributesString = new StringBuilder();
		int iNumAttributes = this.getDefineAttributes(oNode,oAttributesString);
		
		// Output the tag attributes
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>("+ Integer.toString(iNumAttributes) +");");
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("static {");
		
		oBufferedOutput.write(oAttributesString.toString());
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("}");
			
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){");		
		oBufferedOutput.write("return aTagAttributes;");
		oBufferedOutput.write("}");
		
		
		// Define our method
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {");
		
		// Setup our return string
		this.iDepthCount++;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("StringBuilder sToReturn = new StringBuilder();");
	
	}
	
	
	/*
		Output the footer for the class
		
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void footer(BufferedWriter oBufferedOutput) throws java.io.IOException{
		
		// Return the string with all the data in it
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("return sToReturn.toString();");
		
		this.iDepthCount--;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("}");
		
		this.iDepthCount--;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("}");
		
	}
	
	
	
	/*
		Outputs the attribute definitions
		
		@param oNode DOMNode XML DOMNode to compile
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return void
	*/
	private int getDefineAttributes(Node oNode,StringBuilder oAttributesString) throws codotos.exceptions.TagInterpreterException {
		
		ArrayList<Node> aAttributeNodes = this.getChildNodesByTagname(oNode,BUILT_IN_TAG+":defineAttribute");
		
		this.iDepthCount++;
		
		for(int i=0,len=aAttributeNodes.size(); i<len; i++){
		
			Element oElement = (Element) aAttributeNodes.get(i);
			
			// <fel:defineAttribute name="output" required="true" type="String"/>
			// <fel:defineAttribute name="userFeedback" required="true" type="UserFeedbackBean"/>
			
			// get the "name" attribute
			String sName = oElement.getAttribute("name");			
			if(!oElement.hasAttribute("name") || sName.isEmpty()){
			
				throw new codotos.exceptions.TagInterpreterException("<fel:defineAttribute> requires a 'name' attribute",TagTranslator.getRawXML(oElement,2));
				
			}
			
			// get the type attribute
			String sType = "java.lang.String";
			if(oElement.hasAttribute("type")){
				
				sType = oElement.getAttribute("type");
				
				// check if the class exists during compile time				
				Class oExists = null;
				try{
				
					oExists = Class.forName(sType);
					
				}catch(java.lang.ClassNotFoundException e){
				
					// if it does not exist
					codotos.exceptions.TagInterpreterException oException = new codotos.exceptions.TagInterpreterException("Tag attribute definition has type '"+ sType +"' which does not exist",TagTranslator.getRawXML(oElement,2));
			
					oException.initCause(e);
					
					throw oException;
					
				}
				
			}
			
			// get the "required" attribute
			Boolean bRequired = false;			
			if(oElement.hasAttribute("required")){
			
				String sRequired = oElement.getAttribute("required");
				if(sRequired.equals("true")){
					bRequired=true;
				}
			}
			
			// If the "default" attribute is given
			String sDefault = "null";
			if(oElement.hasAttribute("default")){
			
				// If this tag is required, it can't have a default value
				if(bRequired){
				
					throw new codotos.exceptions.TagInterpreterException("Tag attribute definition cannot have a default value if it is required.",TagTranslator.getRawXML(oElement,2));
					
				}
				
				// Grab the default value
				sDefault = "\""+ oElement.getAttribute("default") +"\"";
			}
			
			this.outputNewLineTabs(oAttributesString);
			oAttributesString.append("aTagAttributes.put(\""+ sName +"\",new codotos.tags.TagAttribute(\""+ sName +"\",\""+ sType +"\","+ Boolean.toString(bRequired) +","+ sDefault +"));");
		
		}
		
		this.iDepthCount--;
		
		return aAttributeNodes.size();
		
	}

	
	/*
		Compile the given XML node and write it to the provided file pointer
		
		@param oNode DOMNode XML DOMNode to compile
		@param oBufferedOutput FilePointer File pointer to the compiled file
		
		@return void
	*/
	private void compileNodeXML(Node oNode,BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TagInterpreterException {
		
		NodeList aChildNodes = oNode.getChildNodes();
		
		// Loop through each childnode of the given domnode
		for(int i=0,len=aChildNodes.getLength(); i<len; i++){
			
			// Make sure its an element node
			Node oChildNode = aChildNodes.item(i);			
			if(oChildNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element oChildElement = (Element) oChildNode;
			
			this.outputNewLineTabs(oBufferedOutput);
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("// Node ("+ oChildElement.getTagName() +")");
			
			// if its a <fel:...> node
			if(oChildElement.getPrefix().equals(TagTranslator.BUILT_IN_TAG)){
				
				// <fel:tagdir/> or <fel:taglib/>
				if(oChildElement.getLocalName().equals("tagdir") || oChildElement.getLocalName().equals("taglib")){
					
					// We already used these whne parsing the XML and adding a namespace
					continue;
					
				// <fel:defineAttribute name="output" required="true" type="String"/>
				}else if(oChildElement.getLocalName().equals("defineAttribute")){
					
					// We already used these whne parsing the XML
					continue;
				
				// <fel:text>Blah</fel:text>
				// TODO - Should this evaluate the text as an expression?
				}else if(oChildElement.getLocalName().equals("text")){
					
					// No text node child
					if(!oChildElement.hasChildNodes())
						continue;
					
					this.outputNewLineTabs(oBufferedOutput);
					oBufferedOutput.write("sToReturn.append("+ this.analyzeString(oChildElement.getFirstChild().getTextContent()) +");");
				
				// <fel:attribute name="key" value="boo"/>
				}else if(oChildElement.getLocalName().equals("attribute")){
					
					throw new codotos.exceptions.TagInterpreterException("<fel:attribute/> in incorrect position", TagTranslator.getRawXML((Element) oNode,2));
				
				// <fel:body>...</fel:body>
				}else if(oChildElement.getLocalName().equals("body")){
				
					throw new codotos.exceptions.TagInterpreterException("<fel:body/> in incorrect position", TagTranslator.getRawXML((Element) oNode,2));
				
				// <fel:doBody>
				}else if(oChildElement.getLocalName().equals("doBody")){
					
					if(oChildElement.hasChildNodes()){
						throw new codotos.exceptions.TagInterpreterException("<fel:doBody/> must be a self-closing tag", TagTranslator.getRawXML((Element) oNode,2));
					}
					
					this.outputNewLineTabs(oBufferedOutput);
					oBufferedOutput.write("sToReturn.append(this.getTagContext().getTag().doBody());");
				
				}else{
				
					throw new codotos.exceptions.TagInterpreterException("Tag does not exist", TagTranslator.getRawXML(oChildElement));
				
				}
			
			// If it is not a <fel:.../> node
			}else{
			
				// Increment our variable count
				this.iVariableCount++;
				
				// Start try/catch
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write("try{");
				
				String sTagVariableName = "tag"+ this.iVariableCount;
				String sClassName = this.mUsedTagLibs.get(oChildElement.getPrefix()).getElementFullClassName(oChildElement.getLocalName());
				
				// Create the new tag object
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sClassName +" "+ sTagVariableName +" = new "+ sClassName +"();");
				
				// Set the tags context
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sTagVariableName +".setContext(this.getContext());");
				
				// Set the tags parent
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sTagVariableName +".setParent(this.getTagContext().getTag());");
				
				// For each inline attribute on the node
				NamedNodeMap aAttributes = oChildElement.getAttributes();
				for(int j=0,lenj=aAttributes.getLength(); j<lenj; j++){
				
					Attr oAttribute = (Attr) aAttributes.item(j);
					
					// Set the attribute on the created tag
					this.outputNewLineTabs(oBufferedOutput);
					oBufferedOutput.write(sTagVariableName +".setAttribute(\""+ CompilerUtils.sanitizeString(oAttribute.getName()) +"\","+ this.analyzeString(oAttribute.getValue()) +");");
					
				
				}
				
				// Compile the inside of the DOMNode, if it has a body
				this.compileBodyXML(sTagVariableName,oChildElement,oBufferedOutput);				
				
				// execute the tag, grab its return value
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write("sToReturn.append("+ sTagVariableName +".doTag());");
				
				// catch our error, give it the raw xml
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write("}catch(codotos.exceptions.TagRuntimeException e){");
				oBufferedOutput.write("e.setRawXML(\""+ CompilerUtils.sanitizeString(TagTranslator.getRawXML(oChildElement,2)) +"\");");
				oBufferedOutput.write("throw e;");
				oBufferedOutput.write("}");
			
			}
		
		}
		
	}
	
	
	/*
		Determine if a DOMNode has elements (and not just textNodes, etc)
		
		@param oXML DOMNode XML DOMNode to check for elements
		
		@return Boolean True if the node has child elements (not nodes, elements)
	*/
	private static Boolean NodeHasChildElements(Node oNode){
		
		// Look for a regular DOMNode in the childNodes
		NodeList aChildNodes = oNode.getChildNodes();
		
		// Loop through each childnode of the given domnode
		for(int i=0,len=aChildNodes.getLength(); i<len; i++){
			
			// Make sure its an element node
			Node oChildNode = aChildNodes.item(i);			
			if(oChildNode.getNodeType() == Node.ELEMENT_NODE){
				return true;
			}
			
		}
		
		// Did not find one
		return false;
	
	}
	
	
	/*
		Similar to getElementsByTagName() but only retrieves childNodes that match the tagName
		
		@param oXML DOMNode XML DOMNode to check for elements
		@param sTagName String Tag name to look for (Asterisk to include all)
		
		@return Array Array of DOMNodes
	*/
	private static ArrayList<Node> getChildNodesByTagname(Node oNode,String sTagName){
	
		ArrayList<Node> aToReturn = new ArrayList<Node>();
		
		NodeList aChildNodes = oNode.getChildNodes();
		
		// Loop through each childnode
		for(int i=0,len=aChildNodes.getLength(); i<len; i++){
			
			// Make sure its an element node
			Node oChildNode = aChildNodes.item(i);			
			if(oChildNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			Element oChildElement = (Element) oChildNode;
			
			if(oChildElement.getTagName().equals(sTagName) || sTagName.equals("*")){
				aToReturn.add(oChildElement);
			}
			
		}
		
		return aToReturn;
	
	}
	
	
	/*
		Used to compile the body of a tag (<some:tag>...BODY IS HERE...</some:tag>)
		
		@param sTagVariableName String Name of the tag variable
		@param oXML DOMNode XML DOMNode whos body to compile
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void compileBodyXML(String sTagVariableName,Element oNode,BufferedWriter oBufferedOutput) throws java.io.IOException,codotos.exceptions.TagInterpreterException {
		
		// If the node has no child node elements, dont do anything
		if(!TagTranslator.NodeHasChildElements(oNode)){
			return;
		}
		
		// Get the <fel:attribute/> & <fel:body/> childnode tags, if they exist
		ArrayList<Node> aFelAttributeNodes = TagTranslator.getChildNodesByTagname(oNode,"fel:attribute");
		ArrayList<Node> aFelBodyNodes = TagTranslator.getChildNodesByTagname(oNode,"fel:body");
		
		// Compile the <fel:attribute> tags
		this.compileAttributeFragments(sTagVariableName,aFelAttributeNodes,oBufferedOutput);
		
		// Sanity Check
		if(aFelBodyNodes.size()>1){			
			throw new codotos.exceptions.TagInterpreterException("More than 1 <fel:body> included in a tag",TagTranslator.getRawXML(oNode,2));
		}
		
		// If <fel:body> specified
		if(aFelBodyNodes.size()==1){
			
			// Compile the body fragment using the <fel:body> as the node
			this.compileBodyFragmentXML(sTagVariableName,aFelBodyNodes.get(0),oBufferedOutput);
		
		// If no <fel:body> specified
		}else{
		
			// If there was no body specified, and we didnt have attributes, just treat it like normal
			if(aFelAttributeNodes.size()==0){
			
				// Compile the body fragment using the tag itself as the node
				this.compileBodyFragmentXML(sTagVariableName,oNode,oBufferedOutput);
				
			}
		
		}
		
		// If <fel:body/> or <fel:attribute/> is specified, ensure that no other childnodes exist that are not <fel:attribute> type
		if( ( aFelBodyNodes.size() !=0 || aFelAttributeNodes.size() !=0 ) && TagTranslator.getChildNodesByTagname(oNode,"*").size() > ( aFelBodyNodes.size() + aFelAttributeNodes.size() )){
			
			throw new codotos.exceptions.TagInterpreterException("No other tags allowed inside a tag if <fel:attribute/> or <fel:body/> are specified.",TagTranslator.getRawXML(oNode,2));
			
		}
	
	}
	
	
	/*
		Used to compile <fel:attribute/> elements
		
		@param sTagVariableName String Name of the tag variable
		@param aFelAttributes Array Array of attribute DOMNodes
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void compileAttributeFragments(String sTagVariableName,ArrayList<Node> aFelAttributeNodes,BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TagInterpreterException {
		
		int iAttrCount = 0;
		
		// For each <fel:attribute/>
		for(Node oNode : aFelAttributeNodes){
			
			Element oElement = (Element) oNode;
			
			// create the variable name we will use for our fragment
			String sFragVariableName = sTagVariableName +"_attrFrag"+ iAttrCount++;
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("// Fragment Attribute");
			
			// See if the value tag was specified
			if(oElement.hasAttribute("value")){
			
				// If value tag is specified, <fel:attribute> should be self closing
				if(oElement.hasChildNodes()){
					throw new codotos.exceptions.TagInterpreterException("<fel:attribute/> must be a self-closing tag if \"value\" attribute is supplied", TagTranslator.getRawXML(oElement,2));
				}
				
				// set the attribute value equal to the data supplied in the 'value' attribute (could be EL)
				this.iDepthCount --;
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sTagVariableName +".setAttribute(\""+ oElement.getAttribute("name") +"\","+ TagTranslator.analyzeString(oElement.getAttribute("value")) +");");
				
			
			// value of tag is based on the body of the tag
			}else{
				
				// create the fragment object
				this.compileFragment(sFragVariableName,oNode,oBufferedOutput);
				
				// set the attribute value equal to the data returned from the attribute fragment objects invoke() method
				this.iDepthCount --;
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sTagVariableName +".setAttribute(\""+ oElement.getAttribute("name") +"\","+ sFragVariableName +".invoke());");
			
			}
		
		}
	
	}
	
	
	/*
		Used to compile the body of a tag or <fel:body/> elements
		
		@param sTagVariableName String Name of the tag variable
		@param oDOMNode DOMNode XML DOMNode whos body we will compile
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void compileBodyFragmentXML(String sTagVariableName,Node oNode,BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TagInterpreterException {
		
		// name of our fragment variable
		String sFragVariableName = sTagVariableName +"_bodyFrag";
		
		this.iDepthCount++;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("// Fragment Body");
		
		// create the fragment object
		this.compileFragment(sFragVariableName,oNode,oBufferedOutput);
		
		// set the body equal to the data returned from the body fragment objects invoke() method
		this.iDepthCount--;		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write(sTagVariableName +".setBody("+ sFragVariableName +");");
	
	}
	
	
	private void compileFragment(String sVariableName, Node oNode, BufferedWriter oBufferedOutput) throws java.io.IOException, codotos.exceptions.TagInterpreterException {
		
		// create the fragment object
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("codotos.tags.TagFragment "+ sVariableName +" = new codotos.tags.TagFragment(this.getContext(),this.getTagContext()){");
		
		this.iDepthCount += 2;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public String invoke() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {");
		
		// Setup our return string
		this.iDepthCount++;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("StringBuilder sToReturn = new StringBuilder();");
		
		// Compile the XML inside the fragment
		this.compileNodeXML(oNode,oBufferedOutput);
		
		// Return our string
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("return sToReturn.toString();");
		
		this.iDepthCount--;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("}");
		
		this.iDepthCount--;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("};");
	
		this.iDepthCount--;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
		Retrieved a map of used tag elements, where the key is the element with the prefix and the value is an object containing additional information
		
		@param oDOMDocument DOMDocument XML DOM Document that contains all the tags
		@param mTags HashMap<String,TagCompilerLibItem> Map of defined taglibs
		
		@return null
	*/
	private static HashMap<String,TagCompilerLibItem> getUsedTagLibs(Document oDOMDocument,HashMap<String,TagCompilerLibItem> mTags){
		
		// Map of used tags
		HashMap<String,TagCompilerLibItem> mUsedTagLibs = new HashMap<String,TagCompilerLibItem>();
		
		// Remove the "fel" taglib since its built in and cant be compiled
		mTags.remove(TagTranslator.BUILT_IN_TAG);
		
		// Loop through each defined taglib
		Iterator oIterator = mTags.values().iterator();
		while(oIterator.hasNext()){
			
			TagCompilerLibItem oTagLib = (TagCompilerLibItem) oIterator.next();
			
			// Look for all elements that have the taglib prefix
			NodeList aTagNodes = oDOMDocument.getElementsByTagNameNS(oTagLib.getNamespace(),"*");
			
			Boolean libUsed = false;
			
			// Go through each element
			for(int i=0,len=aTagNodes.getLength(); i<len; i++){
				libUsed = true;
				Element oTagElement = (Element) aTagNodes.item(i);
				oTagLib.addElement(oTagElement.getLocalName());			
			}
			
			// if lib has no elements in this page, dont even use it
			if(!libUsed){
				oIterator.remove();
			}
		}
		
		// Return the map
		return mTags;
		
	}
	
	
	/*
		Retrieves an array of strings that are the file locations of the .tag files this tag relies on
		
		@return List<String> List of .tag files this tag uses
	*/
	private static java.util.List<String> getUsedRawTags(HashMap<String,TagCompilerLibItem> mUsedTagLibs){
		
		java.util.List<String> aToReturn = new java.util.ArrayList<String>();
		
		// Loop through the map of used tag libs
		Iterator oIterator = mUsedTagLibs.values().iterator();
		while(oIterator.hasNext()){
		
			TagCompilerLibItem oTagLib = (TagCompilerLibItem) oIterator.next();
			
			// We are only interested in the tag dir's (.tag files)
			if(!oTagLib.isTagDir())
				continue;
			
			// Get the base package folder for this taglib
			String sBasePackage = oTagLib.getPackageFolder();
			
			// Loop through each of the elements
			for(String sElementName : oTagLib.getElements()){
			
				// add the file location
				aToReturn.add(sBasePackage +"/"+ sElementName +".tag");
				
			}
		
		}
	
		return aToReturn;
	
	}
	
	
	/*
		Used to analyze a provided string expression and turn it into code
		
		@param sRawString String String to be converted into code
		
		@return String Analyzed string
	*/
	private static String analyzeString(String sRawString){
		
		// NOTE: This is similar to TemplateCompiler.php code
		
		// Check for ${variables}
		Pattern oPattern = Pattern.compile("\\$\\{.*\\}");
		Matcher oMatcher = oPattern.matcher(sRawString);
		
		// If it does not contain a variable inside, just output the string
		if(!oMatcher.find()){
			return "\""+ CompilerUtils.sanitizeString(sRawString) +"\"";
		}
		
		return "codotos.tags.Expression.evaluate(\""+ CompilerUtils.sanitizeString(sRawString) +"\",this.getTagContext())";
	
	}
	
	
	/*
		Used to turn a node object into its raw XML equivelant
		
		@param oElement Element The Element to turn into a raw XML String
		
		@return String Raw XML String
	*/
	public static String getRawXML(Element oElement,int iNodeDepth){
		
		StringBuilder sToReturn = new StringBuilder("<");
		
		sToReturn.append(oElement.getTagName());
		
		NamedNodeMap mAttributes = oElement.getAttributes();
		
		for(int i=0,len=mAttributes.getLength(); i<len; i++){
		
			Attr oAttribute = (Attr) mAttributes.item(i);
			
			sToReturn.append(" ");
			sToReturn.append(oAttribute.getName());
			sToReturn.append("=\"");
			sToReturn.append(oAttribute.getValue());
			sToReturn.append("\"");
		
		}		
		
		if(!oElement.hasChildNodes()){
		
			sToReturn.append("/>");
			
		}else{
			sToReturn.append(">");
			
			if(iNodeDepth <= 1){
				sToReturn.append("...");
			}else{
				iNodeDepth--;
				
				ArrayList<Node> aChildNodes = TagTranslator.getChildNodesByTagname(oElement,"*");
				
				for(int i=0,len=aChildNodes.size(); i<len; i++){
		
					// each childNode
					sToReturn.append(TagTranslator.getRawXML((Element) aChildNodes.get(i),iNodeDepth));
				
				}
				
			}
			
			sToReturn.append("</");
			sToReturn.append(oElement.getTagName());
			sToReturn.append(">");
		}
		
		return sToReturn.toString();
	
	}
	
	
	/*
		Default to 1
	*/
	public static String getRawXML(Element oElement){
		return TagTranslator.getRawXML(oElement,1);
	}



}

