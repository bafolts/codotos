package codotos.tags.taglibs.core;


import codotos.tags.Tag;

import java.lang.StringBuilder;
import java.util.Iterator;


public final class ForEach extends Tag {

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public ForEach() throws java.lang.Exception {
		super();
	}

	
	// @Override
	// Define the attributes this tag contains
	protected final void defineAttributes() throws java.lang.Exception {
		
		this.defineAttribute("items","java.util.Collection",true,null);
		this.defineAttribute("name","java.lang.String",true,null);
	
	}


	protected final String output() throws java.lang.Exception {
		
		StringBuilder sToReturn = new StringBuilder();
		
		String sName = this.getAttribute("name").toString();
		Object aItems = this.getAttribute("items");
		
		if(aItems instanceof java.util.Collection<?>){
			
			Iterator oIterator = ((java.util.Collection<?>) aItems).iterator(); 
			
			// TODO - Save the existing value for this variable name before we override it, so we can restore it later?
			
			while(oIterator.hasNext()) {
			
				Object oValue = (Object) oIterator.next();
				
				this.getParent().getTagContext().setVariable(sName,oValue);
				
				sToReturn.append(this.doBody());
				
			}
			
			// Clean up the variable
			this.getParent().getTagContext().removeVariable(sName);
			
		
		// TODO ????
		//}else if(aItems instanceof java.util.Map<?,?>){
		
		
		}else{
		
			throw new java.lang.Exception("Cannot iterate over type '"+ aItems.getClass().getName() +"'");
		
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