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
package tsl.tsllisp;

import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;

public class TLisp {
	protected String inputString = null;
	protected int inputStringLength = 0;
	protected int inputIndex = 0;
	protected Vector tokens = null;
	protected int currentLineNumber = 0;
	protected int tokenIndex = 0;
	protected Object currentValue = null;
	protected boolean preserveSymbolCase = false;
	protected TLObject lastReferenceObject = null;
	protected JFunction lambdaJFunction = null;
	protected SymbolTable globalSymbolTable = null;
	// 5/31/2013
	protected SymbolTable functionSymbolTable = null;
	public static TLisp tLisp = null;
	protected SymbolTable[] symbolTables = new SymbolTable[MAX_SYMBOL_TABLES];
	protected int currentSymbolTableIndex = 0;
	protected KnowledgeBase TSLKnowledgeBase = null;
	protected boolean immediateReturnFlag = false;
	protected boolean throwCatchFlag = false;
	protected static int MAX_SYMBOL_TABLES = 1000;
	protected static String LoadDefinitionFile = "/Applications/eclipseWorkspaces/GATEWorkspace/TSL/resources/TLispLoadDefinitions";

	public TLisp() {
		try {
			tLisp = this;
			this.functionSymbolTable = new SymbolTable(this);
			SymbolTable lastst = null;
			for (int i = 0; i < MAX_SYMBOL_TABLES; i++) {
				SymbolTable st = symbolTables[i] = new SymbolTable(this);
				st.setParent(lastst);
				lastst = st;
			}
			this.globalSymbolTable = this.symbolTables[0];
			PreDefinitions.initFunctions();
			loadFile(LoadDefinitionFile);
//			this.evalString(PreDefinitions.userdefs);
		} catch (Exception e) {
			tLisp.popAllSymbolTables();
			System.out.println("TLISP: " + e.getMessage());
		}
	}
	
	public static TLisp getTLisp() {
		return getTLisp(false);
	}

	public static TLisp getTLisp(boolean preserveSymbolCase) {
		if (tLisp == null) {
			tLisp = new TLisp();
			tLisp.setPreserveSymbolCase(preserveSymbolCase);
		}
		return tLisp;
	}

	public Object loadFile(String filename) {
		return loadFile(filename, false);
	}

	public TLObject loadFile(String filename, boolean addParens) {
		TLObject rv = null;
		try {
			TLObject read = null;
			tokenizeFile(filename, addParens);
			while ((read = readExpr()) != null) {
				rv = eval(read);
			}
		} catch (Exception e) {
			String msg = "LISP: Cannot complete operation:\n"
					+ StrUtils.getStackTrace(e);
			JOptionPane.showMessageDialog(new JFrame(), msg);
		}
		return rv;
	}

	public TLObject evalString(String str) throws Exception {
		tokenizeString(str);
		checkParens();
		TLObject rv = readExpr();
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
		if (input != null) {
			if (addParens) {
				input = "'(\n" + input + "\n)";
			}
			tokenizeString(input);
		}
	}

	void checkParens() throws Exception {
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
				throw new Exception("Incorrect parenthesis matching");
			}
			if (!token.equals(Token.ENDPARENTOKEN)) {
				throw new Exception(
						"Incorrect token placement visavis parentheses");
			}
		}
	}

	public TLObject readExpr() throws Exception {
		TLObject rv = null;
		Token t = Token.readNextToken(this);
		if (t != null) {
			if (t.type == Token.STARTPAREN) {
				return readSexp();
			}
			if (t.type == Token.SYMBOL) {
				return (TLObject) t.value;
			}
			if (t.type == Token.NUMBER || t.type == Token.STRING) {
				return new JavaObject(t.value);
			}
			if (t.type == Token.FUNCTION) {
				TLObject to = readExpr();
				return Sexp.doCons(Symbol.functionSym, Sexp.doList(to));
			}
			if (t.type == Token.QUOTE) {
				TLObject to = readExpr();
				return Sexp.doCons(Symbol.quoteSym, Sexp.doList(to));
			}
			if (t.type == Token.QUASIQUOTE) {
				TLObject to = readExpr();
				return Sexp.doCons(Symbol.quasiQuoteSym, Sexp.doList(to));
			}
			if (t.type == Token.UNQUOTE) {
				TLObject to = readExpr();
				return Sexp.doCons(Symbol.unquoteSym, Sexp.doList(to));
			}
			if (t.type == Token.UNQUOTESPLICING) {
				TLObject to = readExpr();
				return Sexp.doCons(Symbol.unquoteSplicingSym,
						Sexp.doList(to));
			}
			throw new Exception("Unknown token" + t);
		}
		return rv;
	}

	TLObject readSexp() throws Exception {
		Vector<Token> tokensSoFar = new Vector(0);
		TLObject rv = null;
		Vector<TLObject> items = new Vector(0);
		TLObject s = TLUtils.getNIL();
		boolean finished = false;
		while (!finished) {
			Token t = Token.peekNextToken(this);
			if (t == null) {
				throw new Exception("Premature end to Sexpr: " + tokensSoFar);
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
			TLObject o = items.elementAt(i);
			s = Sexp.doCons(o, s);
		}
		if (s == null) {
			throw new Exception("Unable to read Sexpr");
		}
		return s;
	}

	public static TLObject eval(TLObject o) throws Exception {
		if (TLUtils.isBoolean(o)) {
			return o;
		}
		if (TLUtils.isSymbol(o)) {
			Symbol sym = (Symbol) o;
			Symbol osym = TLisp.tLisp.getCurrentSymbolTable().findLocalSymbol(
					sym);
			TLObject to = null;
			if (osym != null) {
				to = osym.getValue();
			}
			return to;
		}
		if (TLUtils.isConstantLiteral(o)) {
			return o;
		}
		if (TLUtils.isCons(o) && TLUtils.isSymbol(((Sexp) o).getFirst())) {
			Sexp s = (Sexp) o;
			Symbol sym = (Symbol) s.getCar();
			if (Function.isFunction(sym.getValue())) {
				return Function.applyFunctionSymbol(s, true);
			}
			// if (WrappedJFunction.isWrappedJFunction(sym.getValue())) {
			// return WrappedJFunction.applyWrappedJFunctionSymbol(s);
			// }
		}
		throw new Exception("Eval: Invalid expression: " + o);
	}
	
	public static TLObject evList(TLObject exp) throws Exception {
		if (TLUtils.isNil(exp)) {
			return TLUtils.getNIL();
		}
		Sexp s = (Sexp) exp;
		return Sexp.doCons(eval(s.getCar()), evList(s.getCdr()));
	}

	// 6/16/2013:  DO I REALLY NEED EXPAND METHODS FOR ALL LANGUAGE CONSTRUCTS?  CAN'T I JUST COPY AND RETURN
	// THEM, JUST HANDLING QUASIQUOTE DIFFERENTLY?
	public static TLObject expandSimple(TLObject o) throws Exception {
		if (!o.isCons()) {
			return o;
		}
		if (TLUtils.isCons(o)) {
			Sexp s = (Sexp) o;
			TLObject first = s.getFirst();
			if (Symbol.quasiQuoteSym.equals(first)) {
				return TLJFunctions.expandQuasiQuote((Sexp) s.getCdr());
			}
			return expandList(o); 
		}
		throw new Exception("Expand: Invalid expression: " + o);
	}
	
	// Before 6/16/2013
	public static TLObject expand(TLObject o) throws Exception {
		if (!o.isCons()) {
			return o;
		}
		if (TLUtils.isCons(o) && TLUtils.isSymbol(((Sexp) o).getFirst())) {
			Sexp s = (Sexp) o;
			Symbol sym = (Symbol) s.getCar();
			if (WrappedJFunction.isWrappedJFunction(sym.getValue())) {
				return WrappedJFunction.applyWrappedJFunctionSymbol(s);
			}
			if (Function.isFunction(sym.getValue())) {
				return Function.applyFunctionSymbol(s, false);
			}
		}
		throw new Exception("Expand: Invalid expression: " + o);
	}

	public static TLObject expandList(TLObject exp) throws Exception {
		if (TLUtils.isNil(exp)) {
			return TLUtils.getNIL();
		} else {
			Sexp s = (Sexp) exp;
			return Sexp.doCons(expand(s.getCar()), expandList(s.getCdr()));
		}
	}
	
	// 5/19/2014:  E.g. Object rv = tlisp.applyJLet("(jlet (x y) (+ x y))", <vector>);
	public Object applyJLet(String str, Vector args) throws Exception {
		Sexp sexp = (Sexp) this.evalString(str);
		return applyJLet(sexp, args);
	}
	
	public Object applyJLet(Sexp jlsexp, Vector args) throws Exception {
		return TLJFunctions.applyJavaLet(jlsexp, args);
	}

	public boolean isPreserveSymbolCase() {
		return preserveSymbolCase;
	}

	public void setPreserveSymbolCase(boolean preserveSymbolCase) {
		this.preserveSymbolCase = preserveSymbolCase;
	}

	public static TLObject getLastReferenceObject() {
		return TLisp.tLisp.lastReferenceObject;
	}

	public static void setLastReferenceObject(TLObject lastReferenceObject) {
		TLisp.tLisp.lastReferenceObject = lastReferenceObject;
	}

	public static void resetLastReferenceObject() {
		TLisp.tLisp.lastReferenceObject = null;
	}

	public void pushSymbolTable() throws Exception {
		if (this.currentSymbolTableIndex >= MAX_SYMBOL_TABLES) {
			this.popAllSymbolTables();
			throw new Exception("Maximum stack depth reached");
		}
		this.currentSymbolTableIndex++;
	}

	public void popSymbolTable() throws Exception {
		if (this.currentSymbolTableIndex == 0) {
			throw new Exception("Attempting to pop global symbol table");
		}
		this.getCurrentSymbolTable().clear();
		this.currentSymbolTableIndex--;
	}

	public KnowledgeBase getTSLKnowledgeBase() {
		if (this.TSLKnowledgeBase == null) {
			this.TSLKnowledgeBase = new KnowledgeBase();
		}
		return TSLKnowledgeBase;
	}

	public void popSymbolTableToIndex(int index) throws Exception {
		if (index == 0) {
			throw new Exception("Attempting to pop global symbol table");
		}
		if (index < this.currentSymbolTableIndex) {
			throw new Exception(
					"Attempting to restore symbol table index higher than current");
		}
		for (int i = this.currentSymbolTableIndex; i > index; i--) {
			this.symbolTables[i].clear();
		}
		this.currentLineNumber = index;
	}

	public void popAllSymbolTables() {
		for (int i = 1; i <= this.currentSymbolTableIndex; i++) {
			this.symbolTables[i].clear();
		}
		this.currentSymbolTableIndex = 0;
	}

	public SymbolTable getCurrentSymbolTable() {
		return this.symbolTables[this.currentSymbolTableIndex];
	}

	public boolean isImmediateReturnFlag() {
		return immediateReturnFlag;
	}

	public void setImmediateReturnFlag(boolean immediateReturnFlag) {
		this.immediateReturnFlag = immediateReturnFlag;
	}

	public boolean isThrowCatchFlag() {
		return throwCatchFlag;
	}

	public void setThrowCatchFlag(boolean throwCatchFlag) {
		this.throwCatchFlag = throwCatchFlag;
	}

}
