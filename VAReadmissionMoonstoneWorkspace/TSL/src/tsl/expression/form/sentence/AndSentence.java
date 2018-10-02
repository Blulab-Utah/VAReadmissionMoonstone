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
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationSentence;
import tsl.utilities.VUtils;

public class AndSentence extends ComplexSentence {

	public AndSentence() {
		super();
	}

	public AndSentence(Vector v) {
		super(v);
	}

	public static AndSentence createAndSentence(Vector v) {
		if (isAndSentence(v)) {
			return new AndSentence(v);
		}
		return null;
	}

	// 3/30/2014:  Create AndSentence wrapping all sentences pertaining to a given
	// Term.
	public static AndSentence createAndSentence(Term subject) {
		Vector<RelationSentence> rsents = (Vector<RelationSentence>) subject
				.gatherRelatedSentences();
		AndSentence as = null;
		if (rsents != null) {
			Vector<Sentence> sents = null;
			for (RelationSentence rsent : rsents) {
				sents = VUtils.add(sents, rsent);
			}
			as = new AndSentence();
			as.setSentences(sents);
		}
		return as;
	}

	public static boolean isAndSentence(Vector v) {
		return (v != null && "and".equals(v.firstElement()));
	}

	public Expression copy() {
		AndSentence as = new AndSentence();
		for (Sentence oldsent : this.sentences) {
			Sentence newsent = (Sentence) oldsent.copy();
			as.sentences = VUtils.add(as.sentences, newsent);
		}
		return as;
	}

	public String toString() {
		String str = "(and ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s + " ";
			}
		}
		str += ")";
		return str;
	}

	public String toLisp() {
		String str = "(and ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s.toLisp() + " ";
			}
		}
		str += ")";
		return str;
	}

	public String toShortString() {
		String str = "(and ";
		if (this.sentences != null) {
			for (Sentence s : this.sentences) {
				str += s.getStringID() + " ";
			}
		}
		str += ")";
		return str;
	}

}
