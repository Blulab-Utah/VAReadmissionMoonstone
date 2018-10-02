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
package moonstone.learning.basilisk;

import java.util.Vector;
import tsl.utilities.ObjectInfoWrapper;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;
import moonstone.rule.Rule;

public class ExtractionPattern extends Rule {
	protected Rule sourceRule = null;
	protected Vector<ObjectInfoWrapper> wordCountWrappers = null;
	protected float totalWordCount = 0;
	protected String signature = null;

	protected static Vector<String> ValidRuleIDs = VUtils
			.arrayToVector(new String[] { "transitive-verb-phrase-rule", "np-vp-s-rule"});

	public ExtractionPattern(Annotation annotation, Vector<Annotation> anchors) {
		Rule rule = annotation.getRule();
		this.sourceRule = rule;
		this.setPhraseType(annotation.getPhraseType());
		this.signature = createSignature(annotation, anchors);
		Vector<Vector> newPatternLists = null;
		for (int i = 0; i < rule.getPatternListCount(); i++) {
			Annotation child = annotation.getLexicallySortedSourceAnnotations()
					.elementAt(i);
			Vector<String> wlst = rule.getPatternLists().elementAt(i);
			if (!anchors.contains(child)) {
				wlst = new Vector<String>(0);
				String text = StrUtils.trimAllWhiteSpace(child.getString()
						.toLowerCase());
				wlst.add(text);
			} else {
				wlst = VUtils.listify(wlst.firstElement());
			}
			newPatternLists = VUtils.add(newPatternLists, wlst);
		}
		this.setPatternLists(newPatternLists);
	}

	// RLogF reflects the tendency of an EP to extract a target set of words.
	// When
	// we use bootstrapping to construct a semantic lexicon ala Thelen&Riloff,
	// the
	// words parameter is the entire lexicon at the current bootstrapping
	// iteration,
	// and RLogF reflects the tendency of that EP to extract those words in
	// particular, /
	// as opposed to all words it might extract.
	public float calculateRLogF(Vector<String> words) {
		double targetExtractedCount = 0;
		for (String word : words) {
			double count = ObjectInfoWrapper.getCount(
					this.wordCountWrappers, word);
			targetExtractedCount += count;
		}
		double score = (targetExtractedCount / this.totalWordCount)
				* Math.log(targetExtractedCount);
		return new Float(score).floatValue();
	}

	// Two EPs are the same if they have the same ptoken.
	public boolean equals(Object o) {
		if (o instanceof ExtractionPattern) {
			ExtractionPattern ep = (ExtractionPattern) o;
			return this.signature.equals(ep.signature);
		}
		return false;
	}

	public static String createSignature(Annotation annotation,
			Vector<Annotation> anchors) {
		StringBuffer sb = new StringBuffer("[");
		Rule rule = annotation.getRule();
		sb.append(rule.getRuleID());
		sb.append(":");
		for (int i = 0; i < rule.getPatternListCount(); i++) {
			Vector<String> wlst = rule.getPatternLists().elementAt(i);
			Annotation child = annotation.getLexicallySortedSourceAnnotations()
					.elementAt(i);
			String text = StrUtils.trimAllWhiteSpace(child.getString()
					.toLowerCase());
			sb.append(wlst.firstElement());
			String value = "*";
			if (!anchors.contains(child)) {
				value = text;
			}
			sb.append("=" + value);
			if (i < rule.getPatternListCount() - 1) {
				sb.append("&");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public static boolean canExtractTypePattern(Annotation annotation) {
		if (annotation.getRule() != null
				&& !annotation.isTerminal()
				&& !annotation.getRule().isVisited()
				&& annotation.getRule().getRuleID() != null
				&& ExtractionPattern.ValidRuleIDs.contains(annotation.getRule()
						.getRuleID()) && annotation.getChildAnnotations().size() >= 2
				&& !annotationContainsConjunct(annotation)) {
			boolean foundanchor = false;
			boolean foundcontext = false;
			for (Annotation cannotation : annotation.getChildAnnotations()) {
				if (cannotation.getTokenLength() > 4) {
					return false;
				}
				if (canUseAsAnchor(cannotation)) {
					if (!foundanchor) {
						foundanchor = true;
					} else {
						foundcontext = true;
					}
				}
			}
			return foundanchor && foundcontext;
		}
		return false;
	}

	public static boolean canExtractRelationPattern(Annotation annotation) {
		if (annotation.getRule() != null
				&& !annotation.isTerminal()
				&& !annotation.getRule().isVisited()
				&& annotation.getRule().getRuleID() != null
				&& ExtractionPattern.ValidRuleIDs.contains(annotation.getRule()
						.getRuleID()) && annotation.getChildAnnotations().size() >= 3
				&& !annotationContainsConjunct(annotation)
				&& canUseAsAnchor(annotation.getChildAnnotations().firstElement())
				&& canUseAsAnchor(annotation.getChildAnnotations().lastElement())) {
			return true;
		}
		return false;
	}

	private static String[] ConjunctStrings = { "and", "or" };

	private static boolean annotationContainsConjunct(Annotation annotation) {
		for (int i = 0; i < ConjunctStrings.length; i++) {
			String cstr = ConjunctStrings[i];
			if (annotation.getString().contains(cstr)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canUseAsAnchor(Annotation annotation) {
		return "#NP#".equals(annotation.getPhraseType())
				|| "#AP#".equals(annotation.getPhraseType());
	}

	public String toString() {
		String str = "<EP: Words=" + this.getPatternLists() + ">";
		return str;
	}

}
