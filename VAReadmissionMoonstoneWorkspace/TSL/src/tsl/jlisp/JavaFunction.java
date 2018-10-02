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

import java.lang.reflect.Method;

public class JavaFunction extends Function {
	private int paramType = -1;
	private Class[] paramTypes = null;
	private Method method = null;

	public JavaFunction(Symbol sym, String methodname, int ptype, boolean doeval) {
		super(sym, doeval);
		try {
			this.paramType = ptype;
			this.paramTypes = (ptype == SEXPPARAM ? SexpParamTypes
					: ObjectParamTypes);
			this.method = LispJFunctions.class.getMethod(methodname,
					this.paramTypes);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public static boolean isJFunction(JLispObject o) {
		return o instanceof JavaFunction;
	}

	public static JLispObject applyJFunctionSymbol(Sexp exp) {
		JLispObject rv = null;
		try {
			Symbol s = (Symbol) exp.getFirst();
			JavaFunction jf = (JavaFunction) s.getValue();
			Sexp args = (Sexp) exp.getCdr();
			if (s.isDoEval()) {
				args = (Sexp) JLisp.evList(args);
			}
			Object farg = (jf.paramType == SEXPPARAM ? (Object) args
					: (Object) JLUtils.convertSexpToLVector(args));
			rv = (JLispObject) jf.method.invoke(LispJFunctions.staticObject,
					new Object[] { farg });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

}
