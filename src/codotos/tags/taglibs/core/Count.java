package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Count extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(1);
	
	
	static{
		aTagAttributes.put("items",new codotos.tags.TagAttribute("items","java.util.ArrayList",true,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Count() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		Object aItems = this.getAttribute("items");
		
		if(aItems instanceof java.util.Collection<?>){
			
			return Integer.toString(((java.util.Collection<?>) aItems).size());
			
		}else if(aItems instanceof java.util.Map<?,?>){
		
			return Integer.toString(((java.util.Map<?,?>) aItems).size());
		
		// TODO aItems.getClass().isArray()
		
		}else{
		
			throw new codotos.exceptions.TagRuntimeException("Cannot count size of type '"+ aItems.getClass().getName() +"'");
		
		}
	
	}


}