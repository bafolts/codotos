package codotos.tags;

import codotos.tags.TagContext;
import codotos.tags.expression.ExpressionParser;

public final class Expression {

	public static Object evaluate(String sRawExpression,TagContext oTagContext) throws codotos.exceptions.TagRuntimeException {
		
		try{
		
			return (new ExpressionParser(sRawExpression,oTagContext)).run();
			
		}catch(codotos.exceptions.ExpressionRuntimeException e){
			
			codotos.exceptions.TagRuntimeException oException = new codotos.exceptions.TagRuntimeException("Error executing EL "+ sRawExpression);
			
			oException.initCause(e);
			
			throw oException;
			
		}

	}

}