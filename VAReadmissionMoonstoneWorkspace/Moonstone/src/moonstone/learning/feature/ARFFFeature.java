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

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.io.readmission.ReadmissionPatientResults;

public class ARFFFeature {

	// 8/17/2017:  Apparently this class is not used...
	
//	private ReadmissionPatientResults results = null;
//	private int patientID = 0;
//	private int MoonstoneFeatureID = 0;
//	private int numberOfDays = 0;
//	private String polarity = "affirmed";
//	private String docType = "other";
//	private String eHOSTAnswer = null;

	public static String[] ReadmissionVariables = { "HOUSING_SITUATION",
			"LIVING_ALONE", "SOCIAL_SUPPORT" };

//	public static String HousingSituationHeader = "@RELATION HOUSING_SITUATION\n"
//			+ "	@ATTRIBUTE patientid NUMERIC\n   @ATTRIBUTE moonstone-feature  NUMERIC\n"
//			+ "   @ATTRIBUTE number-of-days  NUMERIC\n"
//			+ "   @ATTRIBUTE polarity {affirmed, negated}\n"
//			+ "   @ATTRIBUTE doctype {mental, other}\n"
//			+ "   @ATTRIBUTE ehostclass "
//			+ "{\"homeless/marginally housed/temporarily housed/at risk of homelessness\", "
//			+ "\"lives at home/not homeless\", "
//			+ "\"lives in a facility\", "
//			+ "\"lives in a permanent single room occupancy\"}\n\n@DATA\n";
//
//	public static String LivingAloneHeader = "@RELATION LIVING_ALONE\n"
//			+ "	@ATTRIBUTE patientid NUMERIC\n   @ATTRIBUTE moonstone-feature  NUMERIC\n"
//			+ "   @ATTRIBUTE number-of-days  NUMERIC\n"
//			+ "   @ATTRIBUTE polarity {affirmed, negated}\n"
//			+ "   @ATTRIBUTE doctype {mental, other}\n"
//			+ "   @ATTRIBUTE ehostclass " + "{\"does not live alone\", "
//			+ "\"living alone\"}\n\n@DATA\n";
//
//	public static String SocialSupportHeader = "@RELATION SOCIAL_SUPPORT\n"
//			+ "	@ATTRIBUTE patientid NUMERIC\n   @ATTRIBUTE moonstone-feature  NUMERIC\n"
//			+ "   @ATTRIBUTE number-of-days  NUMERIC\n"
//			+ "   @ATTRIBUTE polarity {affirmed, negated}\n"
//			+ "   @ATTRIBUTE doctype {mental, other}\n"
//			+ "   @ATTRIBUTE ehostclass "
//			+ "{\"has access to community services\", "
//			+ "\"has social support\", " + "\"no social support\"}\n\n@DATA\n";
//
//	public ARFFFeature(ReadmissionPatientResults results, int pid,
//			String feature, String polarity, int days, String dtype,
//			String answer) {
//		this.results = results;
//		this.patientID = pid;
//		Feature f = results.processor.featureSet.featureDefinitionVector
//				.addFeature(feature);
////		Feature f = results.processor.featureSet.featureDefinitionVector
////				.getFeature(feature);
//		this.MoonstoneFeatureID = f.getIndex();
//		this.numberOfDays = days;
//		this.polarity = polarity;
//		this.docType = dtype;
//		this.eHOSTAnswer = answer;
//	}
//
//	public String toString() {
//		int x = 1;
//		String ehoststr = "";
//		if (this.eHOSTAnswer != null) {
//			ehoststr = "\"" + this.eHOSTAnswer + "\"";
//		} else {
//			ehoststr = "?";
//		}
//		String pidstr = (this.patientID > 0 ? Integer.toString(this.patientID)
//				: "?");
//		String str = pidstr + ", " + this.MoonstoneFeatureID + ", "
//				+ this.numberOfDays + ", " + this.polarity + ", "
//				+ this.docType + ", " + ehoststr;
//		return str;
//	}
//	
//	public static void gatherPatientARFFVectors(
//			ReadmissionPatientResults results, Hashtable<String, Vector<ARFFFeature>> phash) {
//		FeatureDefinitionVector fdv = results.processor.featureSet.featureDefinitionVector;
//		for (Enumeration<String> e = phash.keys(); e.hasMoreElements();) {
//			String pname = e.nextElement();
//			Vector<ARFFFeature> features = phash.get(pname);
//			Collections.sort(features, new ARFFFeatureIndexSorter());
//			for (int i = 0; i < fdv.getNumberOfFeatures(); i++) {
//				
//			}
//			
//		}
//		
//	}
//	
//	public static class ARFFFeatureIndexSorter implements Comparator {
//		public int compare(Object o1, Object o2) {
//			ARFFFeature a1 = (ARFFFeature) o1;
//			ARFFFeature a2 = (ARFFFeature) o2;
//			if (a1.MoonstoneFeatureID < a2.MoonstoneFeatureID) {
//				return -1;
//			}
//			if (a1.MoonstoneFeatureID > a2.MoonstoneFeatureID) {
//				return 1;
//			}
//			return 0;
//		}
//	}

}