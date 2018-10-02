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

public class Symbol extends TLObject {

	private String name = null;
	private int type = REGULARTYPE;
	private boolean doEval = true;
	private TLObject value = null;

	public static int FUNCTIONTYPE = 1;
	public static int REGULARTYPE = 2;

	public static Symbol NIL = new Symbol(TLisp.tLisp.globalSymbolTable, "nil",
			REGULARTYPE, null, false);
	public static Symbol T = new Symbol(TLisp.tLisp.globalSymbolTable, "t",
			REGULARTYPE, null, false);
	public static Symbol DOT = new Symbol(TLisp.tLisp.globalSymbolTable, ".",
			REGULARTYPE, null, false);
	public static Symbol catchSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"catch", FUNCTIONTYPE, null, false);
	public static Symbol throwSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"throw", FUNCTIONTYPE, null, false);
	public static Symbol returnSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"return", FUNCTIONTYPE, null, false);
	public static Symbol expandSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"expand", FUNCTIONTYPE, null, false);
	public static Symbol evalSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"eval", FUNCTIONTYPE, null, false);
	public static Symbol functionSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "function", FUNCTIONTYPE, null,
			false);
	public static Symbol letSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"let", FUNCTIONTYPE, null, false);
	public static Symbol defunSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"defun", FUNCTIONTYPE, null, false);
	public static Symbol defMethodSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"defmethod", FUNCTIONTYPE, null, false);
	public static Symbol defMacroSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"defmacro", FUNCTIONTYPE, null, false);
	public static Symbol lambdaSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"lambda", FUNCTIONTYPE, null, false);
	public static Symbol setqSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"setq", FUNCTIONTYPE, null, false);
	public static Symbol setfSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"setf", FUNCTIONTYPE, null, false);
	public static Symbol applySym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"apply", FUNCTIONTYPE, null, true);
	public static Symbol notSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"not", FUNCTIONTYPE, null, true);
	public static Symbol consSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"cons", FUNCTIONTYPE, null, true);
	public static Symbol carSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"car", FUNCTIONTYPE, null, true);
	public static Symbol cdrSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"cdr", FUNCTIONTYPE, null, true);
	public static Symbol appendSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"append", FUNCTIONTYPE, null, true);
	public static Symbol ifSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"if", FUNCTIONTYPE, null, true);
	public static Symbol condSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"cond", FUNCTIONTYPE, null, false);
	public static Symbol defineSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"define", FUNCTIONTYPE, null, false);
	public static Symbol sequenceSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "begin", FUNCTIONTYPE, null, false);
	public static Symbol equalpSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"equals", FUNCTIONTYPE, null, true);
	public static Symbol eqpSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"=", FUNCTIONTYPE, null, true);
	public static Symbol lengthSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"length", FUNCTIONTYPE, null, true);
	public static Symbol conspSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"cons?", FUNCTIONTYPE, null, true);
	public static Symbol symbolpSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"symbol?", FUNCTIONTYPE, null, true);
	public static Symbol nullpSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"null?", FUNCTIONTYPE, null, true);
	public static Symbol forLoopSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"for", FUNCTIONTYPE, null, false);
	public static Symbol defClassSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "defclass", FUNCTIONTYPE, null,
			false);
	public static Symbol defInstanceSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "definstance", FUNCTIONTYPE, null,
			false);
	public static Symbol getSlotValueSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "getslot", FUNCTIONTYPE,
			null, false);
	public static Symbol setSlotValueSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "setslot", FUNCTIONTYPE,
			null, false);

	public static Symbol defineWrappedJFunctionSymbol = new Symbol(
			TLisp.tLisp.globalSymbolTable, "define-wrapped-java-function",
			FUNCTIONTYPE, null, false);
	public static Symbol applyWrappedJFunctionSymbol = new Symbol(
			TLisp.tLisp.globalSymbolTable, "apply-wrapped-java-function",
			FUNCTIONTYPE, null, true);

	public static Symbol mapSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"map", FUNCTIONTYPE, null, false);

	// Quote symbols
	public static Symbol quoteSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"quote", FUNCTIONTYPE, null, false);
	public static Symbol unquoteSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"unquote", FUNCTIONTYPE, null, true);
	public static Symbol quasiQuoteSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "quasiquote", FUNCTIONTYPE, null,
			false);
	public static Symbol unquoteSplicingSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "unquotesplicing", FUNCTIONTYPE,
			null, true);

	// TSL functions
	public static Symbol TSLLoadRuleFileSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "tslloadrulefile", FUNCTIONTYPE,
			null, true);
	public static Symbol TSLQuerySym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "tslquery", FUNCTIONTYPE, null,
			false);
	public static Symbol TSLAssertSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "tslassert", FUNCTIONTYPE, null,
			false);

	// Math functions
	public static Symbol addSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"+", FUNCTIONTYPE, null, true);
	public static Symbol subtractSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "-", FUNCTIONTYPE, null, true);
	public static Symbol timesSym = new Symbol(TLisp.tLisp.globalSymbolTable,
			"*", FUNCTIONTYPE, null, true);
	public static Symbol lessThanOrEqualsSym = new Symbol(
			TLisp.tLisp.globalSymbolTable, "<=", FUNCTIONTYPE, null, true);

	public Symbol(String name) {
		this.name = name;
	}

	public Symbol(SymbolTable st, String name, TLObject value) {
		this.name = name;
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
//			this.name = this.name.toUpperCase();
			this.name = this.name.toLowerCase();
		}
		this.value = value;
		st.addSymbol(this);
	}

	public Symbol(SymbolTable st, String name, int type, JavaObject value,
			boolean doeval) {
		this.name = name;
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
//			this.name = this.name.toUpperCase();
			this.name = this.name.toLowerCase();
		}
		this.type = type;
		this.type = type;
		this.value = value;
		this.doEval = doeval;
		if (!this.doEval && this.value == null) { // i.e. so that T can evaluate
													// to T, etc.
			this.value = this;
		}
		if (st != null) {
			st.addSymbol(this);
		}
	}

	public String toString() {
		return this.getName();
	}

	public Object toJava() {
		return this.getName();
	}

	public TLObject getValue() {
		return this.value;
	}

	public void setValue(TLObject value) {
		this.value = value;
	}

	public Symbol copy(SymbolTable st) {
		return new Symbol(st, this.name, this.value);
	}

	public Symbol copy(SymbolTable st, TLObject value) {
		return new Symbol(st, this.name, value);
	}

	public void unbind() {
		this.value = null;
	}

	public boolean isFunction() {
		return this.type == FUNCTIONTYPE;
	}

	public boolean isDoEval() {
		return doEval;
	}

	public void setDoEval(boolean doEval) {
		this.doEval = doEval;
	}

	public String getName() {
		String sname = this.name;
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
			sname = sname.toLowerCase();
		}
		return sname;
	}
	
	public boolean hasName(String name) {
		return name.equals(this.getName());
	}

	public void setName(String sname) {
		this.name = sname;
		if (!TLisp.tLisp.isPreserveSymbolCase()) {
			this.name = sname.toLowerCase();
		}
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isBound() {
		return this.value != null;
	}

	// public boolean equals(Object o) {
	// if (Symbol.class.equals(o.getClass())) {
	// Symbol s = (Symbol) o;
	// return (this.name.equals(s.name)
	// && ((this.value == null && s.value == null)
	// || this.value.equals(s.value)));
	// }
	// return false;
	// }

	public int hashCode() {
		int rv = this.name.hashCode();
		if (this.isBound()) {
			rv |= this.value.hashCode();
		}
		return rv;
	}

	public boolean isVariable() {
		return (this.name.charAt(0) == '?');
	}

	// Not sure where this belongs. The string is not a Symbol, but it is a
	// symbol in the more general sense, and
	// used in the same way...
	public static boolean isVariable(String str) {
		return (str != null && str.length() > 1 && str.charAt(0) == '?');
	}

	public int getType() {
		return type;
	}

}
