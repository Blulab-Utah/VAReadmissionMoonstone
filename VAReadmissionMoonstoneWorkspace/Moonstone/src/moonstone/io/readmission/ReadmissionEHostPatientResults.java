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

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.startup.StartupParameters;
import tsl.utilities.FUtils;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;

public class ReadmissionEHostPatientResults {

	MoonstoneRuleInterface moonstone = null;
	ReadmissionPatientResults patientResults = null;
	KnowtatorIO kio = null;
	Hashtable<String, Boolean> patientSummaryHash = new Hashtable();
	Hashtable<String, String> patientEHostClassificationHash = new Hashtable();
	Hashtable<String, String> patientVariableClassificationHash = new Hashtable();

	public static String EHostPatientResultsDirectory = "EHostPatientResultsDirectory";

	public ReadmissionEHostPatientResults(ReadmissionPatientResults rpr) {
		try {
			int x = 1;
			this.patientResults = rpr;
			this.moonstone = rpr.moonstone;
			System.out
					.println("ReadmissionEHostPatientResults: Reading files...");
			this.readAnnotationFiles();
			if (this.kio != null && kio.getAnnotations() != null) {
				this.kio.createWorkbenchAnnotations(kio.getAnalysis());
				for (AnnotationCollection ac : this.kio
						.getAnnotationCollections()) {
					for (Annotation annotation : ac.getAnnotations()) {
						String classification = (String) annotation
								.getClassificationValue();

						if (classification.toLowerCase().contains("no social")
								|| classification.toLowerCase().contains(
										"marginally")
								|| classification.toLowerCase().contains(
										"community")) {
							x = 1;
						}

						String docname = annotation.getAnnotationCollection()
								.getSourceTextName();
						String pname = getPatientNameFromFileName(docname);
						if (classification != null) {

							// 5/11/2017
							if ("lives in a group setting"
									.equals(classification)) {
								classification = "does not live alone";
							}

							classification = rpr
									.convertOldToNewEHostValue(classification);
							String key = pname + ":" + classification;
							this.patientSummaryHash.put(key, new Boolean(true));

							String variable = this.moonstone.getReadmission()
									.getAttributeValueType(classification);
							if (variable == null) {
								variable = annotation.getName();
							}
							key = pname + ":" + variable;
							this.patientVariableClassificationHash.put(key,
									classification);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean patientHasClassification(String pname, String classification) {
		String key = pname + ":" + classification;
		Boolean istrue = this.patientSummaryHash.get(key);
		if (istrue == null) {
			key = pname + "@@" + classification;
			istrue = this.patientSummaryHash.get(key);
			if (istrue != null) {
				int x = 1;
			}
		}
		return istrue != null;
	}

	public String getPatientClassification(String pname, String variable) {
		String key = pname + ":" + variable;
		String classification = this.patientVariableClassificationHash.get(key);
		return classification;
	}

	public void readAnnotationFiles() {
		try {
			if (this.patientResults != null
					&& this.patientResults.moonstone.getWorkbench() != null) {
				StartupParameters sp = this.patientResults.moonstone
						.getStartupParameters();
				String dname = sp
						.getPropertyValue(EHostPatientResultsDirectory);
				// 4/13/2017: Added name substring so I can use large containing
				// directories

				String pathConstraint = sp
						.getPropertyValue("AnnotationPathnameConstraintString");
				String filenameConstraint = sp
						.getPropertyValue("ReadmissionEHostPathnameAnnotationConstraintString");
				Vector<File> files = FUtils.readFilesFromDirectory(dname,
						pathConstraint, filenameConstraint);
				if (files != null) {
					this.kio = new KnowtatorIO();
					Analysis oldanalysis = this.patientResults.moonstone
							.getWorkbench().getAnalysis();
					Analysis newanalysis = new Analysis(
							oldanalysis.getTypeSystem());
					kio.setAnalysis(newanalysis);
					kio.setTypeSystem(newanalysis.getTypeSystem());
					for (File file : files) {
						String fname = file.getName().toLowerCase();
						if (fname.contains("knowtator")) {
							String pname = getPatientNameFromFileName(file
									.getName());
							String fstr = FUtils.readFile(file);
							Knowtator.readAnnotationFile(kio, null, fstr, true);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 12/11/2017: HOW DID THIS WORK BEFORE? The names are in the format
	// 123_DATENumberString_Doctype.
	// public static String getPatientNameFromFileName(String fname) {
	// int x = 1;
	// int index = fname.indexOf("_");
	// String pname = null;
	// if (index > 0) {
	// if (Character.isDigit(fname.charAt(0))) {
	//
	// }
	//
	// pname = "";
	// for (int i = 0; i < index; i++) {
	// char c = fname.charAt(i);
	// if (Character.isDigit(c)) {
	// pname += c;
	// } else {
	// break;
	// }
	// }
	// }
	// return pname;
	// }

	public static String getPatientNameFromFileName(String fname) {
		int x = 1;
		int index = fname.indexOf("_");
		String pname = null;
		if (index > 0) {
			pname = "";
			for (int i = index + 1; i < fname.length(); i++) {
				char c = fname.charAt(i);
				if (Character.isDigit(c)) {
					pname += c;
				} else {
					break;
				}
			}
		}
		return pname;
	}

}
