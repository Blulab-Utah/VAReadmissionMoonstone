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

public class NotSentence extends Sentence {
	private Sentence sentence = null;

	public NotSentence() {
		super();
	}

	public NotSentence(Sentence s) {
		super();
		this.sentence = s;
	}

	public NotSentence(Vector v) {
		super();
		if (v != null && v.size() == 2) {
			Vector sv = (Vector) v.elementAt(1);
			this.sentence = Sentence.createSentence(sv);
		}
	}

	public static NotSentence createNotSentence(Vector v) {
		if (isNotSentence(v)) {
			return new NotSentence(v);
		}
		return null;
	}

	public static boolean isNotSentence(Vector v) {
		return (v != null && "not".equals(v.firstElement()));
	}

	// 9/14/2013
	public void updateSentenceVariables(KnowledgeBase kb) {
		this.setKnowledgeBase(kb);
		if (this.sentence != null) {
			this.sentence.updateSentenceVariables(kb);
		}
	}

	public Expression copy() {
		NotSentence ns = new NotSentence();
		ns.sentence = (Sentence) this.sentence.copy();
		return ns;
	}

	public Vector gatherRelationalSentences() {
		Vector<RelationSentence> relsents = null;
		relsents = VUtils.append(relsents,
				this.sentence.gatherRelationalSentences());
		return relsents;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		if (this.sentence != null) {
			this.sentence.assignContainingKBExpression(kb,
					containingKBExpression);
		}
	}

	public String validate() {
		if (this.getSentence() == null) {
			return "NotSentence " + this.getStringID()
					+ " must have a nested sentence.";
		}
		return this.getSentence().validate();
	}

	public void setVariable(Variable v) {
		if (this.sentence != null) {
			this.sentence.setVariable(v);
		}
	}

	public Sentence getSentence() {
		return this.sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public String toString() {
		String str = "(not ";
		if (this.sentence != null) {
			str += this.sentence.toString();
		}
		str += ")";
		return str;
	}

	public String toLisp() {
		String str = "(not ";
		if (this.sentence != null) {
			str += this.sentence.toLisp();
		}
		str += ")";
		return str;
	}

	public String toShortString() {
		String str = "(not ";
		if (this.sentence != null) {
			str += this.sentence.getStringID();
		}
		str += ")";
		return str;
	}

	// 10/2/2013
	public Vector<TypeConstant> gatherTypes() {
		Vector<TypeConstant> types = null;
		if (this.getSentence() != null) {
			types = VUtils.append(types, this.getSentence()
					.gatherSupportingTypes());
		}
		return types;
	}
	
	public boolean isLiteral() {
		return this.getSentence().isAtom();
	}

//	public void portKB(KnowledgeBase kb) {
//		this.setKnowledgeBase(kb);
//		this.sentence.portKB(kb);
//	}

}
