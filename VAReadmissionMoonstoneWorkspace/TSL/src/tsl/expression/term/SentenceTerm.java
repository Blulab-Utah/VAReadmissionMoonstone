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

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.Sentence;

// Permits a sentence to be used as an argument to another sentence.

public class SentenceTerm extends Term {
	private Sentence sentence = null;
	
	public SentenceTerm(String name) {
		super(name);
	}
	
	public SentenceTerm(String name, Sentence sentence) {
		super(name);
		this.sentence = sentence;
	}
	
	public SentenceTerm(Sentence sentence) {
		this.sentence = sentence;
	}
	
	// 3/30/2014:  Create SentenceTerm representing an object containing all the RelSents
	// that describe a particular Term.
	public static SentenceTerm createSentenceTerm(Term subject) {
		AndSentence as = AndSentence.createAndSentence(subject);
		SentenceTerm st = null;
		if (as != null) {
			st = new SentenceTerm(as);
		}
		return st;
	}

	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

}
