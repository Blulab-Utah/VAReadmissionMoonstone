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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.DocumentItemConstant;
import tsl.documentanalysis.document.Sentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.knowledge.ontology.umls.CUIStructureShort;
import tsl.knowledge.ontology.umls.UMLSStructuresShort;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import tsl.utilities.VUtils;

import com.mysql.jdbc.Connection;

public class MySQL {

	private com.mysql.jdbc.Connection connection = null;
	private com.mysql.jdbc.Connection umlsConnection = null;
	private Hashtable<String, Document> documentHash = new Hashtable();
	private KnowledgeBase kb = null;
	private Ontology ontology = null;
	private int highestDocumentID = 0;
	private int highestTermIndexID = 0;
	private int highestTermInstanceID = 0;
	private int highestSentenceInstanceID = 0;
	private Hashtable<String, DBWordPattern> dbWordPatternHash = new Hashtable();
	private Hashtable<String, DBTermInfo> termInfoHash = new Hashtable();
	private Hashtable<Integer, DBExpressionInstance> expressionInstanceHash = new Hashtable();
	private Hashtable<String, Analysis> analysisHash = new Hashtable();
	private static MySQL mySQL = null;
	private static String connectionString = "jdbc:mysql://localhost/tsldb?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false&jdbcCompliantTruncation=false&SetBigStringTryClob=true&max_allowed_packet=1G";
	private static String umlsConnectionString = "jdbc:mysql://localhost/short_umls?user=root&ConnectionTimout=10000&SocketTimeout=10000&useUnbufferedInput=true&useReadAheadInput=false";

	public MySQL() {
		getConnection(null);
	}

	public MySQL(String cstr) {
		getConnection(cstr);
	}

	public static MySQL getMySQL() {
		return getMySQL(null);
	}

	public static MySQL getMySQL(String cstr) {
		if (mySQL == null) {
			mySQL = new MySQL(cstr);
			mySQL.readHighestIDs();
			DBWordPattern.initialize();
			// mySQL.initializeTermInfoHash();
		}
		return mySQL;
	}

	public static Vector<Sentence> getSentencesMatchingQuery(String qstr) {
		return MySQL.getMySQL().getSentencesMatchingQuery(qstr, false, false);
	}

	public Vector<Sentence> getSentencesMatchingQuery(String qstr,
			boolean permitPartial, boolean removeDuplicates) {
		Document qdoc = new Document(qstr.toLowerCase());
		qdoc.analyzeSentencesNoHeader();
		Sentence qsent = qdoc.getAllSentences().firstElement();
		MySQL mySQL = MySQL.getMySQL();
		Vector<DocumentItemInfoWrapper> wrappers = mySQL
				.getDocumentItemsMatchingQuery(qsent, false, permitPartial,
						removeDuplicates);
		return DocumentItemInfoWrapper.getSentences(wrappers);
	}

	public static Vector<Document> getDocumentsMatchingQuery(String qstr) {
		return MySQL.getMySQL().getDocumentsMatchingQuery(qstr, false);
	}

	public Vector<Document> getDocumentsMatchingQuery(String qstr,
			boolean permitPartial) {
		Document qdoc = new Document(qstr);
		qdoc.analyzeSentencesNoHeader();
		Sentence qsent = qdoc.getAllSentences().firstElement();
		MySQL mySQL = MySQL.getMySQL();
		Vector<DocumentItemInfoWrapper> wrappers = mySQL
				.getDocumentItemsMatchingQuery(qsent, true, permitPartial,
						false);
		return DocumentItemInfoWrapper.getDocuments(wrappers);
	}

	public Vector<DocumentItemInfoWrapper> getDocumentItemsMatchingQuery(
			tsl.documentanalysis.document.Sentence query, boolean isDocument,
			boolean permitPartial, boolean removeDuplicates) {
		Hashtable<String, double[]> diWeightHash = new Hashtable();
		boolean[] isCUI = null;
		int numterms = 0;
		Vector<DocumentItemInfoWrapper> diWrappers = null;
		double[] termWeights = null;
		Vector<DBTermIndex> queryTermIndexes = DBTermIndex
				.extractDBTermIndexesFromSentence(query, true);
		if (queryTermIndexes == null) {
			return null;
		}
		Vector<String> queryTerms = DBTermIndex.getTerms(queryTermIndexes);
		isCUI = new boolean[queryTerms.size()];
		for (int i = 0; i < queryTerms.size(); i++) {
			if (CUIStructureShort.isCUI(queryTerms.elementAt(i))) {
				isCUI[i] = true;
			}
		}
		Vector<Vector<DBTermIndex>> termIndexVectors = DBTermIndex
				.getDBTermIndexes(queryTerms);
		if (termIndexVectors == null) {
			return null;
		}
		UMLSStructuresShort umss = UMLSStructuresShort.getUMLSStructures();
		queryTerms = DBTermIndex.getFirstTerms(termIndexVectors);
		numterms = queryTerms.size();
		Collections.sort(termIndexVectors, new VUtils.InverseLengthSorter());
		Vector<DBTermIndex> firstTermIndexVector = termIndexVectors
				.firstElement();
		double smallestTermCount = firstTermIndexVector.size();
		termWeights = new double[termIndexVectors.size()];
		Vector<String> sortedQueryTerms = null;
		for (int i = 0; i < termIndexVectors.size(); i++) {
			Vector<DBTermIndex> termIndexVector = termIndexVectors.elementAt(i);
			DBTermIndex ti = termIndexVector.firstElement();
			double weight = 0d;
			if (isCUI[i] || ti.getTerm().contains("=")) {
				weight = 1d;
			} else {
				weight = smallestTermCount / termIndexVector.size();
			}
			termWeights[i] = weight;
			sortedQueryTerms = VUtils.add(sortedQueryTerms, ti.getTerm());
		}

		int numDocumentItems = 0;
		for (int i = 0; i < termIndexVectors.size(); i++) {
			Vector<DBTermIndex> termIndexVector = termIndexVectors.elementAt(i);
			boolean isRelevant = (i == 0 || isCUI[i]);
			if (!isRelevant) {
				DBTermIndex fti = termIndexVector.firstElement();
				String term = fti.getTerm();
				CUIStructureShort css = umss.getCUIStructure(term);
				if (css != null) {
					UMLSTypeConstant utype = (UMLSTypeConstant) css.getType();
					if (utype != null && utype.isRelevantUMLSCondition()) {
						isRelevant = true;
					}
				}
			}
			for (DBTermIndex ti : termIndexVector) {
				if (isRelevant) {
					String sdi = (isDocument ? String.valueOf(ti
							.getDocumentID()) : ti.getSDIndex());
					if (diWeightHash.get(sdi) == null) {
						double[] diweights = new double[numterms];
						diWeightHash.put(sdi, diweights);
						numDocumentItems++;
					}
				}
			}
		}

		System.out.println("Number of documents/sentences used in query: "
				+ numDocumentItems);

		for (int i = 0; i < termIndexVectors.size(); i++) {
			Vector<DBTermIndex> termIndexVector = termIndexVectors.elementAt(i);
			for (DBTermIndex ti : termIndexVector) {
				String sdi = (isDocument ? String.valueOf(ti.getDocumentID())
						: ti.getSDIndex());
				double diweights[] = diWeightHash.get(sdi);
				if (diweights != null) {
					double tweight = termWeights[i];
					diweights[i] = tweight;
				}
			}
		}
		Hashtable<String, DocumentItemInfoWrapper> swhash = new Hashtable();
		for (Enumeration<String> e = diWeightHash.keys(); e.hasMoreElements();) {
			String sdi = e.nextElement();
			DocumentItemConstant di = null;
			if (isDocument) {
				int id = Integer.valueOf(sdi).intValue();
				di = DBDocument.getDocument(id);
			} else {
				di = DBDocument.getSentenceFromSDI(sdi);
			}
			double[] diweights = diWeightHash.get(sdi);
			boolean isValid = true;
			if (!permitPartial) {
				for (int i = 0; i < termIndexVectors.size(); i++) {
					if (diweights[i] == 0) {
						isValid = false;
						break;
					}
				}
			}
			if (isValid) {
				DocumentItemInfoWrapper sw = new DocumentItemInfoWrapper(di,
						diweights, termWeights, sortedQueryTerms);
				boolean isDuplicate = false;
				if (removeDuplicates
						&& swhash.get(sw.getDocumentItem().getText()) != null) {
					isDuplicate = true;
				}
				if (!isDuplicate && sw.getScore() > 0.5d) {
					diWrappers = VUtils.add(diWrappers, sw);
					if (removeDuplicates) {
						swhash.put(sw.getDocumentItem().getText(), sw);
					}
				}
			}
		}
		if (diWrappers != null) {
			Collections.sort(diWrappers,
					new DocumentItemInfoWrapper.ScoreSorter());
		}
		System.out.println("SortedQueryTerms=" + diWrappers);
		return diWrappers;
	}

	public int readHighestIDs() {
		int highest = 0;
		try {
			Connection c = MySQL.getMySQL().getConnection(null);
			String sql = "select COUNT(term) from TERMINDEXES";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				this.highestTermIndexID = rs.getInt(1);
			}
			sql = "select COUNT(id) from DOCUMENTS";
			ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.first()) {
				this.highestDocumentID = rs.getInt(1);
			}
			sql = "select COUNT(id) from TERMINSTANCES";
			ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.first()) {
				this.highestTermInstanceID = rs.getInt(1);
			}
			sql = "select COUNT(id) from SENTENCEINSTANCES";
			ps = (com.mysql.jdbc.PreparedStatement) c.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.first()) {
				this.highestSentenceInstanceID = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return highest;
	}

	public int getAndIncrementHighestDocumentID() {
		return this.highestDocumentID++;
	}

	public int getAndIncrementHighestTermIndexID() {
		return highestTermIndexID++;
	}

	public int getAndIncrementHighestTermInstanceID() {
		return highestTermInstanceID++;
	}

	public int getAndIncrementHighestSentenceInstanceID() {
		return highestSentenceInstanceID++;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public Connection getConnection(String cstr) {
		try {
			if (this.connection != null) {
				return this.connection;
			}
			Class driverClass = Class.forName("com.mysql.jdbc.Driver");
			driverClass.newInstance();
			if (cstr == null) {
				cstr = connectionString;
			}

			Properties props = new Properties();
			String istr = String.valueOf(1024 * 1024 * 256);
			props.setProperty("maxAllowedPacket", istr);

			this.connection = (Connection) DriverManager.getConnection(cstr,
					props);

			System.out.println("Connection succeeded...");
			return connection;
		} catch (Exception ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		return null;
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
		}
		connection = null;
	}

	public Connection getUMLSConnection() {
		return this.umlsConnection;
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public Hashtable<String, Document> getDocumentHash() {
		return documentHash;
	}

	public DBWordPattern getDBWordPattern(String str) {
		return this.dbWordPatternHash.get(str);
	}

	public void addDBWordPattern(String str, DBWordPattern pattern) {
		this.dbWordPatternHash.put(str, pattern);
	}

	public void initializeTermInfoHash() {
		this.termInfoHash.clear();
		Vector<DBTermInfo> termInfos = DBTermInfo.gatherAllTermInfos();
		if (termInfos != null) {
			for (DBTermInfo ti : termInfos) {
				this.termInfoHash.put(ti.getTerm(), ti);
			}
		}
	}

	public void storeExpressionInstanceHash(DBExpressionInstance ei) {
		this.expressionInstanceHash.put(ei.getId(), ei);
	}

	public DBExpressionInstance getExpressionInstanceHash(int id) {
		return this.expressionInstanceHash.get(new Integer(id));
	}

	public Hashtable<String, Analysis> getAnalysisHash() {
		return analysisHash;
	}

}
