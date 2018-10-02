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
package io.i2b2;

import java.util.Vector;

public class Token {
	int type = -1;
	int	lineval = -1;
	int wordval = -1;
	String value = null;
	char startChar = (char) -1;

	static int Empty = -1;
	static int CharEqualsString = 0;
	static int StartEnd = 1;
	static int DoubleBar = 2;
	static Token EmptyToken = new Token(Empty);

	public static String[] tokenNames = { "CharEqualsString", "StartEnd", "DoubleBar"};
	
	Token(int type) {
		this.type = type;
	}

	Token(int type, String value) {
		this.type = type;
		this.value = value;
	}

	public static Token peekNextToken(Parser parser) {
		return (parser.tokenIndex >= parser.tokens.size() ? null : (Token) parser.tokens
				.elementAt(parser.tokenIndex));
	}

	public static Token readNextToken(Parser parser) throws Exception {
		return (parser.tokenIndex >= parser.tokens.size() ? null : (Token) parser.tokens
				.elementAt(parser.tokenIndex++));
	}

	public static Vector readTokensFromInput(Parser parser) {
		Token t = null;
		Vector<Token> tokens = new Vector(0);
		while ((t = readNextTokenFromInput(parser)) != null) {
			if (!t.isEmptyToken()) {
				tokens.add(t);
				parser.tokenIndex++;
			}
		}
		return tokens;
	}

	public static Token readNextTokenFromInput(Parser parser) {
		Token token = null;
		char ch = peekNextChar(parser);
		if (Character.isWhitespace(ch)) {
			eatWhitespace(parser);
			token = readNextTokenFromInput(parser);
		} else if ((token = readDoubleBar(parser)) != null
				|| (token = readCharEqualsString(parser)) != null
				|| (token = readStartEnd(parser)) != null) {
			;
		} else if (parser.inputIndex < parser.inputStringLength) {
			readNextChar(parser);
			token = EmptyToken;
		}
		return token;
	}

	static void eatWhitespace(Parser parser) {
		while (parser.inputIndex < parser.inputStringLength
				&& Character.isWhitespace(parser.inputString.charAt(parser.inputIndex))) {
			parser.inputIndex++;
		}
	}
	
	static Token readDoubleBar(Parser parser) {
		int index = parser.inputIndex;
		String str = parser.inputString;
		Token token = null;
		if (index < parser.inputStringLength - 2
				&& str.charAt(index) == '|'
					&& str.charAt(index+1) == '|') {
			parser.inputIndex += 2;
			token = new Token(DoubleBar);
		}
		return token;
	}
	
	static Token readCharEqualsString(Parser parser) {
		char ch;
		StringBuffer sb = new StringBuffer();
		int index = parser.inputIndex;
		String str = parser.inputString;
		Token token = null;
		char startChar = (char) -1;
		if (index < parser.inputStringLength - 2
				&& Character.isLowerCase(str.charAt(index))
				&& str.charAt(index + 1) == '='
				&& str.charAt(index + 2) == '"') {
			startChar = str.charAt(index);
			index += 3;
			while ((ch = str.charAt(index++)) != '"'
					&& index < parser.inputStringLength) {
				sb.append(ch);
				;
			}
		}
		if (startChar != (char) -1) {
			parser.inputIndex = index;
			token = new Token(CharEqualsString, sb.toString());
			token.startChar = startChar;
		}
		return token;
	}
	
	static Token readStartEnd(Parser parser) {
		char ch;
		StringBuffer sb = new StringBuffer();
		boolean foundColon = false;
		boolean foundDigit = false;
		boolean isValid = true;
		int index = parser.inputIndex;
		String str = parser.inputString;
		Token token = null;
		while (index < parser.inputStringLength) {
			ch = str.charAt(index++);
			if (Character.isDigit(ch)) {
				foundDigit = true;
				sb.append(ch);
			} else if (!foundColon && ch == ':') {
				foundColon = true;
				sb.append(ch);
			} else {
				if (!foundDigit || !foundColon) {
					isValid = false;
				}
				break;
			}
		}
		if (foundDigit && foundColon && isValid) {
			String se = sb.toString();
			String[] ss = se.split(":");
			if (ss.length == 2) {
				token = new Token(StartEnd);
				token.lineval = Integer.parseInt(ss[0]);
				token.wordval = Integer.parseInt(ss[1]);
				parser.inputIndex = index;
			}
		}
		return token;
	}

	public static char peekNextChar(Parser parser) {
		if (parser.inputIndex < parser.inputStringLength) {
			return parser.inputString.charAt(parser.inputIndex);
		} else {
			return (char) -1;
		}
	}

	public static char peekFollowingChar(Parser parser) {
		if (parser.inputIndex < parser.inputStringLength - 1) {
			return parser.inputString.charAt(parser.inputIndex + 1);
		} else {
			return (char) -1;
		}
	}

	public static char readNextChar(Parser parser) {
		if (parser.inputIndex < parser.inputStringLength) {
			return parser.inputString.charAt(parser.inputIndex++);
		} else {
			return (char) -1;
		}
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
	
	public boolean isCharEqualsString() {
		return this.type == CharEqualsString;
	}
	
	public boolean isStartEnd() {
		return this.type == StartEnd;
	}
	
	public boolean isDoubleBar() {
		return this.type == DoubleBar;
	}
	
	public boolean isEmptyToken() {
		return this.type == -1;
	}

}

