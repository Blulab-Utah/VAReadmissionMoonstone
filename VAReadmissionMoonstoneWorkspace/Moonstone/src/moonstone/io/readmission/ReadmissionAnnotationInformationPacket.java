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

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import moonstone.annotation.Annotation;
import moonstone.rulebuilder.MoonstoneRuleInterface;

import tsl.documentanalysis.document.Document;
import tsl.utilities.VUtils;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.input.knowtator.KTClassMention;
import workbench.api.input.knowtator.KTComplexSlotMention;
import workbench.api.input.knowtator.KTSlotMention;

public class ReadmissionAnnotationInformationPacket {
	public Readmission readmission = null;
	public Document document = null;
	public KTAnnotation annotation = null;
	public String annotationType = null;
	public boolean templateIsNarrative = false;
	public boolean templateIsQA = false;
	public boolean templateIsHeader = false;
	public boolean templateIsTableNameValue = false;
	public boolean templateIsNameValue = false;
	public boolean templateIsOrderedList = false;
	public boolean templateIsInstructions = false;
	public boolean templateIsTable = false;
	public boolean templateIsChecklist = false;
	public String relevantAttribute = null;
	public String relevantAttributeValue = null;
	public String functionalStatusValue = null;
	public KTAnnotation linguisticAttributeAnnotation = null;
	public String linguisticAttributeType = null;
	public String linguisticCertaintyText = null;
	public boolean linguisticAttributeIsNegated = false;
	public KTAnnotation semanticAttributeAnnotation = null;
	public KTAnnotation headerAnnotation = null;
	public String MoonstoneConcept = null;
	public String EHostConcept = null;
	public String MoonstoneSnippet = null;
	public String printedClassMentionSlotsTypesAndText = null;
	public Vector<KTClassMention> sortedClassMentions = null;
	public boolean isRelevant = false;
	public boolean MoonstoneWasCorrect = false;
	public Vector<Annotation> MoonstoneDocumentAnnotations = null;

	public ReadmissionAnnotationInformationPacket(Readmission readmission,
			Document document, KTAnnotation annotation) {
		try {
			this.readmission = readmission;
			this.document = document;
			this.annotation = annotation;
			this.annotationType = annotation.annotatedMention.mentionClassID;

			Object tt = annotation.getSlotValue(new String[] { "Template" });

			this.templateIsNarrative = ("narrative".equals(tt));
			this.templateIsQA = "Q&A".equals(tt);
			this.templateIsTableNameValue = "Table:name:value".equals(tt);
			this.templateIsHeader = "heading/subheading".equals(tt);
			this.templateIsNameValue = "name:value".equals(tt);
			this.templateIsOrderedList = "ordered list".equals(tt);
			this.templateIsInstructions = "Instructions".equals(tt);
			this.templateIsTable = "Table".equals(tt);

			if (tt != null
					&& !(this.templateIsHeader || this.templateIsNarrative
							|| this.templateIsQA
							|| this.templateIsTableNameValue || this.templateIsNameValue)) {
				int x = 1;
			}
			this.relevantAttribute = readmission.relevantTypeAttributeHash
					.get(this.annotationType);
			if (this.relevantAttribute != null) {
				this.relevantAttributeValue = (String) annotation
						.getSlotValue(new String[] { this.relevantAttribute });

				System.out.println("Type=" + this.annotationType + ",Attr="
						+ this.relevantAttribute + ",Value="
						+ this.relevantAttributeValue);

				if (this.relevantAttributeValue != null) {
					int x = 1;
				}
			}
			this.functionalStatusValue = (String) annotation
					.getSlotValue(new String[] { "FUNCTIONALSTATUS" });
			this.linguisticAttributeAnnotation = (KTAnnotation) annotation
					.getSlotValue(new String[] { "hasLingAttribute" });
			this.semanticAttributeAnnotation = (KTAnnotation) annotation
					.getSlotValue(new String[] { "hasSemAttribute" });
			this.headerAnnotation = (KTAnnotation) annotation
					.getSlotValuedAnnotation(new String[] { "hasHeading_SubHeading" });
			if (this.linguisticAttributeAnnotation != null) {
				this.linguisticAttributeType = this.linguisticAttributeAnnotation.annotatedMention.mentionClassID;
				if ("lingAttrib_Certainty".equals(this.linguisticAttributeType)) {
					this.linguisticCertaintyText = this.linguisticAttributeAnnotation
							.getText();
					this.linguisticAttributeIsNegated = textIsNegated(this.linguisticCertaintyText);
				}
			}
			this.sortedClassMentions = annotation.getAnnotatedMention()
					.gatherSortedKTClassMentions();
			this.getMoonstoneEHostConcepts();
			this.getConcatenatedMentionText();
			this.determineAnnotationIsRelevant();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void applyMoonstone() {
		if (this.MoonstoneSnippet != null) {
			this.MoonstoneDocumentAnnotations = this.readmission
					.getDocumentAnnotations(this.MoonstoneSnippet);
			if (this.MoonstoneDocumentAnnotations == null) {
				int x = 1;
			}
			if (containsConcept(this.MoonstoneDocumentAnnotations,
					this.MoonstoneConcept)) {
				this.MoonstoneWasCorrect = true;
			} else if (this.readmission.trainerVerifyMoonstoneResult) {
				String summary = this.getSummary();
				Vector<String> mconcepts = Annotation
						.extractConcepts(this.MoonstoneDocumentAnnotations);
				summary += "\n\nMoonstone Results: " + mconcepts;
				summary += "\n\nWas Moonstone correct?";
				int answer = JOptionPane.showConfirmDialog(new JFrame(),
						summary);
				if (answer == JOptionPane.YES_OPTION) {
					this.MoonstoneWasCorrect = true;
				} else if (answer == JOptionPane.CANCEL_OPTION) {
					this.readmission.trainerVerifyMoonstoneResult = false;
				}
			}
		}
	}

	public void printSummary() {
		String str = getSummary();
		System.out.println(str);
	}

	public String getSummary() {
		StringBuffer sb = new StringBuffer();
		sb.append("***********************\n");
		sb.append(this.getClassMentionSlotsTypesAndText());
		this.getMoonstoneEHostConcepts();
		this.getConcatenatedMentionText();
		sb.append("EHostConcept=" + this.EHostConcept + "\n");
		sb.append("MoonstoneConcept=" + this.MoonstoneConcept + "\n");
		sb.append("Moonstone Snippet=\"" + this.MoonstoneSnippet + "\"\n");
		sb.append("IsRelevant=" + this.isRelevant + "\n");
		return sb.toString();
	}

	void determineAnnotationIsRelevant() {
		if (this.readmission.relevantTypeHash.get(this.annotationType) == null) {
			return;
		}

		if (!(this.templateIsNarrative || this.templateIsQA)) {
			return;
		}
		for (KTSlotMention sm : this.annotation.annotatedMention.slotMentions) {
			Object value = sm.getValue();
			if (value instanceof String && value.toString().contains("model")) {
				return;
			}
		}
		if (this.EHostConcept == null || this.MoonstoneConcept == null
				|| this.MoonstoneSnippet == null) {
			return;
		}
		this.isRelevant = true;
	}

	void getMoonstoneEHostConcepts() {
		if ("FUNCTIONAL_STATUS".equals(this.annotationType)) {
			return;
		}
		String summaryType = null;
		String summaryValue = null;
		summaryType = this.annotationType;
		if (this.functionalStatusValue != null) {
			summaryValue = this.functionalStatusValue;
		}
		if (summaryValue == null && this.relevantAttributeValue != null) {
			summaryType = this.annotationType;
			summaryValue = this.relevantAttributeValue;
		}
		// 3/28/2016:  I broke the default tables with Patient schema, and am
		// not sure how to fix them...
//		if (summaryValue == null) {
//			String defaultAttribute = null;
//			if (this.linguisticAttributeIsNegated) {
//				defaultAttribute = this.readmission.defaultNegatedAttributeHash
//						.get(summaryType);
//			} else {
//				defaultAttribute = this.readmission.defaultAffirmedAttributeHash
//						.get(summaryType);
//			}
//			summaryValue = defaultAttribute;
//		}
		if (summaryType != null && summaryValue != null) {
			this.EHostConcept = summaryType + ":" + summaryValue;
			this.MoonstoneConcept = this.readmission
					.convertConceptEHostToMoonstone(this.EHostConcept);
			if (this.MoonstoneConcept == null) {
				int x = 1;
			}
		}
	}

	void getConcatenatedMentionText() {
		String nonheadertext = this.getConcatenatedNonHeaderText();
		String editedtext = null;

		int tstart = this.sortedClassMentions.firstElement().annotation
				.getTextStart();
		int tend = this.sortedClassMentions.lastElement().annotation
				.getTextEnd();
		String doctext = this.document.getText();
		String covertext = doctext.substring(tstart, tend);

		String atext = this.annotation.getText();
		String mtext = null;
		String htext = null;
		if (this.semanticAttributeAnnotation != null) {
			mtext = this.semanticAttributeAnnotation.getText();
		} else if (this.linguisticAttributeAnnotation != null) {
			mtext = this.linguisticAttributeAnnotation.getText();
		}
		if (this.headerAnnotation != null) {
			htext = this.headerAnnotation.getText();
			// System.out.println("getConcatenatedText():  Header=\"" + htext
			// + "\"");
		}
		if (mtext != null) {
			editedtext = atext;
			if (!editedtext.contains(":")) {
				editedtext += ":";
			}
			editedtext += " " + mtext;
			int x = 1;
		}

		if (editedtext != null) {
			this.MoonstoneSnippet = editedtext;
		} else if (nonheadertext != null) {
			this.MoonstoneSnippet = nonheadertext;
		}

		this.MoonstoneSnippet = nonheadertext;

	}

	void getConcatenatedMentionText_BEFORE_10_23_2015() {
		String nonheadertext = this.getConcatenatedNonHeaderText();
		String editedtext = null;

		int tstart = this.sortedClassMentions.firstElement().annotation
				.getTextStart();
		int tend = this.sortedClassMentions.lastElement().annotation
				.getTextEnd();
		String doctext = this.document.getText();
		String covertext = doctext.substring(tstart, tend);

		String atext = this.annotation.getText();
		String mtext = null;
		String htext = null;
		if (this.semanticAttributeAnnotation != null) {
			mtext = this.semanticAttributeAnnotation.getText();
		} else if (this.linguisticAttributeAnnotation != null) {
			mtext = this.linguisticAttributeAnnotation.getText();
		}
		if (this.headerAnnotation != null) {
			htext = this.headerAnnotation.getText();
		}
		if (mtext != null) {
			editedtext = atext;
			if (!editedtext.contains(":")) {
				editedtext += ":";
			}
			editedtext += " " + mtext;
			int x = 1;
		}
		if (editedtext != null) {
			this.MoonstoneSnippet = editedtext;
		} else if (nonheadertext != null) {
			this.MoonstoneSnippet = nonheadertext;
		}

		this.MoonstoneSnippet = nonheadertext;

	}

	void printClassMentionSlotsTypesAndText() {
		String str = getClassMentionSlotsTypesAndText();
		System.out.print(str);
	}

	String getClassMentionSlotsTypesAndText() {
		StringBuffer sb = new StringBuffer("\n");
		getClassMentionSlotsTypesAndText(sb, this.annotation.annotatedMention,
				0);
		return sb.toString();
	}

	void getClassMentionSlotsTypesAndText(StringBuffer sb, KTClassMention cm,
			int depth) {
		try {
			String blanks = getBlanks(depth);
			sb.append(blanks + "Type=" + cm.mentionClassID + "\n");
			sb.append(blanks + "Text=\"" + cm.annotation.getText() + "\"\n");
			if (cm.getSlotMentions() != null) {
				blanks = getBlanks(depth + 1);
				for (KTSlotMention sm : cm.getSlotMentions()) {
					String svalue = (sm.stringValue != null ? sm.stringValue
							: "*");
					sb.append(blanks + "Slot: Attribute=" + sm.mentionSlotID
							+ ",Value=" + svalue + "\n");
					if (sm instanceof KTComplexSlotMention) {
						KTComplexSlotMention csm = (KTComplexSlotMention) sm;
						getClassMentionSlotsTypesAndText(sb,
								csm.complexSlotClassMention, depth + 2);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String getBlanks(int num) {
		String blanks = "";
		for (int i = 0; i < num; i++) {
			blanks += "  ";
		}
		return blanks;
	}

	String getConcatenatedNonHeaderText() {
		String text = null;
		;
		for (int i = 0; i < this.sortedClassMentions.size(); i++) {
			if (text == null) {
				text = "";
			}
			KTClassMention cm = this.sortedClassMentions.elementAt(i);
			if (!cm.annotation.equals(this.headerAnnotation)) {
				text += cm.annotation.getText();
			}
			if (i < this.sortedClassMentions.size() - 1) {
				text += " ";
			}
		}
		return text;
	}

	private static Vector<String> negatedStrings = VUtils
			.arrayToVector(new String[] { "no", "false" });
	private static Vector<String> affirmedStrings = VUtils
			.arrayToVector(new String[] { "yes", "true" });

	private boolean textIsNegated(String text) {
		if (text == null) {
			return false;
		}
		String str = text.trim().toLowerCase();
		return (negatedStrings.contains(str));
	}

	private boolean textIsAffirmed(String text) {
		if (text == null) {
			return false;
		}
		String str = text.trim().toLowerCase();
		return (affirmedStrings.contains(str));
	}

	String getBinaryType(String concept) {
		return this.readmission.conceptBinaryTypeHash.get(concept);
	}

	boolean equalBinaryTypes(String concept1, String concept2) {
		String t1 = getBinaryType(concept1);
		if (t1 != null) {
			concept1 = t1;
		}
		String t2 = getBinaryType(concept2);
		if (t2 != null) {
			concept2 = t2;
		}
		return (t1 != null && t2 != null && t1.equals(t2));
	}

	boolean containsConcept(Vector<Annotation> annotations, String target) {
		if (annotations != null) {
			Vector<String> concepts = Annotation
					.getAllConceptStrings(annotations);
			Vector<String> allConcepts = new Vector(concepts);
			if (this.readmission.includeMirrorNegatedConcepts) {
				for (String concept : concepts) {
					String negated = this.readmission.negatedConceptTable
							.get(concept);
					if (negated != null) {
						allConcepts = VUtils.add(allConcepts, negated);
					}
				}
			}
			for (String concept : allConcepts) {
				if (target.equals(concept)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean containsConceptOLD(Vector<Annotation> annotations, String concept) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				Object o = annotation.getConcept();

				// 9/28/2015: For boolean type matches.
				if (o != null && equalBinaryTypes(concept, o.toString())) {
					// return true;
				}

				if (o != null && concept.equals(o.toString())
						|| containsConcept(annotation.getChildAnnotations(), concept)) {
					return true;
				}
			}
		}
		return false;
	}

}
