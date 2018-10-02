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
package moonstone.learning.basilisk;

import java.util.Vector;

import tsl.utilities.VUtils;

public class Lexicon {
	
	Vector<String> words = null;
	
	public Lexicon(String[] words) {
		if (words != null) {
			this.words = VUtils.arrayToVector(words);
		}
	}
	
	public void addWords(Vector<String> words) {
		this.words = VUtils.append(this.words, words);
	}
	
	public void addWord(String word) {
		this.words = VUtils.add(this.words, word);
	}

	public Vector<String> getWords() {
		return words;
	}
	
	public boolean containsWord(String word) {
		return this.words.contains(word);
	}
	
	public boolean containsSubword(String word) {
		for (String lword : this.getWords()) {
			if (word.contains(lword)) {
				return true;
			}
		}
		return false;
	}

}
