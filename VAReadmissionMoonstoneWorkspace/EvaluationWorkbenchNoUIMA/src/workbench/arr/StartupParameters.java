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
package workbench.arr;

import io.knowtator.KnowtatorIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import typesystem.TypeSystem;

public class StartupParameters {

	EvaluationWorkbench arrTool = null;
	// Properties properties = new Properties();
	String firstAnnotatorName = null;
	String secondAnnotatorName = null;
	Annotator firstAnnotator = null;
	Annotator secondAnnotator = null;
	String onyxKBDirectory = null;
	String GrAFTypeSystemFilename = "TypeSystemSpecs";
	String workbenchTypeSystemFile = null;
	String LispSystemFilename = "TypeSystemSpecsLisp";
	String inputTypeFirstAnnotator = null;
	String inputTypeSecondAnnotator = null;
	String rewriteDirectory = null;
	String NameOfCSVFile = null;
	boolean useTSL = false;
	String TSLRuleFile = null;
	boolean doTSLQueryDebug = false;
	String textInputDirectory = null;
	String workbenchDirectory = null;
	String annotationInputDirectoryFirstAnnotator = null;
	String annotationInputDirectorySecondAnnotator = null;
	String annotationOutputDirectory = null;
	String knowtatorSchemaFile = null;
	String knowtatorPinsFileFirstAnnotator = null;
	String knowtatorPinsFileSecondAnnotator = null;
	String clefAnnotationOutputDirectory = null;
	String startupPropertiesFilename = null;
	String defaultClassificationPropertyNames = null;
	boolean permitEquivalentClassificationNames = false;
	boolean storeGeneralStatisticsModels = true;
	Vector<String> annotatorNameValidators = null;
	String validationFile = null;
	boolean permitAllDefaultClassicationNames = false;
	public static String StartupParameterFileName = "startup.properties";
	public static Vector<String> AnnotatorTypes = VUtils
			.arrayToVector(new String[] { "GrAF", "Knowtator", "XMI", "CSV",
					"PIPELINE", "I2B2", "PyConText", "CLEF", "CLEF2" });
	public static String WorkbenchDirectoryName = "WorkbenchDirectory";
	public static String FirstAnnotatorName = "firstAnnotator";
	public static String SecondAnnotatorName = "secondAnnotator";
	public static String WorkbenchAnnotationFileTypeFirstAnnotator = "WorkbenchAnnotationFileTypeFirstAnnotator";
	public static String WorkbenchAnnotationFileTypeSecondAnnotator = "WorkbenchAnnotationFileTypeSecondAnnotator";
	public static String TextInputDirectory = "TextInputDirectory";
	public static String TextInputDirectoryFirstAnnotator = "TextInputDirectoryFirstAnnotator";
	public static String AnnotationInputDirectoryFirstAnnotator = "AnnotationInputDirectoryFirstAnnotator";
	public static String KnowtatorPinsFileFirstAnnotator = "KnowtatorPinsFileFirstAnnotator";
	public static String AnnotationInputDirectorySecondAnnotator = "AnnotationInputDirectorySecondAnnotator";
	public static String KnowtatorPinsFileSecondAnnotator = "KnowtatorPinsFileSecondAnnotator";
	public static String CLEFAnnotationOutputDirectory = "CLEFAnnotationOutputDirectory";
	public static String WorkbenchTypeSystemFile = "WorkbenchTypeSystemFile";
	public static String TypeSystemSynonymFile = "TypeSystemSynonymFile";
	public static String DefaultClassificationProperties = "DefaultClassificationProperties";
	public static String DefaultAttributes = "DefaultAttributes";
	public static String HiddenTypes = "HiddenTypes";
	public static String WorkbenchKnowtatorSchemaFile = "WorkbenchKnowtatorSchemaFile";
	public static String CSVFileName = "CSVFileName";
	public static String RewriteDirectory = "RewriteDirectory";
	public static String PermitEquivalentClassifications = "PermitEquivalentClassifications";
	public static String StateAttributes = "StateAttributes";
	public static String StoreGeneralStatisticsModels = "StoreGeneralStatisticsModels";
	public static String AnnotatorNameValidators = "AnnotatorNameValidators";
	public static String UseTSL = "useTSL";
	public static String TSLRules = "TSLRules";
	public static String DoTSLQueryDebug = "DoTSLQueryDebug";
	public static String ValidationFile = "ValidationFile";
	public static String PermitAllDefaultClassificationNames = "PermitAllDefaultClassificationNames";

	public StartupParameters(EvaluationWorkbench arrTool, boolean doread)
			throws Exception {
		this.arrTool = arrTool;
		arrTool.startupParameters = this;
		if (doread) {
			readParameters();
		}
	}

	boolean validateParameters() {
		String estr = getErrors();
		return estr == null;
	}

	public String getErrors() {
		File file = null;
		if (this.firstAnnotatorName == null || this.secondAnnotatorName == null) {
			return "firstAnnotator or secondAnnotator parameters not defined.\n";
		}
		if (!StrUtils.containsIgnoreCase(AnnotatorTypes,
				this.inputTypeFirstAnnotator)) {
			return "WorkbenchAnnotationFileTypeFirstAnnotator must be defined, and one of: "
					+ AnnotatorTypes + ".\n";
		}
		if (!StrUtils.containsIgnoreCase(AnnotatorTypes,
				this.inputTypeSecondAnnotator)) {
			return "WorkbenchAnnotationFileTypeSecondAnnotator must be defined, and one of: "
					+ AnnotatorTypes + ".\n";
		}
		if (this.textInputDirectory != null) {
			file = new File(this.textInputDirectory);
		}
		if (file == null || !file.exists() || !file.isDirectory()) {
			if (!canIgnoreParameter(this.inputTypeFirstAnnotator,
					"TextInputDirectory")) {
				return "TextInputDirectory must be defined, and must contain the annotated clinical reports\n";
			}
		}
		file = null;
		if (this.isInputTypeFirstAnnotatorKnowtator()) {
			if (this.knowtatorPinsFileFirstAnnotator != null) {
				file = new File(this.knowtatorPinsFileFirstAnnotator);
				if (!file.exists()) {
					return "KnowtatorPinsFileFirstAnnotator file: "
							+ this.knowtatorPinsFileFirstAnnotator
							+ " does not exist\n";
				}
			}
		}
		if (this.isInputTypeSecondAnnotatorKnowtator()) {
			if (this.knowtatorPinsFileSecondAnnotator != null) {
				file = new File(this.knowtatorPinsFileSecondAnnotator);
				if (!file.exists()) {
					return "KnowtatorPinsFileSecondAnnotator file: "
							+ this.knowtatorPinsFileSecondAnnotator
							+ " does not exist\n";
				}
			}
		}
		if ((this.isInputTypeFirstAnnotatorKnowtator() || this
				.isInputTypeSecondAnnotatorKnowtator())
				&& this.knowtatorSchemaFile == null) {
			return "WorkbenchKnowtatorSchemaFile parameter must point to valid Knowtator schema file";
		}
		if (this.annotationInputDirectoryFirstAnnotator == null
				|| this.annotationInputDirectorySecondAnnotator == null) {
			return "Must specify directories containing annotations for first and second annotator";
		}
		return null;
	}

	// boolean loadPropertiesFromStartupParameterFile() throws Exception {
	// if (this.startupPropertiesFilename == null) {
	// this.startupPropertiesFilename = StartupParameterFileName;
	// }
	// InputStream is = getClass().getResourceAsStream(
	// "/" + this.startupPropertiesFilename);
	// if (is == null) {
	// is = getClass().getResourceAsStream(StartupParameterFileName);
	// }
	// if (is == null) {
	// is = new FileInputStream(StartupParameterFileName);
	// }
	// this.properties.load(is);
	// if (this.properties == null || this.properties.isEmpty()) {
	// System.out.println("Unable to read "
	// + this.startupPropertiesFilename);
	// return false;
	// }
	// return true;
	// }

	String readParameters() throws Exception {
		String str = null;
		tsl.startup.StartupParameters sp = KnowledgeEngine
				.getCurrentKnowledgeEngine().getStartupParameters();
		AnnotationAnalysis analysis = arrTool.getAnalysis();
		this.workbenchDirectory = sp.getRootDirectory();

		if (this.firstAnnotatorName == null) {
			this.firstAnnotatorName = sp.getPropertyValue(FirstAnnotatorName);
		}
		if (this.secondAnnotatorName == null) {
			this.secondAnnotatorName = sp.getPropertyValue(SecondAnnotatorName);
		}
		if (this.firstAnnotatorName == null || this.secondAnnotatorName == null) {
			return this.returnWithError("Missing first/second annotator");
		}
		this.firstAnnotator = new Annotator(analysis, this.firstAnnotatorName);
		this.secondAnnotator = new Annotator(analysis, this.secondAnnotatorName);
		this.inputTypeFirstAnnotator = sp
				.getPropertyValue(WorkbenchAnnotationFileTypeFirstAnnotator);
		this.inputTypeSecondAnnotator = sp
				.getPropertyValue(WorkbenchAnnotationFileTypeSecondAnnotator);
		if (!StrUtils.containsIgnoreCase(AnnotatorTypes,
				inputTypeFirstAnnotator)
				|| !StrUtils.containsIgnoreCase(AnnotatorTypes,
						inputTypeSecondAnnotator)) {
			return this.returnWithError("Missing first/second annotator");
		}
		boolean usesTextDirectory = !(this.isInputTypeFirstAnnotatorCSV() || this
				.isInputTypeFirstAnnotatorPyConText());
		str = sp.getPropertyValue(TextInputDirectory);
		if (str != null) {
			str = FUtils.getAbsolutePathname(str);
			File file = new File(str);
			if (file.exists() && file.isDirectory()) {
				this.textInputDirectory = file.getAbsolutePath();
			}
		} else {
			if (usesTextDirectory) {
				return this.returnWithError("Missing corpus directory");
			}
		}
		str = sp.getPropertyValue(AnnotationInputDirectoryFirstAnnotator);
		if (str != null) {
			str = FUtils.getAbsolutePathname(str);
			File file = new File(str);
			if (file.exists() && file.isDirectory()) {
				this.annotationInputDirectoryFirstAnnotator = file
						.getAbsolutePath();
				if (this.textInputDirectory == null) {
					this.textInputDirectory = this.annotationInputDirectoryFirstAnnotator;
				}
			}
		}
		str = sp.getPropertyValue(KnowtatorPinsFileFirstAnnotator);
		if (str != null) {
			str = FUtils.getAbsolutePathname(str);
			File file = new File(str);
			if (file.exists()) {
				this.knowtatorPinsFileFirstAnnotator = file.getAbsolutePath();
			}
		}

		str = sp.getPropertyValue(AnnotationInputDirectorySecondAnnotator);
		if (str != null) {
			str = FUtils.getAbsolutePathname(str);
			File file = new File(str);
			if (file.exists() && file.isDirectory()) {
				this.annotationInputDirectorySecondAnnotator = file
						.getAbsolutePath();
				if (this.textInputDirectory == null) {
					this.textInputDirectory = this.annotationInputDirectorySecondAnnotator;
				}
			}
		}

		str = sp.getPropertyValue(KnowtatorPinsFileSecondAnnotator);
		if (str != null) {
			str = FUtils.getAbsolutePathname(str);
			File file = new File(str);
			if (file.exists()) {
				this.knowtatorPinsFileSecondAnnotator = file.getAbsolutePath();
			}
		}

		if (this.isInputTypeFirstAnnotatorKnowtator()) {
			if (this.knowtatorPinsFileFirstAnnotator == null
					&& this.annotationInputDirectoryFirstAnnotator == null) {
				return this.returnWithError("Missing input directory");
			}
		}

		if (this.isInputTypeSecondAnnotatorKnowtator()) {
			if (this.knowtatorPinsFileSecondAnnotator == null
					&& this.annotationInputDirectorySecondAnnotator == null) {
				return this.returnWithError("Missing input directory");
			}
		}

		str = sp.getPropertyValue(CLEFAnnotationOutputDirectory);
		if (str != null) {
			this.clefAnnotationOutputDirectory = str;
		}

		str = sp.getPropertyValue(WorkbenchTypeSystemFile);
		this.workbenchTypeSystemFile = str = FUtils.getAbsolutePathname(str);
		arrTool.typeSystem = arrTool.getTypeSystem(str);
		if (arrTool.typeSystem.getAllAnnotationTypes() == null) {
			arrTool.typeSystem.setUseOnlyTypeModel(false);
			arrTool.typeSystem.addDefaultRoot();
		}
		str = sp.getPropertyValue(TypeSystemSynonymFile);
		str = FUtils.getAbsolutePathname(str);
		if (str != null) {
			arrTool.typeSystem.readSynonymsFromFile(str);
		}
		str = sp.getPropertyValue(DefaultClassificationProperties);
		if (str != null) {
			this.defaultClassificationPropertyNames = str;
			Vector v = StrUtils.stringList(str, ',');
			arrTool.typeSystem.setDefaultClassificationProperties(v);
		}

		str = sp.getPropertyValue(DefaultAttributes);
		if (str != null) {
			Vector v = StrUtils.stringList(str, ',');
			arrTool.typeSystem.setDefaultAttributes(v);
		}

		str = sp.getPropertyValue(HiddenTypes);
		if (str != null) {
			Vector v = StrUtils.stringList(str, ',');
			arrTool.typeSystem.setHiddenTypes(v);
		}
		if (this.isInputTypeFirstAnnotatorKnowtator()
				|| this.isInputTypeSecondAnnotatorKnowtator()) {
			String knowtatorSchemaFile = sp
					.getPropertyValue(WorkbenchKnowtatorSchemaFile);
			if (knowtatorSchemaFile != null) {
				File file = new File(knowtatorSchemaFile);
				if (file.exists()) {
					this.knowtatorSchemaFile = file.getAbsolutePath();
				}
			}
			if (this.knowtatorSchemaFile == null) {
				return this.returnWithError("Missing knowtator schema file");
			}
		}
		str = sp.getPropertyValue(CSVFileName);
		this.NameOfCSVFile = FUtils.getAbsolutePathname(str);
		str = sp.getPropertyValue(RewriteDirectory);
		this.rewriteDirectory = FUtils.getAbsolutePathname(str);
		str = sp.getPropertyValue(PermitEquivalentClassifications);
		if (str != null) {
			this.permitEquivalentClassificationNames = Boolean
					.parseBoolean(str);
		}
		str = sp.getPropertyValue(StateAttributes);
		if (str != null) {
			arrTool.stateAttributeNames = VUtils.vectorToStringArray(StrUtils
					.stringList(str, ','));
		}
		// str = sp.getPropertyValue("WorkbenchUseOnyx");
		// if (str != null && Boolean.parseBoolean(str)) {
		// this.getOnyx();
		// }
		str = sp.getPropertyValue(StoreGeneralStatisticsModels);
		if (str != null) {
			this.storeGeneralStatisticsModels = Boolean.parseBoolean(str);
		}

		str = sp.getPropertyValue(AnnotatorNameValidators);
		if (str != null) {
			this.annotatorNameValidators = StrUtils.stringList(str, ',');
		}

		str = sp.getPropertyValue(UseTSL);
		if (str != null) {
			this.useTSL = Boolean.parseBoolean(str);
			if (this.useTSL) {
				this.TSLRuleFile = sp.getPropertyValue(TSLRules);
			}
			str = sp.getPropertyValue(DoTSLQueryDebug);
			if (str != null) {
				this.doTSLQueryDebug = Boolean.parseBoolean(str);
			}
		}
		str = sp.getPropertyValue(ValidationFile);
		if (str != null) {
			this.validationFile = str;
		}
		str = sp.getPropertyValue(PermitAllDefaultClassificationNames);
		if (str != null) {
			this.permitAllDefaultClassicationNames = Boolean.valueOf(str);
		}
		return null;
	}

	// Before 4/19/2015
	// void readParameters() throws Exception {
	// String str = null;
	// AnnotationAnalysis analysis = arrTool.getAnalysis();
	// if (!loadPropertiesFromStartupParameterFile()) {
	// return;
	// }
	// str = sp.getPropertyValue(WorkbenchDirectoryName);
	// this.workbenchDirectory = FUtils.fileStringIfExists(str);
	//
	// if (this.firstAnnotatorName == null) {
	// this.firstAnnotatorName = properties
	// .getProperty(FirstAnnotatorName);
	// }
	// if (this.secondAnnotatorName == null) {
	// this.secondAnnotatorName = properties
	// .getProperty(SecondAnnotatorName);
	// }
	//
	// String current = new java.io.File(".").getCanonicalPath();
	// System.out.println("Current dir:" + current);
	// String currentDir = System.getProperty("user.dir");
	// System.out.println("Current dir using System:" + currentDir);
	//
	// if (this.firstAnnotatorName == null || this.secondAnnotatorName == null)
	// {
	// return;
	// }
	// this.firstAnnotator = new Annotator(analysis, this.firstAnnotatorName);
	// this.secondAnnotator = new Annotator(analysis, this.secondAnnotatorName);
	// this.inputTypeFirstAnnotator = properties
	// .getProperty(WorkbenchAnnotationFileTypeFirstAnnotator);
	// this.inputTypeSecondAnnotator = properties
	// .getProperty(WorkbenchAnnotationFileTypeSecondAnnotator);
	// if (!StrUtils.containsIgnoreCase(AnnotatorTypes,
	// inputTypeFirstAnnotator)
	// || !StrUtils.containsIgnoreCase(AnnotatorTypes,
	// inputTypeSecondAnnotator)) {
	// return;
	// }
	// boolean usesTextDirectory = !(this.isInputTypeFirstAnnotatorCSV() || this
	// .isInputTypeFirstAnnotatorPyConText());
	// str = sp.getPropertyValue(TextInputDirectory);
	// if (str != null) {
	// str = FUtils.getAbsolutePathname(str);
	// File file = new File(str);
	// if (file.exists() && file.isDirectory()) {
	// this.textInputDirectory = file.getAbsolutePath();
	// }
	// } else {
	// if (usesTextDirectory) {
	// return;
	// }
	// }
	//
	// str = sp.getPropertyValue(AnnotationInputDirectoryFirstAnnotator);
	// if (str != null) {
	// str = FUtils.getAbsolutePathname(str);
	// File file = new File(str);
	// if (file.exists() && file.isDirectory()) {
	// this.annotationInputDirectoryFirstAnnotator = file
	// .getAbsolutePath();
	// if (this.textInputDirectory == null) {
	// this.textInputDirectory = this.annotationInputDirectoryFirstAnnotator;
	// }
	// }
	// }
	// str = sp.getPropertyValue(KnowtatorPinsFileFirstAnnotator);
	// if (str != null) {
	// str = FUtils.getAbsolutePathname(str);
	// File file = new File(str);
	// if (file.exists()
	// // && file.isFile()
	// ) {
	// this.knowtatorPinsFileFirstAnnotator = file.getAbsolutePath();
	// }
	// }
	//
	// str = sp.getPropertyValue(AnnotationInputDirectorySecondAnnotator);
	// if (str != null) {
	// str = FUtils.getAbsolutePathname(str);
	// File file = new File(str);
	// if (file.exists() && file.isDirectory()) {
	// this.annotationInputDirectorySecondAnnotator = file
	// .getAbsolutePath();
	// if (this.textInputDirectory == null) {
	// this.textInputDirectory = this.annotationInputDirectorySecondAnnotator;
	// }
	// }
	// }
	//
	// str = sp.getPropertyValue(KnowtatorPinsFileSecondAnnotator);
	// if (str != null) {
	// str = FUtils.getAbsolutePathname(str);
	// File file = new File(str);
	// if (file.exists()
	// // && file.isFile()
	// ) {
	// this.knowtatorPinsFileSecondAnnotator = file.getAbsolutePath();
	// }
	// }
	//
	// if (this.isInputTypeFirstAnnotatorKnowtator()) {
	// if (this.knowtatorPinsFileFirstAnnotator == null
	// && this.annotationInputDirectoryFirstAnnotator == null) {
	// return;
	// }
	// }
	//
	// if (this.isInputTypeSecondAnnotatorKnowtator()) {
	// if (this.knowtatorPinsFileSecondAnnotator == null
	// && this.annotationInputDirectorySecondAnnotator == null) {
	// return;
	// }
	// }
	//
	// str = sp.getPropertyValue(CLEFAnnotationOutputDirectory);
	// if (str != null) {
	// this.clefAnnotationOutputDirectory = str;
	// }
	//
	// str = sp.getPropertyValue(WorkbenchTypeSystemFile);
	// this.workbenchTypeSystemFile = str = FUtils.getAbsolutePathname(str);
	// arrTool.typeSystem = arrTool.getTypeSystem(str);
	// if (arrTool.typeSystem.getAllAnnotationTypes() == null) {
	// arrTool.typeSystem.setUseOnlyTypeModel(false);
	// arrTool.typeSystem.addDefaultRoot();
	// }
	// str = sp.getPropertyValue(TypeSystemSynonymFile);
	// str = FUtils.getAbsolutePathname(str);
	// if (str != null) {
	// arrTool.typeSystem.readSynonymsFromFile(str);
	// }
	// str = sp.getPropertyValue(DefaultClassificationProperties);
	// if (str != null) {
	// this.defaultClassificationPropertyNames = str;
	// Vector v = StrUtils.stringList(str, ',');
	// arrTool.typeSystem.setDefaultClassificationProperties(v);
	// }
	//
	// str = sp.getPropertyValue(DefaultAttributes);
	// if (str != null) {
	// Vector v = StrUtils.stringList(str, ',');
	// arrTool.typeSystem.setDefaultAttributes(v);
	// }
	//
	// str = sp.getPropertyValue(HiddenTypes);
	// if (str != null) {
	// Vector v = StrUtils.stringList(str, ',');
	// arrTool.typeSystem.setHiddenTypes(v);
	// }
	// if (this.isInputTypeFirstAnnotatorKnowtator()
	// || this.isInputTypeSecondAnnotatorKnowtator()) {
	// String knowtatorSchemaFile = properties
	// .getProperty(WorkbenchKnowtatorSchemaFile);
	// if (knowtatorSchemaFile != null) {
	// File file = new File(knowtatorSchemaFile);
	// if (file.exists()) {
	// this.knowtatorSchemaFile = file.getAbsolutePath();
	// }
	// }
	// if (this.knowtatorSchemaFile == null) {
	// return;
	// }
	// }
	// str = sp.getPropertyValue(CSVFileName);
	// this.NameOfCSVFile = FUtils.getAbsolutePathname(str);
	// str = sp.getPropertyValue(RewriteDirectory);
	// this.rewriteDirectory = FUtils.getAbsolutePathname(str);
	// str = sp.getPropertyValue(PermitEquivalentClassifications);
	// if (str != null) {
	// this.permitEquivalentClassificationNames = Boolean
	// .parseBoolean(str);
	// }
	// str = sp.getPropertyValue(StateAttributes);
	// if (str != null) {
	// arrTool.stateAttributeNames = VUtils.vectorToStringArray(StrUtils
	// .stringList(str, ','));
	// }
	// // str = sp.getPropertyValue("WorkbenchUseOnyx");
	// // if (str != null && Boolean.parseBoolean(str)) {
	// // this.getOnyx();
	// // }
	// str = sp.getPropertyValue(StoreGeneralStatisticsModels);
	// if (str != null) {
	// this.storeGeneralStatisticsModels = Boolean.parseBoolean(str);
	// }
	//
	// str = sp.getPropertyValue(AnnotatorNameValidators);
	// if (str != null) {
	// this.annotatorNameValidators = StrUtils.stringList(str, ',');
	// }
	//
	// str = sp.getPropertyValue(UseTSL);
	// if (str != null) {
	// this.useTSL = Boolean.parseBoolean(str);
	// if (this.useTSL) {
	// this.TSLRuleFile = sp.getPropertyValue(TSLRules);
	// }
	// str = sp.getPropertyValue(DoTSLQueryDebug);
	// if (str != null) {
	// this.doTSLQueryDebug = Boolean.parseBoolean(str);
	// }
	// }
	// str = sp.getPropertyValue(ValidationFile);
	// if (str != null) {
	// this.validationFile = str;
	// }
	// str = sp.getPropertyValue(PermitAllDefaultClassificationNames);
	// if (str != null) {
	// this.permitAllDefaultClassicationNames = Boolean.valueOf(str);
	// }
	// }

	public void storeParameterProperties() throws Exception {
		Properties p = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters().getProperties();
		if (this.firstAnnotatorName != null) {
			p.setProperty(FirstAnnotatorName, this.firstAnnotatorName);
		}
		if (this.secondAnnotatorName != null) {
			p.setProperty(SecondAnnotatorName, this.secondAnnotatorName);
		}
		if (this.inputTypeFirstAnnotator != null) {
			p.setProperty(WorkbenchAnnotationFileTypeFirstAnnotator,
					this.inputTypeFirstAnnotator);
		}
		if (this.inputTypeSecondAnnotator != null) {
			p.setProperty(WorkbenchAnnotationFileTypeSecondAnnotator,
					this.inputTypeSecondAnnotator);
		}
		if (this.textInputDirectory != null) {
			p.setProperty(TextInputDirectory, this.textInputDirectory);
		}
		if (this.annotationInputDirectoryFirstAnnotator != null) {
			p.setProperty(AnnotationInputDirectoryFirstAnnotator,
					this.annotationInputDirectoryFirstAnnotator);
		}
		if (this.annotationInputDirectorySecondAnnotator != null) {
			p.setProperty(AnnotationInputDirectorySecondAnnotator,
					this.annotationInputDirectorySecondAnnotator);
		}
		if (this.knowtatorPinsFileFirstAnnotator != null) {
			p.setProperty(KnowtatorPinsFileFirstAnnotator,
					this.knowtatorPinsFileFirstAnnotator);
		}
		if (this.knowtatorPinsFileSecondAnnotator != null) {
			p.setProperty(KnowtatorPinsFileSecondAnnotator,
					this.knowtatorPinsFileSecondAnnotator);
		}
		if (this.clefAnnotationOutputDirectory != null) {
			p.setProperty(CLEFAnnotationOutputDirectory,
					this.clefAnnotationOutputDirectory);
		}
		if (this.arrTool.typeSystem != null) {
			TypeSystem ts = this.arrTool.typeSystem;
			if (ts.getDefaultClassificationProperties() != null) {
				String props = StrUtils.stringListConcat(
						ts.getDefaultClassificationProperties(), ",");
				p.setProperty(DefaultClassificationProperties, props);
			}
		}
		if (this.knowtatorSchemaFile != null) {
			p.setProperty(WorkbenchKnowtatorSchemaFile,
					this.knowtatorSchemaFile);
		}
		if (this.arrTool.annotatorNameValidators != null) {
			String str = StrUtils.stringListConcat(
					this.arrTool.annotatorNameValidators, ",");
			p.setProperty(AnnotatorNameValidators, str);
		}
		if (this.arrTool.validationFile != null) {
			p.setProperty(ValidationFile, this.arrTool.validationFile);
		}
		p.setProperty(StoreGeneralStatisticsModels,
				Boolean.toString(this.isStoreGeneralStatisticsModels()));
	}

	public void writeParametersToStartupFile() throws Exception {
		StringBuffer sb = new StringBuffer();
		for (Enumeration e = this.getProperties().keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = (String) this.getProperties().getProperty(key);
			sb.append(key + " = " + value + "\n");
		}
		String fname = this.workbenchDirectory + File.separatorChar
				+ StartupParameterFileName;
		String bfname = fname + ".BAK";
		FUtils.copyFile(fname, bfname);
		FUtils.writeFile(fname, sb.toString());
	}

	public boolean canIgnoreParameter(String atype, String ptype) {
		if ("pycontext".equals(atype.toLowerCase())) {
			if ("TextInputDirectory".equals(ptype)) {
				return true;
			}
		}
		return false;
	}

	public Annotator getFirstAnnotator() {
		if (this.firstAnnotator == null && this.firstAnnotatorName != null) {
			this.firstAnnotator = new Annotator(this.arrTool.analysis,
					this.firstAnnotatorName);
		}
		return firstAnnotator;
	}

	public void setFirstAnnotator(Annotator firstAnnotator) {
		this.firstAnnotator = firstAnnotator;
	}

	public Annotator getSecondAnnotator() {
		if (this.secondAnnotator == null && this.secondAnnotatorName != null) {
			this.secondAnnotator = new Annotator(this.arrTool.analysis,
					this.secondAnnotatorName);
		}
		return secondAnnotator;
	}

	public void setSecondAnnotator(Annotator secondAnnotator) {
		this.secondAnnotator = secondAnnotator;
	}

	public String getFirstAnnotatorName() {
		return this.firstAnnotatorName;
	}

	public String getSecondAnnotatorName() {
		return this.secondAnnotatorName;
	}

	public boolean isInputTypeFirstAnnotatorPyConText() {
		return "PyConText".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeFirstAnnotatorCLEF() {
		return "CLEF".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeFirstAnnotatorCLEF2() {
		return "CLEF2".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeFirstAnnotatorI2B2() {
		return "I2B2".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeFirstAnnotatorGrAF() {
		return "GrAF".equals(this.inputTypeFirstAnnotator);
	}

	public String getInputTypeFirstAnnotator() {
		return inputTypeFirstAnnotator;
	}

	public void setInputTypeFirstAnnotator(String inputTypeFirstAnnotator) {
		this.inputTypeFirstAnnotator = inputTypeFirstAnnotator;
	}

	public String getInputTypeSecondAnnotator() {
		return inputTypeSecondAnnotator;
	}

	public void setInputTypeSecondAnnotator(String inputTypeSecondAnnotator) {
		this.inputTypeSecondAnnotator = inputTypeSecondAnnotator;
	}

	public boolean isInputTypeFirstAnnotatorKnowtator() {
		return "knowtator".equals(this.inputTypeFirstAnnotator.toLowerCase());
	}

	public boolean isInputTypeFirstAnnotatorXMI() {
		return "XMI".equals(this.inputTypeFirstAnnotator.toLowerCase());
	}

	public boolean isInputTypeFirstAnnotatorCSV() {
		return "CSV".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeFirstAnnotatorPIPELINE() {
		return "PIPELINE".equals(this.inputTypeFirstAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorPyConText() {
		return "PyConText".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorCLEF() {
		return "CLEF".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorCLEF2() {
		return "CLEF2".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorI2B2() {
		return "I2B2".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorGrAF() {
		return "GrAF".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorKnowtator() {
		return "Knowtator".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorXMI() {
		return "XMI".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorCSV() {
		return "CSV".equals(this.inputTypeSecondAnnotator);
	}

	public boolean isInputTypeSecondAnnotatorPIPELINE() {
		return "PIPELINE".equals(this.inputTypeSecondAnnotator);
	}

	public String getRewriteDirectory() {
		return rewriteDirectory;
	}

	public boolean permitEquivalentClassificationNames() {
		return this.permitEquivalentClassificationNames;
	}

	public boolean isStoreGeneralStatisticsModels() {
		return storeGeneralStatisticsModels;
	}

	public boolean isUseTSL() {
		return useTSL;
	}

	public boolean isDoTSLQueryDebug() {
		return doTSLQueryDebug;
	}

	public String getKnowtatorFormatFirstAnnotator() {
		if (this.knowtatorPinsFileFirstAnnotator != null) {
			return KnowtatorIO.LispFormat;
		} else if (this.annotationInputDirectoryFirstAnnotator != null) {
			return KnowtatorIO.SHARPXMLFormat;
		}
		return null;
	}

	public String getKnowtatorFormatSecondAnnotator() {
		if (this.knowtatorPinsFileFirstAnnotator != null) {
			return KnowtatorIO.LispFormat;
		} else if (this.annotationInputDirectoryFirstAnnotator != null) {
			return KnowtatorIO.SHARPXMLFormat;
		}
		return null;
	}

	public String getAnnotationInputDirectoryFirstAnnotator() {
		return this.annotationInputDirectoryFirstAnnotator;
	}

	public String getAnnotationInputDirectorySecondAnnotator() {
		return this.annotationInputDirectorySecondAnnotator;
	}

	public String getTextInputDirectory() {
		return this.textInputDirectory;
	}

	public String getKnowtatorPinsFileFirstAnnotator() {
		return knowtatorPinsFileFirstAnnotator;
	}

	public String getKnowtatorPinsFileSecondAnnotator() {
		return knowtatorPinsFileSecondAnnotator;
	}

	public String getKnowtatorSchemaFile() {
		return knowtatorSchemaFile;
	}

	public String getCLEFAnnotationOutputDirectory() {
		return clefAnnotationOutputDirectory;
	}

	public String getValidationFile() {
		return validationFile;
	}

	public String getWorkbenchDirectory() {
		return workbenchDirectory;
	}

	// public Properties getProperties() {
	// return properties;
	// }

	public boolean isPermitAllDefaultClassicationNames() {
		return permitAllDefaultClassicationNames;
	}

	public void setPermitAllDefaultClassicationNames(
			boolean permitAllDefaultClassicationNames) {
		this.permitAllDefaultClassicationNames = permitAllDefaultClassicationNames;
	}

	public void setTextInputDirectory(String textInputDirectory) {
		this.textInputDirectory = textInputDirectory;
	}

	public void setAnnotationInputDirectoryFirstAnnotator(
			String annotationInputDirectoryFirstAnnotator) {
		this.annotationInputDirectoryFirstAnnotator = annotationInputDirectoryFirstAnnotator;
	}

	public void setAnnotationInputDirectorySecondAnnotator(
			String annotationInputDirectorySecondAnnotator) {
		this.annotationInputDirectorySecondAnnotator = annotationInputDirectorySecondAnnotator;
	}

	public Properties getProperties() {
		return KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters().getProperties();
	}
	
	public String returnWithError(String msg) {
		JOptionPane.showMessageDialog(new JFrame(), msg);
		return null;
	}

}
