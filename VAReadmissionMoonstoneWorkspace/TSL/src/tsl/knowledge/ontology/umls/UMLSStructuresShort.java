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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.dbaccess.mysql.MySQL;
import tsl.documentanalysis.document.DocumentAccess;
import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.HUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import com.mysql.jdbc.PreparedStatement;

public class UMLSStructuresShort {

	Vector<CUIStructureShort> allCUIStructures = null;
	// Hashtable<String, CUIStructureShort> cuiStructureHash = new Hashtable();
	Hashtable<String, Vector<CUIStructureShort>> cuiStructureHash = new Hashtable();
	Hashtable<String, UMLSTypeInfo> typeInfoUIHash = new Hashtable();
	static UMLSStructuresShort currentUMLSStructs = null;

	public static UMLSStructuresShort getUMLSStructures() {
		if (currentUMLSStructs == null) {
			currentUMLSStructs = new UMLSStructuresShort();
		}
		return currentUMLSStructs;
	}

	public void postProcessCUIStructures() {
		if (this.allCUIStructures != null) {
			for (CUIStructureShort cp : this.allCUIStructures) {
				Vector<Word> words = VUtils.arrayToVector(cp.getWords());
				Collections.sort(words, new Word.InverseCUIPhraseNumSorter());
				cp.CPSortedWords = new Word[cp.getWordCount()];
				for (int i = 0; i < cp.getWordCount(); i++) {
					cp.CPSortedWords[i] = words.elementAt(i);
				}
			}
		}
	}

	public CUIStructureShort getCUIStructure(String cui) {
		CUIStructureShort cs = null;
		if (cui != null) {
			Vector<CUIStructureShort> cps = this.cuiStructureHash.get(cui
					.toLowerCase());
			if (cps != null) {
				cs = cps.firstElement();
			}
			// cs = this.cuiStructureHash.get(cui.toLowerCase());
		}
		return cs;
	}

	public Vector<CUIStructureShort> getAllCUIStructures() {
		if (this.allCUIStructures != null) {
			Collections.sort(this.allCUIStructures,
					new CUIStructureShort.WordStringSorter());
		}
		return this.allCUIStructures;
	}

	public CUIStructureShort getCoveringCUIStructure(Vector<String> words) {
		Vector<Token> tokens = Token.wrapWordStrings(words);
		int start = 0;
		int end = tokens.lastElement().getEnd();
		if (tokens != null) {
			Vector<CUIStructureWrapperShort> cws = getCUIStructureWrappers(
					tokens, null, true);
			if (cws != null) {
				for (CUIStructureWrapperShort cw : cws) {
					if (cw.getTextStart() == start && cw.getTextEnd() == end) {
						return cw.getCuiStructure();
					}
				}
			}
		}
		return null;
	}

	// 12/10/2012: Get CPs that cover all of the input words (NOT TESTED)...
	public Vector<CUIStructureShort> getCoveringCUIStructures(Vector<Word> bases) {
		Vector<CUIStructureShort> resultCPs = null;
		if (bases == null) {
			return null;
		}
		int inputWordCount = bases.size();
		Collections.sort(bases, new Word.InverseCUIPhraseNumSorter());
		if (inputWordCount == 1) {
			Word base = bases.firstElement();
			resultCPs = base.getCuiSingletons();
		} else {
			Word bfirst = bases.firstElement();
			Word bsecond = bases.elementAt(1);
			Vector<CUIStructureShort> cps = bfirst
					.getConnectedCUIStructure(bsecond.getString());
			if (cps != null) {
				for (CUIStructureShort cp : cps) {
					if (cp.getWordCount() != inputWordCount) {
						continue;
					}
					if (cp.getWordCount() == 2) {
						resultCPs = VUtils.add(resultCPs, cp);
					} else {
						boolean found = true;
						for (int i = 0; found && i < cp.getWordCount(); i++) {
							Word cpbase = cp.getWords()[i];
							if (!bases.contains(cpbase)) {
								found = false;
							}
						}
						if (found) {
							resultCPs = VUtils.add(resultCPs, cp);
						}
					}
				}
			}

		}
		return resultCPs;
	}

	// 12/14/2012: Get one covering CP.
	public CUIStructureShort getCoveringCuiStructure(Vector<Word> bases) {
		if (bases == null) {
			return null;
		}
		int inputWordCount = bases.size();
		Collections.sort(bases, new Word.InverseCUIPhraseNumSorter());
		if (inputWordCount == 1) {
			Word base = bases.firstElement();
			if (base.getCuiSingletons() != null) {
				return base.getCuiSingletons().firstElement();
			}
		}
		Word bfirst = bases.firstElement();
		Word bsecond = bases.elementAt(1);
		Vector<CUIStructureShort> cps = bfirst.getConnectedCUIStructure(bsecond
				.getString());
		if (cps != null) {
			for (CUIStructureShort cp : cps) {
				if (cp.getWordCount() >= inputWordCount) {
					if (inputWordCount == 2 || cp.covers(bases)) {
						return cp;
					}
				}
			}
		}
		return null;
	}

	// 12/10/2012
	public Vector<CUIStructureShort> getContainedCUIStructuresEXHAUSTIVE(
			Vector<Word> bases) {
		Vector<CUIStructureShort> resultCPs = null;
		Hashtable<CUIStructureShort, CUIStructureShort> CPHash = new Hashtable();
		if (bases == null) {
			return null;
		}
		int numbases = bases.size();
		Hashtable<Word, Word> wordhash = new Hashtable();
		for (Word base : bases) {
			wordhash.put(base, base);
		}
		for (Word base : bases) {
			Vector<CUIStructureShort> cps = base.getCUIPhrases();
			if (cps != null) {
				for (CUIStructureShort cp : cps) {
					if (cp.getWordCount() == 1) {
						CPHash.put(cp, cp);
					} else if (cp.getWordCount() <= numbases) {
						if (CPHash.get(cp) == null) {
							boolean found = true;
							for (int i = 0; found && i < cp.getWordCount(); i++) {
								Word cpbase = cp.getWords()[i];
								if (wordhash.get(cpbase) == null) {
									found = false;
								}
							}
							if (found) {
								CPHash.put(cp, cp);
							}
						}
					}
				}
			}
		}
		if (!CPHash.isEmpty()) {
			for (Enumeration<CUIStructureShort> e = CPHash.keys(); e
					.hasMoreElements();) {
				resultCPs = VUtils.add(resultCPs, e.nextElement());
			}
		}
		return resultCPs;
	}

	public Vector<CUIStructureShort> getContainedCUIStructures(
			Vector<Word> bases) {
		Vector<CUIStructureShort> resultCPs = null;
		Hashtable<CUIStructureShort, CUIStructureShort> storedCPHash = new Hashtable();
		if (bases == null) {
			return null;
		}
		Collections.sort(bases, new Word.InverseCUIPhraseNumSorter());
		int numbases = bases.size();
		Hashtable<Word, Word> wordhash = new Hashtable();
		for (Word base : bases) {
			wordhash.put(base, base);
		}
		// 5/17/2013
  		for (Word base : bases) {
   			storeCPHash(base.getCuiSingletons(), wordhash, storedCPHash,
 					numbases);
		}
		for (int i = 0; i < numbases - 1; i++) {
 			Word first = bases.elementAt(i);
			for (int j = i + 1; j < numbases; j++) {
				Word second = bases.elementAt(j);
				Vector<CUIStructureShort> cps = first
						.getConnectedCUIStructure(second.getString());
				storeCPHash(cps, wordhash, storedCPHash, numbases);
			}
		}
		if (!storedCPHash.isEmpty()) {
			for (Enumeration<CUIStructureShort> e = storedCPHash.keys(); e
					.hasMoreElements();) {
				resultCPs = VUtils.add(resultCPs, e.nextElement());
			}
		}
		return resultCPs;
	}

	public Vector<CUIStructureWrapperShort> getCUIStructureWrappers(
			Vector<Token> tokens, Vector<String> ontologies,
			boolean removeDuplicates) {
		Hashtable<Word, Vector<CUIStructureShort>> wordCPHash = new Hashtable();
		Hashtable<CUIStructureShort, Integer> counthash = new Hashtable();
		Hashtable<Word, Word> wordhash = new Hashtable();
		Vector<Word> basewords = Token.gatherWords(tokens, true);
		if (basewords == null) {
			return null;
		}
		Vector<CUIStructureShort> cps = getContainedCUIStructures(basewords);
		if (cps == null) {
			return null;
		}
		Vector<CUIStructureWrapperShort> cpws = null;
		for (CUIStructureShort cp : cps) {
			for (int i = 0; i < cp.getWordCount(); i++) {
				Word word = cp.getWords()[i];
				VUtils.pushHashVector(wordCPHash, word, cp);
			}
		}
		if (tokens != null) {
			Hashtable<Word, Token> wordTokenHash = new Hashtable();
			for (Token token : tokens) {
				if (token.isWord() && token.getWord() != null
						&& token.getWord().getBase() != null) {
					Word base = token.getWord().getBase();
					wordTokenHash.put(base, token);
					boolean wordseen = wordhash.get(base) != null;
					Vector<CUIStructureShort> wcps = wordCPHash.get(base);
					if (wcps != null) {
						for (CUIStructureShort wcp : wcps) {
							int count = 0;
							if (!wordseen) {
								count = HUtils.incrementCount(counthash, wcp);
							} else {
								count = HUtils.getCount(counthash, wcp);
							}
							if (count >= wcp.getWordCount()) {
								Vector<Token> cptokens = new Vector(6);
								boolean foundEmpty = false;
								for (Word cpword : wcp.getWords()) {
									Token cptoken = wordTokenHash.get(cpword);
									if (cptoken != null) {
										cptokens.add(cptoken);
									} else {
										foundEmpty = true;
									}
								}
								if (!foundEmpty) {
									Collections.sort(cptokens,
											new Token.TextPositionSorter());
									int firstIndex = tokens.indexOf(cptokens
											.firstElement());
									int lastIndex = tokens.indexOf(cptokens
											.lastElement());
									if (isValidTokenSequence(wcp, cptokens,
											tokens, firstIndex, lastIndex)) {
										CUIStructureWrapperShort cpw = new CUIStructureWrapperShort(
												wcp, cptokens, firstIndex,
												lastIndex);
										cpws = VUtils.add(cpws, cpw);
									}
								}
							}
						}
					}
					if (!wordseen) {
						wordhash.put(base, base);
					}
				}
			}
		}
		if (removeDuplicates) {
			cpws = removeDuplicateCUIStructureWrapperShorts(cpws);
		}
		if (cpws != null) {
			Collections.sort(cpws,
					new CUIStructureWrapperShort.TextPositionSorter());
		}
		return cpws;
	}

	public Vector<CUIStructureShort> getCUIStructures(Vector<Token> tokens,
			Vector<String> ontologies) {
		Vector<Word> bases = Token.gatherWords(tokens, true);
		return getContainedCUIStructures(bases);
	}
	
	Vector<CUIStructureWrapperShort> removeDuplicateCUIStructureWrapperShorts(
			Vector<CUIStructureWrapperShort> cpws) {
		Vector<CUIStructureWrapperShort> toRemove = new Vector(0);
		if (cpws != null) {
			Collections.sort(cpws,
					new CUIStructureWrapperShort.TextPositionSorter());
			for (CUIStructureWrapperShort wrapper : cpws) {
				if (!wrapper.isVisited()) {
					for (CUIStructureWrapperShort other : cpws) {
						if (!other.isVisited()
								&& wrapper.getCuiStructure().getWords().length > other
										.getCuiStructure().getWords().length) {
							boolean contains = SeqUtils.contains(
									wrapper.textStart, wrapper.textEnd,
									other.textStart, other.textEnd);
							boolean isStrictSubset = (contains && Word
									.isStrictSubset(other.getCuiStructure()
											.getWords(), wrapper
											.getCuiStructure().getWords()));
							if (isStrictSubset) {
								toRemove = VUtils.add(toRemove, other);
								other.setVisited(true);
							}
						}
					}
				}
			}
			if (!toRemove.isEmpty()) {
				cpws.removeAll(toRemove);
				CUIStructureWrapperShort.resetVisited(toRemove);
			}
		}
		return cpws;
	}

	// Before 12/4/2012:
	// For every token, get all CUIs containing that word. Store a count on each
	// CUI, incremented with the first token instance of each word contained in
	// that CUI. For each CUI with all words matched, get the latest token
	// CUIStructWrapper. This guarantees that we will find all instances of each
	// CUI with words contained in the specified window (6 tokens).

	// 11/29/2012 PROBLEM: If a CP appears more than once on a word's CPPhrase
	// list, it
	// will get counted >1 times for that word, and so will get a count > the
	// number of
	// words that intersect with the input tokens. NOT YET SOLVED!

	public Vector<CUIStructureWrapperShort> getCUIStructureWrappersCOUNT(
			Vector<Token> tokens, Vector<String> ontologies,
			boolean removeDuplicates) {
		Vector<CUIStructureWrapperShort> cpws = null;
		if (tokens != null) {
			int numTokens = tokens.size();
			Hashtable<String, Token> tokenhash = new Hashtable();
			Hashtable<CUIStructureShort, Integer> counthash = new Hashtable();
			Hashtable<String, String> wordhash = new Hashtable();
			for (Token token : tokens) {
				if (token.isWord() && token.getWord() != null) {
					Word base = token.getWord().getBase();
					String bstring = base.getString().toLowerCase();
					boolean wordseen = wordhash.get(bstring) != null;
					Vector<CUIStructureShort> cps = token.getWord()
							.getAllCUIPhrases();
					if (cps != null) {
						tokenhash.put(bstring, token);
						for (CUIStructureShort cp : cps) {
							Word[] cpwords = cp.getWords();
							if (cpwords.length <= numTokens) {
								if (!wordseen) {
									HUtils.incrementCount(counthash, cp);
								}
								int count = HUtils.getCount(counthash, cp);
								if (count >= cp.getWords().length) {
									Vector<Token> cptokens = new Vector(6);
									for (Word cpword : cpwords) {
										String cpwordstr = cpword.getBase()
												.getString().toLowerCase();
										Token cptoken = tokenhash
												.get(cpwordstr);
										cptokens.add(cptoken);
									}
									Collections.sort(cptokens,
											new Token.TextPositionSorter());
									int firstIndex = tokens.indexOf(cptokens
											.firstElement());
									int lastIndex = tokens.indexOf(cptokens
											.lastElement());
									if (isValidTokenSequence(cp, cptokens,
											tokens, firstIndex, lastIndex)
											&& (ontologies == null || ontologies
													.contains(cp.getOntology()))) {
										CUIStructureWrapperShort cpw = new CUIStructureWrapperShort(
												cp, cptokens, firstIndex,
												lastIndex);
										cpws = VUtils.add(cpws, cpw);
									}
								}
							}
						}
					}
					if (!wordseen) {
						wordhash.put(bstring, bstring);
					}
				}
			}
		}
		if (removeDuplicates) {
			cpws = removeDuplicateCUIStructureWrapperShorts(cpws);
		}
		if (cpws != null) {
			Collections.sort(cpws,
					new CUIStructureWrapperShort.TextPositionSorter());
			for (CUIStructureWrapperShort cpw : cpws) {
				if (cpw.getCuiStructure().getWordCount() > 3) {
					System.out.println("WRAPPER=" + cpw);
				}
			}
		}
		return cpws;
	}
	
	// 6/19/2015
	boolean isValidTokenSequence(CUIStructureShort cp, Vector<Token> tokens,
			Vector<Token> allTokens, int firstIndex, int lastIndex) {
		if (tokens != null && cp.getWords().length == tokens.size()
				&& tokens.firstElement().isWord()
				&& Math.abs(lastIndex - firstIndex) <= 6) {
			for (int i = firstIndex; i < lastIndex; i++) {
				Token token = allTokens.elementAt(i);
				if (token == null) {
					int x = 1;
				}
				if (token.isPunctuation()) {
					return false;
				}
				if (token.isWord() && token.getWord() != null && token.getWord().isConjunct()
						&& !cp.isContainsConjunct()) {
					return false;
				}
			}
			int conjunctIndex = -1;
			for (int i = firstIndex; i <= lastIndex; i++) {
				Token token = allTokens.elementAt(i);
				if (token.getWord() != null && token.getWord().isConjunct()) {
					conjunctIndex = i;
				}
				if (token.getWord() != null
						&& token.getWord().getCUIPhrases() != null
						&& !tokens.contains(token)
						&& !(conjunctIndex > 0 && (i - conjunctIndex) < 3)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// Before 6/19/2015
	// 10/18/2011 -- If the invalidating token is to the right
	// of a conjunct, it could be valid, e.g. "left and right lower
	// lobe" should permit "left lower lobe" even though it
	// doesn't contain "right"
//	boolean isValidTokenSequence(CUIStructureShort cp, Vector<Token> tokens,
//			Vector<Token> allTokens, int firstIndex, int lastIndex) {
//		if (tokens != null && cp.getWords().length == tokens.size()
//				&& tokens.firstElement().isWord()
//				&& Math.abs(lastIndex - firstIndex) <= 6) {
//			int conjunctIndex = -1;
//			for (int i = firstIndex; i <= lastIndex; i++) {
//				Token token = allTokens.elementAt(i);
//				if (token.getWord() != null && token.getWord().isConjunct()) {
//					conjunctIndex = i;
//				}
//				if (token.getWord() != null
//						&& token.getWord().getCUIPhrases() != null
//						&& !tokens.contains(token)
//						&& !(conjunctIndex > 0 && (i - conjunctIndex) < 3)) {
//					return false;
//				}
//			}
//			return true;
//		}
//		return false;
//	}
	
	// 7/28/2012: Restrictions -- No CUIs with words containing non-letter
	// chars, no
	// one-letter words, at least one word (or variant thereof) must exist in
	// lexicon
	// or in document corpus.
	public void extractCUIsFromUMLS(DocumentAccess documentAccess) {
		int newCPCount = 0;
		com.mysql.jdbc.Connection connection = MySQL.getMySQL()
				.getUMLSConnection();
		try {
			int offset = 0;
			UMLSOntology ontology = (UMLSOntology) KnowledgeEngine
					.getCurrentKnowledgeEngine().findOntology("umls");
			boolean done = false;
			while (!done) {
				System.out.print(".");
				String sql = "select nstr, cui from mrxns_eng limit 10000 offset "
						+ offset;
				sql = sql.toLowerCase();
				PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) connection
						.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				if (!rs.first()) {
					done = true;
					break;
				}
				do {
					String nstr = rs.getString(1).toLowerCase();
					String cui = rs.getString(2).toLowerCase();
					Vector<String> v = StrUtils.stringList(nstr, ' ');
					boolean cuiIsRequired = RequiredCUIs.isRequiredCUI(cui);
					boolean tuiIsRelevant = false;
					boolean wordContainsNonLetterCharacter = false;
					boolean containsOneLetterWord = false;
					boolean foundWord = false;
					Vector<String> newWords = null;
					for (String wstr : v) {
						if (StrUtils.stringContainsNonLetterCharacter(wstr)) {
							wordContainsNonLetterCharacter = true;
						}
						if (wstr.length() == 1) {
							containsOneLetterWord = true;
						}
						if (Lexicon.currentLexicon.getWord(wstr) != null) {
							foundWord = true;
						} else if (documentAccess.wordExistsInCorpus(wstr)) {
							foundWord = true;
							newWords = VUtils.add(newWords, wstr);
						}
					}
					if (foundWord && !wordContainsNonLetterCharacter
							&& !containsOneLetterWord) {
						String tui = CUIStructureShort.getTUI(cui, connection);
						tuiIsRelevant = (tui != null && UMLSTypeConstant
								.isRelevant(tui));
						if (cuiIsRequired || tuiIsRelevant) {
							UMLSTypeInfo tinfo = UMLSTypeInfo.findByName(tui);
							Vector words = Lexicon.currentLexicon.getWords(v);
							CUIStructureShort cp = CUIStructureShort.create(
									this, words, cui, tinfo, ontology);
							newCPCount++;
							if (newWords != null) {
								for (String wstr : newWords) {
									Lexicon.currentLexicon.getWord(wstr, true);
								}
							}
							System.out.println("\t" + cp);
						}
					}
				} while (rs.next());
				rs.close();
				ps.close();
				offset += 10000;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("\tNew CUI count=" + newCPCount);
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Hashtable<String, UMLSTypeInfo> getTypeInfoUIHash() {
		return typeInfoUIHash;
	}

	public UMLSTypeInfo findTypeInfoViaCUI(String cui) {
		if (cui != null) {
			return typeInfoUIHash.get(cui);
		}
		return null;
	}

	// 8/18/2013
	public void clearAll() {
		for (Word word : Lexicon.currentLexicon.getAllWords()) {
			if (word.getCUIPhrases() != null) {
				word.CUIPhraseHash.clear();
				word.CUIPhrases = null;
				word.cuiSingletons = null;
				word.wordCUIHash.clear();
			}
		}
		this.allCUIStructures = null;
		this.cuiStructureHash.clear();
	}
	
	// FOR VINCI!!
	
	private void storeCPHash(Vector<CUIStructureShort> cps,
			Hashtable<Word, Word> wordhash,
			Hashtable<CUIStructureShort, CUIStructureShort> storedCPHash,
			int numbases) {
		if (cps != null) {
			for (CUIStructureShort cp : cps) {
				if (cp.getWordCount() <= numbases) {
					if (storedCPHash.get(cp) == null) {
						boolean found = true;
						for (int k = 0; found && k < cp.getWordCount(); k++) {
							
							// 4/26/2016: TEST: Should I not be using the basest base?
							Word cpbase = cp.getWords()[k].getBase();
							
//							Word cpbase = cp.getWords()[k];
							
							if (wordhash.get(cpbase) == null) {
								found = false;
							}
						}
						if (found) {
							storedCPHash.put(cp, cp);
						}
					}
				}
			}
		}
	}
	

}
