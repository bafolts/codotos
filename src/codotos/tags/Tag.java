package codotos.tags;


import codotos.config.ConfigManager;
import codotos.context.Context;
import codotos.tags.TagFragment;
import codotos.tags.TagContext;
import codotos.tags.TagAttribute;

import java.util.Iterator;
import java.io.PrintWriter;


/*
	This is the base class for all generated tag objects
	
	@abstract
*/	
public abstract class Tag {
	
	
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
		This is not the direct parent, but the parent .tag file in which this tag resides
	*/
	protected Tag oParent = null;
	
	
	/*
		Body fragment
	*/
	protected TagFragment oBody = null;
	
	
	/*
		Initialize our tag
	*/
	public Tag() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		try{
		
			// If the config manager is setup to run non-precompiled tags & runtime tag cache checks are enabled, check the tags resources for changes
			if(!ConfigManager.getBoolean("preCompiledTags") && ConfigManager.getBoolean("runtimeTagCacheChecks")){
			
				this.checkTagResources();
				
			}
			
			// Create our tag context
			this.oTagContext = new TagContext(this);
			
		}catch(codotos.exceptions.TagRuntimeException e){
			
			e.setResourceName(this.getResourceName());
			throw e;
			
		}
	
	}
	
	
	/*
		Returns the .tag file that defines this tag
		
		@return .Tag file location, or if its a taglib, return the full classname
	*/
	protected String getResourceName() {
	
		return this.getClass().getName();
	
	}
	
	
	/*
		Evaluate the tag, this is where the tags guts are
		Called by other tags
		
		@abstract
		
		@return String Output of the tag once executed
	*/
	abstract protected String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException;
	
	
	/*
		Evaluates the tag and outputs its return value
		Called by the navigator
		
		@final
		
		@return null
	*/
	public final void display() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		try{
			
			PrintWriter oResponseWriter = this.oContext.getResponse().getWriter();
			
			oResponseWriter.print(this.output());
			
		}catch(java.io.IOException e){
		
			codotos.exceptions.TagRuntimeException oException = new codotos.exceptions.TagRuntimeException("Error writing to the response writer");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(codotos.exceptions.TagRuntimeException e){
			
			e.setResourceName(this.getResourceName());
			throw e;
		
		}
	
	}
	
	
	/*
		Evaluate the tag and return its output as a string
		Called by other tags
		
		@final
		
		@return String Output of the tag
	*/
	public final String doTag() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		try{
		
			// Check that all the required attributes have values
			this.ensureRequiredAttributesSet();
			
			// Do the tag
			return this.output();
		
		}catch(codotos.exceptions.TagRuntimeException e){
			
			e.setResourceName(this.getResourceName());
			throw e;
		
		}
	
	}	
	
	
	/*
		Evaluate the tags body fragment and return its output as a string
		Called by other tags
		
		@final
		
		@return String Output of the tags body fragment
	*/
	public final String doBody() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
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
	protected final void checkTagResources() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		// Check to see if we already checked the resources, if we have, ignore
		// NOTE: This is useful when we call the same tag multiple times
		if(this.isTagResourcesAlreadyChecked()){
			return;
		}
		
		// Get the resource dependencies, if they exist		
		java.util.List<String> aTagResources = this.getTagResources();
		
		// Tag resources defined
		if(aTagResources!=null){
		
			for(int i=0,len=aTagResources.size(); i<len; i++){
				
				TagCompiler.compileTagDirFile(aTagResources.get(i));
			
			}
		
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
		Returns a static map of tag attributes for this tag
		
		@return java.util.HashMap<String,codotos.tags.TagAttribute> Map of TagAttribute objects with the key as the attribute name
	*/
	protected abstract java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes();
	
	
	/*
		Sets the value of a tag attribute
		
		@param sName String Name of the attribute		
		@param oValue Object Value of the attribute
		
		@final
		
		@return null
	*/
	public void setAttribute(String sName,Object oValue) throws codotos.exceptions.TagRuntimeException {
		
		// Make sure it is in the provided attributes list
		if(!this.getTagAttributes().containsKey(sName)){
			throw new codotos.exceptions.TagRuntimeException("Tag does not contain attribute '"+ sName +"'");
		}
		
		// Get the tag
		TagAttribute oAttribute = this.getTagAttributes().get(sName);
		
		// if type doesn't match
		if(!oAttribute.isCorrectType(oValue)){
			throw new codotos.exceptions.TagRuntimeException("Tag attribute '"+ sName +"' accepts '"+ oAttribute.getType() +"', not '"+ (oValue==null?"null":oValue.getClass().getName()) +"'");
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
	private final void ensureRequiredAttributesSet() throws codotos.exceptions.TagRuntimeException {
		
		// Get the TagAttribute values
		Iterator oAttributeValues = this.getTagAttributes().values().iterator();
		
		// Loop through each TagAttribute
		while(oAttributeValues.hasNext()){
		
			// get the TagAttribute
			TagAttribute oTagAttribute = (TagAttribute) oAttributeValues.next();
			
			// If the attribute is not set
			if(!this.hasAttribute(oTagAttribute.getName())){
			
				// If the attribute is required
				if(oTagAttribute.isRequired()){
					
					throw new codotos.exceptions.TagRuntimeException("Tag requires attribute '"+ oTagAttribute.getName() +"'");
				
				}
				
				// if the attribute has a default value, set that now
				if(oTagAttribute.hasDefaultValue()){
				
					this.setAttribute(oTagAttribute.getName(),oTagAttribute.getDefaultValue());
					
				}
				
			}
		
		}
	
	}


}

