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
package moonstone.semantic;

import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.AnnotationIntegrationMaps;
import moonstone.annotation.StructureAnnotation;
import moonstone.javafunction.JavaFunctions;
import moonstone.learning.ebl.GrammarEBL;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.lexicon.Word;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.relation.PatternRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.forwardchaining.ForwardChainingInferenceEngine;
import tsl.information.TSLInformation;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Interpretation extends Term {
	protected Annotation annotation = null;
	protected Vector<RelationSentence> relationSentences = null;
	protected Vector<RelationSentence> inferredRelationSentences = null;
	protected String macro = null;
	protected Object extendedConcept = null;
	private boolean invokedInference = false;

	public static Interpretation create(Annotation annotation, String cui,
			Object concept, String macro, Rule rule, TypeConstant type) {
		return new Interpretation(annotation, cui, concept, macro, rule, type);
	}

	// 7/18/2014 NOTES: Need to clean this up. Store cuis, etc, in the property
	// tables only. E.g.
	// (properties (gtype "<PROCEDURE>") (concept (concept ?0)) (cui (cui ?0))
	// (degree "high") �)
	// Add list of properties for TokenIndexes, and write method that iterates
	// over that list and adds those present
	// in the current annotation's property list.
	// (Make backup of current code; devote one day to this.)

	public Interpretation(Annotation annotation) {
		this.annotation = annotation;
	}

	public Interpretation(Annotation annotation, String cui, Object concept,
			String macro, Rule rule, TypeConstant type) {
		this(annotation);

		if (annotation.getRule() != null && annotation.getRule().isDoDebug()) {
			int x = 1;
		}
		this.concept = concept;
		this.cui = cui;
		this.macro = macro;
		this.type = type;
		findCui();
		findConcept();
		findExtendedConcept();
		findMacro();
		findType();

		setPropertiesFromRule();

		// Added this 6/25/2016, but dont think this is necessary. I just
		// didn't have a rule for DOES_NOT_LIVE_AT_HOME
		if (this.getAnnotation().getGrammar()
				.isTargetConcept(this.getConcept())) {
			this.setProperty("contains-target", true);
		}

		if (this.concept == null) {
			this.concept = this.properties.get("concept");
		}
	}

	public static void create(Annotation annotation, TypeConstant type) {
		if (annotation.getSemanticInterpretation() == null) {
			Interpretation si = new Interpretation(annotation,
					annotation.getCui(), annotation.getConcept(),
					annotation.getMacro(), annotation.getRule(), type);
			annotation.setSemanticInterpretation(si);
		}
	}

	public static Interpretation create(Annotation annotation, String cui,
			String concept, TypeConstant type) {
		return new Interpretation(annotation, cui, concept, null, null, type);
	}

	protected void findType() {
		if (this.type == null) {
			TypeConstant type = this.annotation.getRule().getType();
			Rule rule = this.annotation.getRule();
			if (type == null && rule != null && rule.getResultType() != null) {
				Object to = this.annotation.evalPatternRecursive(
						this.annotation.getRule().getResultType(),
						this.annotation.getVariables());
				if (to instanceof Term) {
					type = ((Term) to).getType();
				}
			}
			if (type == null && this.concept instanceof Constant) {
				type = ((Constant) this.concept).getType();
			}
			if (type == null) {
				int index = Variable.getPositionVariableIndex(this.annotation
						.getRule().getResultConcept());
				if (index >= 0) {
					Annotation child = this.annotation.getLexicalChild(index);
					if (child != null) {
						type = child.getType();
					}
				}
			}
			this.setType(type);
		}
		this.setProperty("type", this.type);
	}

	// Before 4/7/2018
	// protected void findType() {
	// if (this.type == null) {
	// TypeConstant type = this.annotation.getRule().getType();
	// if (type == null && this.concept instanceof Constant) {
	// type = ((Constant) this.concept).getType();
	// }
	// if (type == null) {
	// int index =
	// Variable.getPositionVariableIndex(this.annotation.getRule().getResultConcept());
	// if (index >= 0) {
	// Annotation child = this.annotation.getLexicalChild(index);
	// if (child != null) {
	// type = child.getType();
	// }
	// }
	// }
	// this.setType(type);
	// }
	// this.setProperty("type", type);
	// }

	protected void findConcept() {
		if (this.getConcept() == null) {
			Object co = annotation.getRule().getResultConcept();
			Object concept = this.annotation.evalPatternRecursive(co,
					this.annotation.getVariables());
			if (concept instanceof Annotation) {
				concept = ((Annotation) concept).getConcept();
			}
			if (concept == null) {
				String cui = annotation.getRule().getResultCUI();
				concept = AnnotationIntegrationMaps.getName(cui, null);
			}

			if (concept == null) {
				Object o = annotation
						.getPropertyFromRuleOrSourceAnnotations("concept");
				if (o instanceof Annotation) {
					concept = ((Annotation) o).getConcept();
				} else if (o instanceof String) {
					concept = (String) o;
				}
			}
			this.setConcept(concept);
		}
		this.setProperty("concept", this.getConcept());
	}

	protected void findMacro() {
		if (this.getMacro() == null && this.getAnnotation().getRule() != null) {
			this.macro = this.getAnnotation().getRule().getResultMacro();
		}
		this.setProperty("macro", this.getMacro());
	}

	protected void findCui() {
		if (this.getCui() == null) {
			String cui = null;
			String cstr = annotation.getRule().getResultCUI();
			if (Variable.isVariable(cstr)) {
				cui = (String) this.annotation.evalPattern(cstr,
						this.annotation.getVariables());
			} else if (cstr != null) {
				cui = cstr.toUpperCase();
			}
			if (cui == null) {
				findConcept();
				Object concept = this.getConcept();
				if (concept != null) {
					cui = AnnotationIntegrationMaps.getCUI(concept.toString());
				}
			}
			if (cui == null) {
				cui = (String) annotation
						.getPropertyFromRuleOrSourceAnnotations("cui");
			}
			this.setCui(cui);
		}
		this.setProperty("cui", this.getCui());
	}

	// 6/28/2016
	protected void findExtendedConcept() {
		if (this.extendedConcept == null) {
			Rule rule = this.annotation.getRule();
			Vector<Constant> ecv = null;
			if (rule != null && this.annotation.hasChildren()) {
				for (Annotation child : this.annotation
						.getTextuallySortedSourceAnnotations()) {
					Object cec = child.getExtendedConcept();
					if (cec instanceof Constant) {
						ecv = VUtils.add(ecv, cec);
					} else if (child.getConcept() instanceof Constant) {
						ecv = VUtils.add(ecv, child.getConcept());
					}
				}
				if (ecv != null) {
					TypeConstant root = rule.getGrammar().getGrammarModule()
							.getKnowledgeEngine().getCurrentOntology()
							.getRootType();
					String str = StrUtils.stringListConcat(ecv, "");
					StringConstant sc = StringConstant.createStringConstant(
							str, root, true);
					this.extendedConcept = sc;
				}
			}
		}
		if (this.extendedConcept == null && this.concept instanceof Constant) {
			this.extendedConcept = this.concept;
		}
	}

	public Object getExtendedConcept() {
		return this.extendedConcept;
	}

	public static Interpretation unify(Annotation annotation,
			Interpretation s1, Interpretation s2) {
		Interpretation sinterp = null;
		if (s1 == null) {
			return s2;
		}
		if (s2 == null) {
			return s1;
		}
		Vector<Word> words = VUtils.appendNew(s1.getAnnotation()
				.getCoveredWords(), s2.getAnnotation().getCoveredWords());
		CUIStructureShort cp = UMLSStructuresShort.getUMLSStructures()
				.getCoveringCuiStructure(words);
		if (cp != null) {
			sinterp = new Interpretation(annotation, cp.getCui(),
					cp.getWordString(false), null, null, cp.getType());
		}
		return sinterp;
	}

	private void setPropertiesFromRule() {
		int x = 0;
		Rule rule = this.annotation.getRule();
		if (rule != null) {
			this.setProperty("ruleid", rule.getRuleID());
			if (rule.getPropertyPredicates() != null) {
				for (Vector pp : rule.getPropertyPredicates()) {
					String aname = (String) pp.firstElement();
					Object value = this.annotation.evalPatternRecursive(
							pp.elementAt(1), this.annotation.getVariables());
					Interpretation si = this;
					if (!rule.isPropertyToRemove(aname)) {
						si.setProperty(aname, value);
					}
				}
			}
		}
		if (TSLInformation.getRelevantFeatures() != null) {
			for (int i = 0; i < TSLInformation.getRelevantFeatures().length; i++) {
				String attribute = TSLInformation.getRelevantFeatures()[i];
				if (this.getProperty(attribute) == null && !rule.isPropertyToRemove(attribute)) {
					Object value = this.annotation
							.getPropertyFromRuleOrSourceAnnotations(attribute);
					this.setProperty(attribute, value);
					
					if (rule.isDoDebug()) {
						x = 1;
					}
				}
			}
		}

		// 8/26/2015: If annotation is specialization, copy all un-populated
		// properties.
		x = 1;
		if (rule != null && rule.hasSubpattern()) {
			Interpretation source = annotation.getLexicalChild(0)
					.getSemanticInterpretation();
			for (String property : source.getPropertyNames()) {
				if (!rule.isPropertyToRemove(property)
						&& this.getProperty(property) == null) {
					this.setProperty(property, source.getProperty(property));
					
					if (rule.isDoDebug()) {
						x = 1;
					}
				}
			}
		}
	}

	// BEFORE 3/26/2015: Now using Expression.evalPattern().
	// Permit properties of child annotations to be set. If length = 2, select
	// current annotation; otherwise identify annotation to set property of.

	// private void setPropertiesFromRule() {
	// Rule rule = this.annotation.getRule();
	// if (rule != null) {
	// this.setProperty("ruleid", rule.getRuleID());
	// if (rule.getPropertyPredicates() != null) {
	// for (Vector pp : rule.getPropertyPredicates()) {
	// String aname = (String) pp.firstElement();
	//
	// Object value = null;
	// SemanticInterpretation si = this;
	// // 12/1/2014: Permit assignment to other annotations.
	// if (pp.size() == 3) {
	// // NOTE: Doesn't work, because property predicates only
	// // contain the first and last
	// // pattern elements.
	// Object o = this.annotation.evalPattern(pp.elementAt(1),
	// this.annotation
	// .getLexicallySortedSourceAnnotations());
	// if (o instanceof Annotation) {
	// Annotation other = (Annotation) o;
	// if (other.getSemanticInterpretation() != null) {
	// si = other.getSemanticInterpretation();
	// }
	// }
	// }
	// value = pp.lastElement();
	// if (value instanceof Constraint
	// && si.annotation.getVariables() != null) {
	// Constraint pt = (Constraint) value;
	// value = pt.evalConstraint(si.annotation.getVariables());
	// } else if (value instanceof Variable) {
	// Variable var = (Variable) value;
	// value = var.getValue();
	// } else {
	// value = si.annotation.evalPattern(value, si.annotation
	// .getLexicallySortedSourceAnnotations());
	// }
	// si.setProperty(aname, value);
	// }
	// }
	// }
	// if (TSLInformation.getRelevantFeatures() != null) {
	// for (int i = 0; i < TSLInformation.getRelevantFeatures().length; i++) {
	// String attribute = TSLInformation.getRelevantFeatures()[i];
	// if (this.getProperty(attribute) == null) {
	// Object value = this.annotation
	// .getPropertyFromRuleOrSourceAnnotations(attribute);
	// this.setProperty(attribute, value);
	// }
	// }
	// }
	// }

	public String toString() {
		String str = "<SI:" + this.getAnnotation() + ">";
		return str;
	}

	public String getCommaDelimitedPredString() {
		Vector<String> preds = getPropertyPredicates();
		String pstr = null;
		if (preds != null) {
			pstr = "";
			for (int i = 0; i < preds.size(); i++) {
				pstr += preds.elementAt(i);
				if (i < preds.size() - 1) {
					pstr += ",";
				}
			}
		}
		return pstr;
	}

	public Vector<String> getPropertyPredicates() {
		Vector<String> pnames = this.getPropertyNames();
		Vector<String> preds = null;
		String[] noPrintProperties = new String[] { "localvar", "ruleid",
				"type" };
		if (pnames != null) {
			for (String pname : pnames) {
				if (VUtils.containedIn(pname, noPrintProperties)) {
					continue;
				}
				Object value = this.getProperty(pname);
				Object vstr = "*";
				if (!(value instanceof Annotation)) {
					vstr = value.toString();
				} else {
					Annotation va = (Annotation) value;
					if (va.getConcept() != null) {
						vstr = va.getConcept();
					}
				}
				String pred = pname + "=" + vstr;
				preds = VUtils.add(preds, pred);
			}
		}
		return preds;
	}

	public static Interpretation unify(Interpretation si1, Interpretation si2) {
		Interpretation newsi = null;
		if (semanticInterpretationsAreUnifiable(si1, si2)) {
			newsi = new Interpretation(null);
			newsi.setConcept(si1.getConcept());
			newsi.setCui(si1.getCui());
			newsi.setMacro(si1.getMacro());
			newsi.setType(si1.getType());
			newsi.setProperties(TSLInformation.unifyProperties(si1, si2));
		}
		return newsi;
	}

	public static boolean semanticInterpretationsAreUnifiable(
			Interpretation si1, Interpretation si2) {
		if (!(si1.getConcept() == null && si2.getConcept() == null)
				|| si1.getConcept().equals(si2.getConcept())) {
			return false;
		}
		if (!(si1.getCUI() == null && si2.getCUI() == null)
				|| si1.getCUI().equals(si2.getCUI())) {
			return false;
		}
		if (!(si1.getMacro() == null && si2.getMacro() == null)
				|| si1.getMacro().equals(si2.getMacro())) {
			return false;
		}
		if (!(si1.getType() == null && si2.getType() == null)
				|| si1.getType().equals(si2.getType())) {
			return false;
		}
		if (!TSLInformation.propertiesAreUnifiable(si1, si2)) {
			return false;
		}
		return true;
	}

	// 7/22/2016
	public String getTuffyString() {
		Vector<RelationSentence> rsents = this.getRelationSentences();
		StringBuffer sb = new StringBuffer();
		for (RelationSentence rsent : rsents) {
			if (this.annotation.getMoonstoneRuleInterface()
					.isValidTuffyRelationName(rsent.getRelation().getName())) {
				String tstr = rsent.getTuffyString();
				sb.append(tstr + "\n");
			}
		}
		return sb.toString();
	}

	public Vector<RelationSentence> getRelationSentences(
			Vector<Annotation> annotations) {
		Vector<RelationSentence> allRelations = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<RelationSentence> v = annotation
						.getSemanticInterpretation().getRelationSentences();
				allRelations = VUtils.append(allRelations, v);
			}
		}
		return allRelations;
	}

	public Vector<RelationSentence> getRelationSentences() {
		return getRelationSentences(false);
	}

	public Vector<RelationSentence> getRelationSentences(boolean ruleonly) {
		if (this.relationSentences == null
				&& !(this.annotation instanceof StructureAnnotation)) {
			String pname = this.getAnnotation().getDocument().getPatientName();
			ObjectConstant patient = new ObjectConstant(pname);
			Vector<RelationSentence> rsents = null;
			Annotation annotation = this.getAnnotation();
			RelationSentence rs = null;
			Rule rule = annotation.getRule();
			if (rule != null && rule.getSemanticRelations() != null) {
				for (PatternRelationSentence prs : rule.getSemanticRelations()) {
					Vector allTerms = VUtils.listify(prs.getRelation()
							.getName());
					for (int i = 0; i < prs.getTermCount(); i++) {
						Object o = prs.getTerm(i);
						Object value = annotation.evalPattern(o,
								annotation.getVariables());
						if (value != null) {
							value = Term.wrapTerm(value);
						}
						allTerms = VUtils.add(allTerms, value);
					}
					if (!VUtils.containsNull(allTerms)) {
						rs = new RelationSentence(allTerms);
						rsents = VUtils.add(rsents, rs);
					}
				}
			}
			if (!ruleonly) {
				for (String property : this.getPropertyNames()) {
					if (property.contains("rule")) {
						continue;
					}
					Object value = this.getProperty(property);
					Term modifier = Term.wrapTerm(value);
					if ("concept".equals(property)
							&& !(value instanceof Annotation)) {
						String sid = "S_"
								+ this.annotation.getSentenceAnnotation()
										.getNumericID();
						Term sidterm = Term.wrapTerm(sid);
						Term pnameterm = Term.wrapTerm("P_" + pname);
						int dateIndex = this.annotation.getDocument()
								.getAdmitDateRangeIndex();
						int fileTypeIndex = this.annotation.getDocument()
								.getGeneralDictationType();
						Term dateterm = Term.wrapTerm(dateIndex);
						Term ftypeterm = Term.wrapTerm(fileTypeIndex);
						Vector args = VUtils.listify(this.getAnnotation(),
								modifier, sidterm, pnameterm, dateterm,
								ftypeterm);
						// 8/25/2016: Not sure if this is the best solution; I
						// need
						// to distinguish negated annotations in Tuffy...
						String rname = (JavaFunctions.neg(this.annotation) ? "negconcept"
								: "concept");
						rs = new RelationSentence(rname, args);
					} else {
						rs = new RelationSentence(property,
								this.getAnnotation(), modifier);
					}
					rsents = VUtils.add(rsents, rs);
				}
				if (annotation.getChildAnnotations() != null) {
					for (Annotation child : annotation.getChildAnnotations()) {
						if (child.isInterpreted()) {
							// rs = new RelationSentence("childof", annotation,
							// child);
							// rsents = VUtils.add(rsents, rs);
							Vector<RelationSentence> csents = child
									.getSemanticInterpretation()
									.getRelationSentences();
							rsents = VUtils.append(rsents, csents);
						}
					}
				}
			}
			this.relationSentences = rsents;
		}
		return this.relationSentences;
	}

	// 7/7/2016
	public Vector<RelationSentence> getInferredRelationSentences() {
		if (!this.invokedInference) {
			this.invokedInference = true;
			ForwardChainingInferenceEngine fcie = MoonstoneRuleInterface.RuleEditor
					.getForwardChainingInferenceEngine();
			this.inferredRelationSentences = (Vector<RelationSentence>) fcie
					.getAllInferredRelationSentences(this
							.getRelationSentences());
		}
		return this.inferredRelationSentences;
	}

	public Vector<RelationSentence> getAllRelationSentences() {
		this.getInferredRelationSentences();
		return VUtils.append(this.relationSentences,
				this.inferredRelationSentences);
	}

	public void setRelationSentences(Vector<RelationSentence> relationSentences) {
		this.relationSentences = relationSentences;
	}

	public void removeAllRelationSentences() {
		this.relationSentences = null;
		this.inferredRelationSentences = null;
		this.invokedInference = false;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public TypeConstant getGeneralType() {
		if (this.annotation.getRule() != null) {
			return this.annotation.getRule().getGeneralType();
		}
		return this.generalType;
	}

	public void setGeneralType(TypeConstant gtype) {
		this.generalType = gtype;
	}

	public String getMacro() {
		return this.macro;
	}

	public void setMacro(String macro) {
		this.macro = macro;
	}

	public boolean hasMacro() {
		return this.macro != null;
	}

	protected void printRelation(RelationSentence rs, String rid, int depth) {

	}

	public boolean isSourceLearned() {
		return "learned".equals(this.getProperty("source"));
	}

	// 7/7/2016
	public Object getInferredTargetConcept() {
		Vector<RelationSentence> isents = this.getInferredRelationSentences();
		if (isents != null) {
			for (RelationSentence isent : isents) {
				if (GrammarEBL.InferredTargetRelationName.equals(isent
						.getRelation().getName())) {
					return isent.getModifier();
				}
			}
		}
		return null;
	}

	public boolean isInvokedInference() {
		return invokedInference;
	}

}
