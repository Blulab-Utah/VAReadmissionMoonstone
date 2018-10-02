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

public abstract class QuantifierSentence extends Sentence {
	
	protected Sentence sentence = null;
	
	public QuantifierSentence() {
		
	}
	
	public QuantifierSentence(Vector v) {
		super(v);
		Vector vv = (Vector) v.elementAt(1);
		Vector sv = (Vector) v.elementAt(2);
		int vcount = vv.size();
		Vector<Variable> vars = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		for (int i = 0; i < vcount; i++) {
			String vname = (String) vv.elementAt(i);
			Variable var = (Variable) kb.getTerm(this, vname);
			vars = VUtils.add(vars, var);
		}
		this.setVariables(vars);
		this.sentence = Sentence.createSentence(sv);
	}

	public static Sentence createQuantifierSentence(Vector v) {
		Sentence s = null;
		if ((s = ExistentialSentence.createExistentialSentence(v)) != null
				|| (s = UniversalSentence.createUniversalSentence(v)) != null) {
			return s;
		}
		return null;
	}
	
	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.setContainingKBExpression(containingKBExpression);
		this.setKnowledgeBase(kb);
		this.sentence.assignContainingKBExpression(kb, containingKBExpression);
	}
	
	
	public Vector gatherRelationalSentences() {
		Vector<RelationSentence> relsents = null;
		relsents = VUtils.append(relsents, this.sentence
				.gatherRelationalSentences());
		return relsents;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	
	
}
