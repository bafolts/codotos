package codotos.tags.taglibs.core;


import codotos.tags.Tag;

import java.lang.StringBuilder;
import java.util.Iterator;


public final class ForEach extends Tag {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(1);
	
	
	static{
		aTagAttributes.put("items",new codotos.tags.TagAttribute("items","java.lang.Object",true,null));
		aTagAttributes.put("name",new codotos.tags.TagAttribute("name","java.lang.String",true,null));
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public ForEach() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException  {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		StringBuilder sToReturn = new StringBuilder();
		
		String sName = this.getAttribute("name").toString();
		Object aItems = this.getAttribute("items");
		
		if(aItems instanceof java.util.Collection<?>){
			
			Iterator oIterator = ((java.util.Collection<?>) aItems).iterator(); 
			
			// TODO - Save the existing value for this variable name before we override it, so we can restore it later?
			
			while(oIterator.hasNext()) {
			
				Object oValue = oIterator.next();
				
				this.getParent().getTagContext().setVariable(sName,oValue);
				
				sToReturn.append(this.doBody());
				
			}
			
			// Clean up the variable
			this.getParent().getTagContext().removeVariable(sName);
			
		}else if(aItems.getClass().isArray()){
		
			for(int i=0, len=java.lang.reflect.Array.getLength(aItems); i<len; i++){
				
				Object oValue = java.lang.reflect.Array.get(aItems,i);
				
				this.getParent().getTagContext().setVariable(sName,oValue);
				
				sToReturn.append(this.doBody());
				
			}
		
		// TODO - Add java.util.Map?
		//}else if(aItems instanceof java.util.Map<?,?>){
		
		
		}else{
		
			throw new codotos.exceptions.TagRuntimeException("Cannot iterate over type '"+ aItems.getClass().getName() +"'");
		
		}
		
		return sToReturn.toString();
		
		/*
		$aItems = $this->getAttribute("items");
		$sName = $this->getAttribute("name");
		$toReturn = "";
		
		for($i=0,$len=count($aItems); $i<$len; $i++){
			$this->getParent()->getTagContext()->setVariable($sName,$aItems[$i]);
			$toReturn .= $this->doBody();
		}
		
		return $toReturn;
		*/		
	
	}


}