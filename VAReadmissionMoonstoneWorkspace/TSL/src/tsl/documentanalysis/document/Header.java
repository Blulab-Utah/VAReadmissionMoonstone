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

import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Header extends DocumentItemConstant {
	public Document document = null;

	public Vector<Token> tokens = null;

	public String text = null;

	int start = -1;

	int end = -1;

	int textStartTokenIndex = -1;

	int textEndTokenIndex = -1;

	int reportOffset = -1;

	int reportIndex = 0;

	int tokenIndex = 0;

	boolean fromSectionHeaderList = false;

	boolean isQuestion = false;

	private static Vector<String> permissibleInLineWords = VUtils
			.arrayToVector(new String[] { "of", "in", "to", "at", "and", "&",
					"/", "-", "," });
	private static Vector<String> permissibleHeaderPunctuation = VUtils
			.arrayToVector(new String[] { "/", "(", ")" });

	private HeaderContent content = null;

	// public Vector<Sentence> sentences = null;

	// public Header(Document document, String text, int start, int end) {
	// this.setDocument(document);
	// this.setText(text);
	// this.setStart(start);
	// this.setEnd(end);
	// char c = text.charAt(text.length() - 1);
	// this.isQuestion = (c == '?' || c == ':');
	// }

	public Header(Document document) {
		this.document = document;
		this.text = "DEFAULT HEADER";
		this.setTextStartTokenIndex(0);
		this.setTextEndTokenIndex(document.getTokenCount() - 1);
	}

	public Header(Document document, Vector<Token> tokens) {
		this.document = document;
		this.tokens = tokens;
		this.start = tokens.firstElement().getStart();
		this.end = tokens.lastElement().getEnd();
		if (this.end + 1 > document.getTextLength()) {
			this.end = document.getTextLength() - 1;
		}
		this.text = document.getText().substring(this.start, this.end + 1);

		int lastTokenIndex = this.tokens.lastElement().getIndex();
		Token nwtoken = Token.peekNextNonWhiteSpaceToken(document,
				lastTokenIndex + 1);
		if (nwtoken != null) {
			String str = nwtoken.getString();
			if ("?".equals(str) || ":".equals(str)) {
				this.isQuestion = true;
			}
		}

		// 11/30/2017: Hokey- needs to be bundled with previous statement
		char c = text.charAt(text.length() - 1);
		if (c == '?' || c == ':') {
			this.isQuestion = true;
		}
	}

	public String toString() {
		return "<\"" + this.text + ",TokenStart=" + this.textStartTokenIndex
				+ ",TokenEnd=" + this.textEndTokenIndex + ",Content=\"***\"])";
	}

	private static int MaxHeaderTokenLength = 16;

	static Vector<Token> getHeaderTokens(Document document,
			boolean useSectionHeaderHeuristics) {
		Vector<Token> headerTokens = null;
		char endChar = (char) -1;
		boolean endsWithHeaderDelimiter = false;
		boolean hasPatternBeginning = false;
		boolean hasSimpleBeginning = false;
		boolean hasHeaderEnding = false;
		if (document.isNoHeaders()) {
			return null;
		}
		Token startToken = Token.advanceToNextWord(document);
		String docText = document.getText();
		boolean hasSufficientPrecedingWhitespace = false;
		boolean atNewline = false;
		if (startToken != null) {
			hasSufficientPrecedingWhitespace = hasSufficientPrecedingWhitespace(
					docText, startToken.getStart());
			atNewline = startToken.getStart() == 0
					|| docText.charAt(startToken.getStart() - 1) == '\n';
		}

		Token endToken = null;

		if (!hasSufficientPrecedingWhitespace || startToken == null
				|| document.atEnd()) {
			// 6/13/2015: Took this out. Not sure why there is never enough
			// whitespace...
			// return null;
		}
		int matchedHeaderEnd = -1;
		String matchedHeaderString = null;
		if (startToken != null && document.getHeaderHash() != null) {
			Vector<String> possibleHeaders = document.getHeaderHash().get(
					startToken.getString());
			if (possibleHeaders != null) {
				for (String possibleHeader : possibleHeaders) {
					if (StrUtils.stringEquals(docText, possibleHeader,
							startToken.getStart())) {
						matchedHeaderEnd = startToken.getStart()
								+ possibleHeader.length() - 1;
						matchedHeaderString = possibleHeader;
						matchedHeaderEnd = startToken.getStart()
								+ possibleHeader.length() - 1;
						break;
					}
				}
			}
		}
		if (matchedHeaderString != null) {
			for (int i = startToken.getIndex(); endToken == null; i++) {
				Token token = document.getTokens().elementAt(i);
				headerTokens = VUtils.add(headerTokens, token);
				if (token.getEnd() >= matchedHeaderEnd) {
					endToken = token;
				}
			}
		} else if (useSectionHeaderHeuristics
				&& hasSufficientPrecedingWhitespace
		// 10/22/2015: Won't always be the case.
		// && startToken.isFirstuppercase()
		) {
			boolean atEnd = false;
			int endTokenIndex = -1;
			boolean wordsAllFirstUppercase = true;

			// 3/11/2016
			int startIndex = startToken.getIndex();
			int poffset = beginsWithPattern(startToken);
			if (poffset > 0) {
				hasPatternBeginning = true;
				startIndex = startIndex + poffset;
			}

			for (int i = startIndex; !atEnd && i < document.getTokens().size()
					&& i < startIndex + MaxHeaderTokenLength; i++) {
				Token token = document.getTokens().elementAt(i);
				if (token.isSpace()) {
					continue;
				}
				if (token.getEnd() >= docText.length() - 1) {
					token.setEnd(docText.length() - 2);
				}

				// 3/29/2016
				// endChar = (char) -1;
				// Token nextNonspaceToken =
				// Token.peekNextNonSpaceToken(document);
				// if (nextNonspaceToken != null &&
				// !nextNonspaceToken.isWhitespace()) {
				// endChar = nextNonspaceToken.getFirstCharacter();
				//
				// }
				endChar = docText.charAt(token.getEnd() + 1);
				Token nextNonWhiteSpace = Token.peekNextNonWhiteSpaceToken(
						document, token.getIndex() + 1);

				if (token.isWord()
						|| (i > startToken.getIndex() && token.isNumber())
						|| (token.isPunctuation() && isPermissibleHeaderPunctuation(token
								.getString()))) {
					endTokenIndex = i;
					if (!((token.isFirstuppercase()) || permissibleInLineWords
							.contains(token.getString()))) {
						wordsAllFirstUppercase = false;
					}
				} else {
					atEnd = true;
				}
				if (endChar == '\n') {
					atEnd = true;
				}

				// 11/30/2017. Not sure if I need the following statement..
				if (nextNonWhiteSpace != null
						&& (":".equals(nextNonWhiteSpace.getString()) || "?"
								.equals(nextNonWhiteSpace.getString()))) {
					endTokenIndex = i;
					endsWithHeaderDelimiter = true;
					atEnd = true;

					// 12/11/2017: Was getting null pointer error in patient
					// processing...
					if (nextNonWhiteSpace.getEnd() < docText.length() - 1) {
						endChar = docText
								.charAt(nextNonWhiteSpace.getEnd() + 1);
					} else {
						endChar = docText
								.charAt(nextNonWhiteSpace.getEnd() - 1);
					}

					// endChar = docText.charAt(nextNonWhiteSpace
					// .getEnd() + 1);
				}

				// 6/26/2016
				if (endChar == ':' || endChar == '?'
						|| ":".equals(token.getString())
						|| "?".equals(token.getString())) {
					// 6/2/2017
					endsWithHeaderDelimiter = true;
					atEnd = true;
				}
			}
			int distance = endTokenIndex - startToken.getIndex();
			if (endTokenIndex >= startToken.getIndex()
					&& distance <= MaxHeaderTokenLength) {
				hasSimpleBeginning = startToken.isFirstuppercase();
				hasHeaderEnding = (endsWithHeaderDelimiter || (atNewline
						&& endChar == '\n' && wordsAllFirstUppercase));
				if ((hasSimpleBeginning || hasPatternBeginning)
						&& hasHeaderEnding) {
					endToken = document.getTokens().elementAt(endTokenIndex);
				}
			}
		}
		if (endToken != null) {
			try {
				headerTokens = new Vector(document.getTokens().subList(
						startToken.getIndex(), endToken.getIndex() + 2));
				document.setTokenIndex(endToken.getIndex() + 2);
			} catch (Exception e) {
				// 10/20/2015 klooge:
				headerTokens = null;
			}
		}

		// 9/26/2015
		if (matchedHeaderString != null && headerTokens != null) {
			document.setLastHeaderFromHeaderList(true);
		}

		return headerTokens;
	}

	private static boolean hasSufficientPrecedingWhitespace(String text,
			int cindex) {
		int spaceCount = 0;
		for (int i = cindex - 1; i >= 0; i--) {
			char c = text.charAt(i);
			if (!Character.isWhitespace(c)) {
				return false;
			}
			if (i == 0 || c == '\t' || c == '\n') {
				return true;
			}
			if (c == ' ') {
				spaceCount++;
			}
			if (spaceCount > 2) {
				return true;
			}
		}
		return true;
	}

	// public static boolean peekNextHeader(Document document,
	// boolean useSectionHeaderHeuristics) {
	// int index = document.getTokenIndex();
	// Vector<Token> tokens = getHeaderTokens(document,
	// useSectionHeaderHeuristics);
	// document.setTokenIndex(index);
	// return tokens != null;
	// }

	public static Header readNextHeader(Document document,
			boolean useSectionHeaderHeuristics) {
		Vector tokens = getHeaderTokens(document, useSectionHeaderHeuristics);
		Header header = null;
		if (tokens != null) {
			header = new Header(document, tokens);
			if (document.isLastHeaderFromHeaderList()) {
				header.fromSectionHeaderList = true;
			}
		}
		return header;
	}

	public boolean isHistorical() {
		String lowerText = this.getText().toLowerCase();
		return lowerText.indexOf("history") >= 0;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

	public void setTokens(Vector tokens) {
		this.tokens = tokens;
	}

	public String getText() {
		if (this.text == null) {
			System.out.println("Header.getText(): Text is null!!");
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

	public int getReportOffset() {
		return reportOffset;
	}

	public void setReportOffset(int reportOffset) {
		this.reportOffset = reportOffset;
	}

	public Vector<Sentence> getSentences() {
		if (this.content instanceof NarrativeContent) {
			return ((NarrativeContent) this.content).getSentences();
		}
		return null;
	}

	public void addSentence(Sentence sentence) {
		if (this.content instanceof NarrativeContent) {
			((NarrativeContent) this.content).addSentence(sentence);
		}
	}

	public TableContent getTableContent() {
		if (this.containsTable()) {
			return (TableContent) this.content;
		}
		return null;
	}

	public void setTableContent(TableContent tc) {
		this.content = tc;
	}

	public int getReportIndex() {
		return reportIndex;
	}

	public void setReportIndex(int reportIndex) {
		this.reportIndex = reportIndex;
	}

	public HeaderContent getContent() {
		return content;
	}

	public void setContent(HeaderContent content) {
		this.content = content;
	}

	public boolean containsNarrative() {
		return (content instanceof NarrativeContent);
	}

	public boolean containsTable() {
		return (content instanceof TableContent);
	}

	public int getTextStartTokenIndex() {
		return textStartTokenIndex;
	}

	public int getTextEndTokenIndex() {
		return textEndTokenIndex;
	}

	public void setTextStartTokenIndex(int textStartTokenIndex) {
		this.textStartTokenIndex = textStartTokenIndex;
	}

	public void setTextEndTokenIndex(int textEndTokenIndex) {
		this.textEndTokenIndex = textEndTokenIndex;
	}

	public Vector<Token> getSpaceDelimitedStringTokens() {
		Document doc = this.getDocument();
		Vector<Token> tokens = null;
		int firstWordTokenIndex = -1;
		int lastWordTokenIndex = -1;

		for (int i = doc.getTokenIndex(); i <= this.getTextEndTokenIndex(); i++) {
			Token token = doc.getToken(i);
			if (token.isWord() || token.isNumber()) {
				if (firstWordTokenIndex < 0) {
					firstWordTokenIndex = token.getIndex();
				}
				lastWordTokenIndex = token.getIndex();
			} else if (!token.isNonDelimitingCharacter()) {
				break;
			}
		}
		if (firstWordTokenIndex >= 0) {
			tokens = (Vector<Token>) VUtils.subVector(doc.getTokens(),
					firstWordTokenIndex, lastWordTokenIndex + 1);
			doc.setTokenIndex(lastWordTokenIndex + 1);
		}
		return tokens;
	}

	public Token getAVPairDelimiterToken() {
		Document doc = this.getDocument();
		for (int i = doc.getTokenIndex(); i <= this.getTextEndTokenIndex(); i++) {
			Token token = doc.getToken(i);
			if (token.isAVPairDelimiter()) {
				doc.setTokenIndex(token.getIndex() + 1);
				return token;
			}
		}
		return null;
	}

	public boolean atTextEnd() {
		return this.document.getTokenIndex() >= this.getTextEndTokenIndex();
	}

	public String getCoveredText() {
		return Token.getString(this.getTextTokens());
	}

	public Vector<Token> getTextTokens() {
		Vector<Token> tokens = VUtils.subVector(this.document.getTokens(),
				this.getTextStartTokenIndex(), this.getTextEndTokenIndex() + 1);
		return tokens;
	}

	public boolean isFromSectionHeaderList() {
		return fromSectionHeaderList;
	}

	public boolean isQuestion() {
		return isQuestion;
	}

	private static boolean isPermissibleHeaderPunctuation(String s) {
		return permissibleHeaderPunctuation.contains(s);
	}

	// 3/11/2016
	private static int beginsWithPattern(Token start) {
		if (isNumberedBulletHeader(start)) {
			return 3;
		}
		return -1;
	}

	private static boolean isNumberedBulletHeader(Token start) {
		Document d = start.getDocument();
		Vector<Token> tokens = d.getTokens();
		int numTokens = d.getTokenCount();
		int startIndex = start.getIndex();
		if (start.isNumber() && startIndex < (numTokens - 3)) {
			Token next1 = tokens.elementAt(startIndex + 1);
			Token next2 = tokens.elementAt(startIndex + 2);
			if (".".equals(next1.getString()) && next2.isWhitespace()) {
				return true;
			}
		}
		return false;
	}

	// 3/29/2016

}
