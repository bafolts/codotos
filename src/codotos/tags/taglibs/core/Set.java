package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Set extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(3);
	
	
	static{
		aTagAttributes.put("name",new codotos.tags.TagAttribute("name","java.lang.String",true,null));
		aTagAttributes.put("scope",new codotos.tags.TagAttribute("scope","java.lang.String",false,"request"));
		aTagAttributes.put("value",new codotos.tags.TagAttribute("value","java.lang.Object",false,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Set() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
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