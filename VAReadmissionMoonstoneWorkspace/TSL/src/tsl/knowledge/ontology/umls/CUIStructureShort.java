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
package tsl.knowledge.ontology.umls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import tsl.documentanalysis.lexicon.Word;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.utilities.VUtils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class CUIStructureShort {
	Word[] words = null;
	Word[] CPSortedWords = null;
	int checksum = 0;
	private int hashcode = 0;
	int numWords = 0;
	String cui = null;
	UMLSTypeInfo typeInfo = null;
	UMLSStructuresShort umlsStructures = null;
	TypeConstant type = null;
	Ontology ontology = null;
	boolean visited = false;
	boolean isAutomaticallyGenerated = false;
	boolean containsNoun = false;
	boolean containsAdjective = false;
	boolean containsConjunct = false;

	// 8/30/2015
	private Constant conceptConstant = null;

	public CUIStructureShort(UMLSStructuresShort umlss, Vector<Word> words,
			String cui, UMLSTypeInfo tinfo, Ontology ontology) {

		// 8/31/2015:
		StringConstant sc = StringConstant.createStringConstant(cui,
				tinfo.getType(), false);
		sc.setCui(cui);
		
		this.umlsStructures = umlss;
		this.ontology = ontology;
		this.cui = cui;
		this.typeInfo = tinfo;
		if (tinfo != null) {
			this.setType(tinfo.getType());
			this.umlsStructures.getTypeInfoUIHash().put(cui, tinfo);
		}
		this.words = new Word[words.size()];
		this.numWords = words.size();
		Collections.sort(words, new Word.WordSorter());
		String wordstring = getWordStringIndex(words);
		for (int i = 0; i < words.size(); i++) {
			Word word = words.elementAt(i);
			this.words[i] = word;
			Word base = word.getBase();
			if (word.isNoun()) {
				this.containsNoun = true;
			} else if (word.isAdjective()) {
				this.containsAdjective = true;
			} else if (word.isConjunct()) {
				this.containsConjunct = true;
			}

			// 12/8/2012
			String bstring = base.getString();
			this.checksum |= bstring.hashCode();

			if (base.CUIPhraseHash.get(wordstring) == null) {
				base.CUIPhraseHash.put(wordstring, this);
				base.CUIPhrases = VUtils.add(base.CUIPhrases, this);
			}
		}
		this.umlsStructures.allCUIStructures = VUtils.add(
				this.umlsStructures.allCUIStructures, this);

		// 10/1/2013
		// this.umlsStructures.cuiStructureHash.put(this.cui.toLowerCase(),
		// this);
		VUtils.pushHashVector(this.umlsStructures.cuiStructureHash,
				this.cui.toLowerCase(), this);

	}

	// 12/3/2012: If any of the base words in the proposed CP have an index
	// matching
	// the full base string, the CP has already been created.
	public static CUIStructureShort create(UMLSStructuresShort umlss,
			Vector<Word> words, String cui, UMLSTypeInfo tinfo,
			Ontology ontology) {
		CUIStructureShort newcp = null;
		try {
			Collections.sort(words, new Word.WordSorter());
			String wordstring = getWordStringIndex(words);
			for (int i = 0; i < words.size(); i++) {
				Word word = words.elementAt(i);
				Word base = word.getBase();
				if (base.CUIPhraseHash.get(wordstring) != null) {
					return null;
				}
			}
			newcp = new CUIStructureShort(umlss, words, cui, tinfo, ontology);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newcp;
	}

	public static String getWordStringIndex(Vector<Word> words) {
		StringBuffer sb = new StringBuffer();
		for (Word word : words) {
			sb.append(word.getBase().toString());
			sb.append(":");
		}
		return sb.toString();
	}

	// Before 12/3/2012
	// public static CUIStructureShort create(UMLSStructuresShort umlss, Onyx
	// onyx,
	// Vector<Word> words, String cui, UMLSTypeInfo tinfo, Ontology ontology) {
	// CUIStructureShort newcp = null;
	// Collections.sort(words, new Word.WordSorter());
	// String wordstring = "";
	// for (Word word : words) {
	// wordstring += word.getBase().toString() + ":";
	// }
	// for (int i = 0; i < words.size(); i++) {
	// Word word = words.elementAt(i);
	// for (Word variant : word.getWordVariants()) {
	// if (variant.CUIPhraseHash.get(wordstring) != null) {
	// return null;
	// }
	// }
	// }
	// newcp = new CUIStructureShort(umlss, onyx, words, cui, tinfo, ontology);
	// return newcp;
	// }

	public int hashCode() {
		if (this.hashcode == 0) {
			this.hashcode = this.cui.hashCode()
					| this.words[0].toString().hashCode();
		}
		return this.hashcode;
	}

	public int hashCodeFULL() {
		int hashcode = this.cui.hashCode();
		for (int i = 0; i < this.words.length; i++) {
			hashcode |= this.words[i].hashCode();
		}
		return hashcode;
	}

	public boolean equals(Object o) {
		if (o instanceof CUIStructureShort) {
			CUIStructureShort other = (CUIStructureShort) o;
			UMLSTypeInfo tio = this.getTypeInfo();
			UMLSTypeInfo otio = other.getTypeInfo();
			if (this.cui.equals(other.cui)
					&& ((tio == null && otio == null) || (tio != null
							&& otio != null && tio.getUI().equals(otio.getUI())))
					&& this.wordsAreEqual(other)) {
				return true;
			}
		}
		return false;
	}

	public boolean wordsAreEqual(CUIStructureShort cp) {
		if (this.words.length != cp.words.length) {
			return false;
		}
		for (int i = 0; i < this.words.length; i++) {
			String w1 = this.words[i].getBase().toString();
			String w2 = cp.words[i].getBase().toString();
			if (!w1.equals(w2)) {
				return false;
			}
		}
		return true;
	}

	public Word[] getWords() {
		return words;
	}

	public int getWordCount() {
		return this.numWords;
	}

	public String getWordString(boolean useComma) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.getWords().length; i++) {
			Word w = this.getWords()[i];
			sb.append(w.getString());
			if (i < this.getWords().length - 1) {
				if (useComma) {
					sb.append(",");
				} else {
					sb.append(" ");
				}
			}
		}
		return sb.toString();
	}

	public String getCui() {
		return cui;
	}

	public UMLSTypeInfo getTypeInfo() {
		return typeInfo;
	}

	public UMLSStructuresShort getUmlsStructures() {
		return umlsStructures;
	}

	public Ontology getOntology() {
		if (this.ontology == null) {
			this.ontology = this.getTypeInfo().getType().getOntology();
		}
		return this.ontology;
	}

	public String toString() {
		UMLSTypeInfo ti = this.getTypeInfo();
		String str = "<\"" + Word.toString(words) + "\",CUI=" + this.cui
				+ ",TUI=" + (ti != null ? ti.getUI() : "*") + ">";
		return str;
	}

	public boolean isCondition() {
		return UMLSTypeConstant.isCondition(this.getType());
	}

	public boolean isRelevant() {
		return UMLSTypeConstant.isRelevant(this.getType());
	}

	public String getCUI() {
		return this.cui;
	}

	public String getTUI() {
		return this.getTypeInfo().getUI();
	}

	public static class WordStringSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			CUIStructureShort cp1 = (CUIStructureShort) o1;
			CUIStructureShort cp2 = (CUIStructureShort) o2;
			return cp1.words[0].getString().compareTo(cp2.words[0].getString());
		}
	}

	public static class WordLengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			CUIStructureShort cp1 = (CUIStructureShort) o1;
			CUIStructureShort cp2 = (CUIStructureShort) o2;
			if (cp1.words.length > cp2.words.length) {
				return -1;
			}
			if (cp1.words.length < cp2.words.length) {
				return 1;
			}
			return 0;
		}
	}

	public static class InverseWordLengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			CUIStructureShort cp1 = (CUIStructureShort) o1;
			CUIStructureShort cp2 = (CUIStructureShort) o2;
			if (cp1.words.length < cp2.words.length) {
				return -1;
			}
			if (cp1.words.length > cp2.words.length) {
				return 1;
			}
			return 0;
		}
	}

	public static String getTUI(String CUI, Connection connection) {
		String TUI = null;
		try {
			String sql = "select TUI from mrsty where CUI = '" + CUI + "'";
			sql = sql.toLowerCase();
			PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) connection
					.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				String tstr = rs.getString(1);
				TUI = tstr.toLowerCase();
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return TUI;
	}

	public TypeConstant getType() {
		return type;
	}

	public void setType(TypeConstant type) {
		this.type = type;
	}

	public void setWords(Word[] words) {
		this.words = words;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public void setTypeInfo(UMLSTypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

	public void setUmlsStructures(UMLSStructuresShort umlsStructures) {
		this.umlsStructures = umlsStructures;
	}

	public int getChecksum() {
		return checksum;
	}

	public int getNumWords() {
		return numWords;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public static void setVisited(Vector<CUIStructureShort> cps, boolean visited) {
		if (cps != null) {
			for (CUIStructureShort cp : cps) {
				cp.setVisited(visited);
			}
		}
	}

	// 12/13/2012
	public boolean covers(Vector<Word> words) {
		if (words == null || this.getWordCount() < words.size()) {
			return false;
		}
		for (int i = 0; i < this.getWordCount(); i++) {
			if (!words.contains(this.getWords()[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean exactlyCovers(Vector<Word> words) {
		if (words == null || this.getWordCount() != words.size()) {
			return false;
		}
		Collections.sort(words, new Word.WordSorter());
		for (int i = 0; i < this.getWordCount(); i++) {
			if (!words.elementAt(i).equals(this.getWords()[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean isAutomaticallyGenerated() {
		return isAutomaticallyGenerated;
	}

	public void setAutomaticallyGenerated(boolean isAutomaticallyGenerated) {
		this.isAutomaticallyGenerated = isAutomaticallyGenerated;
	}

	public static boolean isCUI(String str) {
		if (str != null && str.length() > 3) {
			char c1 = Character.toLowerCase(str.charAt(0));
			char c2 = str.charAt(1);
			if (c1 == 'c' && Character.isDigit(c2)) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidPhrase() {
		return this.containsNoun || this.containsAdjective;
	}

	public boolean isContainsConjunct() {
		return containsConjunct;
	}

}
