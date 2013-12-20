package codotos.tags.expression;

import java.lang.reflect.Method;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Stack;
import java.util.ArrayList;

import codotos.tags.expression.operators.*;
import codotos.tags.TagContext;
import codotos.tags.Expression;

public final class ExpressionParser {

	private int m_iPosition = 0;
	private int m_iLength = 0;

	private String m_sContent = null;
	private TagContext m_tContext = null;

	private Stack<ExpressionOperator> m_sOperators = new Stack<ExpressionOperator>();
	private Stack<Object> m_sVariables = new Stack<Object>();
	
	private final static ArrayList<ExpressionOperator> m_aOperators = new ArrayList<ExpressionOperator>(15);
	static {
		m_aOperators.add(new Divides());
		m_aOperators.add(new Empty());
		m_aOperators.add(new Equal());
		m_aOperators.add(new GreaterThan());
		m_aOperators.add(new GreaterThanEqual());
		m_aOperators.add(new LessThan());
		m_aOperators.add(new LessThanEqual());
		m_aOperators.add(new LogicalOr());
		m_aOperators.add(new LogicalAnd());
		m_aOperators.add(new NotEqual());
		m_aOperators.add(new Plus());
		m_aOperators.add(new Minus());
		m_aOperators.add(new Multiplies());
		m_aOperators.add(new Not());
		m_aOperators.add(new Remainder());
	}

	private final static Pattern PATTERN_NUMBER = Pattern.compile("^(-)?[0-9.]+");
	private final static Pattern PATTERN_VARIABLE = Pattern.compile("^[A-Za-z][^\\s\\+\\-\\*\\/\\%\\}]*");

	public ExpressionParser(String line, TagContext oTagContext) {
		m_tContext = oTagContext;
		m_sContent = line.trim();
		m_iLength = m_sContent.length();
	}

	public boolean isFiller(char c) {
		return (c==' '||c=='\t');
	}

	public String getCurrentString() {
		return m_sContent.substring(m_iPosition);
	}

	public char getCurrentCharacter() {
		return m_sContent.charAt(m_iPosition);
	}
	
	public boolean moreToParse() {
		return m_iPosition<m_iLength;
	}

	public String getAndSetNextNumber(String s) {
		Matcher m = PATTERN_NUMBER.matcher(s);
		if(m.lookingAt()) {
			String sReturn = m.group();
			m_iPosition += sReturn.length();
			return sReturn;
		} else {
			return null;
		}
	}

	public Object getAndSetNextVariable(String s) throws codotos.exceptions.ExpressionRuntimeException {
		Matcher m = PATTERN_VARIABLE.matcher(s);
		if(m.lookingAt()) {
			String sReturn = m.group();
			m_iPosition += sReturn.length();
			if(sReturn.equals("true")||sReturn.equals("false")) {
				return new Boolean(sReturn);
			} else if(sReturn.equals("null")) {
				return sReturn;
			} else {
				return getVariable(sReturn);
			}
		}
		return null;
	}

	private ExpressionOperator getAndSetNextOperator(String s) {
		for(ExpressionOperator operator : m_aOperators) {
			if(s.startsWith(operator.getOperator())) {
				m_iPosition+=operator.getOperator().length();
				return operator;
			}
		}
		return null;
	}

	public Object performOperator(ExpressionOperator op) {
		int length = op.getParameterCount();
		Object[] aParameters = new Object[length];
		for(int i=0;i<length;i++) {
			aParameters[length-1-i] = m_sVariables.pop();
		}
		return op.perform(aParameters);
	}

	private String getAndSetNextParenthesis() {
		StringBuilder sResult = new StringBuilder();
		boolean bFoundStart = false;
		while(moreToParse()) {
			char curChar = getCurrentCharacter();
			if(isFiller(curChar)) {
				m_iPosition++;
			} else if(bFoundStart==false&&curChar=='(') {
				bFoundStart = true;
				m_iPosition++;
			} else if(curChar=='\'') {
				sResult.append("'");
				sResult.append(getAndSetNextString());
				sResult.append("'");
			} else if(curChar=='"') {
				sResult.append('"');
				sResult.append(getAndSetNextString());
				sResult.append('"');
			} else if(bFoundStart&&curChar=='(') {
				sResult.append("(");
				sResult.append(getAndSetNextParenthesis());
				sResult.append(")");
			} else if(bFoundStart&&curChar==')') {
				m_iPosition++;
				return sResult.toString();
			} else {
				sResult.append(curChar);
				m_iPosition++;
			}
		}
		throw new RuntimeException("Unclosed parenthetical in expression.");
	}

	private String getAndSetNextString() {
		StringBuilder sResult = new StringBuilder();
		char cStartChar = getCurrentCharacter();
		m_iPosition++;
		while(moreToParse()) {
			char curChar = getCurrentCharacter();
			if(curChar=='\\') {
				m_iPosition++;
				char nChar = getCurrentCharacter();
				if(nChar=='n') {
					sResult.append("\n");
				} else if(nChar=='r') {
					sResult.append("\r");
				} else if(nChar=='t') {
					sResult.append("\t");
				} else {
					sResult.append(curChar);
					sResult.append(nChar);
				}
				m_iPosition++;
			} else if(curChar==cStartChar) {
				m_iPosition++;
				return sResult.toString();
			} else {
				sResult.append(curChar);
				m_iPosition++;
			}
		}
		throw new RuntimeException("Unterminated string in expression.");
	}

	public Object run() throws codotos.exceptions.ExpressionRuntimeException {

		StringBuilder sOutput = new StringBuilder();
		boolean bFoundInlineString = false;
		Object oResult = null;

		while(moreToParse()) {
			if(getCurrentString().startsWith("${")) {
				m_iPosition+=2;
				oResult = getAndSetNextExpression();
				sOutput.append(oResult);
			} else {
				bFoundInlineString = true;
				sOutput.append(getCurrentCharacter());
				m_iPosition++;
			}
		}

		if(bFoundInlineString)
			oResult = sOutput.toString();
		
		return oResult;

	}

	public Object getAndSetNextExpression() throws codotos.exceptions.ExpressionRuntimeException {

		boolean bLastOperator = true;

		String curString = null;
		char curChar = ' ';


		while(moreToParse()) {


			String sNumber = null;
			Object oVariable = null;
			ExpressionOperator eOperator = null;


			//check characters
			curChar = getCurrentCharacter();
			if(isFiller(curChar)) {
				m_iPosition++;
				continue;
			} else if(curChar=='}') {
				m_iPosition++;
				break;
			} else if(curChar=='(') {
				m_sVariables.push((new ExpressionParser("${"+getAndSetNextParenthesis()+"}",m_tContext)).run());
			} else if(curChar=='\'') {
				m_sVariables.push(getAndSetNextString());
			} else if(curChar=='"') {
				m_sVariables.push(getAndSetNextString());
			}
			
			//pull and check strings, pull string here for efficiency
			else if((curString = getCurrentString())!=null&&bLastOperator&&(sNumber=getAndSetNextNumber(curString))!=null) {
				if(sNumber.indexOf(".")>-1) {
					m_sVariables.push(new Double(sNumber));
				} else {
					m_sVariables.push(new Integer(sNumber));
				}
			} else if((eOperator = getAndSetNextOperator(curString))!=null) {
				int iPrecedence = eOperator.getPrecedence();
				while(!m_sVariables.empty()&&!m_sOperators.empty()&&iPrecedence<=m_sOperators.peek().getPrecedence()) {
					m_sVariables.push(performOperator(m_sOperators.pop()));
				}
				m_sOperators.push(eOperator);
			} else if((oVariable = getAndSetNextVariable(curString))!=null) {
				if(oVariable.equals("null")) {
					oVariable = null;
				}
				m_sVariables.push(oVariable);
			} else {
				throw new RuntimeException("Error Parsing Expression at character "+m_iPosition+" '"+curString+"'");
			}

			bLastOperator = eOperator!=null;

		}

		while(!m_sVariables.empty()&&!m_sOperators.empty()) {
			m_sVariables.push(performOperator(m_sOperators.pop()));
		}

		if(m_sVariables.size()>1) {
			throw new codotos.exceptions.ExpressionRuntimeException("Error parsing expression, unknown operator in expression.");
		} else if(m_sVariables.empty()) {
			throw new codotos.exceptions.ExpressionRuntimeException("Unbalaced operators in expression.");
		}

		return m_sVariables.pop();
	}

	private Object buildBean(String sOriginalText,Object oBean,String sGetter) throws codotos.exceptions.ExpressionRuntimeException {

		if(oBean == null) {
			throw new codotos.exceptions.ExpressionRuntimeException("No '"+ sGetter +"' on null object. ("+ sOriginalText +")");
		} else {

			int iSplitPos = sGetter.indexOf(".");
			String sFirstHalf = (iSplitPos==-1?sGetter:sGetter.substring(0,iSplitPos));
			String sGetterName = String.format("get%s%s",sFirstHalf.substring(0,1).toUpperCase(),sFirstHalf.substring(1));

			Method oMethod = null;
			try {
				oMethod = oBean.getClass().getMethod(sGetterName);
			} catch(NoSuchMethodException e) {
				throw new codotos.exceptions.ExpressionRuntimeException("'"+ oBean.getClass().getName() +"' does not have a method named '"+ sGetterName +"'. ("+ sOriginalText +")");
			}

			Object oNewBean = null;
			try{

				oNewBean = oMethod.invoke(oBean);

			} catch(java.lang.Exception e){

				codotos.exceptions.ExpressionRuntimeException oException = new codotos.exceptions.ExpressionRuntimeException("Cannot call '"+ sGetterName +"' on '"+ oBean.getClass().getName() +"'. ("+ sOriginalText +")");

				oException.initCause(e);

				throw oException;

			}

			if(iSplitPos!=-1){
				return buildBean(sOriginalText,oNewBean,sGetter.substring(iSplitPos+1));
			}else{
				return oNewBean;
			}

		}
		
	}

	private Object getVariable(String sVariableName) throws codotos.exceptions.ExpressionRuntimeException {

		int iSplitPos = sVariableName.indexOf(".");
		String sFirstHalf = iSplitPos==-1?sVariableName:sVariableName.substring(0,iSplitPos);
		Object oVariable = m_tContext.getVariable(sFirstHalf);

		if(iSplitPos!=-1) {
			oVariable = buildBean(sVariableName,oVariable,sVariableName.substring(iSplitPos+1));
		}

		return oVariable==null?"null":oVariable;

	}

}