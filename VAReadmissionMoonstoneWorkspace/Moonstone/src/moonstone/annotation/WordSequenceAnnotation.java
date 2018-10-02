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
package moonstone.annotation;

import java.util.Collections;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import moonstone.grammar.Grammar;
import moonstone.rule.Rule;
import moonstone.syntactic.Syntax;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.documentanalysis.tokenizer.regexpr.RegExprToken;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.CUIStructureWrapperShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class WordSequenceAnnotation extends Annotation {
	private Sentence sentence = null;
	private Vector<Annotation> annotations = null;
	private Vector<Annotation> expandedCoveringAnnotations = null;
	private int startTokenIndex = 0;
	private Vector<LabelAnnotation> stopWordLabels = null;
	private Vector<String> universalStopWords = null;
	private String string = null;
	private Vector<Token> wordTokens = null;
	private int[] commaPositions = new int[1024];
	private int[] punctuationPositions = new int[2048];
	private int numberOfCommas = 0;
	private int numberOfPunctuations = 0;

	public WordSequenceAnnotation(Grammar grammar) {
		super();
		this.grammar = grammar;
		this.setPropertyType("sentence");
		this.getMoonstoneRuleInterface().storeForGarbageCollection(this);
	}

	public WordSequenceAnnotation(Grammar grammar, Sentence sentence) {
		this(grammar);
		this.sentence = sentence;
		this.string = sentence.text.toLowerCase();
		this.wordTokens = Token.gatherWordTokens(sentence.tokens);
		this.startTokenIndex = sentence.tokens.firstElement().getIndex();

		int cindex = 0;
		for (Token token : sentence.getTokens()) {
			if (token.isPunctuation()) {
				if (!token.isQuestionMark() && !token.isColon()) {
					this.punctuationPositions[this.numberOfPunctuations++] = token
							.getStart();
				}
			}
			if (token.isComma()) {
				this.commaPositions[this.numberOfCommas++] = token.getStart();
			}
		}
	}

	// Altered 6/16/2016 to gather only the largest rule tags.
	public void gatherNarrativeAnnotations() {
		Hashtable<Rule, Integer> ruleCountHash = new Hashtable();
		Hashtable<Vector<Token>, Vector<Rule>> subTokenRuleHash = new Hashtable();
		int tsize = wordTokens.size();
		for (int i = 0; i < wordTokens.size(); i++) {
			Token token = wordTokens.elementAt(i);
			this.addTokenAnnotation(token, token.getString(), token, token);
		}
		for (int i = 0; i < wordTokens.size(); i++) {
			int max = (i + 3 < tsize ? i + 3 : tsize - 1);
			for (int j = max; j >= i; j--) {
				Vector<Token> subTokens = VUtils
						.subVector(wordTokens, i, j + 1);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();
				Vector<Rule> swrules = grammar.getStopWordRules(substr);
				if (swrules != null) {
					for (Rule swrule : swrules) {
						new LabelAnnotation(this, swrule, substr,
								start.getIndex(), end.getIndex(),
								start.getStart(), end.getEnd(),
								start.getWordIndex(), end.getWordIndex());
					}
				}
				Vector<Rule> rules = grammar.getRulesByIndexToken(substr);
				boolean foundRules = (rules != null);
				Word word = null;
				if (i == j) {
					if ((word = subTokens.firstElement().getWord()) != null) {
						for (int k = 0; k < Grammar.UniversalStopwords.length; k++) {
							String swstr = Grammar.UniversalStopwords[k];
							if (word.getString().equals(swstr)) {
								this.universalStopWords = VUtils.addIfNot(
										this.universalStopWords, swstr);
							}
						}
						for (String ptypestr : word.getPartsOfSpeech()) {
							TypeConstant ptype = Syntax
									.convertPartOfSpeechToAnnotationFormat(ptypestr);
							Vector<Rule> prules = grammar
									.getRulesByIndexToken(ptype);
							rules = VUtils.appendIfNot(rules, prules);
						}
					}
					if (subTokens.firstElement().isPunctuation()
							&& !(Annotation.isSpecialAnnotation(subTokens
									.firstElement().getString()))) {
						rules = null;
					}
				}
				if (rules != null) {
					for (Rule rule : rules) {
						HUtils.incrementCount(ruleCountHash, rule);
						VUtils.pushHashVector(subTokenRuleHash, subTokens, rule);
					}
				}
				// PROBLEM!!!:  If a larger substring causes a smaller one to be omitted,
				// then if the rule containing the larger string is invalidated, 
				// rules with the smaller substring are not activated instead.  E.g.
				// the rule for "in home services" causes "home" not to be processed
				// in "lives in home".
				if (foundRules) {
					i = j;
					break;
				}
			}
		}
		Vector<Rule> rules = HUtils.getKeys(ruleCountHash);
		if (rules != null) {
			for (Rule rule : rules) {
				int count = HUtils.getCount(ruleCountHash, rule);
				if (count < rule.getwordOnlyPatternCount()) {
					this.grammar.setRuleIsInvalid(rule);
				}
			}
		}
		Vector<Vector<Token>> subTokenLists = HUtils.getKeys(subTokenRuleHash);
		if (subTokenLists != null) {
			for (Vector<Token> subTokens : subTokenLists) {
				Vector<Rule> strules = subTokenRuleHash.get(subTokens);
				String substr = Token.stringListConcat(subTokens).toLowerCase();
				Token start = subTokens.firstElement();
				Token end = subTokens.lastElement();
				Word word = null;
				Vector<SyntacticTypeConstant> ptypes = null;
				if (subTokens.size() == 1
						&& ((word = subTokens.firstElement().getWord()) != null)
						&& word.getPartsOfSpeech() != null) {
					for (String pos : word.getPartsOfSpeech()) {
						SyntacticTypeConstant ptype = Syntax
								.convertPartOfSpeechToAnnotationFormat(pos);
						ptypes = VUtils.add(ptypes, ptype);
					}
				} else {
					SyntacticTypeConstant ptype = Syntax
							.getTerminalPhraseType(subTokens);
					if (ptype != null) {
						ptypes = VUtils.listify(ptype);
					}
				}
				if (ptypes != null) {
					for (SyntacticTypeConstant ptype : ptypes) {
						new TagAnnotation(this, null, null, ptype, substr,
								start.getIndex(), end.getIndex(),
								start.getStart(), end.getEnd(),
								start.getWordIndex(), end.getWordIndex(), null,
								null);
					}
				} else {
					new TagAnnotation(this, null, null, null, substr,
							start.getIndex(), end.getIndex(), start.getStart(),
							end.getEnd(), start.getWordIndex(),
							end.getWordIndex(), null, null);
				}
			}
		}
		if (this.stopWordLabels != null) {
			Collections.sort(this.stopWordLabels,
					new Annotation.TextStartSorter());
		}
	}

	public void addTokenAnnotation(Token token, String substr, Token start,
			Token end) {
		if (token.isNumber()) {
			new NumberAnnotation(this, substr, start.getIndex(),
					end.getIndex(), start.getStart(), end.getEnd(),
					start.getWordIndex(), end.getWordIndex(), token.getValue());
		} else if (token.isRegExp()) {
			RegExprToken rtoken = (RegExprToken) token;
			new RegExprAnnotation(this, substr, (RegExprToken) token);
		}
	}

	public static WordSequenceAnnotation createWordSequenceAnnotation(
			Grammar grammar, Sentence sentence,
			StructureAnnotation structureAnnotation, boolean useIndexFinder) {
		grammar.initializeChart(sentence.getTokenLength());
		WordSequenceAnnotation sa = new WordSequenceAnnotation(grammar,
				sentence);

		// 4/16/2015
		if (structureAnnotation != null) {
			grammar.addAnnotation(structureAnnotation);
		}

		sa.gatherNarrativeAnnotations();

		if (useIndexFinder) {
			sa.addTokenIndexFinderAnnotations(grammar, sentence);
		}
		return sa;
	}

	public void addTokenIndexFinderAnnotations(Grammar analysis,
			Sentence sentence) {
		Vector<Token> wtokens = Token.gatherWordTokens(sentence.tokens);
		Vector cws = UMLSStructuresShort.getUMLSStructures()
				.getCUIStructureWrappers(wtokens, null, true);
		if (cws != null) {
			for (ListIterator li = cws.listIterator(); li.hasNext();) {
				CUIStructureWrapperShort cw = (CUIStructureWrapperShort) li
						.next();
				CUIStructureShort cs = cw.getCuiStructure();
				TypeConstant tc = cs.getType();
				if (!(tc instanceof UMLSTypeConstant)) {
					continue;
				}
				UMLSTypeConstant utc = (UMLSTypeConstant) tc;

				if (utc != null && !utc.isConnectedToOntology()) {
					continue;
				}
				String tindex = Annotation.getTypeIndex(utc);
				String tui = cw.getCuiStructure().getTUI();

				if (!(analysis.getRulesByIndexToken(tindex) != null || this.grammar
						.getGrammarModule().getTypeInfo()
						.isGenerallyRelevantTUI(tui))) {
					continue;
				}

				if (!cs.isValidPhrase()) {
					continue;
				}

				String cui = cs.getCui();
				String concept = cs.getWordString(false);
				concept = AnnotationIntegrationMaps.getName(cui, concept);
				Token start = cw.getTokens().firstElement();
				Token end = cw.getTokens().lastElement();
				String substr = getDocumentText().substring(start.getStart(),
						end.getEnd() + 1).toLowerCase();
				SyntacticTypeConstant ptype = Syntax.getTerminalPhraseType(cw
						.getTokens());
				TerminalAnnotation ta = new TerminalAnnotation(this, cui,
						concept, ptype, substr, start.getIndex(),
						end.getIndex(), cw.getTextStart(), cw.getTextEnd(),
						start.getWordIndex(), end.getWordIndex(), null, utc);
			}
		}
	}

	void addAnnotation(Annotation annotation) {
		this.annotations = VUtils.add(this.annotations, annotation);
	}

	public Vector<LabelAnnotation> getStopWordLabels() {
		return stopWordLabels;
	}

	public void addStopWordLabel(LabelAnnotation annotation) {
		this.stopWordLabels = VUtils.add(this.stopWordLabels, annotation);
	}

	public String toString() {
		String str = this.string;
		if (str.length() > 100) {
			str = str.substring(0, 100) + "...";
		}
		str = "\"" + str + "\"";
		return str;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public Vector<Annotation> getAnnotations() {
		return annotations;
	}

	public Document getDocument() {
		if (this.sentence != null) {
			return this.sentence.getDocument();
		}
		return null;
	}

	public String getDocumentText() {
		if (this.sentence.getDocument() != null) {
			return this.sentence.getDocument().getText();
		}
		return this.sentence.getText();
	}

	public Vector<Annotation> getExpandedCoveringAnnotations() {
		return expandedCoveringAnnotations;
	}

	public void setExpandedCoveringAnnotations(
			Vector<Annotation> expandedCoveringAnnotations) {
		this.expandedCoveringAnnotations = expandedCoveringAnnotations;
	}

	public int getStartTokenIndex() {
		return startTokenIndex;
	}

	public String getString() {
		return string;
	}

	public Vector<String> getUniversalStopWords() {
		return universalStopWords;
	}

	public Vector<Token> getWordTokens() {
		return this.wordTokens;
	}

	// 6/24/2016
	public boolean containsComma(Annotation annotation) {
		for (int i = 0; i < this.numberOfCommas; i++) {
			int cstart = this.commaPositions[i];
			if (annotation.getTextStart() < cstart
					&& cstart < annotation.getTextEnd()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsPunctuation(Annotation annotation) {
		for (int i = 0; i < this.numberOfPunctuations; i++) {
			int pstart = this.punctuationPositions[i];
			if (annotation.getTextStart() < pstart
					&& pstart < annotation.getTextEnd()) {
				return true;
			}
		}
		return false;
	}

}
