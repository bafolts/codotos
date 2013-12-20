package codotos.tags.expression.operators;

public class Remainder implements codotos.tags.expression.ExpressionOperator {
	
	public int getParameterCount() {
		return 2;
	}
	
	public int getPrecedence() {
		return 2;
	}
	
	public String getOperator() {
		return "%";
	}
	
	public Object perform(Object ... parameters) {
		if(parameters[0] instanceof Integer) {
			int iLhs = ((Integer)parameters[0]).intValue();
			if(parameters[1] instanceof Double) {
				return new Double(iLhs%((Double)parameters[1]).doubleValue()); 
			}
			return new Integer(iLhs%((Integer)parameters[1]).intValue());
		} else if(parameters[0] instanceof Double) {
			double dLhs = ((Double)parameters[0]).doubleValue();
			if(parameters[1] instanceof Integer) {
				return new Double(dLhs%((Integer)parameters[1]).intValue()); 
			}
			return new Double(dLhs%((Double)parameters[1]).doubleValue());
		}
		throw new RuntimeException("Error performing operator.");
	}
	
}