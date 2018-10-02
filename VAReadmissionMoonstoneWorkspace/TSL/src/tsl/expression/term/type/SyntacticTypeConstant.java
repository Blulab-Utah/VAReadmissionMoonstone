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
package tsl.expression.term.type;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class SyntacticTypeConstant extends TypeConstant {

	public SyntacticTypeConstant(String name) {
		super(name);
	}

	public static SyntacticTypeConstant createSyntacticTypeConstant(String name) {
		SyntacticTypeConstant stype = createSyntacticTypeConstant(name, null);
		return stype;
	}

	public static SyntacticTypeConstant createSyntacticTypeConstant(String name, String fullName) {
		SyntacticTypeConstant tc = null;
		try {
			if (name != null) {
				KnowledgeBase kb = KnowledgeEngine.getCurrentKnowledgeEngine().getCurrentKnowledgeBase();
				if (fullName == null) {
					fullName = name;
				}
 				tc = (SyntacticTypeConstant) kb.getNameSpace().getSyntacticTypeConstant(name);
				if (tc == null) {
					tc = new SyntacticTypeConstant(name);
					tc.setFullName(fullName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tc;
	}

	// e.g. "<DISEASE_OR_SYMPTOM>", "#COMPLEX_EVENT#
	public static boolean isSyntacticTypeConstantFormalName(String str) {
		if (str != null && str.length() > 4 && str.charAt(0) == '#' && str.charAt(str.length() - 1) == '#'
				&& Character.isUpperCase(str.charAt(1))) {
			for (int i = 2; i < str.length() - 1; i++) {
				char c = str.charAt(i);
				if (!(Character.isUpperCase(c) || c == '_' || c == '-' || c == ' ' || c == ':' || c == '.')) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName = "#" + generateFormalNameCore(this.name) + "#";
		}
		return this.formalName;
	}
}
