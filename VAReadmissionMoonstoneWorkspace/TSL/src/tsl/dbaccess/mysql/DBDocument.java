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
import java.util.Hashtable;

import com.mysql.jdbc.Connection;

import tsl.documentanalysis.document.Document;

public class DBDocument {

	// Store document
	public static boolean storeDocument(Document doc) {
		try {
			int docid = -1;
			MySQL mysql = MySQL.getMySQL();
			Connection c = mysql.getConnection();
			String sql = "select id from DOCUMENTS where url = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setString(1, doc.getFullName());
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				docid = rs.getInt(1);
				doc.setId(docid);
			} else {
				docid = DBDocument.getHighestDocumentID();
				docid++;
				doc.setId(docid);
				sql = "insert into DOCUMENTS (id, url, text, valid) "
						+ "values(?,?,?,?)";
				ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
				ps.setInt(1, docid);
				ps.setString(2, doc.getFullName());
				ps.setString(3, doc.getText());
				ps.setBoolean(4, true);
				ps.execute();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getHighestDocumentID() {
		int highest = 0;
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select MAX(id) from DOCUMENTS";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				highest = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return highest;
	}

	// Get document given id
	public static Document getDocument(int id) {
		String idstr = String.valueOf(id);
		Connection c = MySQL.getMySQL().getConnection();
		Hashtable<String, Document> dhash = MySQL.getMySQL().getDocumentHash();
		Document document = MySQL.getMySQL().getDocumentHash().get(idstr);
		if (document == null) {
			try {
				String sql = "select url, text, valid from DOCUMENTS where id = ?";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
						.prepareStatement(sql);
				ps.setInt(1, id);
				ResultSet rs = ps.executeQuery();
				if (rs.first()) {
					String url = rs.getString(1);
					String text = rs.getString(2);
					boolean valid = rs.getBoolean(3);
					if (valid) {
						document = new Document(id, url, idstr, "*", text);
						document.analyzeContent(true);
						dhash.put(idstr, document);
//						dhash.put(url, document);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	public static Document getDocument(String url) {
		Hashtable<String, Document> dhash = MySQL.getMySQL().getDocumentHash();
		Connection c = MySQL.getMySQL().getConnection();
		Document document = dhash.get(url);
		if (document == null) {
			try {
				String sql = "select id, text, valid from DOCUMENTS where url = ?";
				com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
						.prepareStatement(sql);
				ps.setString(1, url);
				ResultSet rs = ps.executeQuery();
				if (rs.first()) {
					int id = rs.getInt(1);
					String text = rs.getString(2);
					boolean valid = rs.getBoolean(3);
					String idstr = String.valueOf(id);
					if (valid) {
						document = new Document(id, url, idstr, "*", text);
						document.analyzeContent(true);
						dhash.put(idstr, document);
						dhash.put(url, document);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return document;
	}

	public static tsl.documentanalysis.document.Sentence getSentenceFromSDI(
			String sdi) {
		tsl.documentanalysis.document.Sentence sentence = null;
		String[] strs = sdi.split(":");
		int docid = Integer.valueOf(strs[0]);
		int sentid = Integer.valueOf(strs[1]);
		Document document = DBDocument.getDocument(docid);
		if (document != null) {
			sentence = document.getAllSentences().elementAt(sentid);
		}
		return sentence;
	}

	public static Document getDocumentFromSDI(String sdi) {
		String[] strs = sdi.split(":");
		int docid = Integer.valueOf(strs[0]);
		Document document = DBDocument.getDocument(docid);
		return document;
	}

}
