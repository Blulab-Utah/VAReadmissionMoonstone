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

public class ReadmissionProjectSchema668 extends ReadmissionProjectSchema {

	public ReadmissionProjectSchema668() {
		this.relevantTypes = new String[] { "ADL_IADL_COGNITIVE_IMPAIRMENT",
				"ADL_IADL_PHYSICAL_IMPAIRMENT", "HOMELESSNESS", "LIVING_ALONE",
				"SOCIAL_SUPPORT" };

		this.relevantAttributes = new String[] { "ADL_TYPE",
				"COGNITIVEIMPAIRMENT", "FUNCTIONALSTATUS", "HOMELESSNESS",
				"IADL_TYPE", "IMPAIRMENT", "LIVINGALONE", "SOCIALSUPPORT" };

		this.relevantTypeAttributeMap = new String[][] {
				{ "ADL_IADL_COGNITIVE_IMPAIRMENT", "COGNITIVEIMPAIRMENT" },
				{ "FUNCTIONAL_STATUS", "FUNCTIONALSTATUS" },
				{ "ADL_IADL_PHYSICAL_IMPAIRMENT", "FUNCTIONALSTATUS" },
				{ "HOMELESSNESS", "HOMELESSNESS" },
				{ "LIVING_ALONE", "LIVINGALONE" },
				{ "SOCIAL_SUPPORT", "SOCIALSUPPORT" }, };

		this.typeAttributeValueMap = new String[][] {
				{ "HOMELESSNESS", "marginally/temporarily housed", "homeless",
						"assisted living", "not homeless", "nursing home",
						"lives at home" },
				{ "LIVING_ALONE", "living alone", "does not live alone",
						"assisted living" },
				{ "SOCIAL_SUPPORT", "has access to community services",
						"assisted living", "has social support",
						"no social support" } };

		this.defaultAttributeValueMap = new String[][] {
				{ "HOMELESSNESS", "no mention" },
				{ "SOCIAL_SUPPORT", "no mention" },
				{ "LIVING_ALONE", "no mention" } };

		this.targetBinaryConcepts = new String[] { ":HAVE_SUPPORT:",
				":LACK_SUPPORT:", ":LIVING_ALONE:", ":NOT_LIVING_ALONE:",
				":HOMELESS:", ":NOT_HOMELESS:", ":LIVE_AT_HOME:",
				":DOES_NOT_LIVE_AT_HOME:" };

		this.targetConcepts = new String[] { "SOCIALSTRESSORS", "HOMELESSNESS",
				"LIVINGALONE", "SOCIALSUPPORT", "FUNCTIONAL_STATUS",
				"ADL_IADL_PHYSICAL_IMPAIRMENT" };

		this.conceptConversionMap = new String[][] {

				{ "HOMELESSNESS:marginally/temporarily housed",
						":MARGINALLY_HOUSED:" },
				{ "HOMELESSNESS:homeless", ":HOMELESS:" },
				{ "HOMELESSNESS:assisted living", ":ASSISTED_LIVING:" },
				{ "HOMELESSNESS:not homeless", ":NOT_HOMELESS:" },
				{ "HOMELESSNESS:nursing home", ":NURSING_HOME:" },
				{ "HOMELESSNESS:lives at home", ":LIVE_AT_HOME:" },

				{ "LIVING_ALONE:living alone", ":LIVING_ALONE:" },
				{ "LIVING_ALONE:does not live alone", ":NOT_LIVING_ALONE:" },
				{ "LIVING_ALONE:assisted living", ":ASSISTED_LIVING:" },

				{ "SOCIAL_SUPPORT:has access to community services",
						":ACCESS_TO_COMMUNITY_SERVICES:" },
				{ "SOCIAL_SUPPORT:assisted living", ":ASSISTED_LIVING:" },
				{ "SOCIAL_SUPPORT:has social support", ":HAVE_SUPPORT:" },
				{ "SOCIAL_SUPPORT:no social support", ":LACK_SUPPORT:" }, };

		this.translationalConceptMap = new String[][] {
				{ "ADL_IADL_PHYSICAL_IMPAIRMENT:difficulty ambulating",
						"ADL_IADL_PHYSICAL_IMPAIRMENT:Needs assistance/difficulty ambulating" },

				{
						"SOCIAL_SUPPORT:has social support from one person not necessarily living with patient",
						"SOCIAL_SUPPORT:has social support" },

		};

		this.negatedConceptMap = new String[][] {
				{ ":LIVING_ALONE:", ":NOT_LIVING_ALONE:" },
				{ ":HAVE_SUPPORT:", ":LACK_SUPPORT:" },

		};

	}

}
