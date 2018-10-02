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
package moonstone.annotation;

import java.util.Vector;
import moonstone.rule.Rule;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;

public class TagAnnotation extends TerminalAnnotation {

	public TagAnnotation(WordSequenceAnnotation sentence, String cui,
			String concept, SyntacticTypeConstant ptype, String string, int tokenStart,
			int tokenEnd, int textStart, int textEnd, int wordTokenStart,
			int wordTokenEnd, Object value, TypeConstant tc) {
		super(sentence, cui, concept, ptype, string, tokenStart, tokenEnd,
				textStart, textEnd, wordTokenStart, wordTokenEnd, value, tc);
	}

	// 12/5/2014
	public Vector<Rule> getTagRules() {
		return this.getSentenceAnnotation().getGrammar()
				.getRulesByIndexToken(this.getString());
	}
	
	// 6/28/2015
	private void applyConTextModifiersForward(Vector<Annotation> annotations) {
		WordSequenceAnnotation sa = this.getSentenceAnnotation();
		Rule trule = this.getRule();
		String mdir = trule.getModifierDirection();
		for (Annotation annotation : annotations) {
			
		}
	}


}
