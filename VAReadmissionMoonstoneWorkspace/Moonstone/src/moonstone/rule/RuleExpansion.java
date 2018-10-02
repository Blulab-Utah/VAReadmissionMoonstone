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
package moonstone.rule;

import java.util.Comparator;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.NarrativeAnnotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.grammar.Grammar;
import moonstone.grammar.DocumentGrammar;
import moonstone.grammar.NarrativeGrammar;
import moonstone.grammar.StructureGrammar;
import moonstone.utility.ThreadUtils;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.variable.Variable;
import tsl.utilities.SeqUtils;
import tsl.utilities.VUtils;

public class RuleExpansion {

	protected Grammar grammar = null;
	protected Rule rule = null;
	public Annotation[] matchedAnnotations = null;
	protected boolean isFullyMatched = false;
	protected WordSequenceAnnotation sentenceAnnotation = null;
	protected boolean isValid = true;
	protected String invalidReason = null;
	protected int coveredWordTokenCount = 0;
	protected int totalWordTokenCount = 0;
	protected int wordTokenStart = 0;
	protected int wordTokenEnd = 0;
	protected float wordTokenCoverage = 0f;
	public Vector<Variable> variables = null;
	protected int matchCount = 0;

	private static String SameAnnotations = "SameAnnotations";
	public static String TooLarge = "TooLarge";
	public static String HasOverlap = "HasOverlap";
	public static String NotOrdered = "NotOrdered";
	public static String NotJuxtaposed = "NotJuxtaposed";
	public static String HasInterstitial = "HasInterstitial";
	public static String DifferentSentences = "DifferentSentences";

	public RuleExpansion() {

	}

	public RuleExpansion(Rule rule, Annotation matched, int index) {
		if (rule.isDoDebug()) {
			int x = 1;
			x = x;
		}
		this.setSentenceAnnotation(matched.getSentenceAnnotation());
		this.grammar = Grammar.CurrentGrammar;
		this.rule = rule;
		this.matchedAnnotations = new Annotation[rule.getPatternListCount()];
		this.matchedAnnotations[index] = matched;
		this.isFullyMatched = checkIsFullyMatched();
		this.calculateMatchCount();
		this.isValid = this.checkIsValid();
		if (!this.isValid) {
			if (rule.isDoDebug()) {
				int x = 1;
				// System.out.println("Invalid RuleExpansion: " + this);
			}
		} else {
			if (rule.isDoDebug()) {
				int x = 1;
				// System.out.println("Invalid RuleExpansion: " + this);
			}
		}
	}

	public RuleExpansion(RuleExpansion expansion, Annotation matched, int index) {
		if (expansion != null && expansion.rule.isDoDebug()) {
			int x = 1;
			x = x;
		}
		this.setSentenceAnnotation(matched.getSentenceAnnotation());
		this.grammar = Grammar.CurrentGrammar;
		this.rule = expansion.rule;
		this.matchedAnnotations = new Annotation[rule.getPatternListCount()];
		for (int i = 0; i < this.rule.getPatternListCount(); i++) {
			this.matchedAnnotations[i] = expansion.matchedAnnotations[i];
		}
		this.matchedAnnotations[index] = matched;
		this.isFullyMatched = checkIsFullyMatched();
		this.isValid = this.checkIsValid();
		if (this.rule.isDoDebug() && !this.isValid) {
			String reason = this.invalidReason;
			int x = 1;
		}
	}

	public Annotation createAnnotation() {
		Annotation annotation = null;
		if (this.isFullyMatched()) {
			Vector<Annotation> sources = VUtils
					.arrayToVector(this.matchedAnnotations);
			if (this.getGrammar() instanceof NarrativeGrammar) {
				int start = sources.firstElement().getTextStart();
				int end = sources.lastElement().getTextEnd();
				annotation = new NarrativeAnnotation(this.sentenceAnnotation,
						this.rule, sources, true);
			} else if (this.getGrammar() instanceof DocumentGrammar) {
				annotation = new moonstone.annotation.DocumentAnnotation(
						this.sentenceAnnotation, this.rule, sources, true);
			} else if (this.getGrammar() instanceof StructureGrammar) {
				annotation = new StructureAnnotation(this.sentenceAnnotation,
						this.rule, sources, true);
			}
			if (annotation.isValid()) {
				this.getGrammar().addAnnotation(annotation);
			} else {
				String reason = annotation.getInvalidReason();
			}
		}
		return annotation;
	}

	public boolean checkIsFullyMatched() {
		for (int i = 0; i < matchedAnnotations.length; i++) {
			if (matchedAnnotations[i] == null) {
				return false;
			}
		}
		return true;
	}

	public int calculateMatchCount() {
		int count = 0;
		for (int i = 0; i < matchedAnnotations.length; i++) {
			if (matchedAnnotations[i] != null) {
				this.matchCount++;
			}
		}
		return count;
	}

	public boolean isFullyMatched() {
		return isFullyMatched;
	}

	public Annotation getMatchedAnnotation(int index) {
		return this.matchedAnnotations[index];
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean checkIsValid() {
		Annotation last = null;
		Annotation current = null;
		int lowestStartIndex = 1000;
		int highestEndIndex = 0;
		int lastIndex = 0;
		int currentIndex = 0;
		Rule rule = this.getRule();

		if (rule.isDoDebug()) {
			if (this.isFullyMatched()) {
				int x = 1;
			}
			int x = 1;
			x = x;
		}

		if (rule.isSingleton()) {
			return true;
		}

		// NEW: 3/11/2016
		int totalAnnotationTokenCount = 0;

		for (int i = 0; i < this.matchedAnnotations.length; i++) {
			if (current != null) {
				last = current;
				lastIndex = currentIndex;
			}
			current = this.matchedAnnotations[i];
			currentIndex = i;
			boolean itemsJuxtaposed = (lastIndex == currentIndex - 1);
			if (current != null) {

				// 3/11/2016
				totalAnnotationTokenCount += current.getTokenLength();
				if (totalAnnotationTokenCount > rule.getWindow()) {
					this.invalidReason = TooLarge;
					return false;
				}

				if (last == current) {
					this.invalidReason = SameAnnotations;
					return false;
				}
				if (!(this.getGrammar() instanceof DocumentGrammar)) {
					if (current.getRelativeTokenStart() < lowestStartIndex) {
						lowestStartIndex = current.getRelativeTokenStart();
					}
					if (current.getRelativeTokenEnd() > highestEndIndex) {
						highestEndIndex = current.getRelativeTokenEnd();
					}

					// Removed 3/16/2016
					// if (highestEndIndex - lowestStartIndex >
					// rule.getWindow()) {
					// this.invalidReason = TooLarge;
					// return false;
					// }

					if (current != null && last != null) {

						if (!SeqUtils.disjoint(last.getRelativeTokenStart(),
								last.getRelativeTokenEnd(),
								current.getRelativeTokenStart(),
								current.getRelativeTokenEnd())) {
							this.invalidReason = HasOverlap;
							return false;
						}
						if (rule.isOrdered() || rule.isJuxtaposed()) {
							if (!(last.getRelativeTokenEnd() < current
									.getRelativeTokenStart())) {
								this.invalidReason = NotOrdered;
								return false;
							}
							if (itemsJuxtaposed) {
								if (rule.isDispositionCreateAnnotation()
										&& !rule.isPermitInterstitialAnnotations()
										&& this.grammar
												.interpretedAnnotationsExist(
														last.getRelativeTokenEnd() + 1,
														current.getRelativeTokenStart() - 1)) {
									this.invalidReason = HasInterstitial;
									return false;
								}
								if (rule.isJuxtaposed()) {
									if (last.getWordTokenEnd() != current
											.getWordTokenStart() - 1) {
										this.invalidReason = NotJuxtaposed;
										return false;
									}
								}
							}
						}
					}
				}
			}
		}
		if (rule.isDoDebug()) {
			int x = 1;
		}
		return true;
	}

	public Rule getRule() {
		return rule;
	}

	public WordSequenceAnnotation getSentenceAnnotation() {
		return sentenceAnnotation;
	}

	public void setSentenceAnnotation(WordSequenceAnnotation sentenceAnnotation) {
		this.sentenceAnnotation = sentenceAnnotation;
	}

	public String toString() {
		String str = "<" + this.getRule().getRuleID() + ":";
		for (int i = 0; i < this.matchedAnnotations.length; i++) {
			if (this.matchedAnnotations[i] != null) {
				str += this.matchedAnnotations[i].getText();
			} else {
				str += "*";
			}
			if (i < this.matchedAnnotations.length - 1) {
				str += ":";
			}
		}
		str += "(Start=" + this.wordTokenStart + ",End=" + this.wordTokenEnd
				+ ",%=" + this.wordTokenCoverage + ")";
		str += ">";
		return str;
	}

	public Grammar getGrammar() {
		return this.grammar;
	}

	public void calculateWordTokenValues() {
		int y = 1;
		int start = 1000000, end = -1;
		this.totalWordTokenCount = this.coveredWordTokenCount = this.wordTokenStart = this.wordTokenEnd = 0;
		for (Annotation matched : this.matchedAnnotations) {
			this.totalWordTokenCount += matched.getWordTokenCount();
			if (matched.getWordTokenStart() < start) {
				start = matched.getWordTokenStart();
			}
			if (matched.getWordTokenEnd() > end) {
				end = matched.getWordTokenEnd();
			}
		}
		this.wordTokenStart = start;
		this.wordTokenEnd = end;
		this.coveredWordTokenCount = (end - start) + 1;
		this.wordTokenCoverage = (float) this.totalWordTokenCount
				/ (float) this.coveredWordTokenCount;
	}

	public static class CoverageSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			RuleExpansion e1 = (RuleExpansion) o1;
			RuleExpansion e2 = (RuleExpansion) o2;
			float c1 = e1.wordTokenCoverage;
			float c2 = e2.wordTokenCoverage;
			if (c1 > c2) {
				return -1;
			}
			if (c1 < c2) {
				return 1;
			}
			return 0;
		}
	}

	public int getCoveredWordTokenCount() {
		return this.coveredWordTokenCount;
	}

	public int getTotalWordTokenCount() {
		return this.totalWordTokenCount;
	}

	public int getWordTokenStart() {
		return wordTokenStart;
	}

	public int getWordTokenEnd() {
		return wordTokenEnd;
	}

	public float getWordTokenCoverage() {
		return wordTokenCoverage;
	}

	public boolean isValidPerRuleConstraints() {
		if (this.variables == null) {
			this.variables = Annotation.gatherPositionalPhraseVariables(VUtils
					.arrayToVector(this.matchedAnnotations));
			Variable var = new Variable("?rule", rule);
			this.variables.add(var);

			var = new Variable("?sentence");
			var.bind(this.getSentenceAnnotation());
			this.variables.add(var);
		}
		if (this.getRule().getTestPredicates() != null) {
			for (Constraint c : this.getRule().getTestPredicates()) {
				Boolean rv = (Boolean) c.evalConstraint(this.variables);
				if (rv == null || !rv) {
					this.invalidReason = Annotation.FailTest;
					return false;
				}
			}
		}
		return true;
	}

	public Vector<Variable> getVariables() {
		return variables;
	}

	public int getMatchCount() {
		return matchCount;
	}

}
