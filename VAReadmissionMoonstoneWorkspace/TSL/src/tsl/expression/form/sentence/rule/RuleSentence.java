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
package tsl.expression.form.sentence.rule;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

/*
 * (defrule ili
 (antecedent (and (has-symptom ?p fever)
 (> (fever-of ?p) 102)
 (or (has-symptom ?p sore-throat)
 (has-symptom ?p cough))))
 (consequent (has-possible-diagnosis ?p ILI)))
 */

public class RuleSentence extends ImplicationSentence {

	public RuleSentence() {

	}

	public RuleSentence(String name) {
		super(name);
	}

	public RuleSentence(Vector v) {
		if (v.size() > 1) {
			this.name = (String) v.elementAt(1);
		}
		Vector sv = VUtils.assoc("antecedent", v);
		if (sv != null) {
			this.setAntecedent(Sentence.createSentence((Vector) sv.elementAt(1)));
		}
		sv = VUtils.assoc("consequent", v);
		if (sv != null) {
			this.setConsequent(RelationSentence
					.createJavaRelationSentence((Vector) sv.elementAt(1)));
		}
	}

	public static RuleSentence createRule(Vector v) {
		if (v != null && "defrule".equals(v.firstElement())) {
			return new RuleSentence(v);
		}
		return null;
	}

	public String validate() {
		String msg = null;
		if (this.getName() == null || !Character.isLetter(this.getName().charAt(0))) {
			return "RuleSentence must have valid name, beginning with letter";
		}
		if (this.getAntecedent() == null) {
			return "RuleSentence " + this.getStringID()
					+ " must have antecedent sentence.";
		}
		msg = this.getAntecedent().validate();
		if (msg != null) {
			return msg;
		}
		if (this.getConsequent() == null) {
			return "RuleSentence " + this.getStringID()
					+ " must have consequent sentence.";
		}
		msg = this.getConsequent().validate();
		if (msg != null) {
			return msg;
		}
		if (!(this.getConsequent() instanceof RelationSentence)) {
			return "The consequent of RuleSentence " + this.getStringID()
					+ " must be a RelationSentence.";
		}
		return null;
	}
	
	public String toString() {
		String str = "(defrule ";
		String nstr = "<" + (this.getName() != null ? this.getName() : "*") + ">";
		str += " " + nstr + " ";
		if (this.getAntecedent() != null) {
			str += "(antecedent " + this.getAntecedent().toString() + ") ";
		}
		if (this.getConsequent() != null) {
			str += "(consequent " + this.getConsequent().toString() + ") ";
		}
		str += ")";
		return str;
	}

	public String toLisp() {
		String str = "(defrule ";
		String nstr = "<" + (this.getName() != null ? this.getName() : "*") + ">";
		str += " " + nstr + " ";
		if (this.getAntecedent() != null) {
			str += "(antecedent " + this.getAntecedent().toLisp() + ") ";
		}
		if (this.getConsequent() != null) {
			str += "(consequent " + this.getConsequent().toLisp() + ") ";
		}
		str += ")";
		return str;
	}

	public String toShortString() {
		String str = "(defrule ";
		String nstr = "<" + (this.getName() != null ? this.getName() : "*") + ">";
		str += " " + nstr + " ";
		if (this.getAntecedent() != null) {
			str += "(antecedent " + this.getAntecedent().getStringID() + ") ";
		}
		if (this.getConsequent() != null) {
			str += "(consequent " + this.getConsequent().getStringID() + ") ";
		}
		str += ")";
		return str;
	}

}
