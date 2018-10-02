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
package tsl.expression.term.function.logicfunction;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionTerm;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class LogicFunctionTerm extends FunctionTerm {

	private LogicFunctionConstant fconst = null;

	private Object value = null;

	public LogicFunctionTerm() {

	}

	public LogicFunctionTerm(Vector v) {
		super((String) v.firstElement());
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		// Before 3/24/2014
//		KnowledgeBase kb = this.getKnowledgeBase();
		this.fconst = (LogicFunctionConstant) kb.getNameSpace()
				.getFunctionConstant(this.getName());
		for (int i = 0; i < this.fconst.getVariableCount() - 1; i++) {
			String str = (String) v.elementAt(i + 1);
			Term term = (Term) kb.getTerm(this, str);
			this.addTerm(term);
		}
		kb.addFunctionTermList(this);
	}

	public static LogicFunctionTerm createLogicFunctionTerm(Vector v) {
		if (v != null && "deffunction".equals(v.firstElement())) {
			return new LogicFunctionTerm(v);
		}
		return null;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean prove(Vector alsoProve) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		Vector lfterms = null;
		// 5/13/2014:  I took out the KB stored expression hash.  Need to reimplement this.
//		lfterms = kb.getStoredExpressions(this.fconst.getName()
//				.toLowerCase());
		boolean foundMatch = false;
		if (lfterms != null) {
			List origbinds = null;
			List copybinds = null;
			List apvars = Expression.getProofVariables(alsoProve);
			if (apvars != null) {
				origbinds = new Vector(apvars);
				copybinds = ProofVariable.clone(origbinds);
			}
			for (Enumeration e = lfterms.elements(); e.hasMoreElements();) {
				LogicFunctionTerm head = (LogicFunctionTerm) e.nextElement();
				head.pushProofVariables(null);
				Vector pbinds = (Vector) this.getProofVariables();
				Vector cbinds = (Vector) head.getProofVariables();
				if (LogicFunctionTerm.match(this, pbinds, head, cbinds)) {
					foundMatch = true;
				}
				head.popProofVariables();
				if (apvars != null) {
					ProofVariable.copyBinds(copybinds, origbinds);
				}
			}
		}
		return foundMatch;
	}

	public boolean match(List<ProofVariable> pbinds, LogicFunctionTerm child,
			List<ProofVariable> cbinds) {
		String relname1 = null, relname2 = null;
		Vector terms1 = null, terms2 = null;
		relname1 = this.getName();
		terms1 = this.getTerms();
		relname2 = child.getName();
		terms2 = child.getTerms();
		if (relname1.equals(relname2) && terms1.size() == terms2.size()) {
			for (int i = 0; i < terms1.size(); i++) {
				Object term1 = terms1.elementAt(i);
				Object term2 = terms2.elementAt(i);
				if (term1 instanceof Variable) {
					term1 = ((Variable) term1).getProofVariable(pbinds);
				}
				if (term2 instanceof Variable) {
					term2 = ((Variable) term2).getProofVariable(cbinds);
				}
				if (!Term.match(term1, pbinds, term2, cbinds)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// If the current LFT doesn't have a value (i.e. is contained in a sentence
	// and has variables), use
	// variable bindings to find LFT in KB hash table, and return that LFT's
	// value.
	
	public Object eval() {
		Object value = this.value;
		KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getCurrentKnowledgeBase();
		if (value == null && kb != null) {
			LogicFunctionTerm lft = (LogicFunctionTerm) kb
					.getLogicFunctionValue(this);
			if (lft != null) {
				value = lft.value;
			}
		}
		return value;
	}

	// LFTs are stored with the KB, and values are stored with the LFTs. During
	// a proof, an LFT with bound
	// variables will produce the same hash code as the original bound LFT, and
	// so we can get the value from the
	// KB hash.
	public boolean equals(Object o) {
		if (o instanceof LogicFunctionTerm) {
			LogicFunctionTerm lft = (LogicFunctionTerm) o;
			return this.hashCode() == lft.hashCode();
		}
		return false;
	}

	// OR of name + bound variables.
	public int hashCode() {
		int code = this.fconst.hashCode();
		for (int i = 0; i < this.getTermCount(); i++) {
			Object value = this.getTerm(i);
			if (value instanceof Variable) {
				Variable var = (Variable) value;
				value = var.eval();
			}
			code |= value.hashCode();
		}
		return code;
	}

}
