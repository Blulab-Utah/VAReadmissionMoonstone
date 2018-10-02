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

public class Function extends JLispObject {
	public Symbol sym = null;

	public static int SEXPPARAM = 1;
	public static int OBJECTPARAM = 2;
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
		}
	}

	public Function(Symbol sym) {
		sym.setValue(this);
	}

	public String toString() {
		return "[Function:  " + this.sym.getName() + "]";
	}

	public static boolean isTrue(Boolean b) {
		return b.booleanValue();
	}

}
