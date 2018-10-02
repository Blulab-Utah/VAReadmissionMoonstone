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
package tsl.documentanalysis.document;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.VUtils;

public class Sentence extends DocumentItemConstant {
	public Header header = null;
	public String text = null;
	public int start = -1;
	public int end = -1;
	protected int textLength = -1;
	protected int tokenLength = -1;
	protected int documentIndex = 0;
	protected int tokenStartIndex = -1;
	protected int tokenEndIndex = -1;
	protected int newlineCount = 0;
	protected boolean isQuestion = false;
	// protected int structureAnnotationType = SentenceStructureAnnotationType;
	// public static int SentenceStructureAnnotationType = 1;
	// public static int AVPairStructureAnnotationType = 2;

	public Vector<Token> tokens = null;

	public Sentence() {

	}

	public Sentence(String text) {
		this.text = text;
	}

	public Sentence(Header header, Vector<Token> tokens) {
		this(header, tokens, true);
	}

	// 11/13/2015
	public Sentence(Header header, String text) {
		this.header = header;
		this.text = text;
	}

	public Sentence(Header header, Vector<Token> tokens, boolean addToDocument) {
		this.header = header;
		this.tokens = tokens;
		Token startToken = tokens.firstElement();
		Token endToken = tokens.lastElement();
		this.start = ((Token) tokens.firstElement()).getStart();
		this.end = ((Token) tokens.lastElement()).getEnd();
		if (this.end >= this.header.document.getText().length()) {
			this.end = this.header.document.getText().length() - 1;
		}
		this.textLength = (this.end - this.start) + 1;
		this.tokenLength = this.tokens.size();
		this.tokenStartIndex = startToken.getIndex();
		this.tokenEndIndex = endToken.getIndex();
		this.text = this.header.document.getText().substring(this.start,
				this.end + 1);
		
		// 11/13/2016
//		this.text = new String(this.text);
		
		if (addToDocument) {
			this.header.document.addSentence(this);
			for (Token token : tokens) {
				if (token.isWordSurrogate()) {
					token.setSentence(this);
					String str = token.getString().toLowerCase();
					VUtils.pushIfNotHashVector(
							header.document.getWordSentenceHash(), str, this);
				}
			}
		}
		// 6/14/2016
		Document d = this.getDocument();
		for (int i = this.getTokenEndIndex() + 1; i < d.getTokenCount(); i++) {
			Token t = d.getTokens().elementAt(i);
			if (!t.isWhitespace()) {
				if (t.isQuestionMark()) {
					this.isQuestion = true;
				}
				break;
			}
		}

	}

	public Sentence createSentence(Header header, Vector<Token> tokens,
			boolean addToDocument) {
		if (tokens != null) {
			return new Sentence(header, tokens, addToDocument);
		}
		return null;
	}

	public String toString() {
		return this.toFullString();
	}

	public String toFullString() {
		return "\"" + this.text + "\"";
	}

	public Document getDocument() {
		if (this.header != null && this.header.document != null) {
			return this.header.document;
		}
		return null;
	}

	public Vector<Token> getMatchingTokens(String word) {
		Vector<Token> tokens = null;
		for (Token token : this.tokens) {
			if (word.equals(token.getString())) {
				tokens = VUtils.add(tokens, token);
			}
		}
		return tokens;
	}

	public String[] toDelimitedWordArray() {
		Vector words = null;
		String[] array = null;
		if (this.tokens != null) {
			for (Enumeration e = this.tokens.elements(); e.hasMoreElements();) {
				Token token = (Token) e.nextElement();
				words = VUtils.add(words, token.getString());
			}
			if (words != null && words.size() > 2) {
				words.add(".");
				array = new String[words.size()];
				words.toArray(array);
			}
		}

		return array;
	}

	public static class TextOffsetSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Sentence s1 = (Sentence) o1;
			Sentence s2 = (Sentence) o2;
			if (s1.start < s2.start) {
				return -1;
			}
			if (s1.start > s2.start) {
				return 1;
			}
			return 0;
		}
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public static Vector<String> getTextStrings(Vector<Sentence> sentences) {
		Vector<String> strings = null;
		if (sentences != null) {
			for (Sentence s : sentences) {
				strings = VUtils.add(strings, s.getText());
			}
		}
		return strings;
	}

	public String getText() {
		if (this.text == null) {
			System.out.println("TSL.Document.Sentence.getText(): Text is null!!");
		}
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

	public Vector<Token> gatherWordTokens() {
		return Token.gatherWordTokens(this.getTokens());
	}

	public void setTokens(Vector<Token> tokens) {
		this.tokens = tokens;
	}

	public int getTextLength() {
		return this.textLength;
	}

	public int getTokenLength() {
		return this.tokenLength;
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}

	public void setTokenLength(int tokenLength) {
		this.tokenLength = tokenLength;
	}

	public int getDocumentIndex() {
		return documentIndex;
	}

	public int getTokenStartIndex() {
		return tokenStartIndex;
	}

	public int getTokenEndIndex() {
		return tokenEndIndex;
	}

	public int getNewlineCount() {
		return newlineCount;
	}

	public void setNewlineCount(int newlineCount) {
		this.newlineCount = newlineCount;
	}

	public void incrementNewlineCount() {
		this.newlineCount++;
	}

	public boolean isQuestion() {
		return isQuestion;
	}

}
