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

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.utilities.SetUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class Word {
	private Lexicon lex = null;

	private String string = null;

	private Vector partsOfSpeech = null;

	private Vector values = null;

	private Vector nounForms = null;

	private Vector verbForms = null;

	private Vector variants = null;

	private Vector<String> formTypes = null;

	private Vector<String> formValues = null;

	private Vector<String> spellingVariants = null;

	private Vector<Word> wordVariants = null;

	protected Word base = null;

	protected String baseString = null;

	private int hashCode = 0;

	public Vector<CUIStructureShort> CUIPhrases = null;

	public Hashtable<String, CUIStructureShort> CUIPhraseHash = new Hashtable();

	public boolean visited = false;

	public boolean isNew = false;

	public boolean isUpdated = false;

	// 12/3/2012
	public Hashtable<String, Vector<CUIStructureShort>> wordCUIHash = new Hashtable();
	public Vector<CUIStructureShort> cuiSingletons = null;

	public static Vector<String> defaultPartsOfSpeech = VUtils
			.arrayToVector(new String[] { "noun", "adj" });

	public Word(Lexicon lex, String str) {
		this.lex = lex;
		this.string = str;
		this.partsOfSpeech = defaultPartsOfSpeech;
		this.lex.putWord(str, this);
	}

	public Word(Lexicon lex, String word, String base,
			Vector<String> partsOfSpeech, Vector<String> formTypes,
			Vector<String> formValues, Vector<String> variants,
			Vector<String> spellingVariants) {
		this.lex = lex;
		this.string = word;
		this.baseString = base;
		this.partsOfSpeech = partsOfSpeech;
		this.formTypes = formTypes;
		this.formValues = formValues;
		this.variants = variants;
		this.spellingVariants = spellingVariants;
		this.lex.putWord(this.string, this);
	}

	public Word(Lexicon lex, WordEntry wentry) {
		this(lex, wentry.getWord(), wentry.getBase(),
				wentry.getPartsOfSpeech(), wentry.getFormTypes(), wentry
						.getFormValues(), wentry.getVariants(), wentry
						.getSpellingVariants());
	}

	public Word getBase() {
		if (this.base != null && !this.equals(this.base)) {
			return this.base.getBase();
		}
		return this;
	}

	public Word findMatchingVariant(Vector<Word> words) {
		if (words != null) {
			for (Word word : words) {
				if (this.isWordVariant(word)) {
					return word;
				}
			}
		}
		return null;
	}

	// 7/27/2012
	public Word findMatchingVariant(Word[] words) {
		if (words != null) {
			for (Word word : words) {
				if (this.isWordVariant(word)) {
					return word;
				}
			}
		}
		return null;
	}

	public boolean isWordVariant(Word word) {
		return (this.equals(word)
				|| (this.getWordVariants() != null && this.getWordVariants()
						.contains(word)) || (word.getWordVariants() != null && word
				.getWordVariants().contains(this)));
	}

	// boolean existsInDocumentCorpus() {
	// if
	// (KnowledgeBase.getDocumentAccess().wordExistsInCorpus(this.getString())
	// || RequiredCUIs.isRequiredWord(this.getString())) {
	// return true;
	// }
	// if (this.getBase() != null
	// && (KnowledgeBase.getDocumentAccess().wordExistsInCorpus(
	// this.getBase().getString()) || RequiredCUIs
	// .isRequiredWord(this.getBase().getString()))) {
	// return true;
	// }
	// if (this.getVariants() != null) {
	// for (Word variant : this.getVariants()) {
	// if (KnowledgeBase.getDocumentAccess().wordExistsInCorpus(
	// variant.getString())
	// || RequiredCUIs.isRequiredWord(variant.getString())) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	public static boolean same(Vector<Word> v1, Vector<Word> v2) {
		return (v1 != null && v2 != null && v1.size() == v2.size() && isSubset(
				v1, v2));
	}

	// 7/27/2012
	public static boolean same(Word[] v1, Word[] v2) {
		return (v1 != null && v2 != null && v1.length == v2.length && isSubset(
				v1, v2));
	}

	public static boolean isStrictSubset(Vector<Word> v1, Vector<Word> v2) {
		return isSubset(v1, v2) && v1.size() < v2.size();
	}

	// 7/27/2012
	public static boolean isStrictSubset(Word[] v1, Word[] v2) {
		return isSubset(v1, v2) && v1.length < v2.length;
	}

	public static boolean isSubset(Vector<Word> v1, Vector<Word> v2) {
		if (v1 == null || v2 == null || v1.size() > v2.size()) {
			return false;
		}
		for (Word w1 : v1) {
			if (w1.findMatchingVariant(v2) == null) {
				return false;
			}
		}
		return true;
	}

	// 7/27/2012 -- Accomodating the shorter CUI structure
	public static boolean isSubset(Word[] v1, Word[] v2) {
		if (v1 == null || v2 == null || v1.length > v2.length) {
			return false;
		}
		for (Word w1 : v1) {
			if (w1.findMatchingVariant(v2) == null) {
				return false;
			}
		}
		return true;
	}

	// If a word has a base, the base's variants will include the word itself:
	// Add the base
	// plus the base's variants. If it has no base, add the word itself, and add
	// it's
	// variants (which it will have if it *is* a base).

	public Vector<Word> getWordVariants() {
		if (this.wordVariants == null) {
			this.wordVariants = VUtils.listify(this);
		}
		return this.wordVariants;
	}

	public void addWordVariant(Word variant) {
		this.wordVariants = VUtils.addIfNot(this.wordVariants, variant);
	}

	public void appendWordVariants(Vector<Word> variants) {
		this.wordVariants = VUtils.append(this.wordVariants, variants);
	}

	public Vector<String> getVariants() {
		return this.variants;
	}

	// Before 10/21/2012
	// public Vector<Word> getVariants() {
	// if (this.variants == null) {
	// this.variants = new Vector<Word>(0);
	// if (this.base != null) {
	// this.variants = VUtils.addIfNot(this.variants, this.base);
	// this.variants = VUtils.appendIfNot(variants,
	// this.base.wordVariants);
	// } else {
	// this.variants = VUtils.addIfNot(this.variants, this);
	// this.variants = VUtils.appendIfNot(variants, this.wordVariants);
	// }
	// }
	// return this.variants;
	// }

	public static String determinePhraseType(Vector<Word> words) {
		String ptype = null;
		if (words != null) {
			for (Word word : words) {
				if (word == null || word.isConjunct()) {
					return null;
				}
			}
			Word firstWord = words.firstElement();
			Word lastWord = words.lastElement();
			if (lastWord.isNoun()) {
				if (firstWord.isNoun() || firstWord.isAdjective()) {
					ptype = "np";
				} else if (firstWord.isVerb()) {
					ptype = "vp";
				}
			}
		}
		return ptype;
	}

	// 5/5/2011: Accomodates assumption that unknown word is noun or adjective
	public boolean isNoun() {
		return this.partsOfSpeech == null
				|| this.partsOfSpeech.contains("noun");
	}

	public boolean isAdjective() {
		return this.partsOfSpeech == null || this.partsOfSpeech.contains("adj");
	}

	public boolean isConjunct() {
		return this.partsOfSpeech != null
				&& this.partsOfSpeech.contains("conj");
	}

	public boolean isModal() {
		return this.partsOfSpeech != null
				&& this.partsOfSpeech.contains("modal");
	}

	public boolean isVerb() {
		return this.partsOfSpeech != null
				&& this.partsOfSpeech.contains("verb");
	}

	public boolean isAdverb() {
		return this.partsOfSpeech != null && this.partsOfSpeech.contains("adv");
	}

	public boolean isPrep() {
		return this.partsOfSpeech != null
				&& this.partsOfSpeech.contains("prep");
	}

	public boolean isDet() {
		return this.partsOfSpeech != null && this.partsOfSpeech.contains("det");
	}
	
	public boolean isAux() {
		return this.partsOfSpeech != null && this.partsOfSpeech.contains("aux");
	}

	public boolean hasPartOfSpeech(String pos) {
		return pos != null && this.partsOfSpeech != null
				&& this.partsOfSpeech.contains(pos);
	}

	public static boolean isNoun(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word == null || word.isNoun());
	}

	public static boolean isAdjective(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word == null || word.isAdjective());
	}

	public static boolean isConjunct(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word != null && word.isConjunct());
	}

	public static boolean isVerb(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word != null && word.isVerb());
	}

	public static boolean isAdverb(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word != null && word.isAdverb());
	}

	public static boolean isPrep(String str) {
		Word word = Lexicon.currentLexicon.getWord(str);
		return (word != null && word.isPrep());
	}

	public boolean samePartOfSpeech(Word word) {
		return (SetUtils.intersection(this.partsOfSpeech, word.partsOfSpeech) != null);
	}

	public String toString() {
		return this.string;
	}

	public static String toString(Word[] words) {
		StringBuffer sb = new StringBuffer();
		if (words != null) {
			for (int i = 0; i < words.length; i++) {
				Word word = words[i];
				sb.append(word.string);
				if (i < words.length - 1) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

	public String toLispString() {
		String baseString = (getBase() != null ? getBase().getString() : "*");
		String partsOfSpeech = StrUtils.stringListConcat(
				this.getPartsOfSpeech(), ",");
		String variants = StrUtils.stringListConcat(this.getVariants(), ",");
		String str = "(\"" + getString() + "\" \"" + baseString + "\" \""
				+ partsOfSpeech + "\" \"" + variants + "\")";
		return str;
	}

	public String toFileString() {
		String baseString = (getBase() != null ? getBase().getString() : "*");
		String partsOfSpeech = StrUtils.stringListConcat(
				this.getPartsOfSpeech(), ",");
		String variants = StrUtils.stringListConcat(this.getVariants(), ",");
		String str = getString() + ":" + baseString + ":" + partsOfSpeech + ":"
				+ variants;
		return str;
	}

	public boolean equals(Object o) {
		if (o.getClass().equals(Word.class)) {
			Word word = (Word) o;
			return this.string.equals(word.string);
		}
		return false;
	}

	public int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = this.string.hashCode();
		}
		return this.hashCode;
	}

	public static String stringListConcat(Vector words) {
		Vector strings = null;
		for (Enumeration e = words.elements(); e.hasMoreElements();) {
			Word word = (Word) e.nextElement();
			strings = VUtils.add(strings, word.string);
		}
		return StrUtils.stringListConcat(strings, " ");
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Vector<String> getPartsOfSpeech() {
		return partsOfSpeech;
	}

	public void setPartsOfSpeech(Vector partsOfSpeech) {
		this.partsOfSpeech = partsOfSpeech;
	}

	public Vector getSpellingVariants() {
		return spellingVariants;
	}

	public void setSpellingVariants(Vector spellingVariants) {
		this.spellingVariants = spellingVariants;
	}

	public Vector<CUIStructureShort> getCUIPhrases() {
		return this.getAllCUIPhrases();
	}

	public Vector<CUIStructureShort> getAllCUIPhrases() {
		Vector<CUIStructureShort> cuis = this.CUIPhrases;
		if (cuis == null && this.getBase() != null
				&& this.getBase().CUIPhrases != null) {
			cuis = this.getBase().CUIPhrases;
		}
		return cuis;
	}

	public void setBase(Word base) {
		this.base = base;
	}

	public Vector<String> getFormTypes() {
		return formTypes;
	}

	public void setFormTypes(Vector<String> formTypes) {
		this.formTypes = formTypes;
	}

	public Vector<String> getFormValues() {
		return formValues;
	}

	public void setFormValues(Vector formValues) {
		this.formValues = formValues;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public void setVariants(Vector variants) {
		this.variants = variants;
	}

	public Hashtable<String, Vector<CUIStructureShort>> getWordCUIHash() {
		return wordCUIHash;
	}

	public Vector<CUIStructureShort> getConnectedCUIStructure(String str) {
		return this.getBase().wordCUIHash.get(str);
	}

	public Vector<CUIStructureShort> getCuiSingletons() {
		return cuiSingletons;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public static void setVisited(Vector<Word> words, boolean visited) {
		if (words != null) {
			for (Word word : words) {
				word.setVisited(visited);
			}
		}
	}

	public static class WordSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Word w1 = (Word) o1;
			Word w2 = (Word) o2;
			return w1.string.compareTo(w2.string);
		}
	}

	public static class InverseCUIPhraseNumSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Word w1 = (Word) o1;
			Word w2 = (Word) o2;
			int n1 = (w1.CUIPhrases != null ? w1.CUIPhrases.size() : 0);
			int n2 = (w2.CUIPhrases != null ? w2.CUIPhrases.size() : 0);
			if (n1 < n2) {
				return -1;
			}
			if (n1 > n2) {
				return 1;
			}
			return 0;
		}
	}

	public static class CUIPhraseNumSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Word w1 = (Word) o1;
			Word w2 = (Word) o2;
			int n1 = (w1.CUIPhrases != null ? w1.CUIPhrases.size() : 0);
			int n2 = (w2.CUIPhrases != null ? w2.CUIPhrases.size() : 0);
			if (n1 > n2) {
				return -1;
			}
			if (n1 < n2) {
				return 1;
			}
			return 0;
		}
	}

	public String getBaseString() {
		return baseString;
	}

	public void setBaseString(String baseString) {
		this.baseString = baseString;
	}

	// 10/3/2013
	public boolean isSemanticallyRelevant() {
		if (this.getCUIPhrases() != null) {
			CUIStructureShort cp = this.getCUIPhrases().firstElement();
			if (cp.isRelevant()) {
				return true;
			}
		}
		return false;
	}

}
