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
package moonstone.semantic;

import java.util.Hashtable;

import moonstone.grammar.GrammarModule;

public class TypeInfo {

	private Hashtable<String, Hashtable<String, String>> relevantTUIHashHash = new Hashtable();
	private Hashtable<String, String> generalRelevantTUIHash = new Hashtable();
	private static String defaultTypeTUIs = "condition:t007,t019,t020,t033,t034,t037,t038,t039,t040,t042,"
			+ "t046,t047,t048,t050,t059,t060,t067,t074,t061,t162,t163,t182,t184,t190,t191,t201|"
			+ "location:t017,t021,t022,t023,t024,t029,t030|medication:t121";

	public TypeInfo(GrammarModule control) {
		String str = control.getKnowledgeEngine().getStartupParameters()
				.getPropertyValue("RelevantTUIs");
		extractTypeInfo(defaultTypeTUIs);
		extractTypeInfo(str);
	}

	public boolean isRelevantTUI(String typestr, String tui) {
		Hashtable hash = relevantTUIHashHash.get(typestr);
		if (hash != null) {
			return hash.get(tui) != null;
		}
		return false;
	}

	public boolean isRelevantConditionTUI(String tui) {
		return isRelevantTUI("condition", tui);
	}

	public boolean isRelevantLocationTUI(String tui) {
		return isRelevantTUI("location", tui);
	}

	public boolean isGenerallyRelevantTUI(String tui) {
		Object o = generalRelevantTUIHash.get(tui);
		return o != null;
	}

	public void extractTypeInfo(String tuistr) {
		if (tuistr != null) {
			String[] cstrs = tuistr.split("\\|");
			for (int i = 0; i < cstrs.length; i++) {
				String cstr = cstrs[i];
				String[] tstrs = cstr.split(":");
				Hashtable<String, String> hash = new Hashtable();
				String tname = tstrs[0];
				relevantTUIHashHash.put(tname, hash);
				if (tstrs.length == 2) {
					String[] tuis = tstrs[1].split(",");
					for (int j = 0; j < tuis.length; j++) {
						String tui = tuis[j];

						hash.put(tui, tui);

						// 6/22/2015: Also store the tui on the main
						// hashtable to check for general relevance.
						generalRelevantTUIHash.put(tui, tui);
					}
				}
			}
		}
	}

}
