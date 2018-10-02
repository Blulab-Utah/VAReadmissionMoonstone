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
package tsl.expression.term.function.slot;

import java.util.Vector;
import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionTerm;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class SlotFunctionTerm extends FunctionTerm {

	// 4/6/2013 note: I need to support cardinality. Should I allow Vectors
	// in Property slots? Should I also be storing RelationSentences in Property
	// slots?

	// e.g. (get-slot-age ?x)
	public SlotFunctionTerm(Vector v) {
		super();
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		String slotname = SlotFunctionConstant.getSlotName((String) v
				.firstElement());
		this.setName(slotname);
		Object value = (Term) kb.getTerm(this, (String) v.elementAt(2));
		this.addTerm(value);
	}

	// e.g. inside sentence (> (get-slot-age (person-named "bill")) 100)
	public Object eval() {
		Term sterm = (Term) this.getTerm(0);
		Object value = null;
		if (sterm != null) {
			sterm = (Term) sterm.eval();
			value = sterm.getSlotValue(this.getName());
		}
		return value;
	}
}
