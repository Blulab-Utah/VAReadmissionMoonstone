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

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.io.ehost.MoonstoneEHostXML;
import tsl.documentanalysis.document.Document;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.input.knowtator.KTClassMention;
import workbench.api.input.knowtator.KTSimpleInstance;
import workbench.api.input.knowtator.KTStringSlotMention;

public class ReadmissionSummaryResult {

	private ReadmissionPatientResults patientResults = null;
	private String patientName = null;
	private String semanticVariable = null;
	private String attribute = null;
	private String EHostValue = null;
	private String EHostVariable = "*";
	private String MoonstoneValue = null;
	boolean confirmedAverage = false;
	boolean confirmedInEHost = false;

	boolean confirmedMostCommon = false;
	boolean confirmedMostRecent = false;
	boolean confirmedBestCategory = false;

	boolean matchesEHostAverage = false;
	boolean matchesEHostMostCommon = false;
	boolean matchesEHostMostRecent = false;
	boolean matchesEHostBestCategory = false;
	private Vector<ReadmissionSnippetResult> positiveResults = null;
	private Vector<ReadmissionSnippetResult> negativeResults = null;
	private String statisticalMatchType = null;
	private String aggregateSnippetText = null;

	public Vector<ReadmissionSnippetResult> facilityLivingSnippetResults = null;
	public Vector<ReadmissionSnippetResult> stableLivingSnippetResults = null;
	public Vector<ReadmissionSnippetResult> unstableLivingSnippetResults = null;
	public Vector<ReadmissionSnippetResult> singleRoomLivingSnippetResults = null;
	public Vector<ReadmissionSnippetResult> livingAloneSnippetResults = null;
	public Vector<ReadmissionSnippetResult> notLivingAloneSnippetResults = null;
	public Vector<ReadmissionSnippetResult> haveSupportSnippetResults = null;
	public Vector<ReadmissionSnippetResult> lackSupportSnippetResults = null;
	public Vector<ReadmissionSnippetResult> communityServicenippetResults = null;

	// Before 11/3/2016, new grammar
	// private Vector<ReadmissionSnippetResult> homelessSnippetResults = null;
	// private Vector<ReadmissionSnippetResult> lackSupportSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> haveSupportSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> livingAloneSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> notLivingAloneSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> nursingHomeSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> liveAtHomeSnippetResults = null;
	// private Vector<ReadmissionSnippetResult> assistedLivingSnippetResults =
	// null;
	// private Vector<ReadmissionSnippetResult> groupHomeSnippetResults = null;
	// private Vector<ReadmissionSnippetResult> communityServicenippetResults =
	// null;

	public static String ExcelDelimiter = "|";

	public ReadmissionSummaryResult(ReadmissionPatientResults rpr,
			String pname, String variable, String attr, String evalue) {
		int x = 1;
		this.patientResults = rpr;
		this.patientName = pname;
		this.semanticVariable = variable;
		this.attribute = attr;
		this.EHostValue = evalue;
		Readmission readmission = this.patientResults.processor.moonstone
				.getReadmission();
		this.EHostVariable = readmission.getEHostConceptVariable(evalue);
		if (this.EHostVariable == null) {
			this.EHostVariable = "*";
		}
		this.MoonstoneValue = readmission
				.convertConceptEHostToMoonstone(evalue);
		this.gatherSnippetResultSets();
		x = 2;
		if (rpr.ehostPatientResults != null) {
			this.confirmedInEHost = rpr.ehostPatientResults
					.patientHasClassification(pname, this.getEHostValue());
		}

		this.doEvaluationAverage();
		this.doEvaluationMostCommon();
		this.doEvaluationMostRecent();
		this.doEvaluationBestMatchType();
		// this.getAggregateSnippetText();
		// this.addTuffyString();

		// 12/9/2016

		String key = pname + "@@" + evalue;
		rpr.patientSummaryResultHash.put(key, this);
		VUtils.pushHashVector(rpr.patientSummaryResultHash, pname, this);
		VUtils.pushHashVector(rpr.generalSummaryResultHash, evalue, this);
		rpr.patientNameHash.put(pname, pname);
	}

	public void recordStatisticMatchType(String comparisonType,
			boolean confirmed) {
		String stattype = null;
		if (confirmed && this.confirmedInEHost) {
			stattype = "TP";
		} else if (confirmed && !this.confirmedInEHost) {
			stattype = "FP";
		} else if (!confirmed && this.confirmedInEHost) {
			stattype = "FN";
		} else if (!confirmed && !this.confirmedInEHost) {
			stattype = "TN";
		}
		this.statisticalMatchType = stattype;
		String key = this.EHostValue + ":" + comparisonType + ":" + stattype;
		HUtils.incrementCount(this.patientResults.statisticsCountHash, key);
		HUtils.incrementCount(
				this.patientResults.processor.statisticsCountHash, key);
		String varkey = this.semanticVariable + ":" + comparisonType + ":"
				+ stattype;
		HUtils.incrementCount(
				this.patientResults.processor.statisticsCountHash, varkey);
	}

	public void addTuffyString() {
		try {
			if (this.patientResults.processor.includeTuffyPredicates
					&& this.patientResults.processor.addEHostAnnotationTuffyStrings
					&& this.MoonstoneValue != null
					&& this.MoonstoneValue.length() > 2) {
				String mstr = StrUtils.getTuffyString(this.MoonstoneValue);
				String pstr = "P_" + this.patientName;
				String tstr = null;
				if (this.isconfirmedInEHost()) {
					tstr = "EHostPositive(" + mstr + ", " + pstr + ")\n";
				} else {
					tstr = "EHostNegative(" + mstr + ", " + pstr + ")\n";
				}
				this.patientResults.processor.tuffySB.append(tstr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Removed, 11/4/2016
	// public String generateExcelErrorExampleSummary() {
	// StringBuffer sb = new StringBuffer();
	// int maxlen = 0, neglen = 0, poslen = 0;
	// if (this.positiveResults != null) {
	// poslen = this.positiveResults.size();
	// }
	// if (this.negativeResults != null) {
	// neglen = this.negativeResults.size();
	// }
	// maxlen = Math.max(poslen, neglen);
	// if ("TN".equals(this.statisticalMatchType)
	// || "TP".equals(this.statisticalMatchType)) {
	// return null;
	// }
	// for (int i = 0; i < maxlen; i++) {
	// String line = this.patientName + ExcelDelimiter
	// + this.EHostVariable + ExcelDelimiter + this.EHostValue
	// + ExcelDelimiter + this.statisticalMatchType
	// + ExcelDelimiter + this.getMostCommonConcept()
	// + ExcelDelimiter + this.getMostRecentConcept()
	// + ExcelDelimiter;
	// if (i > 0) {
	// line = "*" + ExcelDelimiter + "*" + ExcelDelimiter + "*"
	// + ExcelDelimiter + "*" + ExcelDelimiter + "*"
	// + ExcelDelimiter + "*" + ExcelDelimiter;
	// }
	// String possent = "*";
	// String negsent = "*";
	// if (i < poslen) {
	// ReadmissionSnippetResult rsr = this.positiveResults
	// .elementAt(i);
	// String pdocname = rsr.documentName;
	// String snippet = StrUtils.convertToLettersDigitsAndSpaces(rsr
	// .getSnippet());
	// line += snippet + ExcelDelimiter + rsr.EHostValue
	// + ExcelDelimiter + rsr.getSentence() + ExcelDelimiter
	// + pdocname + ExcelDelimiter;
	// } else {
	// line += "*" + ExcelDelimiter + "*" + ExcelDelimiter + "*"
	// + ExcelDelimiter + "*" + ExcelDelimiter;
	// }
	// if (i < neglen) {
	// ReadmissionSnippetResult rsr = this.negativeResults
	// .elementAt(i);
	// String ndocname = rsr.documentName;
	// String snippet = StrUtils.convertToLettersDigitsAndSpaces(rsr
	// .getSnippet());
	// line += snippet + ExcelDelimiter + rsr.EHostValue
	// + ExcelDelimiter + rsr.getSentence() + ExcelDelimiter
	// + ndocname;
	// } else {
	// line += "*" + ExcelDelimiter + "*" + ExcelDelimiter + "*"
	// + ExcelDelimiter + "*";
	// }
	// sb.append(line + "\n");
	// break;
	// }
	// return sb.toString();
	// }

	private void doEvaluationMostRecent() {
		String mostRecentEHostConcept = this.getMostRecentConcept();
		if (this.EHostValue.equals(mostRecentEHostConcept)) {
			this.confirmedMostRecent = true;
		}
		this.matchesEHostMostRecent = ((this.confirmedMostRecent && this.confirmedInEHost) || (!this.confirmedMostRecent && !this.confirmedInEHost));
		this.recordStatisticMatchType("recent", this.confirmedMostRecent);
	}

	private void doEvaluationMostCommon() {
		String mostCommonEHostConcept = this.getMostCommonConcept();
		if (this.EHostValue.equals(mostCommonEHostConcept)) {
			this.confirmedMostCommon = true;
		}
		this.matchesEHostMostCommon = ((this.confirmedMostCommon && this.confirmedInEHost) || (!this.confirmedMostCommon && !this.confirmedInEHost));
		this.recordStatisticMatchType("common", this.confirmedMostCommon);
	}

	private void doEvaluationBestMatchType() {
		String best = this.patientResults.processor
				.getTargetConceptPreferredLabelHash(this.EHostValue);
		if ("average".equals(best)) {
			this.confirmedBestCategory = this.confirmedAverage;
		} else if ("common".equals(best)) {
			this.confirmedBestCategory = this.confirmedMostCommon;
		} else if ("recent".equals(best)) {
			this.confirmedBestCategory = this.confirmedMostRecent;
		}
		this.matchesEHostBestCategory = ((this.confirmedBestCategory && this.confirmedInEHost) || (!this.confirmedBestCategory && !this.confirmedInEHost));
		this.recordStatisticMatchType("best", this.confirmedBestCategory);

		if (this.getAttribute().contains("alone")
				&& this.livingAloneSnippetResults != null
				&& !this.matchesEHostBestCategory) {
			System.out.println("\n\nPatient=" + this.patientName
					+ ",Classification=" + this.EHostValue
					+ ",ConfirmedInEHost=" + this.confirmedInEHost);
			if (this.positiveResults != null) {
				System.out.print("POSITIVES=");
				for (ReadmissionSnippetResult s : this.positiveResults) {
					System.out.print(s.snippet + ",");
				}
			}
			if (this.negativeResults != null) {
				System.out.print("NEGATIVES=");
				for (ReadmissionSnippetResult s : this.negativeResults) {
					System.out.print(s.snippet + ",");
				}
			}
			int x = 1;
		}
	}

	private void doEvaluationAverage() {
		if (":STABLE_HOUSING:".equals(this.MoonstoneValue)) {
			this.doEvaluationStableHousing();
		} else if (":UNSTABLE_HOUSING:".equals(this.MoonstoneValue)) {
			this.doEvaluationUnstableHousing();
		} else if (":LIVES_IN_FACILITY:".equals(this.MoonstoneValue)) {
			this.doEvaluationFacilityHousing();
		} else if (":SINGLE_ROOM_OCCUPANCY:".equals(this.MoonstoneValue)) {
			this.doEvaluationSingleRoomHousing();
		} else if (":LIVING_ALONE:".equals(this.MoonstoneValue)) {
			this.doEvaluationLivesAlone();
		} else if (":NOT_LIVING_ALONE:".equals(this.MoonstoneValue)) {
			this.doEvaluationNotLivingAlone();
		} else if (":HAVE_SUPPORT:".equals(this.MoonstoneValue)) {
			this.doEvaluationHaveSupport();
		} else if (":LACK_SUPPORT:".equals(this.MoonstoneValue)) {
			this.doEvaluationLackSupport();
		} else if (":ACCESS_TO_COMMUNITY_SERVICES:".equals(this.MoonstoneValue)) {
			doEvaluationAccessToCommunityServices();
		} else {
			this.confirmedAverage = false;
		}
		this.matchesEHostAverage = ((this.confirmedAverage && confirmedInEHost) || (!this.confirmedAverage && !confirmedInEHost));
		this.recordStatisticMatchType("average", this.confirmedAverage);
	}

	private void gatherSnippetResultSets() {
		this.stableLivingSnippetResults = this.getPatientSnippetResults(
				":STABLE_HOUSING:", "lives at home/not homeless");
		this.unstableLivingSnippetResults = this
				.getPatientSnippetResults(":UNSTABLE_HOUSING:",
						"homeless/marginally housed/temporarily housed/at risk of homelessness");
		this.facilityLivingSnippetResults = this.getPatientSnippetResults(
				":LIVES_IN_FACILITY:", "lives in a facility");
		this.singleRoomLivingSnippetResults = this.getPatientSnippetResults(
				":SINGLE_ROOM_OCCUPANCY:",
				"lives in a permanent single room occupancy");
		this.livingAloneSnippetResults = this.getPatientSnippetResults(
				":LIVING_ALONE:", null);
		this.notLivingAloneSnippetResults = this.getPatientSnippetResults(
				":NOT_LIVING_ALONE:", null);
		this.haveSupportSnippetResults = this.getPatientSnippetResults(
				":HAVE_SUPPORT:", null);
		this.lackSupportSnippetResults = this.getPatientSnippetResults(
				":LACK_SUPPORT:", null);
		this.communityServicenippetResults = this.getPatientSnippetResults(
				":ACCESS_TO_COMMUNITY_SERVICES:", null);
	}

	private void doEvaluationStableHousing() {
		this.positiveResults = this.stableLivingSnippetResults;
		this.negativeResults = this.unstableLivingSnippetResults;
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.facilityLivingSnippetResults);
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.singleRoomLivingSnippetResults);
		this.determineConfirmedAverage(true);
	}

	private void doEvaluationUnstableHousing() {
		this.positiveResults = this.unstableLivingSnippetResults;
		Vector<ReadmissionSnippetResult> nresults = this.stableLivingSnippetResults;
		nresults = VUtils
				.appendNew(nresults, this.facilityLivingSnippetResults);
		nresults = VUtils.appendNew(nresults,
				this.singleRoomLivingSnippetResults);
		this.negativeResults = nresults;

		this.determineConfirmedAverage(true);
	}

	private void doEvaluationFacilityHousing() {
		this.positiveResults = this.facilityLivingSnippetResults;
		this.negativeResults = this.stableLivingSnippetResults;
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.unstableLivingSnippetResults);
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.singleRoomLivingSnippetResults);
		this.determineConfirmedAverage(true);
	}

	private void doEvaluationSingleRoomHousing() {
		this.positiveResults = this.singleRoomLivingSnippetResults;
		this.negativeResults = this.stableLivingSnippetResults;
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.unstableLivingSnippetResults);
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.facilityLivingSnippetResults);
		this.determineConfirmedAverage(true);
	}

	private void doEvaluationLivesAlone() {
		this.positiveResults = this.livingAloneSnippetResults;
		this.negativeResults = this.notLivingAloneSnippetResults;

		// 12/24/2016: Adding bsck assisted living
		this.negativeResults = VUtils.appendNew(this.negativeResults,
				this.facilityLivingSnippetResults);

		this.determineConfirmedAverage(false);
	}

	// 2/23/2016: See if simple inverseof LivingAlone improves performance;
	// no reference to assisted living, etc.
	private void doEvaluationNotLivingAlone() {
		this.positiveResults = this.notLivingAloneSnippetResults;
		this.negativeResults = this.livingAloneSnippetResults;

		// 12/24/2016
		this.positiveResults = VUtils.appendNew(this.positiveResults,
				this.facilityLivingSnippetResults);

		this.determineConfirmedAverage(false);
	}

	private void doEvaluationHaveSupport() {
		this.positiveResults = this.haveSupportSnippetResults;
		this.negativeResults = this.lackSupportSnippetResults;
		this.determineConfirmedAverage(false);
	}

	private void doEvaluationLackSupport() {
		this.positiveResults = this.lackSupportSnippetResults;
		this.negativeResults = this.haveSupportSnippetResults;
		this.determineConfirmedAverage(false);
	}

	private void doEvaluationAccessToCommunityServices() {
		this.positiveResults = this.communityServicenippetResults;
		this.determineConfirmedAverage(false);
	}

	public Vector<ReadmissionSnippetResult> getPatientSnippetResults(
			String msvalue, String ehvalue) {
		String key = this.patientName + "@@" + msvalue;
		Hashtable<String, Vector<ReadmissionSnippetResult>> h = this.patientResults.generalSnippetResultHash;
		Vector<ReadmissionSnippetResult> results = h.get(key);
		if (results == null) {
			key = this.patientName + "@@" + ehvalue;
			results = h.get(key);
		}
		return results;
	}

	public String toXML() {
		String rv = null;
		Vector<KTSimpleInstance> sis = extractKTSimpleInstances();
		StringBuffer sb = new StringBuffer();
		if (sis != null) {
			for (KTSimpleInstance si : sis) {
				sb.append(si.toXML() + "\n");
			}
		}
		rv = sb.toString();
		return rv;
	}

	Vector<KTSimpleInstance> extractKTSimpleInstances() {
		MoonstoneEHostXML mexml = this.patientResults.mexml;
		Vector<KTSimpleInstance> sis = null;
		String datestr = mexml.getEHostDateString();
		String text = StrUtils
				.removeNonAlphaDigitSpaceCharacters(this.EHostValue);
		KTAnnotation kta = new KTAnnotation(
				this.patientResults.mexml.getEHostInstanceID(),
				this.aggregateSnippetText, 0, 1, datestr);
		KTClassMention cm = new KTClassMention(
				this.patientResults.mexml.getEHostInstanceID(),
				this.semanticVariable, text, kta);
		kta.setAnnotatedMentionID(cm.getID());
		KTStringSlotMention ssm = new KTStringSlotMention(
				mexml.getEHostInstanceID(), attribute, this.EHostValue);
		cm.slotMentionIDs = VUtils.add(cm.slotMentionIDs, ssm.getID());

		sis = VUtils.add(sis, kta);
		sis = VUtils.add(sis, ssm);
		sis = VUtils.add(sis, cm);

		return sis;
	}

	// public void getAggregateSnippetText() {
	// String str = "";
	// if (this.positiveResults != null) {
	// str = "POSITIVE:";
	// for (ReadmissionSnippetResult result : this.positiveResults) {
	// str += result.getSnippet() + "||";
	// }
	//
	// }
	// if (this.negativeResults != null) {
	// str += "NEGATIVE:";
	// for (ReadmissionSnippetResult result : this.negativeResults) {
	// str += result.getSnippet() + "||";
	// }
	// }
	// this.aggregateSnippetText = StrUtils
	// .replaceNonDelimNonAlphaNumericCharactersWithSpaces(str, '|');
	// }

	public String getPatientName() {
		return patientName;
	}

	public String getSemanticVariable() {
		return semanticVariable;
	}

	public String getAttribute() {
		return attribute;
	}

	public String getEHostValue() {
		return EHostValue;
	}

	public String getMoonstoneValue() {
		return MoonstoneValue;
	}

	public boolean isConfirmedAverage() {
		return this.confirmedAverage;
	}

	public String toString() {
		String str = "<Patient=" + this.patientName + "Type="
				+ this.semanticVariable + ",EHost=" + this.EHostValue
				+ "Moonstone=" + this.MoonstoneValue + ",Confirmed="
				+ this.confirmedAverage + "MatchesEHost="
				+ this.matchesEHostAverage + ">";
		return str;
	}

	public void determineConfirmedAverage(boolean useClosestTimeslice) {
		// Before 10/4/2016
		determineConfirmedAverage(useClosestTimeslice, 1f, 1f);
		// 10/4/2016-- Would averaging everywhere make a difference?
		// determineConfirmedAverage(false, 1f, 1f);
	}

	public void determineConfirmedAverage(boolean useClosestTimeslice,
			float pfactor, float nfactor) {
		if (this.positiveResults == null && this.negativeResults == null) {
			return;
		}
		float positiveRelevance = 0f;
		float negativeRelevance = 0f;
		float positiveSum = 0f;
		float negativeSum = 0f;
		Vector<ReadmissionSnippetResult> relevantPositive = null;
		Vector<ReadmissionSnippetResult> relevantNegative = null;
		float positiveCount = (this.positiveResults != null ? this.positiveResults
				.size() : 0);
		float negativeCount = (this.negativeResults != null ? this.negativeResults
				.size() : 0);
		float totalCount = positiveCount + negativeCount;

		if (useClosestTimeslice) {
			for (int i = 0; i < Document.DateRanges.length
					&& relevantPositive == null && relevantNegative == null; i++) {
				relevantPositive = ReadmissionSnippetResult
						.gatherDateRelevantSnippets(this.positiveResults, i);
				relevantNegative = ReadmissionSnippetResult
						.gatherDateRelevantSnippets(this.negativeResults, i);
			}
		} else {
			relevantPositive = ReadmissionSnippetResult
					.gatherDateRelevantSnippets(this.positiveResults, -1);
			relevantNegative = ReadmissionSnippetResult
					.gatherDateRelevantSnippets(this.negativeResults, -1);
		}

		if (relevantPositive != null) {
			for (ReadmissionSnippetResult snippet : relevantPositive) {
				positiveSum += (snippet.relevanceWeight * pfactor);
			}
		}
		if (relevantNegative != null) {
			for (ReadmissionSnippetResult snippet : relevantNegative) {
				negativeSum += (snippet.relevanceWeight * nfactor);
			}
		}
		positiveRelevance = positiveSum / totalCount;
		negativeRelevance = negativeSum / totalCount;
		this.confirmedAverage = positiveRelevance >= negativeRelevance;
	}

	public static class ReadmissionSummaryPatientSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ReadmissionSummaryResult sr1 = (ReadmissionSummaryResult) o1;
			ReadmissionSummaryResult sr2 = (ReadmissionSummaryResult) o2;
			int pvalue1 = Integer.parseInt(sr1.patientName);
			int pvalue2 = Integer.parseInt(sr2.patientName);
			if (pvalue1 < pvalue2) {
				return -1;
			}
			if (pvalue1 > pvalue2) {
				return 1;
			}
			return 0;
		}
	}

	private Vector<ReadmissionSnippetResult> filterSnippets(
			Vector<ReadmissionSnippetResult> snippets, boolean negated) {
		Vector<ReadmissionSnippetResult> filtered = null;
		if (snippets != null) {
			for (ReadmissionSnippetResult snippet : snippets) {
				if ((negated && snippet.isNegated())
						|| (!negated && !snippet.isNegated())) {
					filtered = VUtils.add(filtered, snippet);
				}
			}
		}
		return filtered;
	}

	public boolean isconfirmedInEHost() {
		return confirmedInEHost;
	}

	// 10/3/2016
	public String getMostCommonConcept() {
		Hashtable<String, Integer> htable = new Hashtable();
		String key = this.patientName + "@@" + this.EHostVariable;
		Vector<ReadmissionSnippetResult> snippets = this.patientResults.generalSnippetResultHash
				.get(key);
		if (snippets != null) {
			for (ReadmissionSnippetResult snippet : snippets) {
				HUtils.incrementCount(htable, snippet.EHostValue);
			}
		}
		String concept = (String) HUtils.getHighestFrequencyObject(htable);
		return concept;
	}

	public String getMostRecentConcept() {
		String key = this.patientName + "@@" + this.EHostVariable;
		String mostRecentConcept = null;
		int daysSince = 100000;
		Vector<ReadmissionSnippetResult> snippets = this.patientResults.generalSnippetResultHash
				.get(key);
		if (snippets != null) {
			for (ReadmissionSnippetResult snippet : snippets) {
				if (snippet.admitDictationDayDifference < daysSince) {
					daysSince = snippet.admitDictationDayDifference;
					mostRecentConcept = snippet.EHostValue;
				}
			}
		}
		return mostRecentConcept;
	}

	// 12/7/2016
	public static class ReadmissionSummaryResultVariableValueSorter implements
			Comparator {

		public int compare(Object o1, Object o2) {
			ReadmissionSummaryResult sr1 = (ReadmissionSummaryResult) o1;
			ReadmissionSummaryResult sr2 = (ReadmissionSummaryResult) o2;
			String v1 = sr1.getSemanticVariable();
			String v2 = sr2.getSemanticVariable();
			String ev1 = sr1.getEHostValue();
			String ev2 = sr2.getEHostValue();
			int varint = v1.compareTo(v2);
			int evint = ev1.compareTo(ev2);
			if (varint != 0) {
				return varint;
			} else if (evint != 0) {
				return evint;
			}
			return 0;
		}
	}

	public static class ReadmissionSummaryConceptSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ReadmissionSummaryResult sr1 = (ReadmissionSummaryResult) o1;
			ReadmissionSummaryResult sr2 = (ReadmissionSummaryResult) o2;
			String c1 = sr1.getEHostValue();
			String c2 = sr2.getEHostValue();
			return c1.compareTo(c2);
		}
	}

	public static class ReadmissionSummaryVariableSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ReadmissionSummaryResult sr1 = (ReadmissionSummaryResult) o1;
			ReadmissionSummaryResult sr2 = (ReadmissionSummaryResult) o2;
			String c1 = sr1.EHostVariable;
			String c2 = sr2.EHostVariable;
			return c1.compareTo(c2);
		}
	}

}
