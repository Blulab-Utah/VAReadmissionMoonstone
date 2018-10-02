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
import java.util.Hashtable;
import java.util.Vector;

import tsl.dbaccess.mysql.MySQL;
import tsl.utilities.StrUtils;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

public class RequiredCUIs {

	static String[] cuis = {
			"C0003123",
			"C0003862",
			"C0235592",
			"C0008031",
			"C0085593",
			"C0009763",
			"C0010200",
			"COO10520",
			"C0011991",
			"C0013404",
			"C0015672",
			"C0015967",
			"BC0021400",
			"C0018681",
			"C0019079",
			"C0019825",
			"C0521839",
			"LC0021400",
			"C0420679",
			"C0231218",
			"C0231528", // Myalgia is anthrax term
			"C0027497",
			"C0239430",
			"C0085636",
			"C0032285",
			"C1260880",
			"C0242429",
			"IC0021400",
			"C0521026",
			"C0042740",
			"C0043144",
			"C0277794",
			"C0235710",
			"C0009443",
			"C0027429",
			"C0850149",
			"C0004093",
			"C0497156",
			"C0034642",

			// Mike's shigellosis terms -- cf.
			// topaz/trunk/Gate-3-1/Taggers/Biosurveillance_Lists/shig.jape
			"C0015967", // Also an influenza term
			"C0085593", // chill vs. chills?
			"C0000729",
			"C0239182",
			"C0151594",
			"C0018681", // Also an influenza term
			"C0232726",
			"C0426636",
			"C0011991",
			"C1321898",
			"C0009951",
			"C0004610",
			"C0009763", // Also an influenza term
			"C0231218", // Also an influenza term
			"C0232498",

			// Mike's inh_anthrax terms -- cf.
			// topaz/trunk/Gate-3-1/Taggers/Biosurveillance_Lists/inhanthrax.jape
			"C0015967", // Also a shigellosis term
			"C0085593", // Also a shigellosis term
			"C0028081",
			"C0521839", // Also an influenza term
			"C0850149",
			"C0008031",
			"C0013404",
			"C0015672",
			"C0231528",
			"C0242429",
			"C0011168", // Dysphagia
			"C0497156",
			"C0018681", // Also an influenza term
			"C0027497",
			"C1971624",
			"C0235295", // Abdominal distress
			"C0042963",
			"C0011991",
			"C0746459",
			"C0085498",
			"C0917801",
			"C2029900",
			"C0020649",
			"C0019079",
			"C1260880",
			"C0038450",
			"C0010520",
			"C0009676",
			"C0025289",
			"C0013604",

			// Mike's output.jape terms (many redundant with what's gone before)
			"C0235295", "C0000729", "C0232498", "C0003123", "C0003862",
			"C0004093", "C0004610", "C0004611", "C0558348", "C0497156",
			"C0235592", "C0008031", "C0085593", "C0009319", "C0009676",
			"C0009763", "C0009951", "C0009443", "C0010200", "C0850149",
			"C0010520", "C0011175", "C0237849", "C0011991", "C0151594",
			"C0011991", "C0239182", "C0011168", "C0013404", "C0014038",
			"C0497156", "C0015672", "C0015967", "C0016479", "C0017160",
			"C0085498", "C0018681", "C1321898", "C0201811", "C0019079",
			"C0019825", "C0521839", "C0021400", "C0917801", "C0221200",
			"C0021400", "C0036953", "C1971624", "C0423791", "C0231218",
			"C0025289", "C0231528", "C0027429", "C0420679", "C0027497",
			"C0028081", "C0239430", "C0151205", "C0085636", "C0032285",
			"C0277794", "C0034642", "C0267596", "C0426636", "C1260880",
			"C0036082", "C0074447", "C0013371", "C0020649", "C0242429",
			"C0202010", "C0038450", "C0013604", "C0235710", "C0021400",
			"C0013371", "C0039070", "C2029900", "C0232726", "C0521026",
			"C0042740", "C0042963", "C1883552", "C0043144", "C0746459" };

	public static Hashtable requiredCUIHash = null;
	public static Hashtable requiredWordHash = null;

	static void initialize() {
		if (requiredCUIHash == null) {
			requiredCUIHash = new Hashtable();
			requiredWordHash = new Hashtable();
			for (int i = 0; i < cuis.length; i++) {
				String cui = cuis[i].toLowerCase();
				requiredCUIHash.put(cui, cui);
			}
			readMySQL();
		}
	}

	public static boolean isRequiredCUI(String cui) {
		initialize();
		cui = cui.toLowerCase();
		return requiredCUIHash.get(cui) != null;
	}

	public static boolean isRequiredWord(String word) {
		initialize();
		word = word.toLowerCase();
		boolean isRequired = requiredWordHash.get(word) != null;
		return isRequired;
	}

	public static void readMySQL() {
		try {
			Connection connection = MySQL.getMySQL().getUMLSConnection();
			if (connection != null) {
				initialize();
				StringBuffer sb = new StringBuffer();
				for (Enumeration e = requiredCUIHash.keys(); e
						.hasMoreElements();) {
					String cui = (String) e.nextElement();
					sb.append("\'");
					sb.append(cui);
					sb.append("\'");
					if (e.hasMoreElements()) {
						sb.append(",");
					}
				}
				String sql = "select nstr from mrxns_eng where cui in ("
						+ sb.toString() + ")";
				sql = sql.toUpperCase();
				PreparedStatement ps = (com.mysql.jdbc.PreparedStatement) connection
						.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				for (boolean validrow = rs.first(); validrow; validrow = rs
						.next()) {
					String nstr = rs.getString(1).toLowerCase();
					Vector v = StrUtils.stringList(nstr, ',');
					for (Enumeration e = v.elements(); e.hasMoreElements();) {
						String str = (String) e.nextElement();
						requiredWordHash.put(str, str);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
