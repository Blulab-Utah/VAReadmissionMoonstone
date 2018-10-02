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

import java.util.Vector;

import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import typesystem.Attribute;
import workbench.arr.AnnotationEvent;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.GeneralStatistics;

public class SelectedAVPair extends SelectedItem {

	public SelectedAVPair(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		super(arrTool, statistics, item);
	}

	public void analyzeDocument(Document document, typesystem.Annotation level,
			StringBuffer errorSB) throws Exception {
		String attribute = statistics.getSelectedAttributeString();
		AnnotationEvent ae = arrTool.analysis.getAnnotationEvent(document);
		if (attribute == null || ae == null) {
			return;
		}
		AnnotationCollection primaryAC = ae.getPrimaryAnnotationCollection();
		AnnotationCollection secondaryAC = ae
				.getSecondaryAnnotationCollection();

		// Before 2/5/2014
		// Vector<Vector> matching =
		// AnnotationCollection.getMatchingAnnotations(
		// arrTool.analysis.getSelectedLevel(), null, primaryAC,
		// secondaryAC);
		Vector<Vector> matching = AnnotationCollection.getMatchingAnnotations(
				arrTool.analysis.getSelectedLevel(), primaryAC, secondaryAC);

		if (matching != null) {
			for (Vector pair : matching) {
				EVAnnotation primary = (EVAnnotation) pair.elementAt(0);
				EVAnnotation secondary = (EVAnnotation) pair.elementAt(1);
				if (primary != null && secondary != null
						&& primary.hasNonEmptyClassification()
						&& secondary.hasNonEmptyClassification()) {
					for (Object value : this.alternativeValues) {
						String result = matchAttributeValue(document, primary,
								secondary, attribute, value.toString());
						Object key = GeneralStatistics.getResultKey(attribute,
								value, result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);
						String avpair = attribute + ":" + value;
						key = GeneralStatistics.getDocumentResultKey(avpair,
								document.getName(), result);
						HUtils.incrementCount(
								statistics.selectionOutcomeMeasureCountHash,
								key);

						// ////// 11/20/2013
						if (primary != null) {
							VUtils.pushIfNotHashVector(
									statistics.primarySelectionOutcomeMeasureAnnotationHash,
									key, primary);
						}
						if (secondary != null) {
							VUtils.pushIfNotHashVector(
									statistics.secondarySelectionOutcomeMeasureAnnotationHash,
									key, secondary);
						}

						// ///////////////
					}
				}
			}
		}
	}

	public Vector gatherAlternativeValues() {
		this.alternativeValues = null;
		arrTool.analysis.setSelectedAttributeName(null);
		arrTool.analysis.setSelectedAttributeValueNames(null);
		String attribute = getSelectedAttributeString();
		String value = getSelectedValueString();
		Vector v = this.arrTool.getAnalysis()
				.getClassAttributeValues(attribute);
		if (v != null) {
			Vector values = new Vector(v);
			if (value != null) {
				values.remove(value);
				values.insertElementAt(value, 0);
			}
			this.alternativeValues = values;
			arrTool.analysis.setSelectedAttributeName(attribute);
			arrTool.analysis.setSelectedAttributeValueNames(values);
		}
		return this.alternativeValues;
	}

	String matchAttributeValue(Document document, EVAnnotation primary,
			EVAnnotation secondary, String attribute, String value) {
		String avpair = attribute + ":" + value;
		if (EVAnnotation.isPresentAttributeValue(primary, attribute, value)) {
			if (EVAnnotation.isPresentAttributeValue(secondary, attribute,
					value)) {
				VUtils.pushIfNotHashVector(
						statistics.documentTruePositiveFileHash, avpair,
						document.getName());
				HUtils.incrementHashObjectInfoWrapper(
						statistics.documentTruePositiveCountHash, avpair,
						document.getName());
				statistics.totalTruePositive++;
				return GeneralStatistics.TruePositive;
			}
			VUtils.pushIfNotHashVector(
					statistics.documentFalseNegativeFileHash, avpair,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalseNegativeCountHash, avpair,
					document.getName());
			statistics.totalFalseNegative++;
			return GeneralStatistics.FalseNegative;
		}
		if (EVAnnotation.isPresentAttributeValue(secondary, attribute, value)) {
			VUtils.pushIfNotHashVector(
					statistics.documentFalsePositiveFileHash, avpair,
					document.getName());
			HUtils.incrementHashObjectInfoWrapper(
					statistics.documentFalsePositiveCountHash, avpair,
					document.getName());
			statistics.totalFalsePositive++;
			return GeneralStatistics.FalsePositive;
		}
		VUtils.pushIfNotHashVector(statistics.documentTrueNegativeFileHash,
				avpair, document.getName());
		HUtils.incrementHashObjectInfoWrapper(
				statistics.documentTrueNegativeCountHash, avpair,
				document.getName());
		statistics.totalTrueNegative++;
		return GeneralStatistics.TrueNegative;
	}

	public String getSelectedAttributeString() {
		if (this.selectedItem instanceof Attribute) {
			return ((Attribute) this.selectedItem).getName();
		}
		if (this.selectedItem instanceof String) {
			return GeneralStatistics
					.getAttributeFromAVPair((String) this.selectedItem);
		}
		return null;
	}

	public String getSelectedValueString() {
		if (this.selectedItem instanceof String) {
			return GeneralStatistics
					.getValueFromAVPair((String) this.selectedItem);
		}
		return null;
	}

	public String getFirstColumnNameAll(int row) {
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		return (value != null ? value.toString() : null);
	}

}
