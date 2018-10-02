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
package moonstone.grammar;

import java.util.Vector;

import tsl.utilities.SetUtils;
import tsl.utilities.VUtils;

import moonstone.annotation.Annotation;

public class Conjunct {

	public static Vector<Annotation> getExpandedConjuncts(
			Vector<Annotation> annotations) {
		Vector<Annotation> expanded = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.containsConjunct()) {
					expanded = VUtils.append(expanded,
							expandConjuncts(annotation));
				}
			}
		}
		return expanded;
	}

	public static Vector<Annotation> expandConjuncts(Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.isConjunct()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				annotations = VUtils
						.append(annotations, expandConjuncts(child));
			}
		}
		if (annotations == null && annotation.containsConjunct()) {
			annotations = distributeConjuncts(annotation);
		}
		if (annotations == null && annotation.isInterpreted()) {
			annotations = VUtils.listify(annotation);
		}
		return annotations;
	}

	public static Vector<Annotation> distributeConjuncts(Annotation annotation) {
		Vector<Annotation> annotations = null;
		if (annotation.containsConjunct()) {
			Vector<Vector<Annotation>> children = null;
			for (Annotation child : annotation.getChildAnnotations()) {
				if (child.isConjunct()) {
					Vector<Annotation> conjuncts = expandConjuncts(child);
					children = VUtils.add(children, conjuncts);
				} else {
					children = VUtils.add(children, VUtils.listify(child));
				}
			}
			Vector<Vector<Annotation>> csets = SetUtils
					.cartesianProduct(children);
			for (Vector<Annotation> cset : csets) {
				Annotation ca = new Annotation(annotation, cset);
				annotations = VUtils.add(annotations, ca);
			}
		}
		return annotations;
	}

}
