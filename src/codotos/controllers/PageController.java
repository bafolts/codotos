package codotos.controllers;


import codotos.config.ConfigManager;
import codotos.bean.BeanPopulator;
import codotos.resources.ResourceBundleManager;
import codotos.templates.TemplateBundleManager;


public abstract class PageController extends Controller {
	

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
		
		// set the beans
		BeanPopulator.setBean(this.getContext().getRequest(),this);
		
		// do something;
		return this.load();
	
	}


	// This should be overridden
	abstract public String load();
	

}
