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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import workbench.api.gui.WBGUI;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.input.knowtator.KTClassMention;
import moonstone.annotation.Annotation;
import moonstone.javafunction.JavaFunctions;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class Readmission {

	MoonstoneRuleInterface msri = null;
	ReadmissionProjectSchema schema = null;

	Vector<ReadmissionQuery> readmissionQueries = null;
	Hashtable<String, Vector<String>> MoonstoneToEHostConversionHash = new Hashtable();
	Hashtable<String, Vector<String>> EHostToMoonstoneConversionHash = new Hashtable();
	Hashtable<String, String> conceptBinaryTypeHash = new Hashtable();
	Hashtable<String, String> translationalConceptHash = new Hashtable();
	Hashtable<String, String> EHostConceptVariableConversionHash = new Hashtable();

	String NarrativeAttributeName = "narrative";
	String ReadmissionQuestionFile = "ReadmissionQuestions";
	Hashtable<String, Integer> correctRuleCountHash = new Hashtable();
	Hashtable<String, Integer> incorrectRuleCountHash = new Hashtable();
	public boolean trainerVerifyMoonstoneResult = false;
	public boolean includeMirrorNegatedConcepts = false;
	public static String[] DocumentDateStringFormats = new String[] {
			"ddMMMyyyy", "MMddyyyy" };

	public static String ProjectSchemaVersionParameter = "ProjectSchemaVersionParameter";

	Hashtable<String, String> relevantTypeAttributeHash = new Hashtable();
	Hashtable<String, String> relevantTypeHash = new Hashtable();
	Hashtable<String, String> relevantAttributeHash = new Hashtable();

	// 3/28/2016
	Hashtable<String, Vector<String>> typeAttributeValueHash = new Hashtable();

	// 12/7/2016
	Hashtable<String, String> attributeValueTypeHash = new Hashtable();

	Hashtable<String, String> negatedConceptTable = new Hashtable();

	// 12/1/2017
	Hashtable<String, Vector<String>> negatedConceptLists = new Hashtable();

	Hashtable<String, String> targetBinaryConceptHash = new Hashtable();

	Hashtable<String, String> defaultAttributeValueHash = new Hashtable();

	Hashtable<String, Vector<String>> eHOSTTargetConceptEquivalenceHash = new Hashtable();

	Hashtable<String, String> patientLevelEquivalentConceptHash = new Hashtable();

	Vector<String> allMoonstoneTargetConcepts = null;
	Vector<String> allEHostTargetConcepts = null;

	public static Readmission CurrentReadmission = null;

	private static Vector<String> InvalidHeaders = VUtils
			.arrayToVector(new String[] { "morse fall scale", "braden scale",
					"braden scale - for predicting pressure sore risk",
					"fall prevention interventions", "other risk factors",
					"for secondary diagnoses", "katz score" });

	private static String[] MoonstoneInvalidHeaderStrings = new String[] {
			"goal", "plan", "review" };

	public Readmission(MoonstoneRuleInterface msri) {
		CurrentReadmission = this;
		this.msri = msri;
		initialize();
	}

	public static Readmission createReadmission(MoonstoneRuleInterface msri) {
		if (CurrentReadmission == null) {
			new Readmission(msri);
		}
		return CurrentReadmission;
	}

	public void getNarrativeAnnotationMatchPercentage(
			MoonstoneRuleInterface msri) {
		Hashtable<String, Integer> conceptCountHash = new Hashtable();
		Hashtable<String, Integer> hitCountHash = new Hashtable();
		Hashtable<String, Integer> missCountHash = new Hashtable();
		Hashtable<String, Vector<String>> matchValues = new Hashtable();
		Hashtable<String, Vector<String>> missValues = new Hashtable();
		Hashtable<String, String> missDocs = new Hashtable();
		Hashtable<String, String> sentenceHash = new Hashtable();
		Vector<ReadmissionAnnotationInformationPacket> correctPackets = new Vector(
				0);
		Vector<ReadmissionAnnotationInformationPacket> incorrectPackets = new Vector(
				0);

		Hashtable adlh = new Hashtable();

		WBGUI workbench = msri.getWorkbench();
		// for (int i = 0; i < targetConcepts.length; i++) {
		// String concept = targetConcepts[i];
		// conceptCountHash.put(concept, 0);
		// }
		if (msri.getWorkbench() != null
				&& workbench.getAnalysis().getAllAnnotations() != null) {
			for (workbench.api.annotation.Annotation wba : workbench
					.getAnalysis().getAllAnnotations()) {

				KTAnnotation kta = wba.getKtAnnotation();
				KTClassMention ktcm = kta.getAnnotatedMention();
				Document doc = wba.getAnnotationCollection()
						.getAnnotationEvent().getDocument();

				String dstr = doc.getText().substring(wba.getStart(),
						wba.getEnd() + 1);
				String astr = wba.getText();
				ReadmissionAnnotationInformationPacket packet = new ReadmissionAnnotationInformationPacket(
						this, doc, kta);

				if (!packet.isRelevant) {
					continue;
				}

				// packet.printSummary();

				HUtils.incrementCount(conceptCountHash, packet.EHostConcept);
				packet.applyMoonstone();

				sentenceHash.put(packet.MoonstoneSnippet,
						packet.MoonstoneSnippet);

				if (packet.MoonstoneWasCorrect) {
					correctPackets.add(packet);
					HUtils.incrementCount(hitCountHash, packet.EHostConcept);
					VUtils.pushIfNotHashVector(matchValues,
							packet.EHostConcept, packet.MoonstoneSnippet);
					// 11/14/2015
					this.countCorrectIncorrectRules(
							packet.MoonstoneDocumentAnnotations, true);
				} else {
					if (packet.EHostConcept.toLowerCase().contains("margin")) {
						int x = 1;
					}
					incorrectPackets.add(packet);
					HUtils.incrementCount(missCountHash, packet.EHostConcept);
					VUtils.pushIfNotHashVector(missValues, packet.EHostConcept,
							packet.MoonstoneSnippet);
					missDocs.put(packet.MoonstoneSnippet, doc.getName());
					this.countCorrectIncorrectRules(
							packet.MoonstoneDocumentAnnotations, false);

				}
			}
		}
		try {
			Vector<String> concepts = HUtils.getKeys(conceptCountHash);
			Collections.sort(concepts);

			String text = "";
			int scount = HUtils.getElements(sentenceHash).size();
			text += "Total Sentences = " + scount + "\n\n";
			for (String concept : concepts) {
				float annotationCount = HUtils.getCount(conceptCountHash,
						concept);
				float hitCount = HUtils.getCount(hitCountHash, concept);
				float missCount = HUtils.getCount(missCountHash, concept);
				float percent = hitCount / annotationCount;
				text += "\n\nCONCEPT=" + concept + "\", Percent=" + percent;
				text += "\n\n\tHits (" + hitCount + "):\n";
				Vector<String> values = (Vector<String>) matchValues
						.get(concept);
				if (values != null) {
					for (String value : values) {
						String dname = missDocs.get(value);
						text += "\t\t \"" + value + "\"(" + dname + ")\n";
					}
				}

				text += "\n\n\tMisses: (" + missCount + "):\n";
				values = (Vector<String>) missValues.get(concept);
				if (values != null) {
					for (String value : values) {
						String dname = missDocs.get(value);
						text += "\t\t \"" + value + "\"(" + dname + ")\n";
					}
				}
			}

			String fname = "C:\\Users\\VHASLCChrisL1\\Desktop\\ReadmissionMoonstone\\Moonstone\\ReadmissionOutput";
			File f = new File(fname);
			if (f.exists()) {
				f.delete();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(fname));
			out.write(text);
			out.close();

			System.out.println(text);

			Vector<Rule> uselessRules = this.getUselessRules();
			if (uselessRules != null) {
				System.out.println("UselessRules: " + uselessRules);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Vector<Annotation> getDocumentAnnotations(String text) {
		// Removed 9/9/2015: Why did I do this? I have to have punctuation to
		// recognize
		// headers.
		// text = StrUtils.convertToLettersDigitsAndSpaces(text);
		// Vector<Annotation> annotations =
		// msri.applyNarrativeGrammarToText(null,
		// text, true, false);
		// 9/11/2015:
		Document doc = new Document("noname", text);
		Vector<Annotation> annotations = msri.applyNarrativeGrammarToText(doc,
				true, true, false);
		return this.msri.getControl().getDocumentGrammar()
				.getDisplayedAnnotations();
	}

	public MoonstoneRuleInterface getMsri() {
		return msri;
	}

	public void initialize() {
		this.schema = new ReadmissionProjectSchemaPatient2();
		// String sname = this.msri.getStartupParameters().getPropertyValue(
		// ProjectSchemaVersionParameter);
		// if ("668".equals(sname)) {
		// this.schema = new ReadmissionProjectSchema668();
		// } else if ("patient".equals(sname)) {
		// this.schema = new ReadmissionProjectSchemaPatient();
		// } else if ("patient2".equals(sname)) {
		// this.schema = new ReadmissionProjectSchemaPatient2();
		// }

		String fstr = null;

		if (schema.conceptConversionMap != null) {
			for (int i = 0; i < schema.conceptConversionMap.length; i++) {
				String[] entry = schema.conceptConversionMap[i];
				String ec = entry[0];
				String mc = entry[1];
				VUtils.pushIfNotHashVector(this.MoonstoneToEHostConversionHash,
						mc, ec);
				VUtils.pushIfNotHashVector(this.EHostToMoonstoneConversionHash,
						ec, mc);
				String[] strs = ec.split(":");
				String vvalue = strs[0];
				String evalue = strs[1];
				VUtils.pushIfNotHashVector(this.EHostToMoonstoneConversionHash,
						evalue, mc);
				this.EHostConceptVariableConversionHash.put(evalue, vvalue);

				this.allMoonstoneTargetConcepts = VUtils.addIfNot(
						this.allMoonstoneTargetConcepts, mc);
				this.allEHostTargetConcepts = VUtils.addIfNot(
						this.allEHostTargetConcepts, ec);
			}
		}

		if (schema.negatedConceptMap != null) {
			for (int i = 0; i < schema.negatedConceptMap.length; i++) {
				String[] entry = schema.negatedConceptMap[i];
				negatedConceptTable.put(entry[0], entry[1]);
				negatedConceptTable.put(entry[1], entry[0]);

				VUtils.pushIfNotHashVector(negatedConceptLists, entry[0],
						entry[1]);
				VUtils.pushIfNotHashVector(negatedConceptLists, entry[1],
						entry[0]);

			}
		}

		if (schema.relevantTypeAttributeMap != null) {
			for (int i = 0; i < schema.relevantTypeAttributeMap.length; i++) {
				String[] entry = schema.relevantTypeAttributeMap[i];
				relevantTypeAttributeHash.put(entry[0], entry[1]);
				relevantTypeAttributeHash.put(entry[1], entry[0]);
			}
		}

		if (schema.relevantTypes != null) {
			for (int i = 0; i < schema.relevantTypes.length; i++) {
				String entry = schema.relevantTypes[i];
				relevantTypeHash.put(entry, entry);
			}
		}

		if (schema.relevantAttributes != null) {
			for (int i = 0; i < schema.relevantAttributes.length; i++) {
				String entry = schema.relevantAttributes[i];
				relevantAttributeHash.put(entry, entry);
			}
		}

		if (schema.typeAttributeValueMap != null) {
			for (int i = 0; i < schema.typeAttributeValueMap.length; i++) {
				String[] entry = schema.typeAttributeValueMap[i];
				String type = entry[0];
				for (int j = 1; j < entry.length; j++) {
					String value = entry[j];
					VUtils.pushHashVector(this.typeAttributeValueHash, type,
							value);
					this.attributeValueTypeHash.put(value, type);
				}
			}
		}

		// 3/28/2016
		if (schema.defaultAttributeValueMap != null) {
			for (String[] entry : schema.defaultAttributeValueMap) {
				this.defaultAttributeValueHash.put(entry[0], entry[1]);
			}
		}

		if (schema.translationalConceptMap != null) {
			for (int i = 0; i < schema.translationalConceptMap.length; i++) {
				String[] entry = schema.translationalConceptMap[i];
				translationalConceptHash.put(entry[0], entry[1]);
			}
		}

		if (schema.targetBinaryConcepts != null) {
			for (int i = 0; i < schema.targetBinaryConcepts.length; i++) {
				String entry = schema.targetBinaryConcepts[i];
				targetBinaryConceptHash.put(entry, entry);
			}
		}
	}

	public String getEHostConceptVariable(String concept) {
		return this.EHostConceptVariableConversionHash.get(concept);
	}

	public static boolean workbenchAnnotationHasAttribute(
			workbench.api.annotation.Annotation wba, String aname, String value) {
		String v = (String) wba.getAttributeValue(aname);
		return value.equals(v);
	}

	public String extractSchemaValueFromMoonstoneConcept(Object concept,
			boolean istype) {
		String value = null;
		if (concept != null) {
			String mc = concept.toString();
			String ec = this.convertConceptMoonstoneToEHost(mc);
			if (ec != null) {
				int index = ec.indexOf(':');
				if (index > 0) {
					if (istype) {
						value = ec.substring(0, index);
					} else {
						value = ec.substring(index + 1);
					}
				}
			}
		}
		return value;
	}

	public String getRelevantTypeAttribute(String type) {
		if (type != null) {
			return this.relevantTypeAttributeHash.get(type);
		}
		return null;
	}

	public boolean conceptIsRelevant(Object concept) {
		boolean result = false;
		if (concept != null) {
			String cstr = concept.toString();
			result = this.MoonstoneToEHostConversionHash.get(cstr) != null;
			if (result) {
				int x = 1;
			}
		}
		return result;
	}

	public boolean annotationIsRelevant(Annotation annotation) {
		return (annotation != null && !this.annotationIsNegated(annotation)
				&& conceptIsRelevant(annotation.getConcept())
				&& annotation.getText() != null && annotation.getText()
				.length() < 200);
	}

	public Annotation getLowestRelevantAnnotation(Annotation annotation) {
		Annotation lowest = null;
		if (annotationIsRelevant(annotation)) {
			Annotation child = getLowestRelevantAnnotation(annotation
					.getSingleChild());
			lowest = (child != null ? child : annotation);
		}
		return lowest;
	}

	public Vector<Annotation> gatherAllRelevantAnnotations(
			Annotation annotation, boolean lowest) {
		return gatherAllRelevantAnnotations(annotation, new Hashtable(), lowest);
	}

	// 8/31/2016: HUGE CHANGE. THIS WILL RECURSE TO THE BOTTOM OF THE PARSETREE,
	// RATHER THAN ONLY LOOKING AT CHILDREN OF RELEVANT ANNOTATIONS. Motivation:
	// "He lives alone at home" had a complex concept between LivingAlone and
	// LivesAtHome.
	private Vector<Annotation> gatherAllRelevantAnnotations(
			Annotation annotation, Hashtable<String, String> chash,
			boolean lowest) {
		Vector<Annotation> annotations = null;
		if (annotation != null && annotationIsRelevant(annotation)) {
			String cstr = annotation.getConcept().toString();
			Vector<String> negations = this.negatedConceptLists.get(cstr);
			boolean priorneg = false;
			if (negations != null) {
				for (String neg : negations) {
					if (chash.get(neg) != null) {
						priorneg = true;
						break;
					}
				}
			}
			if (chash.get(cstr) == null && !priorneg) {
				chash.put(cstr, cstr);
				annotations = VUtils.listify(annotation);
			}
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherAllRelevantAnnotations(child, chash, lowest));
			}
		}
		return annotations;
	}

	private Vector<Annotation> gatherAllRelevantAnnotations_BEFORE_12_1_2017(
			Annotation annotation, Hashtable<String, String> chash,
			boolean lowest) {
		Vector<Annotation> annotations = null;
		if (annotation != null && annotationIsRelevant(annotation)) {
			String cstr = annotation.getConcept().toString();
			String negated = this.negatedConceptTable.get(cstr);
			if (chash.get(cstr) == null
					&& (negated == null || chash.get(negated) == null)) {
				chash.put(cstr, cstr);
				annotations = VUtils.listify(annotation);
			}
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				annotations = VUtils.append(annotations,
						gatherAllRelevantAnnotations(child, chash, lowest));
			}
		}
		return annotations;
	}

	public void countCorrectIncorrectRules(Vector<Annotation> annotations,
			boolean correct) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				this.countCorrectIncorrectRules(annotation, correct);
			}
		}
	}

	public void countCorrectIncorrectRules(Annotation annotation,
			boolean correct) {
		Rule rule = annotation.getRule();
		if (rule != null && !rule.isTerminal()) {
			Hashtable hash = (correct ? this.correctRuleCountHash
					: this.incorrectRuleCountHash);
			HUtils.incrementCount(hash, rule);
			if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					this.countCorrectIncorrectRules(child, correct);
				}
			}

		}
	}

	public Vector<Rule> getUselessRules() {
		Vector keys = HUtils.getKeys(this.incorrectRuleCountHash);
		Vector<Rule> useless = null;
		if (keys != null) {
			for (Object key : keys) {
				Rule rule = (Rule) key;
				int icount = HUtils.getCount(this.incorrectRuleCountHash, key);
				int ccount = HUtils.getCount(this.correctRuleCountHash, key);
				if (icount > 0 && ccount == 0) {
					useless = VUtils.add(useless, rule);
				}
			}
		}
		return useless;
	}

	public boolean isValidHeader(String hstr) {
		if (hstr != null) {
			if (InvalidHeaders.contains(hstr.toLowerCase().trim())) {
				return false;
			}
		}
		return true;
	}

	public String convertConceptMoonstoneToEHost(String mc) {
		String ec = null;
		Vector<String> v = this.MoonstoneToEHostConversionHash.get(mc);
		if (v != null) {
			ec = v.firstElement();
		}
		if (ec != null) {
			String tc = this.translationalConceptHash.get(ec);
			if (tc != null) {
				ec = tc;
			}
		}
		return ec;
	}

	public String convertConceptEHostToMoonstone(String ec) {
		String mc = null;
		Vector<String> v = this.EHostToMoonstoneConversionHash.get(ec);
		if (v != null) {
			mc = v.firstElement();
		}
		return mc;
	}

	public boolean annotationIsNegated(Annotation annotation) {
		return (annotation != null && JavaFunctions.hasPropertyValue(
				annotation, "directionality", "negated"));
	}

	// 1/13/2016:
	public boolean isTargetBinaryConcept(String concept) {
		return this.targetBinaryConceptHash.get(concept) != null;
	}

	public ReadmissionProjectSchema getSchema() {
		return schema;
	}

	public Vector<String> getTypeAttributeValues(String type) {
		return this.typeAttributeValueHash.get(type);
	}

	// 12/7/2016
	public String getAttributeValueType(String value) {
		return this.attributeValueTypeHash.get(value);
	}

	public String getDefaultAttributeValue(String type) {
		return this.defaultAttributeValueHash.get(type);
	}

	// 6/9/2016
	public boolean MoonstoneHeaderTextisValid(String htext) {
		String htlc = htext.toLowerCase();
		for (int i = 0; i < MoonstoneInvalidHeaderStrings.length; i++) {
			if (htlc.contains(MoonstoneInvalidHeaderStrings[i])) {
				return false;
			}
		}
		return true;
	}

	public String getPatientLevelEquivalentConcept(String concept) {
		String str = this.patientLevelEquivalentConceptHash.get(concept);
		if (str != null) {
			return str;
		}
		return concept;
	}

	public Vector<String> getAllMoonstoneTargetConcepts() {
		return allMoonstoneTargetConcepts;
	}

	public Vector<String> getAllEHostTargetConcepts() {
		return allEHostTargetConcepts;
	}

	public Vector<String> getAllEHostVariables() {
		return HUtils.getKeys(this.relevantTypeHash);
	}

	public boolean conceptsAreNegative(String c1, String c2) {
		Vector<String> negatives = this.negatedConceptLists.get(c1);
		return negatives != null && negatives.contains(c2);
	}
	
	public boolean isAttributeRelevant(String aname) {
		return this.relevantAttributeHash.get(aname.toLowerCase()) != null;
	}
	
	public Vector<String> getNegatedConcepts(String cstr) {
		return this.negatedConceptLists.get(cstr);
	}

}
