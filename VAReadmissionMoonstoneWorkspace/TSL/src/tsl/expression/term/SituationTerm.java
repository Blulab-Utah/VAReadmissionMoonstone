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
package tsl.expression.term;

import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.utilities.VUtils;

public class SituationTerm extends Term {
	private Sentence sentence = null;
	private Vector<Sentence> facts = null;
	
	public SituationTerm(Sentence sentence) {
		this.sentence = sentence;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	
	public Vector<Sentence> getFacts() {
		return this.facts;
	}
	
	public void addFact(Sentence fact) {
		this.facts = VUtils.add(this.facts, fact);
	}

}
