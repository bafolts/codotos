package codotos.tags.taglibs.core;


import codotos.tags.Tag;
import codotos.resources.ResourceBundle;


public final class SetResourceBundle extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public SetResourceBundle() throws java.lang.Exception {
		super();
	}

	
	// @Override
	// Define the attributes this tag contains
	protected final void defineAttributes() throws java.lang.Exception {
		
		this.defineAttribute("name","java.lang.String",true,null);
	
	}


	protected final String output() throws java.lang.Exception {
		
		ResourceBundle.DEFAULT_BUNDLE = this.getAttribute("name").toString();
		
		return "";
	
	}


}