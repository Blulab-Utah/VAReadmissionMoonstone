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

import java.io.File;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.tokenizer.Token;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.FUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.TimeUtils;
import tsl.utilities.VUtils;

public class Document extends DocumentItemConstant {

	private int id = -1;
	private String filepath = null;
	private String relativeFilePath = null;
	private String absoluteFilePath = null;
	private String url = null;
	private String shortName = null;

	public String patientName = null;
	public String documentType = null;
	public String dictateDateString = null;
	public String admitDateString = null;

	public int dictateDateDay = 0;
	public int dictateDateMonth = 0;
	public int dictateDateYear = 0;
	public int admitDateDay = 0;
	public int admitDateMonth = 0;
	public int admitDateYear = 0;

	public int admitDictateDayDifference = -1;
	public int admitDictateDayDifferenceRangeIndex = -1;
	public int admitDateRangeIndex = -1;

	private Vector<Token> tokens = null;

	private Vector<Token> regexTokens = null;

	private Hashtable<Integer, Token> regExTokenHash = new Hashtable();

	public int tokenIndex = 0;

	public int lastTokenIndex = 0;

	private int conditionIndex = 0;

	private String text = null;

	private Vector<Header> headers = null;

	private Hashtable<String, Vector<String>> headerHash = null;

	private Vector<Sentence> allSentences = null;

	private Hashtable<String, Vector> wordSentenceHash = new Hashtable();

	public int textIndex = 0;

	private int selectionStart = 0;

	private int selectionEnd = 0;

	private boolean textWasEdited = false;

	private Document parent = null;

	private boolean isNoHeaders = false;

	private DocumentAccess documentAccess = null;

	private boolean lastHeaderFromHeaderList = false;

	private int generalDictationType = -1;

	public static char[] significantPunctuation = new char[] { ',', ';', ':',
			'?', '.' };

	public static int[][] DateRanges = new int[][] { { -30, 0 }, { 1, 7 },
			{ 8, 30 }, { 31, 180 }, { 181, 365 }, { 366, 1000000 } };

	public static int DICTATION_TYPE_SOCIAL = 1;
	public static int DICTATION_TYPE_EMERGENCY = 2;
	public static int DICTATION_TYPE_HISTORY = 3;
	public static int DICTATION_TYPE_DISCHARGE = 4;
	public static int DICTATION_TYPE_PHYSICIAN = 5;
	public static int DICTATION_TYPE_NURSE = 6;
	public static int DICTATION_TYPE_OTHER = 0;

	public Document() {
	}

	public Document(int id, String url, String name, String path, String text) {
		this.id = id;
		this.filepath = path;
		this.setName(name);
		this.text = text;
	}

	public Document(String text) {
		this.text = text;
	}

	public Document(String name, String text) {
		this.setName(name);
		this.setText(text);
	}

	public Document(String text, Hashtable<String, Vector<String>> headerHash) {
		this("NONAME", text);
		this.headerHash = headerHash;
	}

	public static Vector<Document> getDirectoryDocuments(String dname) {
		Vector<Document> documents = null;
		if (dname != null) {
			File file = new File(dname);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						File f = files[i];
						if (!f.isDirectory() && f.getName().charAt(0) != '.') {
							String text = FUtils.readFile(f);
							if (text != null) {
								Document d = new Document(f.getName(), text);
								d.setFullName(f.getAbsolutePath());
								d.setName(f.getName());
								documents = VUtils.add(documents, d);
							}
						}
					}
				}
			}
		}
		return documents;
	}

	public void tokenize() {
		try {
			this.getText();
			if (this.text != null && this.tokens == null) {
				this.tokenIndex = 0;
				Token.readTokensFromInput(this);
				this.tokenIndex = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		this.tokens = null;
		this.allSentences = null;
		this.headers = null;
		this.resetIndices();
	}

	public void resetIndices() {
		this.tokenIndex = 0;
		this.textIndex = 0;
	}

	public void gatherHeaders(boolean useSectionHeaderHeuristics) {
		if (this.tokens != null && !this.tokens.isEmpty()) {
			boolean atEnd = false;
			int hindex = 0;
			Header header = null;
			while (!atEnd) {
				header = Header
						.readNextHeader(this, useSectionHeaderHeuristics);
				if (header != null) {
					header.setReportIndex(hindex++);
					this.headers = VUtils.add(this.headers, header);
				} else if (this.atEnd()) {
					atEnd = true;
				} else {
					this.incrementTokenIndex(1);
				}
			}
		}
		this.resetIndices();
	}

	public void analyzeContent(boolean useSectionHeaderHeuristics) {
		this.tokenize();
		this.resetIndices();
		this.gatherHeaders(useSectionHeaderHeuristics);
		if (this.headers != null) {
			for (Header header : this.headers) {
				header.textStartTokenIndex = header.getTokens().lastElement()
						.getIndex() + 1;
				int hindex = header.getReportIndex();
				if (hindex < this.getHeaders().size() - 1) {
					Header next = this.getHeaders().elementAt(hindex + 1);
					header.textEndTokenIndex = next.getTokens().firstElement()
							.getIndex() - 1;
				} else {
					header.textEndTokenIndex = this.getTokenCount() - 1;
				}
				HeaderContent.readContent(header);
			}
		}

		// 3/27/2016: This ends up including a header with no sentences among
		// the
		// default header's content, e.g. "Homeless:" is treated as meaning
		// :HOMELESS:. I will try adding the default header only if there are
		// no headers...
		if (this.allSentences == null && this.headers == null) {
			analyzeSentencesNoHeader();
		}

		// 3/29/2016: COULD add step here that creates artificial headers in
		// spaces
		// between headers.
		if (this.allSentences != null) {
			int sindex = 0;
			for (Sentence s : this.allSentences) {
				s.documentIndex = sindex++;
			}
		}
		this.resetIndices();
	}

	// 3/16/2016: Determine whether a sentence overlaps an existing header
	// content.
	public boolean sentenceOverlapsExistingHeader(Sentence s) {
		if (this.headers != null) {
			for (Header h : this.headers) {
				if (!h.equals(s.getHeader())) {
					int hstart = h.textStartTokenIndex;
					int hend = h.textEndTokenIndex;
					int sstart = s.tokenStartIndex;
					int send = s.tokenEndIndex;
					boolean overlaps = SeqUtils.overlaps(hstart, hend, sstart,
							send);
					if (overlaps) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void analyzeSentencesNoHeader() {
		this.tokenize();
		this.setNoHeaders(true);
		if (this.text != null && this.allSentences == null) {
			Header header = new Header(this);
			this.headers = VUtils.add(this.headers, header);
			header.setContent(new NarrativeContent(header));
			header.getContent().readContent();
		}
		this.resetIndices();
	}

	// Before 3/29/2016
	// public void analyzeSentencesNoHeader() {
	// this.tokenize();
	// this.setNoHeaders(true);
	// if (this.text != null && this.allSentences == null) {
	// Header header = new Header(this);
	// this.headers = VUtils.add(this.headers, header);
	// header.setContent(new NarrativeContent(header));
	// header.getContent().readContent();
	// }
	// this.resetIndices();
	// }

	public void removeSentences() {
		this.allSentences = null;
		this.tokens = null;
		this.tokenIndex = this.lastTokenIndex = this.textIndex = 0;
		this.regExTokenHash.clear();
		this.regexTokens = null;
		this.wordSentenceHash.clear();
	}

	public Vector<Sentence> getSentencesContainingWord(String word) {
		Vector<Sentence> sentences = null;
		int index = word.indexOf(' ');
		if (index > 0) {
			Vector<String> words = StrUtils.stringList(word, ' ');
			Vector<Sentence> v = (Vector<Sentence>) wordSentenceHash.get(words
					.firstElement());
			if (v != null) {
				for (Sentence sentence : v) {
					if (sentence.text.indexOf(word) > 0) {
						sentences = VUtils.add(sentences, sentence);
					}
				}
			}
		} else {
			sentences = wordSentenceHash.get(word);
		}
		return sentences;
	}

	// 7/8/2015: Need to download StrUtils from VINCI!
	public Sentence getFirstSentenceContainingString(String str) {
		// str = StrUtils.trimNonAlpha(str);
		if (this.allSentences != null) {
			for (Sentence s : this.allSentences) {
				if (s.getText().contains(str)) {
					return s;
				}
			}
		}
		return null;
	}

	public Vector getSentenceStrings(boolean useSectionHeaderHeuristics) {
		this.analyzeContent(useSectionHeaderHeuristics);
		Vector strings = null;
		if (this.allSentences != null) {
			for (Enumeration e = this.allSentences.elements(); e
					.hasMoreElements();) {
				Sentence s = (Sentence) e.nextElement();
				strings = VUtils.add(strings, s.text);
			}
		}
		return strings;
	}

	public void initialize() {
		this.tokens = null;
		this.tokenIndex = 0;
		this.text = null;
		this.headers = null;
		this.textIndex = 0;
		this.selectionStart = 0;
		this.selectionEnd = 0;
	}

	public String toString() {
		return "[Document: " + this.name + "]";
	}

	// 1/29/2015
	public boolean atEnd() {
		if (this.getText() != null
				&& this.getTextIndex() >= this.getText().length()) {
			return true;
		}
		if (this.tokens != null && this.tokenIndex >= this.tokens.size()) {
			return true;
		}
		return false;
	}

	public boolean effectivelyAtEnd() {
		if (this.atEnd()) {
			return true;
		}
		if (this.getTokenCount() - this.getTokenIndex() < 10) {
			for (int i = 0; i < this.getTokenCount(); i++) {
				Token token = this.getToken(i);
				if (!token.isWhitespace()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// Before 1/29/2015
	// public boolean atEnd() {
	// if (this.tokens != null) {
	// return this.tokenIndex >= this.tokens.size();
	// }
	// return true;
	// }

	public boolean atLastToken() {
		return this.tokenIndex == this.tokens.size() - 2;
	}

	public int lengthToTextEnd() {
		return this.getText().length() - this.textIndex;
	}

	public boolean hasSelection() {
		return this.selectionStart < this.selectionEnd;
	}

	public String getSelection() {
		if (this.hasSelection()) {
			return this.getText().substring(this.selectionStart,
					this.selectionEnd);
		}
		return null;
	}

	public String getText(int start, int end) {
		if (start < this.getText().length() - 1 && start < end) {
			if (end >= this.getText().length() - 1) {
				end = this.getText().length() - 1;
			}
			return this.getText().substring(start, end + 1);
		}
		return null;
	}

	public void setText(String text) {
		this.text = text;
		this.textIndex = 0;
	}

	public String getText() {
		// Added 4/13/2016. WB running out of heap space. Try loading text on
		// demand rather than up front.
		if (this.text == null) {
			this.analyzeContent(true);
		}
		return this.text;
	}

	public void releaseText() {
		this.text = null;
	}

	public boolean equals(Object o) {
		if (o instanceof Document) {
			Document d = (Document) o;
			if (this.getName() != null && this.getName().equals(d.getName())) {
				return true;
			}
			if (this.getAbsoluteFilePath() != null
					&& this.getAbsoluteFilePath().equals(
							d.getAbsoluteFilePath())) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {
		if (this.getAbsoluteFilePath() != null) {
			return this.getAbsoluteFilePath().hashCode();
		}
		if (this.getName() != null) {
			return this.getName().hashCode();
		}
		// !!!
		return this.getText().hashCode();
	}

	public static class NameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Document d1 = (Document) o1;
			Document d2 = (Document) o2;
			return d1.name.compareTo(d2.name);
		}
	}

	public int getTokenIndex() {
		return tokenIndex;
	}

	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

	public int incrementTokenIndex(int amount) {
		this.tokenIndex += amount;
		return this.tokenIndex;
	}

	public int convertTextIndexToTokenIndex(int textIndex) {
		if (this.tokens != null) {
			for (int i = 0; i < this.tokens.size(); i++) {
				Token token = this.tokens.elementAt(i);
				if (token.getStart() >= textIndex
						&& token.getEnd() <= textIndex) {
					return i;
				}
			}
		}
		return -1;
	}

	public int getConditionIndex() {
		return conditionIndex;
	}

	public void setConditionIndex(int conditionIndex) {
		this.conditionIndex = conditionIndex;
	}

	public Vector<Sentence> getAllSentences() {
		return allSentences;
	}

	public void setAllSentences(Vector<Sentence> allSentences) {
		this.allSentences = allSentences;
	}

	public int getTextIndex() {
		return textIndex;
	}

	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}

	public int incrementTextIndex(int amount) {
		this.textIndex += amount;
		return this.textIndex;
	}

	public int getSelectionStart() {
		return selectionStart;
	}

	public void setSelectionStart(int selectionStart) {
		this.selectionStart = selectionStart;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = selectionEnd;
	}

	public boolean isTextWasEdited() {
		return textWasEdited;
	}

	public void setTextWasEdited(boolean textWasEdited) {
		this.textWasEdited = textWasEdited;
	}

	public boolean isNoHeaders() {
		return isNoHeaders;
	}

	public void setNoHeaders(boolean isNoHeaders) {
		this.isNoHeaders = isNoHeaders;
	}

	public Document getParent() {
		return parent;
	}

	public void setParent(Document parent) {
		this.parent = parent;
	}

	public Vector<Token> getTokens() {
		return tokens;
	}

	public Token getToken(int index) {
		if (this.tokens != null && index < this.tokens.size()) {
			return this.tokens.elementAt(index);
		}
		return null;
	}

	public Vector<Token> getTokens(int start, int end) {
		if (start >= 0 && end < this.getTokenCount()) {
			return VUtils.subVector(this.tokens, start, end + 1);
		}
		return null;
	}

	// 10/16/2014: For use with Moonstone / HeidelTime UIMA pipeline.
	public int getTokens(int startindex, int starttext, int endtext,
			Vector<Token> tokens) {
		if (this.tokens != null && startindex < this.getTokenCount()) {
			for (int i = startindex; i < this.getTokenCount(); i++) {
				Token t1 = this.getToken(i);
				if (t1.getStart() > starttext) {
					break;
				}
				if (starttext == t1.getStart()) {
					for (int j = i; j < this.getTokenCount(); j++) {
						Token t2 = this.getToken(j);
						if (endtext == t2.getEnd()) {
							tokens.add(t1);
							if (i < j) {
								tokens.add(t2);
							}
							return j + 1;
						}
					}
				}
			}
		}
		return -1;
	}

	public int getTokenCount() {
		if (this.tokens != null) {
			return this.tokens.size();
		}
		return 0;
	}

	public void setTokens(Vector<Token> tokens) {
		this.tokens = tokens;
	}

	public void addToken(Token token) {
		this.tokens = VUtils.add(this.tokens, token);
	}

	public Hashtable<Integer, Token> getRegExTokenHash() {
		return regExTokenHash;
	}

	public void setRegExTokenHash(Hashtable<Integer, Token> regExTokenHash) {
		this.regExTokenHash = regExTokenHash;
	}

	public Vector<Header> getHeaders() {
		return headers;
	}

	public Header getLastHeader() {
		if (headers != null) {
			return headers.lastElement();
		}
		return null;
	}

	public Token getLastToken() {
		if (this.tokens != null) {
			return this.tokens.lastElement();
		}
		return null;
	}

	public void setHeaders(Vector<Header> headers) {
		this.headers = headers;
	}

	public Hashtable<String, Vector> getWordSentenceHash() {
		return wordSentenceHash;
	}

	public void setWordSentenceHash(Hashtable<String, Vector> wordSentenceHash) {
		this.wordSentenceHash = wordSentenceHash;
	}

	public Vector<Token> getRegexTokens() {
		return regexTokens;
	}

	public void setRegexTokens(Vector<Token> regexTokens) {
		this.regexTokens = regexTokens;
	}

	public void addRegexToken(Token token) {
		if (this.regExTokenHash == null) {
			this.regExTokenHash = new Hashtable<Integer, Token>();
		}
		regExTokenHash.put(new Integer(token.getStart()), token);
		regexTokens = VUtils.add(this.regexTokens, token);
	}

	public Token getRegexToken(int index) {
		if (this.regExTokenHash != null) {
			return this.regExTokenHash.get(new Integer(index));
		}
		return null;
	}

	public DocumentAccess getDocumentAccess() {
		return documentAccess;
	}

	public void setDocumentAccess(DocumentAccess documentAccess) {
		this.documentAccess = documentAccess;
	}

	public Hashtable<String, Vector<String>> getHeaderHash() {
		return headerHash;
	}

	public void addSentence(Sentence sentence) {
		this.allSentences = VUtils.add(this.allSentences, sentence);
	}

	public static char[] getSignificantPunctuation() {
		return significantPunctuation;
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}

	public void setAbsoluteFilePath(String absoluteFilePath) {
		this.absoluteFilePath = absoluteFilePath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

	public Sentence getSentence(int start) {
		if (this.headers != null) {
			for (Header h : this.headers) {
				if (h.containsNarrative()) {
					if (h.getSentences() != null) {
						for (Sentence s : h.getSentences()) {
							if (s.getStart() <= start && s.getEnd() >= start) {
								return s;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public int getNumberOfSentences() {
		return (this.allSentences == null ? 0 : this.allSentences.size());
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	// 4/20/2015
	public String getPatientName() {
		return patientName;
	}

	// E.g. If file name == "150_10102013_moretext", name == "150" &
	// date=10102013.
	public boolean extractPatientNameAndDateReadmissionFormat(String fname) {
		if (fname != null) {
			int index1 = fname.indexOf("_");
			String namestr = null;
			String datestr = null;
			if (index1 > 0) {
				namestr = fname.substring(0, index1);
				int index2 = fname.indexOf("_", index1 + 1);
				if (index2 > index1) {
					datestr = fname.substring(index1 + 1, index2);
				}
			}
			if (namestr != null && datestr != null) {
				this.patientName = namestr;
				this.dictateDateString = datestr;
				return true;
			}
		}
		return false;
	}

	public boolean extractPatientNameAndDatesFromFirstLine(
			String[] dateStringFormats) {
		int nlindex = this.getText().indexOf("\n");
		if (nlindex < 1) {
			this.patientName = "000";
			return true;
		}
		String line = this.getText().substring(0, nlindex);
		int patindex = line.indexOf("StudyID:");
		int admitindex = line.indexOf("AdmitDay:");
		int typeindex = line.indexOf("DocumentType:");
		int dictateindex = line.indexOf("Date:");
		if (patindex != -1 && patindex < admitindex && admitindex < typeindex
				&& typeindex < dictateindex) {
			patindex += "StudyID:".length();
			this.patientName = line.substring(patindex, admitindex - 1).trim();
			admitindex += "AdmitDay:".length();
			int colonindex = line.substring(admitindex).indexOf(":");
			int spaceindex = line.substring(admitindex).indexOf(" ");
			int endindex = (colonindex < spaceindex ? colonindex : spaceindex);
			this.admitDateString = line.substring(admitindex, admitindex + endindex)
					.trim();
			typeindex += "DocumentType:".length();
			this.documentType = line.substring(typeindex, dictateindex - 1)
					.trim();
			dictateindex += "Date:".length();
			this.dictateDateString = StrUtils.getStringToWhitespace(line,
					dictateindex);

			// Before 12/21/2016
//			Date admitdate = TimeUtils.getDateFromString(this.admitDateString,
//					admitDateFormat);
			
			Date admitdate = TimeUtils.getDateFromString(this.admitDateString,
					dateStringFormats);
			
			if (admitdate != null) {
				Calendar c = new GregorianCalendar();
				c.setTime(admitdate);
				this.admitDateDay = c.get(Calendar.DAY_OF_MONTH);
				this.admitDateMonth = c.get(Calendar.MONTH) + 1;
				this.admitDateYear = c.get(Calendar.YEAR);
			}
			
			// Before 12/21/2016
//			Date dictatedate = TimeUtils.getDateFromString(
//					this.dictateDateString, dictateDateFormat);
			Date dictatedate = TimeUtils.getDateFromString(
					this.dictateDateString, dateStringFormats);
			
			if (dictatedate != null) {
				Calendar c = new GregorianCalendar();
				c.setTime(dictatedate);
				this.dictateDateDay = c.get(Calendar.DAY_OF_MONTH);
				this.dictateDateMonth = c.get(Calendar.MONTH) + 1;
				this.dictateDateYear = c.get(Calendar.YEAR);
			}
			return true;
		}
		return false;
	}

	// StudyID:29 AdmitDay:15JUN2012 DocumentType:TCU NURSING NOTE - INPATIENT
	// Date:15JUN2012
	public boolean extractPatientNameAndDatesFromFirstLine_BEFORE_12_12_2016() {
		int nlindex = this.getText().indexOf("\n");
		if (nlindex < 1) {
			this.patientName = "000";
			return true;
		}
		String line = this.getText().substring(0, nlindex);
		int patindex = line.indexOf("StudyID:");
		int admitindex = line.indexOf("AdmitDay:");
		int typeindex = line.indexOf("DocumentType:");
		int dictateindex = line.indexOf("Date:");
		if (patindex != -1 && patindex < admitindex && admitindex < typeindex
				&& typeindex < dictateindex) {
			patindex += "StudyID:".length();
			this.patientName = line.substring(patindex, admitindex - 1).trim();
			admitindex += "AdmitDay:".length();
			this.admitDateString = line.substring(admitindex, typeindex - 1)
					.trim();
			typeindex += "DocumentType:".length();
			this.documentType = line.substring(typeindex, dictateindex - 1)
					.trim();
			dictateindex += "Date:".length();
			this.dictateDateString = StrUtils.getStringToWhitespace(line,
					dictateindex);

			try {
				String daystr = admitDateString.substring(0, 2);
				String monthstr = admitDateString.substring(2, 5);
				String yearstr = admitDateString.substring(5, 9);
				this.admitDateDay = Integer.parseInt(daystr);

				this.admitDateMonth = getMonth(monthstr);
				this.admitDateYear = Integer.parseInt(yearstr);
				daystr = dictateDateString.substring(0, 2);
				monthstr = dictateDateString.substring(2, 5);
				yearstr = dictateDateString.substring(5, 9);
				this.dictateDateDay = Integer.parseInt(daystr);
				this.dictateDateMonth = getMonth(monthstr);
				this.dictateDateYear = Integer.parseInt(yearstr);
			} catch (Exception e) {
				int x = 1;
			}

			return true;
		}
		return false;
	}

	private int getMonth(String mstr) throws Exception {
		int month = 0;
		mstr = mstr.toUpperCase();
		if ("JAN".equals(mstr)) {
			month = 1;
		} else if ("FEB".equals(mstr)) {
			month = 2;
		} else if ("MAR".equals(mstr)) {
			month = 3;
		} else if ("APR".equals(mstr)) {
			month = 4;
		} else if ("MAY".equals(mstr)) {
			month = 5;
		} else if ("JUN".equals(mstr)) {
			month = 6;
		} else if ("JUL".equals(mstr)) {
			month = 7;
		} else if ("AUG".equals(mstr)) {
			month = 8;
		} else if ("SEP".equals(mstr)) {
			month = 9;
		} else if ("OCT".equals(mstr)) {
			month = 10;
		} else if ("NOV".equals(mstr)) {
			month = 11;
		} else if ("DEC".equals(mstr)) {
			month = 12;
		}
		return month;
	}

	public int getAbsoluteAdmitDay() {
		int aday = 0;
		if (this.admitDateDay > 0 && this.admitDateYear > 0
				&& this.admitDateMonth > 0) {
			aday = this.admitDateYear * 356 + this.admitDateMonth * 30
					+ this.admitDateDay;
		}
		return aday;
	}

	public int getAbsoluteDictationDay() {
		int dday = this.dictateDateYear * 356 + this.dictateDateMonth * 30
				+ this.dictateDateDay;
		return dday;
	}

	public int getAdmitDictationDayDifference() {
		return this.getAbsoluteAdmitDay() - this.getAbsoluteDictationDay();
	}

	public static String extractPatientNameFromReportName(String fname) {
		if (fname != null) {
			int index1 = fname.indexOf("_");
			if (index1 > 0) {
				String namestr = fname.substring(0, index1);
				return namestr;
			}
		}
		return null;
	}

	public static String extractDateFromReportName(String fname) {
		if (fname != null) {
			int index1 = fname.indexOf("_");
			if (index1 > 0) {
				int index2 = fname.indexOf("_", index1 + 1);
				if (index2 > index1) {
					return fname.substring(index1 + 1, index2);
				}
			}
		}
		return null;
	}

	public boolean isLastHeaderFromHeaderList() {
		return lastHeaderFromHeaderList;
	}

	public void setLastHeaderFromHeaderList(boolean lastHeaderFromHeaderList) {
		this.lastHeaderFromHeaderList = lastHeaderFromHeaderList;
	}

	// 8/18/2016: Not tested...
	public int getAdmitDateRangeIndex() {
		if (this.admitDateRangeIndex == -1 && this.admitDateDay > 0) {
			this.admitDictateDayDifference = this
					.getAdmitDictationDayDifference();
			if (this.admitDictateDayDifference >= 0) {
				for (int i = 0; i < DateRanges.length; i++) {
					int[] dr = DateRanges[i];
					if (dr[0] <= this.admitDictateDayDifference
							&& this.admitDictateDayDifference <= dr[1]) {
						this.admitDateRangeIndex = i;
						break;
					}
				}
			}
		}
		return this.admitDateRangeIndex;
	}

	public void getGeneralDictationTypeFromFilename() {
		String lcf = this.getName().toLowerCase();
		this.generalDictationType = DICTATION_TYPE_OTHER;
		if (lcf.contains("social") || lcf.contains("sws")) {
			this.generalDictationType = DICTATION_TYPE_SOCIAL;
		} else if (lcf.contains("nur") || lcf.contains("rn")) {
			this.generalDictationType = DICTATION_TYPE_NURSE;
		} else if (lcf.contains("emerg")) {
			this.generalDictationType = DICTATION_TYPE_EMERGENCY;
		} else if (lcf.contains("history") || lcf.contains("h&p")) {
			this.generalDictationType = DICTATION_TYPE_HISTORY;
		} else if (lcf.contains("primary") || lcf.contains("md")) {
			this.generalDictationType = DICTATION_TYPE_PHYSICIAN;
		}
	}

	public int getGeneralDictationType() {
		if (this.generalDictationType == -1) {
			this.getGeneralDictationTypeFromFilename();
		}
		return this.generalDictationType;
	}

}
