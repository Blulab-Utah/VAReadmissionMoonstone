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
package tsl.documentanalysis.tokenizer.regexpr;

import java.util.Vector;
import java.util.regex.Pattern;

import tsl.expression.term.type.TypeConstant;
import tsl.utilities.StrUtils;

public class RegExprPacket {

	private String name = null;
	private TypeConstant type = null;
	private String normalizedValue = null;
	private String javaPattern = null;
	private String description = null;
	private Pattern pattern = null;
	private static String delim = "\\$\\$";

	public RegExprPacket(RegExprManager rmgr, Vector<String> subs) {
		this.name = subs.elementAt(0).trim();
		this.description = subs.elementAt(1).trim();
		String tstr = subs.elementAt(2).trim();
		this.type = TypeConstant.createTypeConstant(tstr);
		this.normalizedValue = subs.elementAt(3).trim();
		this.javaPattern = subs.elementAt(4).trim();
		String pstr = subs.elementAt(5).trim();
		this.pattern = Pattern.compile(pstr.trim(), Pattern.CASE_INSENSITIVE);
	}

	public static RegExprPacket createRegExprPacket(RegExprManager rmgr,
			Vector<String> rtypes, String line) {
		if (line != null) {
			Vector<String> subs = StrUtils.stringList(line, delim);
			if (subs != null && subs.size() == 6
					&& (rtypes == null || rtypes.contains(subs.elementAt(2)))) {
				return new RegExprPacket(rmgr, subs);
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public TypeConstant getType() {
		return type;
	}

	public String getNormalizedValue() {
		return normalizedValue;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public String getJavaPattern() {
		return javaPattern;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		String str = "<" + this.name + ":" + this.type + ":" + this.description
				+ ">";
		return str;
	}

}
