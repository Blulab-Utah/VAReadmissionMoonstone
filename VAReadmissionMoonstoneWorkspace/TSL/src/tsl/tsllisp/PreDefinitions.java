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

public class PreDefinitions {

	public static String userdefs = "(begin "

			+ "(defun doadd (x y) (+ x y))"
			+ "(defun fact (n) (if (<= n 1) 1 (* n (fact (- n 1)))))"
			+ "(defun fib (n) (cond ((= n 0) 0) ((= n 1) 1) (t (+ (fib (- n 1)) (fib (- n 2))))))"

			+ ")";

	public static void initFunctions() throws Exception {

		// S-expression arguments
		new JFunction(Symbol.forLoopSym, "evalForLoop", "expandForLoop",
				TLFunction.SEXPPARAM, false, 4);
		new JFunction(Symbol.returnSym, "evalReturn", "expandReturn",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.expandSym, "evalExpand", "evalExpand",
				TLFunction.SEXPPARAM, false, 1);
		new JFunction(Symbol.letSym, "evalLet", "expandLet",
				TLFunction.SEXPPARAM, false, -1);
		new JFunction(Symbol.functionSym, "evalFunction", "expandFunction",
				TLFunction.SEXPPARAM, false, 3);
		new JFunction(Symbol.defunSym, "evalDefun", "expandDefun",
				TLFunction.SEXPPARAM, false, 3);
		new JFunction(Symbol.defMethodSym, "evalDefMethod", "expandDefMethod",
				TLFunction.SEXPPARAM, false, 3);
		new JFunction(Symbol.defMacroSym, "evalDefmacro", "expandDefmacro",
				TLFunction.SEXPPARAM, false, 3);
		new JFunction(Symbol.lambdaSym, "evalLambda", "expandLambda",
				TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.setqSym, "evalSetq", "expandSetq",
				TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.setfSym, "evalSetf", "expandSetf",
				TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.applySym, "evalApply", "expandApply",
				TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.consSym, "evalCons", "expandCons",
				TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.carSym, "evalCar", "expandCar",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.cdrSym, "evalCdr", "expandCdr",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.appendSym, "evalAppend", "expandAppend",
				TLFunction.SEXPPARAM, true, -1);
		new JFunction(Symbol.ifSym, "evalIf", "expandIf", TLFunction.SEXPPARAM,
				false, 3);
		new JFunction(Symbol.condSym, "evalCond", "expandCond",
				TLFunction.SEXPPARAM, false, -1);
		new JFunction(Symbol.sequenceSym, "evalSequence", "expandSequence",
				TLFunction.SEXPPARAM, false, -1);
		new JFunction(Symbol.notSym, "evalNot", "expandNot",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.equalpSym, "evalEqualp", "expandEqualp",
				TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.eqpSym, "evalEqp", "expandEqp",
				TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.lengthSym, "evalLength", "expandLength",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.conspSym, "evalConsp", "expandConsp",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.symbolpSym, "evalSymbolp", "expandSymbolp",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.nullpSym, "evalNot", "expandNot",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.defClassSym, "evalDefClass", "expandDefClass",
				TLFunction.SEXPPARAM, false, -1);
		new JFunction(Symbol.defInstanceSym, "evalDefInstance",
				"expandDefInstance", TLFunction.SEXPPARAM, false, -1);
		new JFunction(Symbol.getSlotValueSym, "evalGetSlotValue",
				"expandGetSlotValue", TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.setSlotValueSym, "evalSetSlotValue",
				"evalSetSlotValue", TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.defineWrappedJFunctionSymbol,
				"evalDefineWrappedJavaFunction",
				"expandDefineWrappedJavaFunction", TLFunction.SEXPPARAM, false,
				2);
		new JFunction(Symbol.applyWrappedJFunctionSymbol,
				"evalApplyWrappedJFunction", "expandApplyWrappedJFunction",
				TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.evalSym, "evalEval", "expandEval",
				TLFunction.SEXPPARAM, true, 1);
		new JFunction(Symbol.mapSym, "evalMap", "expandMap",
				TLFunction.SEXPPARAM, false, 2);

		// TSL functions
		new JFunction(Symbol.TSLLoadRuleFileSym, "evalTSLLoadRuleFile",
				"expandTSLLoadRuleFile", TLFunction.SEXPPARAM, true, 2);
		new JFunction(Symbol.TSLQuerySym, "evalTSLQuery", "expandTSLQuery",
				TLFunction.SEXPPARAM, false, 2);
		new JFunction(Symbol.TSLAssertSym, "evalTSLAssert", "expandTSLAssert",
				TLFunction.SEXPPARAM, false, 2);

		// Math Functions
		new JFunction(Symbol.addSym, "evalAdd", "expandAdd",
				TLFunction.OBJECTPARAM, true, -1);
		new JFunction(Symbol.subtractSym, "evalSubtract", "expandSubtract",
				TLFunction.OBJECTPARAM, true, 2);
		new JFunction(Symbol.timesSym, "evalTimes", "expandTimes",
				TLFunction.OBJECTPARAM, true, 2);
		new JFunction(Symbol.lessThanOrEqualsSym, "evalLessThanOrEqual",
				"expandLessThanOrEqual", TLFunction.OBJECTPARAM, true, 2);

		// Quote-related functions
		new JFunction(Symbol.quoteSym, "evalQuote", "expandQuote",
				TLFunction.SEXPPARAM, false, 1);
		new JFunction(Symbol.quasiQuoteSym, null, "expandQuasiQuote",
				TLFunction.SEXPPARAM, false, 1);
	}

}
