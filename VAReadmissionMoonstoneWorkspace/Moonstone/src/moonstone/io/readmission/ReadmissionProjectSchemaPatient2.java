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

public class ReadmissionProjectSchemaPatient2 extends ReadmissionProjectSchema {

	public ReadmissionProjectSchemaPatient2() {

		this.relevantTypes = new String[] { "HOUSING_SITUATION",
				"LIVING_ALONE", "SOCIAL_SUPPORT" };

		this.relevantAttributes = new String[] { "housingnorm",
				"livingalonenorm", "socialsupportnorm" };

		this.relevantTypeAttributeMap = new String[][] {
				{ "HOUSING_SITUATION", "housingnorm" },
				{ "LIVING_ALONE", "livingalonenorm" },
				{ "SOCIAL_SUPPORT", "socialsupportnorm" }, };

		this.typeAttributeValueMap = new String[][] {
				{
						"HOUSING_SITUATION",
						"homeless/marginally housed/temporarily housed/at risk of homelessness",
						"lives at home/not homeless", "lives in a facility",
						"lives in a permanent single room occupancy", "no mention" },
				{ "LIVING_ALONE", "does not live alone", "living alone", "no mention"},
				{ "SOCIAL_SUPPORT", "has access to community services",
						"no social support", "has social support", "no mention" }, };

		this.defaultAttributeValueMap = new String[][] {
				{ "HOUSING_SITUATION", "no mention" },
				{ "LIVING_ALONE", "no mention" },
				{ "SOCIAL_SUPPORT", "no mention" } };

		this.targetBinaryConcepts = new String[] { ":HAVE_SUPPORT:",
				":LACK_SUPPORT:", ":LIVING_ALONE:", ":NOT_LIVING_ALONE:",
				":LIVE_AT_HOME:", ":DOES_NOT_LIVE_AT_HOME:" };

		this.targetConcepts = new String[] { "housingnorm", "livingalonenorm",
				"socialsupportnorm" };

		this.conceptConversionMap = new String[][] {
				{ "HOUSING_SITUATION:lives at home/not homeless",
						":STABLE_HOUSING:" },
				{
						"HOUSING_SITUATION:homeless/marginally housed/temporarily housed/at risk of homelessness",
						":UNSTABLE_HOUSING:" },
				{ "HOUSING_SITUATION:lives in a facility",
						":LIVES_IN_FACILITY:" },
				{
						"HOUSING_SITUATION:lives in a permanent single room occupancy",
						":SINGLE_ROOM_OCCUPANCY:" },
				{ "LIVING_ALONE:does not live alone", ":NOT_LIVING_ALONE:" },
				{ "LIVING_ALONE:living alone", ":LIVING_ALONE:" },
				{ "SOCIAL_SUPPORT:has access to community services",
						":ACCESS_TO_COMMUNITY_SERVICES:" },
				{ "SOCIAL_SUPPORT:no social support", ":LACK_SUPPORT:" },
				{ "SOCIAL_SUPPORT:has social support", ":HAVE_SUPPORT:" },

		};

		this.translationalConceptMap = null;

		// 10/26/2016: What to do with "does not live at home" and
		// "not homeless"?
		this.negatedConceptMap = new String[][] {
				{ ":LIVING_ALONE:", ":NOT_LIVING_ALONE:" },
				
				{ ":HAVE_SUPPORT:", ":LACK_SUPPORT:" },
				{ ":HAVE_SUPPORT:", ":POSSIBLE_LACK_SUPPORT:" },
				{ ":POSSIBLE_SUPPORT:", ":LACK_SUPPORT:" },
				{ ":POSSIBLE_SUPPORT:", ":POSSIBLE_LACK_SUPPORT:" },
				
				{ ":LIVE_AT_HOME:", ":DOES_NOT_LIVE_AT_HOME:" },
				
				{ ":LIVING_ALONE:", ":UNABLE_TO_LIVE_ALONE:" },
				{ ":NOT_LIVING_ALONE:", ":UNABLE_TO_LIVE_ALONE:" },
				
				{ ":UNSTABLE_HOUSING:", ":STABLE_HOUSING:" } };


	}

}
