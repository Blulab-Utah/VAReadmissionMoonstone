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

import tsl.expression.form.sentence.ComplexSentence;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.utilities.VUtils;

public class NamedSentence {
	protected ForwardChainingInferenceEngine inferenceEngine = null;
	protected Sentence sentence = null;
	protected NamedSentence parent = null;
	protected int parentSentenceIndex = 0;
	protected NamedSentence[] childNamedSentences = null;
	protected String name = null;
	protected RelationSentence consequentSentence = null;

	public NamedSentence(ForwardChainingInferenceEngine fc, Sentence sentence,
			NamedSentence parent, String name, int pindex) {
		this.inferenceEngine = fc;
		this.sentence = sentence;
		this.parent = parent;
		this.parentSentenceIndex = pindex;
		this.name = name;
		if (sentence.isComplex()) {
			ComplexSentence cs = (ComplexSentence) sentence;
			this.childNamedSentences = new NamedSentence[cs.getSentences()
					.size()];
			for (int i = 0; i < cs.getSentences().size(); i++) {
				Sentence csent = (Sentence) cs.getSentences().elementAt(i);
				String cname = name + ":" + i;
				NamedSentence cns = fc.createNamedSentence(csent, this, cname,
						i);
				this.childNamedSentences[i] = cns;
			}
		} else if (sentence instanceof NotSentence) {
			this.childNamedSentences = new NamedSentence[1];
			Sentence csent = ((NotSentence) sentence).getSentence();
			NamedSentence cns = fc.createNamedSentence(csent, this,
					name + ":0", 0);
			this.childNamedSentences[0] = cns;
		} else if (sentence instanceof ImplicationSentence) {
			this.childNamedSentences = new NamedSentence[1];
			ImplicationSentence is = (ImplicationSentence) sentence;
			NamedSentence ans = fc.createNamedSentence(is.getAntecedent(),
					this, name + ":0", 0);
			this.childNamedSentences[0] = ans;
			this.consequentSentence = (RelationSentence) is.getConsequent();
		}
	}

	public boolean equals(Object o) {
		if (o instanceof NamedSentence) {
			NamedSentence ns = (NamedSentence) o;
			if (this.getName().equals(ns.getName())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public String getName() {
		return name;
	}

	public int getParentSentenceIndex() {
		return parentSentenceIndex;
	}

	public NamedSentence getParent() {
		return this.parent;
	}

	public void setParentSentenceIndex(int parentSentenceIndex) {
		this.parentSentenceIndex = parentSentenceIndex;
	}
	
	public String toString() {
		String str = "<NS:Name=\"" + this.name + "\",Sentence=" + this.sentence.toShortString()
				+ ">";
		return str;
	}

	public void addSentenceInstance(SentenceInstance si) {
		si.namedSentence = this;
		VUtils.pushHashVector(si.namedSentence.getInferenceEngine()
				.getNamedSentenceInstanceTable(), this, si);
	}

	public NamedSentence[] getChildNamedSentences() {
		return childNamedSentences;
	}

	public ForwardChainingInferenceEngine getInferenceEngine() {
		return inferenceEngine;
	}
	

}
