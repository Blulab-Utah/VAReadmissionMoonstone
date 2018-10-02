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

public class Symbol extends JLispObject {

	String name = null;
	
	int type = REGULARTYPE;
	
	boolean doEval = true;

	JLispObject value = null;
	
	public static int FUNCTIONTYPE = 1;
	public static int REGULARTYPE = 2;

	public static Symbol NIL = new Symbol(SymbolTable.globalSymbolTable, "nil", REGULARTYPE, null, false);
	public static Symbol T = new Symbol(SymbolTable.globalSymbolTable, "t", REGULARTYPE, null, false);
	public static Symbol DOT = new Symbol(SymbolTable.globalSymbolTable, ".", REGULARTYPE, null, false);
	public static Symbol functionSym = new Symbol(SymbolTable.globalSymbolTable, "function", FUNCTIONTYPE, null, false);
	public static Symbol defunSym = new Symbol(SymbolTable.globalSymbolTable, "defun", FUNCTIONTYPE, null, false);
	public static Symbol quoteSym = new Symbol(SymbolTable.globalSymbolTable, "quote", FUNCTIONTYPE, null, false);
	public static Symbol setqSym = new Symbol(SymbolTable.globalSymbolTable, "setq", FUNCTIONTYPE, null, false);
	public static Symbol applySym = new Symbol(SymbolTable.globalSymbolTable, "apply", FUNCTIONTYPE, null, true);
	public static Symbol consSym = new Symbol(SymbolTable.globalSymbolTable, "cons", FUNCTIONTYPE, null, true);
	public static Symbol carSym = new Symbol(SymbolTable.globalSymbolTable, "car", FUNCTIONTYPE, null, true);
	public static Symbol cdrSym = new Symbol(SymbolTable.globalSymbolTable, "cdr", FUNCTIONTYPE, null, true);
	public static Symbol addSym = new Symbol(SymbolTable.globalSymbolTable, "+", FUNCTIONTYPE, null, true);
	public static Symbol backquoteSym = new Symbol(SymbolTable.globalSymbolTable, "`", FUNCTIONTYPE, null, false);
	public static Symbol commaSym = new Symbol(SymbolTable.globalSymbolTable, ",", FUNCTIONTYPE, null, true);
	public static Symbol ifSym = new Symbol(SymbolTable.globalSymbolTable, "if", FUNCTIONTYPE, null, true);
	
	
	
	Symbol(String name) {
		this.name = name;
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			this.name = this.name.toUpperCase();
		}
		SymbolTable.globalSymbolTable.addSymbol(this);
	}
	
	Symbol(SymbolTable st, String name) {
		this.name = name;
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			this.name = this.name.toUpperCase();
		}
		st.addSymbol(this);
	}

	Symbol(SymbolTable st, String name, JLispObject value) {
		this.name = name;
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			this.name = this.name.toUpperCase();
		}
		this.value = value;
		st.addSymbol(this);
	}
	
	Symbol(SymbolTable st, String name, int type, JavaObject value, boolean doeval) {
		this.name = name;
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			this.name = this.name.toUpperCase();
		}
		this.type = type;
		this.type = type;
		this.value = value;
		this.doEval = doeval;
		if (!this.doEval && this.value == null) {  // i.e. so that T can evaluate to T, etc.
			this.value = this;
		}
		st.addSymbol(this);
	}
	
	public String toString() {
		return this.getName();
	}
	
	public Object toJava() {
		return this.getName();
	}
	
	public JLispObject getValue() {
		return this.value;
	}
	
	public void setValue(JLispObject value) {
		this.value = value;
	}
	
	public Symbol copy(SymbolTable st) {
		return new Symbol(st, this.name, this.value);
	}
	
	public Symbol copy(SymbolTable st, JLispObject value) {
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
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			sname = sname.toLowerCase();
		}
		return sname;
	}
	
	public void setName(String sname) {
		this.name = sname;
		if (!JLisp.getJLisp().isPreserveSymbolCase()) {
			this.name = sname.toLowerCase();
		}
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isBound() {
		return this.value != null;
	}
	
//	public boolean equals(Object o) {
//		if (Symbol.class.equals(o.getClass())) {
//			Symbol s = (Symbol) o;
//			return (this.name.equals(s.name) 
//					&& ((this.value == null && s.value == null)
//							|| this.value.equals(s.value)));
//		}
//		return false;
//	}
	
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
	
	// Not sure where this belongs.  The string is not a Symbol, but it is a symbol in the more general sense, and
	// used in the same way...
	public static boolean isVariable(String str) {
		return (str != null && str.length() > 1 && str.charAt(0) == '?');
	}
	

}
