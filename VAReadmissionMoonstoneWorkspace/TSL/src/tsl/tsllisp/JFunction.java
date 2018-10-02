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

public class JFunction extends Function {
	private int paramType = -1;
	private Class[] paramTypes = null;
	private Method evalMethod = null;
	private Method expandMethod = null;
	private int numargs = 0;

	public JFunction(Symbol sym, String evalMethodName,
			String expandMethodName, int ptype, boolean doeval, int numargs)
			throws Exception {
		super(sym, doeval);
		this.paramType = ptype;
		this.paramTypes = (ptype == SEXPPARAM ? SexpParamTypes
				: ObjectParamTypes);
		this.numargs = numargs;
		try {
			if (evalMethodName != null) {
				this.evalMethod = TLJFunctions.class.getMethod(evalMethodName,
						this.paramTypes);
			}
			if (expandMethodName != null) {
				this.expandMethod = TLJFunctions.class.getMethod(
						expandMethodName, this.paramTypes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isJFunction(TLObject o) {
		return o instanceof JFunction;
	}

	public static TLObject applyJFunctionObject(JFunction jf, Sexp args,
			boolean isEval) throws Exception {
		int numargs = args.getLength();
		if (!(jf.getNumargs() == -1 || jf.getNumargs() == numargs)) {
			throw new Exception("Incorrect number of arguments for call to "
					+ jf.getSym());
		}
		if (isEval && jf.isDoEval()) {
			args = (Sexp) TLisp.evList(args);
		}
		Object farg = (isEval && jf.paramType == OBJECTPARAM ? (Object) TLUtils
				.convertSexpToJVector(args) : (Object) args);
		Method m = (isEval ? jf.evalMethod : jf.expandMethod);
		TLObject rv = null;
		rv = (TLObject) m.invoke(TLJFunctions.staticObject, farg);
		return rv;
	}

	public int getNumargs() {
		return numargs;
	}

}
