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

import java.util.Enumeration;
import java.util.Vector;

import tsl.expression.term.function.FunctionTerm;

public class LispJFunctions extends FunctionTerm {

	public static LispJFunctions staticObject = new LispJFunctions();

	public static JLispObject evalFunction(Sexp arg) {
		JLispObject rv = null;
		arg = (Sexp) arg.getFirst();
		Vector<JLispObject> params = JLUtils.convertSexpToLVector((Sexp) arg
				.getSecond());
		Sexp body = (Sexp) arg.getThird();
		rv = new LispFunction(null, params, body, true);
		return rv;
	}

	public static JLispObject evalDefun(Sexp arg) {
		JLispObject rv = null;
		Symbol sym = (Symbol) arg.getFirst();
		Vector params = JLUtils.convertSexpToLVector((Sexp) arg.getSecond());
		Sexp body = (Sexp) arg.getThird();
		rv = new LispFunction(sym, params, body, true);
		return rv;
	}

	public static JLispObject evalApply(Sexp exp) {
		LispFunction lf = (LispFunction) exp.getFirst();
		Sexp args = (Sexp) exp.getSecond();
		JLispObject rv = LispFunction.applyLFunctionObject(lf, args);
		return rv;
	}

	public static JLispObject evalQuote(Sexp arg) {
		return arg.getFirst();
	}
	
	public static JLispObject evalCons(Sexp arg) {
		JLispObject o1 = arg.getFirst();
		JLispObject o2 = arg.getSecond();
		return Sexp.doCons(o1, o2);
	}

	public static JLispObject evalCar(Sexp arg) {
		JLisp.setLastReferenceObject(arg);
		return arg.getCar();
	}

	public static JLispObject evalCdr(Sexp arg) {
		JLisp.setLastReferenceObject(arg);
		return arg.getCdr();
	}

	public static JavaObject addFloats(Object args) {
		Vector v = (Vector) args;
		float rv = 0;
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			JavaObject jo = (JavaObject) e.nextElement();
			Float x = (Float) jo.getObject();
			rv += x.floatValue();
		}
		JavaObject josum = new JavaObject(new Float(rv));
		return josum;
	}

	public static JLispObject evalSetq(Sexp arg) {
		Symbol sym = SymbolTable.findLocalSymbol((Symbol) arg.getSecond());
		JLispObject rv = JLisp.eval(arg.getThird());
		sym.setValue(rv);
		return rv;
	}
	
	public static JLispObject evalSetf(Sexp arg) {
		if (JLUtils.isSymbol(arg.getFirst())) {
			return evalSetq(arg);
		}
		JLispObject value = JLisp.eval(arg.getThird());
//		Sexp sexp = (Sexp) arg.getSecond();
		JLispObject jlo = JLisp.eval(arg.getSecond());
		JLispObject reference = JLisp.getLastReferenceObject();
		if (reference != null && reference.isSexp()) {
			Sexp parent = (Sexp) reference;
			if (jlo.equals(parent.getCar())) {
				parent.setCar(value);
			} else if (jlo.equals(parent.getCdr())){
				parent.setCdr(value);
			}
		}
		return value;
	}

}
