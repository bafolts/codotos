package codotos.templates;


import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Method;
import java.util.HashMap;


public class Expression {


	public static String evaluate(String sRawExpression,HashMap<String,Object> mTemplateData) throws java.lang.Exception {
	
		return Expression.compileLine(sRawExpression,mTemplateData).toString();

	}
	
	
	private static Object compileLine(String sRawLine,HashMap<String,Object> mTemplateData) throws java.lang.Exception {
		
		// "  blah  ${ Boo } "
		// (String) "  blah  " + EVAL +" "
		
		Pattern oPattern = Pattern.compile("^(.*?)\\$\\{[\\s]*(.*?)[\\s]*\\}(.*)");
		Matcher oMatcher = oPattern.matcher(sRawLine);
		
		if(!oMatcher.matches()){
			return sRawLine;
		}
		
		// if it only contains a big ${} then it can return complex things
		if(oMatcher.group(1).isEmpty() && oMatcher.group(3).isEmpty()){
		
			return Expression.compileExpression(oMatcher.group(2),mTemplateData);
		
		// if it contains leading or ending characters, it is a string.
		}else{
			
			return	oMatcher.group(1) + 
					new String(Expression.compileExpression(oMatcher.group(2),mTemplateData).toString()) + 
					new String(Expression.compileLine(oMatcher.group(3),mTemplateData).toString());
			
		}
		
	}
	
	
	private static Object compileExpression(String sRawText,HashMap<String,Object> mTemplateData) throws java.lang.Exception {
		
		if(sRawText.isEmpty())
			return "";
		
		String[] aGetters = sRawText.split("\\.",2);		
		String sVariableName = aGetters[0];
		
		Object oVariable = mTemplateData.get(sVariableName);
		
		if(aGetters.length==2){
			return Expression.buildBean(sRawText,oVariable,aGetters[1]);
		}
		
		// No getters to call, is just a null end value
		// TODO - Should this just be null?
		if(oVariable == null){
			return new String("");
		}
		
		return oVariable;
		
	}
	
	
	private static Object buildBean(String sOriginalText, Object oBean,String sGetter) throws java.lang.Exception {
		
		// If the bean is null
		if(oBean == null){
			
			// No getters to call, is just a null end value
			// TODO - Should this just be null?
			if(sGetter.isEmpty()){
				return new String("");
			
			// We cant call the getters on a null
			}else{
				throw new java.lang.Exception("No '"+ sGetter +"' on null object. ("+ sOriginalText +")");
			}
			
		}
		
		String[] aGetters = sGetter.split("\\.",2);		
		String sGetterName = "get" + aGetters[0].substring(0,1).toUpperCase() + aGetters[0].substring(1);
		
		// Get the getter method
		Method oMethod = Expression.getClassMethod(oBean,sGetterName);
		
		// if it does not exist
		if(oMethod == null){
			throw new java.lang.Exception("'"+ oBean.getClass().getName() +"' does not have a method named '"+ sGetterName +"'. ("+ sOriginalText +")");
		}
		
		Object oNewBean = null;
		
		try{
			oNewBean = oMethod.invoke(oBean);
		}catch(java.lang.Exception e){
			throw new java.lang.Exception("Cannot call '"+ sGetterName +"' on '"+ oBean.getClass().getName() +"'. ("+ sOriginalText +")");
		}
		
		if(aGetters.length==2){
			return Expression.buildBean(sOriginalText,oNewBean,aGetters[1]);
		}else{
			return oNewBean;
		}
		
	}
	
	
	private static Method getClassMethod(Object oObject,String sMethodName){
		
		Class oClass = oObject.getClass();
		Method[] aMethods = oClass.getMethods();
		
		for(int i=0,len=aMethods.length; i<len; i++){
			if(aMethods[i].getName().equals(sMethodName))
				return aMethods[i];		
		}
		
		return null;
		
	}
	

}