package codotos.tags.expression.operators;

public class NotEqual implements codotos.tags.expression.ExpressionOperator {
	
	public int getParameterCount() {
		return 2;
	}
	
	public int getPrecedence() {
		return 0;
	}
	
	public String getOperator() {
		return "ne ";
	}
	
	public Object perform(Object ... parameters) {
		if(parameters[0]==null||parameters[1]==null) {
			return parameters[0]!=parameters[1];
		} else {
			return !parameters[0].equals(parameters[1]);
		}
	}
	
}