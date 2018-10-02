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

import tsl.expression.term.Term;
import tsl.expression.term.template.Template;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class DBTermInstance extends DBExpressionInstance {
	private Term term = null;
	private Vector<Variable> parameters = null;

	public DBTermInstance(Term term, int id) {
		this.term = term;
		if (id == -1) {
			id = MySQL.getMySQL().getAndIncrementHighestTermInstanceID();
		}
		this.setId(id);
		MySQL.getMySQL().storeExpressionInstanceHash(this);
	}

	public static DBTermInstance getDBTermInstance(int id) {
		DBTermInstance ti = null;
		ti = (DBTermInstance) MySQL.getMySQL().getExpressionInstanceHash(id);
		if (ti != null) {
			return ti;
		}
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String sql = "select label, type, attributes, cui, concept, parameters from TERMINSTANCES where id = ?";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.first()) {
				String label = rs.getString(1);
				String tstr = rs.getString(2);
				String astr = rs.getString(3);
				String cuistr = rs.getString(4);
				String conceptstr = rs.getString(5);
				String pstr = rs.getString(6);
				Vector<Variable> tiparams = getTermInstanceParameters(pstr);
				Vector<Variable> tparams = getTermParameters(tiparams);
				TypeConstant type = null;
				if (tstr != null) {
					type = kb.getTypeConstant(tstr);
				}
				Term term = (tparams != null ? new Template(label) : new Term(
						label));
				term.setType(type);
				term.setVariables(tparams);
				if (astr != null) {
					String[] strs = astr.split("=");
					for (int i = 0; i < strs.length; i += 2) {
						String as = strs[i];
						String vs = strs[i + 1];
						term.setProperty(as, vs);
					}
				}
				term.setCui(cuistr);
				term.setConcept(conceptstr);
				ti = new DBTermInstance(term, id);
				ti.parameters = tiparams;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ti;
	}

	public void store() {
		try {
			Connection c = MySQL.getMySQL().getConnection();
			String astr = null;
			if (this.term.hasProperties()) {
				astr = "";
				for (Enumeration<String> e = this.term.getPropertyNames()
						.elements(); e.hasMoreElements();) {
					String aname = e.nextElement();
					Object o = this.term.getProperty(aname);
					astr += aname + "=" + o.toString();
					if (e.hasMoreElements()) {
						astr += ",";
					}
				}
			}
			String stype = (this.term.getType() != null ? this.term.getType()
					.getName() : null);
			int otype = this.term.getOntologicalType();
			String paramstr = this.getParameterString();
			String sql = "insert into TERMINSTANCES (id, label, stype, otype, attributes, cui, concept, parameters) "
					+ "values(?, ?, ?, ?, ?, ?, ?, ?)";
			com.mysql.jdbc.PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) c
					.prepareStatement(sql);
			ps.setInt(1, this.getId());
			ps.setString(2, this.term.getName());
			ps.setString(3, stype);
			ps.setInt(4, otype);
			ps.setString(5, astr);
			ps.setString(6, term.getCUI());
			ps.setString(7, term.getConcept().toString());
			ps.setString(8, paramstr);
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getParameterString() {
		String str = null;
		if (this.parameters != null) {
			for (Enumeration<Variable> e = this.parameters.elements(); e
					.hasMoreElements();) {
				Variable var = e.nextElement();
				str += var.getName() + "=" + var.getValue().toString();
				if (e.hasMoreElements()) {
					str += ",";
				}
			}
		}
		return str;
	}

	public static Vector<Variable> getTermInstanceParameters(String str) {
		Vector<Variable> tparams = null;
		if (str != null) {
			String[] sstrs = str.split(",");
			for (int i = 0; i < sstrs.length; i++) {
				String[] avstrs = sstrs[i].split("=");
				String aname = avstrs[0];
				String tidstr = avstrs[1];
				int tid = Integer.valueOf(tidstr).intValue();
				DBTermInstance ti = DBTermInstance.getDBTermInstance(tid);
				Variable var = new Variable(aname, ti);
				tparams = VUtils.add(tparams, var);
			}
		}
		return tparams;
	}

	public static Vector<Variable> getTermParameters(Vector<Variable> tiparams) {
		Vector<Variable> params = null;
		if (tiparams != null) {
			for (Variable tivar : tiparams) {
				DBTermInstance ti = (DBTermInstance) tivar.getValue();
				Variable tvar = new Variable(tivar.getName(), ti.getTerm());
				params = VUtils.add(params, tvar);
			}
		}
		return params;
	}

	public Term getTerm() {
		return term;
	}

}
