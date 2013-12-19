package codotos.tags;


import codotos.context.Context;
import codotos.tags.TagFragment;
import codotos.tags.TagContext;

import java.util.HashMap;


/*
	This is the class that defines a tag attribute
	
	@abstract
*/	
public class TagAttribute {


	/*
		Specifies whether this tag attribute is required
	*/
	protected Boolean bRequired = false;


	/*
		Specifies whether this tag attribute has a default value
	*/
	protected Boolean bDefaultValue = false;


	/*
		Default value (Int/Float/Boolean/String only)
	*/
	protected Object oDefaultValue = null;
	
	
	/*
		Name of attribute
	*/
	protected String sName;
	
	
	/*
		Type of attribute
	*/
	protected String sType;
	
	
	/*
		Initialize our tag
	*/
	public TagAttribute(String sName,String sType,Boolean bRequired,Object oDefaultValue){
		
		this.sName = sName;
		this.bRequired = bRequired;
		this.sType = sType;
		
		// If a default value is supplied
		if(oDefaultValue!=null){
			
			this.bDefaultValue=true;
			this.oDefaultValue=oDefaultValue;
			
		}
		
	}
	
	
	public String getName(){
		return this.sName;
	}
	
	
	public Boolean isRequired(){
		return this.bRequired;
	}
	
	
	public Boolean hasDefaultValue(){
		return this.bDefaultValue;
	}
	
	
	public Object getDefaultValue(){
		return this.oDefaultValue;
	}
	
	
	public String getType(){
		return this.sType;
	}
	
	
	public Boolean isCorrectType(Object oValue){
		
		try{
			return Class.forName(this.sType).isInstance(oValue);
		}catch(java.lang.ClassNotFoundException e){
			// This scenario shouldnt occur because the sType is checked during compilation
			return false;
		}
		
	}
	

}

