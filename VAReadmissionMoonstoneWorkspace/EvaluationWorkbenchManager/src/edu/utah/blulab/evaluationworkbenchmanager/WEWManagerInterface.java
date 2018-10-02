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
import java.util.List;
import java.util.Vector;

import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

import com.ed.wew.api.AnnotatorImpl;
import com.ed.wew.api.AnnotatorReference;
import com.ed.wew.api.AnnotatorType;
import com.ed.wew.api.DocumentImpl;
import com.ed.wew.api.DocumentReference;
import com.ed.wew.api.Params;
import com.ed.wew.api.ResultTable;
import com.ed.wew.api.WEWManager;

public class WEWManagerInterface {

	public static void main(final String[] args) throws Exception {
		doTest();
	}

	public static EvaluationWorkbench initializeOldWorkbench() {
		EvaluationWorkbench wb = null;
		try {
			wb = new EvaluationWorkbench();
			new StartupParameters(wb, true);
			wb.reinitializeTypeSystem();
			AnnotationAnalysis aa = new AnnotationAnalysis(wb);
			aa.initializeAnnotators();
			aa.readAnnotationCollections();
			aa.postProcessAnnotationCollections();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wb;
	}

	public static void doTest() throws Exception {

		EvaluationWorkbench wb = WEWManagerInterface.initializeOldWorkbench();

		// Schema
		DocumentImpl schema = new DocumentImpl();
		String format = wb.getStartupParameters().getInputTypeFirstAnnotator();
		String schemaFileName = wb.getStartupParameters()
				.getKnowtatorSchemaFile();
		String documentDir = wb.getStartupParameters().getTextInputDirectory();
		String primaryAnnotationDir = wb.getStartupParameters()
				.getAnnotationInputDirectoryFirstAnnotator();
		String secondaryAnnotationDir = wb.getStartupParameters()
				.getAnnotationInputDirectorySecondAnnotator();
		String primaryAnnotatorName = wb.getStartupParameters()
				.getFirstAnnotatorName();
		String secondaryAnnotatorName = wb.getStartupParameters()
				.getSecondAnnotatorName();

		if (schemaFileName != null) {
			schema.setName(schemaFileName);
			schema.setReader(new FileReader(schemaFileName));
		}

		// Documents
		List<DocumentReference> documents = new ArrayList();
		Vector<File> files = readFilesFromDirectory(documentDir, "corpus");
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				DocumentImpl d = new DocumentImpl();
				d.setName(sname);
				d.setReader(new FileReader(lname));
				documents.add(d);
			}
		}

		// Primary
		List<AnnotatorReference> primary = new ArrayList();
		files = readFilesFromDirectory(primaryAnnotationDir, "saved");
		if (files != null) {
			for (File f : files) {
				String sname = f.getName();
				String lname = f.getAbsolutePath();
				AnnotatorImpl a1 = new AnnotatorImpl();
				a1.setAnnotatorType(AnnotatorType.Primary);
				a1.setName(sname);
				a1.setReader(new FileReader(lname));
				primary.add(a1);
			}
		}

		// Secondary
		List<AnnotatorReference> secondary = new ArrayList();
		files = readFilesFromDirectory(secondaryAnnotationDir, "saved");
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

		// Params
		Params params = new Params();
		params.putParam("format", format);
		params.putParam("firstAnnotator", primaryAnnotatorName);
		params.putParam("secondAnnotator", secondaryAnnotatorName);

		ResultTable result = WEWManager.load(schema, documents, primary,
				secondary, params);

		String text = result.toString();

		FUtils.writeFile("/Users/leechristensen/Desktop/WEWManagerResult.txt",
				text);
		// System.out.println("\n\n" + result + "\n\n");
	}

	// 3/29/2017
	public static Vector<File> readFilesFromDirectory(String dname,
			String contained) {
		Vector<File> v = null;
		if (dname != null) {
			File sourcedir = new File(dname);
			if (sourcedir != null && sourcedir.exists()
					&& sourcedir.isDirectory()) {
				v = readFilesFromDirectory(sourcedir, contained);
			}
		}
		return v;
	}

	public static Vector<File> readFilesFromDirectory(File sourcedir,
			String contained) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String fpath = file.getAbsolutePath();
				if (file.isFile() && file.getName().charAt(0) != '.'
						&& fpath.contains(contained)) {
					v = VUtils.add(v, file);
				} else if (file.isDirectory()) {
					v = VUtils.append(v,
							readFilesFromDirectory(file, contained));
				}
			}
		}
		return v;
	}

}
