package codotos.tags.expression.operators;

public class LogicalOr implements codotos.tags.expression.ExpressionOperator {
	
	public int getParameterCount() {
		return 2;
	}
	
	public int getPrecedence() {
		return 1;
	}
	
	public String getOperator() {
		return "or ";
	}
	
	public Object perform(Object ... parameters) {
		return (Boolean)parameters[0]||(Boolean)parameters[1];
	}
	
}