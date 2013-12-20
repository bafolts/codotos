package codotos;

public final class Constants {

	public static final String ROOT = System.getProperty("com.codotos.home") +"/"; 
	
	// hidden WEB_INF folder
	public static final String WEB_INF_ROOT = ROOT + "WEB-INF/";
	
	// classes folder
	public static final String CLASSES_ROOT = WEB_INF_ROOT + "classes/";
	
	// lib folder
	public static final String LIB_ROOT = WEB_INF_ROOT + "lib/";

	// Points to the root of the source code
	public static String SRC_BASE_DIR = ROOT +"../work/";

	// Points to the root of the cache folder
	// TODO - Create this folder if it does not exist...
	public static final String CACHE_DIR = WEB_INF_ROOT + "cache/";

	// For the map definition
	public static final String MAP_RESOURCES_DIR = ROOT;

	// For the templating system
	public static final String TEMPLATE_RESOURCES_DIR = ROOT + "templates/";

	// For the tag system
	public static final String TAG_SRC_DIR = ROOT + "tags/";

	// For the page system
	public static final String PAGE_RESOURCES_DIR = ROOT + "pages/";

	// For the resource definitions
	public static final String RESOURCE_RESOURCES_DIR = ROOT + "resources/";

}

