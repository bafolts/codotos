package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class Count extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Count() throws java.lang.Exception {
		super();
	}
	
	
	// @Override
	// Define the attributes this tag contains
	protected final void defineAttributes() throws java.lang.Exception {
		
		// TODO - also supports java.util.Map
		this.defineAttribute("items","java.util.ArrayList",true,null);
	
	}


	protected final String output() throws java.lang.Exception{
		
		Object aItems = this.getAttribute("items");
		
		if(aItems instanceof java.util.Collection<?>){
			
			return Integer.toString(((java.util.Collection<?>) aItems).size());
			
		}else if(aItems instanceof java.util.Map<?,?>){
		
			return Integer.toString(((java.util.Map<?,?>) aItems).size());
		
		}else{
		
			throw new java.lang.Exception("Cannot count size of type '"+ aItems.getClass().getName() +"'");
		
		}
	
	}


}