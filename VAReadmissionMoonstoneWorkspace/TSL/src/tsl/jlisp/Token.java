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
package tsl.jlisp;

import java.util.Vector;

public class Token {
	int type = -1;
	Object value = null;

	public static int STARTPAREN = 1;
	public static int ENDPAREN = 2;
	public static int SYMBOL = 3;
	public static int STRING = 4;
	public static int NUMBER = 5;
	public static int DOT = 6;
	public static int QUOTE = 7;
	public static int FUNCTION = 8;
	public static int BACKQUOTE = 9;
	public static int COMMA = 10;

	public static String[] tokenNames = { "NOTOKEN", "LPAREN", "RPAREN",
			"Symbol", "String", "Number", "Dot", "Quote", "Function" };

	public static Token EMPTYTOKEN = new Token(-1);
	public static Token STARTPARENTOKEN = new Token(STARTPAREN);
	public static Token ENDPARENTOKEN = new Token(ENDPAREN);
	public static Token DOTTOKEN = new Token(DOT);
	public static Token QUOTETOKEN = new Token(QUOTE);
	public static Token BACKQUOTETOKEN = new Token(BACKQUOTE);
	public static Token COMMATOKEN = new Token(COMMA);
	public static Token FUNCTIONTOKEN = new Token(FUNCTION);
	
	private static char StartStringChar = (char) -1;

	// 12/26/2012
	private static char[] VALID_CHARACTERS = new char[] { '.', '_', '-', '/',
		'?', '+', ':', '*', '<', '>', '%', '|', ',', ';', '\\' };
	
	// Before 12/26/2012 -- In Knowtator files, I'm treating [...] as Strings, since
	// they may contain spaces and upper/lower case letters.
//	private static char[] VALID_CHARACTERS = new char[] { '.', '_', '-', '/',
//			'?', '+', ':', '*', '<', '>', '[', ']', '%', '|', ',', ';', '\\' };

	Token(int type) {
		this.type = type;
	}

	Token(int type, Object value) {
		this.type = type;
		this.value = value;
	}

	public String toString() {
		String str = "[" + tokenNames[this.type] + ":";
		if (this.value != null) {
			str += this.value.toString();
		} else {
			str += "*";
		}
		str += "]";
		return str;
	}

	public static Token peekNextToken(JLisp jl) {
		return (jl.tokenIndex >= jl.tokens.size() ? null : (Token) jl.tokens
				.elementAt(jl.tokenIndex));
	}

	public static Token readNextToken(JLisp jl) throws JLispException {
		return (jl.tokenIndex >= jl.tokens.size() ? null : (Token) jl.tokens
				.elementAt(jl.tokenIndex++));
	}

	public static Vector readTokensFromInput(JLisp jl) {
		Token t = null;
		Vector tokens = new Vector(0);
		while ((t = readNextTokenFromInput(jl)) != null) {
			if (!t.isEmptyToken()) {
				tokens.add(t);
			}
		}
		return tokens;
	}

	public static Token readNextTokenFromInput(JLisp jl) {
		Token token = null;
		char ch = peekNextChar(jl);
		char fch = peekFollowingChar(jl);
		if (ch == '(') {
			readNextChar(jl);
			token = STARTPARENTOKEN;
		} else if (ch == ')') {
			readNextChar(jl);
			token = ENDPARENTOKEN;
		} else if (Character.isDigit(ch)
				|| (ch == '-' && Character.isDigit(fch))) {
			Float f = readFloat(jl);
			token = new Token(NUMBER, f);
		} else if (isStringStartDelim(ch)) {
			StartStringChar = ch;
			String str = readString(jl);
			token = new Token(STRING, str);
			StartStringChar = (char) -1;
		} else if (ch == '.') {
			readNextChar(jl);
			token = DOTTOKEN;
		} else if (ch == '\'') {
			readNextChar(jl);
			token = QUOTETOKEN;
		} else if (peekNextString(jl, "#'")) {
			token = FUNCTIONTOKEN;
			jl.inputIndex += 2;
		} else if (ch == '`') {
			readNextChar(jl);
			token = BACKQUOTETOKEN;
		} else if (ch == ',') {
			readNextChar(jl);
			token = COMMATOKEN;
		} else if (Character.isWhitespace(ch)) {
			eatWhitespace(jl);
			token = readNextTokenFromInput(jl);
		} else if (peekNextString(jl, ";")) {
			eatComment(jl);
			token = readNextTokenFromInput(jl);
		} else if (isValidSymbolCharacter(ch)) {
			Symbol sym = readSymbol(jl);
			token = new Token(SYMBOL, sym);
		} else {
			if (jl.inputIndex < jl.inputStringLength) {
				readNextChar(jl);
				token = EMPTYTOKEN;
			}
		}
		return token;
	}

	static void eatComment(JLisp jl) {
		while (readNextChar(jl) != '\n') {
			;
		}
	}

	static void eatWhitespace(JLisp jl) {
		while (jl.inputIndex < jl.inputStringLength
				&& Character.isWhitespace(jl.inputString.charAt(jl.inputIndex))) {
			jl.inputIndex++;
		}
	}

	static Symbol readSymbol(JLisp jl) {
		StringBuffer sb = new StringBuffer();
		char ch;
		while (jl.inputIndex < jl.inputStringLength
				&& isValidSymbolCharacter((ch = jl.inputString
						.charAt(jl.inputIndex)))) {
			sb.append(ch);
			jl.inputIndex++;
		}
		String str = sb.toString();
		Symbol sym = SymbolTable.getGlobalSymbol(str, null);
		return sym;
	}

	static boolean isValidSymbolCharacter(char ch) {
		if (Character.isLetter(ch) || Character.isDigit(ch)) {
			return true;
		}
		for (int i = 0; i < VALID_CHARACTERS.length; i++) {
			if (ch == VALID_CHARACTERS[i]) {
				return true;
			}
		}
		return false;
	}

	static String readString(JLisp jl) {
		StringBuffer sb = new StringBuffer();
		char ch = jl.inputString.charAt(jl.inputIndex);
		if (isStringStartDelim(ch)) {
			jl.inputIndex++;
			while (jl.inputIndex < jl.inputStringLength
					&& !isStringEndDelim(ch = jl.inputString
							.charAt(jl.inputIndex))) {
				sb.append(ch);
				jl.inputIndex++;
			}
			jl.inputIndex++;
		}
		return sb.toString();
	}
	
	static boolean isStringStartDelim(char c) {
		return c == '"' || c == '[';
	}
	
	static boolean isStringEndDelim(char c) {
		if (c == StartStringChar || (StartStringChar == '[' && c == ']')) {
			return true;
		}
		return false;
	}

	static Float readFloat(JLisp jl) {
		char ch;
		StringBuffer sb = new StringBuffer();
		while (jl.inputIndex < jl.inputStringLength
				&& (Character
						.isDigit(ch = jl.inputString.charAt(jl.inputIndex))
						|| ch == '.' || ch == '-')) {
			sb.append(ch);
			jl.inputIndex++;
		}
		return new Float(sb.toString());
	}

	public static boolean peekNextString(JLisp jl, String str) {
		if (jl.inputIndex + str.length() <= jl.inputStringLength) {
			String substr = jl.inputString.substring(jl.inputIndex,
					jl.inputIndex + str.length());
			return (str.equals(substr));
		} else {
			return false;
		}
	}

	public static char peekNextChar(JLisp jl) {
		if (jl.inputIndex < jl.inputStringLength) {
			return jl.inputString.charAt(jl.inputIndex);
		} else {
			return (char) -1;
		}
	}

	public static char peekFollowingChar(JLisp jl) {
		if (jl.inputIndex < jl.inputStringLength - 1) {
			return jl.inputString.charAt(jl.inputIndex + 1);
		} else {
			return (char) -1;
		}
	}

	public static char readNextChar(JLisp jl) {
		if (jl.inputIndex < jl.inputStringLength) {
			return jl.inputString.charAt(jl.inputIndex++);
		} else {
			return (char) -1;
		}
	}
	
	public boolean isEmptyToken() {
		return this.type == -1;
	}

}
