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

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.AnnotatorType;
import workbench.api.OutcomeResult;
import workbench.api.WorkbenchAPIObject;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.MatchedValueStatistics;
import workbench.api.annotation.OverlappingAnnotationPair;

public class ConstraintMatch {

	private Analysis analysis = null;
	private ConstraintPacket constraintPacket = null;
	private WorkbenchAPIObject workbenchAPIObject = null;
	private Vector<String> alternativeValues = null;
	private Vector<String> displayValues = null;
	private Vector<Annotation> falsePositives = null;
	private Vector<Annotation> falseNegatives = null;
	private Hashtable<String, Vector<Annotation>> primaryMatchedDocumentAnnotationHash = new Hashtable();
	private Hashtable<String, Vector<Annotation>> secondaryMatchedDocumentAnnotationHash = new Hashtable();
	private Hashtable<String, Vector<Annotation>> falseAnnotationHash = new Hashtable();
	private int cumulativeTruePositiveCount = 0;
	private int cumulativeFalsePositiveCount = 0;
	private int cumulativeTrueNegativeCount = 0;
	private int cumulativeFalseNegativeCount = 0;
	private MatchedValueStatistics[] matchedValueStatistics = null;
	private boolean applyExactMatch = false;

	public ConstraintMatch(Analysis analysis, ConstraintPacket cp) {
		this.constraintPacket = cp;
		this.analysis = analysis;
		// this.workbenchAPIObject = wao;
		// this.alternativeValues = new Vector(values);
		// Collections.sort(this.alternativeValues);
		// this.matchedValueStatistics = new
		// MatchedValueStatistics[this.alternativeValues
		// .size()];
		// for (int i = 0; i < this.alternativeValues.size(); i++) {
		// String value = this.alternativeValues.elementAt(i);
		// this.valueRowMap.put(value, new Integer(i));
		// }
	}

	public void applyConstraintToMatchedPairs() {
		this.clear();

		int mfacilcount = 0;

		Vector<String> avs = this.analysis.getAllUserSelectionValues();

		String primaryAnnotatorName = KnowledgeEngine
				.getCurrentKnowledgeEngine().getStartupParameters()
				.getPropertyValue("WorkbenchPrimaryAnnotatorAnnotationName");
		if (primaryAnnotatorName != null) {
			primaryAnnotatorName = primaryAnnotatorName.toLowerCase();
		}

		if (avs != null
				&& this.analysis.getAllPrimarySnippetAnnotations() != null
				&& this.analysis.getAllSecondarySnippetAnnotations() != null) {
			this.alternativeValues = new Vector(avs);

			Collections.sort(this.alternativeValues, new Comparator() {
				public int compare(Object o1, Object o2) {
					String s1 = o1.toString().toLowerCase();
					String s2 = o2.toString().toLowerCase();
					return s1.compareTo(s2);
				}
			});

			Annotation.resetContainsClassificationMatch(this.analysis
					.getAllPrimarySnippetAnnotations());
			Annotation.resetContainsClassificationMatch(this.analysis
					.getAllSecondarySnippetAnnotations());

			this.matchedValueStatistics = new MatchedValueStatistics[this.alternativeValues
					.size()];
			Variable avar = new Variable("?annotation");
			Variable vvar = new Variable("?value");
			Variable avar1 = new Variable("?annotation1");
			Variable avar2 = new Variable("?annotation2");
			Variable cvar = new Variable("?classification");
			Variable loopvar = new Variable("?loop");
			Vector<Variable> vars = VUtils.listify(avar, vvar, avar1, avar2,
					cvar, loopvar);

			for (int i = 0; i < this.alternativeValues.size(); i++) {
				String value = this.alternativeValues.elementAt(i);

				// 12/29/2015: Need a better solution..
				if (!this.valueIsRelevant(value)) {
					continue;
				}

				MatchedValueStatistics mvs = new MatchedValueStatistics(this,
						value);
				this.matchedValueStatistics[i] = mvs;
				vvar.bind(value);
				for (Annotation primary : this.analysis
						.getAllPrimarySnippetAnnotations()) {
					String aname = primary.getKtAnnotation().getAnnotatorName()
							.toLowerCase();

					// How should I deal with level?
					if (primary.isDocumentLevel()) {
						continue;
					}
					if (primary.isVisited()) {
						continue;
					}

					// 12/19/2017 TEST
					// Don't do primary match on invalidated annotations
					boolean hasValidationIncorrect = JavaFunctions
							.annotationHasAttributeValue(primary, "validation",
									"incorrect");

					if (hasValidationIncorrect) {
						continue;
					}

					String docname = primary.getDocumentName();

					avar.bind(primary);
					avar1.bind(primary);
					Object pclass = primary.getClassificationValue();
					cvar.bind(pclass);
					loopvar.bind("TP-FN-PRIMARY");
					boolean foundPrimaryMatch = this.constraintPacket
							.getConstraint().doTestConstraint(vars);

					if (foundPrimaryMatch) {

						if (value.toString().toLowerCase().contains("facil")) {
							mfacilcount++;
						}

						// 11/9/2015: In readmission, I want
						// to count all matches. (Should I set an option for
						// control of this feature?)
						// primary.setVisited(true);
						boolean foundSecondaryMatch = false;

						if (primary.getOverlappingAnnotationPairs() != null) {
							for (OverlappingAnnotationPair pair : primary
									.getOverlappingAnnotationPairs()) {
								Annotation secondary = pair
										.getSecondaryAnnotation();
								if (secondary.isVisited()) {
									continue;
								}

								// 11/10/2015

								avar2.bind(secondary);
								avar.bind(secondary);
								loopvar.bind("TP-FN-SECONDARY");
								if (this.constraintPacket.getConstraint()
										.doTestConstraint(vars)) {
									foundSecondaryMatch = true;

									if (!primary.getKtAnnotation()
											.getAnnotatorName().toLowerCase()
											.contains("moonstone")) {
										int x = 1;
									}

									// 11/9/2015

									// 8/16/2016 Reactivated for test...
									// secondary.setVisited(true);

									primary.setContainsClassificationMatch(true);
									secondary
											.setContainsClassificationMatch(true);
									mvs.incrementTruePositive(primary,
											secondary);
									this.cumulativeTruePositiveCount++;
									String key = getDocumentMatchedPairKey(
											docname, value, OutcomeResult.TP);
									VUtils.pushHashVector(
											this.primaryMatchedDocumentAnnotationHash,
											key, primary);
									VUtils.pushHashVector(
											this.secondaryMatchedDocumentAnnotationHash,
											key, secondary);

									// 11/6/2015: Want to count all matches
									// break;
								}
							}
						}
						if (!foundSecondaryMatch) {
							// 2/1/2016
							if (this.analysis.isAnnotationFPtoTPMatch(primary)) {
								mvs.incrementTruePositive(primary, null);
								this.cumulativeTruePositiveCount++;
								String key = getDocumentMatchedPairKey(docname,
										value, OutcomeResult.TP);
								VUtils.pushHashVector(
										this.primaryMatchedDocumentAnnotationHash,
										key, primary);
							} else {
								mvs.incrementFalseNegative(primary, null);
								this.cumulativeFalseNegativeCount++;
								String key = getDocumentMatchedPairKey(docname,
										value, OutcomeResult.FN);
								VUtils.pushHashVector(
										this.primaryMatchedDocumentAnnotationHash,
										key, primary);
								VUtils.pushHashVector(this.falseAnnotationHash,
										"FN:" + value, primary);
							}
						}
					}
				}

				for (Annotation secondary : this.analysis
						.getAllSecondarySnippetAnnotations()) {

					// 11/10/2015: If a secondary annotation overlaps another
					// annotation that matches a primary, don't count it; all
					// the secondaries are presumably part of an equivalence
					// set. (NOTE: THIS COULD UNCOUNTER FALSE POSITIVES, SINCE
					// THE
					// SECONDARY MIGHT OVERLAP A TRUE MATCH, BUT ITSELF BE
					// INCORRECT
					// AND NOT PART OF AN EQUIVALENCE SET.)

					// 8/16/2016 TEST: I can't seem to get FPs to display
					boolean checking = false;

					if (secondary.isVisited()
							|| secondary
									.overlappingSetContainsMatchedClassification()) {
						continue;
					}

					// Before 11/24/2015
					// if (secondary.isVisited()
					// || secondary.getMatchingAnnotations() != null
					// || secondary
					// .overlappingContainsMatchingAnnotations()) {
					// continue;
					// }

					Object sclass = secondary.getClassificationValue();
					String docname = secondary.getDocumentName();
					avar.bind(secondary);
					avar1.bind(secondary);
					cvar.bind(sclass);
					loopvar.bind("FP");
					boolean foundSecondaryMatch = this.constraintPacket
							.getConstraint().doTestConstraint(vars);
					if (foundSecondaryMatch) {

						if (checking) {
							int x = 1;
						}
						// 11/9/2015
						// secondary.setVisited(true);

						// 11/9/2015: All annotations were verifiedTrue before
						secondary.setVerifiedTrue(true);

						// 1/5/2016
						if (this.analysis.isAnnotationFPtoTPMatch(secondary)) {
							mvs.incrementTruePositive(null, secondary);
							this.cumulativeTruePositiveCount++;
							String key = getDocumentMatchedPairKey(docname,
									value, OutcomeResult.TP);
							VUtils.pushHashVector(
									this.secondaryMatchedDocumentAnnotationHash,
									key, secondary);
						} else {
							mvs.incrementFalsePositive(null, secondary);
							this.cumulativeFalsePositiveCount++;
							String key = getDocumentMatchedPairKey(docname,
									value, OutcomeResult.FP);
							VUtils.pushHashVector(
									this.secondaryMatchedDocumentAnnotationHash,
									key, secondary);

							// 1/14/2015
							VUtils.pushHashVector(this.falseAnnotationHash,
									"FP:" + value, secondary);

						}
					} else {
						int x = 1;
					}
				}
			}
		}
		Annotation.setVisited(this.analysis.getAllPrimarySnippetAnnotations(),
				false);
		Annotation.setVisited(
				this.analysis.getAllSecondarySnippetAnnotations(), false);

		System.out.println("Primary Facility count = " + mfacilcount);
	}

	public void clear() {
		this.matchedValueStatistics = null;
		this.alternativeValues = null;
		// this.workbenchAPIObject = null;
		this.falseNegatives = this.falsePositives = null;
		this.primaryMatchedDocumentAnnotationHash = new Hashtable();
		this.secondaryMatchedDocumentAnnotationHash = new Hashtable();
		this.cumulativeFalseNegativeCount = this.cumulativeFalsePositiveCount = this.cumulativeTrueNegativeCount = this.cumulativeTruePositiveCount = 0;
	}

	public static String getDocumentMatchedPairKey(String docname,
			String value, OutcomeResult result) {
		String key = docname + ":" + value + ":" + result;
		return key;
	}

	public int getTruePositiveCount(int row) {
		if (row == -1) {
			return this.getCumulativeTruePositiveCount();
		}
		MatchedValueStatistics mvs = this.matchedValueStatistics[row];
		if (mvs != null) {
			return mvs.getTruePositive();
		}
		return -1;
	}

	public int getTrueNegativeCount(int row) {
		if (row == -1) {
			return this.getCumulativeTrueNegativeCount();
		}
		MatchedValueStatistics mvs = this.matchedValueStatistics[row];
		if (mvs != null) {
			return mvs.getTrueNegative();
		}
		return -1;
	}

	public int getFalsePositiveCount(int row) {
		if (row == -1) {
			return this.getCumulativeFalsePositiveCount();
		}
		MatchedValueStatistics mvs = this.matchedValueStatistics[row];
		if (mvs != null) {
			return mvs.getFalsePositive();
		}
		return -1;
	}

	public int getFalseNegativeCount(int row) {
		if (row == -1) {
			return this.getCumulativeFalseNegativeCount();
		}
		MatchedValueStatistics mvs = this.matchedValueStatistics[row];
		if (mvs != null) {
			return mvs.getFalseNegative();
		}
		return -1;
	}

	public ConstraintPacket getConstraintPacket() {
		return this.constraintPacket;
	}

	public WorkbenchAPIObject getWorkbenchAPIObject() {
		return workbenchAPIObject;
	}

	public Vector<Annotation> getFalsePositives() {
		return falsePositives;
	}

	public Vector<Annotation> getFalseNegatives() {
		return falseNegatives;
	}

	public Vector getHashKey() {
		Vector key = VUtils.listify(this.getWorkbenchAPIObject(),
				this.constraintPacket.getConstraint());
		return key;
	}

	public static Vector getHashKey(WorkbenchAPIObject wao, ConstraintPacket cp) {
		Vector key = VUtils.listify(wao, cp);
		return key;
	}

	public Vector<String> getAlternativeValues() {
		return alternativeValues;
	}

	public String getFirstColumnNameAll(int row) {
		Object value = (this.alternativeValues != null ? this.alternativeValues
				.elementAt(row) : null);
		String str = "*";
		if (value instanceof annotation.Classification) {
			annotation.Classification c = (annotation.Classification) value;
			String dstr = (c != null ? c.getDisplayString() : "*");
			str = (row + 1) + ":" + dstr;
		} else if (value instanceof String) {
			str = (row + 1) + ":" + (String) value;
		}
		return str;
	}

	public int getCumulativeTruePositiveCount() {
		return cumulativeTruePositiveCount;
	}

	public int getCumulativeFalsePositiveCount() {
		return cumulativeFalsePositiveCount;
	}

	public int getCumulativeTrueNegativeCount() {
		return cumulativeTrueNegativeCount;
	}

	public int getCumulativeFalseNegativeCount() {
		return cumulativeFalseNegativeCount;
	}

	public Vector<Annotation> getAllMatchedDocumentAnnotations(String value,
			OutcomeResult result, AnnotatorType atype) {
		Vector<Annotation> allannotations = null;
		if (this.analysis.getAllDocuments() != null) {
			for (Document doc : this.analysis.getAllDocuments()) {
				Vector<Annotation> annotations = this
						.getMatchedDocumentAnnotations(doc.getName(), value,
								result, atype);
				allannotations = VUtils.append(allannotations, annotations);
			}
		}
		return allannotations;
	}

	public Vector<Annotation> getMatchedDocumentAnnotations(String docname,
			String value, OutcomeResult result, AnnotatorType atype) {
		String key = ConstraintMatch.getDocumentMatchedPairKey(docname, value,
				result);
		Vector<Annotation> annotations = null;
		if (atype.equals(AnnotatorType.primary)) {
			annotations = this.primaryMatchedDocumentAnnotationHash.get(key);

			if (annotations != null) {
				int x = 1;
				x = x;
			}

		} else {
			annotations = this.secondaryMatchedDocumentAnnotationHash.get(key);
		}
		return annotations;
	}

	public String toString() {
		String str = "<CM: ConstraintString=\""
				+ this.getConstraintPacket().getConstraint()
						.getConstraintString() + "\">";
		return str;
	}

	public MatchedValueStatistics getMatchedValueStatistics(int index) {
		return this.matchedValueStatistics[index];
	}

	// 7/6/2014: Statistics

	public double getAccuracy(int row) {
		float tp = this.getTruePositiveCount(row);
		float fp = this.getFalsePositiveCount(row);
		float tn = this.getTrueNegativeCount(row);
		float fn = this.getFalseNegativeCount(row);
		float num = tp + tn;
		float den = tp + fp + tn + fn;
		return getDividend(num, den);
	}

	public double getPPV(int row) {
		float tp = this.getTruePositiveCount(row);
		float fp = this.getFalsePositiveCount(row);
		float num = tp;
		float den = tp + fp;
		return getDividend(num, den);
	}

	public double getSensitivity(int row) {
		float tp = this.getTruePositiveCount(row);
		float fn = this.getFalseNegativeCount(row);
		float num = tp;
		float den = tp + fn;
		return getDividend(num, den);
	}

	public double getNPV(int row) {
		float tn = this.getTrueNegativeCount(row);
		float fn = this.getFalseNegativeCount(row);
		float num = tn;
		float den = tn + fn;
		return getDividend(num, den);
	}

	public double getSpecificity(int row) {
		float fp = this.getFalsePositiveCount(row);
		float tn = this.getTrueNegativeCount(row);
		float num = tn;
		float den = tn + fp;
		return getDividend(num, den);
	}

	public double getCohensKappa(int row) {
		float tp = this.getTruePositiveCount(row);
		float tn = this.getTrueNegativeCount(row);
		float fp = this.getFalsePositiveCount(row);
		float fn = this.getFalseNegativeCount(row);
		float totalobs = tp + tn + fp + fn;
		float marg4p2_pos = (tp + fp);
		float marg4p1_pos = (tp + fn);
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);
		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			E_tp = (marg4p1_pos / totalobs) * (marg4p2_pos / totalobs);
			E_tn = (marg4p1_neg / totalobs) * (marg4p2_neg / totalobs);
			Ao = (tp + tn) / totalobs;
			Ae = (E_tp + E_tn);
		}
		float num = Ao - Ae;
		float den = 1 - Ae;
		return getDividend(num, den);
	}

	public double getScottsPi(int row) {
		float tp = this.getTruePositiveCount(row);
		float tn = this.getTrueNegativeCount(row);
		float fp = this.getFalsePositiveCount(row);
		float fn = this.getFalseNegativeCount(row);
		float totalobs = tp + tn + fp + fn;
		float marg4p2_pos = (tp + fp);
		float marg4p1_pos = (tp + fn);
		float marg4p2_neg = (tn + fn);
		float marg4p1_neg = (tn + fp);

		float E_tp = 0;
		float E_tn = 0;
		float Ae = 0;
		float Ao = 0;
		if (totalobs > 0) {
			E_tp = (marg4p1_pos + marg4p2_pos) / (2 * totalobs);
			E_tn = (marg4p1_neg + marg4p2_neg) / (2 * totalobs);
			Ao = (tp + tn) / totalobs;
			Ae = (E_tp + E_tn);
		}
		float num = Ao - Ae;
		float den = 1 - Ae;
		return getDividend(num, den);
	}

	public double getFmeasure(int row) {
		float tp = this.getTruePositiveCount(row);
		float tn = this.getTrueNegativeCount(row);
		float fp = this.getFalsePositiveCount(row);
		float fn = this.getFalseNegativeCount(row);
		float Bwt = 1;
		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);
		return getDividend(num, den);
	}

	private static double getDividend(double numerator, double denominator) {
		double result = 0;
		if (denominator > 0) {
			result = numerator / denominator;
		}
		return result;
	}

	public boolean isApplyExactMatch() {
		return applyExactMatch;
	}

	public void setApplyExactMatch(boolean applyExactMatch) {
		this.applyExactMatch = applyExactMatch;
	}

	// public int getAnnotationClassificationRow(Annotation annotation) {
	// if (annotation != null && annotation.getClassificationValue() != null) {
	// return getClassificationRow(annotation.getClassificationValue());
	// }
	// return -1;
	// }

	// public int getClassificationRow(String value) {
	// Integer row = this.valueRowMap.get(value);
	// return (row != null ? row.intValue() : -1);
	// }

	public static ConstraintMatch getConstraintMatchNamed(
			Vector<ConstraintMatch> cms, String name) {
		if (cms != null && name != null) {
			for (ConstraintMatch cm : cms) {
				if (name.equals(cm.getConstraintPacket().getName())) {
					return cm;
				}
			}
		}
		return null;
	}

	// 12/29/2015 Hacks...
	private boolean valueIsRelevant(String value) {
		if (value.contains("model")) {
			return false;
		}
		return true;
	}

	public Vector<Annotation> getFalsePositives(String value) {
		String key = "FP:" + value;
		return this.falseAnnotationHash.get(key);
	}

	public Vector<Annotation> getFalseNegatives(String value) {
		String key = "FN:" + value;
		return this.falseAnnotationHash.get(key);
	}

	// 2/16/2018: Long mention spreadsheet, match hidden, larger context, as
	// requested by Salomeh
	public void writeSpreadsheetFileMatchHidden() {
		StringBuffer sbNoMatch = new StringBuffer();
		sbNoMatch
				.append("ROWID|CLASSIFICATION|CORRECT?|MENTION|CONTEXT|TIUDocumentSID\n");
		StringBuffer sbMatch = new StringBuffer();
		sbMatch.append("ROWID|CLASSIFICATION|MATCH|MENTION|CONTEXT|TIUDocumentSID|\n");
		Vector<ConstraintMatchPrintable> cps = new Vector(0);

		addToSpreadsheet(cps, this.primaryMatchedDocumentAnnotationHash, 100);
		addToSpreadsheet(cps, this.secondaryMatchedDocumentAnnotationHash, 100);
		Vector<ConstraintMatchPrintable> fcps = new Vector(0);
		for (ConstraintMatchPrintable cp : cps) {
			if ("FP".equals(cp.matchType) || "FN".equals(cp.matchType)) {
				fcps.add(cp);
			}
		}
		cps = fcps;

		Collections.sort(cps, new ConstraintMatchPrintableDocumentNameSorter());
		Collections.sort(cps, new ConstraintMatchPrintableTargetSorter());
		
		int rowid = 0;
		for (ConstraintMatchPrintable cp : cps) {
			String str = rowid + "|" + cp.target + "|     |" + cp.mention + "|" + cp.sentence
					+ "|" + cp.documentName + "|\n";
			rowid++;
			sbNoMatch.append(str);
		}
		String fname = "C:\\Users\\VHASLCChrisL1\\Desktop\\MentionSpreadsheetMatchHidden";
		FUtils.writeFile(fname, sbNoMatch.toString());

		rowid = 0;
		for (ConstraintMatchPrintable cp : cps) {
			String str = rowid + "|" + cp.target + "|" + cp.matchType + "|" + cp.mention
					+ "|" + cp.sentence + "|" + cp.documentName + "|\n";
			rowid++;
			sbMatch.append(str);
		}
		fname = "C:\\Users\\VHASLCChrisL1\\Desktop\\MentionSpreadsheetMatchVisible";
		FUtils.writeFile(fname, sbMatch.toString());
	}

	public void addToSpreadsheet(Vector<ConstraintMatchPrintable> cps,
			Hashtable<String, Vector<Annotation>> hash, int sbuflen) {
		for (Object o : HUtils.getKeys(hash)) {
			String key = (String) o;
			String[] strs = key.split(":");
			String target = strs[1];
			String mtype = strs[2];
			Vector<Annotation> annotations = hash.get(key);
			for (Annotation a : annotations) {
				ConstraintMatchPrintable cp = new ConstraintMatchPrintable(a,
						mtype, sbuflen);
				cps.add(cp);
			}
		}
	}

	private class ConstraintMatchPrintable {
		private String target = null;
		private String matchType = null;
		private String mention = null;
		private String sentence = null;
		private String documentName = null;

		ConstraintMatchPrintable(Annotation a, String mtype, int sbuflen) {
			this.target = a.getClassificationValue().toString();
			this.matchType = mtype;
			String dname = a.getAnalysis().getNoncompressedDocumentName(
					a.getDocumentName());
			this.documentName = dname;
			this.mention = a.getText();
			int astart = a.getStart();
			int aend = a.getEnd();
			String dtext = a.getDocument().getText();
			int dlen = dtext.length();
			int sstart = (astart - sbuflen >= 0 ? astart - sbuflen : 0);
			int send = (aend + sbuflen < dlen ? aend + sbuflen : dlen - 1);

			if (send < 0 || aend < 0) {
				int x = 1;
			}
			String sstr = dtext.substring(sstart, astart);
			sstr += " [ ";
			sstr += dtext.substring(astart, aend);
			sstr += " ] ";
			if (aend < send - 2) {
				sstr += dtext.substring(aend + 1, send);
			}

			sstr = StrUtils.replaceChars(sstr, '\n', ' ');
			sstr = "..." + sstr + "...";
			this.sentence = sstr;
		}
	}

	public static class ConstraintMatchPrintableTargetSorter implements
			Comparator {
		public int compare(Object o1, Object o2) {
			ConstraintMatchPrintable p1 = (ConstraintMatchPrintable) o1;
			ConstraintMatchPrintable p2 = (ConstraintMatchPrintable) o2;
			return p1.target.compareTo(p2.target);
		}
	}

	public static class ConstraintMatchPrintableMatchTypeSorter implements
			Comparator {
		public int compare(Object o1, Object o2) {
			ConstraintMatchPrintable p1 = (ConstraintMatchPrintable) o1;
			ConstraintMatchPrintable p2 = (ConstraintMatchPrintable) o2;
			return p1.matchType.compareTo(p2.matchType);
		}
	}

	public static class ConstraintMatchPrintableMentionSorter implements
			Comparator {
		public int compare(Object o1, Object o2) {
			ConstraintMatchPrintable p1 = (ConstraintMatchPrintable) o1;
			ConstraintMatchPrintable p2 = (ConstraintMatchPrintable) o2;
			return p1.mention.compareTo(p2.mention);
		}
	}

	public static class ConstraintMatchPrintableDocumentNameSorter implements
			Comparator {
		public int compare(Object o1, Object o2) {
			ConstraintMatchPrintable p1 = (ConstraintMatchPrintable) o1;
			ConstraintMatchPrintable p2 = (ConstraintMatchPrintable) o2;
			return p1.documentName.compareTo(p2.documentName);
		}
	}

}
