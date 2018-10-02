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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.NarrativeAnnotation;
import moonstone.io.readmission.Readmission;
import moonstone.learning.feature.FeatureSet;
import moonstone.rule.Rule;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.startup.StartupParameters;
import tsl.tsllisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class NarrativeGrammar extends Grammar {

	public NarrativeGrammar(GrammarModule gmod, String name, String ruledir) {
		super(gmod, name, ruledir);
	}

	public NarrativeGrammar(Grammar parent, String name) {
		super(parent, name);
	}

	public void readRules() {
		super.readRules();
		this.readWordGrammarRuleFile();
	}

	// 7/13/2016
	public void readWordGrammarRuleFile() {
		try {
			KnowledgeBase kb = this.getKnowledgeEngine()
					.getCurrentKnowledgeBase();
			StartupParameters sp = this.getKnowledgeEngine()
					.getStartupParameters();
			String fname = sp.getResourceFileName(WordGrammarRuleFileName);
			if (fname != null && (new File(fname)).exists()) {
				File f = new File(fname);
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = null;
				while ((line = in.readLine()) != null) {
					if (line.length() < 6 || line.charAt(0) == '#') {
						continue;
					}
					String[] parts = line.split("\\|");
					if (parts.length != 4) {
						continue;
					}
					String wordstr = parts[0];
					String conceptstr = parts[1];
					String typestr = parts[2];
					String ruleid = parts[3];

					if (ruleid.toLowerCase().contains("bullet")) {
						int x = 2;
					}
					Rule newrule = new Rule();
					Vector<String> words = StrUtils.stringList(wordstr, ",");
					Vector<Vector<String>> embedded = VUtils.listify(words);
					Vector<Vector> patternLists = newrule
							.extractEmbeddedPatternConstants(embedded);
					Object concept = Constant.extractConstant(kb, conceptstr);
					TypeConstant type = TypeConstant
							.createTypeConstant(typestr);
					Sexp sexp = newrule.toSexp(false);
					if (ruleid != null && type != null && concept != null
							&& patternLists != null && sexp != null) {
						newrule.setRuleID(ruleid);
						newrule.setKnowledgeBase(kb);
						newrule.setType(type);
						newrule.setResultConcept(concept);
						newrule.setPatternLists(patternLists);
						newrule.setSexp(sexp);
						this.addRule(newrule);
					} else {
						System.out.printf(
								"Unable to create grammar rule for %s\n", line);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeWordGrammarRuleFile() {
		StringBuffer sb = new StringBuffer();
		Vector<String> lines = null;
		for (Rule rule : this.getAllRules()) {
			String line = "";
			if (rule.isSingletonAllWordInput()
					&& rule.getResultConcept() instanceof StringConstant) {
				Vector<String> words = rule.getPatternLists().firstElement();
				Collections.sort(words);
				String wstr = StrUtils.stringListConcat(words, ",");
				line += wstr + "|";
				StringConstant concept = (StringConstant) rule
						.getResultConcept();
				TypeConstant type = rule.getType();
				if (type == null) {
					type = concept.getType();
				}
				if (concept != null && type != null) {
					line += concept.getFormalName() + "|";
					line += type.getFormalName() + "|";
					line += rule.getRuleID();
					lines = VUtils.add(lines, line);
				}
			}
		}
		if (lines != null) {
			Collections.sort(lines);
			for (String line : lines) {
				sb.append(line + "\n");
			}
			StartupParameters sp = this.getKnowledgeEngine()
					.getStartupParameters();
			String fname = sp.getPropertyValue(WordGrammarRuleFileName);
			if (fname != null) {
				String fullname = sp.getResourceDirectory()
						+ File.separatorChar + fname;
				FUtils.writeFile(fullname, sb.toString());
			}
		}
	}
	
	public Vector<Annotation> gatherDocumentGrammarInput(Vector<Annotation> annotations, Vector<Annotation> nonnested) {
		Readmission rm = this.getGrammarModule().getMoonstoneRuleInterface().getReadmission();
		Vector<Annotation> input = null;
		Hashtable<Annotation, Annotation> ihash = new Hashtable();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Rule rule = annotation.getRule();
				if (rule == null || rule.isCaptureRule()) {
					continue;
				}

				// 11/30/2017: If annotation is negated, don't assume that we
				// can use its positive target sub-annotations.
				// 12/7/2017: Moved to Annotation.gatherNonconfictingNestedTargetAnnotations()
				// if (annotation.isNegated()) {
				// continue;
				// }

				// 6/21/2017: PROBLEM: Moonstone has been skipping nested
				// meanings like
				// "lives at home with his wife"
				// Need to fix...
				Vector<Annotation> tas = annotation.getTopmostNestedTargetAnnotations();

				// 6/22/2017
				tas = annotation.getNestedTargetAnnotations();

				// 12/1/2017
				tas = Annotation.getNonNegativeNestedTargetAnnotations(
						this.getGrammarModule().getMoonstoneRuleInterface(), tas);

				// 12/7/2017
				tas = annotation.gatherNonconflictingNestedRelevantAnnotations();

				if (tas != null) {
					for (Annotation ta : tas) {
						ihash.put(ta, ta);
					}
				} else if (FeatureSet.annotationIsRelevant(annotation)) {
					annotation.setInferredTargetConcept();
					if (annotation.containsTargetConcept()) {
						ihash.put(annotation, annotation);
					}
				}
			}
		}
		// 1/15/2018:  Don't use subprases..
		boolean useAllExistingPhrases = false;
		if (useAllExistingPhrases && ihash.isEmpty()) {
			Vector<Annotation> nntargets = Annotation.gatherAnnotationsContainingTargetConcept(nonnested);
			Vector<Annotation> tas = Annotation.getNonCoincidingAnnotations(nntargets, true);
			if (tas != null) {
				for (Annotation ta : tas) {
					ihash.put(ta, ta);
				}
			}
		}
		return HUtils.getKeys(ihash);
	}

	public Vector<Annotation> gatherDocumentGrammarInput_Before_12_11_2017(
			Vector<Annotation> annotations) {
		Readmission rm = this.getGrammarModule().getMoonstoneRuleInterface()
				.getReadmission();
		Vector<Annotation> input = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Rule rule = annotation.getRule();
				if (rule == null || rule.isCaptureRule()) {
					continue;
				}

				// 11/30/2017: If annotation is negated, don't assume that we
				// can
				// use its positive target sub-annotations.
				if (annotation.isNegated()) {
					continue;
				}

				// 6/21/2017: PROBLEM: Moonstone has been skipping nested
				// meanings like
				// "lives at home with his wife"
				// Need to fix...
				Vector<Annotation> tas = annotation
						.getTopmostNestedTargetAnnotations();

				// 6/22/2017
				tas = annotation.getNestedTargetAnnotations();

				// 12/1/2017
				tas = Annotation.getNonNegativeNestedTargetAnnotations(this
						.getGrammarModule().getMoonstoneRuleInterface(), tas);

				if (tas != null) {
					input = VUtils.append(input, tas);
				} else if (FeatureSet.annotationIsRelevant(annotation)) {
					annotation.setInferredTargetConcept();
					input = VUtils.add(input, annotation);
				}
			}
		}
		return input;
	}

	public Vector<Annotation> gatherDocumentGrammarInput_BEFORE_6_21_2017(
			Vector<Annotation> annotations) {
		Vector<Annotation> input = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Rule rule = annotation.getRule();
				if (rule == null || rule.isCaptureRule()) {
					continue;
				}
				Vector<Annotation> tas = annotation
						.getTopmostNestedTargetAnnotations();
				if (tas != null) {
					input = VUtils.append(input, tas);
				} else if (FeatureSet.annotationIsRelevant(annotation)) {
					annotation.setInferredTargetConcept();
					input = VUtils.add(input, annotation);
				}
			}
		}
		return input;
	}

}
