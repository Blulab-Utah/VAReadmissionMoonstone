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

public class TLMacro extends Function {

	private Vector<Symbol> params = null;
	private TLObject body = null;

	public TLMacro(Symbol sym, Vector params, TLObject body) {
		this.sym = sym;
		this.params = params;
		this.body = body;
		sym.setValue(this);
	}

	public static boolean isMacro(Object o) {
		return o instanceof TLMacro;
	}

	public static TLObject applyMacroObject(TLMacro macro, Sexp args)
			throws Exception {
		if (!((args.getLength() == 0 && macro.params == null) || (args
				.getLength() == macro.params.size()))) {
			throw new Exception("Macro arguments not of same length: Params="
					+ macro.params + ", Args=" + args);
		}
		TLObject rv = null;
		TLisp tl = TLisp.tLisp;
		tl.pushSymbolTable();
		SymbolTable st = tl.getCurrentSymbolTable();
		if (macro.params != null) {
			int index = 0;
			for (Enumeration<TLObject> e = args.elements(); e.hasMoreElements();) {
				TLObject arg = e.nextElement();
				Symbol param = macro.params.elementAt(index++);
//				TLObject value = TLisp.eval(arg);
				TLObject value = arg;
				st.pushParameter(param, value);
			}
		}
		Sexp expansion = (Sexp) TLisp.eval(macro.body);
		rv = TLisp.eval(expansion);
		tl.popSymbolTable();
		return rv;
	}

	public static TLObject expandMacroObject(TLMacro macro, Sexp args)
			throws Exception {
		if (!((args.getLength() == 0 && macro.params == null) || (args
				.getLength() == macro.params.size()))) {
			throw new Exception("Macro arguments not of same length: Params="
					+ macro.params + ", Args=" + args);
		}
		return Sexp.doCons(macro.sym, TLisp.expand(args));
	}

	public Vector<Symbol> getParams() {
		return params;
	}

	public TLObject getBody() {
		return body;
	}
	
	public String toString() {
		return "<Macro: " + this.sym + ">";
	}

}
