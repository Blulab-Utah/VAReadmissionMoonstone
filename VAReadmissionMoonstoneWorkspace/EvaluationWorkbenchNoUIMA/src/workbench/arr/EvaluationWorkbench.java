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

import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;

import annotation.DocumentAnnotation;
import annotation.SnippetAnnotation;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.StrUtils;
import typesystem.TypeSystem;

public class EvaluationWorkbench {

	// Onyx onyx = null;
	// public StartupParameters startupParameters = null;
	// public StartupParametersGUI startupParametersGUI = null;
	public StartupParameters startupParameters = null;
	public TypeSystem typeSystem = null;
	public AnnotationAnalysis analysis = null;
	public JFrame frame = null;

	public GeneralStatistics statistics = null;
	public GeneralStatistics lastStatistics = null;
	public Object lastGeneralStatisticsKey = null;
	public String validationFile = null;

	static EvaluationWorkbench evaluationWorkbench = null;
	public Hashtable<Object, GeneralStatistics> statisticsHash = new Hashtable();

	Annotator firstAnnotator = null;
	Annotator secondAnnotator = null;

	// File sourceDirectory = null;
	// String rootDirectoryFilename = null;

	// public File rootDirectory = null;

	String inputTypeFirstAnnotator = null;
	String inputTypeSecondAnnotator = null;
	String rewriteDirectory = null;
	String CSVFileName = null;
	boolean isStrictMatchCriterion = false;
	boolean displayBothAnnotationSetsSimultaneously = false;
	Properties properties = new Properties();

	// JFrame typeGraphScrollWindow = null;
	// JFrame detailScrollWindow = null;
	// ClassificationLevelPane classLevelPane = null;
	boolean gettingGeneralStatics = false;
	boolean permitEquivalentClassificationNames = false;
	boolean storeGeneralStatisticsModels = true;
	String[] stateAttributeNames = null;
	Vector<String> annotatorNameValidators = null;
	boolean useTSL = false;
	boolean doTSLQueryDebug = false;
	String annotationInputDirectoryFirstAnnotator = null;
	String annotationInputDirectorySecondAnnotator = null;
	String textInputDirectory = null;
	String knowtatorSchemaFile = null;
	String knowtatorPinsFileFirstAnnotator = null;
	String knowtatorPinsFileSecondAnnotator = null;
	String annotationOutputDirectory = null;

	String GrAFTypeSystemFilename = "TypeSystemSpecs";
	String LispSystemFilename = "TypeSystemSpecsLisp";

	KnowledgeEngine knowledgeEngine = null;

	private static boolean mouseControlKeyInteraction = false;
	private static boolean firingAllDataChanged = false;

	private static String[][] menuInfo = { { null, "file", "File" },
			{ null, "annotate", "Annotate" }, { null, "match", "Match" },
			{ null, "details", "Details" }, { null, "utilities", "Utilities" },
			{ null, "tsl", "TSL" } };

	private static Object[][] menuItemInfo = {
			{ "file", "storeVerifications", "Store Verifications" },
			{ "file", "storeEditedAnnotations", "Store Edited Annotations" },
			{ "file", "editStartupParameters", "Edit startup parameters" },
			{ "file", "quit", "Quit" },
			{ "annotate", "verifyAnnotation", "Verify Annotation" },
			{ "annotate", "unverifyAnnotation", "Unverify Annotation" },
			{ "annotate", "falsifyAnnotation", "falsify Annotation" },
			{ "annotate", "verifyAllAnnotations", "Verify All Annotations" },
			{ "annotate", "unverifyAllAnnotations", "Unverify All Annotations" },
			{ "annotate", "toggleDisplaySimultaneously",
					"Toggle Display Both Annotations Simultaneously" },
			{ "annotate", "deleteAnnotation", "Delete Selected Annotation" },
			{ "annotate", "deleteAttribute", "Delete Selected Attribute" },
			{ "match", "makeClassificationsEquivalent",
					"Make Classifications Equivalent" },
			{ "details", "displayDetailWindow", "Display Detail Window" },
			{ "details", "displayTypeGraphWindow",
					"Display Type / Schema Window" },
			{ "utilities", "convertToPipeDelimitedCLEF",
					"Convert annotations to pipe-delimited format (CLEF)" },
			{ "utilities", "convertToPipeDelimitedCLEF2",
					"Convert annotations to pipe-delimited format (CLEF2)" },
			{ "utilities", "removeCLEF2011InvalidatedCUIs",
					"Remove Invalidated 2012 CUIs (CLEF)" },
			{ "utilities", "displayCUICounts", "Display CUI counts" },
			{ "utilities", "displayOM", "Display Outcome Measures" },
			{ "utilities", "displayErrors", "Display Errors" },
			{ "utilities", "displayDuplicates", "Display Duplicate Annotations" },
			{ "utilities", "indexedDirectoryDocuments",
					"MySQL Index Directory Documents" },
			{ "utilities", "mysqlIndexDocuments",
					"Index Documents for MySQL Storage" },
			{ "utilities", "getMySQLSentencesMatchingQuery",
					"Get MySQL Sentences Matching Query" },
			{ "tsl", "TSLKBInterface", "TSL KB Interface" },
			{ "tsl", "TSLdoQuery", "Do Query" },

	};

	public EvaluationWorkbench() throws Exception {
		evaluationWorkbench = this;
		 this.frame = new JFrame();
		 this.analysis = new AnnotationAnalysis(this);
		 initialize();
	}

	public void initializeFromStartupParameterGUI() throws Exception {
		reinitializeTypeSystem();
		AnnotationAnalysis analysis = new AnnotationAnalysis(this);
		analysis.initializeAnnotators();
		analysis.readAnnotationCollections();
		analysis.postProcessAnnotationCollections();
		if (!this.hasBeenConstructed()) {
			if (this.startupParameters.isUseTSL()) {
				KnowledgeEngine ke = KnowledgeEngine
						.getCurrentKnowledgeEngine();
				ke.getKnowledgeBase().readRuleFile(
						this.startupParameters.TSLRuleFile);
			}
			this.initialize();
//			
//			this.frame.setContentPane(this);
//			this.frame.pack();
//			this.frame.setVisible(true);
		}
		GeneralStatistics.create(this, null);
	}

	public void reinitializeTypeSystem() throws Exception {
		TypeSystem.currentTypeSystem = null;
		this.typeSystem = null;
		String fstr = (this.startupParameters.workbenchTypeSystemFile != null ? this.startupParameters.workbenchTypeSystemFile
				: null);
		this.typeSystem = this.getTypeSystem(fstr);
		if (this.typeSystem.getAllAnnotationTypes() == null) {
			this.typeSystem.setUseOnlyTypeModel(false);
			this.typeSystem.addDefaultRoot();
		}
		if (this.startupParameters.defaultClassificationPropertyNames != null) {
			Vector<String> v = StrUtils.stringList(
					this.startupParameters.defaultClassificationPropertyNames,
					',');
			this.typeSystem.setDefaultClassificationProperties(v);
		}
	}

	public void initialize() throws Exception {
		GeneralStatistics.create(this, null);
	}

	public void fireAllTableDataChanged() throws Exception {
		fireAllTableDataChanged(false);
	}

	public void fireAllTableDataChanged(boolean redoSentences) throws Exception {
	}

	void doTSLQuery() {

	}

	// void selectFile() throws Exception {
	// Object[] names = VUtils.vectorToArray(ARRDocument.getDocumentNames());
	// String s = (String) JOptionPane.showInputDialog(frame,
	// "Annotation File", "Annotation File",
	// JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
	//
	// if (s != null) {
	// documentPane.setSelectedDocument(s);
	// setTitle();
	// }
	// }

	void setTitle() {
		String filename = "*";
		String annotator = "*";
		String level = "*";
		if (analysis.selectedAnnotationEvent != null
				&& analysis.getSelectedAnnotator() != null) {
			annotator = analysis.getSelectedAnnotator().getName();
		}
		if (analysis.selectedDocument != null) {
			filename = analysis.getSelectedDocument().getName();
		}
		if (this.getAnalysis().getSelectedLevel() != null) {
			if (DocumentAnnotation.class.equals(this.getAnalysis()
					.getSelectedLevel())) {
				level = "DOCUMENT";
			} else if (SnippetAnnotation.class.equals(this.getAnalysis()
					.getSelectedLevel())) {
				level = "SNIPPET";
			}
		}
		String title = "Level=" + level + " File=" + filename + " Annotator="
				+ annotator + " Match= "
				+ (this.isStrictMatchCriterion ? "Strict" : "Relaxed");
		// this.frame.setTitle(title);
	}

	// public static JMenuBar createMenuBar(String[][] menuinfo,
	// Object[][] menuiteminfo, ActionListener listener,
	// JComponent component) {
	// Hashtable menuhash = new Hashtable();
	// JMenuBar menubar = new JMenuBar();
	//
	// for (int i = 0; i < menuinfo.length; i++) {
	// String[] array = (String[]) menuinfo[i];
	// String parentname = array[0];
	// String menuname = array[1];
	// String displayname = array[2];
	// JMenu menu = new JMenu(displayname);
	// menuhash.put(menuname, menu);
	// if (parentname != null) {
	// JMenu parent = (JMenu) menuhash.get(parentname);
	// parent.add(menu);
	// } else {
	// menubar.add(menu);
	// }
	// }
	//
	// for (int i = 0; i < menuiteminfo.length; i++) {
	// Object[] array = (Object[]) menuiteminfo[i];
	// String menuname = (String) array[0];
	// String actionname = (String) array[1];
	// String displayname = (String) array[2];
	// int key = -1;
	// int modifier = -1;
	// if (array.length > 3) {
	// Integer k = (Integer) array[3];
	// key = k.intValue();
	// Integer m = (Integer) array[4];
	// modifier = m.intValue();
	// }
	// JMenu menu = (JMenu) menuhash.get(menuname);
	// JMenuItem menuitem = new JMenuItem(displayname);
	// menuitem.setActionCommand(actionname);
	// menuitem.addActionListener(listener);
	// if (modifier > 0) {
	// KeyStroke ks = KeyStroke.getKeyStroke(key, modifier);
	// menuitem.setAccelerator(ks);
	// }
	// menu.add(menuitem);
	// }
	// return menubar;
	// }

	public AnnotationAnalysis getAnalysis() {
		if (this.analysis == null) {
			this.analysis = new AnnotationAnalysis(this);
		}
		return analysis;
	}

	public void setAnalysis(AnnotationAnalysis analysis) {
		this.analysis = analysis;
	}

	public TypeSystem getTypeSystem(String typeSystemFileName) throws Exception {
		if (this.typeSystem == null) {
			this.typeSystem = TypeSystem.getTypeSystem(typeSystemFileName);
		}
		return typeSystem;
	}

	// public TypeSystem getTypeSystem(String typeSystemFileName) {
	// if (this.typeSystem == null) {
	// this.typeSystem = TypeSystem.getTypeSystem(typeSystemFileName);
	// }
	// return typeSystem;
	// }

	public TypeSystem getTypeSystem() throws Exception {
		return getTypeSystem(null);
	}

	public void setTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public boolean isStrictMatchCriterion() {
		return isStrictMatchCriterion;
	}

	public void setStrictMatchCriterion(boolean isStrictMatchCriterion) {
		this.isStrictMatchCriterion = isStrictMatchCriterion;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void toggleDisplayBothAnnotationSetsSimultaneously()
			throws Exception {
	}

	public boolean isDisplayBothAnnotationSetsSimultaneously() {
		return displayBothAnnotationSetsSimultaneously;
	}

	public GeneralStatistics getStatistics() {
		return this.statistics;
	}

	public GeneralStatistics getGeneralStatistics(Object selectedItem,
			boolean doForce) throws Exception {
		GeneralStatistics gs = null;
		Object key = selectedItem;
		if (key == null) {
			key = this.getAnalysis().getSelectedLevel();
		}
		if (key != null) {
			gs = statisticsHash.get(key);
		}
		if (doForce) {
			gs = null;
		}
		if (gs == null) {
			gs = GeneralStatistics.create(this, selectedItem, null);
			if (gs != null
					&& key != null
					&& this.getStartupParameters()
							.isStoreGeneralStatisticsModels()) {
				statisticsHash.put(key, gs);
			}
		}
		if (gs != null && gs != this.statistics) {
			this.statistics = gs;
			GeneralStatistics.setStatistics(gs);
			fireAllTableDataChanged();
		}
		return gs;
	}

	public void clearGeneralStatisticsTable() {
		statisticsHash = new Hashtable();
	}

	public static EvaluationWorkbench getEvaluationWorkbench() {
		return evaluationWorkbench;
	}

	public String[] getStateAttributeNames() {
		return stateAttributeNames;
	}

	public static void toggleIsMouseControlKeyInteraction() {
	}

	public static boolean isMouseControlKeyInteraction() {
		return mouseControlKeyInteraction;
	}

	public StartupParameters getStartupParameters() {
		return this.startupParameters;
	}

	public boolean hasBeenConstructed() {
		return true;
	}

	public KnowledgeEngine getKnowledgeEngine() {
		return knowledgeEngine;
	}

}
