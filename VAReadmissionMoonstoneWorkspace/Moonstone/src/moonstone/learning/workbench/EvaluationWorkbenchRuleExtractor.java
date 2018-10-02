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
package moonstone.learning.workbench;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.io.readmission.Readmission;
import moonstone.io.readmission.ReadmissionAnnotationInformationPacket;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Type;

public class EvaluationWorkbenchRuleExtractor {
	MoonstoneRuleInterface moonstoneRuleInterface = null;

	public EvaluationWorkbenchRuleExtractor(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
	}

	public void extractRulesFromWorkbenchAnnotations() {
		try {
			Vector<Rule> rules = null;
			MoonstoneRuleInterface msri = this.moonstoneRuleInterface;
			if (msri.getWorkbench() != null) {
				msri.getTentativeRuleHash().clear();
				TLisp tl = TLisp.getTLisp();
				Vector<workbench.api.annotation.Annotation> annotations = this
						.getWorkbenchAnnotations();
				if (annotations != null) {
					for (workbench.api.annotation.Annotation annotation : annotations) {
						KTAnnotation kta = annotation.getKtAnnotation();
						ReadmissionAnnotationInformationPacket packet = new ReadmissionAnnotationInformationPacket(
								msri.getReadmission(),
								annotation.getDocument(), kta);
						if (!packet.isRelevant) {
							continue;
						}
						String ruleid = "tentative" + Rule.getNumRules();
						String dstr = extractRuleDefinitionFromAnnotation(
								packet, annotation, ruleid);
						if (dstr != null) {
							Sexp sexp = (Sexp) tl.evalString(dstr);
							Vector<Vector> v = TLUtils
									.convertSexpToJVector(sexp);
							Vector wv = (Vector) VUtils.assoc("words", v);
							Vector<Vector> wlist = VUtils.rest(wv);
							Rule rule = msri.getTentativeRule(wlist);
							if (rule == null) {
								rule = new Rule(msri.getControl()
										.getSentenceGrammar(), sexp, v, wlist);
								msri.addTentativeRule(rule);
							}
						}
					}

				}
				msri.storeTentativeRules();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 12/18/2014: Removed the requirement of being referenced in UMLS
	public String extractRuleDefinitionFromAnnotation(
			ReadmissionAnnotationInformationPacket packet,
			workbench.api.annotation.Annotation annotation, String ruleid) {
		String dstr = null;
		Object concept = packet.MoonstoneConcept;
		String text = packet.MoonstoneSnippet.toLowerCase();
		Vector<Vector<String>> wordLists = null;
		String[] wstrs = text.split(" ");
		for (int i = 0; i < wstrs.length; i++) {
			String wstr = wstrs[i].trim();
			if (this.wordIsPatternRelevant(wstr)) {
				Vector<String> wlst = VUtils.listify(wstr);
				wordLists = VUtils.add(wordLists, wlst);
			}
		}
		if (wstrs.length == 1) {
			int x = 1;
		}
		if (wordLists != null
				&& (wordLists.size() > 1 || wordLists.size() == wstrs.length)) {
			StringBuffer sb = new StringBuffer();
			sb.append("'( (ruleid " + ruleid + ") ");
			sb.append("(concept \"" + concept + "\") ");
			sb.append("(snippet \"" + packet.MoonstoneSnippet + "\") ");
			sb.append("(words ");
			for (Vector<String> wlst : wordLists) {
				sb.append(" (");
				for (String word : wlst) {
					sb.append("\"" + word + "\" ");
				}
				sb.append(")");
			}
			sb.append(") ");
			Hashtable<String, String> phash = new Hashtable();
			Vector<Attribute> attributes = annotation.getAllAttributes();
			if (attributes != null) {
				for (Attribute a : attributes) {
					String value = (String) annotation.getAttributeValue(a);
					String aname = a.getName();
					phash.put(aname, value);
				}
			}
			if (!phash.isEmpty()) {
				sb.append("(properties ");
				for (Enumeration<String> e = phash.keys(); e.hasMoreElements();) {
					String aname = e.nextElement();
					String value = phash.get(aname);
					sb.append("(\"" + aname + "\" \"" + value + "\") ");
				}
				sb.append(") ");
			}
			sb.append("(permit-interstitial true) ");
			sb.append(") ");
			dstr = sb.toString();
			try {
				Sexp sexp = (Sexp) TLisp.getTLisp().evalString(dstr);
				if (sexp != null) {
					dstr = sexp.toNewlinedString(0);
					dstr = "'" + dstr;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dstr;
	}

	public Vector<workbench.api.annotation.Annotation> getWorkbenchAnnotations() {
		Vector<workbench.api.annotation.Annotation> annotations = null;
		if (this.moonstoneRuleInterface.getWorkbench() != null) {
			TLisp tl = TLisp.getTLisp();
			int ruleIndex = 0;
			Vector<AnnotationCollection> acs = this.moonstoneRuleInterface
					.getWorkbench().getAnalysis().getAllAnnotationCollections();
			for (AnnotationCollection ac : acs) {
				if (ac.isPrimary()) {
					annotations = VUtils.append(annotations,
							ac.getAnnotations());
				}
			}
		}
		return annotations;
	}

	Vector<String> wordsToIgnore = VUtils.arrayToVector(new String[] { "the",
			"a", "and", "or", "then", "comment", "comments", "additional",
			"date", "is", "was", "were", "does", });

	private boolean wordIsPatternRelevant(String wstr) {
		if ("no".equals(wstr)) {
			int x = 1;
		}
		if (wstr.length() < 2) {
			return false;
		}
		Word word = Lexicon.currentLexicon.getWord(wstr);
		if (word != null) {
			if (word.isConjunct() || word.isPrep()) {
				return false;
			}
		} else {
			for (int i = 0; i < wstr.length(); i++) {
				char c = wstr.charAt(i);
				if (!Character.isLetter(c)) {
					return false;
				}
			}
		}
		if (wordsToIgnore.contains(wstr)) {
			return false;
		}
		return true;
	}

	// && !(word.isDet() || word.isPrep() || word.isModal() || word
	// .isAux())) {
}
