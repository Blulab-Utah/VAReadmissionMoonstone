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
import java.util.Hashtable;
import java.util.Vector;

import com.mysql.jdbc.Connection;

import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.constant.Constant;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.CUIStructureWrapperShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.utilities.VUtils;

public class DBTermIndex extends Constant {
	private int documentID = 0;
	private int sentenceIndex = 0;
	private int termIndex = 0;
	private String term = null;
	private int textStart = 0;
	private int textEnd = 0;

	private String SDIndex = null;

	public DBTermIndex(String term, int tindex, int docid, int sindex,
			int tstart, int tend) {
		this.term = term.toLowerCase();
		// Before 1/12/2014
//		this.term = term;
		this.termIndex = tindex;
		this.documentID = docid;
		this.sentenceIndex = sindex;
		this.textStart = tstart;
		this.textEnd = tend;
		this.SDIndex = generateSDIndex();
	}

	public static Vector<String> getTerms(Vector<DBTermIndex> tps) {
		Vector<String> terms = null;
		if (tps != null) {
			for (DBTermIndex tp : tps) {
				terms = VUtils.add(terms, tp.getTerm());
			}
		}
		return terms;
	}
	
	public static Vector<String> getFirstTerms(Vector<Vector<DBTermIndex>> tpvs) {
		Vector<String> terms = null;
		if (tpvs != null) {
			for (Vector<DBTermIndex> tpv : tpvs) {
				if (tpv != null) {
					DBTermIndex tp = tpv.firstElement();
					terms = VUtils.add(terms, tp.getTerm());
				}
			}
		}
		return terms;
	}

	public static String generateIndexString(Vector<DBTermIndex> tps) {
		StringBuffer sb = new StringBuffer();
		if (tps != null) {
			for (DBTermIndex tp : tps) {
				String tindex = String.valueOf(tp.getTermIndex());
				String docid = String.valueOf(tp.getDocumentID());
				String sindex = String.valueOf(tp.getSentenceIndex());
				String tstart = String.valueOf(tp.getTextStart());
				String tend = String.valueOf(tp.getTextEnd());
				sb.append(tindex);
				sb.append(",");
				sb.append(docid);
				sb.append(",");
				sb.append(sindex);
				sb.append(",");
				sb.append(tstart);
				sb.append(",");
				sb.append(tend);
				sb.append(";");
			}
		}
		return sb.toString();
	}

	public static void storeTermIndexes(Vector<DBTermIndex> tps) {
		Hashtable<String, Vector<DBTermIndex>> hash = new Hashtable();
		if (tps != null) {
			for (DBTermIndex tp : tps) {
				VUtils.pushHashVector(hash, tp.getTerm(), tp);
			}
			for (Enumeration<String> e = hash.keys(); e.hasMoreElements();) {
				String term = e.nextElement();
				String istr = getDBTermIndexString(term);
				String newstr = "";
				if (istr != null) {
					newstr = new String(istr);
				}
				Vector<DBTermIndex> v = hash.get(term);
				newstr += DBTermIndex.generateIndexString(v);
				storeIndexes(term, newstr);
			}
		}
	}

	public static void storeIndexes(String term, String istr) {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "delete from TERMINDEXES where term = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setString(1, term);
			ps.execute();
			sql = "insert into TERMINDEXES (term, indexes) values(?, ?)";
			ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			ps.setString(1, term);

			ps.setString(2, istr);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getDBTermIndexString(String term) {
		String istr = null;
		try {

			String sql = "select indexes from TERMINDEXES where term = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) MySQL
					.getMySQL().getConnection().prepareStatement(sql);
			ps.setString(1, term);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				istr = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return istr;
	}

	public static Vector<Vector<DBTermIndex>> getDBTermIndexes(
			Vector<String> terms) {
		Vector<Vector<DBTermIndex>> alltps = new Vector(0);
		if (terms != null) {
			for (String term : terms) {
				Vector<DBTermIndex> tps = getDBTermIndexes(term);
				if (tps != null) {
					alltps.add(tps);
				}
			}
		}
		return alltps;
	}

	public static Vector<DBTermIndex> getDBTermIndexes(String term) {
		String istr = getDBTermIndexString(term);
		return extractDBTermIndexesFromIndexString(term, istr);
	}

	public static Vector<DBTermIndex> extractDBTermIndexesFromIndexString(
			String term, String istr) {
		Vector<DBTermIndex> tps = null;
		try {
			if (istr != null) {
				String[] sstrs = istr.split(";");
				if (sstrs != null) {
					for (int i = 0; i < sstrs.length; i++) {
						String[] ss = sstrs[i].split(",");
						int tindex = Integer.parseInt(ss[0]);
						int docid = Integer.parseInt(ss[1]);
						int sindex = Integer.parseInt(ss[2]);
						int tstart = Integer.parseInt(ss[3]);
						int tend = Integer.parseInt(ss[4]);
						DBTermIndex tp = new DBTermIndex(term, tindex, docid,
								sindex, tstart, tend);
						tps = VUtils.add(tps, tp);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tps;
	}

	public static Vector<DBTermIndex> extractDBTermIndexesFromSentence(
			tsl.documentanalysis.document.Sentence s,
			boolean removeCUIDuplicates) {
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		Vector<DBTermIndex> tps = null;
		Vector<CUIStructureWrapperShort> v = uss.getCUIStructureWrappers(
				s.getTokens(), null, removeCUIDuplicates);
		if (v != null) {
			for (CUIStructureWrapperShort cpw : v) {
				if (cpw.getCuiStructure().isRelevant()) {
					String cui = cpw.getCuiStructure().getCui();
					int docid = s.getDocument().getId();
					int sindex = s.getDocumentIndex();
					int tstart = cpw.getTextStart();
					int tend = cpw.getTextEnd();
					DBTermIndex tp = new DBTermIndex(cui, -1, docid, sindex,
							tstart, tend);
					tps = VUtils.add(tps, tp);
				}
			}
		}
		if (s.getTokens() != null) {
			for (Token token : s.getTokens()) {
				if (CUIStructureShort.isCUI(token.getString())
						|| (token.getWord() != null && token.getWord()
								.isSemanticallyRelevant())) {
					String word = token.getString();
					int docid = s.getDocument().getId();
					int sindex = s.getDocumentIndex();
					int tstart = token.getStart();
					int tend = token.getEnd();
					DBTermIndex tp = new DBTermIndex(word, -1, docid, sindex,
							tstart, tend);
					tps = VUtils.add(tps, tp);
				}
			}
		}
		Vector<DBTermIndex> wptps = DBWordPattern.getConTextDBTermIndexes(s);
		tps = VUtils.append(tps, wptps);
		return tps;
	}

	public static void storeIndexes(Vector<Document> documents) {
		if (documents != null) {
			Hashtable<String, Vector<DBTermIndex>> termIndexHash = new Hashtable();
			for (Document document : documents) {
				document.analyzeContent(true);
				DBDocument.storeDocument(document);
				System.out.println("Storing document / term indexes for "
						+ document.getName());
				if (document.getHeaders() != null) {
					for (Header h : document.getHeaders()) {
						if (h.getSentences() != null) {
							for (tsl.documentanalysis.document.Sentence s : h
									.getSentences()) {
								Vector<DBTermIndex> tps = extractDBTermIndexesFromSentence(
										s, false);
								if (tps != null) {
									for (DBTermIndex ti : tps) {
										VUtils.pushHashVector(termIndexHash,
												ti.getTerm(), ti);
									}
								}
							}
						}
					}
				}
				document.removeSentences();
			}
			for (Enumeration<String> e = termIndexHash.keys(); e
					.hasMoreElements();) {
				String term = e.nextElement();
				Vector<DBTermIndex> tis = termIndexHash.get(term);
				if (tis != null) {
					String istr = generateIndexString(tis);
					DBTermIndex.storeIndexes(term, istr);
				}
			}
			System.out.println("");
		}
	}

	public String generateSDIndex() {
		String str = this.documentID + ":" + this.sentenceIndex;
		return str;
	}

	public String getTerm() {
		return term;
	}

	public boolean isCui() {
		return this.term != null && this.term.length() > 2
				&& this.term.charAt(0) == 'C'
				&& Character.isDigit(this.term.charAt(1));
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getTextStart() {
		return textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}

	public int getDocumentID() {
		return documentID;
	}

	public void setDocumentID(int documentID) {
		this.documentID = documentID;
	}

	public String toString() {
		String str = this.term.toString() + ":" + this.termIndex + ":"
				+ this.documentID + ":" + this.sentenceIndex + ":"
				+ this.textStart + ":" + this.textEnd + ":";
		return str;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	public String getSDIndex() {
		return SDIndex;
	}

	public void setSDIndex(String sDIndex) {
		SDIndex = sDIndex;
	}

	public int getTermIndex() {
		return termIndex;
	}

}
