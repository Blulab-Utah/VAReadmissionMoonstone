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
package moonstone.io.ehost;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import edu.utah.blulab.evaluationworkbenchmanager.EvaluationWorkbenchManager;
import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.SeqUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.input.knowtator.KTClassMention;
import workbench.api.input.knowtator.KTSimpleInstance;
import workbench.api.input.knowtator.KTStringSlotMention;
import moonstone.annotation.Annotation;
import moonstone.io.readmission.Readmission;
import moonstone.io.readmission.ReadmissionPatientResults;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class MoonstoneEHostXML {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private int EHostID = 0;
	private boolean useLowestOnly = false;

	private Vector<String> relevantAttributeNames = VUtils
			.arrayToVector(new String[] { "concept" });

	public MoonstoneEHostXML(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
	}

	public void readmissionGenerateEHostAnnotationsNested(
			MoonstoneRuleInterface msri, boolean targetsOnly) {
		readmissionGenerateEHostAnnotationsNestedDirectories(msri, targetsOnly);
	}

	public void readmissionGenerateEHostAnnotationsFlat(
			MoonstoneRuleInterface msri, boolean targetsOnly) {
		readmissionGenerateEHostAnnotationsFlatDirectory(msri, targetsOnly);
	}

	public void readmissionGenerateEHostAnnotationsFlatDirectory(
			MoonstoneRuleInterface msri, boolean targetsOnly) {
		Readmission readmission = Readmission.createReadmission(msri);
		String inputdirname = this.moonstoneRuleInterface
				.getStartupParameters().getPropertyValue("TextInputDirectory");
		String outputdirname = this.moonstoneRuleInterface
				.getStartupParameters().getPropertyValue(
						"ResultsOutputDirectory");
		int fcount = 0;
		File odfile = new File(outputdirname);
		if (odfile.exists()) {
			odfile.delete();
		}

		File sourcedir = new File(inputdirname);
		File[] files = sourcedir.listFiles();

		System.out.println("About to process " + files.length + " files...");

		int fileStartIndex = 0;
		int fileEndIndex = files.length;

		String startstr = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue("ReadmissionFileStartIndex");
		String endstr = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue("ReadmissionFileEndIndex");

		if (startstr != null && endstr != null) {
			fileStartIndex = Integer.parseInt(startstr);
			fileEndIndex = Integer.parseInt(endstr);
		}
		String rangestr = fileStartIndex + "_" + fileEndIndex;

		for (int i = fileStartIndex; i < fileEndIndex; i++) {
			File file = files[i];

			// String fname = file.getName().toLowerCase();
			String fname = file.getName();

			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}

			if (!fname.endsWith(".txt") || fname.contains("xml")
					|| fname.contains("knowtator")) {
				continue;
			}

			String pname = Document.extractPatientNameFromReportName(file
					.getName());
			if (pname == null) {
				continue;
			}

			if (ReadmissionPatientResults.doRejectFileName(fname)) {
				continue;
			}

			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			// String casename = null;
			// String fpath = file.getAbsolutePath();
			// String delim = Pattern.quote("\\");
			// Vector v = StrUtils.stringList(fpath, delim);
			// if (v != null && v.size() > 2) {
			// String penult = (String) v.elementAt(v.size() - 1);
			// if (Character.isDigit(penult.charAt(0))) {
			// casename = penult;
			// }
			// }

			System.out.print("Processing [" + i + "]: " + fname + "...");
			long start = System.currentTimeMillis();
			Document doc = new Document(fname, text);
			doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
			Vector<Annotation> targets = this.gatherTargetAnnotations(
					readmission, doc);
			try {
				String htmlfilename = StrUtils.textToHtml(file.getName());
				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
				xml += this.toXML(targets);
				if (targets != null) {
					System.out.println(xml);
					int x = 1;
				}
				xml += "</annotations>\n";

				String sname = outputdirname + File.separatorChar;
				if (pname != null) {
					sname += pname + File.separatorChar;
				}
				sname += file.getName() + ".knowtator.xml";
				fcount++;
				FUtils.writeFile(sname, xml);
				this.moonstoneRuleInterface.releaseAnnotations();
			} catch (Exception e) {
				e.printStackTrace();
			}

			long end = System.currentTimeMillis();
			long duration = end - start;
			this.moonstoneRuleInterface.releaseAnnotations();
			System.out.println("(" + duration + " milliseconds)");
		}
		System.out
				.println("ReadmissionGeneralEHostAnnotation Done.  File count="
						+ fcount);
	}

	public void readmissionGenerateEHostAnnotationsNestedDirectories(
			MoonstoneRuleInterface msri, boolean targetsOnly) {
		Readmission readmission = Readmission.createReadmission(msri);
		String inputdirname = this.moonstoneRuleInterface
				.getStartupParameters().getPropertyValue("TextInputDirectory");
		String outputdirname = this.moonstoneRuleInterface
				.getStartupParameters().getPropertyValue(
						"ResultsOutputDirectory");
		int fcount = 0;
		File odfile = new File(outputdirname);
		Hashtable<String, Integer> conceptCountHash = new Hashtable();
		Hashtable<String, Integer> conceptDirectCountHash = new Hashtable();
		if (odfile.exists()) {
			odfile.delete();
		}
		Vector<File> files = FUtils.readFilesFromDirectory(inputdirname);

		int i = 0;
		for (File file : files) {
			String fname = file.getName().toLowerCase();

			if (!EvaluationWorkbenchManager.isReportFile(file, true, true)) {
				continue;
			}

			if (!fname.endsWith(".txt") || fname.contains("xml")
					|| fname.contains("knowtator")) {
				continue;
			}

			String pname = Document.extractPatientNameFromReportName(file
					.getName());
			if (pname == null) {
				continue;
			}

			String text = FUtils.readFile(file);
			try {
				text = UnixFormat.convertToUnixFormat(text);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			System.out.print("Processing [" + i++ + "]: " + fname + "...");
			long start = System.currentTimeMillis();
			Document doc = new Document(fname, text);

			doc.extractPatientNameAndDatesFromFirstLine(Readmission.DocumentDateStringFormats);
			Vector<Annotation> targets = this.gatherTargetAnnotations(
					readmission, doc);

			try {
				String htmlfilename = StrUtils.textToHtml(file.getName());

				String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<annotations textSource=\"" + htmlfilename + "\">\n";
				xml += this.toXML(targets);
				if (targets != null) {

					// 5/29/2018: Calculate ratio of direct concept statements
					// to inferred ones.
					for (Annotation target : targets) {
						String key = target.getConcept().toString();
						HUtils.incrementCount(conceptCountHash, key);
						if (target.containsRuleStatingTargetConceptDirectly()) {
							HUtils.incrementCount(conceptDirectCountHash, key);
						}
					}
					System.out.println(xml);
					int x = 1;
				}
				xml += "</annotations>\n";

				String sname = outputdirname + File.separatorChar;
				if (pname != null) {
					sname += pname + File.separatorChar;
				}
				sname += file.getName() + ".knowtator.xml";
				fcount++;
				FUtils.writeFile(sname, xml);
			} catch (Exception e) {
				e.printStackTrace();
			}

			long end = System.currentTimeMillis();
			long duration = end - start;
			this.moonstoneRuleInterface.releaseAnnotations();
			System.out.println("(" + duration + " milliseconds)");
		}
		System.out
				.println("ReadmissionGeneralEHostAnnotation Done.  File count="
						+ fcount);

		for (String concept : conceptCountHash.keySet()) {
			int allcount = conceptCountHash.get(concept);
			int dircount = 0;
			Object o = conceptDirectCountHash.get(concept);
			if (o instanceof Integer) {
				dircount = ((Integer) o).intValue();
			}
			float dratio = (float) dircount / (float) allcount;
			System.out.println("Concept= " + concept + ", DirectRatio="
					+ dratio);
		}
	}

	public String generateEHostXMLSingleDocument(MoonstoneRuleInterface msri,
			String text, boolean targetsOnly) {
		String xml = "";

		// Readmission readmission = new Readmission(msri);
		Readmission readmission = Readmission.createReadmission(msri);
		Document doc = new Document(null, text);
		Vector<Annotation> targets = this.gatherTargetAnnotations(readmission,
				doc);
		try {
			xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			xml += "<annotations textSource=\"000\">\n";
			xml += this.toXML(targets);
			xml += "</annotations>\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.moonstoneRuleInterface.releaseAnnotations();
		return xml;
	}

	public Vector<Annotation> gatherTargetAnnotations(Readmission readmission,
			Document doc) {
		Vector<Annotation> targets = null;
		Vector<Annotation> annotations = this.moonstoneRuleInterface
				.applyNarrativeGrammarToText(doc, true, true, true);
		annotations = this.moonstoneRuleInterface.getControl()
				.getDocumentGrammar().getDisplayedAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> relevant = null;
				if (annotation.getGoodness() < 0.25) {
					continue;
				}
				if (this.useLowestOnly) {
					Annotation ann = readmission
							.getLowestRelevantAnnotation(annotation);
					relevant = VUtils.listify(ann);
				} else {
					relevant = readmission.gatherAllRelevantAnnotations(
							annotation, false);
				}
				if (relevant != null) {
					// targets = appendIfSeparate(targets, relevant);
					targets = VUtils.append(targets, relevant);
				}
			}
		}
		targets = removeDuplicates(targets);
		return targets;
	}

	public Vector<Annotation> gatherTargetAnnotations_BEFORE_12_12_2017(
			Readmission readmission, Document doc) {
		Vector<Annotation> targets = null;
		Vector<Annotation> annotations = this.moonstoneRuleInterface
				.applyNarrativeGrammarToText(doc, true, true, true);
		annotations = this.moonstoneRuleInterface.getControl()
				.getDocumentGrammar().getDisplayedAnnotations();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Vector<Annotation> relevant = null;
				if (annotation.getGoodness() < 0.25) {
					continue;
				}

				if (this.useLowestOnly) {
					Annotation ann = readmission
							.getLowestRelevantAnnotation(annotation);
					relevant = VUtils.listify(ann);
				} else {
					relevant = readmission.gatherAllRelevantAnnotations(
							annotation, false);
				}
				if (relevant != null) {
					targets = appendIfSeparate(targets, relevant);
					// targets = VUtils.append(targets, relevant);
				}
			}
		}
		return targets;
	}

	public void displayParsedSentenceXML() {
		Vector<Annotation> annotations = this.moonstoneRuleInterface
				.getControl().getDocumentGrammar().getDisplayedAnnotations();
		String xml = toXML(annotations);
		System.out.println(xml);
	}

	public String toXML(Vector<Annotation> annotations) {
		String rv = null;
		Vector<KTSimpleInstance> sis = extractAnnotations(annotations);
		StringBuffer sb = new StringBuffer();
		if (sis != null) {
			for (KTSimpleInstance si : sis) {
				sb.append(si.toXML() + "\n");
			}
		}
		rv = sb.toString();
		return rv;
	}

	public Vector<KTSimpleInstance> extractAnnotations(
			Vector<Annotation> annotations) {
		// KnowtatorIO kio = this.moonstoneRuleInterface.getWorkbench()
		// .getAnalysis().getKnowtatorIO();
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				sis = VUtils.append(sis, extractKTSimpleInstances(annotation));
			}
		}
		return sis;
	}

	Vector<KTSimpleInstance> extractKTSimpleInstances(Annotation annotation) {
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		Object concept = annotation.getConcept();
		String semanticType = rd.extractSchemaValueFromMoonstoneConcept(
				concept, true);
		if (semanticType != null && semanticType.toLowerCase().contains("phys")) {
			int x = 1;
		}
		String attributeName = rd.getRelevantTypeAttribute(semanticType);
		String attributeValue = rd.extractSchemaValueFromMoonstoneConcept(
				concept, false);

		if (attributeName != null && attributeValue != null) {
			int start = annotation.getTextStart();
			int end = annotation.getTextEnd();
			String datestr = getEHostDateString();
			String text = annotation.getText();

			try {
				String substr = annotation.getDocument().getText()
						.substring(start, end + 1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			text = StrUtils.removeNonAlphaDigitSpaceCharacters(text);

			KTAnnotation kta = new KTAnnotation(getEHostInstanceID(),
					annotation.getText(), annotation.getTextStart(),
					annotation.getTextEnd() + 1, datestr);
			KTClassMention cm = new KTClassMention(getEHostInstanceID(),
					semanticType, text, kta);
			kta.setAnnotatedMentionID(cm.getID());
			KTStringSlotMention ssm = new KTStringSlotMention(
					getEHostInstanceID(), attributeName, attributeValue);
			cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());

			sis = VUtils.add(sis, kta);
			sis = VUtils.add(sis, ssm);
			sis = VUtils.add(sis, cm);
		}
		return sis;
	}

	Vector<KTSimpleInstance> extractKTSimpleInstancesWithComplexMentions(
			Annotation annotation) throws Exception {
		Vector<KTSimpleInstance> sis = null;
		Readmission rd = this.moonstoneRuleInterface.getReadmission();
		Object concept = annotation.getConcept();
		String semanticType = rd.extractSchemaValueFromMoonstoneConcept(
				concept, true);
		String attributeName = rd.getRelevantTypeAttribute(semanticType);
		Object attributeValue = rd.extractSchemaValueFromMoonstoneConcept(
				concept, false);
		if (attributeName != null && attributeValue != null) {
			int start = annotation.getTextStart();
			int end = annotation.getTextEnd();
			String datestr = getEHostDateString();
			String text = annotation.getText();
			text = StrUtils.removeNonAlphaDigitSpaceCharacters(text);
			KTAnnotation kta = new KTAnnotation(getEHostInstanceID(),
					annotation.getText(), annotation.getTextStart(),
					annotation.getTextEnd(), datestr);

			KTClassMention cm = new KTClassMention(getEHostInstanceID(),
					semanticType, (String) attributeValue, kta);
			sis = VUtils.add(sis, cm);
			sis = VUtils.add(sis, kta);

			if (attributeValue instanceof Annotation) {
				Annotation child = (Annotation) attributeValue;
				Vector<KTSimpleInstance> csis = extractKTSimpleInstancesWithComplexMentions(child);
				if (csis != null) {
					KTClassMention ccm = (KTClassMention) csis.firstElement();
					// KTComplexSlotMention cplxsm = new KTComplexSlotMention();
					// cplxsm.complexSlotClassMention = ccm;
					// cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs,
					// cplxsm.getID());
					// sis = VUtils.add(sis, cplxsm);
				}
			} else {
				KTStringSlotMention ssm = new KTStringSlotMention(
						getEHostInstanceID(), attributeName,
						(String) attributeValue);
				kta.setAnnotatedMentionID(ssm.getID());
				cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());
				sis = VUtils.add(sis, ssm);
			}

		}
		return sis;
	}

	// Thu Jan 31 15:54:18 MST 2013

	public String getEHostDateString() {
		Calendar c = Calendar.getInstance();
		Date date = new Date();
		c.setTime(date);
		return c.getTime().toString();
	}

	public String getEHostInstanceID() {
		String rv = "EHOST_Instance_" + this.EHostID++;
		rv = rv.trim();
		return rv;
	}

	public Vector<Annotation> appendIfSeparate(Vector<Annotation> annotations1,
			Vector<Annotation> annotations2) {
		if (annotations1 == null) {
			return annotations2;
		}
		if (annotations2 == null) {
			return annotations1;
		}
		Vector<Annotation> v = new Vector(annotations1);
		for (Annotation a2 : annotations2) {
			boolean foundDuplicate = false;
			for (Annotation a1 : v) {
				if (a1.getConcept().equals(a2.getConcept())
						&& SeqUtils.overlaps(a1.getTextStart(),
								a1.getTextEnd(), a2.getTextStart(),
								a2.getTextEnd())
						&& a1.getGrammar().equals(a2.getGrammar())) {
					foundDuplicate = true;
					break;
				}
			}
			if (!foundDuplicate) {
				v.add(a2);
			}
		}
		return v;
	}

	public Vector<Annotation> removeDuplicates(Vector<Annotation> annotations) {
		Hashtable<Annotation, Annotation> dhash = new Hashtable();
		Vector<Annotation> v = null;
		if (annotations != null) {
			for (Annotation a1 : annotations) {
				for (Annotation a2 : annotations) {
					if (Annotation.sameConceptSamePolarity(a1, a2)
							&& SeqUtils.overlaps(a1.getTextStart(),
									a1.getTextEnd(), a2.getTextStart(),
									a2.getTextEnd())) {
						if (a1.getNumericID() < a2.getNumericID()) {
							dhash.put(a1, a1);
						} else if (a2.getNumericID() < a1.getNumericID()) {
							dhash.put(a2, a2);
						}
					}
				}
			}
			for (Annotation a1 : annotations) {
				if (dhash.get(a1) == null) {
					v = VUtils.addIfNot(v, a1);
				}
			}
		}
		return v;
	}

	public Vector<Annotation> appendIfSeparate_BEFORE_11_14_2017(
			Vector<Annotation> annotations1, Vector<Annotation> annotations2) {
		if (annotations1 == null) {
			return annotations2;
		}
		if (annotations2 == null) {
			return annotations1;
		}
		Vector<Annotation> v = new Vector(annotations1);
		for (Annotation a2 : annotations2) {
			boolean foundDuplicate = false;
			for (Annotation a1 : v) {
				if (a1.getConcept().equals(a2.getConcept())
						&& SeqUtils.overlaps(a1.getTextStart(),
								a1.getTextEnd(), a2.getTextStart(),
								a2.getTextEnd())) {
					foundDuplicate = true;
					break;
				}
			}
			if (!foundDuplicate) {
				v.add(a2);
			}
		}
		return v;
	}

}
