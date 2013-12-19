package codotos.tags;


import codotos.utils.CompilerUtils;
import codotos.utils.CacheUtils;
import codotos.Constants;


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


public class TagCompiler {

	
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
	
	
	/*
		Array of fragment nodes that need to be output after the base class declaration
		TODO: Traditionally it would be an inline object created, but PHP does not support this
	*/
	private ArrayList<Node> aFragmentNodes = new ArrayList<Node>();

	
	/*
		Compiles the tag from a raw file to a tag class file
		
		@param sFullClassName String Full classname for this tag
		@param sRawFileName String File location of the raw uncompiled file
		
		@return void
	*/
	public void compile(String sFullClassName,String sRawFileLoc) throws java.lang.Exception {
		
		// Reset these
		this.iVariableCount = 0;
		this.iDepthCount = 0;
		
		CompilerUtils.createPackageFolders(sFullClassName);
		
		String sCompiledFileName = CompilerUtils.getClassFileLocation(sFullClassName);
		
		// Open the compiled file for writing
		FileWriter oOutputStream = new FileWriter(sCompiledFileName);
		BufferedWriter oBufferedOutput = new BufferedWriter(oOutputStream);
		
		try{
			
			// Get the XML Document Node from the xml file
			Node oNode = this.getNode(sRawFileLoc);
			
			// Add the header
			this.header(sFullClassName,oNode,oBufferedOutput);
			
			// Evaluate the xml nodes & compile them
			this.compileNodeXML(oNode,oBufferedOutput);
			
			// Add the footer
			this.footer(oBufferedOutput);
			
			// Close the compiled file
			oBufferedOutput.close();
			
			// Compile our used taglib/dirs
			TagCompiler.compileUsedTaglibs(this.mUsedTagLibs);		
			
			// If all has gone well, compile the actual file
			com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
			String[] args = new String[]{ "-d", Constants.COMPILED_ROOT, sCompiledFileName };
			int status = javac.compile(args);
			
			// Status of 1 means error compiling
			if(status!=0){
			
				throw new java.lang.Exception("Error compiling file '"+ sCompiledFileName +"'");
			
			}
		
		// if an error occured while compiling ...
		}catch(java.lang.Exception e){
		
			// Close the compiled file, otherwise we can't delete it
			oBufferedOutput.close();
			
			// Delete the original file, so next time it tries to recreate it.
			// Otherwise it will think the existing .java file was created after the raw .tag file and not try to recompile
			File oCompiledFile = new File(sCompiledFileName);
			oCompiledFile.delete();
			
			// Rethrow our error so it trickles up
			throw e;
		
		}
	
	}

	
	/*
		Retrieves the base DOMNode of an XML file, adding the appropriate namespaces based on the taglib/tagdirs provided
		
		@param sRawFileName String File location of the raw uncompiled xml file
		
		@return Node Base Node for the tag
	*/
	private Node getNode(String sRawFileLoc) throws java.lang.Exception, java.lang.Exception {
	
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
		
			// Get a map of used tag elements so we can compile/include those later
			this.mUsedTagLibs = this.getUsedTagLibs(oTagDocument,mDefinedTagLibs);
			
			// Return the base DOMNode
			return oTagDocument.getFirstChild();
		
		}catch(javax.xml.parsers.ParserConfigurationException e){
			
			throw new java.lang.Exception("javax.xml.parsers.ParserConfigurationException");
		
		}catch(org.xml.sax.SAXException e){
			
			throw new java.lang.Exception("org.xml.sax.SAXException");
		
		}catch(java.io.IOException e){
			
			throw new java.lang.Exception("java.io.IOException");
		
		}
		
	}

	
	/*
		Open the raw XML file, retrieve a map of included taglibs/tagdirs
		
		@param sRawFileName String File location of the raw uncompiled file
		
		@return Map Map of included taglibs/tagdirs, with the key being the supplied prefix
	*/
	private HashMap<String,TagCompilerLibItem> getIncludedTaglibs(String sRawFileLoc) throws java.lang.Exception {
		
		// Map that will hold the different taglib/tagdirs, key will be based on the provided tag prefix/namespace
		HashMap<String,TagCompilerLibItem> mIncludedTaglibs = new HashMap<String,TagCompilerLibItem>();
		
		// Note: Built in taglib "fel"
		mIncludedTaglibs.put(TagCompiler.BUILT_IN_TAG,new TagCompilerLibItem(TagCompiler.BUILT_IN_TAG,"codotos.tags.TODO",false));
		
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
			
			// TODO -- I believe the SRC attribute here is not consistent enough, will they always find a match based on the src name?  Like, if I add this tag on another page with a different src, the name will be biffed right?
			// For each <fel:taglib/> node
			NodeList aTagLibNodes = oTagDocument.getElementsByTagName(TagCompiler.BUILT_IN_TAG+":taglib");
			for(int i=0,len=aTagLibNodes.getLength(); i<len; i++){
				
				Element oTagLibElement = (Element) aTagLibNodes.item(i);
				
				// Add the taglib to our map
				mIncludedTaglibs.put(oTagLibElement.getAttribute("prefix"),new TagCompilerLibItem(oTagLibElement.getAttribute("prefix"),oTagLibElement.getAttribute("src"),false));
				
			}
			
			// For each <fel:tagdir/> node
			NodeList aTagDirNodes = oTagDocument.getElementsByTagName(TagCompiler.BUILT_IN_TAG+":tagdir");
			for(int i=0,len=aTagDirNodes.getLength(); i<len; i++){
				
				Element oTagDirElement = (Element) aTagDirNodes.item(i);
				
				// Add the tagdir to our map
				mIncludedTaglibs.put(oTagDirElement.getAttribute("prefix"),new TagCompilerLibItem(oTagDirElement.getAttribute("prefix"),oTagDirElement.getAttribute("src"),true));
			
			}
		
		}catch(javax.xml.parsers.ParserConfigurationException e){
			
			throw new java.lang.Exception("javax.xml.parsers.ParserConfigurationException");
		
		}catch(org.xml.sax.SAXException e){
			
			throw new java.lang.Exception("org.xml.sax.SAXException");
		
		}catch(java.io.IOException e){
			
			throw new java.lang.Exception("java.io.IOException");
		
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
	private static String addXMLNamespaces(String sRawFileLoc,HashMap<String,TagCompilerLibItem> mNamespaces) throws java.lang.Exception {
		
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
		
		}catch(java.io.FileNotFoundException e){
			
			throw new java.lang.Exception("java.io.FileNotFoundException");
		
		}catch(java.io.IOException e){
			
			throw new java.lang.Exception("java.io.IOException");
		
		}
	
		sToReturn.append("</tag>");
		
		
		// Output the actual XML
		return sToReturn.toString();
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	/*
		Add a newline & the correct number of tabs to the provided file
		
		@param oBufferedOutput FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void outputNewLineTabs(BufferedWriter oBufferedOutput) throws java.io.IOException{
	
		oBufferedOutput.write("\n");
		
		for(int i=0,len=this.iDepthCount; i<len; i++){
			oBufferedOutput.write("\t");
		}
		
	}

	
	/*
		Output the header for the class
		
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void header(String sFullClassName, Node oNode, BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
		
		oBufferedOutput.write("package "+ CompilerUtils.getPackageName(sFullClassName) +";");
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("import codotos.tags.Tag;");
		
		// TODO - Dynamic Attributes inclusion
		//oBufferedOutput.write("import codotos.tags.TagDynamicAttributes;");
		
		// TODO - Intelligently include this?
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("import codotos.tags.TagFragment;");
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("import java.lang.StringBuilder;");
		
		// Define our tag class
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public final class "+ CompilerUtils.getClassName(sFullClassName) +" extends Tag {");
		this.iDepthCount++;
		
		// Define our constructor
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public "+ CompilerUtils.getClassName(sFullClassName) +"() throws java.lang.Exception {super();}");
		
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
			
			// TODO
			for(int i=0,len=aUsedRawTagFiles.size(); i<len; i++){
			
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write("aTagResources.add(\""+ aUsedRawTagFiles.get(i) +"\");");
			
			}
			
			this.iDepthCount--;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("}");
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected final java.util.List<String> getTagResources(){");
			
			this.iDepthCount++;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("return aTagResources;");			
			this.iDepthCount--;
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("}");
		
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected Boolean isTagResourcesAlreadyChecked(){");
			
			this.iDepthCount++;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("return this.bCheckedTagResources;");			
			this.iDepthCount--;
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("}");
		
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("protected void setTagResourcesAlreadyChecked(){");
			
			this.iDepthCount++;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("this.bCheckedTagResources=true;");			
			this.iDepthCount--;
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("}");
			
		}	
	
		// Define our attributes
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public final void defineAttributes() throws java.lang.Exception {");
		
		this.defineAttributes(oNode,oBufferedOutput);
		
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("}");	
		
		// Define our method
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public final String output() throws java.lang.Exception{");
		
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
		
		// Compile the fragments (outside of the main class declaration, since php does not support classes inside classes)
		// TODO
		//this.compileFragments(oBufferedOutput);
		
	}
	
	
	
	/*
		Outputs the attribute definitions
		
		@param oNode DOMNode XML DOMNode to compile
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return void
	*/
	private void defineAttributes(Node oNode,BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
	
		this.iDepthCount++;
		
		ArrayList<Node> aAttributeNodes = this.getChildNodesByTagname(oNode,BUILT_IN_TAG+":defineAttribute");
		
		for(int i=0,len=aAttributeNodes.size(); i<len; i++){
		
			Element oElement = (Element) aAttributeNodes.get(i);
			
			// <fel:defineAttribute name="output" required="true" type="String"/>
			// <fel:defineAttribute name="userFeedback" required="true" type="UserFeedbackBean"/>
			
			// get the "name" attribute
			String sName = oElement.getAttribute("name");			
			if(!oElement.hasAttribute("name") || sName.isEmpty()){
			
				throw new java.lang.Exception("<fel:defineAttribute> requires a 'name' attribute");
				
			}
			
			// get the type attribute
			String sType = "java.lang.String";
			if(oElement.hasAttribute("type")){
				
				sType = oElement.getAttribute("type");
				
				// TODO - Find a better way, this is SLOW!
				// check if the class exists
				Class oExists = null;
				try{
					oExists = Class.forName(sType);
				}catch(java.lang.ClassNotFoundException e){
					// Does not exist!
				}
				
				// if it does not exist
				if(oExists==null){
				
					throw new java.lang.Exception("<fel:defineAttribute name=\""+ sName +"\"> has type '"+ sType +"' which does not exist");
					
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
			
			// get the "default" attribute (if it isnt required)
			String sDefault = "null";
			if(!bRequired && oElement.hasAttribute("default")){
				sDefault = "\""+ oElement.getAttribute("default") +"\"";
			}
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("this.defineAttribute(\""+ sName +"\",\""+ sType +"\","+ Boolean.toString(bRequired) +","+ sDefault +");");
		
		}
		
		this.iDepthCount--;
		
	}

	
	/*
		Compile the given XML node and write it to the provided file pointer
		
		@param oNode DOMNode XML DOMNode to compile
		@param oBufferedOutput FilePointer File pointer to the compiled file
		
		@return void
	*/
	private void compileNodeXML(Node oNode,BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
		
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
			if(oChildElement.getPrefix().equals(TagCompiler.BUILT_IN_TAG)){
				
				// <fel:tagdir/> or <fel:taglib/>
				if(oChildElement.getLocalName().equals("tagdir") || oChildElement.getLocalName().equals("taglib")){
					
					// We already used these whne parsing the XML and adding a namespace
					continue;
					
				// <fel:defineAttribute name="output" required="true" type="String"/>
				}else if(oChildElement.getLocalName().equals("defineAttribute")){
					
					// We already used these whne parsing the XML
					continue;
				
				// <fel:text>Blah</fel:text>
				}else if(oChildElement.getLocalName().equals("text")){
					
					// TODO - Should this evaluate things?
					this.outputNewLineTabs(oBufferedOutput);
					
					// No text node child
					if(!oChildElement.hasChildNodes())
						continue;
					
					oBufferedOutput.write("sToReturn.append("+ this.analyzeString(oChildElement.getFirstChild().getTextContent()) +");");
				
				// <fel:attribute name="key" value="boo"/>
				}else if(oChildElement.getLocalName().equals("attribute")){
					
					throw new java.lang.Exception("<fel:attribute/> in incorrect position");
				
				// <fel:body>...</fel:body>
				}else if(oChildElement.getLocalName().equals("body")){
				
					throw new java.lang.Exception("<fel:body/> in incorrect position");
				
				}else{
				
					throw new java.lang.Exception("<"+ oChildElement.getTagName() +" /> does not exist");
				
				}
			
			// If it is not a <fel:.../> node
			}else{
			
				// Increment our variable count
				this.iVariableCount++;
				
				String sTagVariableName = "tag"+ this.iVariableCount;
				String sClassName = this.mUsedTagLibs.get(oChildElement.getPrefix()).getElementFullClassName(oChildElement.getLocalName());
				
				// Create the new tag object
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sClassName +" "+ sTagVariableName +" = new "+ sClassName +"();");
				
				// Set the tags context
				this.outputNewLineTabs(oBufferedOutput);
				oBufferedOutput.write(sTagVariableName +".setContext(this.getContext());");
				
				// Set the tags parent
				// TODO - parent should actually be the parent tag, pageContext should contain a reference to the main page
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
		@param sTagName String Tag name to look for
		
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
			
			if(oChildElement.getTagName().equals(sTagName)){
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
	private void compileBodyXML(String sTagVariableName,Element oNode,BufferedWriter oBufferedOutput) throws java.io.IOException,java.lang.Exception {
		
		// If the node has no child node elements, dont do anything
		if(!TagCompiler.NodeHasChildElements(oNode)){
			return;
		}
		
		// Get the <fel:attribute/> & <fel:body/> childnode tags, if they exist
		ArrayList<Node> aFelAttributeNodes = TagCompiler.getChildNodesByTagname(oNode,"fel:attribute");
		ArrayList<Node> aFelBodyNodes = TagCompiler.getChildNodesByTagname(oNode,"fel:body");
		
		// Compile the <fel:attribute> tags
		// TODO - do something with the return
		this.compileAttributeFragments(sTagVariableName,aFelAttributeNodes,oBufferedOutput);
		
		// Sanity Check
		if(aFelBodyNodes.size()>1){			
			throw new java.lang.Exception("More than 1 <fel:body> included in a tag");
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
				
			}else{
			
				// TODO - Check if oDOMNode has childNodes outside of <fel:body> & <fel:attribute>, if they do, tell the user thye should use <fel:body> when using <fel:attribute/>
				
			}
			
		}
	
	}
	
	
	/*
		Used to compile <fel:attribute/> elements
		
		@param sTagVariableName String Name of the tag variable
		@param aFelAttributes Array Array of attribute DOMNodes
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void compileAttributeFragments(String sTagVariableName,ArrayList<Node> aFelAttributeNodes,BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
		
		int iAttrCount = 0;
		
		// For each <fel:attribute/>
		for(Node oNode : aFelAttributeNodes){
			
			Element oElement = (Element) oNode;
			
			// create the variable name we will use for our fragment
			String sFragVariableName = sTagVariableName +"_attrFrag"+ iAttrCount++;
			
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write("// Fragment Attribute");
			
			// create the fragment object
			this.compileFragment(sFragVariableName,oNode,oBufferedOutput);
			
			// set the attribute value equal to the data returned from the attribute fragment objects invoke() method
			this.iDepthCount --;
			this.outputNewLineTabs(oBufferedOutput);
			oBufferedOutput.write(sTagVariableName +".setAttribute(\""+ oElement.getAttribute("name") +"\","+ sFragVariableName +".invoke());");
		
		}
	
	}
	
	
	/*
		Used to compile the body of a tag or <fel:body/> elements
		
		@param sTagVariableName String Name of the tag variable
		@param oDOMNode DOMNode XML DOMNode whos body we will compile
		@param oFpCompiled FilePointer File pointer to the compiled file
		
		@return null
	*/
	private void compileBodyFragmentXML(String sTagVariableName,Node oNode,BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
		
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
	
	
	private void compileFragment(String sVariableName, Node oNode, BufferedWriter oBufferedOutput) throws java.io.IOException, java.lang.Exception {
		
		// create the fragment object
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("TagFragment "+ sVariableName +" = new TagFragment(this.getContext(),this.getTagContext()){");
		
		this.iDepthCount += 2;
		this.outputNewLineTabs(oBufferedOutput);
		oBufferedOutput.write("public String invoke() throws java.lang.Exception {");
		
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
		mTags.remove(TagCompiler.BUILT_IN_TAG);
		
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
		Given a map of used tag elements, make sure each taglib file is compiled & up-to-date
		
		@param mUsedTagLibs Map of used tags
		
		@return void
	*/
	private static void compileUsedTaglibs(HashMap<String,TagCompilerLibItem> mUsedTagLibs) throws java.lang.Exception { 
		
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
				
				// Check if the files exist...
				File oRawFile = new File(sRawFileLoc);
				if(!oRawFile.exists()){
					throw new java.lang.Exception("Tag '"+ oTagLib.getPrefix() +":"+ sElementName +"' included but file '"+ sRawFileLoc  +"' not found.");
				}
			
				// If the compiled version is old (or does not exist)
				if(!CacheUtils.isCacheCurrent(sRawFileLoc,sCompiledFileLoc)){
				
					if(oTagCompiler == null){
						oTagCompiler = new TagCompiler();
					}
					
					String sFullClassName = oTagLib.getElementFullClassName(sElementName);
					
					// Compile the tag
					oTagCompiler.compile(sFullClassName,sRawFileLoc);
					
				}
			
			}
			
		}
	
	}
	
	
	public static void compileTagDirFile(String sRelTagDirFile) throws java.lang.Exception {
		
		// Get the raw file name
		String sRawFileLoc = Constants.ROOT + sRelTagDirFile;
		
		// Get the compiled file location
		// TODO - Fix this, there is WAY too much going on here.
		String sFullClassName = TagCompilerLibItem.getCompiledFileNameFromRawFileName(sRelTagDirFile);
		sFullClassName = CompilerUtils.getPackageName(sFullClassName) +"."+ TagCompilerLibItem.getFixedTagDirClassName(CompilerUtils.getClassName(sFullClassName));
		
		String sCompiledFileLoc = CompilerUtils.getClassFileLocation(sFullClassName);
		
		// TagCompiler, used to compile each tag, reusable
		TagCompiler oTagCompiler = null;
		
		// If the compiled version is old (or does not exist)
		if(!CacheUtils.isCacheCurrent(sRawFileLoc,sCompiledFileLoc)){
			
			if(oTagCompiler == null){
				oTagCompiler = new TagCompiler();
			}
			
			// Compile the tag
			oTagCompiler.compile(sFullClassName,sRawFileLoc);
			
		}
		
	}
	
	
	/*
		Used to analyze a provided string expression and turn it into code
		NOTE: This will be replaced with the translator
		
		@param sRawString String String to be converted into code
		
		@return String Analyzed string
	*/
	private static String analyzeString(String sRawString){
		
		// NOTE: This is similar to TemplateCompiler.php code
		// TODO - This pattern is assuming ^ & $, wont match if theres beginning or ending chars
		
		// Check for ${variables}
		Pattern oPattern = Pattern.compile("\\$\\{.*\\}");
		Matcher oMatcher = oPattern.matcher(sRawString);
		
		// If it does not contain a variable inside, just output the string
		if(!oMatcher.find()){
			return "\""+ CompilerUtils.sanitizeString(sRawString) +"\"";
		}
		
		return "(new codotos.tags.Expression(\""+ CompilerUtils.sanitizeString(sRawString) +"\",this.getTagContext())).evaluate()";
	
	}


}

