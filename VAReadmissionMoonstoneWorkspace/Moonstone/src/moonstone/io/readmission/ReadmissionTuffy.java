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
package moonstone.io.readmission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.expression.term.constant.Constant;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;

public class ReadmissionTuffy {

	private Hashtable<String, Boolean> TuffyTargetStatementTruthHash = new Hashtable();
	private Hashtable<String, Boolean> EHostTargetTruthHash = new Hashtable();
	private Hashtable<String, Integer> tuffyEHostMatchHash = new Hashtable();
	private Hashtable<String, String> patientNameHash = new Hashtable();
	private Readmission readmission = null;
	private MoonstoneEHostXML mexml = null;
	private ReadmissionPatientResults patientResults = null;

	public static String ExcelDelimiter = "|";

	public ReadmissionTuffy(ReadmissionCorpusProcessor processor) {
		this.patientResults = new ReadmissionPatientResults(processor, null, null);
		this.readTuffyTargetPredictionFile();
		this.readEHostTargetStatementFile();
		this.processPatients();
		this.storeTuffyEHostMatchStatistics();
	}

	public void storeTuffyEHostMatchStatistics() {
		StringBuffer sb = new StringBuffer();
		String line = "Concept|TP|FP|TN|FN|Accuracy|Sensitivity|Specificity|PPV|NVP\n";
		sb.append(line);
		Vector<String> evalues = new Vector(
				this.patientResults.getAllTargetEHostValues());
		Collections.sort(evalues);
		for (String evalue : evalues) {
			String msvalue = this.readmission
					.convertConceptEHostToMoonstone(evalue);
			msvalue = Constant.getTuffyString(msvalue);
			String key = msvalue + ":TP";
			float tp = HUtils.getCount(this.tuffyEHostMatchHash, key);
			key = msvalue + ":FP";
			float fp = HUtils.getCount(this.tuffyEHostMatchHash, key);
			key = msvalue + ":FN";
			float fn = HUtils.getCount(this.tuffyEHostMatchHash, key);
			key = msvalue + ":TN";
			float tn = HUtils.getCount(this.tuffyEHostMatchHash, key);
			float num = 0;
			float den = 0;
			float accuracy = 0;
			float ppv = 0;
			float npv = 0;
			float sensitivity = 0;
			float specificity = 0;

			num = tp + tn;
			den = tp + fp + tn + fn;
			accuracy = num / den;

			num = tp;
			den = tp + fp;
			if (den > 0) {
				ppv = num / den;
			}

			num = tn;
			den = tn + fn;
			if (den > 0) {
				npv = num / den;
			}

			num = tp;
			den = tp + fn;
			if (den > 0) {
				sensitivity = num / den;
			}

			num = tn;
			den = tn + fp;
			if (den > 0) {
				specificity = num / den;
			}
			line = evalue + ExcelDelimiter + tp + ExcelDelimiter + fp
					+ ExcelDelimiter + tn + ExcelDelimiter + fn
					+ ExcelDelimiter + accuracy + ExcelDelimiter + sensitivity
					+ ExcelDelimiter + specificity + ExcelDelimiter + ppv
					+ ExcelDelimiter + npv;
			sb.append(line + "\n");
		}

		// 11/5/2016:  NEED TO FIX
//		String temsfname = this.moonstoneRuleInterface.getStartupParameters()
//				.getPropertyValue("TuffyEHostMatchStatisticsFile");
//		String fpath = this.moonstoneRuleInterface.getResourceDirectoryName()
//				+ File.separatorChar + temsfname;
//		FUtils.writeFile(fpath, sb.toString());
	}

	public void readTuffyTargetPredictionFile() {
		try {
			String fpath = "/Applications/tuffy/samples/moonstone/TUFFY_RESULTS.txt";
			File f = new File(fpath);
			if (f.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String tstr = null;
				int lineoffset = 0;
				while ((tstr = in.readLine()) != null) {
					int firstParenIndex = tstr.indexOf('(');
					int lastParenIndex = tstr.indexOf(')');
					if (firstParenIndex > 0 && lastParenIndex > 0) {
						String rname = tstr.substring(0, firstParenIndex)
								.trim();
						if ("HasTarget".equals(rname)) {
							String astr = tstr.substring(firstParenIndex + 1,
									lastParenIndex);
							String[] sastrs = astr.split(",");
							String msconcept = sastrs[0].trim();
							msconcept = StrUtils.removeChar(msconcept, '"');
							String pname = sastrs[1].trim();
							pname = StrUtils.removeChar(pname, '"');
							this.patientNameHash.put(pname, pname);
							String key = pname + "@" + msconcept;
							this.TuffyTargetStatementTruthHash.put(key,
									new Boolean(true));
						}
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getTuffyTargetTruthPrediction(String patient, String concept) {
		String key = patient + "@" + concept;
		Boolean rv = this.TuffyTargetStatementTruthHash.get(key);
		return (rv != null ? rv.booleanValue() : false);
	}

	public boolean getEHostTargetTruthStatement(String patient, String concept) {
		String key = patient + "@" + concept;
		Boolean rv = this.EHostTargetTruthHash.get(key);
		return (rv != null ? rv.booleanValue() : false);
	}

	public void readEHostTargetStatementFile() {
		try {
			String fpath = "/Applications/tuffy/samples/moonstone/TuffyEvidenceFile";
			File f = new File(fpath);
			if (f.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String tstr = null;
				int lineoffset = 0;
				while ((tstr = in.readLine()) != null) {
					int firstParenIndex = tstr.indexOf('(');
					int lastParenIndex = tstr.indexOf(')');
					if (firstParenIndex > 0 && lastParenIndex > 0) {
						String rname = tstr.substring(0, firstParenIndex)
								.trim();
						if ("EHOSTPatientAnnotation".equals(rname)) {
							String astr = tstr.substring(firstParenIndex + 1,
									lastParenIndex);
							String[] sastrs = astr.split(",");
							String msconcept = sastrs[0].trim();
							msconcept = StrUtils.removeChar(msconcept, '"');
							String pname = sastrs[1].trim();
							pname = StrUtils.removeChar(pname, '"');
							if (Character.isDigit(pname.charAt(0))) {
								pname = "P_" + pname;
							}
							this.patientNameHash.put(pname, pname);
							String key = pname + "@" + msconcept;
							this.EHostTargetTruthHash.put(key,
									new Boolean(true));
						}
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processPatients() {
		Vector<String> pnames = HUtils.getKeys(this.patientNameHash);
		for (String pname : pnames) {
			for (String type : this.readmission.getSchema().getRelevantTypes()) {
				String attribute = this.readmission
						.getRelevantTypeAttribute(type);
				Vector<String> evalues = this.readmission
						.getTypeAttributeValues(type);
				for (String evalue : evalues) {
					String msconcept = this.readmission
							.convertConceptEHostToMoonstone(evalue);
					msconcept = Constant.getTuffyString(msconcept);
					boolean confirmedInTuffy = this
							.getTuffyTargetTruthPrediction(pname, msconcept);
					boolean confirmedInEHost = this
							.getEHostTargetTruthStatement(pname, msconcept);
					String stattype = null;
					if (confirmedInTuffy && confirmedInEHost) {
						stattype = "TP";
					} else if (confirmedInTuffy && !confirmedInEHost) {
						stattype = "FP";
					} else if (!confirmedInTuffy && confirmedInEHost) {
						stattype = "FN";
					} else if (!confirmedInTuffy && !confirmedInEHost) {
						stattype = "TN";
					}
					String key = msconcept + ":" + stattype;
					HUtils.incrementCount(this.tuffyEHostMatchHash, key);
				}
			}
		}
	}
}
