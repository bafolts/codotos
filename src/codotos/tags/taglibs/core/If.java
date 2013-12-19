package codotos.tags.taglibs.core;


import codotos.tags.Tag;


public final class If extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public If() throws java.lang.Exception {
		super();
	}


	protected final void defineAttributes() throws java.lang.Exception {
		
		this.defineAttribute("test","java.lang.Object",true,null);
		
	}


	protected final Boolean getTest() throws java.lang.Exception {
		
		Object oValue = this.getAttribute("test");
		
		if(oValue instanceof String){
			return Boolean.valueOf((String) oValue);
		}else if(oValue instanceof Boolean){
			return (Boolean) oValue;
		}else{
			throw new java.lang.Exception("Cannot convert type '"+ oValue.getClass().getName() +"' to Boolean. (Tag "+ this.getClass().getName() +")");
		}
	
	}



	protected final String output() throws java.lang.Exception{
		
		if(this.getTest()){
			
			return this.doBody();
		
		}
		
		return new String("");
		
	}
	
	
}