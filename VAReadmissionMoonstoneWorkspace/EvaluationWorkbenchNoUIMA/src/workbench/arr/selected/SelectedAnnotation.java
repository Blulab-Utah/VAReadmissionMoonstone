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
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.arr.AnnotationEvent;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.GeneralStatistics;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;

public class SelectedAnnotation extends SelectedItem {

	public SelectedAnnotation(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		super(arrTool, statistics, item);
	}

	public void analyzeDocument(Document document, typesystem.Annotation level,
			StringBuffer errorSB) throws Exception {
		AnnotationEvent ae = arrTool.analysis.getAnnotationEvent(document);
		if (ae == null || this.alternativeValues == null) {
			return;
		}
		AnnotationCollection primaryAC = ae.getPrimaryAnnotationCollection();
		AnnotationCollection secondaryAC = ae
				.getSecondaryAnnotationCollection();
		AnnotationCollection.discoverTouchingAnnotations(primaryAC
				.getAnnotations());
		AnnotationCollection.discoverTouchingAnnotations(secondaryAC
				.getAnnotations());
		for (Enumeration e = this.alternativeValues.elements(); e
				.hasMoreElements();) {
			Classification classification = (Classification) e.nextElement();
			Vector<Vector> matching = AnnotationCollection
					.getMatchingAnnotations(level, classification, primaryAC,
							secondaryAC);
			if (matching != null) {
				for (Vector pair : matching) {
					EVAnnotation primary = (EVAnnotation) pair.elementAt(0);
					EVAnnotation secondary = (EVAnnotation) pair.elementAt(1);
					String result = matchClassification(document, primary,
							secondary);
					boolean isDuplicate = false;
					if ("FP".equals(result) || "FN".equals(result)) {
						if (primary != null
								&& primary.getIndirectMatchedAnnotation() != null) {
							isDuplicate = true;
						} else if (secondary != null
								&& secondary.getIndirectMatchedAnnotation() != null) {
							isDuplicate = true;
						}
					}
					if (statistics.isMismatch(result)) {
						if (errorSB != null && primary != null) {
							String text = StrUtils.trimAllWhiteSpace(primary
									.getText());
							String str = result + ": DOC=" + document.getName()
									+ ",GOLD=\"" + text + "\""
									+ primary.getSpans() + ",Class="
									+ primary.getClassification().getValue();
							errorSB.append(str + "\n");
						}
						if (errorSB != null && secondary != null) {
							String text = StrUtils.trimAllWhiteSpace(secondary
									.getText());
							String str = result + ": DOC=" + document.getName()
									+ ",SECONDARY=\"" + text + "\""
									+ secondary.getSpans() + ",Class="
									+ secondary.getClassification().getValue();
							errorSB.append(str + "\n");
						}

						if (primary != null) {
							primary.setHasMismatch(result);
						}
						if (secondary != null) {
							secondary.setHasMismatch(result);
						}
					}
					if (!isDuplicate) {
						Object key = GeneralStatistics.getResultKey(
								classification, result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);
						key = GeneralStatistics.getDocumentResultKey(
								classification, document.getName(), result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);
						classification.setUsedInAnnotation(true);

						// 9/4/2013
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
						// ///////////////////////
					}
				}
			}
		}
	}

	public Vector gatherAlternativeValues() {
		this.alternativeValues = Classification
				.getDisplayableClassifications(arrTool.getAnalysis()
						.getClassifications());
		return this.alternativeValues;
	}

	// Before 11/6/2013
	// public Vector gatherAlternativeValues() {
	// this.alternativeValues = arrTool.getAnalysis().getClassifications();
	// return this.alternativeValues;
	// }

	String matchClassification(GeneralStatistics statistics, Document document,
			EVAnnotation primary, EVAnnotation secondary) {

		// 2/19/2013 //////////
		if (primary != null) {
			primary.setMatchedAnnotation(null);
		}
		if (secondary != null) {
			secondary.setMatchedAnnotation(null);
		}
		if (primary != null && secondary != null) {
			primary.setMatchedAnnotation(secondary);
			secondary.setMatchedAnnotation(primary);
		}
		// ////////////////////

		Classification classification = null;
		if (primary != null) {
			classification = primary.getClassification();
		} else if (secondary != null) {
			classification = secondary.getClassification();
		} else {
			return null;
		}

		if (classification == null) {
			return null;
		}
		String key = classification.getAttributeString();
		if (EVAnnotation.isPresentClassification(primary, classification)) {
			if (EVAnnotation.isPresentClassification(secondary, classification)) {
				VUtils.pushIfNotHashVector(
						statistics.documentTruePositiveFileHash, key,
						document.getName());
				HUtils.incrementHashObjectInfoWrapper(
						statistics.documentTruePositiveCountHash, key,
						document.getName());
				statistics.totalTruePositive++;
				return GeneralStatistics.TruePositive;
			}
			VUtils.pushIfNotHashVector(
					statistics.documentFalseNegativeFileHash, key,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalseNegativeCountHash, key,
					document.getName());
			statistics.totalFalseNegative++;
			return GeneralStatistics.FalseNegative;
		}
		if (EVAnnotation.isPresentClassification(secondary, classification)) {
			VUtils.pushIfNotHashVector(
					statistics.documentFalsePositiveFileHash, key,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalsePositiveCountHash, key,
					document.getName());
			statistics.totalFalsePositive++;
			return GeneralStatistics.FalsePositive;
		}
		VUtils.pushIfNotHashVector(statistics.documentTrueNegativeFileHash,
				key, document.getName());
		HUtils.incrementHashObjectInfoWrapper(
				statistics.documentTrueNegativeCountHash, key,
				document.getName());
		statistics.totalTrueNegative++;
		return GeneralStatistics.TrueNegative;
	}

	void processPrimarySecondaryMatch(GeneralStatistics statistics,
			Document document, Classification classification,
			EVAnnotation primary, EVAnnotation secondary, StringBuffer errorSB) {
		String result = matchClassification(statistics, document, primary,
				secondary);
		if (statistics.isMismatch(result)) {
			if (errorSB != null && primary != null) {
				String text = StrUtils.trimAllWhiteSpace(primary.getText());
				String str = result + ": DOC=" + document.getName()
						+ ",GOLD=\"" + text + "\"" + primary.getSpans()
						+ ",Class=" + primary.getClassification().getValue();
				errorSB.append(str + "\n");
			}
			if (errorSB != null && secondary != null) {
				String text = StrUtils.trimAllWhiteSpace(secondary.getText());
				String str = result + ": DOC=" + document.getName()
						+ ",SECONDARY=\"" + text + "\"" + secondary.getSpans()
						+ ",Class=" + secondary.getClassification().getValue();
				errorSB.append(str + "\n");
			}

			if (primary != null) {
				primary.setHasMismatch(result);
			}
			if (secondary != null) {
				secondary.setHasMismatch(result);
			}
		}
		Object key = GeneralStatistics.getResultKey(classification, result);
		HUtils.incrementCount(statistics.selectionOutcomeMeasureCountHash, key);
		key = GeneralStatistics.getDocumentResultKey(classification,
				document.getName(), result);
		HUtils.incrementCount(statistics.selectionOutcomeMeasureCountHash, key);
		classification.setUsedInAnnotation(true);
	}

	public String matchClassification(Document document, EVAnnotation primary,
			EVAnnotation secondary) {
		if (primary != null) {
			primary.setMatchedAnnotation(null);
		}
		if (secondary != null) {
			secondary.setMatchedAnnotation(null);
		}
		if (primary != null && secondary != null) {
			primary.setMatchedAnnotation(secondary);
			secondary.setMatchedAnnotation(primary);
		}

		Classification classification = null;
		if (primary != null) {
			classification = primary.getClassification();
		} else if (secondary != null) {
			classification = secondary.getClassification();
		} else {
			return null;
		}

		if (classification == null) {
			return null;
		}
		String key = classification.getAttributeString();
		if (EVAnnotation.isPresentClassification(primary, classification)) {
			if (EVAnnotation.isPresentClassification(secondary, classification)) {
				VUtils.pushIfNotHashVector(
						statistics.documentTruePositiveFileHash, key,
						document.getName());
				HUtils.incrementHashObjectInfoWrapper(
						statistics.documentTruePositiveCountHash, key,
						document.getName());

				// 7/30/2013
				VUtils.pushHashVector(
						statistics.documentTruePositiveAnnotationHash,
						document, primary);
				VUtils.pushHashVector(
						statistics.documentTruePositiveAnnotationHash,
						document, secondary);

				statistics.totalTruePositive++;
				return GeneralStatistics.TruePositive;
			}
			VUtils.pushIfNotHashVector(
					statistics.documentFalseNegativeFileHash, key,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalseNegativeCountHash, key,
					document.getName());
			statistics.totalFalseNegative++;
			return GeneralStatistics.FalseNegative;
		}
		if (EVAnnotation.isPresentClassification(secondary, classification)) {
			VUtils.pushIfNotHashVector(
					statistics.documentFalsePositiveFileHash, key,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalsePositiveCountHash, key,
					document.getName());

			// 7/30/2013
			VUtils.pushHashVector(
					statistics.documentFalsePositiveAnnotationHash, document,
					secondary);

			statistics.totalFalsePositive++;
			return GeneralStatistics.FalsePositive;
		}
		VUtils.pushIfNotHashVector(statistics.documentTrueNegativeFileHash,
				key, document.getName());
		HUtils.incrementHashObjectInfoWrapper(
				statistics.documentTrueNegativeCountHash, key,
				document.getName());

		// 7/30/2013
		VUtils.pushHashVector(statistics.documentTrueNegativeAnnotationHash,
				document, primary);
		VUtils.pushHashVector(statistics.documentTrueNegativeAnnotationHash,
				document, secondary);

		statistics.totalTrueNegative++;
		return GeneralStatistics.TrueNegative;
	}

	public String getFirstColumnNameAll(int row) {
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		annotation.Classification c = (annotation.Classification) value;
		String dstr = (c != null ? c.getDisplayString() : "*");
		String str = (row + 1) + ":" + dstr;
		return str;
	}

}
