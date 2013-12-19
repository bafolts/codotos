package codotos.tags;


import codotos.tags.Tag;

import java.util.HashMap;


/*
	This is the base class for all generated tag objects with dynamic attributes
	
	@abstract
*/	
public abstract class TagDynamicAttributes extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public TagDynamicAttributes() throws java.lang.Exception {
		super();
	}
	
	
	/*
		Retrieve a list of attribute values
		
		@final
		
		@return Map<String,Object> Map of attributes
	*/
	protected final HashMap<String,Object> getAttributeValues(){
	
		// NOTE: This will return page scope variables too
		return this.getTagContext().getVariables();
	
	}
	
	
	public final void setAttribute(String sName,Object oValue) throws java.lang.Exception {
		
		// Dynamic attributes means that not all attributes need to be defined, however we can specify a few attributes which must
		// be defined, and specify their default values and if they are required
		
		// See if the attribute is defined
		if(this.mAttributeDefinitions.containsKey(sName)){
			
			// Get the tag
			TagAttribute oAttribute = this.mAttributeDefinitions.get(sName);
		
			// if type doesn't match
			if(!oAttribute.isCorrectType(oValue)){
				
				throw new java.lang.Exception("Tag "+ this.getClass().getName() +" attribute '"+ sName +"' accepts '"+ oAttribute.getType() +"', not '"+ oValue.getClass().getName() +"'");
			
			}
		
		}
		
		// Assign the attribute to a local variable
		this.getTagContext().setVariable(sName,oValue);
	
	}
	
	
}


