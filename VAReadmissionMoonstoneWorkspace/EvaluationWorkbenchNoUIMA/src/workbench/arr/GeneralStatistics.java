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
package workbench.arr;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.VUtils;
import workbench.arr.selected.SelectedAVPair;
import workbench.arr.selected.SelectedAnnotation;
import workbench.arr.selected.SelectedAttribute;
import workbench.arr.selected.SelectedItem;
import workbench.arr.selected.SelectedLevel;

public class GeneralStatistics {

	public EvaluationWorkbench arrTool = null;
	public Hashtable<String, Vector<String>> documentTruePositiveFileHash = new Hashtable();
	public Hashtable<String, Vector<String>> documentTrueNegativeFileHash = new Hashtable();
	public Hashtable<String, Vector<String>> documentFalsePositiveFileHash = new Hashtable();
	public Hashtable<String, Vector<String>> documentFalseNegativeFileHash = new Hashtable();

	// 7/30/2013
	public Hashtable<Document, Vector<EVAnnotation>> documentTruePositiveAnnotationHash = new Hashtable();
	public Hashtable<Document, Vector<EVAnnotation>> documentTrueNegativeAnnotationHash = new Hashtable();
	public Hashtable<Document, Vector<EVAnnotation>> documentFalsePositiveAnnotationHash = new Hashtable();
	public Hashtable<Document, Vector<EVAnnotation>> documentFalseNegativeAnnotationHash = new Hashtable();

	public Hashtable<String, Vector> documentTruePositiveCountHash = new Hashtable();
	public Hashtable<String, Vector> documentTrueNegativeCountHash = new Hashtable();
	public Hashtable<String, Vector> documentFalsePositiveCountHash = new Hashtable();
	public Hashtable<String, Vector> documentFalseNegativeCountHash = new Hashtable();
	public Hashtable<String, Integer> selectionOutcomeMeasureCountHash = new Hashtable();

	// 9/4/2013
	public Hashtable<String, Vector<EVAnnotation>> primarySelectionOutcomeMeasureAnnotationHash = new Hashtable();
	public Hashtable<String, Vector<EVAnnotation>> secondarySelectionOutcomeMeasureAnnotationHash = new Hashtable();

	public int totalTruePositive = 0;
	public int totalFalsePositive = 0;
	public int totalTrueNegative = 0;
	public int totalFalseNegative = 0;
	public float classificationOnlyAccuracy = -1;
	public int classificationOnlyNumRight = -1;
	public int classificationOnlyNumWrong = -1;
	public SelectedItem userSelection = null;
	public static GeneralStatistics statistics = null;
	public static String TruePositive = "TP";
	public static String TrueNegative = "TN";
	public static String TrueMissing = "TM";
	public static String FalsePositive = "FP";
	public static String FalseNegative = "FN";
	public static String FalseMissing = "FM";
	public static String SimpleOverlap = "SO";
	public static int IsClassificationAndSpan = 1;
	public static int IsSpanOnly = 2;
	public static int IsClassificationOnly = 3;
	private static long lastCreateTime = 0L;
	private static GeneralStatistics lastGeneralStatistics = null;

	// 6/5/2013
	private static boolean LockGeneralStatistics = false;

	public GeneralStatistics(EvaluationWorkbench arrTool, Object o,
			StringBuffer sb) throws Exception {
		this.arrTool = arrTool;
		this.arrTool.statistics = this;
		setStatistics(this);
		this.userSelection = SelectedItem.createSelectedItem(arrTool, this,
				o);
		if (this.userSelection != null
				&& this.userSelection.getAlternativeValues() != null) {
			this.userSelection.doAnalysis(sb);
		}
	}

	// 6/10/2013: Currently not using locks -- I think noting the same
	// selectedItem + elapsed time will cover that.
	public static GeneralStatistics create(EvaluationWorkbench arrTool,
			Object o, StringBuffer sb) throws Exception {
		
		if (isSameSelection(o)) {
			return lastGeneralStatistics;
		}
		
		GeneralStatistics gs = lastGeneralStatistics = new GeneralStatistics(arrTool, o, sb);
		arrTool.analysis.setDoRefreshClassifications();
//		arrTool.accuracyPane.doExternalSelection();
		lastCreateTime = System.currentTimeMillis();
		
		return lastGeneralStatistics;
	}

	private static boolean isSameSelection(Object selectedItem) {
		if (lastGeneralStatistics != null) {
			long createTime = System.currentTimeMillis();

			boolean sameTime = createTime - lastCreateTime < 10;
			boolean sameSelectedItem = selectedItem == lastGeneralStatistics
					.getSelectedItem();
			return sameTime && sameSelectedItem;
		}
		return false;
	}

	public static GeneralStatistics create(EvaluationWorkbench arrTool,
			StringBuffer sb) throws Exception {
		return create(arrTool, null, sb);
	}

	public static void setStatistics(GeneralStatistics statistics) {
		GeneralStatistics.statistics = statistics;
	}

	void doAnalysis(StringBuffer errorSB) throws Exception {
		Vector<Document> v = arrTool.analysis.getAllDocuments();
		if (v != null) {
			for (Document document : v) {
				this.userSelection.analyzeDocument(document,
						arrTool.analysis.getSelectedLevel(), errorSB);
			}
		}
	}

	void analyzeSimpleOverlapMatch(Document document) throws Exception {
		typesystem.Annotation level = arrTool.getAnalysis().getSelectedLevel();
		AnnotationEvent ae = arrTool.getAnalysis().getAnnotationEvent(document);
		if (ae == null) {
			return;
		}
		AnnotationCollection primaryAC = ae.getPrimaryAnnotationCollection();
		AnnotationCollection secondaryAC = ae
				.getSecondaryAnnotationCollection();
		Vector<Vector> matching = AnnotationCollection
				.getMatchingAnnotationsSimpleOverlap(
						arrTool.analysis.getSelectedLevel(), primaryAC,
						secondaryAC);
		if (matching != null) {
			for (Vector pair : matching) {
				EVAnnotation primary = (EVAnnotation) pair.elementAt(0);
				EVAnnotation secondary = (EVAnnotation) pair.elementAt(1);
				String result = null;
				if (primary != null && secondary == null) {
					result = FalseNegative;
				} else if (primary == null && secondary != null) {
					result = FalsePositive;
				} else {
					result = TruePositive;
				}
				Object key = getResultKey(SimpleOverlap, level, result);
				HUtils.incrementCount(selectionOutcomeMeasureCountHash, key);
				key = getDocumentSimpleOverlapResultKey(level, document, result);
				HUtils.incrementCount(selectionOutcomeMeasureCountHash, key);

				// 9/4/2013
				if (primary != null) {
					VUtils.pushHashVector(
							primarySelectionOutcomeMeasureAnnotationHash, key,
							primary);
				}
				if (secondary != null) {
					VUtils.pushHashVector(
							secondarySelectionOutcomeMeasureAnnotationHash,
							key, primary);
				}
			}
		}
	}

	void calculateClassificationOnlyAccuracy() {
		float numRights = 0f;
		float numWrongs = 0f;
		if (this.arrTool.getAnalysis() != null
				&& this.arrTool.getAnalysis().getAnnotationEvents() != null) {
			for (AnnotationEvent ae : this.arrTool.getAnalysis()
					.getAnnotationEvents()) {
				Vector<EVAnnotation> primaryAnnotations = ae
						.getPrimaryAnnotationCollection().getAnnotations();
				Vector<EVAnnotation> secondaryAnnotations = ae
						.getSecondaryAnnotationCollection().getAnnotations();
				if (primaryAnnotations != null && secondaryAnnotations != null) {
					boolean foundMatch = false;
					boolean foundMismatch = false;
					for (EVAnnotation secondaryAnnotation : secondaryAnnotations) {
						if (secondaryAnnotation.getSpans() == null) {
							continue;
						}
						for (EVAnnotation primaryAnnotation : primaryAnnotations) {
							// Catch up
							if (primaryAnnotation.getSpans() == null
									|| primaryAnnotation.getEnd() < secondaryAnnotation
											.getStart()) {
								continue;
							}
							// Break if too far
							if (primaryAnnotation.getStart() > secondaryAnnotation
									.getEnd()) {
								if (foundMatch) {
									numRights++;
								} else if (foundMismatch) {
									numWrongs++;
								}
								foundMatch = foundMismatch = false;
								break;
							}
							int overlap = SeqUtils.amountOverlap(
									secondaryAnnotation.getStart(),
									secondaryAnnotation.getEnd(),
									primaryAnnotation.getStart(),
									primaryAnnotation.getEnd());
							if (overlap > 0) {
								String cname1 = secondaryAnnotation
										.getClassification().getValue();
								String cname2 = primaryAnnotation
										.getClassification().getValue();
								if (cname1.equals(cname2)) {
									foundMatch = true;
								} else {
									foundMismatch = true;
								}
							}
						}
					}
					if (foundMatch) {
						numRights++;
					} else if (foundMismatch) {
						numWrongs++;
					}
					foundMatch = foundMismatch = false;
				}
			}
			float denominator = numRights + numWrongs;
			float numerator = numRights;
			this.classificationOnlyAccuracy = (denominator > 0 ? numerator
					/ denominator : 0f);
			this.classificationOnlyNumRight = (int) numRights;
			this.classificationOnlyNumWrong = (int) numWrongs;
		}
	}

	public boolean isMismatch(String result) {
		return result == FalsePositive || result == FalseNegative;
	}

	public static String getResultKey(Object o, String result) {
		if (o == null) {
			return null;
		}
		String str = null;
		if (o instanceof annotation.Classification) {
			annotation.Classification c = (annotation.Classification) o;
			str = c.getAttributeString();
		} else {
			str = o.toString();
		}
		return str + ":" + result;
	}

	public static String getResultKey(String attribute, Object value,
			String result) {
		return attribute + ":" + value + ":" + result;
	}

	public static String getDocumentResultKey(Object o, String document,
			String result) {
		String str = null;
		if (o instanceof annotation.Classification) {
			annotation.Classification c = (annotation.Classification) o;
			str = c.getAttributeString();
		} else if (o != null) {
			str = o.toString();
		}
		return str + ":" + document + ":" + result;
	}

	public static String getDocumentSimpleOverlapResultKey(
			typesystem.Annotation level, Document document, String result) {
		String key = SimpleOverlap + ":" + level + ":" + document.getName()
				+ ":" + result;
		return key;
	}

	static String getDocumentSimpleOverlapKey(Class level, Document document) {
		String key = SimpleOverlap + ":" + level.getSimpleName() + ":"
				+ document.getName();
		return key;
	}

//	public String getColumnLabelAt(int col) {
//		if (col < 0) {
//			return null;
//		}
//		int displayType = getDisplayType(this.arrTool);
//		if (displayType == IsClassificationOnly) {
//			Vector<String> labels = ClassificationStatisticsPane.ClassOnlyOutcomeMeasureColumnLabels;
//			return ClassificationStatisticsPane.ClassOnlyOutcomeMeasureColumnLabels
//					.elementAt(col);
//		} else {
//			return ClassificationStatisticsPane.SpanOutcomeMeasureColumnLabels
//					.elementAt(col);
//		}
//	}

	public Object getValueAt(int row, int col) {
		return getValueAt(getDisplayType(this.arrTool), row, col);
	}

	public Object getValueAt(int type, int row, int col) {
		if (type == IsClassificationAndSpan) {
			return getValueAtClassAndSpanSelection(row, col);
		}
		if (type == IsSpanOnly) {
			return getValueAtSpanOnlySelection(row, col);
		}
		if (type == IsClassificationOnly) {
			return this.getValueAtClassificationOnlySelection(row, col);
		}

		return "*";
	}

	public Object getValueAtSpanOnlySelection(int row, int col) {
		switch (col) {
		case 1:
			return getTruePositive(row);
		case 2:
			return getFalsePositive(row);
		case 3:
			return getTrueNegative(row);
		case 4:
			return getFalseNegative(row);
		case 5:
			return getAccuracy(row);
		case 6:
			return getPPV(row);
		case 7:
			return getSensitivity(row);
		case 8:
			return getNPV(row);
		case 9:
			return getSpecificity(row);
			/* PFR added this for Kappa calculation, 4-26, 6-6-12 */
		case 10:
			return getScottsPi(row);
		case 11:
			return getCohensKappa(row);
		case 12:
			return getFmeasure(row);
			/* end PFR added this for Kappa calculation, 4-26 */
		}
		return "*";
	}

	public Object getValueAtClassificationOnlySelection(int row, int col) {
		if (this.classificationOnlyAccuracy < 0) {
			calculateClassificationOnlyAccuracy();
		}
		if (col == 0) {
			String str = String.valueOf(this.classificationOnlyAccuracy);
			if (str.length() > 5) {
				str = str.substring(0, 5);
			}
			return str;
		} else if (col == 1) {
			return String.valueOf(this.classificationOnlyNumRight);
		} else if (col == 2) {
			return String.valueOf(this.classificationOnlyNumWrong);
		} else if (col == 3) {
			int total = this.classificationOnlyNumRight
					+ this.classificationOnlyNumWrong;
			return String.valueOf(total);
		}
		return "*";
	}

	public Object getValueAtClassAndSpanSelection(int row, int col) {
		row = row - 1;
//		if (col == 0) {
//			return getFirstColumnName(row);
//		}
		if (row == -1) { // Summary row
			if (this.userSelection == null) {
				return "*";
			}
			switch (col) {
			case 1:
				return new Integer(
						this.userSelection.getCumulativeOutcomeMeasureCount(
								TruePositive, -1));
			case 2:
				return new Integer(
						this.userSelection.getCumulativeOutcomeMeasureCount(
								FalsePositive, -1));
			case 3:
				return new Integer(
						this.userSelection.getCumulativeOutcomeMeasureCount(
								TrueNegative, -1));
			case 4:
				return new Integer(
						this.userSelection.getCumulativeOutcomeMeasureCount(
								FalseNegative, -1));
			case 5:
				return getAccuracy(row);
			case 6:
				return getPPV(row);
			case 7:
				return getSensitivity(row);
			case 8:
				return getNPV(row);
			case 9:
				return getSpecificity(row);
				/* PFR added this for Kappa calculation, 4-26, 6-6-12 */
			case 10:
				return getScottsPi(row);
			case 11:
				return getCohensKappa(row);
			case 12:
				return getFmeasure(row);
				/* end PFR added this for Kappa calculation, 4-26 */
			}
		} else { // Move all non-summary rows down one
			switch (col) {
			case 1:
				return getTruePositive(row);
			case 2:
				return getFalsePositive(row);
			case 3:
				return getTrueNegative(row);
			case 4:
				return getFalseNegative(row);
			case 5:
				return getAccuracy(row);
			case 6:
				return getPPV(row);
			case 7:
				return getSensitivity(row);
			case 8:
				return getNPV(row);
			case 9:
				return getSpecificity(row);
				/* PFR added this for Kappa calculation, 4-26, 6-6-12 */
			case 10:
				return getScottsPi(row);
			case 11:
				return getCohensKappa(row);
			case 12:
				return getFmeasure(row);
				/* end PFR added this for Kappa calculation, 4-26 */
			}
		}
		return "*";
	}

	public int getTruePositive(int row) {
		return new Integer(getOutcomeMeasureCount(TruePositive, row));
	}

	public int getFalsePositive(int row) {
		return new Integer(getOutcomeMeasureCount(FalsePositive, row));
	}

	public int getTrueNegative(int row) {
		return new Integer(getOutcomeMeasureCount(TrueNegative, row));
	}

	public int getFalseNegative(int row) {
		return new Integer(getOutcomeMeasureCount(FalseNegative, row));
	}

	public String getAccuracyFormula(int row) {
		int tp = getTruePositive(row);
		int fp = getFalsePositive(row);
		int tn = getTrueNegative(row);
		int fn = getFalseNegative(row);
		String str = "Accuracy = (TP=";
		str += tp + " + TN=" + tn + ") / (TP=";
		str += tp + " + FP=" + fp + " + TN=" + tn + " + FN=" + fn + ")";
		return str;
	}

	public String getAccuracy(int row) {
		float tp = getTruePositive(row);
		float fp = getFalsePositive(row);
		float tn = getTrueNegative(row);
		float fn = getFalseNegative(row);
		float num = tp + tn;
		float den = tp + fp + tn + fn;
		return getResultString(num, den);
	}

	public String getPPVFormula(int row) {
		int tp = getTruePositive(row);
		int fp = getFalsePositive(row);
		String str = "Positive Predictive Value = (TP=";
		str += tp + " / (TP=" + tp + " + FP=" + fp + "))";
		return str;
	}

	public String getPPV(int row) {
		float tp = getTruePositive(row);
		float fp = getFalsePositive(row);
		float num = tp;
		float den = tp + fp;
		return getResultString(num, den);
	}

	public String getSensitivityFormula(int row) {
		int tp = getTruePositive(row);
		int fn = getFalseNegative(row);
		String str = "Sensitivity = (TP=" + tp + " / (TP=" + tp + " + FN=" + fn
				+ "))";
		return str;
	}

	public String getSensitivity(int row) {
		float tp = getTruePositive(row);
		float fn = getFalseNegative(row);
		float num = tp;
		float den = tp + fn;
		return getResultString(num, den);
	}

	public String getNPVFormula(int row) {
		int tn = getTrueNegative(row);
		int fn = getFalseNegative(row);
		String str = "Negative Predictive Value = (TN=" + tn + " / (TN=" + tn
				+ "+ FN=" + fn + "))";
		return str;
	}

	public String getNPV(int row) {
		float tn = getTrueNegative(row);
		float fn = getFalseNegative(row);
		float num = tn;
		float den = tn + fn;
		return getResultString(num, den);
	}

	public String getSpecificityFormula(int row) {
		int fp = getFalsePositive(row);
		int tn = getTrueNegative(row);
		String str = "Specificity = (TN=" + tn + " / (TN=" + tn + " + FP= "
				+ fp + "))";
		// String formula = "Specificity = TN / (TN + FP)";
		return str;
	}

	public String getSpecificity(int row) {
		float fp = getFalsePositive(row);
		float tn = getTrueNegative(row);
		float num = tn;
		float den = tn + fp;
		return getResultString(num, den);
	}

	/*
	 * PFR added follwoing measure 6-6,copiedfrom previous version
	 */
	/* PFR added this for Cohens Kappa calculation, 4-26 */
	public String getCohensKappaFormula(int row) {
		String formula = "Cohen's Kappa = *";
		return formula;
	}

	public String getCohensKappa(int row) {
		float tp = getTruePositive(row);
		float tn = getTrueNegative(row);
		float fp = getFalsePositive(row);
		float fn = getFalseNegative(row);

		/* PFR kappa 4-26-2012 */
		/* get Agreement observed, and by chance, Ao - Ae / 1 - Ae */
		float totalobs = tp + tn + fp + fn;

		/* get marginal sums on contingency table */
		/*
		 * eg primary 1 yes no prim 2 yes tp fp no fn tn
		 */
		float marg4p2_pos = (tp + fp); /*
										 * across row if primary 1 is
										 * cols=ground truth
										 */
		float marg4p1_pos = (tp + fn); /* across cols */
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);

		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			/*
			 * essiantly, take Prob(primary 1 said true)*Prob(primary2 said
			 * true), etc... where Prob is calcuated for each rater
			 */
			E_tp = (marg4p1_pos / totalobs) * (marg4p2_pos / totalobs); /*
																		 * expected
																		 * tp
																		 * matches
																		 */
			E_tn = (marg4p1_neg / totalobs) * (marg4p2_neg / totalobs); /*
																		 * expect
																		 * tn
																		 * matches
																		 */

			Ao = (tp + tn) / totalobs; /* agreements or matches frequency */
			Ae = (E_tp + E_tn); /* expect chance agreements */
		}

		float num = Ao - Ae;
		float den = 1 - Ae;
		return getResultString(num, den);
	}

	public String getScottsPiFormula(int row) {
		String formula = "Scott's Pi = *";
		return formula;
	}

	/* PFR added this for Scotts PI calculation, 4-26 */
	public String getScottsPi(int row) {
		float tp = getTruePositive(row);
		float tn = getTrueNegative(row);
		float fp = getFalsePositive(row);
		float fn = getFalseNegative(row);

		/* PFR kappa 4-26-2012 */
		/* get Agreement observed, and by chance, Ao - Ae / 1 - Ae */
		float totalobs = tp + tn + fp + fn;

		/*
		 * eg primary 1 yes no prim 2 yes tp fp no fn tn
		 */
		float marg4p2_pos = (tp + fp); /*
										 * across row if primary 1 is
										 * cols=ground truth
										 */
		float marg4p1_pos = (tp + fn); /* across cols */
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);

		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			/*
			 * essentially, estimate Prob (primary said true) as pooled across
			 * raters, etcc..
			 */
			E_tp = (marg4p1_pos + marg4p2_pos) / (2 * totalobs); /*
																 * expected tp
																 * matches
																 */
			E_tn = (marg4p1_neg + marg4p2_neg) / (2 * totalobs); /*
																 * expect tn
																 * matches
																 */

			Ao = (tp + tn) / totalobs; /* agreements or matches frequency */
			Ae = (E_tp + E_tn); /* expect chance agreements */
		}

		float num = Ao - Ae;
		float den = 1 - Ae;
		return getResultString(num, den);
	}

	public String getFmeasureFormula(int row) {
		String formula = "F-Measure = *";
		return formula;
	}

	public String getFmeasure(int row) {
		float tp = getTruePositive(row);
		float tn = getTrueNegative(row);
		float fp = getFalsePositive(row);
		float fn = getFalseNegative(row);

		/* PFR adding Fmeas 4-26-12 */
		/* F=(1+B^2)*recall*precision/ B^2*precision + recall */
		float Bwt = 1;
		/*
		 * B weight ofrecall vs precsn, B>1,or<1, means weight recall, orprecn,
		 * more
		 */

		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		; /* else let recall be 0 */

		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		; /* else let precsn be 0 */

		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);

		return getResultString(num, den);
	}

	/* END PFR added this for Kappa calculation, 4-26 */

	String getResultString(float numerator, float denominator) {
		float result = 0;
		String resultString = "*";
		if (denominator > 0) {
			result = numerator / denominator;
			resultString = String.valueOf(result);
			if (resultString.length() > 4) {
				resultString = resultString.substring(0, 4);
			}
		}
		return resultString;
	}

	public static String getSelectedOEMType(int col) {
		switch (col) {
		case 1:
			return TruePositive;
		case 2:
			return FalsePositive;
		case 3:
			return TrueNegative;
		case 4:
			return FalseNegative;
		}
		return null;
	}

//	String getFirstColumnName(int row) {
//		if (row < 0) {
//			return "SUMMARY";
//		}
//		if (doDisplayClassificationAndSpan(arrTool)) {
//			if (this.userSelection == null) {
//				return "*";
//			}
//			return this.userSelection.getFirstColumnNameAll(row);
//		}
//		if (doDisplaySpanOnly(arrTool)) {
//			return "SpanOnly";
//		}
//		if (doDisplayClassificationOnly(arrTool)) {
//			return "ClassOnly";
//		}
//		return "*";
//	}

	String getPrimaryAndSecondaryClassificationName(String cname) {
		String str = cname;
		String sname = this.arrTool.getAnalysis()
				.getSecondaryClassificationName(cname);
		if (sname != null) {
			str += "=" + sname;
		}
		return str;
	}

	public int getOutcomeMeasureCount(String result, int row) {
//		if (doDisplaySpanOnly(arrTool)) {
//			return getOutcomeMeasureCountSimpleOverlap(result);
//		}
		if (this.userSelection == null) {
			return 0;
		}
		if (row == -1) {
			return this.userSelection.getCumulativeOutcomeMeasureCount(result,
					-1);
		}
		return this.userSelection.getOutcomeMeasureCountAll(result, row);
	}

	public int getOutcomeMeasureCountSimpleOverlap(String result) {
		typesystem.Annotation level = arrTool.getAnalysis().getSelectedLevel();
		String key = getResultKey(SimpleOverlap, level, result);
		int count = HUtils.getCount(selectionOutcomeMeasureCountHash, key);
		return count;
	}

	public int getOutcomeDocumentMeasureCount(Object object, String rname,
			String result) {
		Object key = getDocumentResultKey(object, rname, result);
		int count = HUtils.getCount(selectionOutcomeMeasureCountHash, key);
		return count;
	}

//	public static boolean doDisplaySpanOnly(EvaluationWorkbench arrTool) {
//		return (arrTool.accuracyPane != null && arrTool.accuracyPane
//				.isSpanOnly());
//	}
//
//	public static boolean doDisplayClassificationAndSpan(
//			EvaluationWorkbench arrTool) {
//		return (arrTool.accuracyPane != null && arrTool.accuracyPane
//				.isClassificationAndSpan());
//	}
//
//	public static boolean doDisplayClassificationOnly(
//			EvaluationWorkbench arrTool) {
//		return (arrTool.accuracyPane != null && arrTool.accuracyPane
//				.isClassificationOnly());
//	}

	public static int getDisplayType(EvaluationWorkbench arrTool) {
//		if (doDisplayClassificationAndSpan(arrTool)) {
//			return IsClassificationAndSpan;
//		}
//		if (doDisplaySpanOnly(arrTool)) {
//			return IsSpanOnly;
//		}
//		if (doDisplayClassificationOnly(arrTool)) {
//			return IsClassificationOnly;
//		}
		return -1;
	}

	public boolean selectedItemIsAnnotation() {
		return (this.userSelection instanceof SelectedAnnotation);
	}
	
	public boolean selectedItemIsAVPair() {
		return (this.userSelection instanceof SelectedAVPair);
	}

	public boolean selectedItemIsAttribute() {
		return (this.userSelection instanceof SelectedAttribute);
	}

	public boolean selectedItemIsAttributeOrAVPair() {
		return this.selectedItemIsAttribute() || this.selectedItemIsAVPair();
	}

	public boolean selectedItemIsLevel() {
		return (this.userSelection instanceof SelectedLevel);
	}

	public String getSelectedAttributeString() {
		return this.userSelection.getSelectedAttributeString();
	}

	public int getRowCount() {
		if (this.userSelection != null) {
			return this.userSelection.getRowCount();
		}
		return 1;
	}

	public Vector getAlternativeValues() {
		if (this.userSelection != null) {
			return this.userSelection.getAlternativeValues();
		}
		return null;
	}

	public Object getSelectedItem() {
		if (this.userSelection != null) {
			return this.userSelection.getSelectedItem();
		}
		return null;
	}

	public SelectedItem getUserSelection() {
		return this.userSelection;
	}

	public static String getAttributeFromAVPair(String str) {
		int index = str.indexOf(':');
		if (index > 0) {
			String[] strings = str.split(":");
			return strings[0];
		}
		return str;
	}

	public static String getValueFromAVPair(String str) {
		int index = str.indexOf(':');
		if (index > 0) {
			String[] strings = str.split(":");
			return strings[1];
		}
		return null;
	}

	public Vector getTruePositiveReports(Object selection) throws Exception {
		Vector<String> reports = null;
			String key = null;
			if (selection instanceof Classification) {
				key = ((Classification) selection).getAttributeString();
			} else if (selection instanceof String) {
				key = (String) selection;
			}
			reports = documentTruePositiveFileHash.get(key);
			if (reports != null) {
				Collections.sort(reports);
			}
		return reports;
	}

	public Vector getTrueNegativeReports(Object selection) {
		String key = null;
		if (selection instanceof Classification) {
			key = ((Classification) selection).getAttributeString();
		} else if (selection instanceof String) {
			key = (String) selection;
		}
		Vector<String> reports = (Vector) documentTrueNegativeFileHash.get(key);
		if (reports != null) {
			Collections.sort(reports);
		}
		return reports;
	}

	public Vector getFalsePositiveReports(Object selection) {
		String key = null;
		if (selection instanceof Classification) {
			key = ((Classification) selection).getAttributeString();
		} else if (selection instanceof String) {
			key = (String) selection;
		}
		Vector<String> reports = documentFalsePositiveFileHash.get(key);
		if (reports != null) {
			Collections.sort(reports);
		}
		return reports;
	}

	public Vector getFalseNegativeReports(Object selection) {
		String key = null;
		if (selection instanceof Classification) {
			key = ((Classification) selection).getAttributeString();
		} else if (selection instanceof String) {
			key = (String) selection;
		}
		Vector<String> reports = documentFalseNegativeFileHash.get(key);
		if (reports != null) {
			Collections.sort(reports);
		}
		return reports;
	}

	public static boolean isLockGeneralStatistics() {
		return LockGeneralStatistics;
	}

	public static void setLockGeneralStatistics(boolean lockGeneralStatistics) {
		LockGeneralStatistics = lockGeneralStatistics;
	}

}
