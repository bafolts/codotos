package codotos.tags;


import codotos.context.Context;
import codotos.tags.TagContext;


/*
	This is the base class for all generated tag fragment objects
	
	@abstract
*/	
public abstract class TagFragment {
	
	
	/*
		Context object
	*/
	private Context oContext = null;
	
	
	/*
		Tag Context object
	*/
	private TagContext oTagContext = null;
	
	
	/*
		Setup our tag fragment, set its context objects
		
		@param oContext ContextObject Context Object to set		
		@param oTagContext TagContextObject Tag Context Object to set
		
		@final
		
		@return null
	*/
	public TagFragment(Context oContext,TagContext oTagContext){
		
		this.oContext = oContext;
		this.oTagContext = oTagContext;
	
	}
	
	
	/*
		Execute our tag fragment
		
		@abstract
		
		@return String Output of the tag fragment once executed
	*/
	abstract public String invoke() throws java.lang.Exception;
	
	
	/*
		Return the current context object
		
		@param ContextObject Context Object
		
		@final
		
		@return null
	*/
	final public Context getContext(){
	
		return this.oContext;
	
	}
	
	
	/*
		Gets the tags context
		
		@final
		
		@return TagContextObject Current Tag Context Object
	*/
	final public TagContext getTagContext(){
	
		return this.oTagContext;
	
	}
	
	
}

