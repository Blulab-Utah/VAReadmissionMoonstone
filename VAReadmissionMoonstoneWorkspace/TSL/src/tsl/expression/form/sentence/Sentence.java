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

import java.util.Comparator;
import java.util.Vector;

import tsl.expression.form.Form;
import tsl.expression.form.sentence.rule.RuleSentence;
import tsl.expression.term.SituationTerm;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypePredicate;
import tsl.expression.term.variable.Variable;
import tsl.inference.forwardchaining.SentenceInstance;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.planner.HTNMethod;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

public abstract class Sentence extends Form {
	private Vector<Sentence> supportedBy = null;
	private Vector<Sentence> supports = null;
	private SituationTerm situation = null;
	private Sentence precondition = null;
	private SentenceInstance derivedFrom = null;

	public Sentence() {
		super();
	}

	public Sentence(String name) {
		super(name);
	}

	public Sentence(Vector source) {
		super();
	}

	public static Sentence createSentence(String str) throws Exception {
		TLisp tlisp = TLisp.getTLisp();
		if (str.charAt(0) != '\'') {
			str = "'" + str;
		}
		Sexp sexp = (Sexp) tlisp.evalString(str);
		Vector v = TLUtils.convertSexpToJVector(sexp);
		Sentence s = createSentence(v);
		s.setSexp(sexp);
		return s;
	}

	public static Sentence createSentence(Vector v) {
		Sentence s = null;
		if ((s = AndSentence.createAndSentence(v)) != null || (s = OrSentence.createOrSentence(v)) != null
				|| (s = NotSentence.createNotSentence(v)) != null
				|| (s = QuantifierSentence.createQuantifierSentence(v)) != null
				|| (s = BindSentence.createBindSentence(v)) != null || (s = HTNMethod.createHTNMethod(v)) != null
				|| (s = RuleSentence.createRule(v)) != null || (s = TypePredicate.createTypePredicate(v)) != null
				|| (s = ImplicationSentence.createImplicationSentence(v)) != null
				|| (s = BiconditionalSentence.createBiconditionalSentence(v)) != null
				|| (s = RelationSentence.createJavaRelationSentence(v)) != null
				|| (s = RelationSentence.createRelationSentence((KnowledgeBase) null, v)) != null) {
			return s;
		}
		return null;
	}

	public RelationSentence getHead() {
		return null;
	}

	// 9/14/2013
	public void updateSentenceVariables(KnowledgeBase kb) {
		this.setKnowledgeBase(kb);
	}

	public static class ChildSentenceNumberSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			if (o1 instanceof RelationSentence) {
				return -1;
			}
			if (o2 instanceof RelationSentence) {
				return 1;
			}
			if (o1 instanceof ImplicationSentence) {
				return -1;
			}
			return 1;
		}
	}

	public Vector<RelationSentence> gatherRelationalSentences() {
		return null;
	}

	public Vector<RelationConstant> gatherRelationConstants() {
		Vector<RelationConstant> relconsts = null;
		Vector<RelationSentence> relsents = this.gatherRelationalSentences();
		if (relsents != null) {
			for (RelationSentence rs : relsents) {
				relconsts = VUtils.addIfNot(relconsts, rs.getRelation());
			}
		}
		return relconsts;
	}

	// 10/25/2013
	public String validate() {
		return null;
	}

	// 10/16/2013
	public void setVariable(Variable v) {

	}

	public Vector<Sentence> getSupportedBy() {
		return supportedBy;
	}

	public void addSupportedBy(Sentence s) {
		this.supportedBy = VUtils.add(this.supportedBy, s);
	}

	public Vector<Sentence> getSupports() {
		return supports;
	}

	public void setSupports(Vector<Sentence> supports) {
		this.supports = supports;
	}

	public SituationTerm getSituation() {
		if (situation == null) {
			situation = new SituationTerm(this);
		}
		return this.situation;
	}

	public void setSituation(SituationTerm situation) {
		this.situation = situation;
	}

	public Term getSentenceTerm(String cname) {
		if ("*situation*".equals(cname)) {
			return this.situation;
		}
		return null;
	}

	public String toString() {
		return "<SENTENCE>";
	}

	public String toLisp() {
		return this.toString();
	}

	public String toShortString() {
		return this.getStringID();
	}

	public String toNewlinedString() {
		if (this.getSexp() != null) {
			return this.getSexp().toNewlinedString(0);
		}
		return "<SENTENCE>";
	}

	public Sentence getPrecondition() {
		return precondition;
	}

	public void setPrecondition(Sentence precondition) {
		this.precondition = precondition;
	}

	public boolean isComplex() {
		return this instanceof ComplexSentence;
	}

	public boolean isAtom() {
		return this instanceof RelationSentence;
	}

	public boolean isLiteral() {
		return this instanceof RelationSentence || (this instanceof NotSentence && ((NotSentence) this).isLiteral());
	}

	public boolean isDerived() {
		return this.derivedFrom != null;
	}

	public SentenceInstance getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(SentenceInstance derivedFrom) {
		this.derivedFrom = derivedFrom;
	}

}
