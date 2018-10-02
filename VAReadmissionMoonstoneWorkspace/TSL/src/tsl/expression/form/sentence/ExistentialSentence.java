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
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

// (exists (?x) (and (condition ?x "fever") (polarity ?x "present")))

public class ExistentialSentence extends QuantifierSentence {
	
	public ExistentialSentence() {
		
	}
	
	public ExistentialSentence(Vector v) {
		super(v);
	}

	public static ExistentialSentence createExistentialSentence(Vector v) {
		if (v != null && "exists".equals(v.firstElement())) {
			return new ExistentialSentence(v);
		}
		return null;
	}
	
	public String toString() {
		String str = "(exists (";
		for (int i = 0; i < this.getVariableCount(); i++) {
			Variable var = this.getVariable(i);
			str += var.getName();
			if (i < this.getVariableCount()-1) {
				str += " ";
			}
		}
		str += ") ";
		str += this.getSentence().toString();
		str += ")";
		return str;
	}

}
