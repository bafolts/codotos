package codotos.resources;


import java.io.Serializable;


/*
	This class is a representation of a resource object
*/	
public class ResourceItem implements Serializable {

	
	/*
		Name of the resource
	*/
	private String sName = null;
	
	
	/*
		Resource is grammatical (meaning it has plural & singular values, not just one value)
	*/	
	private Boolean bIsGrammatical = false;
	
	
	/*
		Singular value (this is the value used if resource is not grammatical)
	*/
	private String sSingularValue = null;
	
	
	/*
		Plural value (not used if resource is not grammatical)
	*/
	private String sPluralValue = null;
	
	
	/*
		Setup our resource
		
		@param sName String Name of the resource
	*/
	public ResourceItem(String sName){
		this.sName = sName;
	}
	
	
	/*
		Set our non-grammatical resource value
		
		@param sValue String Resource value
	*/
	public void load(String sValue){
		
		this.sSingularValue = sValue;
	}
	
	
	/*
		Set our singular resource value
		
		@param sValue String Resource singular value
	*/
	public void loadSingular(String sValue){
		this.bIsGrammatical = true;
		this.sSingularValue = sValue;
	}
	
	
	/*
		Set our plural resource value
		
		@param sValue String Resource plural value
	*/
	public void loadPlural(String sValue){
		this.bIsGrammatical = true;
		this.sPluralValue = sValue;
	}
	
	
	/*
		Retrieve the resource value, with placeholder substitutions
		NOTE: This supports unlimited arguments
		
		@param String Substitution value
		
		@return String Resource value with placeholder substitutions
	*/
	public String getText(int iQty, String ... aPlaceholders){
		
		// TODO - Convert the int to a string and put it at the front of the placeholders array
		return this.getText(aPlaceholders);
		
	}
	
	public String getText(String ... aPlaceholders){
		
		String sValue = null;
		
		// if its not a grammatical resource, or it is grammatical and the first param is 1, we will use the singular value
		if(!this.bIsGrammatical || aPlaceholders[0].equals("1")){
		
			sValue = this.sSingularValue;
		
		// It is grammatical and plural
		}else{
		
			sValue = this.sPluralValue;
			
		}
		
		// Loop through the rest of the method arguments and substitute the placeholders with their data
		for(int i=0,len=aPlaceholders.length; i<len; i++){
			sValue = ResourceItem.replacePlaceholder(sValue,Integer.toString(i),aPlaceholders[i]);
		}
		
		// clean up the leftover {0} {9999} placeholders we did not substitute
		sValue = ResourceItem.replacePlaceholder(sValue,"\\d","");
		
		// Return the resource value with its substitutions
		return sValue;
		
	}
	
	
	/*
		Replace the placeholder text in a resource string with the given value
		
		@param sString String value to replace (PASSED BY REFERENCE)
		@param iNum Integer Placeholder to search/replace
		@param sPlaceholder String String to replace the placeholder with
		
		@return null
	*/
	static private String replacePlaceholder(String sString,String iNum,String sPlaceholder){
		
		return sString.replaceAll("\\{"+iNum+"\\}",ResourceItem.javaFixReplaceAll(sPlaceholder));
		
	}
	
	
	static private String javaFixReplaceAll(String sString){
	
		return sString.replaceAll("([\\$\\\\])","\\\\$0");
		
	}
	
	
}

