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
package tsl.knowledge.knowledgebase;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.Form;
import tsl.expression.form.assertion.Assertion;
import tsl.expression.form.definition.Definition;
import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.function.FunctionTerm;
import tsl.expression.term.function.javafunction.JavaFunctionConstant;
import tsl.expression.term.function.javafunction.JavaFunctionTerm;
import tsl.expression.term.function.logicfunction.LogicFunctionConstant;
import tsl.expression.term.function.logicfunction.LogicFunctionTerm;
import tsl.expression.term.function.slot.SlotFunctionConstant;
import tsl.expression.term.function.slot.SlotFunctionTerm;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.LambdaVariable;
import tsl.expression.term.variable.Variable;
import tsl.expression.term.vector.VectorTerm;
import tsl.inference.InferenceEngine;
import tsl.inference.backwardchaining.BackwardChainingInferenceEngine;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class KnowledgeBase {

	private KnowledgeEngine knowledgeEngine = null;
	private String name = null;
	private KnowledgeBase parentKB = null;
	private NameSpace nameSpace = null;
	private Ontology ontology = null;
	private Hashtable<String, Vector<Expression>> relationSentenceLookupHash = new Hashtable();
	private Hashtable<String, Vector<ImplicationSentence>> implicationSentenceLookupHash = new Hashtable();
	private Hashtable<String, Term> globalVariableHash = new Hashtable();
	private Hashtable<String, Expression> logicFunctionValueHash = new Hashtable();
	private Hashtable<String, Object> stringTermHash = new Hashtable();
	public Vector<Variable> sentenceVariableList = null;
	private Vector<Constant> sentenceConstantList = null;
	private Vector<RelationSentence> relationSentenceList = null;
	private Vector<FunctionTerm> functionTermList = null;
	private Expression queryExpression = null;
	private int proofDepth = 0;
	private Sentence currentSentence = null;
	private InferenceEngine inferenceEngine = null;
	private Hashtable<TypeConstant, Boolean> typeRelevanceHash = new Hashtable();
	private Hashtable<String, Sentence> namedSentenceHash = new Hashtable();
	private Hashtable<String, Macro> macroHash = new Hashtable();
	private static Term nullPlaceholderTerm = new Term("NullPlaceholder");
	private Hashtable<Object, Expression> containingExpressionHash = new Hashtable();

	// private Vector<RelationSentence> allRelationExpressions = null;

	// 11/28/2013: To make the KB accessible from the KE, I now need to call
	public KnowledgeBase() {
		this.knowledgeEngine = KnowledgeEngine.getCurrentKnowledgeEngine();
		this.inferenceEngine = new BackwardChainingInferenceEngine(this);
		this.nameSpace = new NameSpace();
		this.ontology = this.knowledgeEngine.getCurrentOntology();
	}

	public KnowledgeBase(String name) {
		this(name, null);
	}

	public KnowledgeBase(String name, KnowledgeBase parent) {
		this();
		this.name = name;
		this.knowledgeEngine.storeKnowledgeBaseHash(this);
		if (parent != null) {
			this.parentKB = parent;
			// parent.addChildKB(this);
		}
	}

	public KnowledgeBase(String name, KnowledgeEngine ke, KnowledgeBase parent) {
		this(name, parent);
		this.knowledgeEngine = ke;
	}

	public void readRuleFile(String rulefile) {
		if (rulefile != null) {
			readRules(rulefile);
		}
	}

	public static KnowledgeBase getCurrentKnowledgeBase() {
		KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
		return kb;
	}

	public static KnowledgeBase getRootKnowledgeBase() {
		KnowledgeBase kb = getCurrentKnowledgeBase();
		while (kb.getParentKB() != null) {
			kb = kb.getParentKB();
		}
		return kb;
	}

	public Constant getConstant(String name) {
		return this.getNameSpace().getConstant(name);
	}

	public TypeConstant getTypeConstant(String tname) {
		return this.getNameSpace().getTypeConstant(tname);
	}

	public RelationConstant getRelationConstant(String rname) {
		return this.getNameSpace().getRelationConstant(rname);
	}

	// Add Function and Property getters

	public NameSpace getNameSpace() {
		return this.nameSpace;
	}

	public void resolveConstants() {
		this.getNameSpace().getCurrentSymbolTable().resolveConstants();
	}

	public Vector<RelationSentence> getStoredRelationSentences(RelationConstant rc) {
		Vector sentences = this.relationSentenceLookupHash.get(rc.getName());
		if (sentences != null) {
			sentences = new Vector(sentences);
		} else if (this.getParentKB() != null) {
			sentences = this.getParentKB().getStoredRelationSentences(rc);
		}
		return sentences;
	}

	public Vector<ImplicationSentence> getStoredImplicationSentences(RelationConstant rc) {
		Vector sentences = this.implicationSentenceLookupHash.get(rc.getName());
		if (sentences != null) {
			sentences = new Vector(sentences);
		} else if (this.getParentKB() != null) {
			sentences = this.getParentKB().getStoredImplicationSentences(rc);
		}
		return sentences;
	}

	public Vector<Sentence> getStoredSentences(RelationConstant rc) {
		Vector rsents = getStoredRelationSentences(rc);
		Vector isents = getStoredImplicationSentences(rc);
		return VUtils.appendNew(rsents, isents);
	}

	// public Vector getStoredExpressions(String name) {
	// Vector v = expressionLookupHash.get(name);
	// if (v == null && this.getParentKB() != null) {
	// v = this.getParentKB().getStoredExpressions(name);
	// }
	// return v;
	// }

	public void storeStringTerm(String str, Expression expr) {
		this.stringTermHash.put(str, expr);
	}

	public void storeLogicFunctionValue(String skey, Expression expr) {
		this.logicFunctionValueHash.put(skey, expr);
	}

	public Expression getLogicFunctionValue(Vector v) {
		// GENERATE KEY
		return this.logicFunctionValueHash.get(v);
	}

	public Expression getLogicFunctionValue(LogicFunctionTerm lft) {
		// GENERATE KEY
		return this.logicFunctionValueHash.get(lft);
	}

	// public Vector doQuery(Sentence sentence, Vector binds, boolean
	// unpackResults) {
	// setQueryExpression(sentence);
	// initializeProof();
	// sentence.pushProofVariables(binds);
	// this.getInferenceEngine().setKnowledgeBase(this);
	// BackwardChainingInferenceEngine.ProofCount = 0;
	// boolean proved = this.getInferenceEngine().prove(sentence);
	// if (proved && sentence.getProofVariables() != null) {
	// if (unpackResults) {
	// return ProofVariable.unpackValidatedBindings();
	// } else {
	// return ProofVariable.getValidatedBindings();
	// }
	// }
	// return null;
	// }

	// 9/14/2013
	public void updateSentenceVariables(Sentence sentence) {
		clearFields();
		if (sentence != null) {
			sentence.updateSentenceVariables(this);
			sentence.assignContainingKBExpression(this, sentence);
			if (sentenceVariableList != null) {
				sentence.setVariables(new Vector(sentenceVariableList));
			}
		}
		clearFields();
	}

	public void initializeAndAddForm(Form form) {
		if (form != null) {
			initializeForm(form);
			addForm(form);
			clearFields();
		}
	}

	// 6/30/2015
	public static KnowledgeBase createAndInitialize(KnowledgeBase parent, String name,
			Vector<RelationSentence> sentences) {
		KnowledgeBase kb = new KnowledgeBase(name, parent);
		if (sentences != null) {
			for (RelationSentence rs : sentences) {
				kb.initializeForm((Form) rs);
				kb.addForm(rs);
				kb.clearFields();
			}
		}
		return kb;
	}

	public void initializeForm(Form form) {
		this.currentSentence = null;
		if (form instanceof Definition || form instanceof Assertion) {
			form.eval();
		} else if (form instanceof Sentence) {
			Sentence sentence = (Sentence) form;
			this.currentSentence = sentence;
			sentence.setKnowledgeBase(this);
			sentence.assignContainingKBExpression(this, sentence);
			if (sentenceVariableList != null) {
				sentence.setVariables(new Vector(sentenceVariableList));
			}
		}
		clearFields();
	}

	public Form initializeForm(Sexp sexp) {
		Vector v = JLUtils.convertSexpToJVector(sexp);
		Form form = (Form) Form.createForm(v);
		initializeForm(form);
		clearFields();
		return form;
	}

	public Form initializeForm(Vector v) {
		Form form = (Form) Form.createForm(v);
		initializeForm(form);
		return form;
	}

	// 6/28/2013: THIS DOES NOT PERMIT ADDITION OF 'NOT AND 'OR SENTENCES,
	// BUT BREAKS DOWN 'AND SENTENCES INTO INDIVIDUAL RELATION SENTENCES.
	public void addForm(Form form) {
		if (form instanceof Sentence) {
			Sentence sentence = (Sentence) form;
			form.setKnowledgeBase(this);
			if (sentence instanceof ImplicationSentence) {
				addImplicationSentence((ImplicationSentence) sentence);
			} else if (sentence instanceof RelationSentence) {
				RelationSentence rs = (RelationSentence) sentence;
				VUtils.pushIfNotHashVector(this.relationSentenceLookupHash, rs.getRelation().getName(), rs);
			} else if (sentence instanceof AndSentence) {
				addAndSentence((AndSentence) form);
			}
			this.addNamedSentence(sentence);
		}
	}

	void addImplicationSentence(ImplicationSentence isent) {
		VUtils.pushIfNotHashVector(this.implicationSentenceLookupHash,
				isent.getConsequentRelationSentence().getRelation().getName(), isent);
		Vector<RelationSentence> relsents = isent.getAntecedent().gatherRelationalSentences();
		RelationConstant arc = isent.getConsequentRelationSentence().getRelation();
		for (RelationSentence rs : relsents) {
			arc.addInferenceChildRelationConstants(rs.getRelation());
			rs.getRelation().addInferenceParentRelationConstants(arc);
		}
	}

	void addAndSentence(AndSentence asent) {
		for (Sentence subsent : asent.getSentences()) {
			addForm(subsent);
		}
	}

	public Vector<ImplicationSentence> getImplicationSentenceLookupHash(Object key) {
		return (Vector<ImplicationSentence>) this.implicationSentenceLookupHash.get(key);
	}

	public void removeSentence(Sentence sentence) {
		if (sentence instanceof RelationSentence) {
			RelationSentence rs = (RelationSentence) sentence;
			VUtils.popHashVector(this.relationSentenceLookupHash, rs.getName(), rs);
		}
		RelationSentence rs = sentence.getHead();
		VUtils.popHashVector(this.relationSentenceLookupHash, rs.getRelation().getName(), rs);
		rs.getSubject().removeSubjectSentence(rs);
		rs.getModifier().removeModifierSentence(rs);
		if (rs.getSupports() != null) {
			for (Sentence supported : rs.getSupports()) {
				removeSentence(supported);
			}
		}
	}

	public Vector getTerms(Expression expr, Vector v) {
		Vector terms = null;
		if (expr != null && v != null) {
			for (Object o : v) {
				Object term = getTerm(expr, o);
				if (term == null) {
					return null;
				}
				terms = VUtils.add(terms, term);
			}
		}
		return terms;
	}

	public Object getTerm(Expression expr, Object o) {
		Object term = null;
		if (Variable.isVariable(o)) {
			String vname = o.toString();
			KnowledgeBase kb = this;
			if ((term = kb.globalVariableHash.get(vname)) != null) {
				return term;
			}
			if ((term = Variable.find(this.sentenceVariableList, vname)) != null) {
				return term;
			}
			Variable var = new Variable(vname);
			sentenceVariableList = VUtils.add(sentenceVariableList, var);
			var.setContainingKBExpressionIndex(sentenceVariableList.size() - 1);
			return var;
		} else if (o instanceof String) {
			String str = (String) o;
			if (this.currentSentence != null && (term = this.currentSentence.getSentenceTerm(str)) != null) {
				return term;
			}
			if ((term = this.getNameSpace().getTypeConstant(str)) != null) {
				return term;
			}
			if ((term = this.getSentenceConstant(str)) != null) {
				return term;
			}

			if ((term = this.getNameSpace().getStringConstant(str)) != null) {
				return term;
			}
			term = new ObjectConstant(str);
			sentenceConstantList = VUtils.add(sentenceConstantList, term);
			return term;
		} else if (o instanceof Vector) {
			Vector v = (Vector) o;
			term = this.getLogicFunctionValue(v);
			if (term != null) {
				return term;
			}
			if (LambdaVariable.isLambdaVariableDefinition(v)) {
				return LambdaVariable.createLambdaVariable(v);
			}
			if (v.firstElement() instanceof Vector) {
				return new VectorTerm(this, expr, v);
			}
			if (v.firstElement() instanceof String) {
				String fname = (String) v.firstElement();
				if (SlotFunctionConstant.getSlotName(fname) != null) {
					return new SlotFunctionTerm(v);
				}
				Constant c = this.getNameSpace().getFunctionConstant(fname);
				if (c == null) {
					c = JavaFunctionConstant.createJavaFunctionConstant(fname);
				}
				if (c == null) {
					c = LogicFunctionConstant.createLogicFunctionConstant(v);
				}
				if (c != null) {
					if (c instanceof JavaFunctionConstant) {
						return new JavaFunctionTerm(v);
					}
					if (c instanceof LogicFunctionConstant) {
						return new LogicFunctionTerm(v);
					}
				}
			}

			// 5/13/2015: Wrap the vector in an ObjectConstant.
			return new ObjectConstant(o);

		} else {
			// 12/6/2015: Don't wrap if o is already a Term...
			term = (o instanceof Term ? (Term) o : new ObjectConstant(o));
			if (o instanceof String) {
				this.storeStringTerm((String) o, expr);
			}
			return term;
		}
	}

	public Object getTerm_BEFORE_8_3_2016(Expression expr, Object o) {
		Object term = null;
		if (Variable.isVariable(o)) {
			String vname = o.toString();
			KnowledgeBase kb = this;
			if ((term = kb.globalVariableHash.get(vname)) != null) {
				return term;
			}
			if ((term = Variable.find(this.sentenceVariableList, vname)) != null) {
				return term;
			}
			Variable var = new Variable(vname);
			sentenceVariableList = VUtils.add(sentenceVariableList, var);
			var.setContainingKBExpressionIndex(sentenceVariableList.size() - 1);
			return var;
		} else if (o instanceof String) {
			String str = (String) o;
			if (this.currentSentence != null && (term = this.currentSentence.getSentenceTerm(str)) != null) {
				return term;
			}
			if ((term = this.getNameSpace().getTypeConstant(str)) != null) {
				return term;
			}
			if ((term = this.getSentenceConstant(str)) != null) {
				return term;
			}

			if ((term = this.getNameSpace().getStringConstant(str)) != null) {
				return term;
			}
			term = new ObjectConstant(str);
			sentenceConstantList = VUtils.add(sentenceConstantList, term);
			return term;
		} else if (o instanceof Vector) {
			Vector v = (Vector) o;
			term = this.getLogicFunctionValue(v);
			if (term != null) {
				return term;
			}
			if (LambdaVariable.isLambdaVariableDefinition(v)) {
				return LambdaVariable.createLambdaVariable(v);
			}
			String fname = (String) v.firstElement();
			if (SlotFunctionConstant.getSlotName(fname) != null) {
				return new SlotFunctionTerm(v);
			}
			Constant c = this.getNameSpace().getFunctionConstant(fname);
			if (c == null) {
				c = JavaFunctionConstant.createJavaFunctionConstant(fname);
			}
			if (c == null) {
				c = LogicFunctionConstant.createLogicFunctionConstant(v);
			}
			if (c != null) {
				if (c instanceof JavaFunctionConstant) {
					return new JavaFunctionTerm(v);
				}
				if (c instanceof LogicFunctionConstant) {
					return new LogicFunctionTerm(v);
				}
			}

			// 5/13/2015: Wrap the vector in an ObjectConstant.
			return new ObjectConstant(o);

		} else {
			// 12/6/2015: Don't wrap if o is already a Term...
			term = (o instanceof Term ? (Term) o : new ObjectConstant(o));
			if (o instanceof String) {
				this.storeStringTerm((String) o, expr);
			}
			return term;
		}
	}

	// 5/20/2014 NOTE: Should move all these over to InferenceEngine

	public Expression getQueryExpression() {
		return this.queryExpression;
	}

	public void setQueryExpression(Expression queryExpression) {
		this.queryExpression = queryExpression;
	}

	public void initializeProof() {
		ProofVariable.initialize();
		initializeProofDepth();
	}

	public void initializeProofDepth() {
		this.proofDepth = 0;
	}

	public int getProofDepth() {
		return this.proofDepth;
	}

	public void incrementProofDepth() {
		this.proofDepth++;
	}

	public void decrementProofDepth() {
		this.proofDepth--;
	}

	public static Term getNullPlaceholderTerm() {
		return nullPlaceholderTerm;
	}

	public void addRelationSentenceList(RelationSentence rs) {
		this.relationSentenceList = VUtils.add(this.relationSentenceList, rs);
	}

	public void addFunctionTermList(FunctionTerm ft) {
		this.functionTermList = VUtils.add(this.functionTermList, ft);
	}

	public void addSentenceConstantList(Constant c) {
		this.sentenceConstantList = VUtils.add(this.sentenceConstantList, c);
	}

	public void clearFields() {
		this.relationSentenceList = null;
		this.sentenceVariableList = null;
		this.functionTermList = null;
		this.sentenceConstantList = null;
		this.currentSentence = null;
	}

	// 7/13/2016: So I can reload grammar inference rules.
	public void clearImplicationSentenceHash() {
		this.implicationSentenceLookupHash.clear();
	}

	public KnowledgeBase getParentKB() {
		return parentKB;
	}

	public void setParentKB(KnowledgeBase parentKB) {
		this.parentKB = parentKB;
	}

	// public Vector<KnowledgeBase> getChildKBs() {
	// return childKBs;
	// }
	//
	// public void addChildKB(KnowledgeBase child) {
	// this.childKBs = VUtils.add(this.childKBs, child);
	// }
	//
	// public void removeChildKB(KnowledgeBase child) {
	// if (this.childKBs != null) {
	// this.childKBs.remove(child);
	// }
	// }

	public String getName() {
		return this.name;
	}

	public Sentence getCurrentSentence() {
		return currentSentence;
	}

	public void setCurrentSentence(Sentence currentSentence) {
		this.currentSentence = currentSentence;
	}

	public Object getSentenceConstant(String cname) {
		if (this.sentenceConstantList != null) {
			for (Term t : this.sentenceConstantList) {
				if (cname.equals(t.getName())) {
					return t;
				}
			}
		}
		return null;
	}

	public void readRules(String fname) {
		JLisp jl = JLisp.getJLisp();
		Sexp s = (Sexp) jl.loadFile(fname);
		Vector<Vector> v = JLUtils.convertSexpToJVector(s);
		if (v != null) {
			for (Vector sv : v) {
				Macro macro = Macro.createMacro(sv);
				if (macro != null) {
					this.macroHash.put(macro.getName(), macro);
				} else {
					macro = this.macroHash.get(sv.firstElement());
					if (macro != null) {
						sv = (Vector) macro.expand(sv);
					}
					clearFields();
					Form form = Form.createForm(sv);
					if (form instanceof ImplicationSentence) {
						ImplicationSentence isent = (ImplicationSentence) form;
						this.initializeAndAddForm(isent);
					}
				}
			}
		}
	}

	public InferenceEngine getInferenceEngine() {
		return inferenceEngine;
	}

	public void setInferenceEngine(InferenceEngine inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
	}

	public Vector<Variable> getSentenceVariableList() {
		return sentenceVariableList;
	}

	public void setSentenceVariableList(Vector<Variable> sentenceVariableList) {
		this.sentenceVariableList = sentenceVariableList;
	}

	public Vector<Constant> getSentenceConstantList() {
		return sentenceConstantList;
	}

	public void setSentenceConstantList(Vector<Constant> sentenceConstantList) {
		this.sentenceConstantList = sentenceConstantList;
	}

	public Vector<RelationSentence> getRelationSentenceList() {
		return relationSentenceList;
	}

	public void setRelationSentenceList(Vector<RelationSentence> relationSentenceList) {
		this.relationSentenceList = relationSentenceList;
	}

	// 6/28/2013: Not yet tested. In Topaz2, as I add RelSents I will update
	// type relevance
	// for argument types. For each relevant type I will generate TypeTemplates
	// for the new
	// sets of variable bindings.
	public boolean isTypeRelevant(TypeConstant type) {
		Boolean relevant = this.typeRelevanceHash.get(type);
		if (relevant != null) {
			return relevant.booleanValue();
		}
		return false;
	}

	public boolean checkTypeRelevance(TypeConstant type) {
		Boolean relevance = this.typeRelevanceHash.get(type);
		if (relevance == null) {
			relevance = new Boolean(false);
			this.typeRelevanceHash.put(type, relevance);
		}
		if (!relevance) {
			if (type.getDefinitionRelations() != null) {
				boolean found = true;
				for (RelationConstant drc : type.getDefinitionRelations()) {
					if (this.getStoredRelationSentences(drc) == null) {
						found = false;
						break;
					}
				}
				if (found) {
					relevance = true;
					this.typeRelevanceHash.put(type, true);
				}
			}
		}
		return relevance;
	}

	public void addNamedSentence(Sentence sentence) {
		if (sentence.getName() != null) {
			this.namedSentenceHash.put(sentence.getName(), sentence);
		}
	}

	public Sentence getNamedSentence(String name) {
		return this.namedSentenceHash.get(name);
	}

	public Vector<Sentence> getAllNamedSentences() {
		return HUtils.getElements(this.namedSentenceHash);
	}

	public Vector<String> getAllNamedSentencesNames() {
		return HUtils.getKeys(this.namedSentenceHash);
	}

	public Vector<RelationConstant> getAllRelations() {
		return getAllRelationConstants(-1);
	}

	public Vector<RelationConstant> getAllRelationConstants(int arity) {
		Vector<RelationConstant> rcs = null;
		for (Enumeration<String> e = this.relationSentenceLookupHash.keys(); e.hasMoreElements();) {
			String ostr = e.nextElement();
			RelationConstant rc = this.getNameSpace().getRelationConstant(ostr);
			if (rc != null && (arity == -1 || rc.getArity() == arity)) {
				rcs = VUtils.add(rcs, rc);
			}
		}
		return rcs;
	}

	// 10/2/2013: For breaking sentences down into types (inclusive of the type
	// itself)
	// that can be used to identify relevant documents.
	public Vector<TypeConstant> gatherSupportingTypes(Vector<TypeConstant> types) {
		Vector<TypeConstant> alltypes = null;
		if (types != null) {
			for (TypeConstant t : types) {
				alltypes = VUtils.appendIfNot(alltypes, gatherSupportingTypes(t));
			}
		}
		return alltypes;
	}

	public Vector<TypeConstant> gatherSupportingTypes(TypeConstant type) {
		Vector<TypeConstant> types = null;
		// types = VUtils.add(types, type);
		// Vector<Expression> expressions = this.getExpressionLookupHash(type);
		// if (expressions != null) {
		// for (Expression expr : expressions) {
		// types = VUtils.appendIfNot(types, expr.gatherSupportingTypes());
		// }
		// }
		return types;
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

	public Ontology getOntology() {
		if (this.ontology == null) {
			this.ontology = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentOntology();
		}
		return this.ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}

	public Macro getBoundMacro(Vector v) {
		if (v != null && v.size() == 3 && v.firstElement() instanceof String && v.elementAt(1) instanceof Vector
				&& v.elementAt(2) instanceof Vector) {
			Macro macro = this.macroHash.get((String) v.firstElement());
			if (macro != null) {
				Vector params = (Vector) v.elementAt(1);
				Vector<Variable> vars = macro.getVariables();
				if (vars != null && params != null && vars.size() == params.size()) {
					Variable.bind(vars, params);
				}
				Variable var = new Variable("?sentence");
				var.bind(v.elementAt(3));
				macro.setVariables(VUtils.add(vars, var));
				return macro;
			}
		}
		return null;
	}

	// 1/13/2014
	public void indexContainingExpression(List<ProofVariable> l, Expression e) {
		if (l != null && e != null) {
			for (Iterator<ProofVariable> i = l.iterator(); i.hasNext();) {
				ProofVariable pv = i.next();
				if (pv.getValue() != null) {
					Object value = pv.getValue();
					this.containingExpressionHash.put(value, e);
				}
			}
		}
	}

	public Expression getContainingExpression(Object o) {
		return this.containingExpressionHash.get(o);
	}

	// public Form portForm(Form form) {
	// form.setKnowledgeBase(this);
	// this.clearFields();
	// form.portKB(this);
	// initializeForm(form);
	// this.clearFields();
	// return form;
	// }

	public void addGlobalVariable(String vname, Term term) {
		this.globalVariableHash.put(vname, term);
	}

	public Vector<RelationSentence> getAllRelationSentences() {
		Vector<RelationSentence> sentences = null;
		for (Enumeration<String> e = this.relationSentenceLookupHash.keys(); e.hasMoreElements();) {
			String rname = e.nextElement();
			Vector<RelationSentence> rsents = new Vector(this.relationSentenceLookupHash.get(rname));
			sentences = VUtils.append(sentences, rsents);
		}
		if (sentences == null && this.getParentKB() != null) {
			sentences = this.getParentKB().getAllRelationSentences();
		}
		return sentences;
	}

	public Vector<ImplicationSentence> getAllImplicationSentences() {
		Vector<ImplicationSentence> sentences = null;
		for (Enumeration<String> e = this.implicationSentenceLookupHash.keys(); e.hasMoreElements();) {
			String rname = e.nextElement();
			Vector<ImplicationSentence> isents = new Vector(this.implicationSentenceLookupHash.get(rname));
			sentences = VUtils.append(sentences, isents);
		}
		if (sentences == null && this.getParentKB() != null) {
			sentences = this.getParentKB().getAllImplicationSentences();
		}
		return sentences;
	}

	public String toString() {
		return "<" + this.getName() + ">";
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof KnowledgeBase) {
			KnowledgeBase kb = (KnowledgeBase) o;
			return kb.getName().equals(this.getName());
		}
		return false;
	}

}
