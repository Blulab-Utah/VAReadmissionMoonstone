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
package tsl.tsllisp;

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
	public static int FUNCTION = 7;
	public static int QUOTE = 8;
	public static int QUASIQUOTE = 9;
	public static int UNQUOTE = 10;
	public static int UNQUOTESPLICING = 11;

	public static String[] tokenNames = { "NOTOKEN", "LPAREN", "RPAREN",
			"Symbol", "String", "Number", "Dot", "Function", "Quote", 
			"Quasiquote", "Unquote", "UnquoteSplicing"};

	public static Token EMPTYTOKEN = new Token(-1);
	public static Token STARTPARENTOKEN = new Token(STARTPAREN);
	public static Token ENDPARENTOKEN = new Token(ENDPAREN);
	public static Token DOTTOKEN = new Token(DOT);
	public static Token FUNCTIONTOKEN = new Token(FUNCTION);
	public static Token QUOTETOKEN = new Token(QUOTE);
	public static Token QUASIQUOTETOKEN = new Token(QUASIQUOTE);
	public static Token UNQUOTETOKEN = new Token(UNQUOTE);
	public static Token UNQUOTESPLICINGTOKEN = new Token(UNQUOTESPLICING);
	private static char StartStringChar = (char) -1;
	private static char[] VALID_CHARACTERS = new char[] { '.', '_', '-', '/',
			'?', '+', ':', '*', '<', '>', '%', '|', ',', ';', '\\', '=', '#' };

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

	public static Token peekNextToken(TLisp jl) {
		return (jl.tokenIndex >= jl.tokens.size() ? null : (Token) jl.tokens
				.elementAt(jl.tokenIndex));
	}

	public static Token readNextToken(TLisp jl) throws Exception {
		if (jl.tokens == null) {
			return null;
		}
		return (jl.tokenIndex >= jl.tokens.size() ? null : (Token) jl.tokens
				.elementAt(jl.tokenIndex++));
	}

	public static Vector readTokensFromInput(TLisp jl) {
		Token t = null;
		Vector tokens = new Vector(0);
		while ((t = readNextTokenFromInput(jl)) != null) {
			if (!t.isEmptyToken()) {
				tokens.add(t);
			}
		}
		return tokens;
	}

	public static Token readNextTokenFromInput(TLisp jl) {
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
		} else if (peekNextString(jl, ",@")) {
			token = UNQUOTESPLICINGTOKEN;
			jl.inputIndex += 2;
		} else if (ch == '`') {
			readNextChar(jl);
			token = QUASIQUOTETOKEN;
		} else if (ch == ',') {
			readNextChar(jl);
			token = UNQUOTETOKEN;
		} else if (peekNextString(jl, "#'")) {
			token = FUNCTIONTOKEN;
			jl.inputIndex += 2;
		} else if (Character.isWhitespace(ch)) {
			if (ch == '\n') {
				// Later: Store CLN with each token, and refer to line number
				// when handling errors.
				jl.currentLineNumber++;
			}
			eatWhitespace(jl);
			token = readNextTokenFromInput(jl);
		} else if (peekNextString(jl, ";")) {
			eatComment(jl);
			token = readNextTokenFromInput(jl);
		} else if (isValidSymbolCharacter(ch)) {
			TLObject to = readSymbol(jl);
			token = new Token(SYMBOL, to);
		} else {
			if (jl.inputIndex < jl.inputStringLength) {
				readNextChar(jl);
				token = EMPTYTOKEN;
			}
		}
		return token;
	}

	static void eatComment(TLisp jl) {
		while (readNextChar(jl) != '\n') {
			;
		}
	}

	static void eatWhitespace(TLisp jl) {
		while (jl.inputIndex < jl.inputStringLength
				&& Character.isWhitespace(jl.inputString.charAt(jl.inputIndex))) {
			jl.inputIndex++;
		}
	}

	static TLObject readSymbol(TLisp jl) {
		StringBuffer sb = new StringBuffer();
		char ch;
		while (jl.inputIndex < jl.inputStringLength
				&& isValidSymbolCharacter((ch = jl.inputString
						.charAt(jl.inputIndex)))) {
			sb.append(ch);
			jl.inputIndex++;
		}
		String str = sb.toString();
		if ("nil".equals(str.toLowerCase())) {
			return TLUtils.getNIL();
		}
		if ("t".equals(str.toLowerCase())) {
			return TLUtils.getT();
		}
		Symbol sym = SymbolTable.getGlobalSymbol(str, null);
		if (sym == null) {
			sym = SymbolTable.getFunctionSymbol(str);
		}
		if (sym == null) {
			sym = new Symbol(str);
		}
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

	static String readString(TLisp jl) {
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
		String str = sb.toString();
		return str;
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

	static Float readFloat(TLisp jl) {
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

	public static boolean peekNextString(TLisp jl, String str) {
		if (jl.inputIndex + str.length() <= jl.inputStringLength) {
			String substr = jl.inputString.substring(jl.inputIndex,
					jl.inputIndex + str.length());
			return (str.equals(substr));
		} else {
			return false;
		}
	}

	public static char peekNextChar(TLisp jl) {
		if (jl.inputIndex < jl.inputStringLength) {
			return jl.inputString.charAt(jl.inputIndex);
		} else {
			return (char) -1;
		}
	}

	public static char peekFollowingChar(TLisp jl) {
		if (jl.inputIndex < jl.inputStringLength - 1) {
			return jl.inputString.charAt(jl.inputIndex + 1);
		} else {
			return (char) -1;
		}
	}

	public static char readNextChar(TLisp jl) {
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
