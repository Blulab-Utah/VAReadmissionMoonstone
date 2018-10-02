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
package moonstone.learning.basilisk;

import java.util.Vector;

import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;

public class TypeExtractionPattern extends ExtractionPattern {

	public TypeExtractionPattern(Annotation annotation,
			Vector<Annotation> anchors) {
		super(annotation, anchors);
	}

	public static boolean canExtractPattern(Annotation annotation) {
		if (annotation.getRule() != null
				&& !annotation.isTerminal()
				&& !annotation.getRule().isVisited()
				&& annotation.getRule().getRuleID() != null
				&& ExtractionPattern.ValidRuleIDs.contains(annotation.getRule()
						.getRuleID()) && annotation.getChildren().size() >= 2
				&& !annotationContainsConjunct(annotation)) {
			boolean foundanchor = false;
			boolean foundcontext = false;
			for (Annotation cannotation : annotation.getChildAnnotations()) {
				if (cannotation.getTokenLength() > 4) {
					return false;
				}
				if (canUseAsAnchor(cannotation)) {
					if (!foundanchor) {
						foundanchor = true;
					} else {
						foundcontext = true;
					}
				}
			}
			return foundanchor && foundcontext;
		}
		return false;
	}

	private static String[] ConjunctStrings = { "and", "or" };

	private static boolean annotationContainsConjunct(Annotation annotation) {
		for (int i = 0; i < ConjunctStrings.length; i++) {
			String cstr = ConjunctStrings[i];
			if (annotation.getString().contains(cstr)) {
				return true;
			}
		}
		return false;
	}

	public static Vector<Annotation> gatherCandidateAnnotations(
			Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.getRule() != null && !annotation.isTerminal()) {
			if (ExtractionPattern.canExtractTypePattern(annotation)) {
				annotation.getRule().setVisited(true);
				annotations = VUtils.listify(annotation);
			}
			for (Annotation ca : annotation
					.getLexicallySortedSourceAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherCandidateAnnotations(ca));
			}
			if (annotation.getRule() != null
					&& annotation.getRule().isVisited()) {
				annotation.getRule().setVisited(false);
			}
		}
		return annotations;
	}

}
