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
package tsl.knowledge.ontology;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.relation.BinaryRelationSentence;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;

/*
 * (defrelation anatomic_location_of
 (isa location_of)
 (subjecttype condition)
 (modifiertype anatomic_location)
 (constraints <constraints>)
 (comment "")
 (description <some sentence>))	
 */

public class TypeRelationSentence extends BinaryRelationSentence {
	private TypeRelationSentence type = null;

	public TypeRelationSentence(RelationConstant rc, TypeConstant subject,
			TypeConstant modifier) {
		super(rc, subject, modifier);
		subject.addTypeRelationSentence(this);
		// 5/19/2014:  OWL ontologies contain relations with subject == modifier
		if (this.getSubject() != null && this.getModifier() != null
				&& this.getTermCount() == 1) {
			this.addTermForce(this.getModifier());
		}
	}
	
	// 8/30/2013
	public RelationSentence instantiate(Term t1, Term t2) {
		return new RelationSentence(this.getRelation(), t1, t2);
	}

	public boolean equals(Object o) {
		if (o instanceof TypeRelationSentence) {
			TypeRelationSentence trs = (TypeRelationSentence) o;
			if (this.getSubject() != null && this.getModifier() != null
					&& this.getRelation().equals(trs.getRelation())
					&& this.getSubject().equals(trs.getSubject())
					&& this.getModifier().equals(trs.getModifier())) {
				return true;
			}
		}
		return false;
	}

	public TypeRelationSentence getType() {
		return this.type;
	}

	public void setType(TypeRelationSentence trs) {
		this.type = trs;
	}

	public boolean sameType(TypeRelationSentence trs) {
		return (this.type != null && trs.type != null && this.type == trs.type);
	}

	public boolean canUnify(Expression e) {
		TypeRelationSentence trs = (TypeRelationSentence) e;
		return this.type.equals(trs.type);
	}

	public static TypeRelationSentence findTypeRelationSentence(
			RelationConstant rc, TypeConstant subject, TypeConstant modifier) {
		if (subject.getSubjectSentences() != null) {
			for (RelationSentence rs : subject.getSubjectSentences()) {
				TypeRelationSentence trs = (TypeRelationSentence) rs;
				if (rc.equals(trs.getRelation())
						&& modifier.equals(trs.getModifier())) {
					return trs;
				}
			}
		}
		return null;
	}

}
