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
package edu.utah.blulab.evaluationworkbenchmanager;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentImpl.DocumentImplSorter;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.WEWManager;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;
import workbench.api.Analysis;
import workbench.api.gui.WBGUI;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

public class EvaluationWorkbenchManager {

	public static void loadWBGUI(EvaluationWorkbench arrtool,
			final DocumentReference schema,
			final List<DocumentReference> documents,
			final List<AnnotatorReference> primaryAnnotators,
			final List<AnnotatorReference> secondaryAnnotators,
			final Params params) throws Exception {
		try {
			Analysis analysis = new Analysis();
			// Hashtable<String, Integer> annotationFileNameIndexHash = new
			// Hashtable();

			// 4/12/2017 TEST: Removed to try to improve loading speed
			// Hashtable<DocumentReference, String> documentTextHash = new
			// Hashtable();
			KnowtatorIO kio = null;
			String ftype = (String) params.getParams().get("format");
			if (ftype != null
					&& ftype.toLowerCase().equals(
							WEWManager.FormatTypeKnowtator.toLowerCase())) {
				kio = Knowtator.createKnowtatorIO(analysis);
				WEWManager.readKnowtatorSchemaFile(schema, kio);
			}
			int palen = primaryAnnotators.size();
			int salen = secondaryAnnotators.size();
			int dlen = documents.size();
			int primaryIndex = 0, secondaryIndex = 0;

			boolean readCorpusTexts = analysis.getKnownledgeEngine()
					.getStartupParameters().isPropertyTrue("ReadCorpusTexts");
			for (DocumentReference dr : documents) {
				int pi = getAnnotatorReferenceForDocumentReference(dr,
						primaryAnnotators, primaryIndex);
				int si = getAnnotatorReferenceForDocumentReference(dr,
						secondaryAnnotators, secondaryIndex);
				if (pi == -1 || si == -1) {
					continue;
				}
				primaryIndex = pi;
				secondaryIndex = si;
				AnnotatorReference primary = primaryAnnotators
						.get(primaryIndex);
				AnnotatorReference secondary = secondaryAnnotators
						.get(secondaryIndex);

				// annotationFileNameIndexHash.put(dr.getName(), new
				// Integer(i));
				// annotationFileNameIndexHash.put(primary.getName(), new
				// Integer(
				// i));
				// annotationFileNameIndexHash.put(secondary.getName(),
				// new Integer(i));

				String dtext = "";
				if (readCorpusTexts) {
					dtext = WEWManager.readDocumentReference(dr, false);
					dtext = UnixFormat.convertToUnixFormat(dtext);
					// 11/17/2017
					// documentTextHash.put(dr, dtext);
				}

				WEWManager.readAnnotationCollection(analysis, dr, primary,
						params, kio, dtext);
				WEWManager.readAnnotationCollection(analysis, dr, secondary,
						params, kio, dtext);
			}
			if (kio != null) {
				Knowtator.postProcess(kio, analysis);
			}
			analysis.postProcessAnnotationCollections();
			Constraint.initialize();

			// 4/24/2017: Added. Why is this at home but not on VINCI?
			WBGUI.WorkbenchGUI = new WBGUI(analysis, arrtool, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getAnnotatorReferenceForDocumentReference(
			DocumentReference dr, List<AnnotatorReference> arefs, int start) {
		if (start < 0) {
			start = 0;
		}
		if (dr != null && arefs != null) {
			for (int i = start; i < arefs.size(); i++) {
				AnnotatorReference ar = arefs.get(i);
				if (ar.getName().contains(dr.getName())) {
					return i;
				}
				if (ar.getName().compareTo(dr.getName()) > 0) {
					return -1;
				}
			}
		}
		return -1;
	}

	public static int getAnnotatorReferenceForDocumentReference_Before_4_25_2017(
			DocumentReference dr, List<AnnotatorReference> arefs, int start) {
		if (start < 0) {
			start = 0;
		}
		if (dr != null && arefs != null) {
			int dint = StrUtils.extractInteger(dr.getName());
			if (dint >= 0) {
				for (int i = start; i < arefs.size(); i++) {
					AnnotatorReference ar = arefs.get(i);
					int aint = StrUtils.extractInteger(ar.getName());
					if (aint == dint) {
						return i;
					}
					if (ar.getName().compareTo(dr.getName()) > 0) {
						return -1;
					}
				}
			}
		}
		return -1;
	}

	public static int getAnnotatorReferenceForDocumentReference_BEFORE_4_24_2017(
			DocumentReference dr, List<AnnotatorReference> arefs, int start) {
		if (start < 0) {
			start = 0;
		}
		if (dr != null && arefs != null) {
			String dname = dr.getName();
			int index = dname.indexOf('.');
			dname = dname.substring(0, index);
			for (int i = start; i < arefs.size(); i++) {
				AnnotatorReference ar = arefs.get(i);
				String aname = ar.getName();
				index = aname.indexOf('.');
				aname = aname.substring(0, index);
				if (dname.equals(aname)) {
					return i;
				}
				if (aname.compareTo(dname) > 0) {
					return -1;
				}
			}
		}
		return -1;
	}

	public static EvaluationWorkbench initializeOldWorkbench() throws Exception {
		EvaluationWorkbench wb = new EvaluationWorkbench();
		new StartupParameters(wb, true);
		wb.reinitializeTypeSystem();
		AnnotationAnalysis aa = new AnnotationAnalysis(wb);
		aa.initializeAnnotators();
		return wb;
	}

	public static void doTest() throws Exception {
		EvaluationWorkbench wb = EvaluationWorkbenchManager
				.initializeOldWorkbench();

		Params params = new Params();
		Properties properties = wb.getStartupParameters().getProperties();
		for (Enumeration e = properties.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			Object value = properties.get(key);
			params.putParam(key, value);
		}

		DocumentImpl schema = new DocumentImpl();
		String format = (String) wb.getStartupParameters().getProperties()
				.get("format");
		if (format != null && "knowtator".equals(format.toLowerCase())) {
			schema.setName(wb.getStartupParameters().getKnowtatorSchemaFile());
			schema.setReader(new FileReader(wb.getStartupParameters()
					.getKnowtatorSchemaFile()));
		}

		// Documents
		System.out.println("WBManager: Reading Documents...");
		List<DocumentReference> documents = new ArrayList();
		StartupParameters sp = wb.getStartupParameters();

		String reportPathnameConstraintString = sp.getProperties().getProperty(
				"ReportPathnameConstraintString");
		String annotationPathnameConstraintString = sp.getProperties()
				.getProperty("AnnotationPathnameConstraintString");
		boolean isMentionLevel = "mention".equals(sp.getProperties()
				.getProperty("ReportLevel"));

		if (reportPathnameConstraintString == null) {
			reportPathnameConstraintString = "corpus";
		}
		if (annotationPathnameConstraintString == null) {
			annotationPathnameConstraintString = "saved";
		}

		Hashtable<String, String> fnamehash = new Hashtable();

		Vector<File> files = FUtils.readFilesFromDirectory(
				sp.getTextInputDirectory(), reportPathnameConstraintString,
				null);

		if (files != null) {
			boolean isReadmission = isReadmissionTask(sp);
			for (File f : files) {
				if (fnamehash.get(f.getName()) != null) {
					continue;
				}
				if (isReportFile(f, isReadmission, isMentionLevel)) {
					String sname = f.getName();
					String lname = f.getAbsolutePath();
					DocumentImpl d = new DocumentImpl();
					d.setName(sname);
					d.setReader(new FileReader(lname));
					documents.add(d);
					fnamehash.put(f.getName(), f.getName());
				}
			}
		}

		Collections.sort(documents, new DocumentImplSorter());
		fnamehash.clear();

		// Primary
		System.out.println("WBManager: Reading Primary Annotations...");
		List<AnnotatorReference> primary = new ArrayList();
		int x = 1;
		files = FUtils.readFilesFromDirectory(
				sp.getAnnotationInputDirectoryFirstAnnotator(),
				annotationPathnameConstraintString, null);
		if (files != null) {
			for (File f : files) {
				if (isReportAnnotationFile(f, isMentionLevel,
						annotationPathnameConstraintString)) {
					String sname = f.getName();
					if (fnamehash.get(sname) != null) {
						continue;
					}
					String lname = f.getAbsolutePath();
					AnnotatorImpl a1 = new AnnotatorImpl();
					a1.setAnnotatorType(AnnotatorType.Primary);
					a1.setName(sname);
					a1.setReader(new FileReader(lname));
					primary.add(a1);
					fnamehash.put(sname, sname);
				}
			}
		}

		Collections.sort(primary, new DocumentImplSorter());
		fnamehash.clear();

		// Secondary
		System.out.println("WBManager: Reading Secondary Annotations...");
		List<AnnotatorReference> secondary = new ArrayList();

		annotationPathnameConstraintString = null; // TEST

		// 12/11/2017: I am starting to add separate path and filename match
		// strings..
		files = FUtils.readFilesFromDirectory(
				sp.getAnnotationInputDirectorySecondAnnotator(),
				annotationPathnameConstraintString, null);
		if (files != null) {
			for (File f : files) {
				if (isReportAnnotationFile(f, isMentionLevel,
						annotationPathnameConstraintString)) {
					String sname = f.getName();
					if (fnamehash.get(sname) != null) {
						continue;
					}
					String lname = f.getAbsolutePath();
					AnnotatorImpl a1 = new AnnotatorImpl();
					a1.setAnnotatorType(AnnotatorType.Secondary);
					a1.setName(sname);
					a1.setReader(new FileReader(lname));
					secondary.add(a1);
					fnamehash.put(sname, sname);
				}
			}
		}

		Collections.sort(secondary, new DocumentImplSorter());
		fnamehash.clear();

		// params.putParam("format", "Knowtator");
		System.out.println("WBManager: Initializing Workbench...");
		loadWBGUI(wb, schema, documents, primary, secondary, params);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				try {
					doTest();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(new JFrame(),
							StrUtils.getStackTrace(e));
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	public static boolean isReportFile(File file, boolean isReadmission,
			boolean isMentionLevel) {
		String fname = file.getName().toLowerCase();
		String fpath = file.getAbsolutePath().toLowerCase();
		if (isMentionLevel) {
			if (file.isFile() && fname.endsWith(".txt")
					&& (!isReadmission || Character.isDigit(fname.charAt(0)))
					&& !fname.contains("xml") && !fname.contains("knowtator")
					&& !fname.toLowerCase().contains("review")) {
				return true;
			}
		} else if (file.isFile() && fname.contains("patient level review")
				&& fname.endsWith("txt")) {
			return true;
		}
		return false;
	}

	public static boolean isReportAnnotationFile(File file,
			boolean isMentionLevel, String annotationPathnameConstraintString) {
		if (isMentionLevel) {
			return isMentionLevelAnnotationFile(file,
					annotationPathnameConstraintString);
		} else {
			return isPatientLevelAnnotationFile(file,
					annotationPathnameConstraintString);
		}
	}

	// public static boolean isReportAnnotationFile(File file) {
	// boolean rv = false;
	// String fname = file.getName().toLowerCase();
	// String pname = file.getAbsolutePath().toLowerCase();
	// if (file.isFile() && pname.contains("saved")
	// && (fname.contains("xml") || fname.contains("knowtator"))
	// && !fname.toLowerCase().contains("review")) {
	// rv = true;
	// }
	// return rv;
	// }

	public static boolean isMentionLevelAnnotationFile(File file,
			String annotationPathnameConstraintString) {
		boolean rv = false;
		String fname = file.getName().toLowerCase();
		String fpath = file.getAbsolutePath();
		String lcfpath = file.getAbsolutePath().toLowerCase();
		if (file.isFile()
				&& (annotationPathnameConstraintString == null || lcfpath
						.contains(annotationPathnameConstraintString))
				&& Character.isDigit(fname.charAt(0))
				&& fname.contains("knowtator") && fname.endsWith("xml")) {
			rv = true;
		}
		return rv;
	}

	public static boolean isPatientLevelAnnotationFile(File file,
			String annotationPathnameConstraintString) {
		boolean rv = false;
		String fname = file.getName().toLowerCase();
		String fpath = file.getAbsolutePath();
		if (file.isFile()
				&& (annotationPathnameConstraintString == null || fpath
						.contains(annotationPathnameConstraintString))
				&& fname.contains("patient level review")
				&& fname.contains("knowtator") && fname.endsWith("xml")) {
			rv = true;
		}
		return rv;
	}

	public static boolean isReadmissionTask(StartupParameters sp) {
		String oname = sp.getProperties().getProperty("ontology");
		boolean isReadmission = false;
		return oname != null && oname.toLowerCase().contains("readmission");
	}

}
