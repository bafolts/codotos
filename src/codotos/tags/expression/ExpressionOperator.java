package codotos.tags.expression;

public interface ExpressionOperator {

	int getParameterCount();
	int getPrecedence();
	String getOperator();
	Object perform(Object ... parameters);

}