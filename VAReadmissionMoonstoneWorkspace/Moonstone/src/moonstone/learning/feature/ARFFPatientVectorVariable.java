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

import java.util.Comparator;

import tsl.utilities.VUtils;
import moonstone.annotation.Annotation;
import moonstone.io.readmission.ReadmissionPatientResults;

public class ARFFPatientVectorVariable {

	private ReadmissionPatientResults results = null;
	private String patientName = null;
	private int pid = 0;
	private ARFFPatientVector patientVector = null;
	private String variable = null;
	private String answer = null;

	 public static String[] DiscretizedDayStrings = new String[] { 
		 "day7", "day30", "day60", "older" };
	
//	public static String[] DiscretizedDayStrings = new String[] { "dayLTE30",
//			"dayGT30"};
	public static String[] PolarityStrings = new String[] { "na"};
	public static String[] DocumentTypeStrings = new String[] { "na" };

	// Before 6/1/2017
	// public static String[] DiscretizedDayStrings = new String[] { "day0",
	// "day7", "day30", "day60", "day90", "day180" };
	// public static String[] PolarityStrings = new String[] { "affirmed",
	// "negated" };
	// public static String[] DocumentTypeStrings = new String[] { "mental",
	// "other" };
	
public static String HousingSituationEnumeration = "   @ATTRIBUTE ehostclass "
	+ "{\"lives at home/not homeless\", "
	+ "\"homeless/marginally housed/temporarily housed/at risk of homelessness\", "
	+ "\"lives in a facility\", "
	+ "\"lives in a permanent single room occupancy\", \"no mention\"}\n\n@DATA\n";

public static String LivingAloneEnumeration = "	@ATTRIBUTE ehostclass "
	+ "{\"does not live alone\", \"living alone\", \"no mention\"}\n\n@DATA\n";

public static String SocialSupportEnumeration = "   @ATTRIBUTE ehostclass "
	+ "{\"no social support\", " + "\"has social support\", "
	+ "\"has access to community services\", \"no mention\"}\n\n@DATA\n";

// Before 8/16/2017
//	public static String HousingSituationEnumeration = "   @ATTRIBUTE ehostclass "
//			+ "{\"lives at home/not homeless\", "
//			+ "\"homeless/marginally housed/temporarily housed/at risk of homelessness\", "
//			+ "\"lives in a facility\", "
//			+ "\"lives in a permanent single room occupancy\"}\n\n@DATA\n";
//
//	public static String LivingAloneEnumeration = "	@ATTRIBUTE ehostclass "
//			+ "{\"does not live alone\", " + "\"living alone\"}\n\n@DATA\n";
//
//	public static String SocialSupportEnumeration = "   @ATTRIBUTE ehostclass "
//			+ "{\"no social support\", " + "\"has social support\", "
//			+ "\"has access to community services\"}\n\n@DATA\n";
	
	// 7/31/2017
	public ARFFPatientVectorVariable(String pname, String variable, String answer) {
		this.patientName = pname;
		this.pid = new Integer(pname).intValue();
		this.variable = variable;
		this.answer = answer;
		this.patientVector = new ARFFPatientVector(pname);
	}
	
	public ARFFPatientVectorVariable(ReadmissionPatientResults results,
			String pname, String variable, String answer) {
		this.results = results;
		this.patientName = pname;
		this.pid = new Integer(pname).intValue();
		this.variable = variable;
		this.answer = answer;

		if (results != null) {
			this.patientVector = results.processor.ARFFPatientVectorHash.get(pname);
		}
		if (this.patientVector == null) {
			this.patientVector = new ARFFPatientVector(results, pname);
		} else {
			int x = 1;
		}
		String key = variable + ":" + pname;
		results.processor.ARFFPatientVectorVariableHash.put(key, this);
		VUtils.pushHashVector(
				results.processor.ARFFVariablePatientVectorHashHash, variable,
				this);
	}

	public void addFeature(Annotation annotation, String content) {
		this.patientVector.addFeature(annotation, content);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		boolean useanswer = true;
		
		if (this.results != null) {
			useanswer = this.results.processor.ARFFWithEHostAnswer;
		}
		boolean usename = !useanswer;
		
		// 6/15/2017:  Just one result file, with both patient name and answer.
		useanswer = usename = true;
		
		String pnamestr = (usename ? this.patientName : "?");
		String answerstr = (useanswer ? ("\"" + this.answer + "\"") : "?");
		
		String pvectorstr = this.patientVector.toString();
		sb.append(pnamestr + ",");
		sb.append(pvectorstr + ",");
		sb.append(answerstr);
		return sb.toString();
	}
	
	public String toRegularizedFeatureStringWithAnswer(boolean useAnswer) {
		StringBuffer sb = new StringBuffer();
		String pvectorstr = this.patientVector.toRegularizedString();
		sb.append(this.patientName + ",");
		sb.append(pvectorstr + ",");
		String answer = (useAnswer ? this.answer : "?");
		sb.append(answer);
		return sb.toString();
	}

	public static String getARFFHeader(FeatureDefinitionVector fdv,
			String variable) {
		int x = 1;
		StringBuffer sb = new StringBuffer();
		sb.append("@RELATION " + variable + "\n");
		sb.append("	@ATTRIBUTE patientid NUMERIC\n");
		int absindex = 0;
		for (int i = 0; i < fdv.getNumberOfFeatures(); i++) {
			Feature f = fdv.getFeature(i);
			int findex = f.getIndex();
			for (String day : DiscretizedDayStrings) {
				for (String dtype : DocumentTypeStrings) {
					for (String ptype : PolarityStrings) {
						String aname = f.getContent() + "[" + absindex++ + "]_"
								+ day + "_" + dtype + "_" + ptype;
						sb.append("	@ATTRIBUTE " + aname + " NUMERIC\n");
					}
				}
			}
		}
		if ("HOUSING_SITUATION".equals(variable)) {
			sb.append(HousingSituationEnumeration);
		} else if ("LIVING_ALONE".equals(variable)) {
			sb.append(LivingAloneEnumeration);
		} else if ("SOCIAL_SUPPORT".equals(variable)) {
			sb.append(SocialSupportEnumeration);
		}
		return sb.toString();
	}

	public static class PatientIDSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ARFFPatientVectorVariable pvv1 = (ARFFPatientVectorVariable) o1;
			ARFFPatientVectorVariable pvv2 = (ARFFPatientVectorVariable) o2;
			return Integer.compare(pvv1.pid, pvv2.pid);
		}
	}

	public ARFFPatientVector getPatientVector() {
		return patientVector;
	}

	public String getPatientName() {
		return patientName;
	}

	public String getAnswer() {
		return answer;
	}
	

}
