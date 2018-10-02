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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import moonstone.annotation.Annotation;
import moonstone.io.ehost.MoonstoneEHostXML;
import moonstone.io.readmission.Readmission;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class FeatureSet {

	protected MoonstoneRuleInterface moonstoneRuleInterface = null;
	protected MoonstoneEHostXML moonstoneEHostXML = null;
	protected Readmission readmission = null;
	public FeatureDefinitionVector featureDefinitionVector = null;
	protected Hashtable<String, FeaturePatientVector> featurePatientVectorHash = new Hashtable();
	protected String featureDefinitionFileName = null;
	protected String patientTrainingVectorDirectoryName = null;
	protected String patientTestVectorDirectoryName = null;

	public static FeatureSet CurrentFeatureSet = null;

	public FeatureSet(MoonstoneRuleInterface msri, String lfdname,
			String lptstname, String lptrnname) {
		CurrentFeatureSet = this;
		this.moonstoneRuleInterface = msri;
		this.readmission = msri.getReadmission();
		this.moonstoneEHostXML = new MoonstoneEHostXML(msri);
		this.featureDefinitionFileName = lfdname;
		this.patientTrainingVectorDirectoryName = lptstname;
		this.patientTestVectorDirectoryName = lptstname;
		this.featureDefinitionVector = new FeatureDefinitionVector(this);
	}

	public static FeatureSet loadFeatureSet(MoonstoneRuleInterface msri) {
		String lfdname = "LearningFeatureDefinitions";
		String lptstname = "LearningPatientTrainingFeatureVectorDirectory";
		String lptrnname = "LearningPatientTestFeatureVectorDirectory";
		lfdname = msri.getResourceDirectoryName() + File.separatorChar
				+ lfdname;
		lptstname = msri.getResourceDirectoryName() + File.separatorChar
				+ lptstname;
		lptrnname = msri.getResourceDirectoryName() + File.separatorChar
				+ lptrnname;
		FeatureSet fs = new FeatureSet(msri, lfdname, lptstname, lptrnname);
		return fs;
	}

	public static FeatureSet getCurrentFeatureSet(MoonstoneRuleInterface msri) {
		if (CurrentFeatureSet == null) {
			loadFeatureSet(msri);
		}
		return CurrentFeatureSet;
	}

	public FeaturePatientVector getFeaturePatientVector(String pname) {
		return this.featurePatientVectorHash.get(pname);
	}

	public Vector<FeatureDefinitionVector> getAllPatientFeatureVectors() {
		return HUtils.getElements(this.featurePatientVectorHash);
	}

	public void processMultipleTrainingSets(boolean isTraining,
			boolean isDefinitions) {
		long start = System.currentTimeMillis();
		String featureDefinitionFileName = null;
		String patientTrainingVectorDirectoryName = null;
		String patientTestVectorDirectoryName = null;
		String tslfilestr = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue("AllTSLFiles");
		if (tslfilestr != null) {
			String[] fnames = tslfilestr.split(",");
			for (String fname : fnames) {
				fname += ".properties";
				System.out.println("About to initialize " + fname + "...");
				this.processTrainingSet(fname, isTraining, isDefinitions);
			}
			if (isDefinitions) {
				this.featureDefinitionVector.writeFeatureDefinitionFile();
			}
		}
		long end = System.currentTimeMillis();
		long minutes = ((end - start) / 1000) / 60;

		System.out.println("DONE.  Minutes=" + minutes);
	}

	public void processTrainingSet(String tsetname, boolean isTraining,
			boolean isDefinitions) {
		try {
			this.featurePatientVectorHash = new Hashtable();
			String rootdir = this.moonstoneRuleInterface.getStartupParameters()
					.getRootDirectory();
			String tslfilepath = rootdir + File.separatorChar + tsetname;
			File tslfile = new File(tslfilepath);
			Properties props = new Properties();
			FileReader fr = new FileReader(tslfile);
			props.load(fr);
			fr.close();
			String inputdirname = props.getProperty("TextInputDirectory");
			int fcount = 0;
			Vector<File> files = FUtils.readFilesFromDirectory(inputdirname);
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
				if (doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats)) {
					if (isDefinitions) {
						this.featureDefinitionVector.extractFeatureDefinitions(
								tsetname, doc);
					} else {
						FeaturePatientVector fpv = this.featurePatientVectorHash
								.get(doc.getPatientName());
						if (fpv == null) {
							fpv = new FeaturePatientVector(this,
									doc.getPatientName());
						} else {
							int x = 1;
						}
						fpv.extractFeaturesFromReport(tsetname, doc);
						this.moonstoneRuleInterface.releaseAnnotations();
					}
				}
			}
			if (!isDefinitions) {
				for (Enumeration<String> e = this.featurePatientVectorHash
						.keys(); e.hasMoreElements();) {
					String pname = e.nextElement();
					FeaturePatientVector fpv = this.featurePatientVectorHash
							.get(pname);
					fpv.writePatientVectorFile(isTraining);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector<String> getReportFiles() {
		String inputdirname = this.moonstoneRuleInterface
				.getStartupParameters().getPropertyValue("TextInputDirectory");
		return getReportFiles(inputdirname);
	}

	public Vector<String> getReportFiles(String fpath) {
		Vector<File> files = FUtils.readFilesFromDirectory(fpath);
		Vector<String> reportFiles = null;
		if (files != null) {
			for (File file : files) {
				String fname = file.getName().toLowerCase();
				if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
					continue;
				}
				if (!fname.endsWith(".txt") || fname.contains("xml")
						|| fname.contains("knowtator")) {
					continue;
				}
				reportFiles = VUtils.add(reportFiles, file.getAbsolutePath());
			}
		}
		return reportFiles;
	}

	public Vector<String> getRelevantConcepts(Annotation annotation) {
		Vector<String> allcontent = null;
		if (annotation.getNestedTargetAnnotations() != null) {
			for (Annotation target : annotation.getNestedTargetAnnotations()) {
				allcontent = VUtils.add(allcontent, target.getConcept()
						.toString());
			}
		}
		return allcontent;
	}

	// 8/17/2017
	public boolean featureRelevantToVariable(String vname, String cname) {
		return this.featureDefinitionVector.isFeatureRelevantToVariable(vname,
				cname);
	}

	public static Vector<String> getRelevantPropositionalContent(
			Annotation annotation) {
		Vector<String> allcontent = null;
		if (annotation.getNestedTargetAnnotations() != null) {
			for (Annotation target : annotation.getNestedTargetAnnotations()) {
				String content = Feature.generatePropositionalFeature(target);
				allcontent = VUtils.add(allcontent, content);
			}
		} else if (annotationIsRelevant(annotation)) {
			String content = Feature.generatePropositionalFeature(annotation);
			allcontent = VUtils.listify(content);
		} else if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				allcontent = VUtils.appendIfNot(allcontent,
						getRelevantPropositionalContent(child));
			}
		}
		return allcontent;
	}

	public static boolean annotationIsRelevant(Annotation annotation) {
		if (annotation.containsTargetConcept()) {
			return true;
		}
		if (annotation.hasRule() && annotation.getRule().isSpecialized()) {
			return true;
		}
		if (annotation.hasRule() && annotation.getRule().isComplexConcept()) {
			return true;
		}
		if (annotation.hasRule()
				&& annotation.getRule().getSourceFilePath() != null
				&& annotation.getRule().getSourceFilePath().toLowerCase()
						.contains("target")) {
			return true;
		}
		return false;
	}

	public FeatureDefinitionVector getFeatureDefinitionVector() {
		return featureDefinitionVector;
	}
	
	

}
