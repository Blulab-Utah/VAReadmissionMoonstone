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
package tsl.inference.backwardchaining;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class ProofVariable extends Variable {
	public Variable sourceVariable = null;
	public int index = 0;

	// 11/9/2012
	// MOVE THESE TO KNOWLEDGEBASE, NON-STATIC
	public static Vector<Vector<Expression>> provisionalBindings = null;
	public static Vector<Vector<Expression>> validatedBindings = null;

	public static int PVSSize = 1000000;
	public static Vector<ProofVariable> PVS = null;
	public static int PVSIndex = 0;
	public static ObjectConstant NullVariableValue = new ObjectConstant("*NULL*");

	public ProofVariable() {
	}

	public ProofVariable(Variable var) {
		super(var);
		sourceVariable = var;
	}

	public static void initialize() {
		validatedBindings = provisionalBindings = null;
		PVSIndex = 0;
		if (PVS == null) {
			PVS = new Vector();
			for (int i = 0; i < PVSSize; i++) {
				ProofVariable pv = new ProofVariable();
				pv.index = i;
				PVS.add(pv);
			}
		}
	}

	// 5/20/2014: This method adds 20% to overall processing time.
	public static void storeProvisionalBindings() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		List<ProofVariable> pvars = kb.getQueryExpression().getProofVariables();
		Vector<Expression> values = getValues(pvars);
		Vector pbs = provisionalBindings;
		if (values != null) {
			provisionalBindings = VUtils.addIfNot(provisionalBindings, values);
		}
	}

	public static void storeValidatedBindings() {
		Vector<Vector<Expression>> vb = validatedBindings;
		Vector<Vector<Expression>> pb = provisionalBindings;

		validatedBindings = VUtils.append(validatedBindings,
				provisionalBindings);
		Vector<Vector<Expression>> newvb = validatedBindings;
		provisionalBindings = null;
	}

	// Before 5/20/2014
	// public static void storeProvisionalBindings() {
	// KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
	// List<ProofVariable> pvars = kb.getQueryExpression().getProofVariables();
	// Vector values = getValues(pvars);
	// if (values != null) {
	// // EXPENSIVE!! Will there ever be duplicates?
	// provisionalBindings = VUtils.addIfNot(provisionalBindings, values);
	// }
	// }
	//
	// public static void storeValidatedBindings() {
	// // validatedBindings = VUtils.appendIfNot(validatedBindings,
	// // provisionalBindings);
	// // 4/17/2013 -- Will there ever be duplicates?
	// validatedBindings = VUtils.append(validatedBindings,
	// provisionalBindings);
	// provisionalBindings = null;
	// }

	public static Vector getValidatedBindings() {
		return validatedBindings;
	}

	public static Vector unpackValidatedBindings() {
		Vector v = getValidatedBindings();
		Vector unpacked = null;
		if (v != null) {
			for (Enumeration e1 = v.elements(); e1.hasMoreElements();) {
				Vector binds = (Vector) e1.nextElement();
				unpacked = VUtils.add(unpacked, Term.unpack(binds));
			}
		}
		return unpacked;
	}

	public static void clearProvisionalBindings() {
		provisionalBindings = null;
	}

	// 4/4/2014: Disables infinite loops.
	public Object getValue() {
		if (this.isVisited()) {
			return null;
		}
		this.setVisited(true);
		Object value = null;
		if (this.value instanceof ProofVariable) {
			value = ((ProofVariable) this.value).getValue();
		} else if (this.value instanceof Term) {
			value = ((Term) this.value).eval();
		} else {
			value = this.value;
		}
		this.setVisited(false);
		return value;
	}

	public Object eval() {
		return this.getValue();
	}

	public static Object getValue(Object o) {
		if (o instanceof ProofVariable) {
			ProofVariable pvar = (ProofVariable) o;
			return pvar.getValue();
		}
		if (o instanceof Variable) {
			Variable var = (Variable) o;
			return var.eval();
		}
		return o;
	}

	public static Vector getValues(Sentence sentence) {
		List pvars = sentence.getProofVariables();
		Vector values = getValues(pvars);
		return values;
	}

	// 5/22/2014:  If prove() gets this far, all the variables are validated, even if one of them
	// isn't bound to a value, as in the case of an OrSentence.  Use the NullVariable in that case.
	// NOT FULLY TESTED!!
	public static Vector getValues(List<ProofVariable> pvars) {
		Vector values = null;
		boolean hasNonNullValue = false;
		if (pvars != null) {
			for (Iterator i = pvars.iterator(); i.hasNext();) {
				Object o = i.next();
				Object value = getValue(o);
				if (value == null) {
					
					return null;
					// 5/22/2014:  Allow this to succeed if a variable is null in an OrSentence.
//					value = NullVariableValue;
					
				} else {
					hasNonNullValue = true;
				}
				values = VUtils.add(values, value);
			}
		}
		if (!hasNonNullValue) {
			values = null;
		}
		return values;
	}
	
	private static boolean allNullValues(Vector values) {
		for (Object value : values) {
			if (!NullVariableValue.equals(value)) {
				return false;
			}
		}
		return true;
	}
	
//	public static Vector getValues(List<ProofVariable> pvars) {
//		Vector values = null;
//		if (pvars != null) {
//			for (Iterator i = pvars.iterator(); i.hasNext();) {
//				Object o = i.next();
//				Object value = getValue(o);
//				if (value == null) {
//					return null;
//				}
//				values = VUtils.add(values, value);
//			}
//		}
//		return values;
//	}

	public static List<ProofVariable> wrapVariables(Vector<Variable> vars) {
		List pvars = null;
		if (vars != null) {
			int startIndex = PVSIndex;
			for (Variable var : vars) {
				ProofVariable pvar = PVS.elementAt(PVSIndex++);
				pvar.initialize(var.getName(), var.value);

			}
			pvars = PVS.subList(startIndex, PVSIndex);
		}
		return pvars;
	}

	public static Vector wrapVariablesOLD(Vector vars) {
		Vector pvars = null;
		if (vars != null) {
			for (Enumeration e = vars.elements(); e.hasMoreElements();) {
				Variable var = (Variable) e.nextElement();
				ProofVariable pvar = new ProofVariable(var);
				pvars = VUtils.add(pvars, pvar);
			}
		}
		return pvars;
	}

	public static List clone(List<ProofVariable> pvars) {
		List<ProofVariable> newvars = null;
		if (pvars != null) {
			int startIndex = PVSIndex;
			for (ProofVariable pvar : pvars) {
				ProofVariable newvar = PVS.get(PVSIndex++);
				pvar.copy(newvar);
			}
			newvars = PVS.subList(startIndex, PVSIndex);
		}
		return newvars;
	}

	public void copy(ProofVariable newvar) {
		newvar.initialize(this.getName(), this.value);
	}

	public Object clone() {
		ProofVariable newvar = PVS.elementAt(PVSIndex++);
		newvar.initialize(this.getName(), null);
		newvar.value = this.value;
		return newvar;
	}

	public static void resetPVSIndex(int index) {
		PVSIndex = index;
	}

	public static void copyBinds(List<ProofVariable> source,
			List<ProofVariable> target) {
		if (source != null && target != null) {
			for (int i = 0; i < source.size(); i++) {
				ProofVariable spvar = source.get(i);
				ProofVariable tpvar = target.get(i);
				tpvar.bind(spvar.value);
			}
		}
	}

	public String getName() {
		return "PV:" + ((Variable) this).name;
	}
	
	public static Term getNullVariableValue() {
		return NullVariableValue;
	}

}
