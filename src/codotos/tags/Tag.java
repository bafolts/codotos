package codotos.tags;


import codotos.context.Context;
import codotos.tags.TagFragment;
import codotos.tags.TagContext;
import codotos.tags.TagAttribute;

import java.util.Iterator;
import java.util.HashMap;

// TODO - When tag is constructed, check if tags dependencies are recent & compiled ?

/*
	This is the base class for all generated tag objects
	
	@abstract
*/	
public abstract class Tag {
	
	
	/*
		Map that contains attribute definitions
	*/
	protected final HashMap<String,TagAttribute> mAttributeDefinitions = new HashMap<String,TagAttribute>();
	
	
	/*
		Context object
	*/
	protected Context oContext = null;
	
	
	/*
		Tag Context object
	*/
	protected TagContext oTagContext = null;
	
	
	/*
		Parent Tag Object
		TODO: This is not the direct parent, but the parent .tag, I think?
	*/
	protected Tag oParent = null;
	
	
	/*
		Body fragment
	*/
	protected TagFragment oBody = null;
	
	
	/*
		Initialize our tag
	*/
	public Tag() throws java.lang.Exception {
		
		// if this tag depends on run-time compiled tags, check that they are up to date
		this.checkTagResources();
		
		// Create our tag context
		this.oTagContext = new TagContext(this);
		
		// Define attributes, add them to the map, set default values
		this.defineAttributes();
	
	}
	
	
	/*
		Overwritten by extending classes, defines the attributes for the tag
		
		@return null
	*/
	abstract protected void defineAttributes() throws java.lang.Exception;
	
	
	/*
		Evaluate the tag, this is where the tags guts are
		Called by other tags
		
		@abstract
		
		@return String Output of the tag once executed
	*/
	abstract protected String output() throws java.lang.Exception;
	
	
	/*
		Evaluates the tag and outputs its return value
		Called by the navigator
		
		@final
		
		@return null
	*/
	public final void display() throws java.lang.Exception{
	
		// TODO - translate me
		System.out.print(this.output());
	
	}
	
	
	/*
		Evaluate the tag and return its output as a string
		Called by other tags
		
		@final
		
		@return String Output of the tag
	*/
	public final String doTag() throws java.lang.Exception{
		
		// Check that all the required attributes have values
		this.ensureRequiredAttributesSet();
		
		// Do the tag
		return this.output();
		
	
	}	
	
	
	/*
		Evaluate the tags body fragment and return its output as a string
		Called by other tags
		
		@final
		
		@return String Output of the tags body fragment
	*/
	public final String doBody() throws java.lang.Exception{
		
		// If we don't have a fragment for the body...
		if(this.oBody == null){
			return "";
		}
		
		// Execute the tag body's fragment
		return this.oBody.invoke();
	
	}
	
	
	/*
		Check the tag's resources, make sure they are all up to date, otherwise recompile them
		
		@final
		
		@return void
	*/
	protected final void checkTagResources() throws java.lang.Exception {
		
		// Check to see if we already checked the resources, if we have, ignore
		// NOTE: This is useful when we call the same tag multiple times
		if(this.isTagResourcesAlreadyChecked())
			return;
		
		// Get the resource dependencies, if they exist		
		java.util.List<String> aTagResources = this.getTagResources();
		
		for(int i=0,len=aTagResources.size(); i<len; i++){
			
			TagCompiler.compileTagDirFile(aTagResources.get(i));
		
		}
		
		this.setTagResourcesAlreadyChecked();
		
	}
	
	
	/*
		Set the context object
		
		@param oContext ContextObject Context Object to set
		
		@final
		
		@return null
	*/
	public final void setContext(Context oContext){
	
		this.oContext = oContext;
	
	}
	
	
	/*
		Set the body fragment object
		
		@param oFragment FragmentObject Body Fragment Object to set
		
		@final
		
		@return null
	*/
	public final void setBody(TagFragment oFragment){
	
		this.oBody = oFragment;
	
	}
	
	
	/*
		Set the parent tag
		
		@param oParent TagObject Tag who is the parent
		
		@return null
	*/
	public final void setParent(Tag oParent){
		this.oParent = oParent;
	}
	
	
	/*
		Return the current context object
		
		@param ContextObject Context Object
		
		@final
		
		@return null
	*/
	public final Context getContext(){
	
		return this.oContext;
	
	}
	
	
	/*
		Gets the tags context
		
		@final
		
		@return TagContextObject Current Tag Context Object
	*/
	public final TagContext getTagContext(){
	
		return this.oTagContext;
	
	}
	
	
	/*
		Get the parent tag
		
		@return TagObject Tag who is the parent
	*/
	public final Tag getParent(){
		return this.oParent;
	}
	
	
	/*
		Get the tag resource dependencies array.
		This is to be overwritten by generated tags, otherwise it returns null
		
		@return List<String> Array with a list of other .tag files to check compilation of
	*/
	protected java.util.List<String> getTagResources(){
		return null;
	}
	
	
	/*
		Overwritten by extending classes, determines if the tag resources have already been checked
		
		@return Boolean True if resources already checked, False if not
	*/
	protected Boolean isTagResourcesAlreadyChecked(){
		return true;
	}
	
	
	/*
		Overwritten by extending classes, sets the static variable that dictates the tag resources have already been checked
		
		@return null
	*/
	protected void setTagResourcesAlreadyChecked(){
		// do nothing
	}
	
	
	/*
		Creates an attribute definition for the tag, if a default value is specified, set it now
		
		@param sName String Name of the attribute		
		@param sName String Attribute values expected data type
		
		@final
		
		@return null
	*/
	protected final void defineAttribute(String sName,String sType,Boolean sRequired,String sDefault) throws java.lang.Exception {
		
		TagAttribute oAttribute = new TagAttribute(sName,sType,sRequired,sDefault);
		mAttributeDefinitions.put(sName,oAttribute);
		
		// If it has a default value, assign it now (it can be overwritten later)
		if(oAttribute.hasDefaultValue()){
			this.setAttribute(sName,oAttribute.getDefaultValue());
		}
	
	}
	
	
	/*
		Sets the value of a tag attribute
		
		@param sName String Name of the attribute		
		@param oValue Object Value of the attribute
		
		@final
		
		@return null
	*/
	public void setAttribute(String sName,Object oValue) throws java.lang.Exception {
		
		// Make sure it is in the provided attributes list
		if(!mAttributeDefinitions.containsKey(sName)){
			throw new java.lang.Exception("Tag "+ this.getClass().getName() +" does not contain attribute '"+ sName +"'");
		}
		
		// Get the tag
		TagAttribute oAttribute = mAttributeDefinitions.get(sName);
		
		// if type doesn't match
		if(!oAttribute.isCorrectType(oValue)){
			throw new java.lang.Exception("Tag "+ this.getClass().getName() +" attribute '"+ sName +"' accepts '"+ oAttribute.getType() +"', not '"+ oValue.getClass().getName() +"'");
		}
		
		// Assign the attribute to a local variable
		this.getTagContext().setVariable(sName,oValue);
	
	}
	
	
	/*
		Used to determine if a tag has an attribute defined
		
		@param sName String Name of the attribute
		
		@final
		
		@return Boolean True if tag has the attribute defined, False otherwise
	*/
	public final Boolean hasAttribute(String sName){
		
		// Note: This will return page scope variables too
		return this.getTagContext().hasVariable(sName);
	
	}
	
	
	/*
		Retrieve the value of a tags attribute
		
		@param sName String Name of the attribute
		
		@final
		
		@return Object Value of the attribute
	*/
	public final Object getAttribute(String sName){
		
		// Note: This will return page scope variables too
		return this.getTagContext().getVariable(sName);
	
	}
	
	
	/*
		Ensure that the tag currently has all its required attributes set
		
		@final
	*/
	private final void ensureRequiredAttributesSet() throws java.lang.Exception {
		
		// Get the TagAttribute values
		Iterator oAttributeValues = mAttributeDefinitions.values().iterator();
		
		// Loop through each TagAttribute
		while(oAttributeValues.hasNext()){
		
			// get the TagAttribute
			TagAttribute oTagAttribute = (TagAttribute) oAttributeValues.next();
			
			// If the attribute is required, and the attribute is not set...
			if(oTagAttribute.isRequired() && !this.hasAttribute(oTagAttribute.getName())){
				
				// TODO - This is kind of weird, boolean never will return false, will just error up ...
				throw new java.lang.Exception("Tag "+ this.getClass().getName() +" requires the attribute '"+ oTagAttribute.getName() +"'");
			
			}
			
		
		}
	
	}


}

