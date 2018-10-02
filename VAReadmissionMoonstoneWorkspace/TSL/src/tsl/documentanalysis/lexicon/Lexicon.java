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
package tsl.documentanalysis.lexicon;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class Lexicon {
	public Hashtable<String, Word> wordHash = new Hashtable();

	public static Lexicon currentLexicon = null;

	public Lexicon() {
		currentLexicon = this;
	}

	public Lexicon(String lexiconstr) {
		currentLexicon = this;

	}

	public void storeCUIStructures() {
		for (Enumeration<Word> e = this.wordHash.elements(); e
				.hasMoreElements();) {
			Word word = e.nextElement();
			Word base = word.getBase();
			if (base.CUIPhrases != null) {
				Collections.sort(base.CUIPhrases,
						new CUIStructureShort.InverseWordLengthSorter());
			}
			Vector<CUIStructureShort> cps = base.getAllCUIPhrases();
			if (!base.isVisited() && cps != null) {
				base.setVisited(true);
				for (CUIStructureShort cp : cps) {
					if (cp.getWordCount() == 1) {
						base.cuiSingletons = VUtils.add(base.cuiSingletons, cp);
					} else {
						for (int i = 0; i < cp.getWordCount(); i++) {
							Word other = cp.getWords()[i].getBase();
							if (word != other) {
								VUtils.pushHashVector(base.getWordCUIHash(),
										other.getString(), cp);
							}
						}
					}
				}
			}
		}
		for (Enumeration<Word> e = this.wordHash.elements(); e
				.hasMoreElements();) {
			Word word = e.nextElement();
			Word base = word.getBase();
			for (Enumeration<String> ce = base.getWordCUIHash().keys(); ce
					.hasMoreElements();) {
				String key = ce.nextElement();
				Vector<CUIStructureShort> cps = base.getWordCUIHash().get(key);
				Collections.sort(cps,
						new CUIStructureShort.InverseWordLengthSorter());
			}
			base.setVisited(false);
		}

		// 12/8/2012
		UMLSStructuresShort umlss = UMLSStructuresShort.getUMLSStructures();
		umlss.postProcessCUIStructures();
	}

	public void resolveWordVariants() {
		for (Enumeration<Word> e = this.wordHash.elements(); e
				.hasMoreElements();) {
			Word word = e.nextElement();
			word.addWordVariant(word);
			if (word.getBase() != null) {
				word.appendWordVariants(word.getBase().getWordVariants());
			}
		}
	}

	public void resolveBaseWords() {
		for (Enumeration<Word> e = this.wordHash.elements(); e
				.hasMoreElements();) {
			Word word = e.nextElement();
			if (word.base == null && word.baseString != null) {
				word.setBase(this.getWord(word.baseString));
				if (word.getBase() != null) {
					word.getBase().addWordVariant(word);
					word.getBase().addWordVariant(word.getBase());
				}
			}
		}
	}

	public Vector getWords(Vector<String> strings) {
		Vector<Word> words = new Vector(0);
		for (String wstr : strings) {
			Word newword = getWord(wstr, true);

			// 10/26/2012
			if (newword.getBase() == null) {
				newword.setBase(newword);
			}
			if (newword.getWordVariants() == null) {
				newword.addWordVariant(newword);
			}

			words.add(newword);
		}
		return words;
	}

	public Vector<Word> getAllWords() {
		Vector<Word> words = HUtils.getElements(this.getWordHash());
		Collections.sort(words, new Word.WordSorter());
		return words;
	}

	// 8/22/2009 Before attempting to use MySQL
	public Word getWord(String word, boolean createIfNotFound) {
		Word w = null;
		if (word != null) {
			String lcword = word.toLowerCase();
			w = (Word) wordHash.get(lcword);
			if (w == null && createIfNotFound) {
				w = new Word(this, lcword);
			}
		}
		return w;
	}

	public Word getWord(String word) {
		return getWord(word, false);
	}

	public void putWord(String str, Word word) {
		wordHash.put(str, word);
	}

	public String toLispString() {
		StringBuffer sb = new StringBuffer();
		sb.append("'(\n");
		for (Enumeration<Word> e = Lexicon.currentLexicon.wordHash.elements(); e
				.hasMoreElements();) {
			Word word = e.nextElement();
			sb.append(word.toLispString() + "\n");
		}
		sb.append("\n)");
		return sb.toString();
	}

	public Hashtable<String, Word> getWordHash() {
		return wordHash;
	}

	public void setWordHash(Hashtable wordHash) {
		this.wordHash = wordHash;
	}

}
