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
package tsl.expression.term.variable;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.ExpressionProofWrapper;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.jlisp.Symbol;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class Variable extends Term {
	public Object value = null;
	private int containingKBExpressionIndex = -1;
	private Variable sourceVariable = null;

	public Variable() {
	}

	public Variable(Object key, Object value) {
		initialize(key, value);
	}

	public Variable(Object key) {
		initialize(key, null);
	}

	public Variable(Variable var) {
		initialize(var, null);
	}

	public Variable(String name) {
		initialize(name, null);
	}

	// 4/30/2014: IS THIS GOING TO CAUSE PROBLEMS?
	public boolean equals(Object o) {
		if (o instanceof Variable) {
			Variable var = (Variable) o;
			return var.getName().equals(this.getName());
		}
		return false;
	}

	public Expression copy() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		return (Expression) kb.getTerm(null, this);
	}

	protected void initialize(Variable var, Object value) {
		this.setName(var.getName());
		this.setValue(value);
	}

	protected void initialize(Object key, Object value) {
		String name = key.toString();
		name = (name.charAt(0) == '?' ? name : "?" + name);
		this.setName(name);
		this.setValue(value);
	}

	public static Vector getValues(Vector<Variable> vars) {
		Vector values = null;
		if (vars != null) {
			for (Variable var : vars) {
				values = VUtils.add(values, var.getValue());
			}
		}
		return values;
	}

	public Object getValue() {
		if (this.value instanceof Variable) {
			return ((Variable) this.value).getValue();
		}
		return this.value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public static boolean isVariable(Object o) {
		if (o instanceof Variable) {
			return true;
		}
		if (o instanceof Symbol) {
			Symbol sym = (Symbol) o;
			String name = sym.getName();
			if (name != null && name.length() > 0 && name.charAt(0) == '?') {
				return true;
			}
		}
		if (o instanceof String) {
			String str = (String) o;
			if (str.length() > 0 && str.charAt(0) == '?') {
				return true;
			}
		}
		return false;
	}

	public static boolean isVariableString(Object o) {
		return (o instanceof String && isVariable(o));
	}
	
	// 12/3/2015
	public static String getTrimmedName(Object o) {
		String vname = null;
		if (o instanceof Variable) {
			vname = ((Variable) o).getName();
		} else {
			vname = o.toString();
		}
		if (vname.charAt(0) == '?') {
			vname = vname.substring(1);
		}
		return vname;
	}

	public static int getPositionVariableIndex(Object o) {
		String str = null;
		if (o instanceof Variable) {
			str = ((Variable) o).getName();
		} else if (o instanceof String) {
			str = (String) o;
		}
		if (str != null && str.length() > 1 && str.charAt(0) == '?'
				&& Character.isDigit(str.charAt(1))) {
			int index = Integer.valueOf(str.substring(1));
			return index;
		}
		return -1;
	}

	public void bind(Object value) {
		this.value = value;
	}

	// Before 4/4/2014
	// public void bind(Object value) {
	// this.value = value;
	// }

	public void unbind() {
		this.value = null;
	}

	public boolean isBound() {
		return this.value != null;
	}

	public static Variable createBoundVariable(String vname, Object value) {
		Variable var = new Variable(vname);
		var.bind(value);
		return var;
	}

	public static Vector<Variable> createVariables(Vector<String> vnames) {
		return createBoundVariables(vnames, null);
	}

	public static Vector<Variable> createBoundVariables(Vector items,
			Vector values) {
		Vector<Variable> bvars = null;
		if (items != null) {
			for (int i = 0; i < items.size(); i++) {
				Object item = items.elementAt(i);
				Variable bvar = null;
				if (item instanceof Variable) {
					bvar = new Variable((Variable) item);
				} else {
					bvar = new Variable((String) item);
				}
				bvars = VUtils.add(bvars, bvar);
				if (values != null) {
					bvar.bind(values.elementAt(i));
				}
			}
		}
		return bvars;
	}

	public static void bind(Vector<Variable> vars, Vector<Object> values) {
		if (vars != null && values != null && vars.size() == values.size()) {
			for (int i = 0; i < vars.size(); i++) {
				Variable var = vars.elementAt(i);
				Object value = values.elementAt(i);
				var.bind(value);
			}
		}
	}

	public static void unbind(Vector<Variable> vars) {
		if (vars != null) {
			for (int i = 0; i < vars.size(); i++) {
				Variable var = vars.elementAt(i);
				var.unbind();
			}
		}
	}

	public Object eval() {
		Object value = this.getValue();
		if (value == null) {
			ProofVariable pvar = this.getProofVariable();
			if (pvar != null) {
				value = pvar.getValue();
			}
		}
		// 4/28/2015: Recursively retrieve bindings stored with FCIE
		if (value == null && this.getContainingKBExpressionIndex() >= 0) {
			Expression ckbe = this.getContainingKBExpression();
			if (ckbe != null && ckbe.getVariableBindings() != null
					&& this.getContainingKBExpressionIndex() >= 0) {
				
				// 7/7/2016
				value = ckbe.getVariableBindings()[this.getContainingKBExpressionIndex()];
				if (value instanceof Term) {
					value = ((Term) value).eval();
				}

//				Term term = (Term) ckbe.getVariableBindings()[this
//						.getContainingKBExpressionIndex()];
//				if (term != null) {
//					value = term.eval();
//				}
			}
		}
		return value;
	}

	public ProofVariable getProofVariable() {
		if (this.getContainingKBExpression() != null) {
			List pvars = this.getContainingKBExpression().getProofVariables();
			return getProofVariable(pvars);
		}
		return null;
	}

	public ProofVariable getProofVariable(List<ProofVariable> pvars) {
		int index = this.getContainingKBExpressionIndex();
		if (pvars != null && this.getContainingKBExpressionIndex() >= 0) {
			ProofVariable pvar = pvars.get(index);
			return pvar;
		}
		return null;
	}

	public Term getProofVariableValue() {
		Variable pvar = this.getProofVariable();
		if (pvar != null) {
			return (Term) pvar.getValue();
		}
		return null;
	}

	public static Variable find(Vector v, Term t) {
		return find(v, t.getName());
	}

	// 3/26/2015
	public static Variable find(Vector<Variable> v, Object o) {
		String str = (o instanceof Variable ? ((Variable) o).getName()
				: (String) o);
		if (v != null) {
			for (Variable var : v) {
				if (str.equals(var.getName())) {
					return var;
				}
			}
		}
		if (!Variable.isVariable(str)) {
			String vname = "?" + str;
			return find(v, vname);
		}
		return null;
	}

	public static Variable find(Vector<Variable> v, String str) {
		if (v != null) {
			for (Variable var : v) {
				if (str.equals(var.getName())) {
					return var;
				}
			}
		}
		if (!Variable.isVariable(str)) {
			String vname = "?" + str;
			return find(v, vname);
		}
		return null;
	}

	public static Variable find(Variable[] vs, String str) {
		if (vs != null) {
			for (int i = 0; i < vs.length; i++) {
				if (str.equals(vs[i].getName())) {
					return vs[i];
				}
			}
		}
		return null;
	}

	public static Variable findByValue(Vector v, Object value) {
		for (Enumeration<Variable> e = v.elements(); e.hasMoreElements();) {
			Variable var = e.nextElement();
			if (var.value != null && var.value.equals(value)) {
				return var;
			}
		}
		return null;
	}

	public static Object findValue(Vector v, Term t) {
		return findValue(v, t.getName());
	}

	public static Object findValue(Vector v, String name) {
		Variable var = find(v, name);
		return (var != null ? var.value : null);
	}

	public int getContainingKBExpressionIndex() {
		return containingKBExpressionIndex;
	}

	public void setContainingKBExpressionIndex(int containingKBExpressionIndex) {
		this.containingKBExpressionIndex = containingKBExpressionIndex;
	}

	// 5/30/2014
	public Variable getExpressionProofWrapperVariable() {
		if (this.getContainingKBExpression() != null) {
			ExpressionProofWrapper epw = this.getContainingKBExpression()
					.getLastExpressionProofWrapper();
			if (epw != null) {
				return epw.getExpressionProofWrapperVariable(this);
			}
		}
		return null;
	}

	public String toString() {
		ProofVariable pvar = this.getProofVariable();
		if (pvar != null) {
			String str = "(PV)";
			return str + pvar.toString();
		}
		String name = this.getName();
		if (name == null) {
			name = "?*";
		}
		Object value = this.getValue();
		if (value != null) {
			return "[" + name + "=" + value + "]";
		}
		return name;
	}

	public static TypeConstant extractType(String name) {
		TypeConstant type = TypeConstant.findByName(name);
		if (type == null && Variable.isVariable(name)) {
			type = TypeConstant.findByName(name.substring(1));
		}
		if (type == null && name != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				if (c == '<') {
					i++;
					for (; name.charAt(i) != '>'; i++) {
						c = name.charAt(i);
						sb.append(c);
					}
				}
			}
			if (sb.length() > 0) {
				type = TypeConstant.findByName(sb.toString());
			}
		}
		return type;
	}

	// 4/30/2014
	public static Vector<Variable> gatherVariables(Vector v) {
		Vector<Variable> vars = null;
		for (Object o : v) {
			if (Variable.isVariable(o)) {
				if (o instanceof Variable) {
					vars = VUtils.add(vars, (Variable) o);
				} else if (o instanceof String) {
					String vstr = (String) o;
					if (Variable.find(vars, vstr) == null) {
						Variable var = new Variable(vstr);
						vars = VUtils.add(vars, var);
					}
				}
			} else if (o instanceof Vector) {
				vars = VUtils.appendIfNot(vars, gatherVariables((Vector) o));
			}
		}
		return vars;
	}

	public Variable getSourceVariable() {
		return sourceVariable;
	}

	public void setSourceVariable(Variable sourceVariable) {
		this.sourceVariable = sourceVariable;
	}
	
	public String getTuffyString() {
		String vname = this.getName().toLowerCase();
		return vname.substring(1);
	}

}
