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

import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.NameSpace;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class RelationConstant extends Constant {
	private Vector defv = null;
	private Vector<RelationConstant> parents = null;
	private TypeConstant subjectType = null;
	private TypeConstant modifierType = null;
	private Hashtable paramVariableHash = new Hashtable();
	private Hashtable paramTypeHash = new Hashtable();
	private Constraint constraints = null;
	private Vector<RelationConstant> inferenceChildRelationConstants = null;
	private Vector<RelationConstant> inferenceParentRelationConstants = null;
	private int arity = 2;
	// 11/30/2013 - For uee with TSL OntologyCompoents table.
	private int uniqueID = 0;
	private int cardinality = 1;
	private boolean isTransitive = false;
	private boolean isReflexive = false;
	private boolean isSymmetric = false;

	/***
	 * NOTES: A RelationConstant contains information and constraints about
	 * relations. RelationSentences are instances of RelationConstants, e.g.
	 * (located-at Infiltrate LeftLowerLobe) TypeRelationSentences are special
	 * types of RelationSentences whose arguments are TypeConstants, and that
	 * are used to define a semantic network.
	 * 
	 */

	// (defrelation located-at
	// (isa some-type-of-location)
	// (parameters
	// (?c firstparam (types condition something-else) (roles subject))
	// (?l secondparam (types anatomic-location) (roles modifier)))
	// (constraints (and (something ?c) (something-else ?l)))

	public RelationConstant() {
	}
	
	public RelationConstant(String rname) {
		this(rname, (String) null, (String) null);
	}

	public RelationConstant(String rname, TypeConstant st, TypeConstant mt) {
		super(rname);
		this.subjectType = st;
		this.modifierType = mt;
	}

	public RelationConstant(String rname, String stname, String mtname) {
		super(rname);
		this.subjectType = TypeConstant.createTypeConstant(stname);
		this.modifierType = TypeConstant.createTypeConstant(mtname);
	}

	// 8/22/2015
	public static RelationConstant createRelationConstant(Vector v) {
		if (v != null && "defrelation".equals(v.firstElement())) {
			String rname = (String) v.elementAt(1);
			String stname = (String) v.elementAt(2);
			String mtname = (String) v.elementAt(3);
			RelationConstant rc = new RelationConstant(rname, stname, mtname);
			rc.defv = v;
			return rc;
		}
		return null;
	}
	
	public static RelationConstant createRelationConstant(String rname) {
		return createRelationConstant(rname, null, null);
	}

	public static RelationConstant createRelationConstant(String rname,
			String stname, String mtname) {
		return createRelationConstant(rname, null, stname, mtname);
	}

	public static RelationConstant createRelationConstant(String rname,
			String fullName, String stname, String mtname) {
		RelationConstant rc = VariableRelationConstant
				.createVariableRelationConstant(rname);
		if (rc == null) {
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			NameSpace ns = kb.getNameSpace();
			rc = ns.getRelationConstant(rname);
			if (rc == null) {
				rc = new RelationConstant(rname, stname, mtname);
				rc.setFullName(fullName);
			}
		}
		return rc;
	}

	// Before 8/22/2015:
	// public RelationConstant(String rname) {
	// super(rname);
	// }
	//
	// public static RelationConstant createRelationConstant(Vector v) {
	// if (v != null && "defrelation".equals(v.firstElement())) {
	// String rname = (String) v.elementAt(1);
	// RelationConstant rc = new RelationConstant(rname);
	// rc.defv = v;
	// return rc;
	// }
	// return null;
	// }
	//
	// public static RelationConstant createRelationConstant(String rname) {
	// return createRelationConstant(rname, null);
	// }
	//
	// public static RelationConstant createRelationConstant(String rname,
	// String fullName) {
	// RelationConstant rc = VariableRelationConstant
	// .createVariableRelationConstant(rname);
	// if (rc == null) {
	// KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
	// NameSpace ns = kb.getNameSpace();
	// rc = ns.getRelationConstant(rname);
	// if (rc == null) {
	// rc = new RelationConstant(rname);
	// rc.setFullName(fullName);
	// }
	// }
	// return rc;
	// }

	public void resolve() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		NameSpace ns = kb.getNameSpace();
		kb.clearFields();
		RelationConstant prc = ns.getRelationConstant((String) VUtils
				.assocValue("isa", this.defv));
		this.addParent(prc);
		Vector<Vector> paramv = VUtils.rest((Vector) VUtils.assoc("parameters",
				this.defv));
		if (paramv != null) {
			for (Vector pv : paramv) {
				String vname = (String) pv.elementAt(0);
				Variable var = (Variable) kb.getTerm(this, vname);
				this.addVariable(var);
				String pname = (String) pv.elementAt(1);
				this.paramVariableHash.put(pname, var);
				Vector<String> tnames = VUtils.rest(VUtils.assoc("types", pv));
				if (tnames != null) {
					for (String tname : tnames) {
						TypeConstant type = ns.getTypeConstant(tname);
						this.paramVariableHash.put(type, var);
						VUtils.pushHashVector(this.paramTypeHash, var, type);
					}
				}
				Vector<String> roles = VUtils.rest(VUtils.assoc("roles", pv));
				if (roles != null) {
					for (String role : roles) {
						this.paramVariableHash.put(role, var);
					}
				}
			}
			Vector cv = (Vector) VUtils.assocValue("constraints", this.defv);
			if (cv != null) {
				this.constraints = Constraint.createConstraint(kb, cv);
			}
			this.setUserComment((String) VUtils
					.assocValue("comment", this.defv));
		}
	}

	public String toString() {
		return this.getName();
	}

	public boolean testAssignments(Vector<Term> parameters) {
		if (this.paramTypeHash.isEmpty()) {
			return true;
		}
		if (parameters == null || parameters.size() != this.getVariableCount()) {
			return false;
		}
		Variable.unbind(this.getVariables());
		for (Term parameter : parameters) {
			TypeConstant type = parameter.getType();
			if (type == null) {
				return false;
			}
			Variable var = (Variable) this.paramTypeHash.get(type);
			if (var == null) {
				return false;
			}
			var.bind(parameter);
		}
		if (this.constraints != null) {
			if (!this.constraints.doTestConstraint(this.getVariables())) {
				return false;
			}
		}
		return true;
	}

	public boolean testAssignments(Term subject, Term modifier) {
		if (this.paramTypeHash.isEmpty()) {
			return true;
		}
		Variable.unbind(this.getVariables());
		TypeConstant type = subject.getType();
		if (type == null) {
			return false;
		}
		Variable var = (Variable) this.paramTypeHash.get(type);
		if (var == null) {
			return false;
		}
		var.bind(subject);
		if (modifier != null) {
			type = modifier.getType();
			if (type == null) {
				return false;
			}
			var = (Variable) this.paramTypeHash.get(type);
			if (var == null) {
				return false;
			}
			var.bind(modifier);
		}
		if (this.constraints != null) {
			if (!this.constraints.doTestConstraint(this.getVariables())) {
				return false;
			}
		}
		return true;
	}

	public void addInferenceChildRelationConstants(RelationConstant supporting) {
		this.inferenceChildRelationConstants = VUtils.addIfNot(
				this.inferenceChildRelationConstants, supporting);
	}

	public Vector<RelationConstant> getImmediateInferenceChildRelationConstants() {
		return this.inferenceChildRelationConstants;
	}

	public Vector<RelationConstant> getAllInferenceChildRelationConstants() {
		Vector<RelationConstant> allRCs = VUtils.listify(this);
		if (this.inferenceChildRelationConstants != null) {
			for (RelationConstant child : this.inferenceChildRelationConstants) {
				allRCs = VUtils.appendIfNot(allRCs,
						child.getAllInferenceChildRelationConstants());
			}
		}
		return allRCs;
	}

	public void addInferenceParentRelationConstants(RelationConstant parent) {
		this.inferenceParentRelationConstants = VUtils.addIfNot(
				this.inferenceParentRelationConstants, parent);
	}

	public Vector<RelationConstant> getImmediateInferenceParentRelationConstants() {
		return this.inferenceParentRelationConstants;
	}

	public Vector<RelationConstant> getAllInferenceParentRelationConstants() {
		Vector<RelationConstant> allRCs = VUtils.listify(this);
		if (this.inferenceParentRelationConstants != null) {
			for (RelationConstant parent : this.inferenceParentRelationConstants) {
				allRCs = VUtils.appendIfNot(allRCs,
						parent.getAllInferenceParentRelationConstants());
			}
		}
		return allRCs;
	}

	public Variable getTypeVariable(TypeConstant type) {
		return (Variable) this.paramTypeHash.get(type);
	}

	public Vector<TypeConstant> getTypes(Variable var) {
		Vector<TypeConstant> types = (Vector<TypeConstant>) this.paramTypeHash
				.get(var);
		return types;
	}

	public Constraint getConstraints(Variable var) {
		return this.constraints;
	}

	// Need to support multiple parents ...
	public RelationConstant getParent() {
		if (this.parents != null) {
			return this.parents.firstElement();
		}
		return null;
	}

	public void addParent(RelationConstant parent) {
		this.parents = VUtils.add(this.parents, parent);
	}

	public Vector<RelationConstant> getInferenceChildRelationConstants() {
		return inferenceChildRelationConstants;
	}

	public Vector<RelationConstant> getInferenceParentRelationConstants() {
		return inferenceParentRelationConstants;
	}

	public Vector getDefv() {
		return defv;
	}

	public void setDefv(Vector defv) {
		this.defv = defv;
	}

	public Constraint getConstraints() {
		return constraints;
	}

	public void setConstraints(Constraint constraints) {
		this.constraints = constraints;
	}

	public int getArity() {
		return arity;
	}

	public void setArity(int arity) {
		this.arity = arity;
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(int uniqueID) {
		this.uniqueID = uniqueID;
	}

	public boolean isAncestor(RelationConstant rc) {
		return (rc != null && (this.equals(rc.getParent()) || this
				.isAncestor(rc.getParent())));
	}

	// 4/26/2015: Methods for handling information about extensional sets
	// defined by relation constants.

	public int getCardinality() {
		return this.cardinality;
	}

	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	public boolean isTransitive() {
		return isTransitive;
	}

	public void setTransitive(boolean isTransitive) {
		this.isTransitive = isTransitive;
	}

	public boolean isReflexive() {
		return isReflexive;
	}

	public void setReflexive(boolean isReflexive) {
		this.isReflexive = isReflexive;
	}

	public boolean isSymmetric() {
		return isSymmetric;
	}

	public void setSymmetric(boolean isSymmetric) {
		this.isSymmetric = isSymmetric;
	}

	public TypeConstant getSubjectType() {
		return subjectType;
	}

	public TypeConstant getModifierType() {
		return modifierType;
	}
	
	public String getTuffyID() {
		return StrUtils.firstCharacterToUpperCase(this.getName());
	}
}
