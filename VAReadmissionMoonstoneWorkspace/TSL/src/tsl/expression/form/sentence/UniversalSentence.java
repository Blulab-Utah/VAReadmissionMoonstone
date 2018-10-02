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
package tsl.expression.form.sentence;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.knowledge.knowledgebase.KnowledgeBase;

// (forall (?x) (and (condition ?x "fever") (polarity ?x "present")))

public class UniversalSentence extends QuantifierSentence {
	
	public UniversalSentence() {
		super();
	}
	
	public UniversalSentence(Vector v) {
		super(v);
	}

	public static UniversalSentence createUniversalSentence(Vector v) {
		if (v != null && "forall".equals(v.firstElement())) {
			return new UniversalSentence(v);
		}
		return null;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		this.setKnowledgeBase(kb);
		this.sentence.setContainingKBExpression(containingKBExpression);
	}
	
}
