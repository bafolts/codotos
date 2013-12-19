package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Out extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Out() throws java.lang.Exception {
		super();
	}

	
	// @Override
	// Define the attributes this tag contains
	protected final void defineAttributes() throws java.lang.Exception {
		this.defineAttribute("value","java.lang.String",true,null);
	}


	protected final String getValue() throws java.lang.Exception {
	
		return this.getAttribute("value").toString();
	
	}


	protected final String output() throws java.lang.Exception{
		
		return this.getValue();
		
	}
	
	
}