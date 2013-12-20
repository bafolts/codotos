package codotos.tags.expression.operators;

public class Not implements codotos.tags.expression.ExpressionOperator {

	public int getParameterCount() {
		return 1;
	}

	public int getPrecedence() {
		return 1;
	}

	public String getOperator() {
		return "not ";
	}

	public Object perform(Object ... parameters) {
		return !(Boolean)parameters[0];
	}

}