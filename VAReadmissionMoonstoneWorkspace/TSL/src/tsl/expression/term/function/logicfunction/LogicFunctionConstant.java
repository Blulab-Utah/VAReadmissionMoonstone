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
package tsl.expression.term.function.logicfunction;

import java.util.Vector;

import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class LogicFunctionConstant extends FunctionConstant {

	public LogicFunctionConstant(String fname) {
		this(VUtils.listify(fname));
	}
	
	public LogicFunctionConstant(Vector v) {
		super((String) v.firstElement());
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		Vector<Variable> vars = null;
		for (int i = 1; i < v.size(); i++) {
			String vname = (String) v.elementAt(i);
			Variable var = (Variable) kb.getTerm(this, vname);
			vars = VUtils.add(vars, var);
		}
		this.setVariables(vars);
	}
	
	public static LogicFunctionConstant createLogicFunctionConstant(Vector v) {
		if ("deflogicfunctionconstant".equals(v.firstElement())) {
			return new LogicFunctionConstant(v);
		}
		return null;
	}

}
