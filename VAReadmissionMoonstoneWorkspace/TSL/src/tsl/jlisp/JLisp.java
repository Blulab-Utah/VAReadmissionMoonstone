/*
Copyright 2018 Wendy Chapman (wendy.chapman\@utah.edu) & Lee Christensen (leenlp\@q.com)

Licensed under the Apache License, Version 2.0 (the \"License\");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an \"AS IS\" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package tsl.jlisp;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

/*******
 * 9/26/2007 Note: NEED TO REFACTOR! i- Create a class for each data type, e.g.
 * JLString and JLFloat. Let each type have methods toString() (e.g. JLString
 * should print the quoted string), toJava() (e.g. convert from JLFloat to
 * Float, and from Sexp to a Vector), valueOf(), etc.
 * 
 *********/

public class JLisp {
	public String inputString = null;
	public int inputStringLength = 0;
	public int inputIndex = 0;
	public Vector tokens = null;
	public int tokenIndex = 0;
	public Vector symbolTables = new Vector(0);
	public SymbolTable currentSymbolTable = null;
	public Object currentValue = null;
	public boolean preserveSymbolCase = false;
	private JLispObject lastReferenceObject = null;
	public static JLisp jLisp = null;

	public static void main(String[] args) {
		String pinsname = "/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/Wendy_Knowtator/ShARe Projects_MIMIC/Share 01_1_18_12/Annotated/Share_01_Annotated_David/Share_01_David/SHARe_Jan18_2012_base.pins";
		String pontname = "/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/Wendy_Knowtator/ShARe Projects_MIMIC/Share 01_1_18_12/Annotated/Share_01_Annotated_David/Share_01_David/SHARe_Jan18_2012_base.pont";
		JLisp jl = getJLisp();
		Sexp sexp = (Sexp) jl.loadFile(pontname, true);
		Vector ontv = JLUtils.convertSexpToJVector(sexp);
		sexp = (Sexp) jl.loadFile(pinsname, true);
		Vector instv = JLUtils.convertSexpToJVector(sexp);
		for (Object o : ontv) {
			Vector sv = (Vector) o;
			Vector slotv = VUtils.assocAll("single-slot", sv);
			if (slotv != null) {
				System.out.println(slotv + "\n");
			}
		}
	}

	public static JLisp getJLisp() {
		if (jLisp == null) {
			jLisp = new JLisp();
		}
		return jLisp;
	}

	public static JLisp getJLisp(boolean preserveSymbolCase) {
		if (jLisp == null) {
			jLisp = new JLisp();
			jLisp.setPreserveSymbolCase(preserveSymbolCase);
		}
		return jLisp;
	}

	JLisp() {
		jLisp = this;
		this.symbolTables.add(SymbolTable.globalSymbolTable);
		initFunctions();
	}

	public Object loadFile(String filename) {
		return loadFile(filename, false);
	}

	public JLispObject loadFile(String filename, boolean addParens) {
		JLispObject rv = null;
		try {
			JLispObject read = null;
			tokenizeFile(filename, addParens);
			while ((read = readExpr()) != null) {
				rv = eval(read);
			}
		} catch (JLispException e) {
			JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
		} catch (Exception e) {
			String msg = "LISP: Cannot complete operation:\n"
					+ StrUtils.getStackTrace(e);
			JOptionPane.showMessageDialog(new JFrame(), msg);
		}
		return rv;
	}

	public JLispObject evalString(String str) throws JLispException {
		tokenizeString(str);
		checkParens();
		JLispObject rv = readExpr();
		return eval(rv);
	}

	void tokenizeString(String input) {
		if (input != null) {
			this.inputString = input;
			this.inputStringLength = input.length();
			this.inputIndex = 0;
			this.tokens = Token.readTokensFromInput(this);
			this.tokenIndex = 0;
		}
	}

	void tokenizeFile(String filename, boolean addParens) {
		String input = FUtils.readFile(filename);
		if (addParens) {
			input = "'(\n" + input + "\n)";
		}
		tokenizeString(input);
	}

	void checkParens() throws JLispException {
		int numleftparens = 0;
		int numrightparens = 0;
		Token token = null;
		for (int i = 0; i < tokens.size(); i++) {
			token = (Token) tokens.elementAt(i);
			if (token.equals(Token.STARTPARENTOKEN)) {
				numleftparens++;
			} else if (token.equals(Token.ENDPARENTOKEN)) {
				numrightparens++;
			}
		}
		if (numleftparens > 0) {
			if (numleftparens != numrightparens) {
				throw new JLispException("Incorrect parenthesis matching");
			}
			if (!token.equals(Token.ENDPARENTOKEN)) {
				throw new JLispException(
						"Incorrect token placement visavis parentheses");
			}
		}
	}

	public JLispObject readExpr() throws JLispException {
		JLispObject rv = null;
		Token t = Token.readNextToken(this);
		if (t != null) {
			if (t.type == Token.STARTPAREN) {
				rv = readSexp();
			} else if (t.type == Token.SYMBOL) {
				rv = (Symbol) t.value;
			} else if (t.type == Token.NUMBER || t.type == Token.STRING) {
				rv = new JavaObject(t.value);
			} else if (t.type == Token.FUNCTION) {
				rv = readFunction();
			} else if (t.type == Token.QUOTE) {
				rv = readQuote();
			} else if (t.type == Token.BACKQUOTE) {
				rv = readBackquote();
			} else if (t.type == Token.COMMA) {
				rv = readComma();
			} else {
				throw new JLispException("Unknown token, ", t);
			}
		}
		return rv;
	}

	Sexp readFunction() throws JLispException {
		JLispObject rv = readExpr();
		return Sexp.doCons(Symbol.functionSym, Sexp.doList(rv));
	}

	Sexp readQuote() throws JLispException {
		JLispObject rv = readExpr();
		return Sexp.doCons(Symbol.quoteSym, Sexp.doList(rv));
	}

	// 4/1/2013
	Sexp readBackquote() throws JLispException {
		JLispObject rv = readExpr();
		return Sexp.doCons(Symbol.backquoteSym, Sexp.doList(rv));
	}

	// 4/1/2013
	Sexp readComma() throws JLispException {
		JLispObject rv = readExpr();
		return Sexp.doCons(Symbol.commaSym, Sexp.doList(rv));
	}

	JLispObject readSexp() throws JLispException {
		Vector<Token> tokensSoFar = new Vector(0);
		JLispObject rv = null;
		Vector<JLispObject> items = new Vector(0);
		JLispObject s = Symbol.NIL;
		boolean finished = false;
		while (!finished) {
			Token t = Token.peekNextToken(this);
			if (t == null) {
				throw new JLispException("Premature end to Sexpr: "
						+ tokensSoFar);
			}
			tokensSoFar.add(t);
			if (t.type == Token.ENDPAREN) {
				Token.readNextToken(this);
				finished = true;
			} else if (t.type == Token.DOT) {
				Token.readNextToken(this);
				s = readExpr();
				Token.readNextToken(this);
				finished = true;
			} else {
				rv = readExpr();
				items.add(rv);
			}
		}
		for (int i = items.size() - 1; i >= 0; i--) {
			JLispObject o = items.elementAt(i);
			s = Sexp.doCons(o, s);
		}
		if (s == null) {
			throw new JLispException("Unable to read Sexpr");
		}
		return s;
	}

	public static JLispObject evList(JLispObject exp) {
		if (JLUtils.isNil(exp)) {
			return Symbol.NIL;
		} else {
			Sexp s = (Sexp) exp;
			return Sexp.doCons(eval(s.getCar()), evList(s.getCdr()));
		}
	}

	public static JLispObject eval(JLispObject o) {
		JLispObject rv = null;
		if (JLUtils.isSymbol(o)) {
			rv = o;
		} else if (JLUtils.isFloat(o) || JLUtils.isString(o)) {
			rv = o;
		} else if (JLUtils.isCons(o)) {
			Sexp s = (Sexp) o;
			Symbol sym = (Symbol) s.getCar();
			if (JavaFunction.isJFunction(sym.getValue())) {
				rv = JavaFunction.applyJFunctionSymbol(s);
			} else if (LispFunction.isLFunction(sym.getValue())) {
				rv = LispFunction.applyLFunctionSymbol(s);
			}
		} else {
			rv = o;
		}
		return rv;
	}

	public boolean isPreserveSymbolCase() {
		return preserveSymbolCase;
	}

	public void setPreserveSymbolCase(boolean preserveSymbolCase) {
		this.preserveSymbolCase = preserveSymbolCase;
	}

	public static JLispObject getLastReferenceObject() {
		return JLisp.jLisp.lastReferenceObject;
	}

	public static void setLastReferenceObject(JLispObject lastReferenceObject) {
		JLisp.jLisp.lastReferenceObject = lastReferenceObject;
	}

	public static void resetLastReferenceObject() {
		JLisp.jLisp.lastReferenceObject = null;
	}

	// Set doEval=false if we let the function control evaluation of arguments,
	// rather than passing in evaluated arguments.
	public static void initFunctions() {
		new JavaFunction(Symbol.functionSym, "evalFunction",
				LispFunction.SEXPPARAM, false);
		new JavaFunction(Symbol.defunSym, "evalDefun", LispFunction.SEXPPARAM,
				false);
		new JavaFunction(Symbol.quoteSym, "evalQuote", LispFunction.SEXPPARAM,
				false);
		new JavaFunction(Symbol.setqSym, "evalSetq", LispFunction.SEXPPARAM,
				false);
		new JavaFunction(Symbol.applySym, "evalApply", LispFunction.SEXPPARAM,
				true);
		new JavaFunction(Symbol.consSym, "evalCons", LispFunction.SEXPPARAM,
				true);
		new JavaFunction(Symbol.carSym, "evalCar", LispFunction.SEXPPARAM, true);
		new JavaFunction(Symbol.cdrSym, "evalCdr", LispFunction.SEXPPARAM, true);
		new JavaFunction(Symbol.addSym, "addFloats", LispFunction.OBJECTPARAM,
				true);
	}

}
