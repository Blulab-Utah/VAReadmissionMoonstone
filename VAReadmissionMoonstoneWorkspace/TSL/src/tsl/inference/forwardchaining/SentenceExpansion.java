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
package tsl.inference.forwardchaining;

import tsl.expression.Expression;
import tsl.expression.form.sentence.BindSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.JavaRelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class SentenceExpansion extends SentenceObject {

	protected int bindingLength = 0;
	
	// Create a further expansion of an SE based on a new SI. Copy information
	// from the old SE;
	// add the new SI. (Before calling this, check that the new SI's variables
	// are unifiable.)
	public SentenceExpansion(SentenceExpansion se, SentenceInstance si) {
		try {
			Sentence ckbe = (Sentence) se.getNamedSentence().getSentence()
					.getContainingKBExpression();
			int max = Math.max(se.getDepth(), si.getDepth());
			this.setDepth(max + 1);

			this.namedSentence = se.namedSentence;
			this.bindings = new Object[se.bindings.length];
			for (int i = 0; i < se.bindings.length; i++) {
				Object o = (se.bindings[i] != null ? se.bindings[i]
						: si.bindings[i]);
				if (o instanceof Term) {
					o = ((Term) o).eval();
				}
				this.bindings[i] = o;
			}
			this.bindingLength = this.bindings.length; // Just for visibility...
			this.childSentenceInstances = new SentenceInstance[se.childSentenceInstances.length];
			for (int i = 0; i < se.childSentenceInstances.length; i++) {
				this.childSentenceInstances[i] = se.childSentenceInstances[i];
			}

			if (this.childSentenceInstances.length >= 3) {
				int x = 1;
			}
			this.childSentenceInstances[si.namedSentence.parentSentenceIndex] = si;
			process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Given a SentenceInstance, if the si's NamedSentence has a parent
	// NamedSentence, create a new SE
	public SentenceExpansion(SentenceInstance child) {
		try {
			this.setDepth(child.getDepth() + 1);
			this.namedSentence = child.namedSentence.parent;
			this.bindings = new Object[child.bindings.length];
			this.bindingLength = this.bindings.length; // Just for visibility...
			for (int i = 0; i < child.bindings.length; i++) {
				this.bindings[i] = child.bindings[i];
			}
			this.childSentenceInstances = new SentenceInstance[this.namedSentence.childNamedSentences.length];
			this.childSentenceInstances[child.namedSentence
					.getParentSentenceIndex()] = child;
			process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static SentenceExpansion createSentenceExpansion(
			SentenceExpansion se, SentenceInstance si) {
		SentenceExpansion newse = null;
		if (se.isUnifiable(si)) {
			newse = new SentenceExpansion(se, si);
		}
		return newse;
	}

	// 10/29/2015 note:  This is where almost all problems occur, comparing a number with
	// a string, a StringConstant or TypeConstant with an ObjectConstant, etc.  I need
	// a well-thought-out solution.
	public boolean isUnifiable(SentenceInstance si) {
		for (int i = 0; i < this.bindings.length; i++) {
			Object o1 = this.bindings[i];
			Object o2 = si.bindings[i];
			if (o1 != null
					&& o2 != null
					&& !(o1.equals(o2) 
							|| o2.equals(o1) 
							|| o1.toString()
							.equals(o2.toString()))) {
				return false;
			}
		}
		return true;
	}
	
	public void process() {
		if (this.namedSentence.inferenceEngine.isDoDebug()) {
			System.out.println(this.toString());
		}
		Expression cbke = this.namedSentence.sentence
				.getContainingKBExpression();
		cbke.pushVariableBindings(this.bindings);
		if (this.evaluate()) {
			new SentenceInstance(this);
		}
		for (int i = 0; i < this.childSentenceInstances.length; i++) {
			if (this.childSentenceInstances[i] == null) {
				// Analogous to storing indexes of missing pattern elements with
				// RuleExpansions.
				NamedSentence childNS = this.namedSentence.childNamedSentences[i];
				// ?? Do I need to store indices for JavaRelationSentences?
				VUtils.pushHashVector(this.namedSentence.getInferenceEngine()
						.getSentenceExpansionTable(), childNS, this);
			}
		}
		cbke.popVariableBindings();
	}

	// TODO: Adapt for negated sentences.
	public boolean evaluate() {
		return this.isFullyMatched();
	}

	public boolean isFullyMatched() {
		boolean containsValidSentence = false;
		boolean containsNullOrInvalidSentence = false;
		Object[] csis = this.childSentenceInstances;
		int count = 0;
		for (int i = 0; i < this.childSentenceInstances.length; i++) {
			if (this.childSentenceInstances[i] != null) {
				count++;
			}
		}
		for (int i = 0; i < this.childSentenceInstances.length; i++) {
			SentenceInstance csi = this.childSentenceInstances[i];
			if (csi != null
					|| isValidatedJavaRelationSentence(i)
					|| isValidatedBindSentence(i)) {
				containsValidSentence = true;
			} else {
				containsNullOrInvalidSentence = true;
				
				// 7/18/2016:  If not "or", cannot be true at this point...
				// (not tested)
				if (!(this.namedSentence.sentence instanceof OrSentence)) {
					break;
				}
			}
		}
		if (!containsNullOrInvalidSentence
				|| (containsValidSentence && this.namedSentence.sentence instanceof OrSentence)) {
			return true;
		}
		return false;
	}

	private boolean isValidatedBindSentence(int cindex) {
		NamedSentence cns = this.namedSentence.childNamedSentences[cindex];
		if (cns.sentence instanceof BindSentence) {
			BindSentence bs = (BindSentence) cns.sentence;
			Variable var = (Variable) bs.getTerm(0);
			int index = var.getContainingKBExpressionIndex();
			Term arg = (Term) bs.getTerm(1);
			Object value = arg.eval();
			this.bindings[index] = value;
			boolean rv = (this.bindings[index] != null);
			return rv;
		}
		return false;
	}

	// 5/20/2015: TODO: Update this for Negation.
	private boolean isValidatedJavaRelationSentence(int cindex) {
		NamedSentence cns = this.namedSentence.childNamedSentences[cindex];
		boolean result = false;
		if (cns.sentence instanceof JavaRelationSentence) {
			try {
				JavaRelationSentence jrs = (JavaRelationSentence) cns.sentence;
				result = jrs.eval();

				// 5/1/2015
				if (this.getNamedSentence().getSentence() instanceof NotSentence) {
					result = !result;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public NamedSentence getNamedSentence() {
		return namedSentence;
	}

	public void setNamedSentence(NamedSentence namedSentence) {
		this.namedSentence = namedSentence;
	}

	public Object[] getBindings() {
		return bindings;
	}

	public SentenceInstance[] getChildSentenceInstances() {
		return childSentenceInstances;
	}

	public void setChildSentenceInstances(
			SentenceInstance[] childSentenceInstances) {
		this.childSentenceInstances = childSentenceInstances;
	}

	public String toString() {
		String bstr = ForwardChainingInferenceEngine
				.getBindingString(this.bindings);
		String buffer = StrUtils.getBlanks(this.getDepth());
		String str = buffer + "<Expansion:NS=" + this.namedSentence + ",Binds="
				+ bstr + ">";
		return str;
	}

	public void createNotSentenceInstance(NamedSentence notns) {
		if (this.isOnlyMissingChildSentence(notns)) {
			NamedSentence ncs = notns.getChildNamedSentences()[0];
			Expression ckbe = this.getNamedSentence().getSentence()
					.getContainingKBExpression();
			Object[] bindings = new Object[ckbe.getVariableCount()];
			for (int i = 0; i < ncs.sentence.getTermCount(); i++) {
				Object o = ncs.sentence.getTerm(i);
				Object value = null;
				if (o instanceof Variable) {
					Variable var = (Variable) o;
					int index = var.getContainingKBExpressionIndex();
					bindings[i] = this.getBindings()[index];
				} else {
					bindings[i] = o;
				}
				if (bindings[i] == null) {
					return;
				}
			}
			SentenceInstance nsi = new SentenceInstance(notns, bindings);
			new SentenceExpansion(this, nsi);
		}
	}

	// 5/1/2015: Indicate whether a particular NS is the only piece missing in
	// an SE. (Seems overly narrow...)
	private boolean isOnlyMissingChildSentence(NamedSentence cns) {
		if (this.getNamedSentence().equals(cns.getParent())) {
			SentenceInstance[] csis = this.getChildSentenceInstances();
			for (int i = 0; i < this.getNamedSentence()
					.getChildNamedSentences().length; i++) {
				if (i != cns.getParentSentenceIndex() && csis == null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

}
