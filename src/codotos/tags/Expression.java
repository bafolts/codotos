package codotos.tags;


import codotos.tags.TagContext;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Method;


public class Expression {


	private String sRawExpression = "";
	
	private TagContext oTagContext = null;


	public Expression(String sRawExpression,TagContext oTagContext){
	
		// TODO
		this.sRawExpression = sRawExpression;
		this.oTagContext = oTagContext;
	
	}


	public Object evaluate() throws java.lang.Exception {
	
		return Expression.compileLine(this.sRawExpression,this.oTagContext);

	}
	
	
	private static Object compileLine(String sLine,TagContext oTagContext) throws java.lang.Exception {
		
		// "  blah  ${ Boo } "
		// (String) "  blah  " + EVAL +" "
		
		Pattern oPattern = Pattern.compile("^(.*?)\\$\\{[\\s]*(.*?)[\\s]*\\}(.*)");
		Matcher oMatcher = oPattern.matcher(sLine);
		
		if(!oMatcher.matches()){
			return sLine;
		}
		
		// if it only contains a big ${} then it can return complex things
		if(oMatcher.group(1).isEmpty() && oMatcher.group(3).isEmpty()){
		
			return Expression.compileExpression(oMatcher.group(2),oTagContext);
		
		// if it contains leading or ending characters, it is a string.
		}else{
			
			return	oMatcher.group(1) + 
					new String(Expression.compileExpression(oMatcher.group(2),oTagContext).toString()) + 
					new String(Expression.compileLine(oMatcher.group(3),oTagContext).toString());
			
		}
		
	}
	
	
	private static Object compileExpression(String sRawText,TagContext oTagContext) throws java.lang.Exception{
		
		// TODO
		// Examine the incoming string for conditionals, comparators, literals, etc
		
		if(sRawText.isEmpty())
			return "";
		
		String[] aGetters = sRawText.split("\\.",2);		
		String sVariableName = aGetters[0];
		
		Object oVariable = oTagContext.getVariable(sVariableName);
		
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
	
	private static Object buildBean(String sOriginalText, Object oBean,String sGetter) throws java.lang.Exception{
		
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
		
		
		
		
		
		/*
		Pattern oPattern = Pattern.compile("^([0-9]+)[\s]*(.*)$");
		Matcher oMatcher = oPattern.matcher(sRawString);
		
		// Look for numbers
		// look for seperators
		// look for expression statements
		// look for strings
		// look for variables
		
		// If its a raw number (ie blah.test == 3)
		if(preg_match("/^([0-9]+)[\s]*(.*)$/",$sText,$aMatches)){
			return $aMatches[1] . TagCompiler::compileExpression($aMatches[2]);
		}
		
		// If its a string ... (ie blah.test == 'hello world')
		if(preg_match("/^'(.*?)'[\s]*(.*)$/",$sText,$aMatches)){
			return "\"". TagCompiler::sanitizeString($aMatches[1]) . "\"" . TagCompiler::compileExpression($aMatches[2]);
		}
		
		// !ABC !(ABC)
		if(preg_match("/^(!)[\s]*(.*)$/",$sText,$aMatches)){
			return TagCompiler::compileExpressionHelper($aMatches[1],$aMatches[2]);
		}
		
		// = 'boo'  = ABC  ='boo' =ABC
		if(preg_match("/^([\&\|\!\=]+)[\s]*(.*)$/",$sText,$aMatches)){
			return TagCompiler::compileExpressionHelper($aMatches[1],$aMatches[2]);
		}
		
		// ABC=  ABC||  ABC&&
		if(preg_match("/^([^\s]+?)[\s]*([\&\|\!\'\=].*)$/",$sText,$aMatches)){
			return TagCompiler::compileExpressionHelper($aMatches[1],$aMatches[2]);
		}
		
		// ABC eq  not ABC  ABC = 
		if(preg_match("/^([^\s]+?)[\s]+(.*)$/",$sText,$aMatches)){
			return TagCompiler::compileExpressionHelper($aMatches[1],$aMatches[2]);
		}
		
		return TagCompiler::compileExpressionHelper($sText,"");
		
		//throw new java.lang.Exception("TODO - '". $sText ."' - Get yourself out of this pickle TagCompiler::compileExpression!");
		*/
		
	
	
	/*
		Used to analyze a provided string expression and turn it into code
		NOTE: This will be replaced with the translator
		
		@param sText String String to be converted into code
		
		@return null
	*/
	/*
	private static function compileExpressionHelper($sContent,$sAfter){
		
		switch ($sContent){
		
			case "eq":
			case "==":
				return "==". TagCompiler::compileExpression($sAfter);
				break;
		
			case "neq":
			case "!=":
				return "!=". TagCompiler::compileExpression($sAfter);
				break;
		
			case "not":
			case "!":
				return "!(". TagCompiler::compileExpression($sAfter) .")";
				break;
		
			case "||":
			case "or":
				return ") || (". TagCompiler::compileExpression($sAfter) .")";
				break;
				
			case "&&":
			case "and":
				return ") && (". TagCompiler::compileExpression($sAfter) .")";
				break;
		
			case "=":
				throw new java.lang.Exception("'". $sContent ."' not supported in \${}");
				break;
		
			case "":
				throw new java.lang.Exception("Error in TagCompiler::compileExpressionHelper");
				break;
			
			default:
				
				$aVariables = preg_split("/\./",$sContent);
				
				if(count($aVariables)==0){
					throw new java.lang.Exception("Error compiling tag, \${} expression without anything inside");
				}
				
				// Look for page variable first, then look for request variable
				$toReturn = "TagCompiler::getVariable(\this,\"". TagCompiler::sanitizeString($aVariables[0]) ."\")";
				
				for($i=1,$len=count($aVariables); $i<$len; $i++){
					$sGetterName = $aVariables[$i];
					$sGetterName[0] = strtoupper($sGetterName[0]);
					$toReturn .= ".get". $sGetterName ."()";
				}
				
				return "(". $toReturn . TagCompiler::compileExpression($sAfter) .")";
			
			
		}
	
	}
	*/
	

}