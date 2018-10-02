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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.ObjectInfoWrapper;
import tsl.utilities.SetUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import moonstone.annotation.Annotation;
import moonstone.grammar.GrammarModule;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class Basilisk {

	protected Hashtable<String, ExtractionPattern> patternHash = new Hashtable();
	protected Hashtable<String, Vector<ExtractionPattern>> wordPatternHash = new Hashtable();
	protected GrammarModule control = null;
	protected Lexicon currentLexicon = null;
	protected Lexicon entireLexicon = null;
	protected Vector<Lexicon> allLexicons = null;

	public void gatherTypeExtractionPatterns(MoonstoneRuleInterface msri,
			Sentence sent) {
		Vector<Annotation> annotations = msri.getControl()
				.applyNarrativeGrammarRules(sent, null);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> candidateAnnotations = gatherTypeCandidateAnnotations(annotation);
				if (candidateAnnotations != null) {
					for (Annotation ca : candidateAnnotations) {
						extractTypePatterns(ca);
					}
				}
			}
		}
		for (Enumeration<ExtractionPattern> e = this.patternHash.elements(); e
				.hasMoreElements();) {
			ExtractionPattern ep = e.nextElement();
			ep.totalWordCount = ObjectInfoWrapper.total(ep.wordCountWrappers);
		}
	}

	public void gatherRelationExtractionPatterns(MoonstoneRuleInterface msri,
			Sentence sent) {
		Vector<Annotation> annotations = msri.getControl()
				.applyNarrativeGrammarRules(sent, null);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> candidateAnnotations = gatherRelationCandidateAnnotations(annotation);
				if (candidateAnnotations != null) {
					for (Annotation ca : candidateAnnotations) {
						extractRelationPatterns(ca);
					}
				}
			}
		}
		for (Enumeration<ExtractionPattern> e = this.patternHash.elements(); e
				.hasMoreElements();) {
			ExtractionPattern ep = e.nextElement();
			ep.totalWordCount = ObjectInfoWrapper.total(ep.wordCountWrappers);
		}
	}

	private void extractTypePatterns(Annotation annotation) {
		if (ExtractionPattern.canExtractTypePattern(annotation)) {
			for (Annotation ca : annotation
					.getLexicallySortedSourceAnnotations()) {
				if (ExtractionPattern.canUseAsAnchor(ca)) {
					Annotation anchor = ca;
					Vector<Annotation> alist = VUtils.listify(anchor);
					String sig = ExtractionPattern.createSignature(annotation,
							alist);
					ExtractionPattern ep = (ExtractionPattern) this.patternHash
							.get(sig);
					if (ep == null) {
						ep = new ExtractionPattern(annotation, alist);
						this.patternHash.put(sig, ep);
					}
					String text = StrUtils.trimAllWhiteSpace(ca.getText());
					ep.wordCountWrappers = ObjectInfoWrapper.increment(
							ep.wordCountWrappers, text);
					VUtils.pushIfNotHashVector(this.wordPatternHash, text, ep);
				}
			}
		}
	}

	private void extractRelationPatterns(Annotation annotation) {
		if (ExtractionPattern.canExtractRelationPattern(annotation)) {
			Vector<Annotation> cas = annotation
					.getLexicallySortedSourceAnnotations();
			Vector<Annotation> alist = new Vector(0);
			Vector<Token> tokens = null;
			for (int i = 0; i < cas.size(); i++) {
				Annotation child = cas.elementAt(i);
				if (i == 0 || i == cas.size() - 1) {
					alist.add(child);
				} else {
					tokens = VUtils.append(tokens, child.getTokens());
				}
			}
			String text = Token.stringListConcat(tokens);
			if (text != null) {
				String sig = ExtractionPattern.createSignature(annotation,
						alist);
				ExtractionPattern ep = (ExtractionPattern) this.patternHash
						.get(sig);
				if (ep == null) {
					ep = new ExtractionPattern(annotation, alist);
					this.patternHash.put(sig, ep);
				}
				ep.wordCountWrappers = ObjectInfoWrapper.increment(
						ep.wordCountWrappers, text);
				VUtils.pushIfNotHashVector(this.wordPatternHash, text, ep);
			} else {
				int x = 1;
				x = x;
			}
		}
	}

	private Vector<Annotation> gatherTypeCandidateAnnotations(
			Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.getRule() != null && !annotation.isTerminal()) {
			if (ExtractionPattern.canExtractTypePattern(annotation)) {
				annotation.getRule().setVisited(true);
				annotations = VUtils.listify(annotation);
			}
			for (Annotation ca : annotation
					.getLexicallySortedSourceAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherTypeCandidateAnnotations(ca));
			}
			if (annotation.getRule() != null
					&& annotation.getRule().isVisited()) {
				annotation.getRule().setVisited(false);
			}
		}
		return annotations;
	}

	private Vector<Annotation> gatherRelationCandidateAnnotations(
			Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.getRule() != null && !annotation.isTerminal()) {
			if (ExtractionPattern.canExtractRelationPattern(annotation)) {
				annotation.getRule().setVisited(true);
				annotations = VUtils.listify(annotation);
			}
			for (Annotation ca : annotation
					.getLexicallySortedSourceAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherRelationCandidateAnnotations(ca));
			}
			if (annotation.getRule() != null
					&& annotation.getRule().isVisited()) {
				annotation.getRule().setVisited(false);
			}
		}
		return annotations;
	}

	public Vector<String> generatedExpandedLexicon(String[] words) {
		boolean atend = false;
		int numpatterns = 20;
		int numiterations = 0;
		Lexicon l = new Lexicon(words);
		this.setCurrentLexicon(l);
		while (!atend) {
			Vector<String> newwords = gatherNewWordCandidates(l, null,
					numpatterns, 1);
			if (newwords == null || numiterations > 100) {
				atend = true;
			} else {
				l.addWords(newwords);
				numpatterns++;
				numiterations++;
			}
		}
		return l.getWords();
	}

	// //////////////
	// This method exemplifies the Basilisk algorithm, with some nuances.
	public Vector<String> gatherNewWordCandidates(Lexicon lexicon,
			Vector<Lexicon> lexicons, int numPatterns, int numWords) {
		Vector<ObjectInfoWrapper> candidateWrappers = null;
		Vector<String> candidateWords = null;
		// Gather all the EPs that extract words in the lexicon.
		Vector<ExtractionPattern> patternPool = this.selectNBestPatterns(
				lexicon, numPatterns);
		if (patternPool != null) {
			Vector<String> extractedWords = gatherExtractedWords(patternPool);
			// Look just at new words not already contained in any lexicon.
			Vector<String> candidates = SetUtils.difference(extractedWords,
					this.entireLexicon.getWords());
			if (candidates != null) {
				Vector<ObjectInfoWrapper> lexiconRelevanceWrappers = new Vector(
						0);
				// Calculate the relevance of each word in the lexicon. (Used to
				// normalize new candidate word relevance scores.)
				for (String word : lexicon.getWords()) {
					float relevance = calculateAdjustedWordRelevance(lexicon,
							lexicons, word);
					lexiconRelevanceWrappers.add(new ObjectInfoWrapper(word, 1,
							relevance));
				}
				Collections.sort(lexiconRelevanceWrappers,
						new ObjectInfoWrapper.ValueSorter());
				// For each new candidate word, if the word is not already
				// known,
				// or was seen previously, or rejected by the user,
				// calculate the relevance of the candidate per all the
				// lexicons,
				// then calculate the word's normalized relevance against the
				// lexicon's
				// floor and ceiling relevance values. Store candidates +
				// normalized
				// relevance scores.
				for (String word : candidates) {

					// if (this.currentLexicon.containsSubword(word)) {
					// continue;
					// }

					float relevance = calculateAdjustedWordRelevance(lexicon,
							lexicons, word);
					// relevance = normalizeRelevance(lexiconRelevanceWrappers,
					// relevance, false);
					candidateWrappers = VUtils.add(candidateWrappers,
							new ObjectInfoWrapper(word, 1, relevance));
				}
			}
		}
		if (candidateWrappers != null) {
			Collections.sort(candidateWrappers,
					new ObjectInfoWrapper.ValueSorter());
			if (candidateWrappers.size() > numWords) {
				candidateWrappers = VUtils.subVector(candidateWrappers, 0,
						numWords + 1);
			}
			candidateWords = ObjectInfoWrapper.getObjects(candidateWrappers);
		}
		return candidateWords;
	}

	// ///////////////

	// Gather best EPs that select new words not currently found in any lexicon.
	private Vector<ExtractionPattern> selectNBestPatterns(Lexicon lexicon,
			int size) {
		Vector<ExtractionPattern> selected = null;
		Vector<ExtractionPattern> candidates = gatherRelevantPatterns(lexicon);
		if (candidates != null && this.currentLexicon != null) {
			for (ExtractionPattern ep : candidates) {
				for (ObjectInfoWrapper wrapper : ep.wordCountWrappers) {
					// EP selects word not in known lexicons
					if (!this.entireLexicon.containsWord(wrapper.object
							.toString())) {
						selected = VUtils.addIfNot(selected, ep);
						break;
					}
				}
			}
		}
		if (selected != null && selected.size() > size) {
			selected = VUtils.subVector(selected, 0, size + 1);
		}
		return selected;
	}

	private Vector<ExtractionPattern> gatherRelevantPatterns(Lexicon lexicon) {
		Vector<ExtractionPattern> patterns = null;
		for (String word : lexicon.getWords()) {
			Vector<ExtractionPattern> v = this.wordPatternHash.get(word);
			patterns = VUtils.appendIfNot(patterns, v);
		}
		if (patterns != null) {
			Vector wrappers = new Vector(0);
			for (ExtractionPattern ep : patterns) {
				float score = ep.calculateRLogF(lexicon.getWords());
				wrappers.add(new ObjectInfoWrapper(ep, 1, score));
			}
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
			patterns = ObjectInfoWrapper.getObjects(wrappers);
		}
		return patterns;
	}

	private static Vector<String> gatherExtractedWords(
			Vector<ExtractionPattern> patterns) {
		Vector<String> extractedWords = null;
		if (patterns != null) {
			for (ExtractionPattern ep : patterns) {
				Vector objects = ObjectInfoWrapper
						.getObjects(ep.wordCountWrappers);
				extractedWords = VUtils.appendIfNot(extractedWords, objects);
			}
		}
		return extractedWords;
	}

	// The relevance of the candidate word per the target lexicon, minus the
	// best relevance found in any other lexicon.
	private float calculateAdjustedWordRelevance(Lexicon lexicon,
			Vector<Lexicon> lexicons, String word) {
		float relevance = calculateObjectRelevance(lexicon, word);
		float bestOtherRelevance = 0f;
		if (lexicons != null && lexicons.size() > 1) {
			for (Lexicon otherLexicon : lexicons) {
				if (!otherLexicon.equals(lexicon)) {
					float otherRelevance = calculateObjectRelevance(
							otherLexicon, word);
					if (otherRelevance > bestOtherRelevance) {
						bestOtherRelevance = otherRelevance;
					}
				}
			}
		}
		relevance -= bestOtherRelevance;
		return relevance;
	}

	// numberOfPatternsThatExtractWord = # EPs that extract new candidate word.
	// distinctCount = Number of category members extracted by the EPs that
	// extract the candidate word.
	// Score(newWord) =
	private float calculateObjectRelevance(Lexicon lexicon, String word) {
		double wordScore = 0;
		Vector<ExtractionPattern> patternsThatExtractWord = (Vector) this.wordPatternHash
				.get(word);
		if (patternsThatExtractWord != null) {
			double logsum = 0;
			double numberOfPatternsThatExtractWord = patternsThatExtractWord
					.size();
			for (ExtractionPattern ep : patternsThatExtractWord) {
				double distinctCount = 0;
				for (Object lword : lexicon.getWords()) {
					int count = ObjectInfoWrapper.getCount(
							ep.wordCountWrappers, lword);
					if (count > 0) {
						distinctCount++;
					}
				}
				logsum += Math.log(distinctCount + 1);
			}
			double avgLog = logsum / numberOfPatternsThatExtractWord;
			wordScore = new Float(avgLog).floatValue();
			if (wordScore > 0.99f) {
				wordScore = 0.99f;
			}
		}
		return new Float(wordScore).floatValue();
	}

	// If a relevance value is between the low and high values of the current
	// lexicon this
	// produces a normalized relevance between 0 and 1. If the relevance is
	// outside those values
	// the normalized value can be < 0 or > 1.

	private static float normalizeRelevance(Vector wrappers, float relevance,
			boolean doSort) {
		float normalizedRelevance = relevance;
		if (doSort) {
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
		}
		float ceiling = ((ObjectInfoWrapper) wrappers.firstElement()).value;
		float floor = ((ObjectInfoWrapper) wrappers.lastElement()).value;
		if (ceiling > floor) {
			normalizedRelevance = (relevance - floor) / (ceiling - floor);
		}
		return normalizedRelevance;
	}

	private static float normalizeRelevance(float relevance, float ceiling,
			float floor) {
		float normalizedRelevance = relevance;
		if (ceiling > floor) {
			normalizedRelevance = (relevance - floor) / (ceiling - floor);
		}
		return normalizedRelevance;
	}

	public Hashtable<String, Vector<ExtractionPattern>> getWordPatternHash() {
		return wordPatternHash;
	}

	public Lexicon getCurrentLexicon() {
		return currentLexicon;
	}

	public void setCurrentLexicon(Lexicon currentLexicon) {
		this.currentLexicon = currentLexicon;
		this.entireLexicon = currentLexicon;
	}

	public void clear() {
		this.patternHash.clear();
		this.wordPatternHash.clear();
	}

	public void printPatterns() {
		for (Enumeration<String> e = this.getWordPatternHash().keys(); e
				.hasMoreElements();) {
			String key = e.nextElement();
			Vector<ExtractionPattern> eps = this.getWordPatternHash().get(key);
			for (ExtractionPattern ep : eps) {
				System.out.println("Key=" + key + ",Pattern=" + ep);
			}
		}
	}

}
