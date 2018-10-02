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

import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import typesystem.Annotation;
import typesystem.Attribute;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.GeneralStatistics;
import annotation.EVAnnotation;

public abstract class SelectedItem {
	public EvaluationWorkbench arrTool = null;
	public GeneralStatistics statistics = null;
	public Object selectedItem = null;
	public Vector alternativeValues = null;

	public SelectedItem(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		this.selectedItem = item;
		this.arrTool = arrTool;
		this.statistics = statistics;
	}

	public static SelectedItem createSelectedItem(EvaluationWorkbench arrTool,
			GeneralStatistics statistics, Object item) {
		if (itemIsAnnotation(item)) {
			return new SelectedAnnotation(arrTool, statistics, item);
		}
		if (itemIsAVPair(item)) {
			return new SelectedAVPair(arrTool, statistics, item);
		}
		if (itemIsAttribute(item)) {
			return new SelectedAttribute(arrTool, statistics, item);
		}

		// 1/31/2014
		if (itemIsLevel(item)) {
			return new SelectedLevel(arrTool, statistics, item);
		}
		return null;
	}

	public Vector getAlternativeValues() {
		if (this.alternativeValues == null) {
			this.alternativeValues = gatherAlternativeValues();
		}
		return alternativeValues;
	}
	
	public void setAlternativeValues(Vector values) {
		this.alternativeValues = values;
	}

	public Vector gatherAlternativeValues() {
		return null;
	}

	public void doAnalysis(StringBuffer errorSB) throws Exception {
		Vector<Document> v = arrTool.analysis.getAllDocuments();
		if (v != null) {
			for (Document document : v) {
				this.analyzeDocument(document,
						arrTool.analysis.getSelectedLevel(), errorSB);
			}
		}
	}

	public void analyzeDocument(Document document, typesystem.Annotation level,
			StringBuffer errorSB) throws Exception {

	}

	public static boolean itemIsAnnotation(Object item) {
		return (item instanceof EVAnnotation || item == null);
	}

	public static boolean itemIsAttributeOrAVPair(Object item) {
		return (item instanceof Attribute || item instanceof String);
	}

	public static boolean itemIsAttribute(Object item) {
		if (item instanceof Attribute) {
			return true;
		}
		if (item instanceof String) {
			String str = (String) item;
			if (str.indexOf(':') < 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean itemIsAVPair(Object item) {
		if (item instanceof String) {
			String str = (String) item;
			if (str.indexOf(':') > 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean itemIsLevel(Object item) {
		return item instanceof Annotation;
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public int getRowCount() {
//		if (this.alternativeValues != null
//				&& GeneralStatistics.doDisplayClassificationAndSpan(arrTool)) {
//			return (this.alternativeValues.size());
//		}
		return 1;
	}

	public String getFirstColumnNameAll(int row) {
		return "*";
	}

	public int getOutcomeMeasureCountAll(String result, int row) {
		String attribute = statistics.getSelectedAttributeString();
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		Object key = null;
		if (this instanceof SelectedAnnotation || this instanceof SelectedLevel
				|| this instanceof SelectedAttribute) {
			key = GeneralStatistics.getResultKey(value, result);
		} else if (this instanceof SelectedAVPair) {
			key = GeneralStatistics.getResultKey(attribute, value, result);
		}
		if (key != null) {
			Integer count = HUtils.getCount(
					statistics.selectionOutcomeMeasureCountHash, key);
			return count;
		}
		return 0;
	}

	public int getCumulativeOutcomeMeasureCount(String result, int selectedRow) {
		String astr = getSelectedAttributeString();
		int count = 0;
		if (this.alternativeValues != null) {
			for (int i = 0; i < this.alternativeValues.size(); i++) {
				if (selectedRow == -1 || i != selectedRow) {
					Object key = null;
					Object value = this.alternativeValues.elementAt(i);
					if (this instanceof SelectedAnnotation
							|| this instanceof SelectedAttribute
							|| this instanceof SelectedLevel) {
						key = GeneralStatistics.getResultKey(value, result);
					} else if (this instanceof SelectedAVPair) {
						key = GeneralStatistics.getResultKey(astr, value,
								result);
					}
					if (key != null) {
						Object o = statistics.selectionOutcomeMeasureCountHash
								.get(key);
						if (o != null) {
							count += (Integer) o;
						}
					}
				}
			}
		}
		return count;
	}

	public String getSelectedAttributeString() {
		return null;
	}

	public String toString() {
		return "<" + this.getClass().getSimpleName() + ":" + this.selectedItem
				+ ">";
	}

}
