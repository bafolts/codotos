package codotos.bean;


import codotos.controllers.Controller;
import codotos.config.ConfigManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Array;


/*
 * Supported types:
 *	1) "model.setter=5" where setSetter(Type x) is defined, "Type" must accept a single "String" as its constructor
 *  2) "model.setter[]=1&model.setter[]=2" where setSetter(Type[] x) is defined, "Type" must accept a single "String" as its constructor
 *	3) "model.setter(10).bean=11" where Map<Type1, Object> getSetter() returns a instanceOf MAP, and "Type1" must accept a single "String" as its constructor. Where setBean(Type2 x) must be declared on "Object", and "Type2" must accept a single "String" as its constructor
 *	4) "model.setter[10].bean=11" where List<Object> getSetter() returns a instanceOf LIST, where setBean(Type1 x) must be declared on "Object", and "Type1" must accept a single "String" as its constructor
 *
 * FUTURE CONSIDERATION:
 *	1) "model.setter(5)=3" where Map<Type1, Type2> getSetter() returns a instanceOf MAP, and "Type1" & "Type2" must accept a single "String" as its constructor
 *  2) "model.setter[5]=3" where List<Type1> getSetter() returns a instanceOf LIST, and "Type1" must accept a single "String" as its constructor
 */
public final class BeanPopulator {

	
	// Begins with a character [A-Za-z]
	// Cannot contain characters not allowed in a method [A-Za-z0-9_$]
	// Can contain "(x)" or "[x]" as long as "x" occurs once and these are immediately followed by a "."
	// Can end with a []
	/*
	test("",false);
	test("a",true);
	test("5a",false);
	test("b5$_d",true);
	test("a.b",true);
	test("5a.b",false);
	test("a.b5$_d",true);

	test("abC5[]",true);
	test("a.b.c[]",true);
	test("a.b5$_d[]",true);
	test("abC5[].value",false);

	test("abC5(xxx)",false);
	test("abC5(xxx).5a",false);
	test("abC5(xxx).value",true);
	test("abC5(invalid.name).value",false);
	test("abC5(xxx).value[]",true);

	test("abC5[x]",false);
	test("abC5[55].5a",false);
	test("abC5[55].value",true);
	test("abC5[555].value[]",true);

	test("abC5.x422d[55].blahd(monkey).pants[]",true);
	test("abC5.x422d[55].blahd(mon.key).pants[]",false);
	test("abC5.x422d[5.5].blahd(monkey).pants[]",false);
	*/
	private final static Pattern oKeyPattern = Pattern.compile("^([A-Za-z][A-Za-z0-9_\\$]*((\\([A-Za-z0-9_\\$]+\\))|(\\[[0-9]+\\]))?\\.)*[A-Za-z][A-Za-z0-9_\\$]*(\\[\\])?$");
	private final static Pattern oMapPattern = Pattern.compile("^(.*)\\((.*)\\)$");
	private final static Pattern oListPattern = Pattern.compile("^(.*)\\[(.*)\\]$");
	
	
	/*
		Used to populate a controller objects setters given the get/post data
		Called by the Controller object before it's load() method is executed
		This will retrieve the GET/POST data & then call the corresponding Controller setters
		
		@param HttpServletRequest oRequest The request object
		@param oController Controller The controller that is to be populated	
		
		@return null
	*/
	public final static void setBean(HttpServletRequest oRequest,Controller oController) throws codotos.exceptions.BeanUtilsClassMethodInvocationException {
		
		Map params = oRequest.getParameterMap();
		Set set = params.entrySet();
		Iterator iter = set.iterator();
		
		while (iter.hasNext()) {
			Entry n = (Entry) iter.next();			
			String[] arr = (String[]) n.getValue();
			String sKey = (String) n.getKey();
			
			// If the supplied key does not match our validity requirements then ignore it
			Matcher oMatcher = oKeyPattern.matcher(sKey);
			if(oMatcher.matches()){
			
				try{
				
					populateBean(sKey,arr,oController);
					
				}catch(codotos.exceptions.BeanUtilsNoClassMethodException e){
				
					// When a .getX() or .setX(Object) or .setX(Object[]) is not defined on the object the ".x" is called on
					// These can be ignored, user can input false data and it will throw this (eg ".DNE")
					
					// Check the config manager
					if(ConfigManager.getBoolean("showBeanPopulatorStackTracesInConsole")){
						e.printStackTrace();
					}
					
				}catch(codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException e){
				
					// Occurs when error occurs creating (new Object(String)) to use for setter(Object)
					// Don't throw an error here, user can manipulate incoming data to cause these errors (eg "abc" for Integer)
					
					// Check the config manager
					if(ConfigManager.getBoolean("showBeanPopulatorStackTracesInConsole")){
						e.printStackTrace();
					}
					
				}catch(codotos.exceptions.BeanUtilsClassMethodInvocationException e){
				
					// These should cause page level errors, as they indicate an error in the getX() or setX() code
					throw e;
				
				}
				
			}
			
		}
		

	}
	
	
	private final static void populateBean(String sKey, String[] aValues, Object oBean) throws codotos.exceptions.BeanUtilsNoClassMethodException, codotos.exceptions.BeanUtilsClassMethodInvocationException, codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException {
		
		String[] aGetters = sKey.split("\\.",2);
		
		if(aGetters.length == 1){
			setBeanOnObject(aGetters[0],aValues,oBean);
		}else{
			getBeanFromObject(aGetters[0],aGetters[1],aValues,oBean);
		}
	
	}
	
	
	private final static void getBeanFromObject(String sKey, String sLeftover, String[] aValues, Object oBean) throws codotos.exceptions.BeanUtilsNoClassMethodException, codotos.exceptions.BeanUtilsClassMethodInvocationException, codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException {
		
		Boolean isList = false,
				isMap = false;
		String sListMapKey = null;
		
		//	Look for sKey to end with [...]
		Matcher oListMatcher = oListPattern.matcher(sKey);		
		if(oListMatcher.matches()){
			isList=true;
			sListMapKey=oListMatcher.group(2);
			sKey=oListMatcher.group(1);
		}else{
		
			//	Look for sKey to end with (...)
			Matcher oMapMatcher = oMapPattern.matcher(sKey);		
			if(oMapMatcher.matches()){
				isMap=true;
				sListMapKey=oMapMatcher.group(2);
				sKey=oMapMatcher.group(1);
			}
			
		}
		
		// Build the getter method name
		String sMethodName = "get" + sKey.substring(0,1).toUpperCase() + sKey.substring(1);
		
		// get the getter() method
		Class<?> oClass = oBean.getClass();
		
		Method oMethod = null;
		try{
		
			oMethod = oClass.getMethod(sMethodName); // No parameter names on a getter()
			
		}catch(java.lang.Exception e){
		
			codotos.exceptions.BeanUtilsNoClassMethodException oException = new codotos.exceptions.BeanUtilsNoClassMethodException("'"+ oClass.getName() +"' does not have a method named '"+ sMethodName +"'.");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
		// Execute the method, capture its returned object
		Object oNewBean = invokeHelper(oBean,oMethod,null);
		
		// See if the new bean is null
		if(oNewBean == null){
			
			// if it s a map or list
			if(isMap || isList){
			
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"() returns null, cannot grab map/list value.");
			
			}else{
			
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"() returns null, cannot call '"+ sLeftover +"' on null.");
			
			}
			
		}
		
		
		// If it was a list or a map:
		//   Check that it is an instance of that type
		//   Call the .get() or .item() method
		if(isMap){
			
			if(!(oNewBean instanceof java.util.Map)){
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"() should return an instanceof java.util.Map, but instead returns "+ oNewBean.getClass().getName());
			}
			
			oNewBean = ((java.util.Map) oNewBean).get(sListMapKey);
			
			// See if the new bean is null
			if(oNewBean == null){
			
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"().get("+ sListMapKey +") returns null.");
			
			}
		
		}else if(isList){
		
			if(!(oNewBean instanceof java.util.List)){
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"() should return an instanceof java.util.List, but instead returns "+ oNewBean.getClass().getName());
			}
			
			try{
			
				oNewBean = ((java.util.List) oNewBean).get(Integer.parseInt(sListMapKey));
				
			}catch(java.lang.IndexOutOfBoundsException e){
				
				codotos.exceptions.BeanUtilsNoClassMethodException oException = new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"().get("+ sListMapKey +") failed. List does not contain an element at position "+ sListMapKey +".");
				
				oException.initCause(e);
				
				throw oException;
				
			}
			
			// See if the new bean is null
			if(oNewBean == null){
			
				throw new codotos.exceptions.BeanUtilsNoClassMethodException(oClass.getName() + "."+ sMethodName +"().get("+ sListMapKey +") returns null.");
			
			}
		
		}
		
		// Continue populating the bean
		populateBean(sLeftover,aValues,oNewBean);
	
	}
	
	
	private final static void setBeanOnObject(String sKey, String[] aValues, Object oBean) throws codotos.exceptions.BeanUtilsNoClassMethodException, codotos.exceptions.BeanUtilsClassMethodInvocationException, codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException {
	
		// NOTE: If someone submits multiple model.x=5 & model.x=6 then it will call setX() once, for the first item in the aValues[] list
		// NOTE: If the bean defines multiple setX() methods, they will ALL be executed
		
		// Whether or not the key is .setter[] or .setter
		Boolean isArray = false;
		
		if(sKey.endsWith("[]")){
			isArray = true;
			sKey=sKey.substring(0,sKey.length()-2);
		}
	
		// Build the setter method name
		String sMethodName = "set" + sKey.substring(0,1).toUpperCase() + sKey.substring(1);
		
		// Get the setter methods
		Class<?> oClass = oBean.getClass();		
		Boolean oMethodFound = false;
		
		// Get all the methods for this class
		Method[] aMethods = oClass.getMethods();
		
		// Loop through each method
		for(int i=0,len=aMethods.length; i<len; i++){
			
			// If our method does not have the same name as our setter...go to the next
			if(!aMethods[i].getName().equals(sMethodName)){
				continue;
			}
			
			Method oMethod = aMethods[i];
			Class[] oClasses = oMethod.getParameterTypes();
			
			// If our method does not have one parameter...go to the next
			if(oClasses.length!=1){
				continue;
			}
			
			// Grab the parameter class for the setter
			Class<?> oParameterClass = oClasses[0];
			
			// TODO - Should we only support primitive objects as parameters? Or anything with a constructor(String)?
			
			// If our parameter IS an array (Object[])
			if(oParameterClass.isArray()){
				// If the data passed in ISNT an array, this isn't allowed
				if(!isArray){			
					continue;
				}				
			// If our parameter ISNT an array (Object[])
			}else{
				// If the data passed in IS an array, this isn't allowed
				if(isArray){
					continue;
				}
			}
			
			// We found our method
			oMethodFound=true;
			
			Object oNewSetterObject = null;
			
			// If its an array setter[] then we need to loop through each value
			if(isArray){
				
				// grab the non-array class type
				oParameterClass = oParameterClass.getComponentType();
				
				// We will convert our array of strings into an array of objects that match the parameter class
				oNewSetterObject = Array.newInstance(oParameterClass,aValues.length);
				
				for(int j=0,lenj=aValues.length; j<lenj; j++){
				
					Array.set(oNewSetterObject,j,createSetterParameterHelper(oClass, oMethod, oParameterClass, aValues[j]));
				
				}
				
			}else{
				
				// Only take the first value
				oNewSetterObject = createSetterParameterHelper(oClass, oMethod, oParameterClass, aValues[0]);
			
			}
			
			// then pass the contructor instance into the setBean()
			invokeHelper(oBean,oMethod,oNewSetterObject);
			
		}
		
		if(!oMethodFound){
		
			throw new codotos.exceptions.BeanUtilsNoClassMethodException(""+ oClass.getName() +"."+ sMethodName +"(Object"+ (isArray?"[]":"") +") does not exist, or does not accept 1 parameter.");
			
		}
		
	}
	
	
	private final static Object invokeHelper(Object oBean,Method oMethod,Object oParameter) throws codotos.exceptions.BeanUtilsClassMethodInvocationException {
		
		Object oNewObject = null;
		
		try{
			
			if(oParameter==null){
				oNewObject = oMethod.invoke(oBean);
			}else{
				oNewObject = oMethod.invoke(oBean,new Object[]{oParameter});
			}
			
		}catch(java.lang.Exception e){
			
			String sParameter = "";
			
			if(oParameter!=null){
				Class oParamClass = oParameter.getClass();
				sParameter = (oParamClass.isArray()?oParamClass.getComponentType().getName()+"[]":oParamClass.getName());			
			}
			
			// Error invoking the getter or setter
			codotos.exceptions.BeanUtilsClassMethodInvocationException oException = new codotos.exceptions.BeanUtilsClassMethodInvocationException("Error invoking "+ oBean.getClass().getName() +"."+ oMethod.getName() +"("+ sParameter +")");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
		return oNewObject;
		
	}
	
	
	private final static Object createSetterParameterHelper(Class<?> oClass, Method oMethod, Class<?> oParameterClass, String sValue) throws codotos.exceptions.BeanUtilsNoClassMethodException, codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException {
	
		// Check if the one parameter it accepts has a constructor with a single/array string parameter...
		Constructor oParameterClassConstructor = null;
		Class[] aConstructorParamArray = new Class[]{String.class};
		
		// Look for the constructor with a string parameter
		try{
			
			oParameterClassConstructor = oParameterClass.getConstructor(aConstructorParamArray);
		
		}catch(java.lang.Exception e){
			
			codotos.exceptions.BeanUtilsNoClassMethodException oException = new codotos.exceptions.BeanUtilsNoClassMethodException(""+ oParameterClass.getName() +" does not have a Constructor(String), used for "+ oClass.getName() +"."+ oMethod.getName() +"("+ oParameterClass.getName() +")");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
		Object oNewSetterObject = null;
		
		try{
		
			// Execute the constructor, passing in the string, to get a new object to pass the setter
			oNewSetterObject = oParameterClassConstructor.newInstance(new Object[]{sValue});
		
		}catch(java.lang.Exception e){
		
			// Error invoking the constructor method
			codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException oException = new codotos.exceptions.BeanUtilsCreateMethodParameterInstanceException("Error creating instance of "+ oParameterClass.getName() +" to pass into "+ oClass.getName()  +"."+ oMethod.getName() +"("+ oParameterClass.getName() +").");
			
			oException.initCause(e);
			
			throw oException;
			
		}
		
		return oNewSetterObject;
	
	}


}


