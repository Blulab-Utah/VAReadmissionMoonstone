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
package moonstone.grammar;

import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.utilities.VUtils;
import moonstone.annotation.Annotation;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;
import tsl.expression.term.constant.StringConstant;

public class DocumentGrammar extends Grammar {

	protected Vector<RuleExpansion> newAgenda = new Vector(0);
	private Hashtable ruleInputTokenHash = new Hashtable();

	public DocumentGrammar(GrammarModule gmod, String name, String ruledir) {
		super(gmod, name, ruledir);
	}

	public void storeRuleInputTokens() {
		if (this.getAllRules() != null) {
			for (Rule rule : this.getAllRules()) {
				if (rule.getPatternLists() != null) {
					for (Vector plist : rule.getPatternLists()) {
						for (Object po : plist) {
							this.ruleInputTokenHash.put(po, rule);
						}
					}
				}
			}
		}
	}

	public void run() {
		this.runRuleExpansions();
	}

	public void runRuleExpansions() {
		while (!this.newAgenda.isEmpty()) {
			Vector<RuleExpansion> currentAgenda = new Vector(this.newAgenda);
			this.newAgenda.clear();
			for (RuleExpansion expansion : currentAgenda) {
				Rule rule = expansion.getRule();
				if (rule.isDispositionExecuteActions()) {
					if (expansion.isValidPerRuleConstraints()) {
						for (Constraint c : expansion.getRule().getActions()) {
							c.evalConstraint(expansion.getVariables());
						}
					}
				} else {
					expansion.createAnnotation();
				}
			}
		}
	}

	public void addAnnotation(Annotation annotation) {
		this.allAnnotations = VUtils.add(this.allAnnotations, annotation);
		this.expandAnnotation(annotation);
	}

	protected void processExpansion(RuleExpansion newExpansion) {
		if (newExpansion.isValid()) {
			Rule rule = newExpansion.getRule();
			if (newExpansion.isFullyMatched()) {
				this.newAgenda.add(newExpansion);
			} else {
				for (int i = 0; i < rule.getPatternListCount(); i++) {
					Annotation matchedAnnotation = newExpansion
							.getMatchedAnnotation(i);
					if (matchedAnnotation == null) {
						String key = rule.getRuleID() + ":" + i;
						// FROM PROFILER: THIS TAKES 20% OF THE TIME
						VUtils.pushHashVector(this.ruleExpansionHash, key,
								newExpansion);
					}
				}
			}
		}
	}

	public void clearAnnotationStructures() {
		this.allAnnotations = null;
		if (this.ruleExpansionHash != null) {
			this.ruleExpansionHash = new Hashtable();
			this.invalidatedRuleHash = new Hashtable();
			this.annotationSignatureHash = new Hashtable();
		}
		this.interpretedAnnotationChart = null;
		this.newAgenda = new Vector(0);

		// 11/11/2016
		this.newAgendas = null;
		this.displayedAnnotations = null;
	}

	public boolean annotationContainsRuleInput(Annotation annotation) {
		if (annotation.getConcept() != null) {
			return this.ruleInputTokenHash.get(annotation.getConcept()) != null;
		}
		return false;
	}

	public void addRule(Rule rule) {
		super.addRule(rule);
		this.addGateRuleEquivalentConcepts(rule);
	}

	public void addGateRuleEquivalentConcepts(Rule rule) {
		if (rule.getPatternListCount() == 1
				&& rule.getResultConcept() instanceof StringConstant) {
			StringConstant sc = (StringConstant) rule.getResultConcept();
			Vector sublist = rule.getPatternLists().elementAt(0);
			
			// 1/17/2018:  Add equivalence between pattern and concept
			sublist = new Vector(sublist);
			sublist.add(sc);

			for (Object o1 : sublist) {
				for (Object o2 : sublist) {
					if (o1 != o2 && o1 instanceof StringConstant
							&& o2 instanceof StringConstant) {
						StringConstant sc1 = (StringConstant) o1;
						StringConstant sc2 = (StringConstant) o2;
						VUtils.pushIfNotHashVector(
								gateRuleEquivalentConceptMap, sc1, sc2);
						VUtils.pushIfNotHashVector(
								gateRuleEquivalentConceptMap, sc2, sc1);
					}
				}
			}
		}
	}

	public boolean conceptsAreGateEquivalents(StringConstant sc1,
			StringConstant sc2) {
		if (sc1 != null && sc2 != null && sc1 != sc2) {
			Vector<StringConstant> scs = this.gateRuleEquivalentConceptMap
					.get(sc1);
			if (scs != null) {
				return scs.contains(sc2);
			}
		}
		return false;
	}

}
