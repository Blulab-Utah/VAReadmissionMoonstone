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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.javafunction.JavaFunctions;
import moonstone.learning.feature.ARFFPatientVector;
import moonstone.learning.feature.ARFFPatientVectorVariable;
import moonstone.learning.feature.FeatureSet;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;

public class ReadmissionPatientResults {

	public ReadmissionCorpusProcessor processor = null;
	public MoonstoneRuleInterface moonstone = null;
	String tsetname = null;
	public ReadmissionEHostPatientResults ehostPatientResults = null;
	public MoonstoneEHostXML mexml = null;
	public Hashtable<String, Integer> statisticsCountHash = new Hashtable();
	public Hashtable<String, Vector<ReadmissionSnippetResult>> snippetEHostVariableHash = new Hashtable();
	public Hashtable<String, Vector<ReadmissionSnippetResult>> generalSnippetResultHash = new Hashtable();
	public Hashtable<String, Vector<ReadmissionSnippetResult>> patientSnippetResultHash = new Hashtable();
	public Hashtable<String, Object> patientSummaryResultHash = new Hashtable();
	public Hashtable<String, String> patientNameHash = new Hashtable();

	// 4/16/2018:
	public Hashtable<String, Integer> patientClassifierLineFeatureCountHash = new Hashtable();
	public Hashtable<String, String> patientClassifierAnswerHash = new Hashtable();

	public Hashtable<String, Vector<ReadmissionSummaryResult>> generalSummaryResultHash = new Hashtable();
	public static String[] RejectDocumentNameStrings = { "SUICIDE", "suicide",
			"7220_800336985906_NURSING NOTE", "ICU NURSING ASSESSMENT",
			"NURSING SHIFT ASSESSMENT [DT] NSA", "NURS CARE MANAGER NOTE",
			"NURSE INTRAOPERATIVE REPORT", "transitional" };

	public static String patientClassificationLineFeatureFilename = "PatientClassificationLineFeatureFile";
	public static String patientClassificationVariableAnswerFilename = "PatientClassificationVariableAnswerFile";

	public ReadmissionPatientResults(ReadmissionCorpusProcessor processor,
			MoonstoneRuleInterface moonstone) {
		this.processor = processor;
		this.moonstone = moonstone;
		this.mexml = new MoonstoneEHostXML(moonstone);
	}

	public ReadmissionPatientResults(ReadmissionCorpusProcessor processor,
			MoonstoneRuleInterface moonstone, String tsetname) {
		this.processor = processor;
		this.moonstone = moonstone;
		this.tsetname = tsetname;
		this.mexml = new MoonstoneEHostXML(moonstone);
		this.analyzeTrainingSet();
	}

	public void analyzeTrainingSet() {
		int x = 1;
		this.ehostPatientResults = new ReadmissionEHostPatientResults(this);
		this.processPatients();
		// if (!this.processor.doTuffy) {
		// // this.printExcelStatisticsAllMeasures(null);
		// this.updateLearningPatientTrainingAnswers();
		// }
	}

	public void processPatients() {
		int fmode = this.processor.fileOrganizationMode;
		if (fmode == ReadmissionCorpusProcessor.CombinedFileOrganizationMode) {
			this.processFilesSingleLayerCombined();
		} else if (fmode == ReadmissionCorpusProcessor.MultiLayerFileOrganizationMode) {
			this.processFilesMultiLayer();
		} else if (fmode == ReadmissionCorpusProcessor.SingleFileOrganizationMode) {
			this.processFilesSingleLayer();
		}
		if (!this.processor.doARFF) {
			Vector<String> pnames = HUtils.getKeys(this.patientNameHash);
			if (pnames != null) {
				Collections
						.sort(pnames,
								new ReadmissionPatientResults.IntegerPatientNameSorter());

				for (String pname : pnames) {
					for (String variable : this.processor.moonstone
							.getReadmission().getSchema().getRelevantTypes()) {
						String attribute = this.processor.moonstone
								.getReadmission().getRelevantTypeAttribute(
										variable);
						Vector<String> evalues = this.processor.moonstone
								.getReadmission().getTypeAttributeValues(
										variable);
						for (String evalue : evalues) {
							ReadmissionSummaryResult rsr = new ReadmissionSummaryResult(
									this, pname, variable, attribute, evalue);
						}
					}
				}
				if (!this.processor.doTuffy) {
					this.storeSalomehPatientStatistics();
					// for (String pname : pnames) {
					// // this.writePatientXML(pname);
					// }
				}
			}
		}
	}

	public void writePatientXML(String pname) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		String fname = "A.Patient Level Review_" + pname + ".txt.knowtator.xml";
		String htmlfilename = StrUtils.textToHtml(fname);
		xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
		Vector<ReadmissionSummaryResult> results = (Vector<ReadmissionSummaryResult>) this.patientSummaryResultHash
				.get(pname);
		if (results != null) {
			for (ReadmissionSummaryResult result : results) {
				if (result.isConfirmedAverage()) {
					xml += result.toXML();
				}
			}
		}
		xml += "</annotations>\n";
		String outputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("PatientResultsOutputDirectory");
		String sname = outputdirname + File.separatorChar + fname;
		FUtils.writeFile(sname, xml);
	}

	// For validation files, single layer
	public void processFilesSingleLayer() {
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String outputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("ResultsOutputDirectory");
		String startstr = this.moonstone.getStartupParameters()
				.getPropertyValue("ReadmissionFileStartIndex");
		String endstr = this.moonstone.getStartupParameters().getPropertyValue(
				"ReadmissionFileEndIndex");
		Hashtable<Integer, Integer> patentIDHash = new Hashtable();

		int pcount = -1;
		// int pcount = getPatientCount(inputdirname);

		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}

		Vector<Integer> pids = new Vector(0);
		File sourcedir = new File(inputdirname);
		File[] files = sourcedir.listFiles();

		System.out.println("About to process " + files.length + " files...");

		int fileStartIndex = 0;
		int fileEndIndex = files.length;

		if (startstr != null && endstr != null) {
			fileStartIndex = Integer.parseInt(startstr);
			fileEndIndex = Integer.parseInt(endstr);
		}
		String rangestr = fileStartIndex + "_" + fileEndIndex;

		Hashtable phash = new Hashtable();
		for (int i = fileStartIndex; i < fileEndIndex; i++) {
			File file = files[i];
			String fname = file.getName().toLowerCase();
			int index = fname.indexOf('_');
			if (index > 0) {
				String pname = fname.substring(0, index);
				try {
					phash.put(pname, Integer.parseInt(pname));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		pids = HUtils.getElements(phash);
		Collections.sort(pids);
		Vector<Integer> missing = null;
		int last = 0;
		for (Enumeration<Integer> e = pids.elements(); e.hasMoreElements();) {
			Integer current = e.nextElement();
			if (last < current - 1) {
				for (int i = last + 1; i < current; i++) {
					missing = VUtils.add(missing, i);
				}
			}
			last = current;
		}

		if (missing != null) {
			String fname = this.processor.moonstone.getResourceDirectoryName()
					+ File.separatorChar + "MissingPatients_" + rangestr;
			StringBuffer sb = new StringBuffer();
			int total = pids.size() + missing.size();
			sb.append("Total Patients =" + total + "\n");
			sb.append("Total Processed Patients = " + pids.size() + "\n");
			sb.append("Missing: ");
			sb.append("Total Missing patients = " + missing.size() + "\n");
			for (Enumeration<Integer> e = missing.elements(); e
					.hasMoreElements();) {
				Integer pid = e.nextElement();
				sb.append(pid + "\n");
			}
			FUtils.writeFile(fname, sb.toString());
		}
		phash = null;
		pids = null;
		missing = null;

		// if (1 == 1)
		// return;

		boolean atStart = true;
		for (int i = fileStartIndex; i < fileEndIndex; i++) {
			File file = files[i];
			if (doRejectFileName(file.getName())) {
				continue;
			}
			String fname = file.getName().toLowerCase();
			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}
			fcount++;
			String text = FUtils.readFile(file);
			Document doc = new Document(file.getName(), text);

			Readmission prm = this.processor.moonstone.getReadmission();
			if (doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats)) {
				this.processor.totalFilesProcessed++;
				System.out.print("Processing file " + i + ": " + tsetname + ":"
						+ doc.getName() + "...");
				long start = System.currentTimeMillis();
				String pname = doc.getPatientName();
				this.patientNameHash.put(pname, pname);
				if (this.processor.doTuffy) {
					this.invokeMoonstoneTuffy(doc);
				} else if (this.processor.doARFF) {
					this.invokeMoonstoneARFF(doc);
				} else {
					this.invokeMoonstone(doc);
				}
				atStart = false;
				long end = System.currentTimeMillis();
				long milli = end - start;
				System.out
						.println("Done.  Elapsed: " + milli + " milliseconds");
			}

			doc.releaseText();
		}
		this.processor.totalPatients += this.patientNameHash.size();
		System.out.println("Initial pcount=" + pcount + ", processed pcount = "
				+ this.processor.totalPatients + "\n\n");
	}

	// For validation files, single layer
	public void processFilesSingleLayerCombined() {
		int x = 1;
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TrainingTextInputDirectory");
		if (inputdirname == null) {
			inputdirname = this.moonstone.getStartupParameters()
					.getPropertyValue("TextInputDirectory");
		}
		String outputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("ResultsOutputDirectory");

		String reportConstraint = this.moonstone.getStartupParameters()
				.getPropertyValue("ReportPathnameConstraintString");
		if (reportConstraint == null) {
			reportConstraint = "corpus";
		}

		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}

		Vector<Integer> pids = new Vector(0);
		File sourcedir = new File(inputdirname);

		System.out.println("About to read Corpus files...");
		// 12/11/2017 Added parameter for filename constraint
		Vector<File> files = FUtils.readFilesFromDirectory(inputdirname,
				reportConstraint, null);

		System.out.println("About to process " + files.size() + " files...");
		Collections.sort(files, new FilePatientNameSorter());
		String lastpname = null;

		int startFileID = 0;
		String startFileIDStr = this.processor.moonstone.getStartupParameters()
				.getPropertyValue("ReadmissionStartFileID");
		if (startFileIDStr != null) {
			startFileID = Integer.parseInt(startFileIDStr);
		}

		for (File file : files) {
			if (doRejectFileName(file.getName())) {
				continue;
			}
			String fname = file.getName().toLowerCase();
			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}

			// 5/29/2017
			if (fcount++ < startFileID) {
				continue;
			}
			String text = FUtils.readFile(file);
			Document doc = new Document(file.getName(), text);

			Readmission prm = this.processor.moonstone.getReadmission();
			if (doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats)) {
				this.processor.totalFilesProcessed++;
				System.out.print("Processing file " + fcount + ": " + tsetname
						+ ":" + doc.getName() + "...");
				long start = System.currentTimeMillis();
				String pname = doc.getPatientName();
				this.patientNameHash.put(pname, pname);
				if (this.processor.doTuffy) {
					this.invokeMoonstoneTuffy(doc);
				} else if (this.processor.doARFF) {
					this.invokeMoonstoneARFF(doc);
				} else {
					this.invokeMoonstone(doc);
				}
				if (lastpname != null && !lastpname.equals(pname)) {
					this.processor.writeandClearARFFFileTables();
				}
				lastpname = pname;
				long end = System.currentTimeMillis();
				long milli = end - start;
				System.out
						.println("Done.  Elapsed: " + milli + " milliseconds");
			}
			doc.releaseText();
		}
		this.processor.totalPatients += this.patientNameHash.size();
		System.out.println("Processed pcount = " + this.processor.totalPatients
				+ "\n\n");
	}

	public static boolean doRejectFileName(String fname) {
		for (String str : RejectDocumentNameStrings) {
			if (fname.contains(str)
					|| fname.toLowerCase().contains(str.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static Vector<Integer> getMissingPatients(File[] files) {
		Vector<Integer> pids = new Vector(0);
		Hashtable phash = new Hashtable();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String fname = file.getName().toLowerCase();
			int index = fname.indexOf('_');
			if (index > 0) {
				String pname = fname.substring(0, index);
				phash.put(pname, Integer.parseInt(pname));
			}
		}
		pids = HUtils.getElements(phash);
		Collections.sort(pids);
		Vector<Integer> missing = null;
		int last = 0;
		for (Enumeration<Integer> e = pids.elements(); e.hasMoreElements();) {
			Integer current = e.nextElement();
			if (last < current - 1) {
				for (int i = last + 1; i < current; i++) {
					missing = VUtils.add(missing, i);
				}
			}
			last = current;
		}
		return missing;
	}

	// Before single layer, 12/23/2016
	public void processFilesMultiLayer() {
		String inputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String outputdirname = this.moonstone.getStartupParameters()
				.getPropertyValue("ResultsOutputDirectory");

		int pcount = -1;
		// int pcount = getPatientCount(inputdirname);

		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}
		Vector<File> files = FUtils.readFilesFromDirectory(inputdirname);

		this.processor.totalFilesEncountered += files.size();

		for (File file : files) {
			String fname = file.getName().toLowerCase();
			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}
			if (!fname.endsWith(".txt") || fname.contains("xml")
					|| fname.contains("knowtator")) {
				continue;
			}
			fcount++;
			String text = FUtils.readFile(file);
			Document doc = new Document(file.getName(), text);

			Readmission prm = this.processor.moonstone.getReadmission();
			if (doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats)) {
				this.processor.totalFilesProcessed++;
				System.out.print("Processing file: " + tsetname + ":"
						+ doc.getName() + "...");
				long start = System.currentTimeMillis();
				String pname = doc.getPatientName();
				this.patientNameHash.put(pname, pname);
				if (this.processor.doTuffy) {
					this.invokeMoonstoneTuffy(doc);
				} else if (this.processor.doARFF) {
					this.invokeMoonstoneARFF(doc);
				} else {
					this.invokeMoonstone(doc);
				}
				long end = System.currentTimeMillis();
				long milli = end - start;
				System.out
						.println("Done.  Elapsed: " + milli + " milliseconds");
				if (milli > 2000) {
					int x = 1;
				}
			}

			doc.releaseText();
		}
		this.processor.totalPatients += this.patientNameHash.size();
		System.out.println("Initial pcount=" + pcount + ", processed pcount = "
				+ this.processor.totalPatients + "\n\n");
	}

	private class RunMoonstone implements Runnable {

		private ReadmissionPatientResults patientResults = null;
		private String tsetname = null;
		private Document document = null;

		private RunMoonstone(ReadmissionPatientResults rpr, Document doc) {
			this.patientResults = rpr;
			this.document = doc;
		}

		public void run() {
			Readmission rm = this.patientResults.moonstone.getReadmission();
			if (this.document
					.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats)) {
				System.out.print("Processing file (THREAD): " + tsetname + ":"
						+ this.document.getName() + "...");
				long start = System.currentTimeMillis();
				String pname = this.document.getPatientName();
				this.patientResults.patientNameHash.put(pname, pname);
				if (this.patientResults.processor.doTuffy) {
					this.patientResults.invokeMoonstoneTuffy(this.document);
				} else {
					this.patientResults.invokeMoonstone(this.document);
				}

				// Add ARFF process call here somehow...

				long end = System.currentTimeMillis();
				System.out.println("Done.  Elapsed: " + (end - start)
						+ " milliseconds");
			}
		}
	}

	public void invokeMoonstoneTuffy(Document document) {
		Vector<Annotation> annotations = this.moonstone
				.applyNarrativeGrammarToText(document, true, true, true);
		annotations = this.moonstone.getSentenceGrammar()
				.getDisplayedAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				this.addTuffyStringNEW(annotation);
			}
		}
	}

	public void invokeMoonstone(Document document) {
		Vector<Annotation> targets = this.mexml.gatherTargetAnnotations(
				this.moonstone.getReadmission(), document);
		Vector<Annotation> expanded = Annotation
				.getNonNestedNonCoincidingAnnotations(targets, true);
		if (expanded != null) {
			for (Annotation target : expanded) {
				String concept = target.getConcept().toString();
				boolean isNegated = JavaFunctions.isNegated(target);
				ReadmissionSnippetResult tsr = new ReadmissionSnippetResult(
						this, document, target);
			}
		}
		this.moonstone.releaseAnnotations();
	}

	// I'm using all annotations, including nested ones, increasing the numbers
	// of target concepts added as features. I will plan to remove nested,
	// coincident
	// annotations.
	public void invokeMoonstoneARFF_BEFORE_5_15_2017(Document document) {
		MoonstoneRuleInterface msri = this.processor.moonstone;
		Vector<Annotation> annotations = msri.applyNarrativeGrammarToText(
				document, false, false, false);
		if (annotations != null) {
			for (String variable : this.moonstone.getReadmission()
					.getAllEHostVariables()) {
				invokeMoonstoneARFF(document, variable, annotations);
			}
		}
		msri.releaseAnnotations();
	}

	// 6/1/2017: Use just document-level annotations stored in EHost.
	// (Find original in 5/31 source backup)
	public void invokeMoonstoneARFF(Document document) {
		MoonstoneRuleInterface msri = this.processor.moonstone;
		Vector<Annotation> annotations = this.mexml.gatherTargetAnnotations(
				msri.getReadmission(), document);
		if (annotations != null) {
			for (String variable : this.moonstone.getReadmission()
					.getAllEHostVariables()) {
				if (this.processor.doLinePatientFeatures) {
					invokeMoonstoneLinePatientFeatures(document, variable,
							annotations);
				} else if (this.processor.doARFF) {
					invokeMoonstoneARFF(document, variable, annotations);
				}
			}
		}
		msri.releaseAnnotations();
	}

	// 4/16/2018
	public void invokeMoonstoneLinePatientFeatures(Document document,
			String variable, Vector<Annotation> annotations) {
		String pname = document.getPatientName();
		String answer = "no mention";
		if (this.ehostPatientResults != null) {
			String rv = this.ehostPatientResults.getPatientClassification(
					pname, variable);
			if (rv != null) {
				answer = rv;
			}
		}
		String key = pname + "|" + variable;
		this.patientClassifierAnswerHash.put(key, answer);
		for (Annotation annotation : annotations) {
			storePatientClassifierLineFeatures(pname, variable, annotation);
		}
	}

	public void storePatientClassifierLineFeatures(String pname,
			String variable, Annotation annotation) {
		int x = 1;
		if (annotation.hasConcept() && !annotation.isNegated()) {
			String dname = annotation.getDocument().getName();
			int dayIndex = ARFFPatientVector.getDayOffset(annotation);
			String daystr = String.valueOf(dayIndex);
			int docTypeIndex = ARFFPatientVector
					.getDocumentTypeOffset(annotation);
			String docTypeStr = String.valueOf(docTypeIndex);
			String concept = annotation.getConcept().toString();
			String text = annotation.getText();
			String startend = annotation.getTextStart() + "-"
					+ annotation.getTextEnd();

			String feature = pname + "|" + concept + "|" + text + "|" + dname
					+ "|" + docTypeStr + "|" + daystr + "|" + startend;
			this.patientClassifierLineFeatureCountHash.put(feature, 1);

			// Before 4/30/2018
			// String key = pname + "|" + concept + "|" + docTypeStr
			// + "|" + daystr;
			// HUtils.incrementCount(this.patientClassifierLineFeatureCountHash,
			// key);

			if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					if (child.hasConcept() && !child.isNegated()) {
						storePatientClassifierLineFeatures(pname, variable,
								child);
					}
				}
			}
		}
	}

	public void writePatientClassifierLineFeatureFiles() {
		String dirname = this.processor.moonstone.getResourceDirectoryName();
		String featurefname = dirname + File.separatorChar
				+ patientClassificationLineFeatureFilename;
		String answerfname = dirname + File.separatorChar
				+ patientClassificationVariableAnswerFilename;
		StringBuffer linesb = new StringBuffer();
		StringBuffer answersb = new StringBuffer();
		Vector<String> features = HUtils
				.getKeys(this.patientClassifierLineFeatureCountHash);
		Collections.sort(features);
		Vector<String> answerkeys = HUtils
				.getKeys(this.patientClassifierAnswerHash);
		Collections.sort(answerkeys);
		for (String feature : features) {
			linesb.append(feature + "\n");
		}
		for (String key : answerkeys) {
			String classification = this.patientClassifierAnswerHash.get(key);
			String str = key + "=" + classification;
			answersb.append(str + "\n");
		}
		FUtils.writeFile(featurefname, linesb.toString());
		FUtils.writeFile(answerfname, answersb.toString());
	}

	// 4/16/2018
	public void writePatientClassifierLineFeatureFiles_BEFORE_4_30_2018() {
		String dirname = this.processor.moonstone.getResourceDirectoryName();
		String featurefname = dirname + File.separatorChar
				+ patientClassificationLineFeatureFilename;
		String answerfname = dirname + File.separatorChar
				+ patientClassificationVariableAnswerFilename;
		StringBuffer linesb = new StringBuffer();
		StringBuffer answersb = new StringBuffer();
		Vector<String> linekeys = HUtils
				.getKeys(this.patientClassifierLineFeatureCountHash);
		Collections.sort(linekeys);
		Vector<String> answerkeys = HUtils
				.getKeys(this.patientClassifierAnswerHash);
		Collections.sort(answerkeys);
		for (String key : linekeys) {
			int count = this.patientClassifierLineFeatureCountHash.get(key);
			String str = key + "=" + count;
			linesb.append(str + "\n");
		}
		for (String key : answerkeys) {
			String classification = this.patientClassifierAnswerHash.get(key);
			String str = key + "=" + classification;
			answersb.append(str + "\n");
		}
		FUtils.writeFile(featurefname, linesb.toString());
		FUtils.writeFile(answerfname, answersb.toString());
	}

	public void invokeMoonstoneARFF(Document document, String variable,
			Vector<Annotation> annotations) {
		String pname = document.getPatientName();
		String answer = "no mention";
		if (this.ehostPatientResults != null) {
			String rv = this.ehostPatientResults.getPatientClassification(
					pname, variable);
			if (rv != null) {
				answer = rv;
			}
		}
		String key = variable + ":" + pname;
		ARFFPatientVectorVariable apvv = this.processor.ARFFPatientVectorVariableHash
				.get(key);
		if (apvv == null) {
			apvv = new ARFFPatientVectorVariable(this, pname, variable, answer);
		}
		for (Annotation annotation : annotations) {
			apvv.addFeature(annotation, annotation.getConcept().toString());
		}
	}

	public void storeTuffyEvidence() {
		if (this.processor.TuffyEvidenceFilename != null) {
			String fpath = this.moonstone.getResourceDirectoryName()
					+ File.separatorChar + this.processor.TuffyEvidenceFilename;
			FUtils.writeFile(fpath, this.processor.tuffySB.toString());
		}
	}

	// 12/6/2016 Create WEKA ARFF file strings

	public void storeWEKAVariableFileStrings() {
		for (String variable : this.processor.moonstone.getReadmission()
				.getSchema().getRelevantTypes()) {
			Vector<String> evalues = this.processor.moonstone.getReadmission()
					.getTypeAttributeValues(variable);
			for (String evalue : evalues) {
				for (Enumeration<String> e = this.patientNameHash.keys(); e
						.hasMoreElements();) {
					String pname = e.nextElement();
					boolean confirmedInEHost = this.ehostPatientResults
							.patientHasClassification(pname, evalue);
					if (confirmedInEHost) {
						Vector<ReadmissionSnippetResult> sresults = this.patientSnippetResultHash
								.get(pname);
						if (sresults != null) {
							for (ReadmissionSnippetResult sresult : sresults) {
								String MoonstoneConcept = sresult.MoonstoneConcept;

							}
						}
					}
				}
			}
		}
	}

	public void storeSalomehPatientStatistics() {
		Vector<String> pnames = HUtils.getKeys(this.patientNameHash);
		Collections.sort(pnames, new IntegerPatientNameSorter());
		Readmission rm = this.processor.moonstone.getReadmission();
		for (String pname : pnames) {
			String line = pname;
			for (String variable : rm.getAllEHostVariables()) {
				line += "\t";
				String codestr = null;
				for (String classification : rm
						.getTypeAttributeValues(variable)) {
					String key = pname + "@@" + classification;
					ReadmissionSummaryResult rsr = (ReadmissionSummaryResult) this.patientSummaryResultHash
							.get(key);
					if (rsr != null && rsr.confirmedBestCategory) {
						codestr = this.processor.SalomehClassificationCodeHash
								.get(classification);
						break;
					}
				}
				if (codestr == null) {
					codestr = ".";
				}
				line += codestr;
			}
			line += "\n";
			this.processor.SalomehPatientResultsSB.append(line);
		}
	}

	// 12/7/2016
	public void storeSalomehPatientStatisticsOLD() {
		Vector<String> pnames = HUtils.getKeys(this.patientNameHash);
		Collections.sort(pnames);
		for (String pname : pnames) {
			Vector<ReadmissionSummaryResult> rsrs = (Vector<ReadmissionSummaryResult>) this.patientSummaryResultHash
					.get(pname);
			if (rsrs != null) {
				Collections
						.sort(rsrs,
								new ReadmissionSummaryResult.ReadmissionSummaryResultVariableValueSorter());
				for (ReadmissionSummaryResult rsr : rsrs) {
					int confirmed = rsr.confirmedBestCategory ? 1 : 0;
					String line = pname + "\t" + rsr.getSemanticVariable()
							+ "\t" + rsr.getEHostValue() + "\t" + confirmed
							+ "\n";
					this.processor.SalomehPatientResultsSB.append(line);
				}
			}
		}
	}

	public void printExcelStatisticsBestOnly(String header) {
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
			String ed = ReadmissionCorpusProcessor.ExcelDelimiter;
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
			line = casename + ed + evalue + ed + tp + ed + fp + ed + tn + ed
					+ fn + ed + accuracy + ed + sensitivity + ed + specificity
					+ ed + fscore + "\n";
			sb.append(line);
		}
		String fname = this.processor.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionPatientStatistics.txt";
		String ftext = FUtils.readFile(fname);
		StringBuffer newsb = new StringBuffer();
		if (ftext != null) {
			newsb.append(ftext);
		}
		newsb.append(sb.toString());
		FUtils.writeFile(fname, newsb.toString());
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
		String ed = ReadmissionCorpusProcessor.ExcelDelimiter;
		for (String evalue : this.getAllTargetEHostValues()) {
			line = null;
			for (String mlabel : ReadmissionCorpusProcessor.MatchMeasureLabels) {
				if (line == null) {
					line = casename + ed + evalue;
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
				line += ed + tp + ed + fp + ed + tn + ed + fn + ed + accuracy
						+ ed + sensitivity + ed + specificity + ed + fscore;
			}
			sb.append(line + "\n");
		}
		String fname = this.processor.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionPatientStatistics.txt";
		String ftext = FUtils.readFile(fname);
		StringBuffer newsb = new StringBuffer();
		if (ftext != null) {
			newsb.append(ftext);
		}
		newsb.append(sb.toString());
		FUtils.writeFile(fname, newsb.toString());
	}

	public void printVariableSummaryStatisticsBestOnly() {
		StringBuffer sb = new StringBuffer();
		String inputdirname = this.processor.moonstone.getStartupParameters()
				.getPropertyValue("TextInputDirectory");
		String casename = null;
		String line = "Variable|TP|FP|TN|FN|Accuracy|Sensitivity|SpecificityF-Measure\n";
		sb.append(line);
		String ed = ReadmissionCorpusProcessor.ExcelDelimiter;
		for (String generalType : this.processor.moonstone.getReadmission()
				.getSchema().getRelevantTypes()) {
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
			line = generalType + ed + tp + ed + fp + ed + tn + ed + fn + ed
					+ accuracy + ed + sensitivity + ed + specificity + ed
					+ fmeasure + "\n";
			sb.append(line);
		}
		String fname = this.processor.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionVariableStatistics.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void printVariableSummaryStatisticsAllMeasures() {
		StringBuffer sb = new StringBuffer();
		String casename = null;
		String line = "Variable|AverageTP|AverageFP|AverageTN|AverageFN|AverageAccuracy|AverageSensitivity|AverageSpecificity|AverageF-Measure|CommonTP|CommonFP|CommonTN|CommonFN|CommonAccuracy|CommonSensitivity|CommonSpecificity|CommonF-Measure|RecentTP|RecentFP|RecentTN|RecentFN|RecentAccuracy|RecentSensitivity|RecentSpecificity|RecentF-Measure\n";
		sb.append(line);
		line = null;
		String ed = ReadmissionCorpusProcessor.ExcelDelimiter;
		for (String generalType : this.processor.moonstone.getReadmission()
				.getSchema().getRelevantTypes()) {
			line = generalType;
			for (String mlabel : ReadmissionCorpusProcessor.MatchMeasureLabels) {
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
				line += ed + tp + ed + fp + ed + tn + ed + fn + ed + accuracy
						+ ed + sensitivity + ed + specificity + ed + fmeasure;
			}
			sb.append(line + "\n");
		}
		String fname = this.processor.moonstone.getResourceDirectoryName()
				+ File.separator + "ReadmissionVariableStatistics.txt";
		FUtils.writeFile(fname, sb.toString());
	}

	public void updateLearningPatientTrainingAnswers() {
		if (this.ehostPatientResults != null) {
			for (Enumeration<String> e = this.patientNameHash.keys(); e
					.hasMoreElements();) {
				String pname = e.nextElement();
				for (String variable : this.processor.moonstone
						.getReadmission().getSchema().getRelevantTypes()) {
					String attribute = this.processor.moonstone
							.getReadmission()
							.getRelevantTypeAttribute(variable);
					Vector<String> evalues = this.processor.moonstone
							.getReadmission().getTypeAttributeValues(variable);
					for (String evalue : evalues) {
						boolean confirmedInEHost = this.ehostPatientResults
								.patientHasClassification(pname, evalue);
						if (confirmedInEHost) {
							int x = 1;
						}
						int confirmed = confirmedInEHost ? 1 : 0;
						String str = pname + "," + evalue + "," + confirmed
								+ "\n";
						this.processor.trainingAnswerSB.append(str);
					}
				}
			}
		}
	}

	public void printLearningPatientTrainingAnswers() {
		String fname = this.processor.moonstone.getStartupParameters()
				.getPropertyValue("LearningPatientTrainingAnswers");
		if (fname != null) {
			String fpath = this.processor.moonstone.getResourceDirectoryName()
					+ File.separatorChar + fname;
			// FUtils.writeFile(fpath, this.trainingAnswerSB.toString());
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
		Readmission readmission = this.processor.moonstone.getReadmission();
		for (String type : readmission.getSchema().getRelevantTypes()) {
			String attribute = readmission.getRelevantTypeAttribute(type);
			Vector<String> evalues = readmission.getTypeAttributeValues(type);
			allEvalues = VUtils.appendNew(allEvalues, evalues);
		}
		return allEvalues;
	}

	public void addTuffyStringNEW(Annotation annotation) {
		MoonstoneRuleInterface msri = annotation.getMoonstoneRuleInterface();
		FeatureSet fset = FeatureSet.getCurrentFeatureSet(msri);
		String pname = annotation.getPatientName();
		StringBuffer sb = new StringBuffer();
		Vector<String> features = FeatureSet
				.getRelevantPropositionalContent(annotation);
		if (pname != null && features != null) {
			pname = "P_" + pname;
			for (String feature : features) {
				fset.featureDefinitionVector.addFeature(feature);
				feature = StrUtils.removeNonAlphaDigitCharacters(feature);
				String tpred = "AnnotationFeature(" + feature + ", " + pname
						+ ")";
				sb.append(tpred + "\n");
			}
			this.processor.tuffySB.append(sb.toString());
		}
	}

	public String convertOldToNewEHostValue(String evalue) {
		String newname = this.processor.oldToNewGrammarEHostValueConversionHash
				.get(evalue);
		if (newname != null) {
			evalue = newname;
		}
		return evalue;
	}

	public static class IntegerPatientNameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			int pid1 = Integer.parseInt((String) o1);
			int pid2 = Integer.parseInt((String) o2);
			if (pid1 < pid2) {
				return -1;
			}
			if (pid2 < pid1) {
				return 1;
			}
			return 0;
		}
	}

	// 5/13/2017: To move ARFF file update to patient level.
	public static class FilePatientNameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			File f1 = (File) o1;
			File f2 = (File) o2;
			return f1.getName().compareTo(f2.getName());
		}
	}

}
