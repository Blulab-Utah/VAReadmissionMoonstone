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

import java.util.Enumeration;
import java.util.Vector;

import tsl.utilities.VUtils;

public class Function extends TLObject {
	public Symbol sym = null;

	public static int SEXPPARAM = 1;
	public static int OBJECTPARAM = 2;
	private boolean doEval = true;
	public static Class[] SexpParamTypes = new Class[] { Sexp.class };
	public static Class[] ObjectParamTypes = new Class[] { Object.class };
	public static Boolean TRUTH = new Boolean(true);
	public static Boolean FALSITY = new Boolean(false);

	public Function() {
	}

	public Function(Symbol sym, boolean doeval) {
		if (sym != null) {
			this.sym = sym;
			sym.setType(Symbol.FUNCTIONTYPE);
			sym.setValue(this);
			sym.setDoEval(doeval);
			this.setDoEval(doeval);
		}
	}

	public Function(Symbol sym) {
		if (sym != null) {
			sym.setValue(this);
		}
	}

	public static TLObject applyFunctionSymbol(Sexp exp, boolean isEval)
			throws Exception {
		Symbol s = (Symbol) exp.getFirst();
		if (!(s.getValue() instanceof Function)) {
			throw new Exception("Symbol " + s + "not bound to function");
		}
		return applyFunctionObject((Function) s.getValue(),
				(Sexp) exp.getCdr(), isEval);
	}

	public static TLObject applyFunctionObject(Function f, Sexp args,
			boolean isEval) throws Exception {
		if (f instanceof JFunction) {
			return JFunction.applyJFunctionObject((JFunction) f, args, isEval);
		}
		if (f instanceof TLFunction) {
			if (isEval) {
				return TLFunction.applyLFunctionObject((TLFunction) f, args,
						isEval);
			}
			return TLFunction.expandLFunctionObject((TLFunction) f, args);
		}
		if (f instanceof TLMethod) {
			if (isEval) {
				return TLMethod.applyMethodObject((TLMethod) f, args);
			}
			return TLMethod.expandMethodObject((TLMethod) f, args);
		}
		if (f instanceof TLMacro) {
			if (isEval) {
				return TLMacro.applyMacroObject((TLMacro) f, args);
			}
			return TLMacro.expandMacroObject((TLMacro) f, args);
		}
		return null;
	}

	public static TLObject applyRecursiveFunctionObject(Function f,
			TLObject args, boolean doEval) throws Exception {
		TLObject result = TLUtils.getNIL();
		if (args.isSexp()) {
			Sexp sexp = (Sexp) args;
			TLObject to = null;
			if (f instanceof JFunction) {
				to = JFunction.applyJFunctionObject((JFunction) f,
						(Sexp) sexp.getCar(), doEval);
			} else if (f instanceof TLFunction) {
				to = TLFunction.applyLFunctionObject((TLFunction) f,
						(Sexp) sexp.getCar(), doEval);
			}
			return Sexp.doCons(to,
					applyRecursiveFunctionObject(f, sexp.getCdr(), doEval));
		}
		return result;
	}

	public static Sexp expandParameterList(Sexp pdesc) throws Exception {
		Vector<TLObject> pv = null;
		for (Enumeration<TLObject> e = pdesc.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			if (!to.isParameterPair()) {
				throw new Exception("Let: " + to + " is not valid parameter");
			}
			Symbol sym = null;
			TLObject value = null;
			TLObject pto = null;
			if (to.isCons()) {
				Sexp pair = (Sexp) to;
				sym = (Symbol) TLisp.expand(pair.getFirst());
				value = TLisp.expand(pair.getSecond());
				pto = Sexp.doCons(sym, Sexp.doList(value));
			} else {
				pto = TLisp.expand(to);
			}
			pv = VUtils.add(pv, pto);
		}
		return (Sexp) TLUtils.convertLVectorToSexp(pv);
	}

	public static Sexp expandStandardFunctionForm(Symbol sym, Sexp arg)
			throws Exception {
		return Sexp.doCons(sym, TLisp.expandList(arg));
	}

	public static boolean isFunction(TLObject o) {
		return o instanceof Function;
	}

	public String toString() {
		return "[Function:  " + this.sym.getName() + "]";
	}

	public static boolean isTrue(Boolean b) {
		return b.booleanValue();
	}

	public Symbol getSym() {
		return sym;
	}

	public boolean isDoEval() {
		return doEval;
	}

	public void setDoEval(boolean doEval) {
		this.doEval = doEval;
	}

	public static Function getFunction(TLObject to) {
		if (to != null) {
			if (Function.isFunction(to)) {
				return (Function) to;
			}
			if (to.isSymbol()) {
				Symbol sym = (Symbol) to;
				if (Function.isFunction(sym.getValue())) {
					return (Function) sym.getValue();
				}
			}
			if (to.isCons()) {
				try {
					TLObject rv = TLisp.eval(to);
					if (rv instanceof Function) {
						return (Function) rv;
					}
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

}
