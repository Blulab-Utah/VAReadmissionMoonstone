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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.StrUtils;
import annotation.AnnotationCollection;
import annotation.EVAnnotation;

public class Validation {

	public static void storeValidations(AnnotationAnalysis analysis) {
		String fname = analysis.getArrTool().getStartupParameters()
				.getValidationFile();
		if (fname != null) {
			StringBuffer sb = new StringBuffer();
			for (AnnotationCollection ac : analysis
					.getAllAnnotationCollections()) {
				if (ac.getDocument() != null) {
					for (EVAnnotation annotation : ac.getAnnotations()) {
						if (annotation.isVerified()) {
							String c = annotation.getClassification()
									.getFirstDisplayableValue();
							if (c != null) {
								int start = annotation.getStart();
								int end = annotation.getEnd();
								String istrue = annotation.isVerifiedTrue() ? "true"
										: "false";
								String line = ac.getDocument().getName() + ","
										+ ac.getAnnotatorName() + "," + c + ","
										+ start + "," + end + "," + istrue;
								sb.append(line + "\n");

								String fullline = createFullLine(annotation);
								System.out.println(fullline);
							}
						}
					}
				}
			}
			if (!sb.toString().isEmpty()) {
				// FUtils.writeFile(fname, sb.toString());
			}
		}
	}

	public static String createFullLine(EVAnnotation annotation) {
		StringBuffer sb = new StringBuffer();
		String id = (annotation.getKtAnnotation() != null ? annotation
				.getKtAnnotation().getAnnotatedMentionID() : "*");
		sb.append("ID=" + id + ",");
		sb.append("DOC=" + annotation.getDocument().getName() + ",");
		sb.append("ANN=" + annotation.getAnnotatorType() + ",");
		if (annotation.getClassification() != null
				&& annotation.getClassification().hasDisplayableName()) {
			String cname = annotation.getClassification()
					.getFirstDisplayableValue();
			sb.append("CLASS=" + cname + ",");
		}
		if (annotation.getAttributeNames() != null) {
			for (String aname : annotation.getAttributeNames()) {
				sb.append(aname + "=" + annotation.getAttribute(aname) + ",");
			}
		}
		if (annotation.getRelations() != null) {
			for (String rname : annotation.getRelations()) {
				EVAnnotation relatum = (EVAnnotation) annotation.getRelata(
						rname).firstElement();
				String rid = (relatum != null
						&& relatum.getKtAnnotation() != null ? relatum
						.getKtAnnotation().getID() : "*");
				sb.append(rname + "=" + rid + ",");
			}
		}
		sb.append("START=" + annotation.getStart() + ",");
		sb.append("END=" + annotation.getEnd() + ",");
		String istrue = annotation.isVerifiedTrue() ? "true" : "false";
		sb.append("VERIFIED=" + istrue);
		return sb.toString();
	}

	public static void readValidations(AnnotationAnalysis analysis)
			throws Exception {
		Annotator annotator = null;
		AnnotationCollection ac = null;
		Document doc = null;
		String fname = analysis.getArrTool().getStartupParameters()
				.getValidationFile();
		if (fname != null) {
			File file = new File(fname);
			if (!file.exists()) {
				return;
			}
		} else {
			return;
		}

		if (fname != null) {
			BufferedReader in = new BufferedReader(new FileReader(fname));
			String line = null;
			while ((line = in.readLine()) != null) {
				Vector<String> v = StrUtils.stringList(line, ',');
				if (v != null && v.size() >= 6) {
					String docname = v.elementAt(0);
					String aname = v.elementAt(1);
					String cname = v.elementAt(2);
					int start = Integer.parseInt(v.elementAt(3));
					int end = Integer.parseInt(v.elementAt(4));
					String vstr = v.elementAt(5);
					annotator = analysis.getAnnotator(aname);
					doc = analysis.getDocument(docname, annotator);
					ac = analysis.getAnnotationCollection(doc, annotator);
					boolean verified = Boolean.parseBoolean(vstr);
					if (ac != null) {
						EVAnnotation annotation = ac.findMatchingAnnotation(
								cname, start, end);
						if (annotation != null) {
							annotation.setVerified(true);
							annotation.setVerifiedTrue(verified);
						}
					}
				}
			}
		}
	}
}
