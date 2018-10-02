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

import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.rule.InferenceRule;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;
import moonstone.rule.StructuredRuleExpansion;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.VUtils;

public class StructureGrammar extends Grammar {

	protected Vector<Annotation> newAgenda = null;
	protected Vector<Vector<Annotation>> annotationChart = null;

	public StructureGrammar(GrammarModule gmod, String name, String ruledir) {
		super(gmod, name, ruledir);
	}
	
	/**********************************************************
	 * Before 3/31/2015: New grammar with polynomial complexity.  I'm disabling it for
	 * right now because it is producing far too many annotations and taking forever to
	 * complete...

	public void run() {
		while (this.newAgenda != null) {
			Vector<Annotation> currentAgenda = this.newAgenda;
			this.newAgenda = null;
			for (Annotation annotation : currentAgenda) {
				expandAnnotation(annotation);
			}
		}
	}
	
	public void addAnnotation(Annotation annotation) {
		if (!duplicateAnnotationExists(annotation)) {
			String str = annotation.getString();
			
			this.annotationSignatureHash.put(annotation.getSignature(),
					annotation);
			this.allAnnotations = VUtils.add(this.allAnnotations, annotation);
			this.newAgenda = VUtils.add(this.newAgenda, annotation);
		} else {
			int x = 1;
			x = x;
		}
	}

	protected void expandAnnotation(Annotation annotation) {
		Vector<String> tokens = annotation.getIndexTokens();
		boolean processed = false;
		if (!processed && tokens != null) {
			for (String token : tokens) {
				Vector<Rule> rules = this
						.getExpandableRulesByFirstIndexToken(token);
				if (rules != null) {
					for (Rule rule : rules) {
						StructuredRuleExpansion newExpansion = new StructuredRuleExpansion(
								rule, annotation);
						processExpansion(newExpansion);
					}
				}
				String key = token + ":" + annotation.getTokenStart();
				Vector<RuleExpansion> expansions = ruleExpansionHash.get(key);
				if (expansions != null) {
					for (RuleExpansion expansion : expansions) {
						StructuredRuleExpansion newExpansion = new StructuredRuleExpansion(
								(StructuredRuleExpansion) expansion, annotation);
						processExpansion(newExpansion);
					}
				}
			}
		}
	}

	protected void processExpansion(StructuredRuleExpansion newExpansion) {
		if (newExpansion.isFullyMatched()) {
			newExpansion.createAnnotation();
		} else {
			Annotation last = newExpansion.getLastMatchedAnnotation();
			// WHY ISN'T TOKEN "USED"?
			for (String token : last.getIndexTokens()) {
				int end = last.getTokenEnd() + 1;
				String key = token + ":" + end;
				VUtils.pushHashVector(this.ruleExpansionHash, key, newExpansion);
			}
		}
	}

	public Vector<Rule> getExpandableRulesByFirstIndexToken(Object key) {
		Vector<Rule> rules = null;
		if (key != null) {
			Vector<Rule> v = ruleTokenHash.get(key);
			if (v != null) {
				for (Rule rule : v) {
					if (!this.ruleIsInvalid(rule)
							&& !(rule instanceof moonstone.rule.LabelRule)
							&& !rule.isVisited()) {
						rules = VUtils.add(rules, rule);
						rule.setVisited(true);
					}
				}
			}
		}
		if (rules != null) {
			for (Rule rule : rules) {
				rule.setVisited(false);
			}
		}
		return rules;
	}

	public void addRule(Rule rule) {
		this.allRules = VUtils.add(this.allRules, rule);
		this.pushRuleHash(rule.getRuleType(), rule);
		this.pushRuleHash(rule.getRuleID(), rule);
		if (!(rule instanceof InferenceRule) && rule.getWordLists() != null) {
			Vector<String> words = rule.getWordLists().elementAt(0);
			for (String word : words) {
				this.pushRuleHash(word, rule);
			}
		}
	}

	public void initializeChart(int length) {
		this.newAgenda = null;
		this.annotationChart = new Vector(0);
		for (int i = 0; i < length; i++) {
			this.annotationChart.add(new Vector<Annotation>(0));
		}
	}

	public void clearAnnotationStructures() {
		this.allAnnotations = null;
		this.ruleExpansionHash.clear();
		this.invalidatedRuleHash.clear();
		this.annotationSignatureHash.clear();
		this.newAgenda = null;
		this.annotationChart = null;
	}
	
	**********************************************************/

}
