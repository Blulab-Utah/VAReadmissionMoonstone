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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import moonstone.learning.feature.ARFFFeature;
import moonstone.learning.feature.ARFFPatientVectorVariable;
import moonstone.learning.feature.Feature;
import moonstone.learning.feature.FeatureSet;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class ReadmissionPatientClassificationTables {

	private MoonstoneRuleInterface moonstone = null;
	private Hashtable<String, ReadmissionPatientClassificationTable> tableHash = new Hashtable();
	public static String ReadmissionPatientTableDirectory = "ReadmissionPatientTableDirectory";
	public static String AnnotationSetDescription = "AnnotationSetDescription";
	public static String Separator = ":";
	public static String PrintSeparator = "|";
	public static int NumberOfBootstrapIterations = 400;

	public ReadmissionPatientClassificationTables(MoonstoneRuleInterface msri) {
		this.moonstone = msri;
		// this.readTableFiles();
	}

	public void printClassificationStatistics() {
		StringBuffer sb = new StringBuffer();
		this.printClassificationStatistics("Phase2_Tiffany",
				"Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany", "Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printClassificationStatistics("Phase2_Kristi", "Phase2_moonstone",
				sb);
		sb.append("\n\n");

		this.printVariableStatistics("Phase2_Kristi", "Phase2_moonstone", sb);
		sb.append("\n\n");
		this.printClassificationStatistics("Phase2_Holly", "Phase2_moonstone",
				sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly", "Phase2_moonstone", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Tiffany", "Phase2_Holly", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany", "Phase2_Holly", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Tiffany", "Phase2_Kristi",
				sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Tiffany", "Phase2_Kristi", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Holly", "Phase2_Tiffany", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly", "Phase2_Tiffany", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Holly", "Phase2_Kristi", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Holly", "Phase2_Kristi", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Kristi", "Phase2_Tiffany",
				sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Kristi", "Phase2_Tiffany", sb);
		sb.append("\n\n");

		this.printClassificationStatistics("Phase2_Kristi", "Phase2_Holly", sb);
		sb.append("\n\n");
		this.printVariableStatistics("Phase2_Kristi", "Phase2_Holly", sb);
		sb.append("\n\n");

		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar
				+ "ReadmissionClassificationStatistics.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printClassificationStatistics(String a1, String a2,
			StringBuffer sb) {
		ReadmissionPatientClassificationTable t1 = this.getTable(a1);
		ReadmissionPatientClassificationTable t2 = this.getTable(a2);
		Hashtable<String, Integer> chash = new Hashtable();
		String line = "\n\n" + a1 + " vs " + a2 + "\n";
		line += "Variable|Concept|TP|FP|TN|FN|F-Measure\n";
		sb.append(line);
		boolean withMoonstone = a2.toLowerCase().contains("moonstone");
		this.getMatchCounts(t1, t2, chash, withMoonstone);
		for (String vname : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			Vector<String> classifications = this.moonstone.getReadmission()
					.getTypeAttributeValues(vname);
			for (String c : classifications) {
				String key = vname + Separator + c + Separator + "TP";
				float tp = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "FP";
				float fp = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "TN";
				float tn = HUtils.getCount(chash, key);
				key = vname + Separator + c + Separator + "FN";
				float fn = HUtils.getCount(chash, key);
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
				float fmeasure = getFmeasure(tp, tn, fp, fn);
				float kappa = getCohensKappa(tp, tn, fp, fn);
				line = vname + PrintSeparator + c + PrintSeparator + (int) tp
						+ PrintSeparator + (int) fp + PrintSeparator + (int) tn
						+ PrintSeparator + (int) fn + PrintSeparator + fmeasure
						+ "\n";
				sb.append(line);
			}
		}
	}

	public void printVariableStatistics(String a1, String a2, StringBuffer sb) {
		ReadmissionPatientClassificationTable t1 = this.getTable(a1);
		ReadmissionPatientClassificationTable t2 = this.getTable(a2);
		Hashtable<String, Integer> chash = new Hashtable();
		String line = a1 + " vs " + a2 + "\n";
		line += "Variable|TP|FP|TN|FN|F-Measure\n";
		sb.append(line);
		boolean withMoonstone = a2.toLowerCase().contains("moonstone");
		this.getMatchCounts(t1, t2, chash, withMoonstone);
		for (String vname : this.moonstone.getReadmission().getSchema()
				.getRelevantTypes()) {
			String key = vname + Separator + "TP";
			float tp = HUtils.getCount(chash, key);
			key = vname + Separator + "FP";
			float fp = HUtils.getCount(chash, key);
			key = vname + Separator + "TN";
			float tn = HUtils.getCount(chash, key);
			key = vname + Separator + "FN";
			float fn = HUtils.getCount(chash, key);

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
			float fmeasure = getFmeasure(tp, tn, fp, fn);
			float kappa = getCohensKappa(tp, tn, fp, fn);
			line = vname + PrintSeparator + (int) tp + PrintSeparator
					+ (int) fp + PrintSeparator + (int) tn + PrintSeparator
					+ (int) fn + PrintSeparator + fmeasure + "\n";
			sb.append(line);
		}
	}

	public void getMatchCounts(ReadmissionPatientClassificationTable t1,
			ReadmissionPatientClassificationTable t2,
			Hashtable<String, Integer> chash, boolean withMoonstone) {
		Vector<String> patients = t1.getPatients();
		Vector<String> variables = t1.getVariables();
		if (patients != null && variables != null) {
			for (String pname : patients) {
				for (String vname : variables) {
					String cstr = null;
					String c1 = t1.getClassification(pname, vname);
					String c2 = t2.getClassification(pname, vname);

					if (c1 == null || c2 == null) {
						continue;
					}
					boolean isNoMention = c1.toLowerCase().contains("mention");
					// Note: I didn't train on "no mention"; therefore
					// it artificially lowers results to include these
					// cases...
					if (withMoonstone && isNoMention) {
						continue;
					}

					if (isNoMention) {
						int x = 1;
					}

					if (c1.equals(c2)) {
						String key = vname + Separator + "TP";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + c1 + Separator + "TP";
						HUtils.incrementCount(chash, key);
					} else {
						String key = vname + Separator + "FN";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + "FP";
						HUtils.incrementCount(chash, key);

						key = vname + Separator + c1 + Separator + "FN";
						HUtils.incrementCount(chash, key);
						key = vname + Separator + c2 + Separator + "FP";
						HUtils.incrementCount(chash, key);

					}
				}
			}
		}
	}

	// Before 4/27/2017
	// public void getMatchCounts(ReadmissionPatientClassificationTable t1,
	// ReadmissionPatientClassificationTable t2,
	// Hashtable<String, Integer> chash) {
	// Vector<String> patients = t1.getPatients();
	// Vector<String> variables = t1.getVariables();
	// if (patients != null && variables != null) {
	// for (String pname : patients) {
	// for (String vname : variables) {
	// String cstr = null;
	// String c1 = t1.getClassification(pname, vname);
	// String c2 = t2.getClassification(pname, vname);
	// if (c1 != null) {
	// if (c1.equals(c2)) {
	// cstr = "TP";
	// } else if (c2 != null) {
	// cstr = "FP";
	// } else {
	// cstr = "FN";
	// }
	// } else if (c2 != null) {
	// cstr = "FP";
	// } else {
	// cstr = "TN";
	// }
	// if (cstr != null) {
	// String key = vname + Separator + cstr;
	// HUtils.incrementCount(chash, key);
	// }
	// }
	// }
	// }
	// }

	public void addTableFromEHost() {
		String aname = this.moonstone.getStartupParameters().getPropertyValue(
				AnnotationSetDescription);
		if (aname != null) {
			ReadmissionCorpusProcessor rcp = new ReadmissionCorpusProcessor(
					this.moonstone, false);
			ReadmissionPatientResults rpr = new ReadmissionPatientResults(rcp,
					this.moonstone);
			ReadmissionEHostPatientResults repr = new ReadmissionEHostPatientResults(
					rpr);
			this.addTableFromEHost(aname, repr);
			this.writeTableFiles();
			System.out.println(".. Finished");
		}
	}

	public void addTableFromEHost(String aname,
			ReadmissionEHostPatientResults repr) {
		ReadmissionPatientClassificationTable t = this.getTable(aname);
		t.extractTableObjectsFromEHost(repr);
	}

	public void addTableFromFile(File f) {
		String aname = f.getName();
		ReadmissionPatientClassificationTable t = this.getTable(aname);
		t.readTableFile(f);
	}

	public void addTablesFromWEKA() {
		ReadmissionCorpusProcessor rpc = new ReadmissionCorpusProcessor(
				this.moonstone, false);
		String aname = "moonstone";
		ReadmissionPatientClassificationTable t = this.getTable(aname, true);
		Hashtable<String, Integer> pvhash = new Hashtable();
		Hashtable<Integer, String> phash = new Hashtable();
		Hashtable<String, String> pchash = new Hashtable();
		for (String variable : ReadmissionCorpusProcessor.SalomehVariables) {
			this.applyWEKATrainingToTestData(variable, pchash);
		}
		for (Enumeration<String> e = pchash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			String[] strs = key.split(":");
			String vname = strs[0];
			String pname = strs[1];
			String cname = pchash.get(key);
			if (cname != null) {
				t.addTableObject(pname, vname, cname);
			}
		}
		this.writeTableFiles();
	}

	public void applyWEKATrainingToTestData(String variable,
			Hashtable<String, String> pchash) {
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

			fc.buildClassifier(traindata);

			for (int i = 0; i < testdata.numInstances(); i++) {
				Instance instance = testdata.instance(i);
				double pred = fc.classifyInstance(instance);
				double pd = instance.value(0);
				int pid = Integer.valueOf((int) pd);
				String pname = String.valueOf(pid);
				String key = variable + ":" + pname;
				int cvalue = (int) instance.classValue();
				String classification = testdata.classAttribute().value(cvalue);
				String classification2 = testdata.classAttribute().value(
						(int) pred);
				pchash.put(key, classification2);

				System.out.println("Patid=" + pname + ", Variable=" + variable
						+ ", Classification=" + classification
						+ ",Classification2=" + classification2);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readTableFiles() {
		try {
			String dname = this.moonstone.getStartupParameters()
					.getPropertyValue(ReadmissionPatientTableDirectory);
			String dpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + dname;
			Vector<File> files = FUtils.readFilesFromDirectory(dpath);
			if (files != null) {
				for (File f : files) {
					this.addTableFromFile(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTableFiles() {
		String dname = this.moonstone.getStartupParameters().getPropertyValue(
				ReadmissionPatientTableDirectory);
		dname = ReadmissionPatientTableDirectory; // temp

		if (dname != null) {
			int x = 1;
			String dpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + dname;
			for (ReadmissionPatientClassificationTable t : this.tableHash
					.values()) {
				t.sortTableObjects();
				String aname = t.getAnnotator();
				String fpath = dpath + File.separatorChar + aname;
				FUtils.writeFile(fpath, t.toString());
			}
		}
	}

	public ReadmissionPatientClassificationTable getTable(String aname) {
		return this.getTable(aname, true);
	}

	public ReadmissionPatientClassificationTable getTable(String aname,
			boolean create) {
		ReadmissionPatientClassificationTable t = this.tableHash.get(aname);
		if (t == null && create) {
			t = new ReadmissionPatientClassificationTable(this, aname);
			this.tableHash.put(aname, t);
		}
		return t;
	}

	public static float getFmeasure(float tp, float tn, float fp, float fn) {
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

	public static float getCohensKappa(float tp, float tn, float fp, float fn) {
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
		if (den > 0) {
			return num / den;
		}
		return 0;
	}

	// ////////////////////////////////////////////
	// 6/15/2017 After communication with Samir.

	public void doBoostrapTest() {
		StringBuffer sb = new StringBuffer();
		FeatureSet.loadFeatureSet(this.moonstone);
		for (String vname : ARFFFeature.ReadmissionVariables) {
			Hashtable<String, Integer> tchash = new Hashtable();
			Hashtable<String, Integer> mchash = new Hashtable();
			for (int i = 0; i < NumberOfBootstrapIterations; i++) {
				System.out.println("Bootstrap: " + vname + ": Iteration " + i);
				this.doBoostrapTest(vname, tchash, mchash);
			}
			for (String concept : tchash.keySet()) {
				float numTotal = HUtils.getCount(tchash, concept);
				float tp = HUtils.getCount(mchash, concept);
				float fp = numTotal - tp;
				float fn = fp;
				float tn = 0;

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
				float fmeasure = getFmeasure(tp, tn, fp, fn);
				float kappa = getCohensKappa(tp, tn, fp, fn);
				String line = vname + PrintSeparator + concept + PrintSeparator
						+ (int) tp + PrintSeparator + (int) fp + PrintSeparator
						+ (int) tn + PrintSeparator + (int) fn + PrintSeparator
						+ fmeasure + "\n";
				sb.append(line);
			}
		}
		String fname = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + "ARFF-Bootstrap-Results"
				+ File.separatorChar
				+ "ReadmissionBootstrapClassificationStatistics.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void doBoostrapTest(String vname, Hashtable<String, Integer> tchash,
			Hashtable<String, Integer> mchash) {
		Hashtable<String, String> hash70 = new Hashtable();
		Hashtable<String, String> hash30 = new Hashtable();
		Hashtable<String, String> pahash = new Hashtable();
		Hashtable<String, Vector<ARFFPatientVectorVariable>> pvhash = new Hashtable();
		this.readPatientVectorVariableFile(vname, pvhash, pahash);
		int numPatients = pvhash.keySet().size();
		int num70 = (int) (0.7 * numPatients);
		int num30 = (int) (0.3 * numPatients);
		Vector<String> allpatids = HUtils.getKeys(pvhash);
		int pcount = 0;
		Random rn = new Random();
		while (pcount++ < num70) {
			int index = rn.nextInt(num70 + 1);
			String patid = allpatids.elementAt(index);
			hash70.put(patid, patid);
		}
		for (String patid : allpatids) {
			if (hash70.get(patid) == null) {
				hash30.put(patid, patid);
			}
		}
		this.apply_70_30_Test(vname, pvhash, pahash, hash70, hash30, tchash,
				mchash);
	}

	// mchash = "match count hash", tchash = "total count hash"
	private void apply_70_30_Test(String vname,
			Hashtable<String, Vector<ARFFPatientVectorVariable>> pvhash,
			Hashtable<String, String> pahash, Hashtable<String, String> hash70,
			Hashtable<String, String> hash30,
			Hashtable<String, Integer> tchash, Hashtable<String, Integer> mchash) {
		try {
			boolean ignoreNoMention = this.moonstone.getStartupParameters()
					.isPropertyTrue("ReadmissionIgnoreNoMention");
			String trainfpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + "WEKA_70_30_Directory"
					+ File.separatorChar + "_TempTrainingFile";
			String testfpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + "WEKA_70_30_Directory"
					+ File.separatorChar + "_TempTestFile";
			FeatureSet fs = FeatureSet.CurrentFeatureSet;
			StringBuffer sb = new StringBuffer();
			String header = ARFFPatientVectorVariable.getARFFHeader(
					fs.featureDefinitionVector, vname);
			sb.append(header);
			for (String pname : hash70.keySet()) {
				Vector<ARFFPatientVectorVariable> v = pvhash.get(pname);
				for (ARFFPatientVectorVariable apvv : v) {
					if (ignoreNoMention && apvv.getAnswer().contains("mention")) {
						continue;
					}
					String str = apvv
							.toRegularizedFeatureStringWithAnswer(true);
					sb.append(str + "\n");
				}
			}
			FUtils.writeFile(trainfpath, sb.toString());

			sb = new StringBuffer();
			sb.append(header);
			for (String pname : hash30.keySet()) {
				Vector<ARFFPatientVectorVariable> v = pvhash.get(pname);
				for (ARFFPatientVectorVariable apvv : v) {
					if (ignoreNoMention && apvv.getAnswer().contains("mention")) {
						continue;
					}
					String str = apvv
							.toRegularizedFeatureStringWithAnswer(false);
					sb.append(str + "\n");
				}
			}
			FUtils.writeFile(testfpath, sb.toString());

			String firstpname = (String) HUtils.getKeys(pvhash).elementAt(0);
			Vector<ARFFPatientVectorVariable> v = pvhash.get(firstpname);
			ARFFPatientVectorVariable apvv = v.firstElement();
			// int fcount = apvv.getPatientVector().getFeatureArraySize();

			int[] rmarray = new int[] { 1 };
			rmarray = new int[] { 0 };

			// rmarray = this.getRelevantFeatureIndices(fs, vname, apvv);

			BufferedReader reader = new BufferedReader(new FileReader(
					trainfpath));
			int x = 1;
			Instances traindata = new Instances(reader);
			reader.close();
			traindata.setClassIndex(traindata.numAttributes() - 1);

			reader = new BufferedReader(new FileReader(testfpath));
			Instances testdata = new Instances(reader);
			reader.close();
			testdata.setClassIndex(testdata.numAttributes() - 1);

			Classifier c = new weka.classifiers.meta.MultiClassClassifier();
			String[] options = weka.core.Utils
					.splitOptions("-M 0 -R 2.0 -S 1 -W weka.classifiers.functions.Logistic -- -R 1.0E-8 -M -1");
			c.setOptions(options);

			rmarray = this.getIrrelevantFeatureIndices(fs, vname, apvv);
			Remove rm = new Remove();
			rm.setAttributeIndicesArray(rmarray);
			rm.setInvertSelection(false);
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(c);
			fc.setFilter(rm);

			fc.buildClassifier(traindata);

			for (int i = 0; i < testdata.numInstances(); i++) {
				Instance instance = testdata.instance(i);
				double pred = fc.classifyInstance(instance);
				double pd = instance.value(0);
				int pid = Integer.valueOf((int) pd);
				String pname = String.valueOf(pid);
				String key = vname + ":" + pname;
				int cvalue = (int) instance.classValue();
				String answer = pahash.get(key);
				// String answer = testdata.classAttribute().value(cvalue);
				String classification = testdata.classAttribute().value(
						(int) pred);
				HUtils.incrementCount(tchash, answer);
				if (answer.equals(classification)) {
					HUtils.incrementCount(mchash, answer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int[] getIrrelevantFeatureIndices(FeatureSet fs, String vname,
			ARFFPatientVectorVariable apvv) {
		Vector<Integer> irrelevants = new Vector(0);
		int fcount = fs.getFeatureDefinitionVector().getNumberOfFeatures();
		int fsubcount = apvv.getPatientVector().getNumberOfSubFeatures();
		int pvarraysize = apvv.getPatientVector().getFeatureArraySize();
		irrelevants.add(new Integer(0));
		for (int i = 0; i < pvarraysize; i++) {
			int findex = i / fsubcount;
			Feature f = fs.getFeatureDefinitionVector().getFeature(findex);
			if (!fs.getFeatureDefinitionVector().isFeatureRelevantToVariable(
					vname, f.getContent())) {
				irrelevants = VUtils.addIfNot(irrelevants, new Integer(i + 1));
			}
		}
		int[] irrelevantArray = new int[irrelevants.size()];
		for (int i = 0; i < irrelevants.size(); i++) {
			irrelevantArray[i] = irrelevants.elementAt(i);
		}
		return irrelevantArray;
	}

	private int[] getRelevantFeatureIndices(FeatureSet fs, String vname,
			ARFFPatientVectorVariable apvv) {
		Vector<Integer> relevants = new Vector(0);
		int fcount = fs.getFeatureDefinitionVector().getNumberOfFeatures();
		int fsubcount = apvv.getPatientVector().getNumberOfSubFeatures();
		int pvarraysize = apvv.getPatientVector().getFeatureArraySize();
		for (int i = 0; i < pvarraysize; i++) {
			int findex = i / fsubcount;
			Feature f = fs.getFeatureDefinitionVector().getFeature(findex);
			if (fs.getFeatureDefinitionVector().isFeatureRelevantToVariable(
					vname, f.getContent())) {
				relevants = VUtils.addIfNot(relevants, new Integer(i + 1));
			}
		}
		relevants.add(new Integer(pvarraysize));
		int[] relevantArray = new int[relevants.size()];
		for (int i = 0; i < relevants.size(); i++) {
			relevantArray[i] = relevants.elementAt(i);
		}
		return relevantArray;
	}

	public void readPatientVectorVariableFile(String vname,
			Hashtable<String, Vector<ARFFPatientVectorVariable>> pvhash,
			Hashtable<String, String> pahash) {
		String WEKAfname = this.moonstone.getResourceDirectoryName()
				+ File.separatorChar + "WEKA_70_30_Directory"
				+ File.separatorChar + "ARFF_" + vname + ".arff";
		File f = new File(WEKAfname);
		if (f.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = null;
				int x = 1;
				int lineoffset = 0;
				char[] quotes = new char[] { '"' };
				while ((line = in.readLine()) != null) {
					if (line.length() > 10 && Character.isDigit(line.charAt(0))) {
						String[] strs = line.split(",");
						String pname = strs[0];
						String answer = strs[strs.length - 1];
						ARFFPatientVectorVariable apvv = new ARFFPatientVectorVariable(
								pname, vname, answer);
						for (int i = 1; i < strs.length - 1; i++) {
							int count = Integer.valueOf(strs[i]).intValue();
							apvv.getPatientVector().setFeatureCount(i, count);
						}
						VUtils.pushHashVector(pvhash, pname, apvv);
						String key = vname + ":" + pname;
						String quoteless = StrUtils.trim(answer, quotes, false);
						pahash.put(key, quoteless);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
