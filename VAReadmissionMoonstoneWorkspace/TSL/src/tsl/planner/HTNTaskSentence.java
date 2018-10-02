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
package tsl.planner;

import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;
import tsl.inference.InferenceEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class HTNTaskSentence extends RelationSentence {
	private HTNMethod method = null;

	public HTNTaskSentence(Vector v) {
		super(v);
	}

	public boolean preProofValidate() {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		if (this.getPrecondition() != null) {
			InferenceEngine ie = kb.getInferenceEngine();
			return ie.prove(this.getPrecondition(), null);
		}
		return true;
	}

	public HTNMethod getMethod() {
		return method;
	}

	public void setMethod(HTNMethod method) {
		this.method = method;
	}

}
