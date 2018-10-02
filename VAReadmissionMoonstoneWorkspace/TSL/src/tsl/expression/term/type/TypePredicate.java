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

import java.util.Vector;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class TypePredicate extends Sentence {
	private TypeConstant type = null;
	private Term argument = null;
	
	public TypePredicate(String tname, String aname) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.type = kb.getNameSpace().getTypeConstant(tname);
		this.argument = (Term) kb.getTerm(this, aname);
	}
	
	public static TypePredicate createTypePredicate(Vector v) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (v.size() == 2) {
			String tname = v.elementAt(0).toString();
			String aname = v.elementAt(1).toString();
			TypeConstant type = kb.getNameSpace().getTypeConstant(tname);
			if (type != null) {
				return new TypePredicate(tname, aname);
			}
		}
		return null;
	}

	public TypeConstant getType() {
		return type;
	}

	public Term getArgument() {
		return argument;
	}

}
