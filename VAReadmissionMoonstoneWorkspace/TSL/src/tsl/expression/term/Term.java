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
package tsl.expression.term;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.function.javafunction.JavaFunctionTerm;
import tsl.expression.term.function.logicfunction.LogicFunctionTerm;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.property.PropertySentence;
import tsl.expression.term.relation.JavaRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.utilities.ListUtils;
import tsl.utilities.VUtils;

public class Term extends Expression {
	protected TypeConstant type = null;
	protected TypeConstant generalType = null;
	private Vector<RelationSentence> subjectSentences = null;
	private Vector<RelationSentence> modifierSentences = null;
	private Vector<PropertySentence> propertySentences = null;
	protected String cui = null;
	protected Object concept = null;

	private Vector<Term> parents = null;
	private Vector<Term> children = null;

	private boolean usedInGrammarRulePattern = false;

	public static int ObjectConstantType = 1;
	public static int TypeConstantType = 2;
	public static int RelationConstantType = 3;
	public static int UndefinedType = 4;

	public Term() {
		super();
	}

	public Term(String name) {
		super(name);
	}

	public Term(Vector pattern) {
		super(pattern);
	}

	public static Term createTerm(Vector v) {
		return Constant.createConstant(v);
	}

	public static List getProofVariables(Vector<Sentence> sentences) {
		List<ProofVariable> pvars = null;
		if (sentences != null) {
			for (Sentence sentence : sentences) {
				pvars = ListUtils.appendIfNot(pvars, sentence.getProofVariables());
			}
		}
		return pvars;
	}

	public Object eval() {
		return this;
	}

	public static Object evalObject(Object o) {
		if (o instanceof Term) {
			return ((Term) o).eval();
		}
		return o;
	}

	public static boolean unpack(Vector terms, Object[] argv) {
		if (terms != null) {
			for (int i = 0; i < terms.size(); i++) {
				Object value = unpack(terms.elementAt(i));
				if (value == null) {
					return false;
				}
				argv[i] = value;
			}
		}
		return true;
	}

	public static Object unpack(Object o) {
		Object value = null;
		value = Term.evalObject(o);
		if (value instanceof ObjectConstant) {
			ObjectConstant jow = (ObjectConstant) value;
			value = jow.getObject();
		}
		return value;
	}

	public static Vector evalObjects(Vector terms) {
		Vector values = null;
		if (terms != null) {
			for (int i = 0; i < terms.size(); i++) {
				Object value = evalObject(terms.elementAt(i));
				if (value == null) {
					return null;
				}
				values = VUtils.add(values, value);
			}
		}
		return values;
	}

	// // 4/5/2014: Contains Klooges...
	public static boolean match(Object term1, List<ProofVariable> pbinds, Object term2, List<ProofVariable> cbinds) {
		Object o = null;
		Object val1 = null;
		Object val2 = null;
		Expression ckbe = null;

		if (term1 instanceof JavaFunctionTerm || term2 instanceof JavaFunctionTerm) {
			Term t1 = (Term) term1;
			Term t2 = (Term) term2;
			ckbe = t1.getContainingKBExpression();
			if (ckbe == null) {
				ckbe = t2.getContainingKBExpression();
			}
			if (ckbe != null) {
				ckbe.setSelectedProofVariableList(pbinds);
			}
		}

		if (!(term1 instanceof Variable)) {
			o = Term.evalObject(term1);
			if (o != null && !(o instanceof Term)) {
				term1 = new ObjectConstant(o);
			}
		}

		if (!(term2 instanceof Variable)) {
			o = Term.evalObject(term2);
			if (o != null && !(o instanceof Term)) {
				term2 = new ObjectConstant(o);
			}
		}

		if (ckbe != null) {
			ckbe.setSelectedProofVariableList(null);
		}

		if (!(term1 instanceof Variable) && !(term2 instanceof Variable)) {
			if (term1 != null && term2 != null) {
				if (term1.equals(term2)) {
					return true;
				}
			}
			if (term1 instanceof LogicFunctionTerm && term2 instanceof LogicFunctionTerm) {
				LogicFunctionTerm lft1 = (LogicFunctionTerm) term1;
				LogicFunctionTerm lft2 = (LogicFunctionTerm) term2;
				return match(lft1, pbinds, lft2, cbinds);
			}
		}

		// Why do I need this?
		if (term1 instanceof ProofVariable && (val1 = ((ProofVariable) term1).eval()) != null) {
			return match(val1, pbinds, term2, cbinds);
		}
		if (term2 instanceof ProofVariable && (val2 = ((ProofVariable) term2).eval()) != null) {
			return match(term1, pbinds, val2, cbinds);
		}

		if (term1 instanceof ProofVariable && term1 != term2) {
			((ProofVariable) term1).bind(term2);
			return true;
		}
		if (term2 instanceof ProofVariable && term1 != term2) {
			((ProofVariable) term2).bind(term1);
			return true;
		}
		return false;
	}

	public Vector<RelationSentence> getMatchingSentences(Vector<RelationSentence> sentences, RelationSentence tomatch) {
		Vector<RelationSentence> matches = null;
		if (tomatch != null && sentences != null) {
			for (RelationSentence rs : sentences) {
				if (rs.getRelation().equals(tomatch.getRelation())) {
					matches = VUtils.add(matches, rs);
				}
			}
		}
		return matches;
	}

	public void addSubjectSentence(RelationSentence rs) {
		if (!(this instanceof Variable)) {
			this.subjectSentences = VUtils.addIfNot(this.subjectSentences, rs);
		}
	}

	public Vector<RelationSentence> getSubjectSentences() {
		return this.subjectSentences;
	}
	
	public void setSubjectSentences(Vector<RelationSentence> sentences) {
		this.subjectSentences = sentences;
	}

	public void setModifierSentences(Vector<RelationSentence> modifierSentences) {
		this.modifierSentences = modifierSentences;
	}

	public void setPropertySentences(Vector<PropertySentence> propertySentences) {
		this.propertySentences = propertySentences;
	}

	// 10/16/2013 (NOT YET TESTED)
	// Given a term, add all its sentential modifiers as attribute assignments
	public void addSubjectSentencesAsAttributes() {
		if (this.subjectSentences != null) {
			for (RelationSentence rs : this.subjectSentences) {
				this.setProperty(rs.getRelation().getName(), rs.getModifier());
			}
		}
	}

	public Vector<RelationSentence> getSubjectSentences(RelationConstant rc) {
		Vector<RelationSentence> sentences = null;
		if (this.subjectSentences != null) {
			for (RelationSentence rs : this.subjectSentences) {
				if (rc.equals(rs.getRelation())) {
					sentences = VUtils.add(sentences, rs);
				}
			}
		}
		return sentences;
	}

	public void removeSubjectSentence(RelationSentence rs) {
		this.subjectSentences = VUtils.remove(this.subjectSentences, rs);
	}

	public void addModifierSentence(RelationSentence rs) {
		if (!(this instanceof Variable)) {
			this.modifierSentences = VUtils.add(this.modifierSentences, rs);
		}
	}

	public Vector<RelationSentence> getModifierSentences() {
		return this.modifierSentences;
	}

	public Vector<RelationSentence> getModifierSentences(RelationConstant rc) {
		Vector<RelationSentence> sentences = null;
		if (this.modifierSentences != null) {
			for (RelationSentence rs : this.modifierSentences) {
				if (rc.equals(rs.getRelation())) {
					sentences = VUtils.add(sentences, rs);
				}
			}
		}
		return sentences;
	}

	public void removeModifierSentence(RelationSentence rs) {
		this.modifierSentences = VUtils.remove(this.modifierSentences, rs);
	}

	public void addPropertySentence(PropertySentence ps) {
		this.propertySentences = VUtils.add(this.propertySentences, ps);
	}

	public void addPropertySentence(String property, Object value) {
		PropertyConstant pc = PropertyConstant.createPropertyConstant(property, this.getType(), value);
		PropertySentence ps = PropertySentence.createPropertySentence(pc, this, value);
		if (ps != null) {
			this.propertySentences = VUtils.add(this.propertySentences, ps);
		}
	}

	public Vector<PropertySentence> getPropertySentences() {
		return propertySentences;
	}

	public Vector<PropertySentence> getPropertySentences(String property) {
		Vector<PropertySentence> matches = null;
		if (this.propertySentences != null) {
			for (PropertySentence ps : this.propertySentences) {
				if (property.equals(ps.getRelation().getName())) {
					matches = VUtils.add(matches, ps);
				}
			}
		}
		return matches;
	}

	public TypeConstant getType() {
		return this.type;
	}

	public void setType(TypeConstant type) {
		this.type = type;
	}

	public boolean hasTypeName(String tname) {
		return tname != null && this.type != null && this.type.getName().contains(tname);
	}

	// 6/26/2013: Get either slot value, inherited slot value, or modifier from
	// a
	// relation sentence.
	public Object getRelatum(RelationConstant rc) {
		return getRelatum(rc.getName());
	}

	public Object getRelatum(String rname) {
		Object relatum = this.getSlotValue(rname);
		if (relatum == null && this.subjectSentences != null) {
			for (RelationSentence rs : this.subjectSentences) {
				if (rname.equals(rs.getRelation().getName())) {
					relatum = rs.getModifier();
					break;
				}
			}
		}
		return relatum;
	}

	public Object getHeritableProperty(String property) {
		Object value = this.getProperty(property);
		if (value instanceof PropertySentence) {
			PropertySentence ps = (PropertySentence) value;
			value = ps.getRangeValue();
		}
		if (value == null && this.getType() != null) {
			value = this.getType().getHeritableDefaultProperty(property);
		}
		return value;
	}

	public Object getSlotValue(String property) {
		return this.getHeritableProperty(property);
	}

	public void setSlotValue(String property, Object value) {
		// Before 6/26/2013 -- This is the formal way to do it, but for now, I
		// am
		// just using the properties hashtable.
		// KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		// NameSpace ns = kb.getNameSpace();
		// PropertyConstant pc = ns.getPropertyConstant(property);
		// PropertySentence ps = PropertySentence.createPropertySentence(pc,
		// this.getType(), value);
		// this.setProperty(property, ps);
		if (property != null && value != null) {
			this.setProperty(property, value);
		}
	}

	public static class LabelSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Term t1 = (Term) o1;
			Term t2 = (Term) o2;
			return t1.getLabel().compareTo(t2.getLabel());
		}
	}

	// 7/19/2013
	public String toString() {
		if (this.getName() != null) {
			return this.getName();
		}
		return "*";
	}

	// 11/3/2013
	public int getOntologicalType() {
		if (this instanceof ObjectConstant) {
			return ObjectConstantType;
		}
		if (this instanceof TypeConstant) {
			return TypeConstantType;
		}
		if (this instanceof RelationConstant) {
			return RelationConstantType;
		}
		return UndefinedType;
	}

	public String getCui() {
		return getCUI();
	}

	public String getCUI() {
		return this.cui;
	}

	public boolean hasCui() {
		return this.cui != null;
	}

	public Object getConcept() {
		return this.concept;
	}

	public void setCui(String cui) {
		this.cui = cui;
		this.setProperty("cui", cui);
	}

	public void setConcept(Object concept) {
		this.concept = concept;
		this.setProperty("concept", concept);
	}

	public boolean hasConcept() {
		return this.concept != null;
	}

	// 3/30/2014: Gathers a set of sentences that are "about" a particular term.
	public Vector<RelationSentence> gatherRelatedSentences() {
		Vector<RelationSentence> sents = null;
		if (!this.isVisited()) {
			this.setVisited(true);
			if (this.getSubjectSentences() != null) {
				sents = new Vector(this.getSubjectSentences());
				for (RelationSentence rs : sents) {
					Vector<RelationSentence> msents = rs.getModifier().gatherRelatedSentences();
					sents = VUtils.append(sents, msents);
				}
			}
			this.setVisited(false);
		}
		return sents;
	}

	public Vector<Expression> gatherRelatedExpressions() {
		Vector<Expression> expressions = null;
		Vector<RelationSentence> sentences = this.gatherRelatedSentences();
		if (sentences != null) {
			for (RelationSentence rs : sentences) {
				expressions = VUtils.addIfNot(expressions, rs.getRelation());
				expressions = VUtils.addIfNot(expressions, rs.getSubject());
				expressions = VUtils.addIfNot(expressions, rs.getModifier());
			}
		}
		return expressions;
	}

	public Vector<String> getRelatedTermStringVector() {
		Vector<String> names = null;
		Vector<Expression> expressions = this.gatherRelatedExpressions();
		if (expressions != null) {
			names = Expression.getNames(expressions);
			Collections.sort(names);
		}
		return names;
	}

	public static Term wrapTerm(Object o) {
		if (o != null) {
			if (o instanceof Term) {
				return (Term) o;
			} else {
				return new ObjectConstant(o);
			}
		}
		return null;
	}

	public static Vector<Term> wrapTerms(Vector v) {
		Vector<Term> terms = null;
		for (Object o : v) {
			Term t = wrapTerm(o);
			terms = VUtils.add(terms, t);
		}
		return terms;
	}

	// 7/1/2016: To accomodate OWL constants which can have parents/children

	public Vector<Term> getParents() {
		return parents;
	}

	public void setParents(Vector<Term> parents) {
		this.parents = parents;
	}

	public void addParent(Term parent) {
		this.parents = VUtils.addIfNot(this.parents, parent);
		if (parent != null) {
			parent.children = VUtils.addIfNot(parent.children, this);
		}
	}

	public Vector<Term> getChildren() {
		return children;
	}

	public void setChildren(Vector<Term> children) {
		this.children = children;
	}

	public void addChild(Term child) {
		this.children = VUtils.addIfNot(this.children, child);
	}

	public boolean isUsedInGrammarRulePattern() {
		return usedInGrammarRulePattern;
	}

	public void setUsedInGrammarRulePattern(boolean usedInGrammarRulePattern) {
		this.usedInGrammarRulePattern = usedInGrammarRulePattern;
	}

}
