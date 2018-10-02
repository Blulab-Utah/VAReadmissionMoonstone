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
package tsl.knowledge.ontology.umls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import tsl.dbaccess.mysql.MySQL;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.VUtils;

import com.mysql.jdbc.PreparedStatement;

public class UMLSTypeInfo {
	private boolean isRelation = false;

	private String UI = "unspecified";

	private String name = "unspecified";

	private Term term = null;

	private String description = null;

	public UMLSTypeInfo(TypeConstant type) {
		this.term = type;
		this.name = type.getName();
		((TypeConstant) this.term).setTypeInfo(this);
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		uss.getTypeInfoUIHash().put(this.name, this);
	}

	public UMLSTypeInfo(String rt, String tui, String name, String description) {
		this(rt, tui, name, description, null);
	}

	public UMLSTypeInfo(String rt, String tui, String name, String description,
			String[] pnames) {
		this.isRelation = rt.equals("RL");
		this.UI = tui.toLowerCase();
		this.name = name.toLowerCase();
		this.description = description;
		if (this.isRelation) {
			this.term = RelationConstant.createRelationConstant(this.name);
		} else {
			this.term = UMLSTypeConstant.createUMLSTypeConstant(this);
		}
		if (this.term instanceof UMLSTypeConstant) {
			UMLSTypeConstant utc = (UMLSTypeConstant) this.term;
			utc.setTypeInfo(this);
			if (pnames != null) {
				for (String pname : pnames) {
					TypeConstant parent = (TypeConstant) TypeConstant
							.createTypeConstant(pname);
					utc.addParent(parent);
					utc.addUnifier(utc);
				}
			} else {
				TypeConstant parent = null;
				if (utc.isCondition()) {
					parent = TypeConstant
							.createTypeConstant("medical_condition");
				} else if (utc.isLocation()) {
					parent = TypeConstant
							.createTypeConstant("medical_location");
				} else if (UMLSTypeConstant.isRelevant(tui)) {
					parent = TypeConstant.createTypeConstant("medical_thing");
				}
				if (parent != null) {
					utc.addParent(parent);
					utc.addUnifier(utc);
				}
			}
		}
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		uss.getTypeInfoUIHash().put(this.UI, this);
		uss.getTypeInfoUIHash().put(this.name, this);
	}

	public static UMLSTypeInfo create(TypeConstant type) {
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		UMLSTypeInfo ti = (UMLSTypeInfo) uss.getTypeInfoUIHash().get(
				type.getName());
		if (ti == null) {
			ti = new UMLSTypeInfo(type);
		}
		return ti;
	}

	public TypeConstant getType() {
		return (TypeConstant) this.term;
	}

	public static Vector getTypeConstants() {
		Vector typeConstants = null;
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		for (Enumeration e = uss.getTypeInfoUIHash().elements(); e
				.hasMoreElements();) {
			UMLSTypeInfo ti = (UMLSTypeInfo) e.nextElement();
			if (!ti.isRelation) {
				typeConstants = VUtils.add(typeConstants, ti.term);
			}
		}
		return typeConstants;
	}

	public String toString() {
		String str = "<UMLSTypeInfo:Relation=" + isRelation + ",UI=" + this.UI
				+ ",name=" + this.name + ",Description=\"" + this.description
				+ "\">";
		return str;
	}

	public String toLispString() {
		String str = "(\"" + this.getUI() + "\" \"" + this.getName() + "\")";
		return str;
	}

	public static UMLSTypeInfo findByUI(String ui) {
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		if (uss.getTypeInfoUIHash() != null && ui != null) {
			return (UMLSTypeInfo) uss.getTypeInfoUIHash().get(ui);
		}
		return null;
	}

	public static UMLSTypeInfo findByName(String name) {
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		if (uss.getTypeInfoUIHash() != null && name != null) {
			return (UMLSTypeInfo) uss.getTypeInfoUIHash().get(
					name.toLowerCase());
		}
		return null;
	}

	public static void readMySQL() {
		try {
			if (MySQL.getMySQL().getUMLSConnection() != null) {
				String sql = "select RT, UI, STY_RL, DEF from SRDEF";
				sql = sql.toLowerCase();
				PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) MySQL
						.getMySQL().getUMLSConnection().prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				for (boolean validrow = rs.first(); validrow; validrow = rs
						.next()) {
					String rt = rs.getString(1);
					String ui = rs.getString(2);
					String styrl = rs.getString(3);
					String def = rs.getString(4);
					new UMLSTypeInfo(rt, ui, styrl, def);
				}
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isRelation() {
		return isRelation;
	}

	public void setRelation(boolean isRelation) {
		this.isRelation = isRelation;
	}

	public String getUI() {
		return UI;
	}

	public void setUI(String uI) {
		UI = uI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static Vector<UMLSTypeInfo> getAllTypeInfos() {
		Vector<UMLSTypeInfo> v = new Vector(0);
		UMLSStructuresShort uss = UMLSStructuresShort.getUMLSStructures();
		for (Enumeration e = uss.getTypeInfoUIHash().elements(); e
				.hasMoreElements();) {
			UMLSTypeInfo ti = (UMLSTypeInfo) e.nextElement();
			v.add(ti);
		}
		return v;
	}

}
