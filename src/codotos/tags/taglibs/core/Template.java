package codotos.tags.taglibs.core;


import codotos.tags.TagDynamicAttributes;
import codotos.templates.TemplateBundleManager;


public final class Template extends TagDynamicAttributes {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Template() throws java.lang.Exception {
		super();
	}

	
	protected final void defineAttributes() throws java.lang.Exception {
		this.defineAttribute("bundle","java.lang.String",true,null);
		this.defineAttribute("name","java.lang.String",true,null);
		// Rest of attributes are dynamic
	}


	protected final String output() throws java.lang.Exception {
		
		codotos.templates.Template oTemplate = TemplateBundleManager.getBundle(this.getAttribute("bundle").toString()).getTemplate(this.getAttribute("name").toString());
		
		return oTemplate.getText(this.getAttributeValues());
		
	}
	
	
}