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
package tsl.documentanalysis.tokenizer;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.DocumentAccess;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.regexpr.RegExprManager;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Token {

	private Document document = null;

	private int type = -1;

	private String typeString = null;

	private int subtype = -1;

	private String string = null;

	private Object value = null;

	private Word word = null;

	private boolean firstuppercase = false;

	private boolean alluppercase = false;

	private int start = -1;

	private int end = -1;

	private int index = -1;

	// 2/23/2014
	private int wordIndex = -1;

	private int count = 1;

	private Sentence sentence = null;

	// Token types
	public static int WORD = 0;

	public static int WHITESPACE = 1;

	public static int PUNCTUATION = 2;

	public static int QUOTEDSTRING = 3;

	public static int DEIDENTIFIEDBLOCK = 4;

	public static int NUMBER = 5;

	public static int TITLE = 6;

	public static int ABBREVIATION = 7;

	public static int ENDDOCUMENTDELIM = 8;

	public static int DATE = 9;

	public static int TIME = 10;

	public static int URL = 11;

	public static int LONGNEWLINE = 12;

	public static int ASTERISK = 13;
	
	public static int CHECKBOX = 14;
	
	public static int UNDERLINE = 15;


	// Word subtypes
	public static int FIRSTUPPERCASE = 0;

	public static int ALLUPPERCASE = 1;

	public static int ALLLOWERCASE = 2;

	public static int REGEX = 3;

	// Delimiter / punctuation subtypes
	public static int COLON = 0;

	public static int SEMICOLON = 1;

	public static int PERIOD = 2;

	public static int QUESTION = 3;

	public static int EXCLAMATION = 4;

	// Other punctuation subtypes
	public static int COMMA = 6;

	public static int SLASH = 7;

	public static int SINGLEQUOTE = 8;

	public static int DOUBLEQUOTE = 9;

	public static int UNKNOWN = 10;

	// Added 10/2/2008 so I can parse surface pattern element strings, e.g.
	// "np<restcond>[amalgam]"
	public static int LEFTARROW = 11;

	public static int RIGHTARROW = 12;

	public static int LEFTBRACKET = 13;

	public static int RIGHTBRACKET = 14;

	public static int LEFTPAREN = 15;

	public static int RIGHTPAREN = 16;

	public static int SPACE = 17;

	public static int NEWLINE = 18;

	public static int TAB = 19;

	public static int CHECKED = 20;
	
	public static int UNCHECKED = 21;
	

	public static int POSSESSIVE_CHARACTER = 1;

	public static int[] tokenTypes = { WORD, WHITESPACE, PUNCTUATION,
			QUOTEDSTRING, DEIDENTIFIEDBLOCK, NUMBER, TITLE, ABBREVIATION,
			ENDDOCUMENTDELIM, DATE, TIME, URL, LONGNEWLINE, ASTERISK, CHECKBOX, UNDERLINE };

	public static String[] tokenNames = { "WORD", "WHITESPACE", "PUNCTUATION",
			"QUOTE", "DEIDENT", "NUMBER", "TITLE", "ABBREVIATION",
			"ENDDOCDELIM", "DATE", "TIME", "URL", "LONGNEWLINE", "ASTERISK", "CHECKBOX", "UNDERLINE" };

	public static String[] longNewlines = { "\n\n", "\n\f\n\f", "\f\n\f\n",
			"\r\n\r\n" };

	public static int[] longNewlineTypes = { LONGNEWLINE, LONGNEWLINE,
			LONGNEWLINE, LONGNEWLINE };

	public static String[] longNewlineNames = { "double newline",
			"double newline", "double newline" };

	public static String[] punctuation = { ":", ";", ",", ".", "?", "!", "/",
			"'", "\"", "<", ">", "[", "]", "(", ")", " ", "\n", "	", "*" };

	public static char[] NonDelimitingPunctuation = { '-', '/', '(', ')', '=',
			' ', '	', '@' };
	
	public static char[] WordCharacterStandins = { '-', '/', '@' };

	public static int[] punctuationTypes = { COLON, SEMICOLON, COMMA, PERIOD,
			QUESTION, EXCLAMATION, SLASH, SINGLEQUOTE, DOUBLEQUOTE, LEFTARROW,
			RIGHTARROW, LEFTBRACKET, RIGHTBRACKET, LEFTPAREN, RIGHTPAREN,
			SPACE, NEWLINE, TAB, ASTERISK };

	public static String[] punctuationNames = { "colon", "semicolon", "comma",
			"period", "question", "exclamation", "slash", "singlequote",
			"doublequote", "leftarrow", "rightarrow", "leftbracket",
			"rightbracket", "leftparen", "rightparen", "space", "newline",
			"tab", "asterisk" };

	public static String[] NonDelimitingPunctuationNames = { "hyphen", "slash",
			"leftparen", "rightparen", "equals", "space", "tab", "at" };

	// 1/28/2015
	public static String[] Whitespace = { " ", "\n", "\t" };

	public static int[] WhitespaceTypes = { SPACE, NEWLINE, TAB };

	public static String[] WhitespaceNames = { "space", "newline", "tab" };

	public static String[] titles = { "Dr.", "Mr.", "Mrs.", "Ms." };

	public static String[] abbreviations = { "Ph.D.", "D.M.D.", "D.D.S.",
			"M.D.", "q.", "b.i.d.", "m.g.", "g.", "a.m.", "p.m.", "e.g.",
			"p.o.", "t.i.d.", "p.r.n.", "q.h.s", "q.i.d", "q.p.m", "q.6h.",
			"q.7h.", "q.8h." };

	static String endDocumentDelim = "<ENDDOC>";

	// 2/14/2015: Need to use this everywhere I currently use the arrays.
	private static Hashtable<String, String> SubtypeNameHash = null;

	public Token() {

	}

	public Token(Token token) {
		this(token.getType(), token.getString(), token.getStart(), token
				.getEnd(), token.getValue());
		this.index = token.index;
		setValue(token.getValue());
	}

	public Token(int type, String str, int start, int end, Object value) {
		try {
			this.type = type;
			this.typeString = tokenNames[this.type].toLowerCase();
			this.string = str;
			this.start = start;
			this.end = end;
			if (Character.isLetter(this.string.charAt(0))) {
				Lexicon lexicon = Lexicon.currentLexicon;
				this.word = lexicon.getWord(this.string);
				this.determineWordSubtype();
			} else if (type == WHITESPACE) {
				this.subtype = determineSubtype(this.string, Whitespace,
						WhitespaceTypes);
			} else if (type == PUNCTUATION) {
				this.subtype = determineSubtype(this.string, punctuation,
						punctuationTypes);
			} else if (type == NUMBER) {
				try {
					this.value = new Float(Float.parseFloat(this.string));
				} catch (NumberFormatException e) {
					this.value = null;
				}
			} else if (type == CHECKBOX) {
				if (this.isCheckboxChecked()) {
					this.subtype = CHECKED;
				} else {
					this.subtype = UNCHECKED;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initialize() {
		if (SubtypeNameHash == null) {
			SubtypeNameHash = new Hashtable();
			for (int i = 0; i < punctuationNames.length; i++) {
				SubtypeNameHash.put(punctuation[i], punctuationNames[i]);
			}
			for (int i = 0; i < NonDelimitingPunctuationNames.length; i++) {
				String cstr = Character.toString(NonDelimitingPunctuation[i]);
				SubtypeNameHash.put(cstr, NonDelimitingPunctuationNames[i]);
			}
			for (int i = 0; i < WhitespaceNames.length; i++) {
				SubtypeNameHash.put(Whitespace[i], WhitespaceNames[i]);
			}
		}
		Hashtable hash = SubtypeNameHash;
	}

	public static void readTokensFromInput(Document report) {
		initialize();
		Token t = null;
		RegExprManager rxm = RegExprManager.getRegExprManager();
		if (rxm != null) {
			// How to determine which tokenTypes/semanticTypes are relevant?
			rxm.applyRegExPatterns(report);
		}
		if (report.getText() != null) {
			int index = 0;
			int wordIndex = 0;
			Token lastToken = null;
			while ((t = readNextTokenFromInput(report)) != null) {
				if (t != lastToken) {
					t.index = index++;
					if (t.isWordSurrogate()) {
						t.wordIndex = wordIndex++;
					}
					report.addToken(t);
					lastToken = t;
				}
			}
		}
	}

	public static Token readNextTokenFromInput(Document report) {
		Token token = null;
		int start = report.getTextIndex();
		int end = -1;
		String str = null;
		Token lastToken = report.getLastToken();
		if (report.getText() == null || report.getText().length() == 0) {
			return null;
		}
		if ((token = report.getRegexToken(report.getTextIndex())) != null) {
			token.index = report.getTokenIndex();
			report.textIndex = token.end + 1;
		} else if ((str = readLongNewline(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(LONGNEWLINE, str, start, end, null);
		} else if ((str = readURL(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(URL, str, start, end, null);
		} else if ((str = readTitle(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(TITLE, str, start, end, null);
		} else if ((str = readAbbreviation(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(ABBREVIATION, str, start, end, null);
		} else if ((str = readNumber(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(NUMBER, str, start, end, null);
		} else if ((str = readWord(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(WORD, str, start, end, null);
		} else if ((str = readQuotedString(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(QUOTEDSTRING, str, start, end, null);
		} else if ((str = readDeidentifiedBlock(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(DEIDENTIFIEDBLOCK, str, start, end, null);
		} else if ((str = readEndDocumentDelim(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(ENDDOCUMENTDELIM, str, start, end, null);
		} else if ((str = readWhitespace(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(WHITESPACE, str, start, end, null);
		} else if ((str = readUnderline(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(UNDERLINE, str, start, end, null);
		}
		else if ((str = readCheckBox(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(CHECKBOX, str, start, end, null);
		} 
		else if ((str = readPunctuation(report)) != null) {
			end = report.textIndex - 1;
			token = new Token(PUNCTUATION, str, start, end, null);
		} else {
			if (report.textIndex < report.getText().length()) {
				report.textIndex++;
				token = readNextTokenFromInput(report);
			}
		}
		if (token != null) {
			token.document = report;
		}
		return token;
	}

	public String toString() {
		String str = "[" + tokenNames[this.type] + "=";
		if (this.string != null) {
			str += "\"" + this.string + "\"";
		} else {
			str += "*";
		}
		str += "]";
		str += "<" + this.start + "-" + this.end + ">";
		str += "{Tindex=" + this.index + "}";
		return str;
	}

	public boolean isWord() {
		return this.type == WORD;
	}

	public boolean isWordSurrogate() {
		return (isWord() || isNumber() || isDeidentifiedBlock()
				|| isAbbreviation() || isTitle() || isQuotedString() || isURL() || isCheckbox());
	}

	public boolean isUpperCaseWord() {
		return isWord() && this.alluppercase;
	}

	public static boolean isAllUpperCaseWords(Vector<Token> tokens) {
		if (tokens != null) {
			for (Token token : tokens) {
				if (!token.isAllUpperCase()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isAllFirstUpperCaseWords(Vector<Token> tokens) {
		if (tokens != null) {
			for (Token token : tokens) {
				if (!token.isFirstuppercase()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean isPunctuation() {
		return this.type == PUNCTUATION;
	}

	public boolean isPunctuation(String str) {
		return isPunctuation() && this.getString().equals("=");
	}

	public boolean isComma() {
		return this.type == PUNCTUATION && this.subtype == COMMA;
	}

	public boolean isColon() {
		return this.type == PUNCTUATION && this.subtype == COLON;
	}

	public boolean isQuestionMark() {
		return this.type == PUNCTUATION && this.subtype == QUESTION;
	}

	public boolean isLongNewline() {
		return this.type == LONGNEWLINE;
	}

	public boolean isQuotedString() {
		return this.type == QUOTEDSTRING;
	}

	public boolean isNumber() {
		return this.type == NUMBER;
	}

	public boolean isNonDelimitingCharacter() {
		return this.string.length() == 1
				&& isNonDelimitingCharacter(this.string.charAt(0));
	}

	public boolean isRegExp() {
		return this.type == REGEX;
	}

	public boolean isDeidentifiedBlock() {
		return this.type == DEIDENTIFIEDBLOCK;
	}

	public boolean isTitle() {
		return this.type == TITLE;
	}

	public boolean isAbbreviation() {
		return this.type == ABBREVIATION;
	}

	public boolean isCheckbox() {
		return this.type == CHECKBOX;
	}

	public boolean isEndDocumentDelim() {
		return this.type == ENDDOCUMENTDELIM;
	}

	public boolean isLeftArrow() {
		return this.subtype == LEFTARROW;
	}

	public boolean isRightArrow() {
		return this.subtype == RIGHTARROW;
	}

	public boolean isLeftParen() {
		return this.subtype == LEFTPAREN;
	}

	public boolean isRightParen() {
		return this.subtype == RIGHTPAREN;
	}

	public boolean isURL() {
		return (this.type == URL);
	}

	public Object getValue() {
		return this.value;
	}

	public String getString() {
		return this.string;
	}

	public char getFirstCharacter() {
		String str = this.getString();
		return str.charAt(0);
	}

	public int getLength() {
		return end - start + 1;
	}

	public static Token findTokenByPosition(Vector<Token> tokens, int position) {
		if (tokens != null) {
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.elementAt(i);
				if (token.start <= position && position < token.end) {
					return token;
				}
			}
		}
		return null;
	}

	public static Token findTokenAfter(Vector tokens, Object key) {
		for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
			Token t = (Token) e.nextElement();
			if (t.getValue().equals(key)) {
				return (Token) e.nextElement();
			}
		}
		return null;
	}

	static int determineSubtype(String str, String[] strarray, int[] intarray) {
		for (int i = 0; i < strarray.length; i++) {
			String s = strarray[i];
			if (s.equals(str)) {
				return intarray[i];
			}
		}
		return -1;
	}

	public static Token peekNextToken(Document report) {
		if (report.tokenIndex < report.getTokens().size()) {
			return (Token) report.getTokens().elementAt(report.tokenIndex);
		}
		return null;
	}

	// 3/29/2016
	public static Token peekNextNonSpaceToken(Document report) {
		for (int i = report.tokenIndex + 1; i < report.getTokenCount(); i++) {
			Token t = report.getToken(i);
			if (!t.isSpace()) {
				return t;
			}
		}
		return null;
	}
	
	public static Token peekNextNonWhiteSpaceToken(Document report, int index) {
		for (int i = index; i < report.getTokenCount(); i++) {
			Token t = report.getToken(i);
			if (!t.isWhitespace()) {
				return t;
			}
		}
		return null;
	}

	public static Token peekNextToken(Document report, String str) {
		if (report.tokenIndex < report.getTokens().size()) {
			Token token = (Token) report.getTokens().elementAt(
					report.tokenIndex);
			if (token.string.equals(str)) {
				return token;
			}
		}
		return null;
	}

	public static Token peekFollowingToken(Document report) {
		if (report.getTokenIndex() < report.getTokens().size() - 1) {
			return (Token) report.getTokens().elementAt(
					report.getTokenIndex() + 1);
		}
		return null;
	}

	public static Token readNextToken(Document report) {
		report.lastTokenIndex = report.tokenIndex;
		return (report.tokenIndex >= report.getTokens().size() ? null
				: (Token) report.getTokens().elementAt(report.tokenIndex++));
	}

	public static Token readNextToken(Document report, String str) {
		Token token = peekNextToken(report, str);
		if (token != null) {
			report.lastTokenIndex = report.tokenIndex++;
		}
		return token;
	}

	public static void returnToLastToken(Document report) {
		if (report.lastTokenIndex >= 0) {
			report.tokenIndex = report.lastTokenIndex;
		}
	}

	public Vector readTokensToDelimiter(Document report, String delim) {
		int startindex = report.tokenIndex;
		Token token = null;
		Vector tokens = null;
		boolean found = false;
		while (!found && !report.atEnd()) {
			token = readNextToken(report);
			if (delim.equals(token.string)) {
				found = true;
			} else {
				tokens = VUtils.add(tokens, token);
			}
		}
		if (!found) {
			report.tokenIndex = startindex;
			tokens = null;
		}
		return tokens;
	}

	public static Token advanceToNextWord(Document report) {
		Token token = null;
		while (!report.atEnd()) {
			token = Token.peekNextToken(report);
			if (token.isWordSurrogate()) {
				break;
			} else {
				Token.readNextToken(report);
			}
		}
		return token;
	}

	public static Vector<Token> readTokensFromString(String str) {
		Vector<Token> tokens = null;
		if (str != null) {
			Document document = DocumentAccess.getEmptyDocument();
			document.setText(str);
			readTokensFromInput(document);
			tokens = document.getTokens();
		}
		return tokens;
	}

	static String readDeidentifiedBlock(Document report) {
		return null;
//		int index = report.textIndex;
//		StringBuffer sb = new StringBuffer();
//		String str = null;
//		boolean badblock = false;
//		if (peekNextString(report, "**")) {
//			report.textIndex += 2;
//			while (peekNextChar(report) != -1 && str == null && !badblock) {
//				char ch = readNextChar(report);
//				if (ch == '\n') {
//					badblock = true;
//				} else {
//					sb.append(ch);
//				}
//				if (ch == ']') {
//					str = sb.toString();
//				}
//			}
//		}
//		if (badblock) {
//			report.textIndex = index;
//		}
//		return str;
	}

	static String readQuotedString(Document report) {
		char ch = peekNextChar(report);
		int x = 1, y = 1;
		if (x == y) {
			return null;
		}
		if (ch == '"' || ch == '\'') {
			boolean instring = true;
			StringBuffer sb = new StringBuffer();
			readNextChar(report);
			while (instring) {
				ch = readNextChar(report);
				if (ch == '"' || ch == '\'') {
					instring = false;
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}
		return null;
	}

	static String readWord(Document report) {
		char ch = peekNextChar(report);
		if (Character.isLetter(ch) || Character.isDigit(ch)) {
			char fch;
			boolean inword = true;
			StringBuffer sb = new StringBuffer();
			while (inword) {
				ch = peekNextChar(report);
				fch = peekFollowingChar(report);
				if (Character.isLetter(ch)
						|| Character.isDigit(ch)
						|| isWordCharacterStandin(ch)
						|| (!Character.isWhitespace(ch)
								&& !isSignificantPunctuation(report, ch) && (Character
								.isLetter(fch) || Character.isDigit(fch)))) {
					sb.append(ch);
					report.textIndex++;
				} else {
					inword = false;
				}
			}
			return sb.toString();
		}
		return null;
	}

	static String readNumber(Document report) {
		char ch = peekNextChar(report);
		if (Character.isDigit(ch)) {
			boolean inword = true;
			boolean founddelimiter = false;
			StringBuffer sb = new StringBuffer();
			while (inword) {
				ch = peekNextChar(report);
				char fch = peekFollowingChar(report);
				if (Character.isDigit(ch)
						|| (!founddelimiter && (ch == '.' || ch == '/') && Character
								.isDigit(fch))) {
					if (ch == '.' || ch == '/') {
						founddelimiter = true;
					}
					sb.append(ch);
					report.textIndex++;
				} else {
					inword = false;
				}
			}
			return sb.toString();
		}
		return null;
	}

	static String readPunctuation(Document report) {
		char ch = peekNextChar(report);
		String punctuation = null;
		if (ch != (char) -1
				&& !(Character.isDigit(ch) || Character.isLetter(ch) || Character
						.isWhitespace(ch))) {
			punctuation = Character.toString(ch);
			report.textIndex++;
		}
		return punctuation;
	}
	
	// 5/2/2016
	static String readUnderline(Document report) {
		int oldindex = report.getTextIndex();
		char c = peekNextChar(report);
		int underlineCount = 0;
		String underline = null;
		if (c == '_') {
			while (!report.atEnd()) {
				c = readNextChar(report);
				if (c == '_') {
					underlineCount++;
				} else {
					break;
				}
			}
		}
		if (underlineCount > 1) {
			underline = report.getText().substring(oldindex, report.getTextIndex() - 1);
		} else {
			report.setTextIndex(oldindex);
		}
		return underline;
	}

	static String readWhitespace(Document report) {
		String founddelim = null;
		char ch = peekNextChar(report);
		if (ch != (char) -1
				&& !(Character.isDigit(ch) || Character.isLetter(ch))) {
			for (int i = 0; i < Whitespace.length; i++) {
				String delim = Whitespace[i];
				if (peekNextString(report, delim)) {
					founddelim = delim;
					report.textIndex += founddelim.length();
					break;
				}
			}
		}
		return founddelim;
	}

	static String readLongNewline(Document report) {
		String founddelim = "";
		int oldIndex = report.textIndex;
		int newlinecount = 0;
		while (!report.atEnd()) {
			char c = readNextChar(report);
			if (Character.isWhitespace(c)) {
				founddelim += c;
				if (c == '\n') {
					newlinecount++;
				}
			} else {
				report.textIndex--;
				break;
			}
		}
		if (newlinecount < 2) {
			founddelim = null;
			report.setTextIndex(oldIndex);
		} else {
			int x = 1;
		}
		return founddelim;
	}

	// 2/29/2016: Causing memory havoc, and I'm not using URLs
	// right now...
	static String readURL(Document report) {
		return null;
		// return readDelimitedString(report, "<<", ">>");
	}

	static String readSemiStructuredTableLine(Document report) {
		int lastti = report.tokenIndex - 1;
		String text = report.getText();
		if (lastti > 0 && text.charAt(lastti) == '\n') {
			char c = report.getText().charAt(report.lastTokenIndex);
			if (c == '\n') {
				int nextnli = report.getText().indexOf('\n');
				if (nextnli > 0) {
					String str = text.substring(report.tokenIndex, nextnli);
					if (isSemiStructuredTableLine(report, str)) {
						return str;
					}

				}
			}
		}
		return null;
	}

	// I will eventually have a classifier for this...
	static boolean isSemiStructuredTableLine(Document report, String str) {
		// if ((str.contains(":") || str.contains("?"))
		// && report.getLas

		return false;
	}

	static String readCheckBox(Document report) {
		String str = readDelimitedString(report, "[", "]", 6);
		if (str == null) {
			str = readDelimitedString(report, "(", ")", 6);
		}
		return str;
	}

	boolean isCheckboxChecked() {
		if (this.type == CHECKBOX) {
			for (int i = 0; i < this.string.length(); i++) {
				char c = this.string.charAt(i);
				if (c == 'x' || c == 'X') {
					return true;
				}
			}
		}
		return false;
	}

	static String readDelimitedString(Document report, String start,
			String end, int maxlen) {
		if (peekNextString(report, start)) {
			StringBuffer sb = new StringBuffer();
			int oldIndex = report.textIndex;
			report.textIndex += start.length();
			boolean inString = true;
			boolean completeString = false;
			while (inString) {
				if (maxlen > 0 && (report.textIndex - oldIndex) > maxlen) {
					break;
				}
				if (peekNextString(report, end)) {
					inString = false;
					completeString = true;
					report.textIndex += end.length();
				} else {
					char ch = peekNextChar(report);
					sb.append(ch);
					report.textIndex++;
				}
			}
			if (completeString) {
				String str = start + sb.toString() + end;
				return str;
			}
			report.textIndex = oldIndex;
		}
		return null;
	}

	static String readTitle(Document report) {
		if (report != null && report.getText() != null) {
			int remaining = report.getText().length() - report.textIndex;
			for (int i = 0; i < titles.length; i++) {
				String title = titles[i];
				if (remaining > title.length()
						&& title.regionMatches(0, report.getText(),
								report.textIndex, title.length())) {
					report.textIndex += title.length();
					return title;
				}
			}
		}
		return null;
	}

	static String readAbbreviation(Document report) {
		if (report != null && report.getText() != null) {
			int remaining = report.getText().length() - report.textIndex;
			for (int i = 0; i < abbreviations.length; i++) {
				String abbreviation = abbreviations[i];
				if (remaining > abbreviation.length()
						&& abbreviation.regionMatches(0, report.getText(),
								report.textIndex, abbreviation.length())) {
					report.textIndex += abbreviation.length();
					return abbreviation;
				}
			}
		}
		return null;
	}

	static String readEndDocumentDelim(Document report) {
		if (report.lengthToTextEnd() >= endDocumentDelim.length()
				&& report.getText().charAt(report.textIndex) == '<') {
			int remaining = report.getText().length() - report.textIndex;
			if (remaining >= endDocumentDelim.length()
					&& endDocumentDelim.regionMatches(0, report.getText(),
							report.textIndex, endDocumentDelim.length())) {
				report.textIndex += endDocumentDelim.length();
				return endDocumentDelim;
			}
		}
		return null;
	}

	public static boolean peekNextString(Document report, String str) {
		if (peekNextChar(report) == str.charAt(0)
				&& report.textIndex + str.length() <= report.getText().length()) {
			String substr = report.getText().substring(report.textIndex,
					report.textIndex + str.length());
			return (str.equals(substr));
		}
		return false;
	}

	public static char peekNextChar(Document report) {
		if (report.getText() == null) {
			return (char) -1;
		}
		if (report.textIndex < report.getText().length()) {
			return report.getText().charAt(report.textIndex);
		} else {
			return (char) -1;
		}
	}

	public static char peekFollowingChar(Document report) {
		if (report.textIndex < report.getText().length() - 1) {
			return report.getText().charAt(report.textIndex + 1);
		} else {
			return (char) -1;
		}
	}

	public static char readNextChar(Document report) {
		if (report.textIndex < report.getText().length()) {
			return report.getText().charAt(report.textIndex++);
		} else {
			return (char) -1;
		}
	}

	public static String stringListConcat(Vector tokens) {
		Vector strings = null;
		for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
			Token token = (Token) e.nextElement();
			if (token.string != null) {
				strings = VUtils.add(strings, token.string);
			}
		}
		return StrUtils.stringListConcat(strings, " ");
	}

	public static Vector<Token> gatherWordTokens(Vector tokens) {
		Vector wtokens = null;
		boolean insideParens = false;
		if (tokens != null) {
			for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
				Token token = (Token) e.nextElement();
				if (token.isLeftParen()) {
					insideParens = true;
				} else if (token.isRightParen()) {
					insideParens = false;
				}
				if (!insideParens
						&& (token.isWordSurrogate() || token.isComma())) {
					wtokens = VUtils.add(wtokens, token);
				}
			}
		}
		if (wtokens != null) {
			Vector v = new Vector(wtokens);
			for (int i = 1; i < v.size() - 1; i++) {
				Token prev = (Token) v.elementAt(i - 1);
				Token curr = (Token) v.elementAt(i);
				Token next = (Token) v.elementAt(i + 1);
				if (curr.isConjunct() && next.isConjunct()) {
					if (curr.isPunctuation()) {
						wtokens.remove(curr);
					} else if (next.isPunctuation()) {
						wtokens.remove(next);
					} else {
						wtokens.remove(curr);
					}
				}
			}
		}
		return wtokens;
	}

	public static String determinePhraseType(Vector<Token> tokens) {
		String ptype = null;
		if (tokens != null) {
			for (Token token : tokens) {
				if (!token.isWordSurrogate()) {
					return null;
				}
			}
			Token firstToken = tokens.firstElement();
			Token lastToken = tokens.lastElement();
			Word firstWord = firstToken.getWord();
			Word lastWord = lastToken.getWord();
			if (lastWord == null || lastWord.isNoun()) {
				if (firstWord == null || firstWord.isNoun()
						|| firstWord.isAdjective()) {
					ptype = "np";
				} else if (firstWord.isVerb()) {
					ptype = "vp";
				}
			}
		}
		return ptype;
	}

	boolean isConjunct() {
		String str = this.string.toLowerCase();
		if ("or".equals(str) || "and".equals(str) || ",".equals(str)) {
			return true;
		}
		return false;
	}

	public static Vector gatherCoveredTokens(Vector tokens, int start, int end) {
		Vector covered = null;
		boolean inarea = false;
		for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
			Token token = (Token) e.nextElement();
			if (token.start >= start && token.end <= end) {
				inarea = true;
				covered = VUtils.add(covered, token);
			} else if (inarea || token.start >= end) {
				break;
			}
		}
		return covered;
	}

	public static Vector gatherWordSubstrings(String str) {
		return gatherWordSubstrings(str, 2);
	}

	public static boolean isSignificantPunctuation(Document report, char ch) {
		if (report.getSignificantPunctuation() != null) {
			for (int i = 0; i < report.getSignificantPunctuation().length; i++) {
				char p = report.getSignificantPunctuation()[i];
				if (p == ch) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isWordCharacterStandin(char ch) {
		for (int i = 0; i < WordCharacterStandins.length; i++) {
			if (ch == WordCharacterStandins[i]) {
				return true;
			}
		}
		return false;
	}

	public static Vector gatherWordSubstrings(String str, int len) {
		Vector substrings = null; // this a synchronization test. Just ignore.
		Vector words = gatherWordStrings(str);
		if (words != null) {
			for (int i = 0; i < words.size(); i++) {
				int end = (i + 2 < words.size() ? i + len : words.size() - 1);
				for (int j = end; j >= i; j--) {
					String substr = StrUtils.stringListConcat(
							VUtils.subVector(words, i, j + 1), " ");
					if (substr != null) {
						substrings = VUtils.add(substrings, substr);
					}
				}
			}
		}
		return substrings;
	}

	public static Vector gatherWordStrings(String str) {
		Vector strings = null;
		if (str != null) {
			Vector tokens = readTokensFromString(str);
			if (tokens != null) {
				for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
					Token token = (Token) e.nextElement();
					if (token.isWordSurrogate()) {
						strings = VUtils.add(strings, token.string);
					}
				}
			}
		}
		return strings;
	}

	public static Vector wrapWordStrings(Vector<String> strings) {
		Vector<Token> tokens = null;
		if (strings != null) {
			int start = 0;
			for (String str : strings) {
				int end = start + str.length();
				Token token = new Token(WORD, str, start, end, null);
				tokens = VUtils.add(tokens, token);
				start = end;
			}
		}
		return tokens;
	}

	// 12/10/2012
	public static Vector<Word> gatherWords(Vector<Token> tokens, boolean isBase) {
		Hashtable<Word, Word> whash = new Hashtable();
		Vector<Word> words = null;
		if (tokens != null) {
			for (Token token : tokens) {
				if (token.isWord() && token.word != null) {
					Word word = token.word;
					if (isBase) {
						word = word.getBase();
					}
					if (word != null) {
						whash.put(word, word);
					}
				}
			}
		}
		for (Enumeration<Word> e = whash.keys(); e.hasMoreElements();) {
			words = VUtils.add(words, e.nextElement());
		}
		return words;
	}

	public static Vector gatherNounAndAdjectiveStrings(String str) {
		Vector strings = null;
		if (str != null) {
			str = str.toLowerCase().trim();
			Vector tokens = readTokensFromString(str);
			if (tokens != null) {
				for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
					Token token = (Token) e.nextElement();
					if (token.isWordSurrogate()) {
						boolean isNounOrAdjective = false;
						String wstr = token.string;
						Word word = Lexicon.currentLexicon.getWord(wstr);
						if (word != null) {
							if (word.getPartsOfSpeech().contains("noun")
									|| word.getPartsOfSpeech().contains("adj")) {
								isNounOrAdjective = true;
							}
						} else {
							isNounOrAdjective = true;
						}
						if (isNounOrAdjective) {
							strings = VUtils.addIfNot(strings, wstr);
						}
					}
				}
			}
		}
		return strings;
	}

	public static int getStartPosition(Vector<Token> tokens) {
		return tokens.firstElement().start;
	}

	public static int getEndPosition(Vector<Token> tokens) {
		return tokens.lastElement().end;
	}

	public static class TextPositionSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Token t1 = (Token) o1;
			Token t2 = (Token) o2;
			if (t1 != null && t2 != null) {
				if (t1.start < t2.start) {
					return -1;
				}
				if (t2.start < t1.start) {
					return 1;
				}
			}
			return 0;
		}
	}

	public static String[] gatherTokenStrings(String str) {
		Vector tokens = Token.readTokensFromString(str);
		Vector v = VUtils.gatherFields(tokens, "string");
		String[] strings = new String[v.size()];
		v.toArray(strings);
		return strings;
	}

	public Object clone() {
		Token newToken = new Token(this.type, this.string, this.start,
				this.end, this.value);
		return newToken;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubtype() {
		return subtype;
	}

	public void setSubtype(int subtype) {
		this.subtype = subtype;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public boolean isFirstuppercase() {
		return firstuppercase;
	}

	public boolean isFirstUppercaseOnly() {
		return this.firstuppercase && !this.alluppercase;
	}

	public void setFirstuppercase(boolean firstuppercase) {
		this.firstuppercase = firstuppercase;
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public void setString(String string) {
		this.string = string;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getTypeString() {
		return typeString;
	}

	public boolean isAllUpperCase() {
		return alluppercase;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public boolean isSentenceDelimiter() {
		if (this.isNewline()) {
			Token next = this.getNextWordToken();
			if (next != null && next.isFirstuppercase()) {
				// 6/26/2015: Causing "transfer to \nCLC" to break.
				// return true;
			}
		}
		boolean result = (this.getType() == LONGNEWLINE || (this
				.isPunctuation() && (this.subtype == PERIOD
				|| this.subtype == SEMICOLON || this.subtype == COLON
				|| this.subtype == QUESTION || this.subtype == EXCLAMATION || this.subtype == ASTERISK)));
		return result;
	}

	private Token getNextWordToken() {
		for (int i = this.getIndex(); i < this.getDocument().getTokenCount(); i++) {
			Token token = this.getDocument().getToken(i);
			if (token.isWord()) {
				return token;
			}
		}
		return null;
	}

	// Before 6/16/2015
	// public boolean isSentenceDelimiter() {
	// boolean result = (this.getType() == LONGNEWLINE || (this
	// .isPunctuation() && (this.subtype == PERIOD
	// || this.subtype == SEMICOLON || this.subtype == COLON
	// || this.subtype == QUESTION || this.subtype == EXCLAMATION)));
	// return result;
	// }

	public boolean isAVPairDelimiter() {
		boolean result = (this.isPunctuation() && (this.getSubtype() == COLON || this
				.getSubtype() == QUESTION));
		return result;
	}

	public boolean isSpace() {
		return (this.getType() == WHITESPACE && this.getSubtype() == SPACE);
	}

	public boolean isWhitespace() {
		return (this.getType() == WHITESPACE);
	}

	public boolean isNonNewlineWhitespace() {
		return this.isWhitespace() && !this.isNewline();
	}

	public boolean isNewline() {
		return this.isWhitespace() && this.subtype == NEWLINE;
	}

	public static boolean isSpace(String str) {
		return " ".equals(str);
	}

	public int getCount() {
		return this.count;
	}

	public void incrementCount() {
		this.count++;
	}

	public boolean isAttributeValueDelimiter() {
		return this.isColon() || this.isQuestionMark();
	}

	public static String getString(Vector<Token> tokens) {
		StringBuffer sb = new StringBuffer();
		if (tokens != null) {
			for (Token token : tokens) {
				sb.append(token.getString());
			}
		}
		return sb.toString();
	}

	public static boolean isNonDelimitingCharacter(char c) {
		if (!Character.isLetterOrDigit(c)) {
			for (int i = 0; i < NonDelimitingPunctuation.length; i++) {
				if (c == NonDelimitingPunctuation[i]) {
					return true;
				}
			}
		}
		return false;
	}

	public Vector<String> generateMoonstoneIndex() {
		Vector<String> indexes = null;
		String index = this.getTypeString();
		indexes = VUtils.add(indexes, index);
		String stname = null;
		if (this.isWord()) {
			stname = this.determineWordSubtypeName();
		} else {
			stname = this.getSubtypeName();
		}
		if (stname != null) {
			index += ":" + stname;
		}
		indexes = VUtils.addIfNot(indexes, index);
		return indexes;
	}

	// private String determinePunctuationSubtypeName() {
	// if (this.isPunctuation() || this.isWhitespace()) {
	// for (int i = 0; i < punctuation.length; i++) {
	// if (punctuation[i].equals(this.getString())) {
	// return punctuationNames[i];
	// }
	// }
	// return this.getString();
	// }
	// return null;
	// }

	private String determineWordSubtypeName() {
		if (this.isWord()) {
			if (this.isAllUpperCase()) {
				return "uppercase";
			}
			if (this.isFirstuppercase()) {
				return "firstuppercase";
			}
			return "alllowercase";
		}
		return null;
	}

	private String getSubtypeName() {
		return SubtypeNameHash.get(this.getString());
	}

	public int getWordIndex() {
		return wordIndex;
	}

	public void determineWordSubtype() {
		this.firstuppercase = (Character.isUpperCase(this.string.charAt(0)));
		if (this.firstuppercase) {
			this.alluppercase = true;
			for (int i = 0; i < this.string.length(); i++) {
				char ch = this.string.charAt(i);
				if (Character.isLowerCase(ch)) {
					this.alluppercase = false;
					break;
				}
			}
		}
		this.subtype = (alluppercase ? ALLUPPERCASE
				: (firstuppercase ? FIRSTUPPERCASE : ALLLOWERCASE));
	}
	
	//4/12/2016- So I can create terminal annotations using token type and
	// subtype as indexes.
	public String getGrammarConcept() {
		if (this.isCheckbox()) {
			if (this.subtype == CHECKED) {
				return ":CHECKBOX_CHECKED:";
			}
			if (this.subtype == UNCHECKED) {
				return ":CHECKBOX_UNCHECKED:";
			}
		}
		return null;
	}

}
