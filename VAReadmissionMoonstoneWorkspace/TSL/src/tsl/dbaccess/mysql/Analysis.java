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
package tsl.dbaccess.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import com.mysql.jdbc.Connection;

import tsl.documentanalysis.document.Document;
import tsl.expression.form.sentence.Sentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

public class Analysis {

	private Document document = null;
	private String source = null;
	private String name = null;
	private Vector<Sentence> sentences = null;
	private KnowledgeBase knowledgeBase = null;

	public Analysis(Document document, String source, Vector<Sentence> sentences) {
		this.setDocument(document);
		this.setSource(source);
		this.setSentences(sentences);
		this.getName();
		this.getKnowledgeBase();
	}

	public static Analysis getAnalysis(Document document, String source,
			Vector<Sentence> sentences) {
		Analysis analysis = new Analysis(document, source, sentences);
		return analysis;
	}

	public static Analysis getAnalysis(Document document, String source) {
		String name = createName(document, source);
		Analysis analysis = MySQL.getMySQL().getAnalysisHash().get(name);
		if (analysis == null) {
			boolean createdFromSource = false;
			Vector<Sentence> sentences = getSentencesFromDB(document, source);
			if (sentences == null) {
				sentences = getSentencesFromSource(document, source);
				createdFromSource = true;
			}
			if (sentences != null) {
				analysis = new Analysis(document, source, sentences);
				if (createdFromSource) {
					analysis.store();
				}
			}
			if (analysis != null) {
				MySQL.getMySQL().getAnalysisHash().put(name, analysis);
			}
		}
		return analysis;
	}

	// 11/28/2013: Placeholder method for passing in a document and getting back
	// a
	// list of interpretation sentences.
	public static Vector<Sentence> getInterpretation(Document document,
			String source) {
		return null;
	}

	private static Vector<Sentence> getSentencesFromSource(Document document,
			String source) {
		return getInterpretation(document, source);
	}

	private static Vector<Sentence> getSentencesFromDB(Document document,
			String source) {
		Connection c = MySQL.getMySQL().getConnection();
		Vector<Sentence> sentences = null;
		try {
			String sql = "select analysis from ANALYSES where url = ?, source = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setString(1, document.getUrl());
			ps.setString(2, source);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				String astr = rs.getString(1);
				sentences = Analysis.convertAnalysisStringToSentences(astr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sentences;
	}

	public void store() {
		Connection c = MySQL.getMySQL().getConnection();
		try {
			if (this.sentences != null) {
				StringBuffer sb = new StringBuffer();
				for (Sentence s : this.sentences) {
					String str = s.toLisp();
					sb.append(str + "\n");
				}
				String analysis = sb.toString();
				String sql = "insert url = ?, source = ?, analysis = ? into ANALYSIS";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
						.prepareStatement(sql);
				ps.setString(1, this.document.getUrl());
				ps.setString(2, source);
				ps.setString(3, analysis);
				ps.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 10/2/2013: Converts an analysis string to a set of sentences.
	public static Vector<Sentence> convertAnalysisStringToSentences(String astr) {
		Vector<Sentence> sentences = null;
		try {
			if (astr != null) {
				KnowledgeBase kb = new KnowledgeBase("temporary");
				String str = "'" + astr;
				Sexp sexp = (Sexp) TLisp.getTLisp().evalString(str);
				if (sexp != null) {
					for (Enumeration<Sexp> e = sexp.elements(); e
							.hasMoreElements();) {
						Sexp s = e.nextElement();
						Vector v = TLUtils.convertSexpToJVector(s);
						Sentence sent = (Sentence) kb.initializeForm(v);
						sentences = VUtils.add(sentences, sent);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sentences;
	}

	public KnowledgeBase getKnowledgeBase() {
		if (this.knowledgeBase == null) {
			this.knowledgeBase = new KnowledgeBase();
			if (this.sentences != null) {
				for (Sentence s : this.sentences) {
					this.knowledgeBase.initializeAndAddForm(s);
				}
			}
		}
		return this.knowledgeBase;
	}

	public void pushKnowledgeBase() {
		this.knowledgeBase.getKnowledgeEngine().pushKnowledgeBase(
				this.getKnowledgeBase());
	}

	public void popKnowledgeBase() {
		this.knowledgeBase.getKnowledgeEngine().popKnowledgeBase();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Vector<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(Vector<Sentence> sentences) {
		this.sentences = sentences;
	}

	public String getName() {
		if (this.name == null) {
			this.name = createName(this.document, this.source);
		}
		return this.name;
	}

	private static String createName(Document document, String source) {
		return document.getUrl() + ":" + source;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
