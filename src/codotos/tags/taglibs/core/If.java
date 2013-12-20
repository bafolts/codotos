package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class If extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(1);
	
	
	static{
		aTagAttributes.put("test",new codotos.tags.TagAttribute("test","java.lang.Object",true,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public If() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final Boolean getTest() throws codotos.exceptions.TagRuntimeException {
		
		Object oValue = this.getAttribute("test");
		
		if(oValue instanceof String){
			return Boolean.valueOf((String) oValue);
		}else if(oValue instanceof Boolean){
			return (Boolean) oValue;
		}else{
			throw new codotos.exceptions.TagRuntimeException("Cannot convert type '"+ oValue.getClass().getName() +"' to Boolean. (Tag "+ this.getClass().getName() +")");
		}
	
	}



	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		if(this.getTest()){
			
			return this.doBody();
		
		}
		
		return new String("");
		
	}
	
	
}