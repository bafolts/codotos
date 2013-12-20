package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Out extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(2);
	
	
	static{
		aTagAttributes.put("value",new codotos.tags.TagAttribute("value","java.lang.Object",true,null));
		aTagAttributes.put("escapeXML",new codotos.tags.TagAttribute("escapeXML","java.lang.Boolean",false,true));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Out() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String getValue() throws codotos.exceptions.TagRuntimeException {
		
		if(this.getAttribute("value") == null){
			return "";
		}
		
		if((Boolean) this.getAttribute("escapeXML")){
		
			return this.getAttribute("value").toString().replace("&","&amp;").replace("'","&apos;").replace("\"","&quot;").replace("<","&lt;").replace(">","&gt;");
		
		}else{
		
			return this.getAttribute("value").toString();
		
		}
	
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		return this.getValue();
		
	}
	
	
}