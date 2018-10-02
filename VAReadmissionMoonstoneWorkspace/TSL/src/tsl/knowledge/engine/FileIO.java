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
package tsl.knowledge.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tsl.documentanalysis.lexicon.Lexicon;
import tsl.documentanalysis.lexicon.Word;
import tsl.expression.term.type.TypeConstant;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSOntology;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import tsl.knowledge.ontology.umls.UMLSTypeInfo;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class FileIO {

	private Hashtable<Class, Integer> highestIDHash = new Hashtable();
	private String kbdir = null;
	private static String cuiPhraseFile = "cuiphrases";
	private static String tuiInfoFile = "tuiInfo";
	private static String dictionaryFile = "dictionary";
	private static String ontologyFile = "ontology";
	private static String ontologyDirectory = "ontologies";
	public static String LoadUMLS = "LoadUMLS";

	public FileIO() {
		this.kbdir = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters().getKBDirectory();
	}

	int getHighestID(Class c) {
		int highestID = 0;
		Integer rv = this.highestIDHash.get(c);
		if (rv != null) {
			highestID = rv.intValue();
		}
		return highestID;
	}

	void setHighestID(Class c, int id) {
		Integer highestID = getHighestID(c);
		if (id >= 0 && highestID < id) {
			this.highestIDHash.put(c, id);
		}
	}

	public int incrementHighestID(Class c) {
		int highestID = getHighestID(c) + 1;
		this.highestIDHash.put(c, new Integer(highestID));
		return highestID;
	}

	public void loadAllKBFiles() throws Exception {
		loadDictionaryFile();
		if (KnowledgeEngine.getCurrentKnowledgeEngine().getStartupParameters()
				.isPropertyTrue(LoadUMLS)) {
			loadOntologyFiles();
			loadTUIFile();
			loadCUIFile();
		}
		Lexicon.currentLexicon.storeCUIStructures();
	}

	public void loadOntologyFiles() throws Exception {
		String sourcedirname = this.kbdir + File.separator + ontologyDirectory;
		File sourcedir = new File(sourcedirname);
		Vector<File> subdirs = FUtils.getSubdirectories(sourcedir);
		if (subdirs != null) {
			for (File subdir : subdirs) {
				String ofilename = subdir.getAbsolutePath() + File.separator
						+ ontologyFile;
				String ostr = FUtils.readFile(ofilename);
				// Need to make UMLS ontology a standard ontology.
				if ("umls".equals(subdir.getName())) {
					UMLSOntology.createFromLisp(ostr);
				} else {
					Ontology.createFromLisp(ostr);
				}
			}
		}
	}

	public void loadTUIFile() throws Exception {
		String fname = this.kbdir + File.separator + tuiInfoFile;
		Hashtable<String, String> shash = new Hashtable();
		if ((new File(fname)).exists()) {
			Object o = JLisp.getJLisp().loadFile(fname);
			if (o instanceof Sexp) {
				Sexp sexp = (Sexp) o;
				for (Enumeration e = sexp.elements(); e.hasMoreElements();) {
					String[] pnames = null;
					Sexp subsexp = (Sexp) e.nextElement();
					Vector<String> v = JLUtils.convertSexpToJVector(subsexp);
					String tui = v.elementAt(0);
					String name = v.elementAt(1);
					
					if (v.size() > 2) {
						pnames = v.elementAt(2).split(",");
					}
					new UMLSTypeInfo("STY", tui, name, "NODESC", pnames);
					shash.put(tui, name);
				}
				Vector<String> tuis = HUtils.getKeys(shash);
				Collections.sort(tuis);
				for (String tui : tuis) {
					String name = shash.get(tui);
					String tstr = "(\"" + tui + "\" \"" + name + "\")";
				}
			}
		}
	}

	public boolean loadCUIFile() throws Exception {
		String fname = this.kbdir + File.separator + cuiPhraseFile + ".txt";
		boolean loaded = false;
		File f = new File(fname);
		if (f.exists()) {
			System.out.print("Loading CUIs...");
			int count = 0;
			long startTime = System.currentTimeMillis();
			UMLSStructuresShort umlss = UMLSStructuresShort.getUMLSStructures();
			UMLSOntology umlsOntology = KnowledgeEngine
					.getCurrentKnowledgeEngine().getUMLSOntology();
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (line.length() > 3 && line.charAt(0) != '#') {
					String[] parts = line.split(":");
					if (parts == null || parts.length >= 3) {
						Vector<String> wstrs = StrUtils.stringList(parts[0],
								',');
						String cui = parts[1];
						String tui = parts[2];
						String ostr = parts[3];
						
						if (!UMLSTypeConstant.isRelevant(tui)) {
							continue;
						}
						Ontology ontology = KnowledgeEngine
								.getCurrentKnowledgeEngine().findOntology(ostr);
						UMLSTypeInfo tinfo = UMLSTypeInfo.findByName(tui);

						// 7/2/2013
						if (tinfo == null & "onyx".equals(ostr)) {
							TypeConstant tc = UMLSTypeConstant
									.createTypeConstant(tui);
							tinfo = UMLSTypeInfo.create(tc);
						}

						if (tinfo != null) {
							Vector<Word> words = null;
							boolean invalid = false;
							boolean foundNonWord = false;
							boolean sequenceTooLong = false;
							for (int i = 0; !invalid && i < wstrs.size(); i++) {
								if (i > 10) {
									sequenceTooLong = true;
									break;
								}
								String wstr = wstrs.elementAt(i);
								Word word = Lexicon.currentLexicon
										.getWord(wstr);
								if (word == null) {
									foundNonWord = true;
									break;
								}
								if ("uncomfortable".equals(wstr)) {
									int x = 1;
								}
								words = VUtils.add(words, word);
							}
							boolean doStore = (!sequenceTooLong && !foundNonWord);
							if (doStore) {
								CUIStructureShort.create(umlss, words, cui,
										tinfo, ontology);
								loaded = true;
							}
						}
					}
				}
			}
			// umlss.postProcessCUIStructures();
			if (umlss.getAllCUIStructures() != null) {
				count = umlss.getAllCUIStructures().size();
			}
			long endTime = System.currentTimeMillis();
			float diff = endTime - startTime;
			float time = diff / 1000;
			System.out.println("NumberOfCUIs=" + count + ",Seconds=" + time);
		}
		return loaded;
	}

	public boolean loadDictionaryFile() throws Exception {
		String fname = this.kbdir + File.separator + dictionaryFile + ".txt";
		boolean loaded = false;
		try {
			new Lexicon();
			FileInputStream in = new FileInputStream(fname);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String lstr = null;
			while ((lstr = br.readLine()) != null) {
				if (lstr.charAt(0) != '#') {
					String[] parts = lstr.split(":");
					if (parts.length == 4) {
						String str = parts[0];
						String base = parts[1].toLowerCase();
						String pstr = parts[2].toLowerCase();
						String vstr = parts[3].toLowerCase();
						Vector<String> partsOfSpeech = StrUtils.stringList(
								pstr, ',');
						Vector<String> variants = StrUtils
								.stringList(vstr, ',');
						new Word(Lexicon.currentLexicon, str, base,
								partsOfSpeech, null, null, null, variants);
						loaded = true;
					}
				}
			}
			Lexicon.currentLexicon.resolveBaseWords();
			Lexicon.currentLexicon.resolveWordVariants();
		} catch (Exception e) {
			System.out.println(e);
		}
		return loaded;
	}

	public void writeAllKBFiles() throws Exception {
		File f = new File(this.kbdir);
		if (!f.exists()) {
			JOptionPane.showMessageDialog(new JFrame(), this.kbdir
					+ ": No such directory");
			return;
		}
		writeCUIFile();
		writeDictionaryFile();
		writeOntologyFiles();
		writeTUIFile();
		writeCUIFile();
	}

	public void writeOntologyFiles() throws Exception {
		for (Ontology ontology : KnowledgeEngine.getCurrentKnowledgeEngine()
				.getOntologies()) {
			String fname = FUtils.getFileName(this.kbdir, ontologyDirectory,
					ontology.getName(), ontologyFile);
			String str = ontology.toLispString();
			FUtils.writeFile(fname, str);
		}
	}

	public void writeDictionaryFile() throws Exception {
		String fname = this.kbdir + File.separator + dictionaryFile + ".txt";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					fname), true));
			for (Word word : Lexicon.currentLexicon.getAllWords()) {
				bw.write(word.toFileString());
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
		}
	}

	public void writeCUIFile() throws Exception {
		String fname = this.kbdir + File.separator + cuiPhraseFile + ".txt";
		UMLSStructuresShort us = UMLSStructuresShort.getUMLSStructures();
		Vector<CUIStructureShort> cps = us.getAllCUIStructures();
		BufferedWriter out = new BufferedWriter(new FileWriter(fname));
		if (cps != null) {
			for (CUIStructureShort cs : cps) {
				StringBuffer sb = new StringBuffer();
				sb.append(cs.getWordString(true));
				sb.append(":");
				sb.append(cs.getCUI());
				sb.append(":");
				sb.append(cs.getTUI());
				sb.append(":");
				sb.append(cs.getOntology().getName());
				sb.append("\n");
				out.write(sb.toString());
			}
		}
		out.close();
	}

	public void writeTUIFile() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("'(\n");
		String fname = this.kbdir + File.separator + tuiInfoFile;
		for (UMLSTypeInfo ti : UMLSTypeInfo.getAllTypeInfos()) {
			sb.append(ti.toLispString() + "\n");
		}
		sb.append(")\n");
		FUtils.writeFile(fname, sb.toString());
	}

}
