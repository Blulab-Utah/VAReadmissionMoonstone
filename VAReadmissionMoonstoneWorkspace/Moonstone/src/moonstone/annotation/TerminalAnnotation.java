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

import java.util.Vector;

import moonstone.semantic.Interpretation;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.VUtils;

public class TerminalAnnotation extends Annotation {

	public TerminalAnnotation() {

	}

	public TerminalAnnotation(WordSequenceAnnotation sentence, String string) {
		super(sentence);
		this.setString(string);
	}

	// 3/5/2015: Just pass in start and end tokens. Get rid of the int values
	// taken from those tokens.

	public TerminalAnnotation(WordSequenceAnnotation sentence, String cui, String concept,
			SyntacticTypeConstant phraseType, String string, int tokenStart, int tokenEnd, int textStart, int textEnd,
			int wordTokenStart, int wordTokenEnd, Object value, TypeConstant type) {
		super(sentence);
		if (cui != null || concept != null) {
			this.semanticInterpretation = Interpretation.create(this, cui, concept, type);
		}
		this.setString(string);
		this.setType(type);
		this.setTokenStart(tokenStart);
		this.setTokenEnd(tokenEnd);
		this.setWordTokenStart(wordTokenStart);
		this.setWordTokenEnd(wordTokenEnd);
		this.setTextStart(textStart);
		this.setTextEnd(textEnd);
		this.textlength = (this.textEnd - this.textStart) + 1;
		this.setValue(value);
		int rstart = this.getTokenStart() - this.getSentenceAnnotation().getStartTokenIndex();
		int rend = this.getTokenEnd() - this.getSentenceAnnotation().getStartTokenIndex();
		if (rstart >= 0) {
			this.setRelativeTokenStart(rstart);
			this.setRelativeTokenEnd(rend);
		}

		Vector<Token> tokens = VUtils.subVector(sentence.getSentence().getDocument().getTokens(), tokenStart,
				tokenEnd + 1);
		this.setPhraseType((SyntacticTypeConstant) phraseType);
		this.assignPhraseType(tokens);
		this.generateIndexTokens();
		this.setSignature();
		this.getCoveredTextLength();
		this.getGoodness();
		if (this.checkIsValid()) {
			sentence.getGrammar().addAnnotation(this);
		}
		this.doPostProcessing();
	}

	public double getGoodness() {
		if (this.goodness == null) {
			this.symmetryFactor = 1;
			this.coveredTextPercent = 1;
			this.goodness = new Double(1.0);
		}
		return this.goodness.doubleValue();
	}

	public boolean checkIsValid() {
		return true;
	}

	public void setSignature() {
		if (this.signature == null) {
			this.signatureID = LastSignatureID++;
			StringBuffer sb = new StringBuffer();
			Interpretation si = this.getSemanticInterpretation();
			sb.append("[");
			String startend = "(" + this.getTokenStart() + "-" + this.getTokenEnd() + ")";
			String cuistr = (this.getCui() != null ? this.getCui() : "*");
			String typestr = (this.getType() != null ? this.getType().getName() : "*:");
			String ptypestr = (this.getPhraseType() != null ? this.getPhraseType().toString() : "*");
			sb.append(startend + ":");
			sb.append(ptypestr + ":");
			sb.append(cuistr + ":");
			sb.append(typestr + ":");
			sb.append("]");
			this.signature = sb.toString();
			// System.out.println("Signature: " + this.signature);
			int x = 1;

		}
	}

	public void generateIndexTokens() {
		super.generateIndexTokens();
		this.indexTokens = VUtils.add(this.indexTokens, this.getString());
		this.indexTokens = VUtils.add(this.indexTokens, this.getCui());

		// 3/25/2015
		if (this.isWord()) {
			this.indexTokens = VUtils.add(this.indexTokens, "?word");
		}
	}

	// 4/24/2015. Format: Headword:Concept:Type
	public String getLPCFGMeaningToken() {
		String str = this.getString().toLowerCase() + ":*:*";
		return str;
	}

}
