package codotos.tags.taglibs.core;


import codotos.tags.TagDynamicAttributes;
import codotos.templates.TemplateBundleManager;


public final class Template extends TagDynamicAttributes {


	protected static java.util.HashMap<String,codotos.tags.TagAttribute> aTagAttributes = new java.util.HashMap<String,codotos.tags.TagAttribute>(3);
	
	
	static{
		aTagAttributes.put("name",new codotos.tags.TagAttribute("name","java.lang.String",true,null));
		aTagAttributes.put("bundle",new codotos.tags.TagAttribute("bundle","java.lang.String",true,null));
		aTagAttributes.put("escapeXML",new codotos.tags.TagAttribute("escapeXML","java.lang.Boolean",false,true));
		// Rest of attributes are dynamic
	}
	
	
	// @override
	protected final java.util.HashMap<String,codotos.tags.TagAttribute> getTagAttributes(){	
		return aTagAttributes;
	}

	
	// Need this here because it can't extend Tag without having a constructor that 'throws'
	public Template() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		super();
	}


	protected final String output() throws codotos.exceptions.TagRuntimeException, codotos.exceptions.TagCompilerException, codotos.exceptions.TagInterpreterException {
		
		try{
		
			codotos.templates.Template oTemplate = TemplateBundleManager.getBundle(this.getAttribute("bundle").toString()).getTemplate(this.getAttribute("name").toString(),this.getContext());
			
			return oTemplate.getText(this.getAttributeValues());
		
		}catch(codotos.exceptions.TemplateInterpreterException e){
			
			codotos.exceptions.TagRuntimeException oException =  new codotos.exceptions.TagRuntimeException("Error interpreting template '"+ this.getAttribute("name").toString() +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(codotos.exceptions.TemplateCompilerException e){
			
			codotos.exceptions.TagRuntimeException oException =  new codotos.exceptions.TagRuntimeException("Error compiling template '"+ this.getAttribute("name").toString() +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(codotos.exceptions.TemplateRuntimeException e){
			
			codotos.exceptions.TagRuntimeException oException =  new codotos.exceptions.TagRuntimeException("Error executing the template '"+ this.getAttribute("name").toString() +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}catch(codotos.exceptions.ResourceRuntimeException e){
			
			codotos.exceptions.TagRuntimeException oException = new codotos.exceptions.TagRuntimeException("Error executing the template '"+ this.getAttribute("name").toString() +"'");
			
			oException.initCause(e);
			
			throw oException;
		
		}
		
	}
	
	
}