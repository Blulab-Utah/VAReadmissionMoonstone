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
package workbench.api.input.ehost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Hashtable;

import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import workbench.api.constraint.ConstraintMatch;

public class MoonstoneEHostFinalAdjudication {

	private static String dirname = "C:\\Users\\VHASLCChrisL1\\Desktop\\READMISSION";
	private static String VisibleMismatchFileName = "MentionSpreadsheetMatchVisible_4_23_2018";
	private static String InvisibleMismatchFileName = "Completed_Mention_Spreadsheet_05.04.18.txt";
	private static String MoonstoneErrorFileName = "MoonstoneMatchErrorFile";
	private static String UnadjudicatedMatchFileName = "UnadjudicatedMatchFile.txt";
	private static String AdjustedStatisticResultsFileName = "AdjustedStatisticResults.txt";

	public static void main(String[] args) {
		getMoonstoneEHostFinalAdjudicationCounts();
	}

	public static void getMoonstoneEHostFinalAdjudicationCounts() {
		Hashtable<String, Integer> MoonstoneCorrectHash = new Hashtable();
		Hashtable<String, Integer> ClassFalsePositiveHash = new Hashtable();
		Hashtable<String, Integer> ClassFalseNegativeHash = new Hashtable();
		Hashtable<String, Integer> ClassCorrectedFalsePositiveHash = new Hashtable();
		Hashtable<String, Integer> ClassCorrectedFalseNegativeHash = new Hashtable();
		Hashtable<String, Integer> UnadjustedClassCountHash = new Hashtable();

		String vfs = dirname + File.separatorChar + VisibleMismatchFileName;
		String ivfs = dirname + File.separatorChar + InvisibleMismatchFileName;
		String matchfilename = dirname + File.separatorChar
				+ MoonstoneErrorFileName;
		String unadjudicatedMatchFilename = dirname + File.separatorChar
				+ UnadjudicatedMatchFileName;
		String statResultsFilename = dirname + File.separatorChar + AdjustedStatisticResultsFileName;

		File umf = new File(unadjudicatedMatchFilename);

		if (umf.exists()) {
			try {
				BufferedReader umin = new BufferedReader(new FileReader(umf));
				String line = null;
				while ((line = umin.readLine()) != null) {
					if (line.length() < 4) {
						continue;
					}
					String[] strs = line.split(",");
					if (strs.length != 5) {
						int x = 1;
					}
					String cname = strs[0];
					int tpcount = Integer.valueOf(strs[1]);
					int fpcount = Integer.valueOf(strs[2]);
					int tncount = Integer.valueOf(strs[3]);
					int fncount = Integer.valueOf(strs[4]);
					UnadjustedClassCountHash.put(cname + ":TP", new Integer(
							tpcount));
					UnadjustedClassCountHash.put(cname + ":FP", new Integer(
							fpcount));
					UnadjustedClassCountHash.put(cname + ":TN", new Integer(
							tncount));
					UnadjustedClassCountHash.put(cname + ":FN", new Integer(
							fncount));
				}
				umin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File vf = new File(vfs);
		File ivf = new File(ivfs);

		Hashtable<String, Integer> AllAnnotationHash = new Hashtable();
		StringBuffer MoonstoneErrorSB = new StringBuffer();
		Hashtable<String, String> MoonstoneMentionErrorHash = new Hashtable();
		try {
			if (vf.exists() && ivf.exists()) {
				BufferedReader vin = new BufferedReader(new FileReader(vf));
				BufferedReader ivin = new BufferedReader(new FileReader(ivf));
				String vline = null;
				String ivline = null;
				while (true) {
					vline = vin.readLine();
					ivline = ivin.readLine();
					if (vline == null || ivline == null) {
						break;
					}

					String[] strs1 = vline.split("\\t");
					String[] strs2 = ivline.split("\t");
					if (strs1[0].toLowerCase().contains("row")) {
						continue;
					}
					String rowstr = strs1[0];
					String classification = strs1[1];
					String matchtype = strs1[2];
					String mention = strs2[3];
					String context = strs2[4];
					String adjstr = strs2[2].toLowerCase();
					int adjudication = ("yes".equals(adjstr) ? 1 : 0);
					int MoonstoneCorrect = 0;
					boolean isFP = false;
					if ("FP".equals(matchtype)) {
						isFP = true;
						MoonstoneCorrect = adjudication;
						HUtils.incrementCount(ClassFalsePositiveHash,
								classification);
					} else {
						MoonstoneCorrect = (adjudication == 1 ? 0 : 1);
						HUtils.incrementCount(ClassFalseNegativeHash,
								classification);
					}
					HUtils.incrementCount(AllAnnotationHash, classification);
					if (MoonstoneCorrect == 1) {
						HUtils.incrementCount(MoonstoneCorrectHash,
								classification);
						if (isFP) {
							HUtils.incrementCount(
									ClassCorrectedFalsePositiveHash,
									classification);
						} else {
							HUtils.incrementCount(
									ClassCorrectedFalseNegativeHash,
									classification);
						}
					} else {
						int existing = HUtils.getCount(MoonstoneCorrectHash,
								classification);
						if (existing == 0) {
							HUtils.setCount(MoonstoneCorrectHash,
									classification, 0);
						}
						if (MoonstoneMentionErrorHash.get(mention) == null) {
							MoonstoneMentionErrorHash.put(mention, mention);
							String line = classification + " (" + matchtype
									+ ") \" MENTION=" + mention
									+ "\" CONTEXT= \"" + context;
							MoonstoneErrorSB.append(line + "\n\n");
						}
					}
				}
				vin.close();
				ivin.close();
				float allTotals = 0;
				float allMoonstoneValidations = 0;
				int allCorrectedFPCount = 0;
				int allCorrectedFNCount = 0;
				String outstr = "";
				for (Object key : HUtils.getKeys(MoonstoneCorrectHash)) {
					String classification = (String) key;
					int fpcount = HUtils.getCount(ClassFalsePositiveHash,
							classification);
					int fncount = HUtils.getCount(ClassFalseNegativeHash,
							classification);

					int cfpcount = HUtils.getCount(
							ClassCorrectedFalsePositiveHash, classification);
					int cfncount = HUtils.getCount(
							ClassCorrectedFalseNegativeHash, classification);

					allCorrectedFPCount += cfpcount;
					allCorrectedFNCount += cfncount;

					int value = HUtils.getCount(MoonstoneCorrectHash, key);
					allMoonstoneValidations += value;
					int total = HUtils.getCount(AllAnnotationHash, key);
					allTotals += total;
					float improvement = (float) value / (float) total;

					int unadjustedTP = 0;
					int unadjustedFP = 0;
					int unadjustedTN = 0;
					int unadjustedFN = 0;

					if (UnadjustedClassCountHash.get(classification + ":TP") != null) {
						unadjustedTP = UnadjustedClassCountHash
								.get(classification + ":TP");
					}
					if (UnadjustedClassCountHash.get(classification + ":FP") != null) {
						unadjustedFP = UnadjustedClassCountHash
								.get(classification + ":FP");
					}
					if (UnadjustedClassCountHash.get(classification + ":TN") != null) {
						unadjustedTN = UnadjustedClassCountHash
								.get(classification + ":TN");
					}
					if (UnadjustedClassCountHash.get(classification + ":FN") != null) {
						unadjustedFN = UnadjustedClassCountHash
								.get(classification + ":FN");
					}

					int adjustedTP = unadjustedTP + cfpcount;
					int adjustedFP = unadjustedFP - cfpcount;
					int adjustedTN = unadjustedTN + cfncount;
					int adjustedFN = unadjustedFN - cfncount;
					
					String ups = getPrecision(unadjustedTP, unadjustedFP);
					String urs = getRecall(unadjustedTP, unadjustedFN);
					String unpv = getNPV(unadjustedTN, unadjustedFN);
					String uppv = getPPV(unadjustedTP, unadjustedFP);
					String uacc = getAccuracy(unadjustedTP, unadjustedFP, unadjustedTN, unadjustedFN);
					String ufms = getFmeasure(unadjustedTP, unadjustedFP,
							unadjustedTN, unadjustedFN);
					
					String aps = getPrecision(adjustedTP, adjustedFP);
					String ars = getRecall(adjustedTP, adjustedFN);
					String anpv = getNPV(adjustedTN, adjustedFN);
					String appv = getPPV(adjustedTP, adjustedFP);
					String aacc = getAccuracy(adjustedTP, adjustedFP, adjustedTN, adjustedFN);
					String afms = getFmeasure(adjustedTP, adjustedFP,
							adjustedTN, adjustedFN);

					outstr += "Classification=" + classification + "\n"
							+ "\tAdjustedTP=" + adjustedTP + "\n"
							+ "\tAdjustedFP=" + adjustedFP + "\n"
							+ "\tAdjustedFN=" + adjustedFN + "\n\n"
							
							+ "\tAdjustedPrecision=" + aps + "\n"
							+ "\tAdjustedRecall=" + ars + "\n"
							+ "\tAdjustedNPV=" + anpv + "\n"
							+ "\tAdjustedPPV=" + appv + "\n"
							+ "\tAdjustedAccuracy=" + aacc + "\n"
							+ "\tAdjustedFScore=" + afms + "\n\n";
				}
				float percentMoonstoneValidation = allMoonstoneValidations
						/ allTotals;
				outstr += "TotalCorrectedFPs=" + allCorrectedFPCount + "\n"
						+ "TotalCorrectedFNs=" + allCorrectedFNCount + "\n"
						+ "PercentMoonstoneValidation="
						+ percentMoonstoneValidation + "\n\n";
				
				FUtils.writeFile(statResultsFilename, outstr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getRecall(float tp, float fn) {
		float den = tp + fn;
		float recall = (den > 0 ? tp / den : 0);
		return convertFloatToString(recall);
	}

	public static String getPrecision(float tp, float fp) {
		float den = tp + fp;
		float precision = (den > 0 ? tp / den : 0);
		return convertFloatToString(precision);
	}

	public static String getFmeasure(float tp, float fp, float tn, float fn) {

		/* F=(1+B^2)*recall*precision/ B^2*precision + recall */
		float Bwt = 1;
		/*
		 * B weight of recall vs precsn, B>1,or<1, means weight recall, orprecn,
		 * more
		 */

		float recall = 0;
		float den = tp + fn;
		if (den > 0) {
			recall = tp / den;
		}
		/* else let recall be 0 */

		float precision = 0;
		den = tp + fp;
		if (den > 0) {
			precision = tp / den;
		}
		/* else let precsn be 0 */

		float num = (1 + Bwt * Bwt) * recall * precision;
		den = (Bwt * Bwt * precision + recall);

		float fm = (den > 0 ? num / den : 0);

		return convertFloatToString(fm);
	}
	
	public static String getAccuracy(float tp, float fp, float tn, float fn) {
		float num = tp + tn;
		float den = tp + fp + tn + fn;
		return getResultString(num, den);
	}

	public static String getPPV(float tp, float fp) {
		float num = tp;
		float den = tp + fp;
		return getResultString(num, den);
	}

	public static String getSensitivity(float tp, float fn) {
		float num = tp;
		float den = tp + fn;
		return getResultString(num, den);
	}

	public static String getNPV(float tn, float fn) {
		float num = tn;
		float den = tn + fn;
		return getResultString(num, den);
	}

	public static String getSpecificity(float fp, float tn) {
		float num = tn;
		float den = tn + fp;
		return getResultString(num, den);
	}

	private static String getResultString(float numerator, float denominator) {
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

	private static String convertFloatToString(float f) {
		String resultString = String.valueOf(f);
		if (resultString.length() > 4) {
			resultString = resultString.substring(0, 4);
		}
		return resultString;
	}

}
