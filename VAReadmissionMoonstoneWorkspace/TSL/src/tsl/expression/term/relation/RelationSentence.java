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
package tsl.expression.term.relation;

import java.util.Comparator;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class RelationSentence extends Sentence {

	public RelationConstant relation = null; // For VUtils.gatherFields() ---
												// Need to replace that...
												// private JavaRelationConstant
												// javaRelationConstant = null;
	// private boolean failedJavaInvocation = false;
	private Vector validBindingStack = null;
	private TypeRelationSentence typeRelationSentence = null;
	private Term subject = null;
	private Term modifier = null;
	private int arity = 2;
	private Object anchor = null;

	public RelationSentence() {
		super();
	}

	public RelationSentence(RelationConstant rc) {
		super();
		this.setRelation(rc);
	}

	public RelationSentence(String rname) {
		super();
		RelationConstant rc = RelationConstant.createRelationConstant(rname);
		this.setRelation(rc);
	}

	// 1/28/2014: I need Expression constructors that take Sexp's as
	// arguments!!!
	public RelationSentence(Vector v) {
		super();
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		Object ro = v.firstElement();
		RelationConstant rc = null;

		if (ro instanceof RelationConstant) {
			rc = (RelationConstant) ro;
		} else if (ro instanceof String) {
			rc = RelationConstant.createRelationConstant((String) ro);
		}
		this.setRelation(rc);

		for (int i = 1; i < v.size(); i++) {
			Object o = v.elementAt(i);
			Object t = kb.getTerm(this, o);
			this.addTerm(t);
		}
		if (this.getTermCount() >= 1) {
			this.subject = (Term) this.getTerm(0);
			this.subject.addSubjectSentence(this);
		}
		if (this.getTermCount() >= 2) {
			this.modifier = (Term) (this.getTerm(1));
			this.modifier.addModifierSentence(this);
		}
		rc.setArity(v.size() - 1);
		kb.addRelationSentenceList(this);
	}

	public RelationSentence(RelationConstant rc, Term subject, Term modifier) {
		this(rc);
		this.addTerm(subject);
		this.addTerm(modifier);
		this.subject = subject;
		this.modifier = modifier;
		subject.addSubjectSentence(this);
		modifier.addModifierSentence(this);
		this.getRelation().setArity(2);
	}

	public RelationSentence(String rname, Term subject, Term modifier) {
		this(RelationConstant.createRelationConstant(rname), subject, modifier);
	}

	public RelationSentence(String rname, Term subject) {
		this(RelationConstant.createRelationConstant(rname), subject);
	}

	public RelationSentence(RelationConstant rc, Term subject) {
		this(rc);
		this.addTerm(subject);
		this.subject = subject;
		subject.addSubjectSentence(this);
		rc.setArity(1);
	}

	public RelationSentence(RelationConstant rc, Vector<Term> terms) {
		super();
		this.relation = rc;
		this.setTerms(new Vector(terms));
		this.subject = (Term) terms.elementAt(0);
		this.subject.addSubjectSentence(this);
		if (terms.size() == 2) {
			this.modifier = (Term) terms.elementAt(1);
			this.modifier.addModifierSentence(this);
		}
	}

	public RelationSentence(String rname, Vector<Term> terms) {
		this(RelationConstant.createRelationConstant(rname), terms);
	}

	// Need to add constraint test...
	public static RelationSentence createJavaRelationSentence(Vector v) {
		RelationSentence rs = JavaRelationSentence
				.createJavaRelationSentence(v);
		if (rs == null) {
			if (v != null && v.size() >= 1 && v.elementAt(0) instanceof String) {
				rs = new RelationSentence(v);
			}
		}
		return rs;
	}

	// I pass in a KB rather than using the currentKB because the sentence might
	// belong to
	// no KB at all; e.g. a query sentence.
	public static RelationSentence createRelationSentence(KnowledgeBase kb,
			Vector v) {
		RelationSentence rs = null;
		if (v != null) {
			RelationConstant rc = null;
			if (v.firstElement() instanceof String) {
				String rname = (String) v.firstElement();
				if (!isValidRelationName(rname)) {
					return null;
				}
				rc = RelationConstant.createRelationConstant(rname);
			} else if (v.firstElement() instanceof RelationConstant) {
				rc = (RelationConstant) v.firstElement();
			}
			rs = new RelationSentence(rc);
			for (int i = 1; i < v.size(); i++) {
				Object arg = v.elementAt(i);
				if (kb != null) {
					arg = kb.getTerm(rs, arg);
				} else if (!(arg instanceof Term)) {
					arg = new ObjectConstant(arg);
				}
				rs.addTerm(arg);
			}
			rc.setArity(v.size() - 1);
		}
		return rs;
	}

	public static RelationSentence createRelationSentence(RelationConstant rc,
			Term subject, Term modifier) {
		if (rc != null && rc.testAssignments(subject, modifier)) {
			RelationSentence rs = new RelationSentence(rc, subject, modifier);
			return rs;
		}
		return null;
	}

	public static RelationSentence createRelationSentence(String rname,
			Term subject, Term modifier) {
		RelationConstant rc = RelationConstant.createRelationConstant(rname);
		return createRelationSentence(rc, subject, modifier);
	}

	public static RelationSentence createRelationSentence(String rname,
			Term subject) {
		RelationConstant rc = RelationConstant.createRelationConstant(rname);
		if (rc != null && rc.testAssignments(subject, null)) {
			RelationSentence rs = new RelationSentence(rc, subject);
			return rs;
		}
		return null;
	}

	// 7/12/2013
	public static RelationSentence createRelationSentence(String rname,
			Vector<Term> arguments) {
		RelationConstant rc = RelationConstant.createRelationConstant(rname);
		RelationSentence rs = new RelationSentence(rc);
		if (arguments != null && arguments.size() > 1) {
			rs = new RelationSentence(rc);
			for (Term arg : arguments) {
				rs.addTerm(arg);
			}
			Term subject = rs.getSubject();
			subject.addSubjectSentence(rs);
			Term modifier = rs.getModifier();
			if (modifier != null) {
				modifier.addModifierSentence(rs);
			}
			rc.setArity(arguments.size());
		}
		return rs;
	}

	public static boolean isRelationSentence(Vector v) {
		if (v != null && v.size() > 1) {
			for (Object o : v) {
				if (!(o instanceof String)) {
					return false;
				}
			}
		}
		return true;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		if (this.getTerms() != null) {
			for (Object o : this.getTerms()) {
				if (o instanceof Expression) {
					((Expression) o).assignContainingKBExpression(kb,
							containingKBExpression);
				}
			}
		}
	}

	// 10/16/2013: Not yet tested...
	public void setVariable(Variable v) {
		if (this.getTermCount() > 0) {
			for (int i = 0; i < this.getTermCount(); i++) {
				Object o = this.getTerm(i);
				if (o instanceof Variable) {
					Variable tv = (Variable) o;
					if (tv.getName().equals(v.getName())) {
						this.getTerms().setElementAt(v, i);
					}
				}
			}
		}
	}

	public Vector gatherRelationalSentences() {
		return VUtils.listify(this);
	}

	// 9/14/2013
	public void updateSentenceVariables(KnowledgeBase kb) {
		this.setKnowledgeBase(kb);
		if (this.getTermCount() > 0) {
			Vector newterms = null;
			for (Object o : this.getTerms()) {
				Object newterm = (o instanceof Variable ? kb.getTerm(this, o)
						: o);
				newterms = VUtils.add(newterms, newterm);
			}
			this.setTerms(newterms);
		}
	}

	// 6/27/2013
	public Expression copy() {
		RelationSentence rs = new RelationSentence(this.getRelation());
		rs.setTypeRelationSentence(this.getTypeRelationSentence());
		for (int i = 0; i < this.getTermCount(); i++) {
			Term oldterm = (Term) this.getTerm(i);
			Term newterm = (Term) oldterm.copy();
			rs.addTerm(newterm);
		}
		if (rs.getTermCount() > 1) {
			rs.setSubject((Term) rs.getTerm(0));
			rs.setModifier((Term) rs.getTerm(1));
		}
		return rs;
	}

	public RelationSentence getHead() {
		return this;
	}

	public int getArity() {
		return this.getTermCount();
	}

	public boolean isBinary() {
		return this.arity == 2;
	}

	public RelationConstant getRelation() {
		return relation;
	}

	public void setRelation(RelationConstant rc) {
		this.relation = rc;
	}

	public Vector getValidBindingStack() {
		return this.validBindingStack;
	}

	public void resetValidBindingStack() {
		this.validBindingStack = null;
	}

	public Term getSubject() {
		if (this.subject == null && this.getTerms() != null) {
			this.subject = (Term) this.getTerms().firstElement();
		}
		return this.subject;
	}

	public void setSubject(Term subject) {
		this.subject = subject;
		this.addTerm(subject);
		this.subject.addSubjectSentence(this);
	}

	public TypeConstant getSubjectType() {
		if (this.getSubject() != null) {
			return this.getSubject().getType();
		}
		return null;
	}

	public Term getModifier() {
		if (this.modifier == null && this.getTermCount() >= 2
				&& this.getTerm(1) instanceof Term) {
			this.modifier = (Term) this.getTerm(1);
		}
		return this.modifier;
	}

	public void setModifier(Term modifier) {
		this.modifier = modifier;
		this.addTerm(modifier);
		this.modifier.addModifierSentence(this);
	}

	public TypeConstant getModifierType() {
		if (this.getModifier() != null) {
			return this.getModifier().getType();
		}
		return null;
	}

	public static Term findRootTerm(Vector sentences) {
		Vector roots = findRootTerms(sentences);
		return (roots != null ? (Term) roots.firstElement() : null);
	}

	public static Vector findRootTerms(Vector<RelationSentence> sentences) {
		Vector roots = new Vector(0);
		for (int i = 0; i < sentences.size(); i++) {
			RelationSentence sent = (RelationSentence) sentences.elementAt(i);
			boolean foundmodifier = false;
			for (int j = 0; !foundmodifier && j < sentences.size(); j++) {
				if (i != j) {
					RelationSentence osent = (RelationSentence) sentences
							.elementAt(j);
					if (osent.getModifier().equals(sent.getSubject())) {
						foundmodifier = true;
					}
				}
			}
			if (!foundmodifier) {
				roots.add(sent.getSubject());
			}
		}
		return (!roots.isEmpty() ? roots : null);
	}

	public static Vector findRootSentences(Vector sentences) {
		Vector roots = new Vector(0);
		for (int i = 0; i < sentences.size(); i++) {
			RelationSentence sent = (RelationSentence) sentences.elementAt(i);
			boolean foundmodifier = false;
			for (int j = 0; !foundmodifier && j < sentences.size(); j++) {
				if (i != j) {
					RelationSentence osent = (RelationSentence) sentences
							.elementAt(j);
					if (osent.getModifier().equals(sent.getSubject())) {
						foundmodifier = true;
					}
				}
			}
			if (!foundmodifier) {
				roots.add(sent);
			}
		}
		return (!roots.isEmpty() ? roots : null);
	}

	public String toString() {
		String str = "(\"" + this.getRelation() + "\" ";
		for (int i = 0; i < this.getTermCount(); i++) {
			Object value = this.getTerm(i);
			String tstr = (value != null ? value.toString() : "*");
			str += tstr;
			if (i < this.getTermCount() - 1) {
				str += " ";
			}
		}
		str += ")";
		return str;
	}

	public String toLisp() {
		String str = "(" + this.getRelation() + " ";
		for (int i = 0; i < this.getTermCount(); i++) {
			Object o = this.getTerm(i);
			if (o instanceof Constant) {
				Constant c = (Constant) o;
				String os = (c.getFormalName() != null ? c.getFormalName() : o
						.toString());
				str += "\"" + os + "\"";
			} else if (o instanceof String) {
				str += "\"" + o + "\"";
			} else {
				str += o;
			}
			if (i < this.getTermCount() - 1) {
				str += " ";
			}
		}
		str += ")";
		return str;
	}

	public String toShortString() {
		return this.toString();
	}

	public static String toString(Term subject, Term modifier) {
		String str = "(" + subject.getName() + " " + modifier.getName() + ")";
		return str;
	}

	public static String toString(RelationConstant rc, Term subject,
			Term modifier) {
		String str = "(" + rc.getName() + " " + subject.getName() + " "
				+ modifier.getName() + ")";
		return str;
	}

	public static String toString(String rc, String subject, String modifier) {
		String str = "(" + rc + " " + subject + " " + modifier + ")";
		return str;
	}

	public static String toString(Vector<RelationSentence> rsv) {
		String str = null;
		if (rsv != null) {
			str = "";
			for (RelationSentence rs : rsv) {
				if (!"type-of".equals(rs.getRelation().getName())) {
					str += rs.toShortString();
				}
			}
			str += ",";
		}
		return str;
	}

	public static class RelationSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			RelationSentence rs1 = (RelationSentence) o1;
			RelationSentence rs2 = (RelationSentence) o2;
			return rs1.getRelation().getName()
					.compareTo(rs2.getRelation().getName());
		}
	}

	public static RelationSentence findMatching(
			Vector<RelationSentence> sentences, RelationSentence sentence) {
		if (sentence != null && sentences != null) {
			for (RelationSentence rs : sentences) {
				if (rs.getRelation().equals(sentence.getRelation())) {
					return rs;
				}
			}
		}
		return null;
	}

	public static Vector findConnecting(Vector<RelationSentence> sentences,
			Term t1, Term t2) {
		Vector connecting = null;
		for (RelationSentence rs : sentences) {
			boolean foundTerm1 = false;
			boolean foundTerm2 = false;
			for (int i = 0; i < rs.getTermCount(); i++) {
				Term t = (Term) rs.getTerm(i);
				if (t.equals(t1)) {
					foundTerm1 = true;
				} else if (t.equals(t1)) {
					foundTerm2 = true;
				}
			}
			if (foundTerm1 && foundTerm2) {
				connecting = VUtils.add(connecting, rs);
			}
		}
		return connecting;
	}

	public void setTypeRelationSentence(TypeRelationSentence trs) {
		this.typeRelationSentence = trs;
	}

	public TypeRelationSentence getTypeRelationSentence() {
		return typeRelationSentence;
	}

	public Vector<TypeConstant> gatherSupportingTypes() {
		Vector<TypeConstant> types = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (this.getTermCount() > 0 && kb != null) {
			for (Object o : this.getTerms()) {
				TypeConstant type = TypeConstant.getType(o);
				if (type != null) {
					Vector<TypeConstant> stypes = kb
							.gatherSupportingTypes(type);
					types = VUtils.addIfNot(types, stypes);
				}
			}
		}
		return types;
	}

	public static boolean isValidRelationName(String rname) {
		return (rname != null && rname.indexOf("def") != 0);
	}

	public boolean containsVariable() {
		for (int i = 0; i < this.getTermCount(); i++) {
			if (this.getTerm(i) instanceof Variable) {
				return true;
			}
		}
		return false;
	}

	public static RelationSentence findByRelation(Vector<RelationSentence> v,
			String rname) {
		if (v != null && rname != null) {
			rname = rname.toLowerCase();
			for (RelationSentence rs : v) {
				if (rname.equals(rs.getRelation().getName())) {
					return rs;
				}
			}
		}
		return null;
	}

	// Find first relation that doesn't have a given name.
	public static RelationSentence findByNotRelation(
			Vector<RelationSentence> v, String rname) {
		if (v != null && rname != null) {
			rname = rname.toLowerCase();
			for (RelationSentence rs : v) {
				if (!rname.equals(rs.getRelation().getName())) {
					return rs;
				}
			}
		}
		return null;
	}

	public Object getAnchor() {
		return anchor;
	}

	public void setAnchor(Object anchor) {
		this.anchor = anchor;
	}
	
	// 7/22/2016
	public String getTuffyString() {
		String rname = this.getRelation().getTuffyID();
		String rstid = rname + "(";
		for (int i = 0; i < this.getArity(); i++) {
			Object o = this.getTerm(i);
			String tid = null;
			if (o instanceof Expression) {
				tid = ((Expression) o).getTuffyString();
			} else {
				tid = o.toString();
			}
			tid = StrUtils.firstCharacterToUpperCase(tid);
			rstid += tid;
			if (i < this.getArity()-1) {
				rstid += ", ";
			} else {
				rstid += ")";
			}
		}
		return rstid;
	}
	

}
