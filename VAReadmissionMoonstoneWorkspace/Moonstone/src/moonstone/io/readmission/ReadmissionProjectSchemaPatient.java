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

public class ReadmissionProjectSchemaPatient extends ReadmissionProjectSchema {

	public ReadmissionProjectSchemaPatient() {

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
						"not homeless/but other housing situation",
						"living in an assisted living facility",
						"living in a nursing home",
						"living in a group home",
						"lives in a permanent single room occupancy",
						"lives at home",
						"marginally housed/temporarily housed/at risk of homelessness",
						"homeless", },
				{ "LIVING_ALONE", "does not live alone", "living alone" },
				{ "SOCIAL_SUPPORT", "has access to community services",
						"no social support", "has social support" }, };

		this.defaultAttributeValueMap = new String[][] {
				{ "HOUSING_SITUATION", "no mention" },
				{ "LIVING_ALONE", "no mention" },
				{ "SOCIAL_SUPPORT", "no mention" } };

		this.targetBinaryConcepts = new String[] { ":HAVE_SUPPORT:",
				":LACK_SUPPORT:", ":LIVING_ALONE:", ":NOT_LIVING_ALONE:",
				":HOMELESS:", ":NOT_HOMELESS:", ":LIVE_AT_HOME:",
				":DOES_NOT_LIVE_AT_HOME:" };

		this.targetConcepts = new String[] { "housingnorm", "livingalonenorm",
				"socialsupportnorm" };

		this.conceptConversionMap = new String[][] {
				{ "HOUSING_SITUATION:not homeless/but other housing situation",
						":NOT_HOMELESS_BUT_OTHER_LIVING_SITUATION:" },
				{ "HOUSING_SITUATION:living in an assisted living facility",
						":LIVE_IN_ASSISTED_LIVING:" },
				{ "HOUSING_SITUATION:living in a nursing home",
						":LIVE_IN_NURSING_HOME:" },
				{ "HOUSING_SITUATION:living in a group home",
						":LIVE_IN_GROUP_HOME:" },
				{
						"HOUSING_SITUATION:lives in a permanent single room occupancy",
						":SINGLE_ROOM_OCCUPANCY:" },  // Added 10/26/2016
				{ "HOUSING_SITUATION:lives at home", ":LIVE_AT_HOME:" },
				{
						"HOUSING_SITUATION:marginally housed/temporarily housed/at risk of homelessness",
						":MARGINALLY_HOUSED:" },
				{ "HOUSING_SITUATION:homeless", ":HOMELESS:" },

				{ "LIVING_ALONE:does not live alone", ":NOT_LIVING_ALONE:" },
				{ "LIVING_ALONE:living alone", ":LIVING_ALONE:" },

				{ "SOCIAL_SUPPORT:has access to community services",
						":ACCESS_TO_COMMUNITY_SERVICES:" },
				{ "SOCIAL_SUPPORT:no social support", ":LACK_SUPPORT:" },
				{ "SOCIAL_SUPPORT:has social support", ":HAVE_SUPPORT:" },

		};

		this.translationalConceptMap = null;

		this.negatedConceptMap = new String[][] {
				{ ":LIVING_ALONE:", ":NOT_LIVING_ALONE:" },
				{ ":HAVE_SUPPORT:", ":LACK_SUPPORT:" },
				{ ":LIVE_AT_HOME:", ":DOES_NOT_LIVE_AT_HOME:" },
				{ ":HOMELESS:", ":NOT_HOMELESS:" } };

	}

}
