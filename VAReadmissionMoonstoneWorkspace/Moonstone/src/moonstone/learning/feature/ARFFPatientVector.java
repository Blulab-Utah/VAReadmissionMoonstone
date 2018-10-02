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
package moonstone.learning.feature;

import tsl.documentanalysis.document.Document;
import tsl.utilities.VUtils;
import moonstone.annotation.Annotation;
import moonstone.io.readmission.ReadmissionPatientResults;
import moonstone.javafunction.JavaFunctions;

public class ARFFPatientVector {

	private ReadmissionPatientResults results = null;
	private String pname = null;
	private int[] featureCounts = null;
	private int featureArraySize = 0;
	private boolean containsFeatures = false;
	private int numberOfSubFeatures = 0;

	public static int NumDiscretizedDays = ARFFPatientVectorVariable.DiscretizedDayStrings.length;
	public static int NumPolarities = ARFFPatientVectorVariable.PolarityStrings.length;
	public static int NumDocumentTypes = ARFFPatientVectorVariable.DocumentTypeStrings.length;

	// 7/31/2017
	public ARFFPatientVector(String pname) {
		this.pname = pname;
		int fcount = FeatureSet.CurrentFeatureSet.featureDefinitionVector
				.getNumberOfFeatures();
		this.numberOfSubFeatures = NumDiscretizedDays * NumDocumentTypes
				* NumPolarities;
		this.featureArraySize = fcount * this.numberOfSubFeatures;
		this.featureCounts = new int[this.featureArraySize];
	}

	public ARFFPatientVector(ReadmissionPatientResults results, String pname) {
		this.results = results;
		this.pname = pname;
		int fcount = results.processor.featureSet.featureDefinitionVector
				.getNumberOfFeatures();
		this.numberOfSubFeatures = NumDiscretizedDays * NumDocumentTypes
				* NumPolarities;
		this.featureArraySize = fcount * this.numberOfSubFeatures;
		this.featureCounts = new int[this.featureArraySize];
		this.results.processor.ARFFPatientVectorHash.put(pname, this);
	}

	public void addFeature(Annotation annotation, String content) {
		// Before 5/15/2017
		FeatureDefinitionVector fdv = this.results.processor.featureSet.featureDefinitionVector;
		Feature feature = fdv.getFeature(content);
		if (feature == null) {
			System.out.println("\n\tARFFPatientVector:  Not storing content "
					+ content);
			return;
		}
		int dayIndex = getDayOffset(annotation);
		int polarityIndex = getPolarityOffset(annotation);
		int typeIndex = getDocumentTypeOffset(annotation);
		int fcount = fdv.getNumberOfFeatures();
		int findex = feature.getIndex();
		int baseFeatureOffset = this.numberOfSubFeatures * findex;
		// inset = ((NumDayIndices * NumTypeIndices) * dayIndex) +
		// (NumTypeIndices * polarIndex) + polarIndex;

		// Before 6/1/2017
		// int inset = (4 * dayIndex) + (2 * typeIndex) + polarityIndex;
		int inset = baseFeatureOffset + dayIndex;
		// int featureIndex = baseFeatureOffset + inset;
		int featureIndex = inset;
		if (featureIndex <= this.featureArraySize) {
			this.featureCounts[featureIndex]++;
			this.containsFeatures = true;
		} else {
			System.out.println("\n\tARFFPatientVector:  Not storing content "
					+ content);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.featureArraySize; i++) {
			int fcount = this.featureCounts[i];
			String fcountstr = String.valueOf(fcount);
			sb.append(fcountstr);
			if (i < this.featureArraySize - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	public String toRegularizedString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < this.featureArraySize; i++) {
			int fcount = this.featureCounts[i];
			int freg = getRegularizedFeatureCount(fcount, 5);
			String fcountstr = String.valueOf(freg);
			sb.append(fcountstr);
			if (i < this.featureArraySize - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public static int getDocumentTypeOffset(Annotation annotation) {
		int dtype = 1;
		Document d = annotation.getDocument();
		String lcf = d.getName().toLowerCase();
		if (lcf.contains("social") || lcf.contains("sws")
				|| lcf.contains("mental") || lcf.contains("psych")) {
			dtype = 0;
		}
		return dtype;
	}

	public static int getPolarityOffset(Annotation annotation) {
		int polarity = JavaFunctions.isAffirmed(annotation) ? 0 : 1;

		// 6/1/2017
		polarity = 0;

		return polarity;
	}

	public static int getDayOffset_JUST_TWO(Annotation annotation) {
		int val = 0;
		Document d = annotation.getDocument();
		if (d != null) {
			int diff = d.getAdmitDictationDayDifference();
			if (diff <= 30) {
				val = 0;
			} else {
				val = 1;
			}
		}
		return val;
	}
	
	public static int getDayOffset(Annotation annotation) {
		int val = 3;
		Document d = annotation.getDocument();
		if (d != null) {
			int diff = d.getAdmitDictationDayDifference();
			if (diff < 7) {
				val = 0;
			} else if (diff < 30) {
				val = 1;
			} else if (diff < 60){
				val = 2;
			}
		}
		return val;
	}

	public static int getDayOffset_BEFORE_6_1_2017(Annotation annotation) {
		int val = 5;
		Document d = annotation.getDocument();
		if (d != null) {
			int diff = d.getAdmitDictationDayDifference();
			if (diff < 0) {
				val = 0;
			} else if (diff < 7) {
				val = 1;
			} else if (diff < 30) {
				val = 2;
			} else if (diff < 90) {
				val = 3;
			} else if (diff < 180) {
				val = 4;
			} else {
				val = 5;
			}
		}
		return val;
	}

	public boolean isContainsFeatures() {
		return containsFeatures;
	}

	public static int getRegularizedFeatureCount(int count, int inc) {
		int value = 0;
		int last = 0;
		if (count == 0) {
			value = 0;
		} else if (count > 0 && count <= (inc * 1)) {
			value = 1;
		} else if (count > (inc * 1) && count <= (inc * 2)) {
			value = 2;
		} else if (count > (inc * 2) && count <= (inc * 3)) {
			value = 3;
		} else if (count > (inc * 3)) {
			value = 4;
		}
		return value;
	}
	
	public static int getRegularizedFeatureCount_ORIG(int count) {
		int value = 0;
		int x = 1;
		if (count == 0) {
			value = 0;
		} else if (count > 0 && count <= 4) {
			value = 1;
		} else if (count > 4 && count <= 8) {
			value = 2;
		} else if (count > 8 && count <= 12) {
			value = 3;
		} else if (count > 12) {
			value = 4;
		}
		return value;
	}

	public void setFeatureCount(int i, int count) {
		if (i >= 0 && i < this.featureArraySize) {
			this.featureCounts[i] = count;
		}
	}

	public int[] getFeatureCounts() {
		return featureCounts;
	}

	public int getFeatureArraySize() {
		return featureArraySize;
	}

	public int getNumberOfSubFeatures() {
		return numberOfSubFeatures;
	}
	
	

}
