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
package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class ComplexSentence extends Sentence {
	
	protected Vector<Sentence> sentences = null;
	
	public ComplexSentence() {
		super();
	}
	
	public ComplexSentence(Vector v) {
		super();
		this.sentences = null;
		for (int i = 1; i <= v.size()-1; i++) {
			Vector sv = (Vector) v.elementAt(i);
			Sentence s = Sentence.createSentence(sv);
			this.sentences = VUtils.add(this.sentences, s);
		}
	}

	public void updateSentenceVariables(KnowledgeBase kb) {
		this.setKnowledgeBase(kb);
		if (this.getSentences() != null) {
			for (Sentence child : this.getSentences()) {
				child.updateSentenceVariables(kb);
			}
		}
	}
	
	public Vector gatherRelationalSentences() {
		Vector<RelationSentence> relsents = null;
		if (this.sentences != null) {
			for (Sentence subsent : this.sentences) {
				relsents = VUtils.append(relsents,
						subsent.gatherRelationalSentences());
			}
		}
		return relsents;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		if (this.sentences != null) {
			for (Sentence sentence : this.sentences) {
				sentence.assignContainingKBExpression(kb,
						containingKBExpression);
			}
		}
	}
	
	public String validate() {
		if (this.getSentences() == null || this.getSentences().size() < 2) {
			return "ComplexSentence " + this.getStringID() + " must have 2+ subsentences.";
		}
		for (Sentence s : this.getSentences()) {
			String msg = s.validate();
			if (msg != null) {
				return msg;
			}
		}
		return null;
	}
	
	// 10/16/2013
	public void setVariable(Variable v) {
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				s.setVariable(v);
			}
		}
	}

	public Vector<Sentence> getSentences() {
		return this.sentences;
	}
	
	public void setSentences(Vector<Sentence> sentences) {
		this.sentences = sentences;
	}

	public void addSentence(Sentence s) {
		this.sentences = VUtils.add(this.sentences, s);
	}
	
	public Vector<TypeConstant> gatherTypes() {
		Vector<TypeConstant> types = null;
		if (this.getSentences() != null) {
			for (Sentence s : this.getSentences()) {
				types = VUtils.append(types, s.gatherSupportingTypes());
			}
		}
		return types;
	}
	
//	public void portKB(KnowledgeBase kb) {
//		this.setKnowledgeBase(kb);
//		for (Sentence s : this.getSentences()) {
//			s.portKB(kb);
//		}
//	}

}
