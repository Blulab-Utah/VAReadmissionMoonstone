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
package workbench.api.constraint;

import java.util.Vector;

import workbench.api.annotation.Annotation;
import workbench.api.typesystem.Attribute;

public class JavaFunctions {

	public static boolean adjudicationEHostVsMoonstone(Annotation annotation,
			String value, String loop) {
		if (annotationHasClassification(annotation, value)) {
			String aname = annotation.getKtAnnotation().getAnnotatorName()
					.toLowerCase();
			boolean isMoonstone = aname.contains("moonstone");
			boolean hasValidationCorrect = annotationHasAttributeValue(
					annotation, "validation", "correct");
			boolean hasValidationIncorrect = annotationHasAttributeValue(
					annotation, "validation", "incorrect");

			if (!isMoonstone) {
				int x = 1;
			} else {
				int x = 1;
			}

			boolean isValid = true;
			if (hasValidationCorrect) {
				isValid = true;
			} else if (hasValidationIncorrect) {
				isValid = false;
			} else if (!isMoonstone && !hasValidationIncorrect) {
				isValid = true;
			}

			if ("TP-FN-PRIMARY".equals(loop)) {
				return isValid;
			}
			if ("TP-FN-SECONDARY".equals(loop)) {
				return isValid && isMoonstone;
			}
			if ("FP".equals(loop)) {
				return isMoonstone && !isValid;
			}

			int x = 1;
		}
		return false;
	}

	public static boolean annotationPairHasSameType(Annotation a1, Annotation a2) {
		return a1 != null && a2 != null && a1.getType().equals(a2.getType());
	}

	public static boolean annotationHasClassification(Annotation annotation,
			String value) {
		Object cvalue = annotation.getClassificationValue();
		boolean rv = value.equals(cvalue);
		if (rv) {
			int x = 1;
		}
		return rv;
	}

	public static boolean annotationHasValue(Annotation annotation, String value) {
		Vector<Attribute> attributes = annotation.getAllAttributes();
		if (attributes != null) {
			for (Attribute attr : attributes) {
				Object o = annotation.getAttributeValue(attr);
				if (value.equals(o)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean sameSemanticType(Annotation a1, Annotation a2) {
		return (a1.getType() != null && a1.getType().equals(a2.getType()));
	}

	public static boolean hasSemanticType(Annotation a, String tname) {
		return (a.getType() != null && tname != null && tname.equals(a
				.getType().getName()));
	}

	public static boolean annotationHasAttributeValue(Annotation annotation,
			String aname, Object value) {
		Object o = annotation.getAttributeValue(aname);
		boolean result = value.equals(o);
		return result;
	}

	public static boolean annotationHasAttribute(Annotation annotation,
			String aname) {
		Attribute attr = annotation.getType().getAttribute(aname);
		return attr != null;
	}

}
