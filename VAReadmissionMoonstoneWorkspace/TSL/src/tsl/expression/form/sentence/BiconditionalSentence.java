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

import tsl.expression.term.relation.RelationSentence;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.Symbol;
import tsl.tsllisp.TLUtils;

public class BiconditionalSentence extends ImplicationSentence {

	
	public BiconditionalSentence() {
	}

	public BiconditionalSentence(String name) {
		super(name);
	}

	public BiconditionalSentence(Vector v) {
		super();
		if (v.size() == 3) {
			this.antecedent = Sentence.createSentence((Vector) v.elementAt(1));
			this.consequent = Sentence.createSentence((Vector) v.elementAt(2));
		}
	}

	public static BiconditionalSentence createBiconditionalSentence(Vector v) {
		if (v != null && "<->".equals(v.firstElement())) {
			return new BiconditionalSentence(v);
		}
		return null;
	}
	
	public static boolean isBiconditionalSentence(Sexp sexp) {
		return (sexp.getLength() == 3
				&& TLUtils.isSymbol(sexp.getFirst())
				&& "<->".equals(((Symbol) sexp.getFirst()).getName())
				&& TLUtils.isCons(sexp.getSecond())
				&& TLUtils.isCons(sexp.getThird()));
	}

	
}
