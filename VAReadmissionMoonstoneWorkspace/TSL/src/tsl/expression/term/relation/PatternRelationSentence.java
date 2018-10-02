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

import java.util.Vector;

import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.ontology.TypeRelationSentence;

public class PatternRelationSentence extends RelationSentence {

	public PatternRelationSentence(TypeRelationSentence trs, String sname,
			String mname) {
		initialize(trs, sname, mname);
	}

	public PatternRelationSentence(Vector v) {
		try {
			Object rco = v.elementAt(0);
			if (rco instanceof RelationConstant) {
				this.relation = (RelationConstant) rco;
			} else if (rco instanceof String) {
				this.relation = RelationConstant
						.createRelationConstant((String) rco);
			}
			this.setSubject(new Variable(v.elementAt(1)));
			if (v.size() > 2) {
				this.setModifier(new ObjectConstant(v.elementAt(2)));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Before 8/15/2014: I am not permitting terms that contain vectors; e.g.
	// (tense ?* (tense ?1))
	// public PatternRelationSentence(Vector v) {
	// this.relation = RelationConstant.createRelationConstant((String) v
	// .elementAt(0));
	// this.setSubject(new Variable(v.elementAt(1)));
	// this.setModifier(new Variable(v.elementAt(2)));
	// }

	void initialize(TypeRelationSentence trs, String sname, String mname) {
		this.relation = trs.getRelation();
		this.setSubject(new Variable(sname));
		this.setModifier(new Variable(mname));
		this.getSubject().setType(trs.getSubjectType());
		this.getModifier().setType(trs.getModifierType());
		this.setTypeRelationSentence(trs);
	}

	public void setSubjectBinding(Object value) {
		((Variable) this.getSubject()).bind(value);
	}

	public Object getSubjectBinding() {
		return ((Variable) this.getSubject()).getValue();
	}

	public Variable getSubjectVariable() {
		return ((Variable) this.getSubject());
	}

	public void setModifierBinding(Object value) {
		((Variable) this.getModifier()).bind(value);
	}

	public Object getModifierBinding() {
		return ((Variable) this.getModifier()).getValue();
	}

	public Variable getModifierVariable() {
		return ((Variable) this.getModifier());
	}

//	public static PatternRelationSentence findByRelation(
//			Vector<PatternRelationSentence> v, String rname) {
//		if (v != null && rname != null) {
//			rname = rname.toLowerCase();
//			for (PatternRelationSentence prs : v) {
//				if (rname.equals(prs.getRelation().getName())) {
//					return prs;
//				}
//			}
//		}
//		return null;
//	}

}
