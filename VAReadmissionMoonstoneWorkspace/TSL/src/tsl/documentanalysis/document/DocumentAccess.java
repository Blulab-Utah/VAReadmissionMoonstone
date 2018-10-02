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
package tsl.documentanalysis.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.MorphRule;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.lexicon.WordEntry;
import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.ObjectInfoWrapper;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class DocumentAccess {
	String directoryName = null;
	Vector<Document> allDocuments = null;
	Document currentDocument = null;
	private static Document emptyDocument = new Document();
	Hashtable<Integer, Document> documentIDHash = new Hashtable();
	Hashtable<String, Vector> wordDocumentCountHash = new Hashtable();
	Hashtable<String, Float> wordIDFHash = new Hashtable();
	public Hashtable<String, WordEntry> wordEntryHash = new Hashtable();

	public DocumentAccess() {
	}

	public DocumentAccess(String directoryName,
			boolean useSectionHeaderHeuristics) {
		this.directoryName = directoryName;
		this.loadAllDocuments(useSectionHeaderHeuristics);
	}

	public void loadAllDocuments(boolean useSectionHeaderHeuristics) {
		if (this.directoryName != null) {
			Vector<File> dfiles = FUtils
					.readFilesFromDirectory(this.directoryName);
			if (dfiles != null) {
				for (File dfile : dfiles) {
					String text = FUtils.readFile(dfile);
					String path = dfile.getAbsolutePath();
					// Need urls
					Document document = new Document(-1, path, dfile.getName(),
							path, text);
					document.analyzeSentencesNoHeader();
					this.addDocument(document, -1);
					document.reset();
				}
			}
			calculateStatistics(useSectionHeaderHeuristics);
		}
	}

	public void addDocument(Document document, int id) {
		if (id >= 0) {
			documentIDHash.put(new Integer(id), document);
		}
		document.setDocumentAccess(this);
		this.allDocuments = VUtils.add(this.allDocuments, document);
	}

	public void calculateStatistics(boolean useSectionHeaderHeuristics) {
		documentIDHash = new Hashtable();
		wordDocumentCountHash = new Hashtable();
		wordIDFHash = new Hashtable();
		if (this.allDocuments != null) {
			for (Document document : this.allDocuments) {
				document.analyzeContent(useSectionHeaderHeuristics);
				if (document.getTokens() != null) {
					for (Token token : document.getTokens()) {
						if (token.isWordSurrogate()) {
							storeWordCount(document, token.getString());
						}
					}
				}
				document.reset();
			}
		}
		calculateWordIDF();
	}

	public void storeWordCount(Document document, String str) {
		HUtils.incrementHashObjectInfoWrapper(this.wordDocumentCountHash, str,
				document);
	}

	public void calculateWordIDF() {
		if (this.allDocuments != null) {
			float numDocuments = this.allDocuments.size();
			for (Enumeration<String> e = this.wordDocumentCountHash.keys(); e
					.hasMoreElements();) {
				String word = e.nextElement();
				Vector<ObjectInfoWrapper> wrappers = this.wordDocumentCountHash
						.get(word);
				float numWrappers = wrappers.size();
				double idf = Math.log(numDocuments / numWrappers);
				this.wordIDFHash.put(word, new Float(idf));
			}
		}
	}

	public float getWordIDF(String word) {
		Float idf = this.wordIDFHash.get(word);
		return (idf != null ? idf.floatValue() : 0f);
	}

	public float getWordTFIDF(String word, Document document) {
		float wcount = HUtils.getCount(this.wordDocumentCountHash,
				word.toLowerCase(), document);
		float idf = getWordIDF(word);
		return wcount * idf;
	}

	public boolean wordExistsInCorpus(String str) {
		return this.wordDocumentCountHash.get(str) != null;
	}

	public Vector<Document> getWordDocuments(String str) {
		return (Vector<Document>) ObjectInfoWrapper.getObjects(
				this.wordDocumentCountHash, str);
	}

	public boolean cooccurringDocuments(Vector<String> words, int index,
			Vector<ObjectInfoWrapper> lastWrappers) {
		if (words != null && index < words.size() - 1) {
			String word = words.elementAt(index);
			ObjectInfoWrapper.setVisited(lastWrappers, false);
			Vector<ObjectInfoWrapper> wrappers = this.wordDocumentCountHash
					.get(word);
			if (wrappers != null) {
				for (ObjectInfoWrapper wrapper : wrappers) {
					wrapper.setVisited(true);
				}
			}
			Vector<ObjectInfoWrapper> newWrappers = (wrappers != null ? wrappers
					: lastWrappers);
			cooccurringDocuments(words, index + 1, newWrappers);
			if (lastWrappers != null) {
				for (ObjectInfoWrapper wrapper : lastWrappers) {
					if (wrapper.isVisited()) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}

	public void putdocumentIDHash(Document doc, int id) {
		documentIDHash.put(new Integer(id), doc);
	}

	public Document findByID(int id) {
		Document doc = (Document) documentIDHash.get(new Integer(id));
		return doc;
	}

	public Document getCurrentDocument() {
		return currentDocument;
	}

	public void setCurrentDocument(Document currentDocument) {
		this.currentDocument = currentDocument;
	}

	public static Document getEmptyDocument() {
		return emptyDocument;
	}

	public Vector<Document> getAllDocuments() {
		return this.allDocuments;
	}

	public boolean hasDocuments() {
		return this.allDocuments != null;
	}

	public WordEntry createWordEntry(String word) {
		WordEntry wentry = (WordEntry) this.wordEntryHash.get(word);
		if (wentry == null && Lexicon.currentLexicon.getWord(word) == null) {
			wentry = new WordEntry(this, word);
		}
		return wentry;
	}

	void addGeneratedWordEntryVariants() {
		Hashtable hash = new Hashtable();
		for (Enumeration e = wordEntryHash.elements(); e.hasMoreElements();) {
			WordEntry wentry = (WordEntry) e.nextElement();
			if (wentry.isRegular()) {
				Vector<String[]> toProcess = new Vector(0);
				for (String pos : wentry.getPartsOfSpeech()) {
					Vector<String[]> variants = MorphRule
							.generateLexicalVariants(wentry.getWord(), pos);
					if (variants != null) {
						for (String[] variant : variants) {
							if (wordEntryHash.get(variant[0]) == null
									&& hash.get(variant) == null) {
								hash.put(variant, variant);
								toProcess.add(variant);
							}
						}
					}
				}
				for (String[] variant : toProcess) {
					String newword = variant[0];
					WordEntry newentry = this.createWordEntry(newword);
					if (newentry != null) {
						newentry.setBaseWordEntry(wentry);
						wentry.addVariantWordEntry(newentry);
						String newpos = variant[1];
						String formType = (variant[2] != null ? variant[2]
								: "*");
						String formValue = (variant[3] != null ? variant[3]
								: "*");
						newentry.setBase(wentry.getWord());
						if (newentry.getPartsOfSpeech() == null
								|| !newentry.getPartsOfSpeech()
										.contains(newpos)) {
							newentry.addPartOfSpeech(newpos);
							newentry.addFormType(formType);
							newentry.addFormValue(formValue);
							newentry.store();
						}
					}
				}
			}
		}
	}

	public void updateLexiconFromTrainingCorpus(File slfile) {
		MorphRule.initialize();
		readSpecialistLexiconFile(slfile);
		addGeneratedWordEntryVariants();
		addWordEntriesToLexicon();
	}

	void readSpecialistLexiconFile(File file) {
		try {
			if (file != null && file.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = null;
				WordEntry wentry = null;
				while ((line = in.readLine()) != null) {
					String str = null;
					if (line.charAt(0) == '{') {
						str = line.substring(1).trim();
						Vector v = StrUtils.stringList(str, '=');
						String word = (String) v.elementAt(1);
						if (Character.isUpperCase(word.charAt(0))) {
							continue;
						}
						wentry = this.createWordEntry(word);
					} else if (line.charAt(0) == '}') {
						if (wentry != null) {
							wentry.store();
						}
						str = null;
						wentry = null;
					} else if (line.length() > 3) {
						str = line.trim();
					}
					if (wentry != null) {
						wentry.assignValueFromSpecialistLexicon(str);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addWordEntriesToLexicon() {
		Vector<Word> newWords = null;
		for (Enumeration<String> e = wordEntryHash.keys(); e.hasMoreElements();) {
			String str = e.nextElement();
			if (Lexicon.currentLexicon.getWord(str) == null) {
				WordEntry wentry = wordEntryHash.get(str);
				Word word = new Word(Lexicon.currentLexicon, wentry);
				newWords = VUtils.add(newWords, word);
				System.out.print(".");
			}
		}
		Lexicon.currentLexicon.resolveBaseWords();
		Lexicon.currentLexicon.resolveWordVariants();
	}

	public void printAllWordEntries() {
		for (Enumeration e = this.wordEntryHash.elements(); e.hasMoreElements();) {
			WordEntry wentry = (WordEntry) e.nextElement();
			System.out.println(wentry);
		}
	}

	public int getHighestDocumentID() {
		int highest = 0;
		for (Enumeration e = this.documentIDHash.keys(); e.hasMoreElements();) {
			Integer id = (Integer) e.nextElement();
			if (id.intValue() > highest) {
				highest = id.intValue();
			}
		}
		return highest;
	}

}
