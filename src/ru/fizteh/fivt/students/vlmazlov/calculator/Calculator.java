package ru.fizteh.fivt.students.vlmazlov.calculator;
import java.util.Vector;
import java.util.Scanner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.lang.StringBuilder;

class WrongArithmeticExpressionException extends Exception {
	public WrongArithmeticExpressionException() { 
		super(); 
	}
	public WrongArithmeticExpressionException(String message) { 
		super(message); 
	}
	public WrongArithmeticExpressionException(String message, Throwable cause) { 
		super(message, cause); 
	}
	public WrongArithmeticExpressionException(Throwable cause) { 
		super(cause); 
	}
};

public class Calculator {

	private enum OperationType {
		sum("+"),
		sub("-"),
		mult("*"),
		div("/"),
		end("."),
		close(")");

		private String operationType;

		private OperationType(String type) {
			operationType = type;
		}

		public static OperationType getByType(String type) {
			
			for (OperationType operation: OperationType.values()) {
				if (operation.operationType.equals(type)) {
					return operation;
				}
			}

			throw new NoSuchElementException("Operation " + type + " is of unknown type");
		}

		public String getType() {
			return operationType;
		}
	}

	private final static int radix = 17;
	private String curToken;
	private char curChar;
	private boolean spaceSkipped; //vital for detecting spaces inside an integer
	private InputStream expression;

	Calculator() {
		curToken = "";
	}

	public static void	 main(String[] args) {
		String arg = "";
		int curRes;
		Calculator calc = new Calculator();

		StringBuilder argBuilder = new StringBuilder();

		for (String tmp : args) {
			argBuilder.append(tmp);
		}

		arg = argBuilder.toString();
		System.out.println(arg);
		if (arg.equals("")) {
			System.out.println("Usage: valid aritmetic expression, possibly divided in several strings");
			System.exit(1);
		}

		calc.expression = new ByteArrayInputStream(arg.getBytes()); 
		
		try {
			System.out.println(calc.countExpression());
		} catch (WrongArithmeticExpressionException ex) {
			System.out.println(ex.getMessage() + ". Please try again.");
			System.exit(2);
		} catch (ArithmeticException ex) {
			System.out.println("Division by zero inside the expression. Please try again.");
			System.exit(3);
		} catch (NoSuchElementException ex) {
			System.out.println(ex.getMessage() + ". Please try again.");
			System.exit(4);
		}
	}

	private boolean isNumber(String toCheck) {
		try {
			int tmp = Integer.parseInt(toCheck, radix);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;	
	}

	private boolean isValid() {
		return ((('0' <= curChar) && ('9' >= curChar)) || (('A' <= curChar) && ('A' + radix - 10 > curChar)));
	}

	private void parseFail(String errorMessage) throws WrongArithmeticExpressionException {
		throw new WrongArithmeticExpressionException(errorMessage);
	}

	public double countExpression() throws WrongArithmeticExpressionException {
		nextChar();
		nextToken();
		double res = parseExpression();

		if (!curToken.equals(".")) {
			throw new WrongArithmeticExpressionException(curToken + " out of place");
		}

		return res;
	}

	//In all following functions it is assumed that current token
	//is the beginning of the parsed expression

	private double parseExpression() throws WrongArithmeticExpressionException {
		//Expression is considered to be a sequence of summators,
		//connected by + and -

		double res = parseSummator();

		while ((curToken.equals("+")) || (curToken.equals("-"))) {

			OperationType operation = OperationType.getByType(curToken);

			switch(operation) {
			case sum:
				nextToken();
				
				res += parseSummator();
				
				if (res == Double.POSITIVE_INFINITY) {
					parseFail("Arithmetic overflow"); 
				}

				break;
			case sub:
				nextToken();

				res -= parseSummator();
				
				if (res == Double.NEGATIVE_INFINITY) {
					parseFail("Arithmetic overflow"); 
				}

				break;
			case end:
			case mult:
			case div:
			case close:
				return res;
			default:
				if (operation == OperationType.end) {
					parseFail("Expression unfinished");
				} else {
					parseFail(operation.getType() + " out of place");
				}
			}
		}
		return res;
 	}

	private double parseSummator() throws WrongArithmeticExpressionException {
		//Summator is a sequence of multipliers
		//connected with * or /

		double res = parseMultiplier();

		while ((curToken.equals("*")) || (curToken.equals("/"))) {

			OperationType operation = OperationType.getByType(curToken);

			switch(operation) {
			case mult:
				nextToken();
				
				res *= parseMultiplier();

				if (res == Double.POSITIVE_INFINITY) {
					parseFail("Arithmetic overflow"); 
				} else if (res == Double.NEGATIVE_INFINITY) {
					parseFail("Arithmetic overflow"); 
				}


				break;
			case div:
				nextToken();
				
				double tmpRes = parseMultiplier();
				if (Math.abs(tmpRes) >= 1e-7) { //!= 0
					if (Math.abs(res) >= 1e-7) { //!= 0
						res /= tmpRes;

						if (Math.abs(res) < 1e-7) {//== 0
							parseFail("Arithmetic underflow");
						}
					} else {
						res = 0;
					}

				} else throw new ArithmeticException();	

				break;
			case end:
			case sum:
			case sub:
			case close:
				return res;
			default:
				if (operation == OperationType.end) {
					parseFail("Expression unfinished");
				} else {
					parseFail(operation.getType() + " out of place");
				}
			}
		}
		return res;
	}

	private double parseMultiplier() throws WrongArithmeticExpressionException {
		//Multiplier is either a number or an expression in brackets

		double res = 0;

		if (curToken.equals("(")) {
			nextToken();
			
			res = parseExpression(); 

			if (!curToken.equals(")")) {
				parseFail("Unbalanced brackets");
			}

			nextToken();

		} else if (isNumber(curToken)) { 
			res = Integer.parseInt(curToken, radix);
			nextToken();

		} else if (curToken.equals("-")) {
			nextToken();
			res = -parseMultiplier();

		} else {
			if (curToken.equals(".")) {
				parseFail("Expression unfinished");
			} else {
				parseFail(curToken + " out of place");
			}
		}

		return res;
	} 

	private void nextChar() {
		try {
			if (0 < expression.available()) {
				curChar = (char)expression.read();
			}  else {
				curChar = '.';
			}
		} catch (IOException ex) {
			curChar = '.';
		}
		if (' ' == curChar) {
			spaceSkipped = true;
			nextChar();
		}
	}

	//Valid tokens:
	// (,),+,-,/,*,., string representing a number
	//. - end of expression

	private void nextToken() throws WrongArithmeticExpressionException {
		//nextToken is never called recursively, therefore, it's valid
		spaceSkipped = false;
		
		switch (curChar) {
		case '(':
		case ')':
		case '+':
		case '-':
		case '/':
		case '*':
		case '.':
			char[] curCharArray = {curChar};
			curToken = new String(curCharArray);	
			nextChar();
			break;
		default:
			if (!isValid()) {
				parseFail("Unknown symbol " + curChar); 
			}
			
			StringBuilder curTokenBuilder = new StringBuilder();

			while ((isValid()) && (!spaceSkipped)) {
				curTokenBuilder.append(curChar);
				nextChar();
			}
			
			spaceSkipped = false;

			curToken = curTokenBuilder.toString();
		}
	}
}