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
import tsl.expression.term.function.FunctionTerm;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class TLJFunctions extends FunctionTerm {

	public static TLJFunctions staticObject = new TLJFunctions();

	/*
	 * NOTES:
	 * 6/3/2013: Change all the arguments to TLObject.
	 * 6/14/2013:  I haven't yet tested most of the Expand functions.
	 */
	// 
	
	// (for <params> <test> <increment> <body>)
	public static TLObject evalForLoop(Sexp arg) throws Exception {
		if (!arg.isConseList()) {
			throw new Exception("For:  Incorrect arguments or body length");
		}
		Sexp pdesc = (Sexp) arg.getFirst();
		TLisp tl = TLisp.tLisp;
		tl.pushSymbolTable();
		for (Enumeration<TLObject> e = pdesc.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			if (!to.isParameterPair()) {
				throw new Exception("For: " + to + " is not valid parameter");
			}
			Symbol sym = null;
			TLObject value = null;
			if (to.isCons()) {
				Sexp pair = (Sexp) to;
				sym = (Symbol) pair.getFirst();
				value = TLisp.eval(pair.getSecond());
			} else {
				sym = (Symbol) to;
				value = TLUtils.getNIL();
			}
			tl.getCurrentSymbolTable().pushParameter(sym, value);
		}
		Sexp test = (Sexp) arg.getSecond();
		Sexp inc = (Sexp) arg.getThird();
		Sexp body = (Sexp) arg.getFourth();
		TLObject result = TLUtils.getNIL();
		while (TLisp.eval(test).isTrue()) {
			result = TLisp.eval(body);
			TLisp.eval(inc);
		}
		tl.popSymbolTable();
		return result;
	}
	
	public static TLObject expandForLoop(Sexp arg) throws Exception {
		if (!arg.isConseList()) {
			throw new Exception("For:  Incorrect arguments or body length");
		}
		return Sexp.doCons(Symbol.forLoopSym,
				Sexp.doCons(TLisp.expandList(arg.getSecond()),
						Sexp.doCons(TLisp.expand(arg.getThird()),
								Sexp.doList(TLisp.expand(arg.getFourth())))));
	}
	
	public static TLObject evalReturn(Sexp arg) throws Exception {
		TLisp.tLisp.setImmediateReturnFlag(true);
		return arg.getFirst();
	}
	
	public static TLObject expandReturn(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.returnSym, arg);
	}
	
	public static TLObject evalExpand(Sexp arg) throws Exception {
		return TLisp.expand(arg.getFirst());
	}

	// 6/3/2013: Not tested...
	public static TLObject evalCatchCC(Sexp arg) throws Exception {
		TLObject to = TLisp.eval(arg.getFirst());
		return to;
	}

	public static TLObject expandCatchCC(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.catchSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalThrowCC(Sexp arg) throws Exception {
		TLObject to = TLisp.eval(arg.getFirst());
		return to;
	}

	public static TLObject expandThrowCC(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.throwSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalMap(Sexp arg) throws Exception {
		Function f = Function.getFunction(arg.getFirst());
		if (f == null || !arg.getSecond().isCons()) {
			throw new Exception("Map: <Function> <arguments>");
		}
		Sexp sexp = (Sexp) TLisp.eval(arg.getSecond());
		return Function.applyRecursiveFunctionObject(f, sexp, false);
	}

	public static TLObject expandMap(Sexp arg) throws Exception {
		Function f = Function.getFunction(arg.getFirst());
		if (f == null || !arg.getSecond().isCons()) {
			throw new Exception("Map: <Function> <arguments>");
		}
		TLObject fto = TLisp.expand(arg.getFirst());
		Sexp plst = (Sexp) TLisp.expand(arg.getSecond());
		return Sexp.doCons(Symbol.mapSym, Sexp.doCons(fto, Sexp.doList(plst)));
	}

	public static TLObject evalEval(Sexp arg) throws Exception {
		return TLisp.eval(arg.getFirst());
	}

	public static TLObject expandEval(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.evalSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	// 5/19/2014: Call from Java...
	public static Object applyJavaLet(Sexp sexp, Vector args) throws Exception {
		if (!(sexp.getLength() == 3 && sexp.getFirst().isSymbol()
				&& ((Symbol) sexp.getFirst()).hasName("jlet")
				&& TLUtils.isAtomList(sexp.getSecond()) && TLUtils.isCons(sexp
				.getThird()))) {
			throw new Exception(
					"JavaLet:  Must have form (jlet <params> <body>)");
		}
		TLisp tl = TLisp.tLisp;
		Sexp params = (Sexp) sexp.getSecond();
		Sexp body = (Sexp) sexp.getThird();
		if (!body.getFirst().isCons()) {
			body = Sexp.doList(body);
		}
		tl.pushSymbolTable();
		SymbolTable st = tl.getCurrentSymbolTable();
		for (int i = 0; i < params.getLength(); i++) {
			Symbol param = (Symbol) params.getNth(i);
			Object o = args.elementAt(i);
			JavaObject jo = new JavaObject(o);
			st.pushParameter(param, jo);
		}
		TLObject result = TLUtils.getNIL();
		Object rv = null;
		for (Enumeration<TLObject> e = body.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			result = TLisp.eval(to);
		}
		tl.popSymbolTable();
		if (!result.isNil()) {
			rv = TLUtils.convertToJObject(result);
		}
		return rv;
	}

	public static TLObject evalLet(Sexp arg) throws Exception {
		if (!(arg.getLength() > 1 && arg.getFirst().isCons())) {
			throw new Exception("Let:  Incorrect arguments or body length");
		}
		Sexp pdesc = (Sexp) arg.getFirst();
		TLisp tl = TLisp.tLisp;
		tl.pushSymbolTable();
		for (Enumeration<TLObject> e = pdesc.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			if (!to.isParameterPair()) {
				throw new Exception("Let: " + to + " is not valid parameter");
			}
			Symbol sym = null;
			TLObject value = null;
			if (to.isCons()) {
				Sexp pair = (Sexp) to;
				sym = (Symbol) pair.getFirst();
				value = TLisp.eval(pair.getSecond());
			} else {
				sym = (Symbol) to;
				value = TLUtils.getNIL();
			}
			tl.getCurrentSymbolTable().pushParameter(sym, value);
		}
		Sexp body = (Sexp) arg.getCdr();
		TLObject result = TLUtils.getNIL();
		for (Enumeration<TLObject> e = body.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			result = TLisp.eval(to);
			if (tl.isImmediateReturnFlag()) {
				tl.setImmediateReturnFlag(false);
				break;
			}
		}
		tl.popSymbolTable();
		return result;
	}

	public static TLObject expandLet(Sexp arg) throws Exception {
		if (!(arg.getLength() > 1 && arg.getFirst().isCons())) {
			throw new Exception("Let:  Incorrect arguments or body length");
		}
		Sexp params = Function.expandParameterList((Sexp) arg.getFirst());
		Sexp body = (Sexp) TLisp.expand(arg.getSecond());
		return Sexp.doCons(Symbol.letSym,
				Sexp.doCons(params, Sexp.doList(body)));
	}

	public static TLObject evalFunction(Sexp arg) throws Exception {
		Vector<TLObject> params = TLUtils.convertSexpToLVector((Sexp) arg
				.getFirst());
		Sexp body = (Sexp) arg.getSecond();
		return new TLFunction(null, params, body, true);
	}

	// Later...
	public static TLObject expandFunction(Sexp arg) throws Exception {
		return TLUtils.getNIL();
	}

	public static TLObject evalDefun(Sexp arg) throws Exception {
		if (!(TLUtils.isSymbol(arg.getFirst())
				&& TLUtils.isAtomList(arg.getSecond()) && TLUtils.isCons(arg
				.getThird()))) {
			throw new Exception(
					"Error in definition: (defun <symbol> <params> <body>)");
		}
		Symbol sym = (Symbol) arg.getFirst();
		Vector params = TLUtils.convertSexpToLVector((Sexp) arg.getSecond());
		Sexp body = (Sexp) arg.getThird();
		return new TLFunction(sym, params, body, true);
	}
	
	public static TLObject expandDefun(Sexp arg) throws Exception {
		if (!(arg.getFirst().isSymbol() && arg.getSecond().isCons())) {
			throw new Exception("Defun: (defun <symbol> <params> body)");
		}
		Symbol sym = (Symbol) TLisp.expand(arg.getFirst());
		Sexp params = Function.expandParameterList((Sexp) arg.getSecond());
		TLObject body = TLisp.expand(arg.getThird());
		return Sexp.doCons(Symbol.defunSym,
				Sexp.doCons(sym, Sexp.doCons(params, Sexp.doList(body))));
	}
	
	public static TLObject evalDefMethod(Sexp arg) throws Exception {
		if (!(TLUtils.isSymbol(arg.getFirst())
				&& arg.getSecond().isParameterPair() && TLUtils.isCons(arg
				.getThird()))) {
			throw new Exception(
					"Error in definition: (defmethod methodname (param classname) <body>)");
		}
		Symbol msym = (Symbol) arg.getFirst();
		Sexp pexp = (Sexp) arg.getSecond();
		Symbol param = (Symbol) pexp.getFirst();
		TLObject mto = TLisp.eval(pexp.getSecond());
		if (!(mto instanceof TLClass)) {
			throw new Exception(
			"Error in definition: (defmethod methodname (param classname) <body>)");
		}
		TLClass mclass = (TLClass) mto;
		Sexp body = (Sexp) arg.getThird();
		return new TLMethod(msym, param, mclass, body);
	}
	
	public static TLObject expandDefMethod(Sexp arg) throws Exception {
		if (!(TLUtils.isSymbol(arg.getFirst())
				&& arg.getSecond().isParameterPair() && TLUtils.isCons(arg
				.getThird()))) {
			throw new Exception(
					"Error in definition: (defmethod methodname (param classname) <body>)");
		}
		Symbol msym = (Symbol) arg.getFirst();
		Sexp pexp = (Sexp) arg.getSecond();
		TLObject mto = TLisp.eval(pexp.getSecond());
		if (!(mto instanceof TLClass)) {
			throw new Exception(
			"Error in definition: (defmethod methodname (param classname) <body>)");
		}
		Sexp body = (Sexp) arg.getThird();
		return Sexp.doCons(Symbol.defMethodSym,
				Sexp.doCons(msym, Sexp.doCons(pexp, Sexp.doList(body))));
	}

	public static TLObject evalDefmacro(Sexp arg) throws Exception {
		if (!(TLUtils.isSymbol(arg.getFirst())
				&& TLUtils.isAtomList(arg.getSecond()) && TLUtils.isCons(arg
				.getThird()))) {
			throw new Exception(
					"Error in definition: (defmacro <symbol> <params> <body>)");
		}
		Symbol sym = (Symbol) arg.getFirst();
		Vector params = TLUtils.convertSexpToLVector((Sexp) arg.getSecond());
		Sexp body = (Sexp) (TLisp.expand(arg.getThird()));
		return new TLMacro(sym, params, body);
	}

	// Not tested
	public static TLObject expandDefmacro(Sexp arg) throws Exception {
		if (!(TLUtils.isSymbol(arg.getFirst())
				&& TLUtils.isAtomList(arg.getSecond()) && TLUtils.isCons(arg
				.getThird()))) {
			throw new Exception(
					"Error in definition: (defmacro <symbol> <params> <body>)");
		}
		Symbol sym = (Symbol) arg.getFirst();
		Sexp params = (Sexp) TLisp.expand(arg.getSecond());
		Sexp body = (Sexp) (TLisp.expand(arg.getThird()));
		return Sexp.doCons(Symbol.defMacroSym,
				Sexp.doCons(sym, Sexp.doCons(params, Sexp.doList(body))));
	}

	public static TLObject evalLambda(Sexp arg) throws Exception {
		if (!(TLUtils.isAtomList(arg.getFirst()) && TLUtils.isCons(arg
				.getSecond()))) {
			throw new Exception(
					"Error in Lambda definition: (lambda <params> <body>)");
		}
		Vector params = TLUtils.convertSexpToLVector((Sexp) arg.getFirst());
		Sexp body = (Sexp) arg.getSecond();
		return new TLFunction(null, params, body, true);
	}

	public static TLObject expandLambda(Sexp arg) throws Exception {
		if (!(TLUtils.isAtomList(arg.getFirst()))) {
			throw new Exception(
					"Error in Lambda definition: (lambda <params> <body>)");
		}
		Sexp params = Function.expandParameterList((Sexp) arg.getFirst());
		TLObject body = TLisp.expand(arg.getSecond());
		return Sexp.doCons(Symbol.lambdaSym,
				Sexp.doCons(params, Sexp.doList(body)));
	}

	public static TLObject evalApply(Sexp exp) throws Exception {
		if (!(exp.getFirst() instanceof TLFunction && TLUtils.isCons(exp
				.getSecond()))) {
			throw new Exception(
					"Error in Apply definition: (apply <function> <body>)");
		}
		Function f = (Function) exp.getFirst();
		Sexp args = (Sexp) exp.getSecond();
		TLObject rv = Function.applyFunctionObject(f, args, true);
		return rv;
	}

	public static TLObject expandApply(Sexp exp) throws Exception {
		if (!TLUtils.isCons(exp.getSecond())) {
			throw new Exception(
					"Error in Apply definition: (apply <function> <body>)");
		}
		return Sexp.doCons(
				Symbol.applySym,
				Sexp.doCons(TLisp.expand(exp.getFirst()),
						Sexp.doList(TLisp.expand(exp.getSecond()))));
	}

	public static TLObject evalQuote(Sexp arg) throws Exception {
		return arg.getFirst();
	}

	public static TLObject expandQuote(Sexp arg) throws Exception {
		if (arg.isNil()) {
			return arg;
		}
		return Sexp.doCons(Symbol.quoteSym, arg);
	}

	// x[0] = first
	// (quasiquote ((unquotesplicing x) y))
	// (quasiquote (unquote x))
	// (quasiquote x)

	public static TLObject expandQuasiQuote(Sexp arg) throws Exception {
		return expandQuasiQuote(arg.getFirst());
	}

	public static TLObject expandQuasiQuote(TLObject x) throws Exception {
		if (x.isNil()) {
			return Sexp.NullSexp;
		}
		if (x.isAtom()) {
			return Sexp.doCons(Symbol.quoteSym, Sexp.doList(x));
		}
		Sexp sexp = (Sexp) x;
		TLObject first = sexp.getFirst();
		if (Symbol.unquoteSym.equals(first)) {
			return sexp.getSecond();
		}
		if (first.isPair()
				&& Symbol.unquoteSplicingSym.equals(((Sexp) first).getFirst())) {
			Sexp uqssexp = (Sexp) first;
			TLObject uqsarg = uqssexp.getSecond();
			TLObject rv = Sexp.doCons(
					Symbol.appendSym,
					Sexp.doCons(uqsarg,
							Sexp.doList(expandQuasiQuote(sexp.getCdr()))));
			return rv;
		}

		TLObject rv = Sexp.doCons(
				Symbol.consSym,
				Sexp.doCons(expandQuasiQuote(first),
						Sexp.doList(expandQuasiQuote(sexp.getCdr()))));

		return rv;

		// Original:
		// return Sexp.doCons(Symbol.consSym, Sexp.doCons(
		// Sexp.doCons(Symbol.quoteSym, Sexp.doList(first)),
		// Sexp.doList(expandQuasiQuote(sexp.getCdr()))));
	}

	public static TLObject expandListQuasiQuote(TLObject exp) throws Exception {
		if (TLUtils.isNil(exp)) {
			return exp;
		}
		Sexp s = (Sexp) exp;
		return Sexp.doCons(expandQuasiQuote(s.getCar()),
				expandListQuasiQuote(s.getCdr()));
	}

	public static TLObject evalCons(Sexp arg) throws Exception {
		TLObject o1 = arg.getFirst();
		TLObject o2 = arg.getSecond();
		return Sexp.doCons(o1, o2);
	}

	public static TLObject expandCons(Sexp arg) throws Exception {
		return Sexp.doCons(
				Symbol.consSym,
				Sexp.doCons(TLisp.expand(arg.getFirst()),
						Sexp.doList(TLisp.expand(arg.getSecond()))));
	}

	public static TLObject evalCar(Sexp arg) throws Exception {
		Sexp exp = (Sexp) arg.getFirst();
		return exp.getCar();
	}

	public static TLObject expandCar(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.carSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalCdr(Sexp arg) throws Exception {
		Sexp exp = (Sexp) arg.getFirst();
		return exp.getCdr();
	}

	public static TLObject expandCdr(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.cdrSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalAppend(Sexp arg) throws Exception {
		TLObject first = arg.getFirst();
		if (first.isNonNilAtom()) {
			throw new Exception("Append:  Arguments must be non-nil");
		}
		if (arg.getCdr().isNil()) {
			return first;
		}
		return Sexp.doAppend(Sexp.doCopy(arg.getFirst()),
				evalAppend((Sexp) arg.getCdr()));
	}

	public static TLObject expandAppend(Sexp arg) throws Exception {
		return Sexp.doCons(
				Symbol.appendSym,
				Sexp.doCons(TLisp.expand(arg.getFirst()),
						Sexp.doList(TLisp.expand(arg.getSecond()))));
	}

	public static TLObject evalSetq(Sexp arg) throws Exception {
		if (!arg.getFirst().isSymbol()) {
			throw new Exception("Setq:  First argument must be a symbol");
		}
		Symbol sym = TLisp.tLisp.getCurrentSymbolTable().findLocalSymbol(
				(Symbol) arg.getFirst());
		TLObject rv = TLisp.eval(arg.getSecond());
		sym.setValue(rv);
		return rv;
	}

	public static TLObject expandSetq(Sexp arg) throws Exception {
		if (!arg.getFirst().isSymbol()) {
			throw new Exception("Setq:  First argument must be a symbol");
		}
		Symbol sym = (Symbol) TLisp.expand(arg.getFirst());
		TLObject body = TLisp.expand(arg.getSecond());
		return Sexp.doCons(Symbol.setqSym, Sexp.doCons(sym, Sexp.doList(body)));
	}

	public static TLObject evalIf(Sexp arg) throws Exception {
		return (TLisp.eval(arg.getFirst()).isTrue() ? TLisp.eval(arg.getSecond())
				: TLisp.eval(arg.getThird()));
	}

	public static TLObject expandIf(Sexp arg) throws Exception {
		TLObject test = TLisp.expand(arg.getFirst());
		TLObject result1 = TLisp.expand(arg.getSecond());
		TLObject result2 = TLisp.expand(arg.getThird());
		return (Sexp.doCons(Symbol.ifSym,
				Sexp.doCons(test, Sexp.doCons(result1, Sexp.doList(result2)))));
	}

	public static TLObject evalCond(Sexp arg) throws Exception {
		for (Enumeration<TLObject> e = arg.elements(); e.hasMoreElements();) {
			TLObject to = e.nextElement();
			if (!(to.isCons() && ((Sexp) to).getLength() == 2)) {
				throw new Exception("Cond: " + to + " is not valid pair");
			}
			Sexp pair = (Sexp) to;
			TLObject rv = TLisp.eval(pair.getFirst());
			if (rv.isTrue()) {
				return TLisp.eval(pair.getSecond());
			}
		}
		return TLUtils.getNIL();
	}

	public static TLObject expandCond(Sexp arg) throws Exception {
		if (!(arg.getFirst().isCons() && ((Sexp) arg.getFirst()).getLength() == 2)) {
			throw new Exception("Cond: " + arg.getFirst() + "is not pair");
		}
		Sexp first = (Sexp) arg.getFirst();
		TLObject rest = TLUtils.getNIL();
		if (!arg.getCdr().isNil()) {
			rest = expandCond((Sexp) arg.getCdr());
		}
		return Sexp.doCons(
				Symbol.ifSym,
				Sexp.doCons(
						TLisp.expand(first.getFirst()),
						Sexp.doCons(TLisp.expand(first.getSecond()),
								Sexp.doList(rest))));
	}

	public static TLObject evalSetf(Sexp arg) throws Exception {
		if (TLUtils.isSymbol(arg.getFirst())) {
			return evalSetq(arg);
		}
		TLObject value = TLisp.eval(arg.getThird());
		// Sexp sexp = (Sexp) arg.getSecond();
		TLObject jlo = TLisp.eval(arg.getSecond());
		TLObject reference = TLisp.getLastReferenceObject();
		if (reference != null && reference.isSexp()) {
			Sexp parent = (Sexp) reference;
			if (jlo.equals(parent.getCar())) {
				parent.setCar(value);
			} else if (jlo.equals(parent.getCdr())) {
				parent.setCdr(value);
			}
		}
		return value;
	}

	public static TLObject expandSetf(Sexp arg) throws Exception {
		TLObject arg1 = TLisp.expand(arg.getFirst());
		TLObject arg2 = TLisp.expand(arg.getSecond());
		return Sexp
				.doCons(Symbol.setfSym, Sexp.doCons(arg1, Sexp.doList(arg2)));
	}

	public static TLObject evalDefine(Sexp arg) throws Exception {
		if (!(arg.getFirst() instanceof Symbol)) {
			throw new Exception("First argument of Define must be symbol");
		}
		Symbol sym = (Symbol) arg.getFirst();
		sym.setValue(TLisp.eval(arg.getSecond()));
		return sym.getValue();
	}

	public static TLObject expandDefine(Sexp arg) throws Exception {
		if (!(arg.getFirst() instanceof Symbol)) {
			throw new Exception("First argument of Define must be symbol");
		}
		Symbol sym = (Symbol) TLisp.expand(arg.getFirst());
		TLObject body = TLisp.expand(arg.getSecond());
		return Sexp.doCons(Symbol.defineSym,
				Sexp.doCons(sym, Sexp.doList(body)));
	}

	public static TLObject evalSequence(Sexp arg) throws Exception {
		TLisp tl = TLisp.tLisp;
		TLObject rv = TLUtils.getNIL();
		if (arg != null) {
			for (Enumeration<TLObject> e = arg.elements(); e.hasMoreElements();) {
				TLObject to = e.nextElement();
				rv = TLisp.eval(to);
				if (tl.isImmediateReturnFlag()) {
					tl.setImmediateReturnFlag(false);
					break;
				}
			}
		}
		return rv;
	}

	public static TLObject expandSequence(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.sequenceSym, TLisp.expandList(arg));
	}

	public static TLObject evalNot(Sexp arg) throws Exception {
		return (TLUtils.isNil(arg.getFirst()) ? TLUtils.getT() : TLUtils
				.getNIL());
	}

	public static TLObject expandNot(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.notSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalEqualp(Sexp arg) throws Exception {
		boolean rv = arg.getFirst().equals(arg.getSecond());
		return (rv ? TLUtils.getT() : TLUtils.getNIL());
	}

	public static TLObject expandEqualp(Sexp arg) throws Exception {
		return Sexp.doCons(
				Symbol.equalpSym,
				Sexp.doCons(TLisp.expand(arg.getFirst()),
						Sexp.doList(TLisp.expand(arg.getSecond()))));
	}

	// ADD FUNCTIONS/SYMBOLS

	public static TLObject evalEqp(Sexp arg) throws Exception {
		boolean rv = arg.getFirst().equals(arg.getSecond());
		return (rv ? TLUtils.getT() : TLUtils.getNIL());
	}
	
	// 6/17/2013:  Don't want this calling Sexp.equals(), but this won't return T for
	// equal JavaObjects...
//	public static TLObject evalEqp(Sexp arg) throws Exception {
//		boolean rv = arg.getFirst() == arg.getSecond();
//		return (rv ? TLUtils.getT() : TLUtils.getNIL());
//	}

	public static TLObject expandEqp(Sexp arg) throws Exception {
		return Sexp.doCons(
				Symbol.eqpSym,
				Sexp.doCons(TLisp.expand(arg.getFirst()),
						Sexp.doList(TLisp.expand(arg.getSecond()))));
	}

	public static TLObject evalLength(Sexp arg) throws Exception {
		if (!TLUtils.isCons(arg)) {
			throw new Exception("Argument to Length must be list");
		}
		Sexp sexp = (Sexp) arg;
		return new JavaObject(sexp.getLength());
	}

	public static TLObject expandLength(Sexp arg) throws Exception {
		if (!TLUtils.isCons(arg)) {
			throw new Exception("Argument to Length must be list");
		}
		return Sexp.doCons(Symbol.lengthSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalConsp(Sexp arg) throws Exception {
		return (arg.getFirst().isCons() ? TLUtils.getT() : TLUtils.getNIL());
	}

	public static TLObject expandConsp(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.conspSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalSymbolp(Sexp arg) throws Exception {
		return (arg.getFirst().isSymbol() ? TLUtils.getT() : TLUtils.getNIL());
	}

	public static TLObject expandSymbolp(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.symbolpSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalDefClass(Sexp arg) throws Exception {
		return (new TLClass(arg));
	}

	public static TLObject expandDefClass(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.defClassSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	public static TLObject evalDefInstance(Sexp arg) throws Exception {
		return (new TLClassInstance(arg));
	}

	public static TLObject expandDefInstance(Sexp arg) throws Exception {
		return Sexp.doCons(Symbol.defInstanceSym,
				Sexp.doList(TLisp.expand(arg.getFirst())));
	}

	// e.g. (getslot bill sex)
	public static TLObject evalGetSlotValue(Sexp arg) throws Exception {
		TLObject to = TLisp.eval(arg.getFirst());
		if (!(to instanceof TLClassInstance)) {
			throw new Exception("Invalid class instance: " + arg);
		}
		TLClassInstance inst = (TLClassInstance) to;
		Symbol csym = (Symbol) arg.getSecond();
		return inst.getSlotValue(csym);
	}

	public static TLObject expandGetSlotValue(Sexp arg) throws Exception {
		if (!(arg.getFirst() instanceof TLClassInstance && arg.getSecond()
				.isSymbol())) {
			throw new Exception("Invalid class instance: " + arg);
		}
		return Sexp.doCons(
				Symbol.getSlotValueSym,
				Sexp.doCons(TLisp.expand(arg.getFirst()),
						Sexp.doList(TLisp.expand(arg.getSecond()))));
	}

	// e.g. (getslot bill (sex male))
	public static TLObject evalSetSlotValue(Sexp arg) throws Exception {
		TLObject to = TLisp.eval(arg.getFirst());
		if (!(to instanceof TLClassInstance && arg
				.getSecond().isParameterPair())) {
			throw new Exception("Invalid class instance: " + arg);
		}
		TLClassInstance inst = (TLClassInstance) to;
		Sexp pair = (Sexp) arg.getSecond();
		Symbol csym = (Symbol) pair.getFirst();
//		TLObject value = TLisp.eval(pair.getSecond());
		TLObject value = pair.getSecond();
		inst.setSlotValue(csym, value);
		return value;
	}

	public static TLObject expandSetSlotValue(Sexp arg) throws Exception {
		if (!(arg.getFirst() instanceof TLClassInstance && arg.getSecond()
				.isSymbol())) {
			throw new Exception("Invalid class instance: " + arg);
		}
		return Sexp.doCons(Symbol.setSlotValueSym, TLisp.expandList(arg));
	}

	public static TLObject evalDefineWrappedJavaFunction(Sexp arg)
			throws Exception {
		return new WrappedJFunction(arg);
	}

	public static TLObject expandDefineWrappedJavaFunction(Sexp arg)
			throws Exception {
		return Sexp.doCons(Symbol.defineWrappedJFunctionSymbol,
				TLisp.expandList(arg));
	}

	public static TLObject evalApplyWrappedJFunction(Sexp arg) throws Exception {
		return WrappedJFunction.applyWrappedJFunctionSymbol(arg);
	}

	public static TLObject expandApplyWrappedJFunction(Sexp arg)
			throws Exception {
		return Function.expandStandardFunctionForm(
				Symbol.applyWrappedJFunctionSymbol, arg);
	}

	// TSL Functions

	// (tslloadrulefile <rulefile>)
	public static TLObject evalTSLLoadRuleFile(Sexp arg) throws Exception {
		KnowledgeBase kb = TLisp.tLisp.getTSLKnowledgeBase();
		if (!arg.getFirst().isString()) {
			throw new Exception(
					"tslloadrulefile: First argument must be string.");
		}
		String fname = (String) ((JavaObject) arg.getFirst()).getObject();
		kb.readRules(fname);
		return TLUtils.getT();
	}

	public static TLObject expandTSLLoadRuleFile(Sexp arg) throws Exception {
		return Function.expandStandardFunctionForm(Symbol.TSLLoadRuleFileSym,
				arg);
	}

	// (tslquery <expression>)
	public static TLObject evalTSLQuery(Sexp arg) throws Exception {
		if (!arg.getFirst().isCons()) {
			throw new Exception("tslquery: Argument must be ");
		}
		return TLUtils.getT();
	}

	public static TLObject expandTSLQuery(Sexp arg) throws Exception {
		if (!arg.getFirst().isCons()) {
			throw new Exception("tslquery: Argument must be ");
		}
		return Function.expandStandardFunctionForm(Symbol.TSLQuerySym, arg);
	}

	// (tslassert <expression>)
	public static TLObject evalTSLAssert(Sexp arg) throws Exception {
		return TLUtils.getT();
	}

	public static TLObject expandTSLAssert(Sexp arg) throws Exception {
		return Function.expandStandardFunctionForm(Symbol.TSLAssertSym, arg);
	}

	// MATH FUNCTIONS
	public static TLObject evalAdd(Object args) throws Exception {
		Vector v = (Vector) args;
		float rv = 0;
		for (Enumeration<Float> e = v.elements(); e.hasMoreElements();) {
			Float f = e.nextElement();
			rv += f.floatValue();
		}
		JavaObject josum = new JavaObject(new Float(rv));
		return josum;
	}

	public static TLObject expandAdd(Object args) throws Exception {
		Sexp s = (Sexp) TLisp.expandList((Sexp) args);
		return Sexp.doCons(Symbol.addSym, s);
	}

	public static TLObject evalSubtract(Object args) throws Exception {
		Vector v = (Vector) args;
		if (v.size() != 2) {
			throw new Exception("subtract:  Must have 2 arguments");
		}
		Float f1 = (Float) v.elementAt(0);
		Float f2 = (Float) v.elementAt(1);
		return new JavaObject(f1 - f2);
	}

	public static TLObject expandSubtract(Object args) throws Exception {
		Sexp s = (Sexp) TLisp.expandList((Sexp) args);
		return Sexp.doCons(Symbol.subtractSym, s);
	}

	public static TLObject evalTimes(Object args) throws Exception {
		Vector v = (Vector) args;
		if (v.size() != 2) {
			throw new Exception("times:  Must have 2 arguments");
		}
		Float f1 = (Float) v.elementAt(0);
		Float f2 = (Float) v.elementAt(1);
		return new JavaObject(f1 * f2);
	}

	public static TLObject expandTimes(Object args) throws Exception {
		Sexp s = (Sexp) TLisp.expandList((Sexp) args);
		return Sexp.doCons(Symbol.timesSym, s);
	}

	public static TLObject evalLessThanOrEqual(Object args) throws Exception {
		Vector v = (Vector) args;
		if (v.size() != 2) {
			throw new Exception("<=:  Must have 2 arguments");
		}
		Float f1 = (Float) v.elementAt(0);
		Float f2 = (Float) v.elementAt(1);
		return (f1 <= f2 ? TLUtils.getT() : TLUtils.getNIL());
	}

	public static TLObject expandLessThanOrEqual(Object args) throws Exception {
		Sexp s = (Sexp) TLisp.expandList((Sexp) args);
		return Sexp.doCons(Symbol.lessThanOrEqualsSym, s);
	}

}
