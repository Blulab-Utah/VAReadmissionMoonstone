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

import java.util.Vector;

import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.JavaRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class SentenceInstance extends SentenceObject {

	protected RelationSentence derivedFrom = null;

	// 4/30/2015: What to do about child sentence instances?
	public SentenceInstance(NamedSentence ns, Object[] bindings) {
		ns.addSentenceInstance(this);
		this.bindings = bindings;
	}

	public SentenceInstance(SentenceExpansion se) {
		this.setDepth(se.getDepth() + 1);
		se.namedSentence.addSentenceInstance(this);
		int blen = se.bindings.length;
		this.bindings = new Object[se.bindings.length];
		for (int i = 0; i < bindings.length; i++) {
			this.bindings[i] = se.bindings[i];
		}
		this.childSentenceInstances = new SentenceInstance[se.childSentenceInstances.length];
		for (int i = 0; i < se.childSentenceInstances.length; i++) {
			this.childSentenceInstances[i] = se.childSentenceInstances[i];
		}
		process();
	}

	public SentenceInstance(RelationSentence rs, NamedSentence rns) {
		try {
			this.derivedFrom = rs;
			rns.addSentenceInstance(this);
			RelationSentence nrs = (RelationSentence) rns.sentence;
			Sentence kbsent = (Sentence) nrs.getContainingKBExpression();
			this.bindings = new Object[kbsent.getVariableCount()];
			for (int i = 0; i < nrs.getTermCount(); i++) {
				Term nsterm = (Term) nrs.getTerm(i);
				Term rsterm = (Term) rs.getTerm(i);
				if (nsterm instanceof Variable) {
					Variable nsvar = (Variable) nsterm;
					int index = nsvar.getContainingKBExpressionIndex();
					this.bindings[index] = rsterm;
				}
			}
			process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void process() {
		if (this.namedSentence.inferenceEngine.isDoDebug()) {
			System.out.println(this.toString());
		}
		if (this.namedSentence.consequentSentence instanceof RelationSentence) {
			RelationSentence crs = this.namedSentence.consequentSentence;
			// 5/3/2015: Execute action. (Not yet tested.)
			if (crs instanceof JavaRelationSentence) {
				JavaRelationSentence jrs = (JavaRelationSentence) crs;
				jrs.eval();
			} else {
				Vector<Term> terms = null;
				for (int i = 0; i < crs.getTermCount(); i++) {
					Term term = (Term) crs.getTerm(i);
					if (term instanceof Variable) {
						Variable var = (Variable) term;
						int index = var.getContainingKBExpressionIndex();
						Object value = this.bindings[index];
						term = Term.wrapTerm(value);
					}
					terms = VUtils.add(terms, term);
				}
				RelationSentence newrs = new RelationSentence();
				newrs.setRelation(crs.getRelation());
				newrs.setTerms(terms);
				newrs.setDerivedFrom(this);
				this.namedSentence.getInferenceEngine().addInferredSentence(newrs);
			}
		} else if (this.doPermitExpansion()) {
			if (this.namedSentence.parent != null) {
				new SentenceExpansion(this);
			}
			Vector<SentenceExpansion> ses = this.namedSentence.getInferenceEngine().getSentenceExpansionTable()
					.get(this.namedSentence);
			if (ses != null) {
				for (SentenceExpansion se : ses) {
					SentenceExpansion.createSentenceExpansion(se, this);
				}
			}
		}
	}

	private boolean doPermitExpansion() {
		if (this.getNamedSentence().getSentence() instanceof NotSentence
				&& !this.getNamedSentence().getInferenceEngine().isExpandNegationSentences()) {
			return false;
		}
		return true;
	}

	public String toString() {
		String buffer = StrUtils.getBlanks(this.getDepth());
		String bstr = ForwardChainingInferenceEngine.getBindingString(this.bindings);
		String str = buffer + "<SentenceInstance:NS=" + this.namedSentence + ",Binds=" + bstr + ">";
		return str;
	}

	public NamedSentence getNamedSentence() {
		return namedSentence;
	}

	public Object[] getBindings() {
		return bindings;
	}

	public SentenceInstance[] getChildSentenceInstances() {
		return childSentenceInstances;
	}

	public RelationSentence getDerivedFrom() {
		return derivedFrom;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

}
