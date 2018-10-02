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
package tsl.expression.term.constant;

import java.util.Vector;

import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.StrUtils;



public class StringConstant extends Constant {
	private StringConstant parent = null;
	private boolean isComplex = false;
	
	public StringConstant(String label, boolean iscomplex) {
		super(label);
		this.isComplex = iscomplex;
	}

	public StringConstant(String label, TypeConstant type, boolean iscomplex) {
		super(label, type);
		this.isComplex = iscomplex;
	}

	// e.g. (defstringconstant x y z type), (defstringconstant x), ...
	// Problem: Returns just the first constant.
	public static StringConstant createStringConstant(Vector v) {
		StringConstant sc = null;
		if (v != null && "defstringconstant".equals(v.firstElement())
				&& v.size() > 2) {
			String tname = (String) v.lastElement();
			TypeConstant type = KnowledgeBase.getCurrentKnowledgeBase()
					.getTypeConstant(tname);
			int end = (type == null ? v.size() - 1 : v.size() - 2);
			for (int i = 1; i <= end; i++) {
				String label = (String) v.elementAt(i);
				sc = new StringConstant(label, type, false);
			}
		}
		return sc;
	}
	
	public static StringConstant createStringConstant(String name, TypeConstant type, boolean iscomplex) {
		KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getCurrentKnowledgeBase();
		StringConstant sc = kb.getNameSpace().getStringConstant(name);
		if (sc == null) {
			sc = new StringConstant(name, type, iscomplex);
		}
		return sc;
	}
	
	public String toString() {
		return this.name;
	}

	public boolean equals(Object o) {
		return o != null && this.toString().equals(o.toString());
	}

	// e.g. ":COMPULSIVE_GAMBLING:"
	public static boolean isStringConstantFormalName(String str) {
		if (str != null && str.length() > 2 && str.charAt(0) == ':'
				&& str.charAt(str.length() - 1) == ':'
				&& Character.isUpperCase(str.charAt(1))) {
			for (int i = 2; i < str.length() - 1; i++) {
				char c = str.charAt(i);
				if (!(Character.isUpperCase(c) || c == '_' || c == '-'
						|| c == ' ' || c == ':' || c == '.')) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public StringConstant getParent() {
		return parent;
	}

	public void setParent(StringConstant parent) {
		this.parent = parent;
	}
	
	public String getTuffyString() {
		String cname = this.getFormalName();
		char fc = cname.charAt(0);
		if (!Character.isLetter(fc)) {
			cname = cname.substring(1, cname.length()-1);
		}
		cname = StrUtils.replaceNonAlphaNumericCharactersWithDelim(cname, '_');
		return cname;
	}

	public boolean isComplex() {
		return isComplex;
	}
	

}
