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

import tsl.utilities.VUtils;

public class Parser {

	String inputString = null;
	String token = null;
	int number = 0;
	int tokenIndex = 0;
	Vector<Token> tokens = null;
	Vector<Classification> classifications = null;
	int inputIndex = 0;
	int inputStringLength = 0;

	public Parser(String str) {
		this.inputString = str;
		this.inputStringLength = str.length();
		this.tokens = Token.readTokensFromInput(this);
		gatherClassifications();
	}

	void gatherClassifications() {
		if (this.tokens != null) {
			for (int i = 0; i < this.tokens.size(); i++) {
				Token token = this.tokens.elementAt(i);
				Token next1 = null;
				Token next2 = null;
				Classification c = null;
				if (i < this.tokens.size() - 2) {
					next1 = this.tokens.elementAt(i + 1);
					next2 = this.tokens.elementAt(i + 2);
				}
				if (token.isCharEqualsString()) {
					if (next2 != null) {
						c = new Classification(token, next1, next2);
					} else {
						c = new Classification(token);
					}
					this.classifications = VUtils.add(this.classifications, c);
				}
			}
		}
	}
	
	public String getInputString() {
		return inputString;
	}

	public int getInputIndex() {
		return inputIndex;
	}

	public void setInputIndex(int inputIndex) {
		this.inputIndex = inputIndex;
	}

	public int getInputStringLength() {
		return inputStringLength;
	}

	public int getTokenIndex() {
		return tokenIndex;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

}
