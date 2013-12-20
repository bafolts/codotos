package codotos.tags.expression.operators;

public class LessThanEqual implements codotos.tags.expression.ExpressionOperator {
	
	public int getParameterCount() {
		return 2;
	}
	
	public int getPrecedence() {
		return 0;
	}
	
	public String getOperator() {
		return "le ";
	}
	
	public Object perform(Object ... parameters) {
		if(parameters[0] instanceof Integer) {
			int iLhs = ((Integer)parameters[0]).intValue();
			if(parameters[1] instanceof Double) {
				return iLhs<=((Double)parameters[1]).doubleValue();
			}
			return iLhs<=((Integer)parameters[1]).intValue();
		} else if(parameters[0] instanceof Double) {
			double dLhs = ((Double)parameters[0]).doubleValue();
			if(parameters[1] instanceof Integer) {
				return dLhs<=((Integer)parameters[1]).intValue(); 
			}
			return dLhs<=((Double)parameters[1]).doubleValue();
		}
		throw new RuntimeException("Error performing operation.");
	}
	
}