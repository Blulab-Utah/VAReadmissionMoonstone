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
package workbench.arr.selected;

import java.util.Enumeration;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import typesystem.Annotation;
import workbench.arr.AnnotationEvent;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.GeneralStatistics;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;

public class SelectedLevel extends SelectedItem {

	public SelectedLevel(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		super(arrTool, statistics, item);
	}

	public Vector gatherAlternativeValues() {
		Annotation annotation = (Annotation) this.selectedItem;
		this.alternativeValues = annotation.getClassifications();
		return this.alternativeValues;
	}

	public void analyzeDocument(Document document, Annotation level,
			StringBuffer errorSB) throws Exception {
		AnnotationEvent ae = arrTool.analysis.getAnnotationEvent(document);
		AnnotationCollection primaryAC = ae.getPrimaryAnnotationCollection();
		AnnotationCollection secondaryAC = ae
				.getSecondaryAnnotationCollection();
		for (Enumeration e = this.alternativeValues.elements(); e
				.hasMoreElements();) {
			typesystem.Classification c = (typesystem.Classification) e
					.nextElement();
			Annotation parent = (Annotation) c.getParentTypeObject();
			Vector<Vector> matching = getMatchingAnnotations(parent, primaryAC,
					secondaryAC);
			if (matching != null) {
				for (Vector pair : matching) {
					EVAnnotation primary = (EVAnnotation) pair.elementAt(0);
					EVAnnotation secondary = (EVAnnotation) pair.elementAt(1);
					if (primary == null
							|| secondary == null
							|| primary.getClassification().equals(
									secondary.getClassification())) {
						String result = matchLevel(arrTool, statistics,
								document, primary, secondary);
						Object key = GeneralStatistics.getResultKey(c, result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);
						key = GeneralStatistics.getDocumentResultKey(c,
								document.getName(), result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);

						// 2/1/2014
						key = GeneralStatistics.getDocumentResultKey(
								c.getParentTypeObject(), document.getName(),
								result);
						if (primary != null) {
							VUtils.pushHashVector(
									statistics.primarySelectionOutcomeMeasureAnnotationHash,
									key, primary);
						}
						if (secondary != null) {
							VUtils.pushHashVector(
									statistics.secondarySelectionOutcomeMeasureAnnotationHash,
									key, secondary);
						}
					}
				}
			}
		}
	}

	public static Vector<Vector> getMatchingAnnotations(
			typesystem.Annotation type, AnnotationCollection ac1,
			AnnotationCollection ac2) throws Exception {
		if (ac1 == null || ac2 == null) {
			return null;
		}
		Vector<EVAnnotation> annotations1 = ac1.typeAnnotationMap.get(type);
		Vector<EVAnnotation> annotations2 = ac2.typeAnnotationMap.get(type);
		Vector<Vector> matching = null;
		if (annotations1 != null) {
			for (EVAnnotation annotation1 : annotations1) {
				EVAnnotation matchedSecond = null;
				int maxOverlap = -1;
				if (annotations2 != null) {
					for (EVAnnotation annotation2 : annotations2) {
						int overlap = AnnotationCollection
								.getAnnotationOverlap(annotation1, annotation2);
						boolean hasStrictOverlap = AnnotationCollection
								.hasStrictOverlap(annotation1, annotation2);
						boolean requiresStrictOverlap = ac1.getAnalysis()
								.getArrTool().isStrictMatchCriterion();
						annotation1.getType();
						typesystem.Classification c1 = annotation1
								.getClassification().getParentClassification();
						typesystem.Classification c2 = annotation2
								.getClassification().getParentClassification();
						if (c1 != null
								&& c2 != null
								&& c1.equals(c2)
								&& ((requiresStrictOverlap && hasStrictOverlap) || (!requiresStrictOverlap && overlap > 0))) {
							annotation2.setVisited();
							if (maxOverlap < overlap) {
								maxOverlap = overlap;
								matchedSecond = annotation2;
							} else if (maxOverlap == overlap) {
								float closeness1 = annotation1
										.attributeSimilarity(matchedSecond);
								float closeness2 = annotation1
										.attributeSimilarity(annotation2);
								if (closeness2 > closeness1) {
									matchedSecond = annotation2;
								}
							}
						}
					}
				}
				Vector v = VUtils.listify(annotation1);
				v.add(matchedSecond);
				matching = VUtils.add(matching, v);
			}
		}
		if (annotations2 != null) {
			for (EVAnnotation annotation2 : annotations2) {
				if (!annotation2.isVisited()) {
					Vector<EVAnnotation> v = VUtils.listify(null);
					v.add(annotation2);
					matching = VUtils.add(matching, v);
				}
				annotation2.resetVisited();
			}
		}
		return matching;
	}

	String matchLevel(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Document document,
			EVAnnotation primary, EVAnnotation secondary) throws Exception {
		Class level = (primary != null ? primary.getClass() : null);
		if (EVAnnotation.isPresentLevel(primary, level)) {
			if (EVAnnotation.isPresentLevel(secondary, level)) {
				VUtils.pushIfNotHashVector(
						statistics.documentTruePositiveFileHash, level,
						document.getName());
				return GeneralStatistics.TruePositive;
			}
			VUtils.pushIfNotHashVector(
					statistics.documentFalseNegativeFileHash, level,
					document.getName());
			return GeneralStatistics.FalseNegative;
		}
		if (EVAnnotation.isPresentLevel(secondary, level)) {
			VUtils.pushIfNotHashVector(
					statistics.documentFalsePositiveFileHash, level,
					document.getName());
			return GeneralStatistics.FalsePositive;
		}
		VUtils.pushIfNotHashVector(statistics.documentTrueNegativeFileHash,
				level, document.getName());
		return GeneralStatistics.TrueNegative;
	}

	public String getFirstColumnNameAll(int row) {
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		typesystem.Classification c = (typesystem.Classification) value;
		return (c != null ? c.toString() : "*");
	}

}
