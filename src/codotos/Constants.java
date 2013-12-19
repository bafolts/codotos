package codotos;

public final class Constants {

	// TODO - prepend with $_SERVER["DOCUMENT_ROOT"]
	public static final String ROOT = "";
	
	// compiled folder
	public static final String COMPILED_ROOT = ROOT + "compiled/";

	// Points to the root of the source code
	public static final String SRC_BASE_DIR = ROOT + "src/";

	// For the mvc source is kept
	public static final String MVC_SRC_DIR = SRC_BASE_DIR + "codotos/";

	// For the navigation directory
	public static final String NAVIGATION_SRC_DIR = MVC_SRC_DIR + "navigation/";

	// For the context directory
	public static final String CONTEXT_SRC_DIR = MVC_SRC_DIR + "context/";

	// For the utils directory
	public static final String UTILS_SRC_DIR = MVC_SRC_DIR + "utils/";

	// Points to the root of the cache folder
	public static final String CACHE_DIR = MVC_SRC_DIR + "cache/";

	// Where the controllers are kept
	public static final String CONTROLLER_RESOURCES_DIR = SRC_BASE_DIR + "controllers/";
	public static final String CONTROLLER_SRC_DIR = MVC_SRC_DIR + "controllers/";

	// For the map definition
	public static final String MAP_RESOURCES_DIR = ROOT;
	public static final String MAP_CACHE_DIR = CACHE_DIR;

	// For the templating system
	public static final String TEMPLATE_RESOURCES_DIR = ROOT + "templates/";
	public static final String TEMPLATE_CACHE_DIR = CACHE_DIR + "templates/";
	public static final String TEMPLATE_SRC_DIR = MVC_SRC_DIR + "templates/";

	// For the tag system
	// TODO TAG_RESOURCES_DIR??
	public static final String TAG_SRC_DIR = ROOT + "tags/";

	// For the page system
	public static final String PAGE_RESOURCES_DIR = ROOT + "pages/";
	public static final String PAGE_SRC_DIR = MVC_SRC_DIR + "pages/";

	// For the resource definitions
	public static final String RESOURCE_RESOURCES_DIR = ROOT + "resources/";
	public static final String RESOURCE_CACHE_DIR = CACHE_DIR + "resources/";
	public static final String RESOURCE_SRC_DIR = MVC_SRC_DIR + "resources/";

}

