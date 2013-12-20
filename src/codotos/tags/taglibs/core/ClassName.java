package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class ClassName extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(1);
	
	
	static{
		aTagAttributes.put("value",new codotos.tags.TagAttribute("value","java.lang.Object",true,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public ClassName() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		return this.getAttribute("value").getClass().getName();
		
	}
	
	
}