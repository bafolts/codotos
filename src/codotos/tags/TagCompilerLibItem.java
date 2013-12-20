package codotos.tags;


import codotos.utils.CompilerUtils;
import codotos.tags.TagTranslator;
import codotos.Constants;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.BufferedWriter;


public class TagCompilerLibItem {


	private String sPrefix = null;


	private String sSource = null;
	
	
	private Boolean bIsTagDir = false;
	
	
	private ArrayList<String> aElements = new ArrayList<String>();
	
	
	public TagCompilerLibItem(String sPrefix,String sSource,Boolean bIsTagDir){
	
		this.sPrefix = sPrefix;
		this.sSource = sSource;
		this.bIsTagDir = bIsTagDir;
	
	}
	
	
	public void addElement(String sElementName){
		if(!this.aElements.contains(sElementName)){		
			this.aElements.add(sElementName);
		}
	}
	
	
	public Boolean isTagDir(){
		return this.bIsTagDir;
	}
	
	
	public String getPrefix(){
		return this.sPrefix;
	}
	
	
	public String getNamespace(){
		return this.sSource;
	}
	
	
	public String getPackageName(){
		if(this.isTagDir()){
			return this.getCompiledFileNameFromRawFileName(this.sSource+".tag");
		}else{
			return this.sSource;
		}
	}
	
	
	public String getPackageFolder(){
		if(this.isTagDir()){
			return this.sSource;
		}else{
			return null;
		}
	}
	
	
	public ArrayList<String> getElements(){
		return this.aElements;
	}
	
	
	// Taglibs are first-letter capitalized
	// TagDirs are like taglibs but prefixed with "fel_"
	public String getElementFullClassName(String sElementName){
		return this.getPackageName() +"."+ this.getElementName(sElementName);
	}
	
	
	public String getElementName(String sElementName){
		if(this.isTagDir()){
			return this.getFixedTagDirClassName(sElementName);
		}else{
			return Character.toUpperCase(sElementName.charAt(0)) + sElementName.substring(1);
		}
	}
	
	
	public static String getCompiledFileNameFromRawFileName(String sRelRawFileName){
		return "codotos.tags.generated." + CompilerUtils.getClassNameFromFileLocation(sRelRawFileName);
	}
	
	
	public static String getFixedTagDirClassName(String sClassName){
		return TagTranslator.PREFIX + CompilerUtils.getClassName(sClassName);		
	}

}