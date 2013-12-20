package codotos.tags.taglibs.core;


import codotos.tags.Tag;
import codotos.resources.ResourceBundle;


public final class SetResourceBundle extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>();
	
	
	static{
		aTagAttributes.put("name",new codotos.tags.TagAttribute("name","java.lang.String",true,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public SetResourceBundle() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		ResourceBundle.DEFAULT_BUNDLE = this.getAttribute("name").toString();
		
		return "";
	
	}


}