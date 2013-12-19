package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Set extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Set() throws java.lang.Exception {
		super();
	}

	
	// @Override
	// Define the attributes this tag contains
	protected final void defineAttributes() throws java.lang.Exception {
		
		this.defineAttribute("name","java.lang.String",true,null);
		this.defineAttribute("scope","java.lang.String",false,"request");
		this.defineAttribute("value","java.lang.Object",true,null); // TODO - Object from expression language?
	
	}


	protected final String output() throws java.lang.Exception {
		
		String sName = (String) this.getAttribute("name");
		String sScope = "page";
		Object oValue = null;
		
		// if they defined a scope
		if(this.hasAttribute("scope")){
			sScope = (String) this.getAttribute("scope");
		}
		
		// If "value" attribute is defined
		if(this.hasAttribute("value")){
		
			oValue = this.getAttribute("value");
		
		// Otherwise use the inside of the tag
		}else{
		
			oValue = this.doBody();
			
		}
		
		// Request scope
		if(sScope.equals("request")){
		
			this.getContext().setVariable(sName,oValue);
		
		// Tag page scope
		}else{
		
			this.getParent().getTagContext().setVariable(sName,oValue);
			
		}
		
		return new String("");
		
	}
	
	
}