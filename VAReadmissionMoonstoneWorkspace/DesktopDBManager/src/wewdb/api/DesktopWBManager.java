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
package wewdb.api;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.WEWManager;

import tsl.expression.form.sentence.constraint.Constraint;
import tsl.utilities.StrUtils;
import utility.UnixFormat;
import workbench.api.Analysis;
import workbench.api.gui.WBGUI;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

public class DesktopWBManager {

	private static WBGUI NewWorkbenchGUI = null;

	public static void loadWBGUI(EvaluationWorkbench arrtool,
			final DocumentReference schema,
			final List<DocumentReference> documents,
			final List<AnnotatorReference> primaryAnnotators,
			final List<AnnotatorReference> secondaryAnnotators,
			final Params params) throws Exception {
		Analysis analysis = new Analysis();
		Hashtable<String, Integer> annotationFileNameIndexHash = new Hashtable();
		Hashtable<DocumentReference, String> documentTextHash = new Hashtable();
		KnowtatorIO kio = null;
		String ftype = (String) params.getParams().get("format");
		if (ftype != null
				&& ftype.toLowerCase().equals(
						WEWManager.FormatTypeKnowtator.toLowerCase())) {
			kio = Knowtator.createKnowtatorIO(analysis);
			WEWManager.readKnowtatorSchemaFile(schema, kio);
		}
		for (int i = 0; i < documents.size(); i++) {
			DocumentReference dr = documents.get(i);
			AnnotatorReference primary = primaryAnnotators.get(i);
			AnnotatorReference secondary = secondaryAnnotators.get(i);
			annotationFileNameIndexHash.put(dr.getName(), new Integer(i));
			annotationFileNameIndexHash.put(primary.getName(), new Integer(i));
			annotationFileNameIndexHash
					.put(secondary.getName(), new Integer(i));
			String dtext = WEWManager.readDocumentReference(dr, false);

			// 11/5/2015: Convert to Unix format, so WB annotation snippets will
			// coincide with eHOST snippets.
			dtext = UnixFormat.convertToUnixFormat(dtext);

			documentTextHash.put(dr, dtext);
			WEWManager.readAnnotationCollection(analysis, dr, primary, params,
					kio, dtext);
			WEWManager.readAnnotationCollection(analysis, dr, secondary,
					params, kio, dtext);
		}
		if (kio != null) {
			Knowtator.postProcess(kio, analysis);
		}
		analysis.postProcessAnnotationCollections();
		Constraint.initialize();
		// analysis.initializeAllDefinedConstraintMatches();
		NewWorkbenchGUI = new WBGUI(analysis, arrtool, true);
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
		EvaluationWorkbench wb = DesktopWBManager.initializeOldWorkbench();

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
		Vector<File> files = tsl.utilities.FUtils.readFilesFromDirectory(sp
				.getTextInputDirectory());
		if (files != null) {
			for (File f : files) {
				if (isReportFile(f)) {
					String sname = f.getName();
					String lname = f.getAbsolutePath();
					DocumentImpl d = new DocumentImpl();
					d.setName(sname);
					d.setReader(new FileReader(lname));
					documents.add(d);
				}
			}
		}

		// Primary
		System.out.println("WBManager: Reading Primary Annotations...");
		List<AnnotatorReference> primary = new ArrayList();
		files = tsl.utilities.FUtils.readFilesFromDirectory(sp
				.getAnnotationInputDirectoryFirstAnnotator());
		if (files != null) {
			for (File f : files) {
				if (isReportAnnotationFile(f)) {
					String sname = f.getName();
					String lname = f.getAbsolutePath();
					AnnotatorImpl a1 = new AnnotatorImpl();
					a1.setAnnotatorType(AnnotatorType.Primary);
					a1.setName(sname);
					a1.setReader(new FileReader(lname));
					primary.add(a1);
				}
			}
		}

		// Secondary
		System.out.println("WBManager: Reading Secondary Annotations...");
		List<AnnotatorReference> secondary = new ArrayList();
		files = tsl.utilities.FUtils.readFilesFromDirectory(sp
				.getAnnotationInputDirectorySecondAnnotator());
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Secondary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				secondary.add(a1);
			}
		}

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

	public static boolean isReportFile(File file) {
		String fname = file.getName().toLowerCase();
		String pname = file.getAbsolutePath().toLowerCase();
		if (pname.contains("corpus") && pname.endsWith(".txt")
				&& !fname.contains("xml") && !fname.contains("knowtator")
				&& !fname.contains("patient level review")) {
			return true;
		}
		return false;
	}

	public static boolean isReportAnnotationFile(File file) {
		String fname = file.getName().toLowerCase();
		String pname = file.getAbsolutePath().toLowerCase();
		if (pname.contains("saved")
				&& (fname.contains("xml") || fname.contains("knowtator"))) {
			return true;
		}
		return false;
	}

}
