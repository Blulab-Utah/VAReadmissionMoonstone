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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.VUtils;

public class HeaderContent {

	protected Header header = null;
	private Sentence coveringSentence = null;
	private Vector<Sentence> sentences = null;
	private static Vector<String> TableHeaderStrings = null;
	private static String TableHeaderStringFileName = "TableHeaderStrings";
	private static String ResourceDirectoryName = "resource_directory";

	public HeaderContent(Header header) {
		this.header = header;
		this.coveringSentence = new Sentence(header, header.getTextTokens(),
				false);
	}

	public static void initialize() {
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		String rdname = ke.getStartupParameters().getPropertyValue(
				ResourceDirectoryName);
		String thfname = ke.getStartupParameters().getPropertyValue(
				TableHeaderStringFileName);
		if (rdname != null) {
			String fullname = rdname + File.separatorChar + thfname;
			File file = new File(fullname);
			if (file.exists()) {
				try {
					InputStream fis = new FileInputStream(fullname);
					InputStreamReader isr = new InputStreamReader(fis,
							Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(isr);
					String line = null;
					while ((line = br.readLine()) != null) {
						line = line.toLowerCase().trim();
						TableHeaderStrings = VUtils.add(TableHeaderStrings,
								line);
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void readContent() {

	}

	// 8/19/2015 Changed
	public static void readContent(Header header) {
		HeaderContent content = null;
		if (header.getTextTokens() != null && !header.getTextTokens().isEmpty()) {
			content = new NarrativeContent(header);
			content.readContent();
			header.setContent(content);
		}
	}

	public static boolean isNarrative(Header header) {
		return !isTable(header);
	}

	public static boolean isTable(Header header) {
		boolean flag = true;
		if (flag) {
			return false;
		}
		Document doc = header.document;
		int avdcount = 0;
		if (isKnownTableHeader(header)) {
			return true;
		}
		for (int i = header.getTextStartTokenIndex(); i < header
				.getTextEndTokenIndex(); i++) {
			Token token = doc.getToken(i);
			if (token.isAVPairDelimiter()) {
				avdcount++;
			}
		}
		return (avdcount > 3);
	}

	private static boolean isKnownTableHeader(Header header) {
		if (TableHeaderStrings != null) {
			String htext = header.getText().toLowerCase();
			for (String hstr : TableHeaderStrings) {
				if (htext.contains(hstr)) {
					return true;
				}
			}
		}
		return false;
	}

	public Header getHeader() {
		return header;
	}

	public Vector<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(Vector<Sentence> sentences) {
		this.sentences = sentences;
	}

	public void addSentence(Sentence sentence) {
		this.sentences = VUtils.add(this.sentences, sentence);
	}

	public Sentence getCoveringSentence() {
		return coveringSentence;
	}

	public String toString() {
		String str = "<Header=" + this.header + ",Content="
				+ this.getSentences() + ">";
		return str;
	}

	// 4/8/2016
	public boolean hasInterveningQuestionMarkOrColon(int sindex1, int sindex2) {
		if (this.sentences != null && sindex1 == sindex2 - 1
				&& sindex2 < this.sentences.size()) {
			Sentence s1 = this.sentences.elementAt(sindex1);
			Sentence s2 = this.sentences.elementAt(sindex2);
			boolean foundpunc = false;
			boolean foundnonwhite = false;
			for (int i = s1.tokenEndIndex + 1; i < s2.tokenStartIndex - 1; i++) {
				Token t = s1.getDocument().getToken(i);
				if (t.isQuestionMark() || t.isColon()) {
					foundpunc = true;
				} else if (!t.isWhitespace()) {
					foundnonwhite = true;
				}
			}
			return (foundpunc && !foundnonwhite);
		}
		return false;
	}

}
