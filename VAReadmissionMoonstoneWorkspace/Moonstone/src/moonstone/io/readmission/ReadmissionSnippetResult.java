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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.javafunction.JavaFunctions;
import moonstone.learning.feature.FeatureSet;
import moonstone.rulebuilder.MoonstoneRuleInterface;

import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class ReadmissionSnippetResult {
	ReadmissionPatientResults results = null;
	protected String MoonstoneType = null;
	protected String EHostType = null;
	protected String MoonstoneConcept = null;
	protected String EHostConcept = null;
	protected String EHostAttribute = null;
	protected String EHostValue = null;
	protected String patientName = null;
	protected String snippet = null;
	protected String sentence = null;
	protected boolean isNegated = false;
	protected int generalDictationType = Document.DICTATION_TYPE_OTHER;
	protected float relevanceWeight = 0f;
	protected String tuffyString = null;
	protected int admitDictationDayDifference = -1;
	protected int admitDateRangeIndex = -1;
	protected String documentName = null;
	protected String EHostVariable = "*";

	public static String[] DateRangeDescriptors = { "current", "prevweek",
			"prevmonth", "prev6months", "prevyear", "morethanyear" };

	public static float[] DateRangeWeights = { 1.0f, 0.9f, 0.8f, 0.7f, 0.6f,
			0.5f };

	public ReadmissionSnippetResult(ReadmissionPatientResults results,
			Document document, Annotation annotation) {
		Readmission readmission = results.processor.moonstone.getReadmission();
		this.results = results;
		
		// Note: These are not carried into the Procesor; just for debugging...
		this.snippet = annotation.getText();
		this.sentence = annotation.getSentenceAnnotation().getSentence().getText();
		
		this.admitDictationDayDifference = document
				.getAdmitDictationDayDifference();
		this.admitDateRangeIndex = document.getAdmitDateRangeIndex();
		this.documentName = document.getName();

		this.MoonstoneConcept = annotation.getConcept().toString();
		
		this.EHostType = readmission.extractSchemaValueFromMoonstoneConcept(
				this.MoonstoneConcept, true);
		this.EHostValue = readmission.extractSchemaValueFromMoonstoneConcept(
				this.MoonstoneConcept, false);
		this.EHostAttribute = readmission
				.getRelevantTypeAttribute(this.EHostType);

		String ev = readmission.getEHostConceptVariable(this.EHostValue);
		if (ev != null) {
			this.EHostVariable = ev;
			VUtils.pushHashVector(results.snippetEHostVariableHash, ev, this);
		}

		this.patientName = document.patientName;
		// this.snippet = annotation.getText();

		String sstr = annotation.getSentenceAnnotation().getSentence()
				.getText();
		// this.sentence = StrUtils
		// .replaceNonAlphaNumericCharactersWithSpaces(sstr);
		this.isNegated = JavaFunctions.isNegated(annotation);
		String fname = document.getName();
		VUtils.pushHashVector(this.results.generalSnippetResultHash,
				this.MoonstoneConcept, this);
		VUtils.pushHashVector(this.results.patientSnippetResultHash,
				this.patientName, this);
		String key = this.patientName + "@@" + this.EHostValue;
		VUtils.pushHashVector(this.results.generalSnippetResultHash, key, this);

		key = this.patientName + "@@" + this.MoonstoneConcept;
		VUtils.pushHashVector(this.results.generalSnippetResultHash, key, this);

		// 10/3/2016
		key = this.patientName + "@@" + this.EHostVariable;
		VUtils.pushHashVector(this.results.generalSnippetResultHash, key, this);
		
		// 4/11/2017, for general max,min,mean stats.
		this.results.processor.generalPatientNameHash.put(this.patientName, this.patientName);
		HUtils.incrementCount(this.results.processor.generalPatientVariableSnippetCountHash,
				key);

		this.calculateRelevanceWeight();
		if (this.results.processor.doTuffy) {
			this.addTuffyString(annotation);
		}
	}

	private void addTuffyString(Annotation annotation) {
		String tuffyString = annotation.getSemanticInterpretation()
				.getTuffyString();
		if (tuffyString != null && tuffyString.length() > 2) {
			this.results.processor.tuffySB.append(tuffyString);
		}
	}

	public String toString() {
		String str = "<Patient=" + this.patientName + ",MoonstoneConcept="
				+ this.MoonstoneConcept + ",EHostConcept=" + this.EHostConcept
				+ ",DictatedDate=XXX" + ",AdmitDate=XXX" + ",Negated="
				+ (this.isNegated ? "true" : "false") + ",Text=" + this.snippet + ">";
		return str;
	}

	public ReadmissionPatientResults getResults() {
		return results;
	}

	// public Document getDocument() {
	// return document;
	// }
	//
	// public Annotation getAnnotation() {
	// return annotation;
	// }

	public String getMoonstoneType() {
		return MoonstoneType;
	}

	public String getEHostType() {
		return EHostType;
	}

	public String getMoonstoneConcept() {
		return MoonstoneConcept;
	}

	public String getEHostConcept() {
		return EHostConcept;
	}

	public String getEHostAttribute() {
		return EHostAttribute;
	}

	public String getEHostValue() {
		return EHostValue;
	}

	public String getPatientName() {
		return patientName;
	}

	// public String getSnippet() {
	// return snippet;
	// }

	public boolean isNegated() {
		return isNegated;
	}

	public static class ReadmissionSnippetDictationAdmissionRecencySorter
			implements Comparator {
		public int compare(Object o1, Object o2) {
			ReadmissionSnippetResult sr1 = (ReadmissionSnippetResult) o1;
			ReadmissionSnippetResult sr2 = (ReadmissionSnippetResult) o2;
			int diff1 = sr1.admitDictationDayDifference;
			int diff2 = sr2.admitDictationDayDifference;
			if (diff1 < diff2) {
				return -1;
			}
			if (diff2 < diff1) {
				return 1;
			}
			return 0;
		}
	}

	public int getGeneralDictationType() {
		return generalDictationType;
	}

	public void calculateRelevanceWeight() {
		float typeweight = 0;
		int gdt = this.generalDictationType;
		if (gdt == Document.DICTATION_TYPE_SOCIAL) {
			typeweight = 1;
		} else if (gdt == Document.DICTATION_TYPE_HISTORY) {
			typeweight = 0.8f;
		} else if (gdt == Document.DICTATION_TYPE_PHYSICIAN
				|| gdt == Document.DICTATION_TYPE_NURSE) {
			typeweight = 0.7f;
		} else if (gdt == Document.DICTATION_TYPE_EMERGENCY) {
			typeweight = 0.6f;
		} else {
			typeweight = 0.6f;
		}
		float dateweight = 0;
		int dateIndex = this.admitDateRangeIndex;
		if (dateIndex >= 0) {
			dateweight = DateRangeWeights[dateIndex];
		}
		this.relevanceWeight = dateweight * typeweight;
	}

	public static Vector<ReadmissionSnippetResult> gatherDateRelevantSnippets(
			Vector<ReadmissionSnippetResult> results, int dateIndex) {
		Vector<ReadmissionSnippetResult> relevant = null;
		if (results != null) {
			Collections
					.sort(results,
							new ReadmissionSnippetResult.ReadmissionSnippetDictationAdmissionRecencySorter());
			for (ReadmissionSnippetResult sr : results) {
				int srindex = sr.admitDateRangeIndex;
				if (srindex == dateIndex || dateIndex == -1) {
					relevant = VUtils.add(relevant, sr);
				}
			}
		}
		return relevant;
	}

	// public String getSentence() {
	// return sentence;
	// }

}
