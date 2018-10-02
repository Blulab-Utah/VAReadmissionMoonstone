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
package moonstone.rule;

import java.util.Vector;

import moonstone.grammar.Grammar;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class SimplifiedRuleFormat {


	public static void readSimplifiedRuleFile(Grammar grammar, String fname) {
		Sexp sexp = (Sexp) TLisp.getTLisp().loadFile(fname);
		Vector<String> rstrings = TLUtils.convertSexpToJVector(sexp);
		for (String rstr : rstrings) {
			Rule rule = createRuleFromString(grammar, rstr);
		}
	}

	public static Rule createRuleFromString(Grammar grammar, String rstr) {
		Vector pattern = createRulePattern(rstr);
		Vector<Vector<String>> wlists = (Vector<Vector<String>>) VUtils
				.assocValueTopLevel("words", pattern);
		Rule rule = new Rule(grammar, null, pattern, wlists);
		return rule;
	}

	public static String convertRuleToString(Rule rule) {
		String rstr = "";
		for (int i = 0; i < rule.getPattern().size(); i++) {
			Vector sv = (Vector) rule.getPattern().elementAt(i);
			String pname = (String) sv.firstElement();
			String pstr = null;
			if ("words".equals(pname)) {
				String wstr = getWordListString(VUtils.rest(sv));
				pstr = "words=" + wstr;
			} else if ("properties".equals(pname)) {
				String pstrs = getPropertiesString(VUtils.rest(sv));
				pstr = "properties=" + pstrs;
			} else if (sv.elementAt(1) instanceof String) {
				pstr = pname + "=" + sv.elementAt(1);
			}
			if (pstr != null) {
				rstr += pstr;
				if (i < rule.getPattern().size() - 1) {
					rstr += "&";
				}
			}
		}
		return rstr;
	}

	public static Vector createRulePattern(String rstr) {
		Vector pattern = null;
		Vector entry = null;
		String[] svs = rstr.split("&");
		for (int i = 0; i < svs.length; i++) {
			String[] avpair = svs[i].split("=");
			String aname = avpair[0];
			String vstr = avpair[1];
			if ("ruleid".equals(aname)) {
				entry = VUtils.listify("ruleid", vstr);
			} else if ("words".equals(aname)) {
				Vector<Vector<String>> wlists = getWordLists(vstr);
				entry = VUtils.listify("words", wlists);
			} else if ("properties".equals(aname)) {
				entry = getProperties(vstr);
			} else {
				entry = VUtils.listify(aname, vstr);
			}
			pattern = VUtils.add(pattern, entry);
		}
		return pattern;
	}

	private static Vector<Vector<String>> getProperties(String pstr) {
		Vector<Vector<String>> plists = null;
		String[] sstr = pstr.split(";");
		for (int i = 0; i < sstr.length; i++) {
			String[] avpair = sstr[i].split(",");
			Vector<String> plist = VUtils.listify("\"" + avpair[0] + "\"", "\""
					+ avpair[1] + "\"");
			plists = VUtils.add(plists, plist);
		}
		return plists;
	}

	public static Vector<Vector<String>> getWordLists(String wstr) {
		Vector<Vector<String>> wlists = null;
		String[] sstr = wstr.split(";");
		for (int i = 0; i < sstr.length; i++) {
			String[] words = sstr[i].split(",");
			wlists = VUtils.add(wlists, VUtils.arrayToVector(words));
		}
		return wlists;
	}

	private static String getWordListString(Vector<Vector<String>> v) {
		String wstr = null;
		if (v != null) {
			wstr = "";
			for (int i = 0; i < v.size(); i++) {
				Vector<String> sv = v.elementAt(i);
				String str = StrUtils.stringListConcat(sv, ",");
				if (i < v.size() - 1) {
					str += ";";
				}
				wstr += str;
			}
		}
		return wstr;
	}

	private static String getPropertiesString(Vector<Vector<String>> v) {
		String pstrs = null;
		if (v != null) {
			pstrs = "";
			for (int i = 0; i < v.size(); i++) {
				Vector<String> sv = v.elementAt(i);
				String pstr = sv.firstElement() + "," + sv.lastElement();
				if (i < v.size() - 1) {
					pstr += ";";
				}
				pstrs += pstr;
			}
		}
		return pstrs;
	}

}
