package codotos.tags;


import codotos.tags.Tag;
import codotos.context.Context;

import java.util.HashMap;


/*
	This is the context used in a tag file, it is responsible for keeping track of the current tag, parent tag, & variables
*/
public class TagContext {
	
	
	/*
		Tag associated with this tag context
	*/
	private Tag oTag = null;
	
	
	/*
		Map of variables associated with this tag context
	*/
	private HashMap<String,Object> mVariables = new HashMap<String,Object>();
	
	
	/*
		Setup our tag context
		
		@param oTag TagObject Tag whos context this belongs to
		
		@return null
	*/
	public TagContext(Tag oTag){
		
		// Set our tag
		this.oTag = oTag;

	}
	
	
	/*
		Get the tag associated with this tag context
		
		@return TagObject Tag whos context this belongs to
	*/
	public Tag getTag(){
		return this.oTag;
	}
	
	
	/*
		Set a variable thats available anywhere in the tag
		
		@param sName String Name of variable
		@param oValue Object Variable value
		
		@return null
	*/
	public void setVariable(String sName,Object oValue){
		this.mVariables.put(sName,oValue);
	}
	
	
	/*
		Removes a variable that has or has not been set
		
		@param sName String Name of variable
		
		@return null
	*/
	public void removeVariable(String sName){
		this.mVariables.remove(sName);
	}
	
	
	/*
		Checks if a tag variable exists
		
		@param sName String Name of variable
		
		@return Boolean True if the variable exists, False if not
	*/
	public Boolean hasVariable(String sName){
		return this.mVariables.containsKey(sName);
	}
	
	
	/*
		Retrieves a tag variables value
		
		@param sName String Name of variable
		
		@return Object Value of the tag variable
	*/
	public Object getVariable(String sName){
	
		// Look in our tag context
		if(this.hasVariable(sName)){
			return this.mVariables.get(sName);
		}
		
		// Look in our overall context
		Context oContext = this.getTag().getContext();
		if(oContext.hasVariable(sName)){
			return oContext.getVariable(sName);
		}
		
		return null;		
	}
	
	
	/*
		Retrieves a map of all the tag variables
		
		@return Map Key/Value pairs for all of the variables
	*/
	public HashMap<String,Object> getVariables(){
		return this.mVariables;
	}
	
	
}

