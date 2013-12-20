package codotos.controllers;


import codotos.context.Context;
import codotos.config.ConfigManager;
import codotos.bean.BeanPopulator;
import codotos.resources.ResourceBundleManager;
import codotos.templates.TemplateBundleManager;


// Do NOT ever override this
public final class SinglePageController extends PageController {
	

	// Do NOT ever override this
	public String control() throws codotos.exceptions.BeanUtilsClassMethodInvocationException, codotos.exceptions.ResourceRuntimeException, codotos.exceptions.TemplateInterpreterException, codotos.exceptions.TemplateCompilerException {
		
		// Check the config manager
		if(ConfigManager.getBoolean("runtimeTemplateCacheChecks")){
		
			// check the template bundle manager for run-time changes
			TemplateBundleManager.checkCache();
		
		}
		
		// Check the config manager
		if(ConfigManager.getBoolean("runtimeResourceCacheChecks")){
		
			// check the resource bundle manager for run-time changes
			ResourceBundleManager.checkCache();
		
		}
		
		// This output is never used
		return null;
	
	}
	
	
	// Take care of the abstract, this will never be called
	public String load(){
		return null;
	}
	
	
	// This would normally be considered a bad practice, but for single page files they should probably have access to this
	public Context getContext(){
		return super.getContext();
	}
	

}
