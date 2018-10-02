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
import tsl.expression.term.constant.Constant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class TypeTemplate extends Constant {
	private KnowledgeBase knowledgeBase = null;
	private RelationSentence typesent = null;
	private Sentence integration = null;

	public TypeTemplate(KnowledgeBase kb, TypeConstant type,
			Vector<Variable> bvars) {
		this.knowledgeBase = kb;
		this.knowledgeBase.clearFields();
		this.setType(type);
		if (bvars != null && type.getIntegration() != null) {
			this.knowledgeBase.clearFields();
			Vector<Variable> newbvars = new Vector(bvars);
			Variable selfvar = new Variable("?self");
			selfvar.bind(this);
			newbvars.add(selfvar);
			this.knowledgeBase.setSentenceVariableList(newbvars);
			this.typesent = new RelationSentence("typeof", this, type);
			this.integration = (Sentence) type.getIntegration().copy();
			this.setVariables(this.knowledgeBase.getSentenceVariableList());
			for (Variable var : this.getVariables()) {
				Object value = var.getValue();
				Constant c = type.getParameterConstant(var.getName());
				if (c instanceof PropertyConstant) {
					this.setProperty(var.getName(), value);
				} else if (c instanceof RelationConstant
						&& value instanceof Term) {
					RelationConstant rc = (RelationConstant) c;
					Term t = (Term) value;
					new RelationSentence(rc, this, t);
				}
			}
			this.knowledgeBase.initializeAndAddForm(this.typesent);
			this.knowledgeBase.initializeAndAddForm(this.integration);
			if (this.getSubjectSentences() != null) {
				for (RelationSentence rs : this.getSubjectSentences()) {
					this.knowledgeBase.initializeAndAddForm(rs);
				}
			}
			this.knowledgeBase.clearFields();
		}
	}

	public static TypeTemplate create(KnowledgeBase kb, TypeConstant type,
			Vector<Variable> bvars) {
		if (type.validate(bvars)) {
			return new TypeTemplate(kb, type, bvars);
		}
		return null;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

}
