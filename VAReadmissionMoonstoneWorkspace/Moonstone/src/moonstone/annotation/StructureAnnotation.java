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
import tsl.utilities.VUtils;
import moonstone.rule.Rule;

public class StructureAnnotation extends Annotation {

	public static String SentenceConceptString = ":SENTENCE:";
	public static String AVPairConceptString = ":ATTRIBUTE_VALUE_PAIR:";
	public static String[] AnalyzableStructureTypes = new String[] {
			SentenceConceptString, AVPairConceptString };

	public StructureAnnotation(WordSequenceAnnotation sentenceAnnotation,
			Rule rule, Vector<Annotation> sources, boolean doExpand) {
		super(sentenceAnnotation, rule, sources, doExpand);
	}

	// 3/31/2015: I was calling Annotation.checkIsValid() which returns true on
	// every annotation. Of COURSE I was getting far too many annotations!
	// Try again with the new grammar later...
	public boolean checkIsValid() {
		try {
			if (this.getRule() != null && this.getRule().isDoDebug()) {
				int x = 1;
				x = x;
			}
			if (this.getDepth() > 20) {
				return false;
			}
			for (int i = 0; i < this.lexicallySortedSourceAnnotations.size() - 1; i++) {
				Annotation a1 = this.lexicallySortedSourceAnnotations
						.elementAt(i);
				Annotation a2 = this.lexicallySortedSourceAnnotations
						.elementAt(i + 1);
				if (a1.getTextEnd() != a2.getTextStart() - 1) {
					return false;
				}
				// 4/2/2015: Assume there is no window in structure, since
				// "window"
				// is really only a coherent notion in semantic groupings of
				// concepts.
				// if (a2.getTokenEnd() - a1.getTokenStart() > this.getRule()
				// .getWindow()) {
				// return false;
				// }
			}
			if (this.getRule().getTestPredicates() != null) {
				for (Constraint c : this.getRule().getTestPredicates()) {
					Boolean rv = (Boolean) c
							.evalConstraint(this.getVariables());
					if (rv == null || !rv) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static Vector<Annotation> extractAnalyzableStructureAnnotations(
			Vector<Annotation> annotations) {
		Vector<Annotation> typedAnnotations = null;
		if (annotations != null) {
			for (Annotation sa : annotations) {
				for (int i = 0; i < AnalyzableStructureTypes.length; i++) {
					String type = AnalyzableStructureTypes[i];
					if (sa.getConcept() != null
							&& type.equals(sa.getConcept().toString())) {
						typedAnnotations = VUtils.add(typedAnnotations, sa);
					}
				}
			}
		}
		return typedAnnotations;
	}

	public static boolean isAttributeValuePair(Annotation annotation) {
		return (annotation instanceof StructureAnnotation && ((StructureAnnotation) annotation)
				.isAttributeValuePair());
	}

	public boolean isAttributeValuePair() {
		return this.getConcept() != null
				&& AVPairConceptString.equals(this.getConcept().toString());
	}
	
	public boolean isAttributeValueValue() {
		if (this.isAttributeValuePair()) {
			StructureAnnotation sa = (StructureAnnotation) this;
			
		}
		return this.getConcept() != null
				&& AVPairConceptString.equals(this.getConcept().toString());
	}

	public boolean isSentence() {
		return this.getConcept() != null
				&& SentenceConceptString.equals(this.getConcept().toString());
	}

	public StructureAnnotation getAVPairAttribute() {
		if (this.isAttributeValuePair()) {
			return (StructureAnnotation) this.getChildren().firstElement();
		}
		return null;
	}

	public StructureAnnotation getAVPairValue() {
		if (this.isAttributeValuePair()) {
			return (StructureAnnotation) this.getChildren().lastElement();
		}
		return null;
	}

}
