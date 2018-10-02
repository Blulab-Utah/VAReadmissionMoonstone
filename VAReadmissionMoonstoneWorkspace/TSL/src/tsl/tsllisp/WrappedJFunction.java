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

import java.lang.reflect.Method;
import java.util.Enumeration;

// (define-wrapped-java-function onyx '("string"))
//
// (apply-wrapped-java-function onyx ("/users/leechristensen/Desktop/EvaluationWorkbench/documents"))

public class WrappedJFunction extends Function {
	private Method method = null;
	private int numargs = 0;

	public WrappedJFunction(Sexp arg) throws Exception {
		if (!(TLUtils.isAtomList(arg))) {
			throw new Exception("Invalid Java function definition: " + arg);
		}
		Symbol fsym = (Symbol) arg.getFirst();
		Class[] ptypes = new Class[arg.getLength() - 1];
		int i = 0;
		for (Enumeration<JavaObject> e = (((Sexp) arg.getCdr())).elements(); e
				.hasMoreElements();) {
			JavaObject o = e.nextElement();
			ptypes[i++] = o.getObject().getClass();
		}
		this.numargs = i;
		Class source = Class.forName(fsym.getName());
		this.method = source.getMethod(fsym.getName(), ptypes);
		TLisp.tLisp.functionSymbolTable.getSymbol(fsym.getName(), this);
	}

	public static boolean isWrappedJFunction(TLObject o) {
		return o instanceof WrappedJFunction;
	}

	public static TLObject applyWrappedJFunctionSymbol(Sexp exp)
			throws Exception {
		if (!(exp.getFirst() instanceof WrappedJFunction && TLUtils
				.isJavaObjectList(exp.getSecond()))) {
			throw new Exception("Incorrect WrappedJFunction application: "
					+ exp);
		}
		WrappedJFunction jf = (WrappedJFunction) exp.getFirst();
		Sexp arguments = (Sexp) exp.getSecond();
		if (arguments.getLength() != jf.numargs) {
			throw new Exception("Incorrect number of arguments: " + exp);
		}
		Object[] params = TLUtils.convertSexpToArray(arguments);
		Object result = jf.method.invoke(TLJFunctions.staticObject, params);
		return new JavaObject(result);
	}

}
