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
package tsl.expression.form.sentence.constraint;

import tsl.expression.Expression;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.variable.Variable;
import tsl.information.TSLInformation;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

public class Constraint {

	private KnowledgeBase knowledgeBase = null;
	private String methodName = null;
	private Method method = null;
	private Object[] params = null;
	private Object[] args = null;
	private boolean requiresBoundVariables = false;
	private String constraintString = null;
	private boolean isTerminal = true;
	private static Hashtable<String, String> ConstraintOperatorHash = new Hashtable();
	private static String[][] ConstraintOperatorMap = new String[][] {
			{ "<", "lessThan" }, { "<=", "lessThanOrEqual" },
			{ ">", "greaterThan" }, { ">=", "greaterThanOrEqual" },
			{ "==", "argumentsAreEqual" }, { "!=", "argumentsAreUnequal" },
			{ "if", "ifThenElse" }, { "and", "andTest" }, { "or", "orTest" },
			{ "not", "notTest" }, { "1+", "plusOne" }, { "1-", "minusOne" },
			{ "tsc", "termStringContains" },
			{ "ridsr", "ruleIDContainsStringRecursive" },
			{ "ccsr", "conceptContainsStringRecursive" }, };

	public Constraint(KnowledgeBase kb, Method method, int psize) {
		this.knowledgeBase = kb;
		this.method = method;
		this.params = new Object[psize];
		this.args = new Object[psize];
	}

	public static Constraint createConstraint(KnowledgeBase kb, String cstr) {
		Constraint c = null;
		try {
			Sexp s = (Sexp) TLisp.getTLisp().evalString(cstr);
			Vector v = TLUtils.convertSexpToJVector(s);
			Vector<Variable> vars = Variable.gatherVariables(v);
			c = createConstraint(kb, v, vars);
			c.constraintString = cstr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static Constraint createConstraint(KnowledgeBase kb, Vector v) {
		return createConstraint(kb, v, null);
	}

	// 3/27/2015 note: A constraint can't currently contain a pattern language
	// vector.
	// Have to use local variables...
	public static Constraint createConstraint(KnowledgeBase kb, Vector v,
			Vector<Variable> vars) {
		Constraint c = null;
		String str = (String) v.firstElement();
		String mname = getConstraintOperatorName(str);
		Method method = getMethod(kb, mname, v.size() - 1);
		if (method != null) {
			c = new Constraint(kb, method, v.size() - 1);
			for (int i = 1; i < v.size(); i++) {
				Object o = v.elementAt(i);
				if (o instanceof Vector) {
					c.isTerminal = false;
					Constraint subc = createConstraint(kb, (Vector) o, vars);
					if (subc != null) {
						c.params[i - 1] = subc;
					} else {
						c.params[i - 1] = new ObjectConstant(o);    // 5/13/2015
					}
				} else if (Variable.isVariableString(o)) {
					c.params[i - 1] = (String) o;
				} else {
					c.params[i - 1] = o;
				}
				if (c.params[i - 1] == null) {
					return null;
				}
			}
		}
		return c;
	}

	private static Method getMethod(KnowledgeBase kb, String mname, int psize) {
		Method m = null;
		String pstr = kb.getKnowledgeEngine().getStartupParameters()
				.getPropertyValue("JavaPackages");
		if (pstr != null) {
			String[] cnames = pstr.split(",");
			for (int i = 0; i < cnames.length && m == null; i++) {
				try {
					Class c = Class.forName(cnames[i]);
					for (int j = 0; j < c.getMethods().length; j++) {
						Method cm = c.getMethods()[j];
						if (mname.equals(cm.getName())
								&& cm.getParameterAnnotations().length == psize) {
							m = cm;
							break;
						}
					}
				} catch (ClassNotFoundException e) {
					System.out.println("Constraint:  Class Not Found: "
							+ cnames[i]);
				}
			}
		}
		return m;
	}

	public static void initialize() {
		for (int i = 0; i < ConstraintOperatorMap.length; i++) {
			String key = ConstraintOperatorMap[i][0];
			String value = ConstraintOperatorMap[i][1];
			ConstraintOperatorHash.put(key, value);
		}
	}

	private static String getConstraintOperatorName(String str) {
		String value = ConstraintOperatorHash.get(str);
		if (value == null) {
			value = str;
		}
		return value;
	}
	
	public boolean doTestConstraint(Vector<Variable> boundVars) {
		boolean success = false;
		try {
			if (this.method != null && this.params != null && boundVars != null) {
				Object result = this.evalConstraint(boundVars);
				if (result instanceof Boolean) {
					Boolean b = (Boolean) result;
					success = b.booleanValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public boolean doTestConstraint(Object[] arguments) {
		boolean success = false;
		try {
			if (this.method != null && this.params != null && arguments != null) {
				Object result = this.evalConstraint(arguments);
				if (result instanceof Boolean) {
					Boolean b = (Boolean) result;
					success = b.booleanValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public Object evalConstraint(Vector<Variable> boundVars) {
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];

			Object arg = Expression.evalPattern(param, boundVars, true);

			if (arg == null) {
				return null;
			}
			args[i] = arg;
		}
		return evalConstraint(args);
	}

	public Object evalConstraint(Object[] arguments) {
		Object result = null;
		try {
			result = this.method.invoke(null, arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean requiresBoundVariables() {
		return this.requiresBoundVariables;
	}

	public String toString() {
		String str = "<TestPredicate: " + this.method.getName() + ">";
		return str;
	}

	public String toLisp() {
		String lstr = "(\"";
		lstr += this.getMethodName() + "\" ";
		int x = 0;
		for (int i = 0; i < this.params.length; i++) {
			Object o = this.params[i];
			String str = o.toString();
			if (o instanceof Expression) {
				Expression e = (Expression) o;
				str = e.toLisp();
			}
			lstr += str;
			if (i < this.params.length - 1) {
				lstr += " ";
			}
		}
		lstr += ")";
		return lstr;
	}
	
	// 2/3/2015
	public static String toLisp(Vector<Constraint> constraints) {
		String lstr = "";
		for (Constraint c : constraints) {
			lstr += c.toLisp() + " ";
		}
		return lstr;
	}

	protected Vector<Variable> gatherVariables() {
		Vector<Variable> variables = null;
		for (int i = 0; i < this.params.length; i++) {
			Object param = this.params[i];
			if (param instanceof Variable) {
				variables = VUtils.addIfNot(variables, param);
			} else if (param instanceof Constraint) {
				Constraint tp = (Constraint) param;
				variables = VUtils.appendIfNot(variables, tp.gatherVariables());
			}
		}
		return variables;
	}

	public String getConstraintString() {
		return this.constraintString;
	}

	public String getMethodName() {
		return this.method.getName();
	}
	
	public Object[] getParmeters() {
		return this.params;
	}
	
	public Object getParameter(int index) {
		if (index < this.params.length) {
			return this.params[index];
		}
		return null;
	}
	
	public boolean isTerminal() {
		return this.isTerminal;
	}
	
}
