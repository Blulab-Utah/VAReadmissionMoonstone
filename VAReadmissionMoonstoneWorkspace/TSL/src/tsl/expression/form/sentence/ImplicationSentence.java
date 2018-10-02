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
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.Symbol;
import tsl.tsllisp.TLUtils;
import tsl.utilities.VUtils;

public class ImplicationSentence extends Sentence {
	protected Sentence antecedent = null;
	protected Sentence consequent = null;
	private Object containingObject = null;

	public ImplicationSentence() {
	}

	public ImplicationSentence(String name) {
		super(name);
	}

	public ImplicationSentence(Vector v) {
		super();
		if (v.size() == 3) {
			this.antecedent = Sentence.createSentence((Vector) v.elementAt(1));
			this.consequent = RelationSentence
					.createJavaRelationSentence((Vector) v.elementAt(2));
		}
	}

	public static ImplicationSentence createImplicationSentence(Vector v) {
		if (v != null && "->".equals(v.firstElement())) {
			return new ImplicationSentence(v);
		}
		return null;
	}
	
	public static boolean isImpllicationSentence(Sexp sexp) {
		return (sexp.getLength() == 3
				&& TLUtils.isSymbol(sexp.getFirst())
				&& "->".equals(((Symbol) sexp.getFirst()).getName())
				&& TLUtils.isCons(sexp.getSecond())
				&& TLUtils.isCons(sexp.getThird()));
	}

	// 9/14/2013
	public void updateSentenceVariables(KnowledgeBase kb) {
		this.setKnowledgeBase(kb);
		if (this.antecedent != null) {
			this.antecedent.updateSentenceVariables(kb);
		}
		if (this.consequent != null) {
			this.consequent.updateSentenceVariables(kb);
		}
	}

	public Expression copy() {
		ImplicationSentence is = new ImplicationSentence();
		if (this.antecedent != null) {
			is.setAntecedent((Sentence) this.antecedent.copy());
		}
		if (this.consequent != null) {
			is.setConsequent((RelationSentence) this.consequent.copy());
		}
		return is;
	}

	public Vector gatherRelationalSentences() {
		Vector v = null;
		if (this.antecedent != null) {
			v = VUtils
					.appendNew(v, this.antecedent.gatherRelationalSentences());
		}
		if (this.consequent != null) {
			v = VUtils
					.appendNew(v, this.consequent.gatherRelationalSentences());
		}
		return v;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		this.setKnowledgeBase(kb);
		if (this.antecedent != null) {
			this.antecedent.assignContainingKBExpression(kb,
					containingKBExpression);
		}
		if (this.consequent != null) {
			this.consequent.assignContainingKBExpression(kb,
					containingKBExpression);
		}
	}

	public String validate() {
		String msg = null;
		if (this.getAntecedent() == null) {
			return "ImplicationSentence " + this.getStringID()
					+ " must have antecedent sentence.";
		}
		msg = this.getAntecedent().validate();
		if (msg != null) {
			return msg;
		}
		if (this.getConsequent() == null) {
			return "ImplicationSentence " + this.getStringID()
					+ " must have consequent sentence.";
		}
		msg = this.getConsequent().validate();
		if (msg != null) {
			return msg;
		}
		if (!(this.getConsequent() instanceof RelationSentence)) {
			return "The consequent of ImplicationSentence " + this.getStringID()
					+ " must be a RelationSentence.";
		}
		return null;
	}

	public Sentence getAntecedent() {
		return antecedent;
	}
	
	public Sentence getConsequent() {
		return consequent;
	}
	
	public RelationSentence getConsequentRelationSentence() {
		return (RelationSentence) this.consequent;
	}

	// Before 1/29/2016
//	public RelationSentence getConsequent() {
//		return (RelationSentence) consequent;
//	}

	public void setAntecedent(Sentence antecedent) {
		this.antecedent = antecedent;
	}

	public void setConsequent(Sentence consequent) {
		this.consequent = consequent;
	}

	public RelationSentence getHead() {
		return (RelationSentence) this.getConsequent();
	}
	
	// Before 1/29/2016
//	public RelationSentence getHead() {
//		return this.getConsequent();
//	}

	public String toString() {
		String str = "(-> ";
		if (this.antecedent != null) {
			str += this.antecedent;
		}
		if (this.consequent != null) {
			str += " " + this.consequent;
		}
		str += ")";
		return str;
	}

	public String toLisp() {
		String str = "(-> ";
		if (this.antecedent != null) {
			str += this.antecedent.toLisp();
		}
		if (this.consequent != null) {
			str += " " + this.consequent.toLisp();
		}
		str += ")";
		return str;
	}

	public String toShortString() {
		String str = "(-> ";
		if (this.antecedent != null) {
			str += this.antecedent.getStringID();
		}
		if (this.consequent != null) {
			str += " " + this.consequent.getStringID();
		}
		str += ")";
		return str;
	}
	
	public Object getContainingObject() {
		return containingObject;
	}

	public void setContainingObject(Object containingObject) {
		this.containingObject = containingObject;
	}
	
//	public void portKB(KnowledgeBase kb) {
//		this.antecedent.portKB(kb);
//		this.consequent.portKB(kb);
//	}

}
