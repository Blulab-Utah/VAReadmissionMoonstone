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
import annotation.EVAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import typesystem.Attribute;
import workbench.arr.AnnotationEvent;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.GeneralStatistics;

public class SelectedAttribute extends SelectedItem {

	public SelectedAttribute(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		super(arrTool, statistics, item);
	}

	// 2/5/2014
	public void analyzeDocument(Document document, typesystem.Annotation level,
			StringBuffer errorSB) throws Exception {
		Object o = this.alternativeValues.firstElement();
		if (!(this.alternativeValues != null && this.alternativeValues
				.firstElement() instanceof Attribute)) {
			return;
		}
		Attribute attribute = (Attribute) this.alternativeValues.firstElement();
		AnnotationEvent ae = arrTool.analysis.getAnnotationEvent(document);
		AnnotationCollection primaryAC = ae.getPrimaryAnnotationCollection();
		AnnotationCollection secondaryAC = ae
				.getSecondaryAnnotationCollection();
		Vector<Vector> matching = AnnotationCollection.getMatchingAnnotations(
				arrTool.analysis.getSelectedLevel(), primaryAC, secondaryAC);
		if (matching != null) {
			for (Vector<EVAnnotation> pair : matching) {
				EVAnnotation primary = pair.firstElement();
				EVAnnotation secondary = pair.lastElement();
				String result = null;
				if (hasAttribute(primary, attribute) && hasAttribute(secondary, attribute)) {
					result = "TP";
				} else if (hasAttribute(primary, attribute) && !hasAttribute(secondary, attribute)) {
					result = "FN";
				} else if (!hasAttribute(primary, attribute) && hasAttribute(secondary, attribute)) {
					result = "FP";
				} else {
					result = "TN";
				} 
				Object key = GeneralStatistics.getResultKey(
						attribute, result);
				HUtils.incrementCount(
						statistics.selectionOutcomeMeasureCountHash,
						key);
				key = GeneralStatistics.getDocumentResultKey(
						attribute, document.getName(), result);
				HUtils.incrementCount(
						statistics.selectionOutcomeMeasureCountHash,
						key);
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

	// 2/4/2014
	public Vector gatherAlternativeValues() {
		this.alternativeValues = null;
		Attribute attribute = (Attribute) this.getSelectedItem();
		this.alternativeValues = VUtils.listify(attribute);
		return this.alternativeValues;
	}

	private static boolean hasAttribute(EVAnnotation annotation,
			Attribute attribute) {
		return annotation != null && attribute != null
				&& annotation.getAttribute(attribute.getName()) != null;
	}

	public String getSelectedAttributeString() {
		if (this.selectedItem instanceof Attribute) {
			return ((Attribute) this.selectedItem).getName();
		}
		if (this.selectedItem instanceof String) {
			String str = this.selectedItem.toString();
			int index = str.indexOf(':');
			if (index > 0) {
				String[] strings = str.split(":");
				return strings[0];
			}
			return str;
		}
		return null;
	}
	
	public String getFirstColumnNameAll(int row) {
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		String dstr = "*";
		if (value instanceof Attribute) {
			dstr = ((Attribute) value).getName();
		}
		String str = (row + 1) + ":" + dstr;
		return str;
	}

}
