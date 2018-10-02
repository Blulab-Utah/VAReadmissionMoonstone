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
package moonstone.syntactic;

import java.util.Vector;

import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class Syntax {

	public static SyntacticTypeConstant SENTENCE = convertStringToPhraseType("SENTENCE");
	public static SyntacticTypeConstant NP = convertStringToPhraseType("NP");
	public static SyntacticTypeConstant VP = convertStringToPhraseType("VP");
	public static SyntacticTypeConstant AP = convertStringToPhraseType("AP");
	public static SyntacticTypeConstant PP = convertStringToPhraseType("PP");
	public static SyntacticTypeConstant INFL = convertStringToPhraseType("INFL");
	public static SyntacticTypeConstant PREP = convertStringToPhraseType("PREP");
	public static SyntacticTypeConstant NOUN = convertStringToPhraseType("NOUN");
	public static SyntacticTypeConstant ADJ = convertStringToPhraseType("ADJ");
	public static SyntacticTypeConstant VERB = convertStringToPhraseType("VERB");
	public static SyntacticTypeConstant NUMBER = convertStringToPhraseType("NUMBER");
	public static SyntacticTypeConstant CONJ = convertStringToPhraseType("CONJ");
	public static SyntacticTypeConstant STRINGS = convertStringToPhraseType("STRINGS");
	public static SyntacticTypeConstant PUNCTUATION = convertStringToPhraseType("PUNCTUATION");

	public static SyntacticTypeConstant getTerminalPhraseType(Vector<Token> tokens) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (kb != null && tokens != null && tokens.size() >= 1) {
			int numTokens = tokens.size();
			Token firstToken = tokens.firstElement();
			String fstr = firstToken.getString();
			Token lastToken = tokens.lastElement();
			String lstr = lastToken.getString();
			if (numTokens == 1) {
				if (firstToken.isNumber()) {
					return NUMBER;
				}
				if (firstToken.isPunctuation()) {
					return PUNCTUATION;
				}
			}
			Word firstWord = Lexicon.currentLexicon.getWord(fstr);
			Word lastWord = Lexicon.currentLexicon.getWord(lstr);
			if (firstWord != null && lastWord != null) {
				if (numTokens > 1 && firstWord.isPrep() && lastWord.isNoun()) {
					return PP;
				}
				if (numTokens == 1) {
					if (firstWord.isConjunct()) {
						return CONJ;
					}
					if (firstWord.isPrep()) {
						return PREP;
					}
				}
				if (lastWord.isNoun()) {
					return NP;
				}
				if (lastWord.isAdjective()) {
					return AP;
				}
			}
		}
		return null;
	}

	public static SyntacticTypeConstant convertStringToPhraseType(String ptypestr) {
		return SyntacticTypeConstant.createSyntacticTypeConstant(ptypestr);
	}

	public static SyntacticTypeConstant convertPartOfSpeechToAnnotationFormat(String pos) {
		if (pos != null && Character.isLetter(pos.charAt(0)) && Character.isLowerCase(pos.charAt(0))) {
			pos = "#" + pos + "#";
		}
		return SyntacticTypeConstant.createSyntacticTypeConstant(pos.toUpperCase());
	}

}
