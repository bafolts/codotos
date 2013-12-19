package codotos.controllers;


import codotos.utils.BeanUtils;
import codotos.context.Context;


public abstract class Controller {


	private Context oContext = null;
	

	// Do NOT ever override this
	final public String control(){
		
		// set the beans
		// TODO - Convert BeanUtils to Java
		//BeanUtils.setBean(this);
		
		// do something;
		return this.load();
	
	}


	// This should be overridden
	abstract public String load();


	public void setContext(Context oContext){
		this.oContext = oContext;
	}


	protected Context getContext(){
		return this.oContext;
	}
	

}

