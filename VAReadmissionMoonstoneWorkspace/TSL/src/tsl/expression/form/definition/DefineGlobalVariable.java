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
package tsl.expression.form.definition;

import java.util.Vector;

import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class DefineGlobalVariable extends Definition {
	private String vname = null;
	private ObjectConstant term = null;
	
	public DefineGlobalVariable(Vector v) {
		super();
		this.vname = (String) v.elementAt(1);
		this.term = new ObjectConstant(v.elementAt(2));
	}
	
	public Object eval() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		kb.addGlobalVariable(this.vname, this.term);
		return this.term;
	}
	
	public static DefineGlobalVariable createDefineGlobalVariable(Vector v) {
		if (isDefineGlobalVariable(v)) {
			return new DefineGlobalVariable(v);
		}
		return null;
	}
	
	// (defvar ?*high-level-words* '("high" "severe" ...))
	public static boolean isDefineGlobalVariable(Vector v) {
		if (v != null && v.size() == 3 && "defvar".equals(v.firstElement())
				&& v.elementAt(1) instanceof String) {
			String vname = (String) v.elementAt(1);
			if (Variable.isVariable(vname) && vname.charAt(1) == '*') {
				return true;
			}
		}
		return false;
	}

}
