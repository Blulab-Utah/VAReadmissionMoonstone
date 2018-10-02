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

public class TLFunction extends Function {
	private Symbol fsym = null;
	private Vector<Symbol> params = null;
	private TLObject body = null;
	private boolean doEval = true;

	public static int SEXPPARAM = 1;
	public static int OBJECTPARAM = 2;
	public static Class[] sexpParamTypes = new Class[] { Sexp.class };
	public static Class[] objectParamTypes = new Class[] { Object.class };

	public TLFunction(Symbol sym, Vector params, TLObject body, boolean doeval) {
		super(null);
		this.fsym = sym;
		this.params = params;
		this.body = body;
		this.doEval = doeval;
		// Function is not anonymous
		if (sym != null) {
			sym.setValue(this);
		}
	}

	public static boolean isLFunction(Object o) {
		return o instanceof TLFunction;
	}

	public static TLObject applyLFunctionObject(TLFunction lf, Sexp args,
			boolean isEval) throws Exception {
		if (!((args.getLength() == 0 && lf.params == null) || (args.getLength() == lf.params
				.size()))) {
			throw new Exception(
					"Function arguments not of same length: Params="
							+ lf.params + ", Args=" + args);
		}
		TLObject rv = null;
		TLisp tl = TLisp.tLisp;
		tl.pushSymbolTable();
		SymbolTable st = tl.getCurrentSymbolTable();
		if (lf.params != null) {
			int index = 0;
			for (Enumeration<TLObject> e = args.elements(); e.hasMoreElements();) {
				TLObject arg = e.nextElement();
				Symbol param = lf.params.elementAt(index++);
				TLObject value = TLisp.eval(arg);
				st.pushParameter(param, value);
			}
		}
		rv = TLisp.eval(lf.body);
		tl.popSymbolTable();
		return rv;
	}

	public static TLObject expandLFunctionObject(TLFunction lf, Sexp args)
			throws Exception {
		if (!((args.getLength() == 0 && lf.params == null) || (args.getLength() == lf.params
				.size()))) {
			throw new Exception(
					"Function arguments not of same length: Params="
							+ lf.params + ", Args=" + args);
		}
		if (lf.fsym == null) { // Store cons with object instead of
								// reconverting...
			Sexp pexp = (Sexp) TLUtils.convertLVectorToSexp(lf.params);
			return Sexp.doCons(Symbol.lambdaSym,
					Sexp.doCons(pexp, Sexp.doList(lf.body)));
		} else {
			return Sexp.doCons(lf.sym, TLisp.expand(args));
		}
	}

	public String toString() {
		if (this.fsym != null) {
			return this.fsym.getName();
		} else {
			return "<AnonymousLFunction>";
		}
	}

}
