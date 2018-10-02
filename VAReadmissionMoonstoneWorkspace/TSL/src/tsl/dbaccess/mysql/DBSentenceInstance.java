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

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.ComplexSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

/*
 * 
 * DO:   When I get the DBSentenceInstance from the id, I get the sentence string 
 * and list of term ids.  I need to create DBTermInstances from those ids, and 
 * construct a sentence from the string, containing the terms from those 
 * TermInstances.  When I want to create a DBSentenceInstance from a Sentence, 
 * I need to construct the string, create a list of TermInstances, create a string 
 * for the termInstances, and store the strings to the DB.
 */

public class DBSentenceInstance extends DBExpressionInstance {
	private Vector<DBTermInstance> termInstances = null;
	private Sentence sentence = null;
	private String sentenceString = null;

	public DBSentenceInstance(int id) {
		if (id == -1) {
			id = MySQL.getMySQL().getAndIncrementHighestSentenceInstanceID();
		}
		this.setId(id);
	}

	public DBSentenceInstance(Sentence sentence, int id) {
		this.sentence = sentence;
		if (id == -1) {
			id = MySQL.getMySQL().getAndIncrementHighestSentenceInstanceID();
		}
		this.setId(id);
		this.getTermInstances();
		this.getSentenceString();
	}

	// Reconstruct a SentenceInstance from the DB. The SI will contain a list of
	// TermInstances and a sentence string.
	public static DBSentenceInstance getSentenceInstance(int id) {
		DBSentenceInstance si = (DBSentenceInstance) MySQL.getMySQL()
				.getExpressionInstanceHash(id);
		if (si != null) {
			return si;
		}
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select terminstanceids, sentencestring from SENTENCEINSTANCES where id = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				si = new DBSentenceInstance(id);
				String tidstr = rs.getString(1);
				String[] tidstrs = tidstr.split(",");
				Vector<DBTermInstance> tis = null;
				for (String ts : tidstrs) {
					int tid = Integer.valueOf(ts).intValue();
					DBTermInstance ti = DBTermInstance.getDBTermInstance(tid);
					tis = VUtils.add(tis, ti);
				}
				si.termInstances = tis;
				si.sentenceString = rs.getString(2);
				si.getSentence();
				MySQL.getMySQL().storeExpressionInstanceHash(si);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return si;
	}

	public void store() {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String tistr = "";
			for (Enumeration<DBTermInstance> e = this.termInstances.elements(); e
					.hasMoreElements();) {
				DBTermInstance ti = e.nextElement();
				ti.store();
				tistr += ti.getId();
				if (e.hasMoreElements()) {
					tistr += ",";
				}
			}
			String sql = "insert into SENTENCEINSTANCES (id, terminstanceids, sentencestring) "
					+ "values(?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setInt(1, this.getId());
			ps.setString(2, tistr);
			ps.setString(3, this.sentenceString);
			ps.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getSentenceString() {
		if (this.sentenceString == null && this.sentence != null) {
			this.sentenceString = getSentenceString(this, this.sentence);
		}
		return this.sentenceString;
	}

	private static String getSentenceString(DBSentenceInstance csi,
			Sentence sentence) {
		String str = "";
		if (sentence instanceof ComplexSentence) {
			ComplexSentence cs = (ComplexSentence) sentence;
			str += (cs instanceof AndSentence ? "(and " : "(or ");
			for (Enumeration<Sentence> e = cs.getSentences().elements(); e
					.hasMoreElements();) {
				Sentence sub = e.nextElement();
				str += getSentenceString(csi, sub);
				if (e.hasMoreElements()) {
					str += " ";
				}
			}
			str += ") ";
		} else if (sentence instanceof NotSentence) {
			NotSentence ns = (NotSentence) sentence;
			str += "(not " + getSentenceString(csi, ns.getSentence()) + ") ";
		} else if (sentence instanceof RelationSentence) {
			RelationSentence rs = (RelationSentence) sentence;
			str += "(" + rs.getRelation().getUniqueID() + " ";
			for (int i = 0; i < rs.getTermCount(); i++) {
				Term term = (Term) rs.getTerm(i);
				DBTermInstance ti = new DBTermInstance(term, -1);
				str += ti.getId();
				if (i < rs.getTermCount() - 1) {
					str += " ";
				}
			}
			str += ") ";
		}
		return str;
	}

	// Construct a Sentence from a SentenceInstance sentenceString + list of
	// TermInstances
	public Sentence getSentence() {
		if (this.sentence == null && this.sentenceString != null) {
			try {
				String str = "'" + this.sentenceString;
				Sexp s = (Sexp) TLisp.getTLisp().evalString(str);
				Vector v = TLUtils.convertSexpToJVector(s);
				this.sentence = (Sentence) reconstructSentence(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.sentence;
	}

	private Sentence reconstructSentence(Vector v) {
		Sentence sentence = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (AndSentence.isAndSentence(v)) {
			AndSentence as = new AndSentence();
			for (int i = 1; i < v.size(); i++) {
				Vector subv = (Vector) v.elementAt(i);
				Sentence sub = reconstructSentence(subv);
				as.addSentence(sub);
			}
			sentence = as;
		} else if (OrSentence.isOrSentence(v)) {
			OrSentence os = new OrSentence();
			for (int i = 1; i < v.size(); i++) {
				Vector subv = (Vector) v.elementAt(i);
				Sentence sub = reconstructSentence(subv);
				os.addSentence(sub);
			}
			sentence = os;
		} else if (NotSentence.isNotSentence(v)) {
			NotSentence ns = new NotSentence();
			Sentence sub = reconstructSentence((Vector) v.elementAt(1));
			ns.setSentence(sub);
			sentence = ns;
		} else if (RelationSentence.isRelationSentence(v)) {
			String rname = (String) v.firstElement();
			Vector<Term> arguments = null;
			for (int i = 1; i < v.size(); i++) {
				String tstr = (String) v.elementAt(i);
				DBTermInstance ti = this.getTermInstance(tstr);
				if (ti != null) {
					arguments = VUtils.add(arguments, ti.getTerm());
				}
			}
			RelationSentence rs = RelationSentence.createRelationSentence(
					rname, arguments);
			sentence = rs;
		}
		return sentence;
	}

	// Given a Sentence, create the list of termInstances.
	private Vector<DBTermInstance> getTermInstances() {
		if (this.termInstances == null && this.sentence != null) {
			Hashtable<String, DBTermInstance> termhash = new Hashtable();
			Vector<RelationSentence> relsents = this.sentence
					.gatherRelationalSentences();
			if (relsents != null) {
				for (RelationSentence rs : relsents) {
					for (Object o : rs.getTerms()) {
						Term term = (Term) o;
						DBTermInstance ti = new DBTermInstance(term, -1);
						termhash.put(term.getName(), ti);
					}
				}
				this.termInstances = HUtils.getElements(termhash);
			}
		}
		return this.termInstances;
	}

	private DBTermInstance getTermInstance(String id) {
		if (this.termInstances != null) {
			for (DBTermInstance ti : this.termInstances) {
				if (id.equals(ti.getId())) {
					return ti;
				}
			}
		}
		return null;
	}

}
