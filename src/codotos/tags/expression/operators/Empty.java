package codotos.tags.expression.operators;

public class Empty implements codotos.tags.expression.ExpressionOperator {

	public int getParameterCount() {
		return 1;
	}

	public int getPrecedence() {
		return 1;
	}

	public String getOperator() {
		return "empty ";
	}

	public Object perform(Object ... parameters) {
		return parameters[0]==null||((parameters[0] instanceof String) && ((String)parameters[0]).isEmpty());
	}

}