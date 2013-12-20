package codotos.controllers;


import codotos.config.ConfigManager;


public class Error extends Controller {
	
	
	java.lang.Exception oException = null;


	public String control(){
		
		this.oException = (java.lang.Exception) this.getContext().getAttribute("ERROR");
		
		// Look at a config prop, and either debug the error, or redirect to a standalone error page
		if(ConfigManager.getBoolean("redirectOnError")){
		
			return "redirect";
		
		}else{
		
			return "debug";
		
		}
	
	}
	
	
	public java.lang.Exception getException(){
	
		return this.oException;
	
	}
	

}

