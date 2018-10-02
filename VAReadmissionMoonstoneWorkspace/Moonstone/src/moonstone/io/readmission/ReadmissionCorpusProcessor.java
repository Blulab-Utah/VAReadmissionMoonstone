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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.io.readmission.ReadmissionPatientResults.IntegerPatientNameSorter;
import moonstone.learning.feature.ARFFFeature;
import moonstone.learning.feature.ARFFPatientVector;
import moonstone.learning.feature.ARFFPatientVectorVariable;
import moonstone.learning.feature.FeatureSet;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

public class ReadmissionCorpusProcessor {

	public MoonstoneRuleInterface moonstone = null;
	public Hashtable<String, Integer> statisticsCountHash = new Hashtable();

	public String TuffyEvidenceFilename = null;
	public Hashtable<String, String> targetConceptPreferredLabelHash = new Hashtable();
	public Hashtable<String, String> oldToNewGrammarEHostValueConversionHash = new Hashtable();

	// 12/8/2016
	public Hashtable<String, String> variableARFFFeatureHeaderHash = new Hashtable();
	public Hashtable<String, Vector<ARFFFeature>> variableARFFFeatureHash = new Hashtable();

	public Hashtable<String, ARFFPatientVector> ARFFPatientVectorHash = new Hashtable();
	public Hashtable<String, ARFFPatientVectorVariable> ARFFPatientVectorVariableHash = new Hashtable();
	public Hashtable<String, Vector<ARFFPatientVectorVariable>> ARFFVariablePatientVectorHashHash = new Hashtable();

	// 4/11/2017
	public Hashtable<String, String> generalPatientNameHash = new Hashtable();
	public Hashtable<String, Integer> generalPatientVariableSnippetCountHash = new Hashtable();

	public StringBuffer tuffySB = new StringBuffer();
	public StringBuffer trainingAnswerSB = new StringBuffer();
	public StringBuffer SalomehPatientResultsSB = new StringBuffer();

	public boolean doTuffy = false;
	public boolean doARFF = false;
	public boolean doLinePatientFeatures = false;
	public int fileOrganizationMode = MultiLayerFileOrganizationMode;
	public boolean combinedMultiLayerFiles = false;
	public boolean ARFFWithEHostAnswer = true;
	public boolean includeTuffyPredicates = false;
	public boolean addEHostAnnotationTuffyStrings = false;
	public int totalFilesProcessed = 0;
	public int totalFilesEncountered = 0;
	public int totalPatients = 0;
	public Hashtable<String, String> SalomehClassificationCodeHash = new Hashtable();
	public FeatureSet featureSet = null;

	public static String ExcelDelimiter = "|";
	public static String TuffyEvidenceFileName = "TuffyEvidenceFileName";
	public static String TuffyEvidenceFileNameNoEHost = "TuffyEvidenceFileNameNoEHost";

	public static int SingleFileOrganizationMode = 0;
	public static int CombinedFileOrganizationMode = 1;
	public static int MultiLayerFileOrganizationMode = 2;

	public static Vector<String> MatchMeasureLabels = VUtils
			.arrayToVector(new String[] { "average", "common", "recent" });

	public static String[][] TargetConceptPreferredLabels = new String[][] {
			{
					"homeless/marginally housed/temporarily housed/at risk of homelessness",
					"average" }, { "lives in a facility", "recent" },
			{ "lives in a permanent single room occupancy", "average" },
			{ "lives at home/not homeless", "average" },
			{ "does not live alone", "average" }, { "living alone", "common" },
			{ "has access to community services", "average" },
			{ "no social support", "average" },
			{ "has social support", "average" } };

	public static String[][] TargetConceptPreferredLabels_BEFORE_NEW_GRAMMAR = new String[][] {
			{ "not homeless/but other housing situation", "average" },
			{ "living in an assisted living facility", "recent" },
			{ "living in a nursing home", "recent" },
			{ "living in a group home", "average" },
			{ "lives in a permanent single room occupancy", "average" },
			{ "lives at home", "average" },
			{ "marginally housed/temporarily housed/at risk of homelessness",
					"average" }, { "homeless", "common" },
			{ "does not live alone", "average" }, { "living alone", "common" },
			{ "has access to community services", "average" },
			{ "no social support", "average" },
			{ "has social support", "average" }, { "homeless", "average" } };

	public static String[][] OldToNewGrammarEHostValueConversionMap = {
			{ "not homeless/but other housing situation",
					"lives at home/not homeless" },
			{ "living in an assisted living facility", "lives in a facility" },
			{ "living in a nursing home", "lives in a facility" },
			{ "living in a group home", "lives in a facility" },
			{ "lives at home", "lives at home/not homeless" },
			{ "marginally housed/temporarily housed/at risk of homelessness",
					"homeless/marginally housed/temporarily housed/at risk of homelessness" },
			{ "homeless",
					"homeless/marginally housed/temporarily housed/at risk of homelessness" } };

	public static String[][] SalomehClassificationCodes = {
			{ "no social support", "0" },
			{ "has social support", "1" },
			{ "has access to community services", "2" },
			{
					"homeless/marginally housed/temporarily housed/at risk of homelessness",
					"2" }, { "lives at home/not homeless", "1" },
			{ "lives in a facility", "3" },
			{ "lives in a permanent single room occupancy", "4" },
			{ "does not live alone", "0" }, { "living alone", "1" } };

	public ReadmissionCorpusProcessor(MoonstoneRuleInterface moonstone,
			boolean includeTuffyPredicates) {
		this.moonstone = moonstone;
		this.featureSet = FeatureSet.loadFeatureSet(moonstone);
		this.includeTuffyPredicates = includeTuffyPredicates;
		for (int i = 0; i < TargetConceptPreferredLabels.length; i++) {
			String[] pair = TargetConceptPreferredLabels[i];
			String concept = pair[0];
			String label = pair[1];
			this.targetConceptPreferredLabelHash.put(concept, label);
		}
		for (int i = 0; i < OldToNewGrammarEHostValueConversionMap.length; i++) {
			String[] pair = OldToNewGrammarEHostValueConversionMap[i];
			String oldname = pair[0];
			String newname = pair[1];
			this.oldToNewGrammarEHostValueConversionHash.put(oldname, newname);
		}
		for (String[] pair : SalomehClassificationCodes) {
			this.SalomehClassificationCodeHash.put(pair[0], pair[1]);

		}
		if (includeTuffyPredicates) {
			this.TuffyEvidenceFilename = this.moonstone.getStartupParameters()
					.getPropertyValue(TuffyEvidenceFileName);
		} else {
			this.TuffyEvidenceFilename = this.moonstone.getStartupParameters()
					.getPropertyValue(TuffyEvidenceFileNameNoEHost);
		}
	}

	// 12/7/2016: What did I do with the code for processing patients without
	// tsl.properties files?
	public void analyzeMultipleCasesOnePass(boolean arff, boolean eHOSTAnswer,
			int fmode) {
		clear();
		this.doARFF = arff;
		this.ARFFWithEHostAnswer = eHOSTAnswer;
		this.fileOrganizationMode = fmode;
		long starttime = System.currentTimeMillis();
		ReadmissionPatientResults rpr = new ReadmissionPatientResults(this,
				this.moonstone, "NOTSL");
		if (this.doTuffy) {
			this.storeTuffyEvidence();
		} else if (this.doARFF) {
			this.createARFFFiles();
		} else {
			this.printSalomehPatientStatistics();

			this.printExcelStatisticsAllMeasures("SUMMARY");
			this.printVariableSummaryStatisticsAllMeasures();

			this.printLearningPatientTrainingAnswers();
		}

		long endtime = System.currentTimeMillis();
		long minutes = ((endtime - starttime) / 1000) / 60;

		System.out.printf("END PATIENT REPORTS.  TIME= %d MINUTES, "
				+ "PATIENTS=%d, FILES PROCESSED=%d, FILES ENCOUNTERED=%d\n",
				minutes, this.totalPatients, this.totalFilesProcessed,
				this.totalFilesEncountered);
	}

	public void analyzeMultipleCasesFromMultipleFolders(boolean useWorkbench,
			boolean tuffy, boolean arff, boolean eHOSTAnswer) {
		clear();
		this.fileOrganizationMode = MultiLayerFileOrganizationMode;
		this.doTuffy = tuffy;
		this.doARFF = arff;
		this.ARFFWithEHostAnswer = eHOSTAnswer;
		long starttime = System.currentTimeMillis();
		String tslfilestr = this.moonstone.getStartupParameters()
				.getPropertyValue("AllTSLFiles");
		if (tslfilestr != null) {
			String[] fnames = tslfilestr.split(",");
			for (String fname : fnames) {
				fname += ".properties";
				try {
					System.out.println("About to initialize " + fname + "...");
					MoonstoneRuleInterface tmsri = new MoonstoneRuleInterface(
							fname, !useWorkbench);
					ReadmissionPatientResults rpr = new ReadmissionPatientResults(
							this, tmsri, fname);
					if (tuffy) {
						this.storeTuffyEvidence();
					} else if (arff) {
						this.createARFFFiles();
					} else {
						// this.printExcelStatistics("SUMMARY");
						// this.printVariableSummaryStatistics();

						this.printSalomehPatientStatistics();

						this.printExcelStatisticsAllMeasures("SUMMARY");
						this.printExcelStatisticsBestMeasure("SUMMARY");
						this.printVariableSummaryStatisticsAllMeasures();
						this.printVariableSummaryStatisticsBestMeasure();

						this.printLearningPatientTrainingAnswers();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		long endtime = System.currentTimeMillis();
		long minutes = ((endtime - starttime) / 1000) / 60;

		this.printGeneralVariableMaxMinMean();

		System.out
				.printf("END PATIENT REPORTS.  TIME= %d MINUTES, FILES PROCESSED= %d\n",
						minutes, this.totalFilesProcessed);
	}

	// 2/15/2017
	public void analyzeMultipleCasesFromSingleFolder(boolean useWorkbench,
			boolean tuffy, boolean arff, boolean doLinePatientFeatures,
			boolean eHOSTAnswer, int fmode) {
		clear();
		this.fileOrganizationMode = fmode;
		this.doTuffy = tuffy;
		this.doARFF = arff;
		this.doLinePatientFeatures = doLinePatientFeatures;
		this.ARFFWithEHostAnswer = eHOSTAnswer;
		long starttime = System.currentTimeMillis();
		ReadmissionPatientResults rpr = new ReadmissionPatientResults(this,
				this.moonstone, "ALL");
		if (doLinePatientFeatures) {
			rpr.writePatientClassifierLineFeatureFiles();
		} else if (tuffy) {
			this.storeTuffyEvidence();
		} else if (arff) {
			// Removed 5/13/2016
			// this.createARFFFiles();
		} 
		long endtime = System.currentTimeMillis();
		long minutes = ((endtime - starttime) / 1000) / 60;
		System.out
				.printf("END PATIENT REPORTS.  TIME= %d MINUTES, FILES PROCESSED= %d\n",
						minutes, this.totalFilesProcessed);
	}

	public void storeTuffyEvidence() {
		if (this.TuffyEvidenceFilename != null) {
			String fpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + this.TuffyEvidenceFilename;
			FUtils.writeFile(fpath, this.tuffySB.toString());
		}
	}

	public void createARFFFiles() {
		// Can't do this: Need to have the same definition file for aggregate
		// runs.
		// this.featureSet.featureDefinitionVector.writeFeatureDefinitionFile();
		for (String variable : ARFFFeature.ReadmissionVariables) {
			createARFFFile(variable);
		}
	}

	public void writeandClearARFFFileTables() {
		createARFFFiles();
		ARFFPatientVectorHash.clear();
		ARFFPatientVectorVariableHash.clear();
		ARFFVariablePatientVectorHashHash.clear();
	}

	public void createARFFFile(String variable) {
		Vector<ARFFPatientVectorVariable> apvvs = this.ARFFVariablePatientVectorHashHash
				.get(variable);
		if (apvvs != null) {
			int x = 1;
			boolean useanswer = this.ARFFWithEHostAnswer;
			String astr = "ARFF" + "_" + (useanswer ? "train" : "test");
			String fpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + astr + File.separatorChar + "ARFF_"
					+ variable + ".arff";
			Collections.sort(apvvs,
					new ARFFPatientVectorVariable.PatientIDSorter());
			StringBuffer sb = new StringBuffer();
			boolean includeHeader = !(new File(fpath).exists());
			if (includeHeader) {
				String header = ARFFPatientVectorVariable.getARFFHeader(
						this.featureSet.featureDefinitionVector, variable);
				sb.append(header);
			}

			// 7/26/2017
			boolean useOnlyNonZeroVectors = false;
			for (ARFFPatientVectorVariable apvv : apvvs) {
				if (useOnlyNonZeroVectors) {
					if (apvv.getPatientVector().isContainsFeatures()) {
						String data = apvv.toString() + "\n";
						sb.append(data);
					}
				} else {
					String data = apvv.toString() + "\n";
					sb.append(data);
				}
			}
			FUtils.appendFile(fpath, sb.toString());
		}
	}

	public void printSalomehPatientStatistics() {
		String fname = "SalomehPatientStatisticsFile";
		String fpath = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + fname;
		StringBuffer sb = new StringBuffer();
		String line = "Patient";
		for (String variable : this.moonstone.getReadmission()
				.getAllEHostVariables()) {
			line += "\t" + variable;
		}
		sb.append(line + "\n");
		sb.append(this.SalomehPatientResultsSB.toString());
		FUtils.writeFile(fpath, sb.toString());
	}

	public void printExcelStatisticsBestMeasure(String header) {
		StringBuffer sb = new StringBuffer();
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String casename = null;
		if (header != null) {
			casename = header;
		} else {
			int index = inputdirname.lastIndexOf(File.separatorChar);
			casename = inputdirname.substring(index + 1);
		}
		String line = "Case|Concept|TP|FP|TN|FN|Accuracy|Sensitivity|Specificity|FScore\n";
		sb.append(line);
		for (String evalue : this.getAllTargetEHostValues()) {
			String mlabel = "best";
			String key = null;
			key = evalue + ":" + mlabel + ":TP";
			float tp = HUtils.getCount(this.statisticsCountHash, key);
			key = evalue + ":" + mlabel + ":FP";
			float fp = HUtils.getCount(this.statisticsCountHash, key);
			key = evalue + ":" + mlabel + ":FN";
			float fn = HUtils.getCount(this.statisticsCountHash, key);
			key = evalue + ":" + mlabel + ":TN";
			float tn = HUtils.getCount(this.statisticsCountHash, key);
			float num = 0;
			float den = 0;
			float accuracy = 0;
			float sensitivity = 0;
			float specificity = 0;
			float fscore = 0;

			num = tp + tn;
			den = tp + fp + tn + fn;
			accuracy = num / den;
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
			fscore = getFmeasure(tp, tn, fp, fn);
			line = casename + ExcelDelimiter + evalue + ExcelDelimiter + tp
					+ ExcelDelimiter + fp + ExcelDelimiter + tn
					+ ExcelDelimiter + fn + ExcelDelimiter + accuracy
					+ ExcelDelimiter + sensitivity + ExcelDelimiter
					+ specificity + ExcelDelimiter + fscore + "\n";
			sb.append(line);
		}
		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionExcelStatisticsBest.txt";
		// String ftext = FUtils.readFile(fname);
		// StringBuffer newsb = new StringBuffer();
		// if (ftext != null) {
		// newsb.append(ftext);
		// }
		// newsb.append(sb.toString());
		FUtils.writeFile(fname, sb.toString());
	}

	public void printExcelStatisticsAllMeasures(String header) {
		StringBuffer sb = new StringBuffer();
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String casename = null;
		if (header != null) {
			casename = header;
		} else {
			int index = inputdirname.lastIndexOf(File.separatorChar);
			casename = inputdirname.substring(index + 1);
		}
		String line = "Case|Concept|AverageTP|AverageFP|AverageTN|AverageFN|AverageAccuracy|AverageSensitivity|AverageSpecificity|AverageFScore|CommonTP|CommonFP|CommonTN|CommonFN|CommonAccuracy|CommonSensitivity|CommonSpecificity|CommonFScore|RecentTP|RecentFP|RecentTN|RecentFN|RecentAccuracy|RecentSensitivity|RecentSpecificity|RecentFScore\n";
		sb.append(line);
		for (String evalue : this.getAllTargetEHostValues()) {
			line = null;
			for (String mlabel : MatchMeasureLabels) {
				if (line == null) {
					line = casename + ExcelDelimiter + evalue;
				}
				String key = null;
				key = evalue + ":" + mlabel + ":TP";
				float tp = HUtils.getCount(this.statisticsCountHash, key);
				if (tp > 0 && "average".equals(mlabel)) {
					int x = 1;
				}
				key = evalue + ":" + mlabel + ":FP";
				float fp = HUtils.getCount(this.statisticsCountHash, key);
				key = evalue + ":" + mlabel + ":FN";
				float fn = HUtils.getCount(this.statisticsCountHash, key);
				key = evalue + ":" + mlabel + ":TN";
				float tn = HUtils.getCount(this.statisticsCountHash, key);
				float num = 0;
				float den = 0;
				float accuracy = 0;
				float sensitivity = 0;
				float specificity = 0;
				float fscore = 0;

				num = tp + tn;
				den = tp + fp + tn + fn;
				accuracy = num / den;
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
				fscore = getFmeasure(tp, tn, fp, fn);
				line += ExcelDelimiter + tp + ExcelDelimiter + fp
						+ ExcelDelimiter + tn + ExcelDelimiter + fn
						+ ExcelDelimiter + accuracy + ExcelDelimiter
						+ sensitivity + ExcelDelimiter + specificity
						+ ExcelDelimiter + fscore;
			}
			sb.append(line + "\n");
		}
		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionExcelStatisticsAll.txt";
		// String ftext = FUtils.readFile(fname);
		// StringBuffer newsb = new StringBuffer();
		// if (ftext != null) {
		// newsb.append(ftext);
		// }
		// newsb.append(sb.toString());
		FUtils.writeFile(fname, sb.toString());
	}

	public void printVariableSummaryStatisticsBestMeasure() {
		StringBuffer sb = new StringBuffer();
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String casename = null;
		String line = "Variable|TP|FP|TN|FN|Accuracy|Sensitivity|Specificity|F-Measure\n";
		sb.append(line);
		for (String generalType : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			String mlabel = "best";
			String key = null;
			key = generalType + ":" + mlabel + ":TP";
			float tp = HUtils.getCount(this.statisticsCountHash, key);
			key = generalType + ":" + mlabel + ":FP";
			float fp = HUtils.getCount(this.statisticsCountHash, key);
			key = generalType + ":" + mlabel + ":FN";
			float fn = HUtils.getCount(this.statisticsCountHash, key);
			key = generalType + ":" + mlabel + ":TN";
			float tn = HUtils.getCount(this.statisticsCountHash, key);
			float num = 0;
			float den = 0;
			float accuracy = 0;
			float sensitivity = 0;
			float specificity = 0;
			num = tp + tn;
			den = tp + fp + tn + fn;
			accuracy = num / den;
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
			float fmeasure = this.getFmeasure(tp, tn, fp, fn);
			line = generalType + ExcelDelimiter + tp + ExcelDelimiter + fp
					+ ExcelDelimiter + tn + ExcelDelimiter + fn
					+ ExcelDelimiter + accuracy + ExcelDelimiter + sensitivity
					+ ExcelDelimiter + specificity + ExcelDelimiter + fmeasure
					+ "\n";
			sb.append(line);
		}
		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionVariableStatisticsBest.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printVariableSummaryStatisticsAllMeasures() {
		StringBuffer sb = new StringBuffer();
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String casename = null;
		String line = "Variable|AverageTP|AverageFP|AverageTN|AverageFN|AverageAccuracy|AverageSensitivity|AverageSpecificity|AverageF-Measure|CommonTP|CommonFP|CommonTN|CommonFN|CommonAccuracy|CommonSensitivity|CommonSpecificity|CommonF-Measure|RecentTP|RecentFP|RecentTN|RecentFN|RecentAccuracy|RecentSensitivity|RecentSpecificity|RecentF-Measure\n";
		sb.append(line);
		line = null;
		for (String generalType : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			line = generalType;
			for (String mlabel : MatchMeasureLabels) {
				String key = null;
				key = generalType + ":" + mlabel + ":TP";
				float tp = HUtils.getCount(this.statisticsCountHash, key);
				key = generalType + ":" + mlabel + ":FP";
				float fp = HUtils.getCount(this.statisticsCountHash, key);
				key = generalType + ":" + mlabel + ":FN";
				float fn = HUtils.getCount(this.statisticsCountHash, key);
				key = generalType + ":" + mlabel + ":TN";
				float tn = HUtils.getCount(this.statisticsCountHash, key);
				float num = 0;
				float den = 0;
				float accuracy = 0;
				float sensitivity = 0;
				float specificity = 0;
				num = tp + tn;
				den = tp + fp + tn + fn;
				accuracy = num / den;
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
				float fmeasure = this.getFmeasure(tp, tn, fp, fn);
				line += ExcelDelimiter + tp + ExcelDelimiter + fp
						+ ExcelDelimiter + tn + ExcelDelimiter + fn
						+ ExcelDelimiter + accuracy + ExcelDelimiter
						+ sensitivity + ExcelDelimiter + specificity
						+ ExcelDelimiter + fmeasure;
			}
			sb.append(line + "\n");
		}
		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionVariableStatisticsAll.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printLearningPatientTrainingAnswers() {
		String fname = this.moonstone.getStartupParameters().getPropertyValue(
				"LearningPatientTrainingAnswers");
		if (fname != null) {
			String fpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + fname;
			FUtils.writeFile(fpath, this.trainingAnswerSB.toString());
		}
	}

	public float getFmeasure(float tp, float tn, float fp, float fn) {
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
		if (den > 0) {
			return num / den;
		}
		return 0;
	}

	Vector<String> getAllTargetEHostValues() {
		Vector<String> allEvalues = null;
		for (String type : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			String attribute = this.moonstone.getReadmission()
					.getRelevantTypeAttribute(type);
			Vector<String> evalues = this.moonstone.getReadmission()
					.getTypeAttributeValues(type);
			allEvalues = VUtils.appendNew(allEvalues, evalues);
		}
		return allEvalues;
	}

	public String getTargetConceptPreferredLabelHash(String concept) {
		String label = this.targetConceptPreferredLabelHash.get(concept);
		if (label == null) {
			label = "average";
		}
		return label;
	}

	public void clear() {
		statisticsCountHash = new Hashtable();
		trainingAnswerSB = new StringBuffer();
		totalFilesProcessed = 0;
		String sfname = this.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionPatientStatistics.txt";
		FUtils.deleteFileIfExists(sfname);
		sfname = this.moonstone.getResourceDirectoryName() + File.separator
				+ "ReadmissionVariableStatistics.txt";
		FUtils.deleteFileIfExists(sfname);
	}

	// ////////////////////////////////////////////////////////////////////////
	// 12/28/2016: For generating Salomeh's 1500 patient files

	/****
	 * HOUSING: {
	 * "homeless/marginally housed/temporarily housed/at risk of homelessness",
	 * "lives at home/not homeless", "lives in a facility",
	 * "lives in a permanent single room occupancy"} == {2, 1, 3, 4}
	 * 
	 * LIVING ALONE: {"does not live alone", "living alone"} == {0, 1}
	 * 
	 * SUPPORT: {"has access to community services", "has social support",
	 * "no social support"} == {2, 1, 0}
	 ****/

	public static String[] HousingClassificationPositionalConcepts = {
			"homeless/marginally housed/temporarily housed/at risk of homelessness",
			"lives at home/not homeless", "lives in a facility",
			"lives in a permanent single room occupancy" };
	public static String[] LivingAlonePositionalConcepts = {
			"does not live alone", "living alone" };
	public static String[] SocialSupportPositionalConcepts = {
			"has access to community services", "has social support",
			"no social support" };

	public static String[] SalomehVariables = { "LIVING_ALONE",
			"HOUSING_SITUATION", "SOCIAL_SUPPORT" };
	public Hashtable<Integer, String> WEKAPatientIDHash = new Hashtable();
	// public static String WEKATrainDirName =
	// "/Users/leechristensen/Desktop/ARFF_train_12_28_2016";
	// public static String WEKATestDirName =
	// "/Users/leechristensen/Desktop/ARFF_test_12_28_2016_2";

	public static String WEKATrainDirName = "C:\\Users\\VHASLCChrisL1\\Desktop\\ARFF\\ARFF_train_12_28_2016";
	public static String WEKATestDirName = "C:\\Users\\VHASLCChrisL1\\Desktop\\ARFF\\ARFF_test_12_28_2016_2";

	public static int[] HousingSituationClassificationCodes = { 2, 1, 3, 4 };
	public static int[] LivingAloneClassificationCodes = { 0, 1 };
	public static int[] SocialSupportClassificationCodes = { 2, 1, 0 };

	public String getPositionalClassification(String variable, int index) {
		String[] concepts = null;
		if ("HOUSING_SITUATION".equals(variable)) {
			concepts = HousingClassificationPositionalConcepts;
		} else if ("LIVING_ALONE".equals(variable)) {
			concepts = LivingAlonePositionalConcepts;
		} else if ("SOCIAL_SUPPORT".equals(variable)) {
			concepts = SocialSupportPositionalConcepts;
		}
		if (concepts != null && index < concepts.length) {
			return concepts[index];
		}
		return "?";
	}

	public int getVariableClassificationCode(String variable, int classIndex) {
		int[] codes = null;
		if ("HOUSING_SITUATION".equals(variable)) {
			codes = HousingSituationClassificationCodes;
		} else if ("LIVING_ALONE".equals(variable)) {
			codes = LivingAloneClassificationCodes;
		} else if ("SOCIAL_SUPPORT".equals(variable)) {
			codes = SocialSupportClassificationCodes;
		}
		if (codes != null && classIndex < codes.length) {
			return codes[classIndex];
		}
		return -1;
	}

	// 4/13/2017: For Salomeh's request to ascertain what WEKA inferred when
	// EHost annotators
	// said "no mention"

	public void generateSalomehFormatResultsViaWEKA() {
		StringBuffer sb = new StringBuffer();
		sb.append("Patient	LIVING_ALONE	HOUSING_SITUATION	SOCIAL_SUPPORT\n");
		Hashtable<String, Integer> pvhash = new Hashtable();
		Hashtable<Integer, String> phash = new Hashtable();
		Hashtable<String, String> pchash = new Hashtable();
		for (String variable : SalomehVariables) {
			generateSalomehFormatResultsViaWEKA(variable, pvhash, phash, pchash);
		}
		Vector<Integer> pv = HUtils.getKeys(phash);
		Collections.sort(pv);
		for (Integer pid : pv) {
			String pname = phash.get(pid);
			String line = pname + "\t";
			for (int i = 0; i < SalomehVariables.length; i++) {
				String variable = SalomehVariables[i];
				String key = variable + ":" + pname;
				int code = pvhash.get(key);
				line += code;
				if (i < SalomehVariables.length - 1) {
					line += "\t";
				}
			}
			sb.append(line + "\n");
		}
		String fpath = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + "SalomehPatientStatistics";
		FUtils.writeFile(fpath, sb.toString());
	}

	// 4/13/2017: For Salomeh's request to ascertain what WEKA inferred when
	// EHost annotators
	// said "no mention"

	public void generateSalomehWEKAVsMissingClassificationResults() {
		StringBuffer sb = new StringBuffer();
		Hashtable<String, Integer> pvhash = new Hashtable();
		Hashtable<Integer, String> phash = new Hashtable();
		Hashtable<String, String> pchash = new Hashtable();
		Hashtable<String, Integer> variableNoMentionCountHash = new Hashtable();
		Hashtable<String, Integer> variableCountHash = new Hashtable();
		Hashtable<String, Integer> classificationCountHash = new Hashtable();
		Hashtable<String, Vector<String>> variableClassificationHash = new Hashtable();
		ReadmissionPatientResults rpr = new ReadmissionPatientResults(this,
				this.moonstone);
		ReadmissionEHostPatientResults epr = new ReadmissionEHostPatientResults(
				rpr);

		for (String variable : SalomehVariables) {
			generateSalomehFormatResultsViaWEKA(variable, pvhash, phash, pchash);
		}
		Vector<Integer> pv = HUtils.getKeys(phash);
		Collections.sort(pv);
		for (Integer pid : pv) {
			String pname = phash.get(pid);
			for (int i = 0; i < SalomehVariables.length; i++) {
				String variable = SalomehVariables[i];
				HUtils.incrementCount(variableCountHash, variable);
				String key = variable + ":" + pname;
				String wclass = pchash.get(key);
				VUtils.pushIfNotHashVector(variableClassificationHash,
						variable, wclass);
				HUtils.incrementCount(classificationCountHash, wclass);
				String eclass = epr.getPatientClassification(pname, variable);
				boolean missing = (eclass == null || eclass.toLowerCase()
						.contains("mention"));
				if (missing) {
					HUtils.incrementCount(variableNoMentionCountHash, variable);
				}
			}
		}
		for (String variable : SalomehVariables) {
			int vcount = HUtils.getCount(variableCountHash, variable);
			int nmcount = HUtils.getCount(variableNoMentionCountHash, variable);
			for (String cname : variableClassificationHash.get(variable)) {
				int ccount = HUtils.getCount(classificationCountHash, cname);
				float cval = (float) ccount / (float) vcount;
				String line = "Variable: " + variable + ",NoMentionCount="
						+ nmcount + ",Classification=" + cname + ",Weka%="
						+ cval;
				sb.append(line + "\n");
			}
		}
		String fpath = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + "SalomehNoMentionVariableStatistics";
		FUtils.writeFile(fpath, sb.toString());
	}

	public void generateSalomehFormatResultsViaWEKA(String variable,
			Hashtable<String, Integer> pihash,
			Hashtable<Integer, String> phash, Hashtable<String, String> pchash) {
		int x = 1;
		String fname = "ARFF_" + variable + ".arff";
		String WEKATrainDirectory = this.moonstone.getStartupParameters()
				.getPropertyValue("WEKATrainDirectory");
		String WEKATestDirectory = this.moonstone.getStartupParameters()
				.getPropertyValue("WEKATestDirectory");
		String trainfpath = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + WEKATrainDirectory + File.separatorChar
				+ fname;
		String testfpath = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + WEKATestDirectory + File.separatorChar
				+ fname;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					trainfpath));
			Instances traindata = new Instances(reader);
			reader.close();
			traindata.setClassIndex(traindata.numAttributes() - 1);

			reader = new BufferedReader(new FileReader(testfpath));
			Instances testdata = new Instances(reader);
			reader.close();

			// TEST
			// Instances testdata = new Instances(traindata);

			testdata.setClassIndex(testdata.numAttributes() - 1);

			// Before 5/27/2017
			Classifier c = new weka.classifiers.meta.MultiClassClassifier();
			String[] options = weka.core.Utils
					.splitOptions("-M 0 -R 2.0 -S 1 -W weka.classifiers.functions.Logistic -- -R 1.0E-8 -M -1");

			// 5/27/2017
			// Classifier c = new weka.classifiers.trees.RandomForest();
			// String[] options = weka.core.Utils
			// .splitOptions("-I 100 -K 0 -S 1");

			c.setOptions(options);

			Remove rm = new Remove();
			rm.setAttributeIndices("1");
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(c);
			fc.setFilter(rm);

			c.buildClassifier(traindata);

			for (int i = 0; i < testdata.numInstances(); i++) {
				Instance instance = testdata.instance(i);
				double pred = c.classifyInstance(instance);
				double pd = instance.value(0);
				int pid = Integer.valueOf((int) pd);
				String pname = String.valueOf((int) pd);
				phash.put(pid, pname);
				String key = variable + ":" + pname;
				int cvalue = (int) instance.classValue();
				Object value = testdata.classAttribute().value(cvalue);
				int code = this.getVariableClassificationCode(variable,
						(int) pred);
				pihash.put(key, new Integer(code));
				String classification = this.getPositionalClassification(
						variable, (int) pred);
				pchash.put(key, classification);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 4/11/2017: Gather stats for Salomeh's talk
	public void printGeneralVariableMaxMinMean() {
		Vector<String> pnames = HUtils.getKeys(this.generalPatientNameHash);
		Collections.sort(pnames, new IntegerPatientNameSorter());
		Readmission rm = this.moonstone.getReadmission();
		int maxHousing = 0, minHousing = 100000, sumHousing = 0;
		int maxSupport = 0, minSupport = 100000, sumSupport = 0;
		int maxAlone = 0, minAlone = 100000, sumAlone = 0;
		float patientsNoMentionHousing = 0, patientsNoMentionSupport = 0, patientsNoMentionAlone = 0;
		for (String pname : pnames) {
			for (String variable : rm.getAllEHostVariables()) {
				String key = pname + "@@" + variable;
				int count = HUtils.getCount(
						this.generalPatientVariableSnippetCountHash, key);
				if ("HOUSING_SITUATION".equals(variable)) {
					int housingCount = count;
					sumHousing += housingCount;
					if (housingCount > maxHousing) {
						maxHousing = housingCount;
					}
					if (housingCount < minHousing) {
						minHousing = housingCount;
					}
					if (housingCount == 0) {
						patientsNoMentionHousing++;
					}
				} else if ("LIVING_ALONE".equals(variable)) {
					int aloneCount = count;
					sumAlone += aloneCount;
					if (aloneCount > maxAlone) {
						maxAlone = aloneCount;
					}
					if (aloneCount < minAlone) {
						minAlone = aloneCount;
					}
					if (aloneCount == 0) {
						patientsNoMentionAlone++;
					}
				} else if ("SOCIAL_SUPPORT".equals(variable)) {
					int supportCount = count;
					sumSupport += supportCount;
					if (supportCount > maxSupport) {
						maxSupport = supportCount;
					}
					if (supportCount < minSupport) {
						minSupport = supportCount;
					}
					if (supportCount == 0) {
						patientsNoMentionSupport++;
					}
				}
			}
		}
		float numPatients = (float) pnames.size();
		float meanHousing = (float) sumHousing / numPatients;
		float meanAlone = (float) sumAlone / numPatients;
		float meanSupport = (float) sumSupport / numPatients;
		float percentNoMentionHousing = patientsNoMentionHousing / numPatients;
		float percentNoMentionAlone = patientsNoMentionAlone / numPatients;
		float percentNoMentionSupport = patientsNoMentionSupport / numPatients;

		System.out.println("\n\nHousing: Max=" + maxHousing + ",Min="
				+ minHousing + ",Mean=" + meanHousing + ",%NoMention="
				+ percentNoMentionHousing);
		System.out
				.println("LivingAlone: Max=" + maxAlone + ",Min=" + minAlone
						+ ",Mean=" + meanAlone + ",%NoMention="
						+ percentNoMentionAlone);
		System.out.println("SocialSupport: Max=" + maxSupport + ",Min="
				+ minSupport + ",Mean=" + meanSupport + ",%NoMention="
				+ percentNoMentionSupport);

	}

}
