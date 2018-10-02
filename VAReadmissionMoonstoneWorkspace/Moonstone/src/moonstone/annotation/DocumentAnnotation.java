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

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.utilities.SeqUtils;
import moonstone.rule.Rule;
import moonstone.rule.RuleExpansion;

public class DocumentAnnotation extends Annotation {

	public DocumentAnnotation(WordSequenceAnnotation sentenceAnnotation,
			Rule rule, Vector<Annotation> sources, boolean doExpand) {
		super(sentenceAnnotation, rule, sources, doExpand);
		if (this.getSemanticInterpretation() != null) {
			// this.getSemanticInterpretation().getRelationSentences();
		}
	}

	// Before 4/17/2015
	// public void assignRelativeOffsets() {
	// Annotation start = this.textuallySortedSourceAnnotations.firstElement();
	// Annotation end = this.textuallySortedSourceAnnotations.lastElement();
	// this.relativeTokenStart = start.tokenStart;
	// this.relativeTokenEnd = end.tokenEnd;
	// }

	public boolean checkIsValid() {
		try {
			Vector<Annotation> children = this.getTextuallySortedSourceAnnotations();
			if (this.rule.isDoDebug()) {
				int x = 1;
			}
			
			Rule rule = this.rule;
			float depth = this.getDepth();
			if (this.getDepth() > 20) {
				this.invalidReason = Annotation.FailDepth;
				return false;
			}
			if (this.typeLoopCheck(this)) {
				this.invalidReason = Annotation.FailTypeCheck;
				return false;
			}
			for (int i = 0; i < this.textuallySortedSourceAnnotations.size() - 1; i++) {
				Annotation a1 = this.textuallySortedSourceAnnotations
						.elementAt(i);
				Annotation a2 = this.textuallySortedSourceAnnotations
						.elementAt(i + 1);
				if (!SeqUtils.disjoint(a1.getTokenStart(), a1.getTokenEnd(),
						a2.getTokenStart(), a2.getTokenEnd())) {
					this.invalidReason = RuleExpansion.HasOverlap;
					return false;
				}
				
				// 1/15/2016:  JUST FOR READMISSION:  I DON'T NEED MULTI-SENTENCE
				// ANNOTATIONS
				// 2/26/2016:  When I discard document-level overlapping annotations, 
				// invalid multi-sentence annotations with low probability can swallow 
				// more valid ones.  Need to think of a good solution.
				if (!a1.getSentenceAnnotation().equals(a2.getSentenceAnnotation())) {
					return false;
				}
			}
			if (this.getRule().isOrdered()) {
				for (int i = 0; i < this.lexicallySortedSourceAnnotations
						.size() - 1; i++) {
					Annotation a1 = this.lexicallySortedSourceAnnotations
							.elementAt(i);
					Annotation a2 = this.lexicallySortedSourceAnnotations
							.elementAt(i + 1);
					if (!(a1.getTokenEnd() < a2.getTokenStart())) {
						this.invalidReason = RuleExpansion.NotOrdered;
						return false;
					}
				}
			}

			if (this.getRule().getTestPredicates() != null) {
				if (this.rule.isDoDebug()) {
					int x = 1;
				}
				for (Constraint c : this.getRule().getTestPredicates()) {
					Boolean rv = (Boolean) c
							.evalConstraint(this.getVariables());
					if (rv == null || !rv) {
						this.invalidReason = Annotation.FailTest;
						
						if (this.getConcept().toString().toLowerCase().contains("support")) {
							int x = 1;
						}
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
