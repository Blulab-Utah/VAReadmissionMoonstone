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
package tsl.expression.term.property;

import java.util.Vector;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.NameSpace;
import tsl.utilities.VUtils;

public class PropertyConstant extends RelationConstant {
	private Vector<TypeConstant> domainTypes = null;
	private Class rangeJavaClass = null;
	private Object defaultRangeValue = null;
	private Variable domainVar = null;
	private Variable rangeVar = null;
	private int cardinality = 1;

	/*
	 * 
	 * (defproperty age (domaintypes (condition something-else)) (rangeclass
	 * "java.lang.Float") (constraints (and (something ?domain) (<= ?range
	 * 1000))) (comment ""))
	 */

	public PropertyConstant(String rname) {
		super(rname);
	}

	public static PropertyConstant createPropertyConstant(Vector v) {
		if (v != null && "defproperty".equals(v.firstElement())) {
			String rname = (String) v.elementAt(1);
			PropertyConstant pc = new PropertyConstant(rname);
			pc.setDefv(v);
			return pc;
		}
		return null;
	}

	// Create PropertyConstant "on the fly" using a name, domain term and value.
	public static PropertyConstant createPropertyConstant(String name,
			TypeConstant type, Object value) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		NameSpace ns = kb.getNameSpace();
		PropertyConstant pc = ns.getPropertyConstant(name);
		if (pc == null && name != null) {
			pc = new PropertyConstant(name);
			pc.domainTypes = VUtils.add(pc.domainTypes, type);
			if (value != null) {
				pc.rangeJavaClass = value.getClass();
				pc.defaultRangeValue = value;
			}
		}
		return pc;
	}

	public void resolve() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		NameSpace ns = kb.getNameSpace();
		kb.clearFields();
		this.addParent(ns.getRelationConstant((String) VUtils.assocValue("isa",
				this.getDefv())));
		this.domainVar = new Variable("?domain");
		this.rangeVar = new Variable("?range");
		this.addVariable(this.domainVar);
		this.addVariable(this.rangeVar);
		Vector<String> dtnames = (Vector<String>) VUtils.assocValue(
				"domaintypes", this.getDefv());
		if (dtnames != null) {
			for (String dtname : dtnames) {
				TypeConstant dtype = ns.getTypeConstant(dtname);
				this.domainTypes = VUtils.add(this.domainTypes, dtype);
			}
		}
		String rcname = (String) VUtils
				.assocValue("rangeclass", this.getDefv());
		if (rcname != null) {
			try {
				this.rangeJavaClass = Class.forName(rcname);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Vector cv = (Vector) VUtils.assocValue("constraints", this.getDefv());
		if (cv != null) {
			// Before 6/17/2013
			// Constraint tp = new Constraint(cv, this.getVariables());
			Constraint tp = Constraint.createConstraint(kb, cv,
					this.getVariables());
			this.setConstraints(tp);
		}
		this.setUserComment((String) VUtils.assocValue("comment",
				this.getDefv()));
	}

	public boolean testAssignments(Term domain, Object range) {
		if (this.domainTypes != null) {
			TypeConstant ttype = domain.getType();
			if (ttype == null) {
				return false;
			}
			boolean found = false;
			for (TypeConstant dtype : this.domainTypes) {
				if (ttype.subsumedBy(dtype)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		if (this.getConstraints() != null) {
			this.domainVar.bind(domain);
			this.rangeVar.bind(range);
			boolean rv = this.getConstraints().doTestConstraint(
					this.getVariables());
			this.domainVar.unbind();
			this.rangeVar.unbind();
			return rv;
		}
		return true;
	}

	public String toString() {
		return this.getName();
	}

	public Vector<TypeConstant> getDomainTypes() {
		return domainTypes;
	}

	public Class getRangeJavaClass() {
		return rangeJavaClass;
	}
	
	public Object getDefaultRangeValue() {
		return this.defaultRangeValue;
	}

	public int getCardinality() {
		return cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

}
