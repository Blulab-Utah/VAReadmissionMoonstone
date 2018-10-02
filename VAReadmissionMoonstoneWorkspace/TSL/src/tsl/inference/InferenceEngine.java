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
package tsl.inference;

import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public abstract class InferenceEngine {

	private KnowledgeBase knowledgeBase = null;
	private Sentence querySentence = null;
	public int proofCount = 0;
	
	public InferenceEngine() {
	}

	public InferenceEngine(KnowledgeBase kb) {
		this.knowledgeBase = kb;
	}

	public void initialize() {
		this.setProofCount(0);
	}

	public boolean prove(Sentence sentence,
			Vector<RelationSentence> localSentences) {
		return false;
	}

	public void initializeQuerySentence(KnowledgeBase kb, Sentence sentence,
			Vector binds) {
	}

	public Vector unpackValidatedBindings(boolean unpack) {
		return null;
	}

	public KnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public int getProofCount() {
		return proofCount;
	}

	public void setProofCount(int pc) {
		this.proofCount = pc;
	}

	public void incrementProofCount() {
		proofCount++;
	}

	public Sentence getQuerySentence() {
		return querySentence;
	}

	public void setQuerySentence(Sentence querySentence) {
		this.querySentence = querySentence;
	}

}
