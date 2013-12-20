package codotos.config;


import java.util.HashMap;


public final class ConfigManager {


	public static final HashMap<String,Boolean> mBooleanConfigs = new HashMap<String,Boolean>();
	public static final HashMap<String,Integer> mIntegerConfigs = new HashMap<String,Integer>();
	
	
	static {
		
		// Set this to true to have any changes to map.xml be updated during runtime
		mBooleanConfigs.put("runtimeNavigatorCacheChecks",true);
		
		// Set this to true to have any changes to the resources files to be updated during runtime
		mBooleanConfigs.put("runtimeResourceCacheChecks",true);
		
		// Set this to true to have any changes to the template files to be updated during runtime
		// NOTE: If you have preCompiledTemplates=true this is ignored
		mBooleanConfigs.put("runtimeTemplateCacheChecks",true);
		
		// Set this to true to have any changes to the tag/page files to be updated during runtime
		// NOTE: If you have preCompiledTags=true then this is ignored
		mBooleanConfigs.put("runtimeTagCacheChecks",true);
		
		// Set to true if you are including pre-compiled tags/templates
		// Set to false if you want the compiler to compile them in realtime
		mBooleanConfigs.put("preCompiledTags",false);
		mBooleanConfigs.put("preCompiledTemplates",false);
		
		// Set this to true if you want any errors with bean map population to be shown in the console
		mBooleanConfigs.put("showBeanPopulatorStackTracesInConsole",false);
		
		// Set this to true if you do not want to display the errors to the user, and instead want to redirect to a new page (see map.xml <error> node)
		mBooleanConfigs.put("redirectOnError",false);
	}
	
	
	public static final Boolean getBoolean(String sName){
	
		return mBooleanConfigs.get(sName);
	
	}
	
	
	public static final Integer getInteger(String sName){
	
		return mIntegerConfigs.get(sName);
	
	}
	

}