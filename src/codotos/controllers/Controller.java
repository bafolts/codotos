package codotos.controllers;


import codotos.context.Context;


public abstract class Controller {


	private Context oContext = null;
	

	abstract public String control() throws java.lang.Exception;


	public void setContext(Context oContext){
		this.oContext = oContext;
	}

	
	// Protected to keep tags/templates from grabbing context data
	// Should make methods instead
	protected Context getContext(){
		return this.oContext;
	}
	

}

