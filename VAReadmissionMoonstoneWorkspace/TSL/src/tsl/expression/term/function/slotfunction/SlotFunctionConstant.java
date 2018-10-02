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
package tsl.expression.term.function.slotfunction;

import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class SlotFunctionConstant extends FunctionConstant {

	public SlotFunctionConstant(String sname) {
		super(sname);
	}

	public static SlotFunctionConstant createSlotFunctionConstant(String sname) {
		SlotFunctionConstant sfc = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		sfc = (SlotFunctionConstant) kb.getNameSpace().getFunctionConstant(
				sname);
		if (sfc == null) {
			sfc = new SlotFunctionConstant(sname);
		}
		return sfc;
	}

	public Object apply(Object arg) {
		Object result = null;
		if (arg instanceof Term) {
			Term sterm = (Term) arg;
			result = sterm.getHeritableProperty(this.getName());
		}
		return result;
	}

	// e.g. "slot-age" => "age"
	public static String getSlotName(String str) {
		if (str.indexOf("get-slot-") == 0 || str.indexOf("set-slot-") == 0) {
			int i = str.lastIndexOf("-");
			return str.substring(i + 1);
		}
		return null;
	}

}
