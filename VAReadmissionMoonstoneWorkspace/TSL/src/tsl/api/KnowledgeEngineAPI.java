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
package tsl.api;

import java.util.ArrayList;

import tsl.knowledge.engine.KnowledgeEngine;

public class KnowledgeEngineAPI {
	
	private static String report = "The patient is an 88 year old female with coronary artery disease," +
	"congestive heart failure and diabetes mellitus who presented with fever, abdominal pain after " +
	"being found down at her nursing home.  Her history patient is a resident at " +
	"[**Hospital 192**] who was found status post questionable fall the morning of admission  " +
	"and was noted to have a left-sided weakness without head trauma or loss of consciousness.  " +
	"The fall was unwitnessed.  Subsequently the patient had a large occult blood positive " +
	"stool and was also found to have complaints of abdominal pain.  At the nursing home the " +
	"temperature was 102.1 with a pulse of 126, blood in by ambulance to [**Hospital 86**] " +
	"Emergency Department for evaluation with a temperature of " +
	"103.2, pulse 120, blood pressure 108/40 and respiratory rate of 30 with an oxygen " +
	"saturation of 94%.  In the Emergency Department the patient was found to have an increased respiratory rate.  " +
	"She denied cough, chest pain, shortness of breath, nausea and vomiting or dysuria.  " +
	"She did complain of abdominal pain and diarrhea.  The patient is demented at baseline.  " +
	"The patient denied any fevers or chills prior though it is unclear but it is possibly " +
	"p.o. intake had been decreased for several days.";
	
	public static void main(String[] args) {
		KnowledgeEngineAPI api = new KnowledgeEngineAPI();
		ArrayList<NamedEntityWrapper> wrappers = api.getNamedEntities(report);
		if (wrappers != null) {
			for (NamedEntityWrapper wrapper : wrappers) {
				System.out.println(wrapper);
			}
		}
	}
	
	public KnowledgeEngineAPI() {
		KnowledgeEngine.getCurrentKnowledgeEngine();
	}
	
	public ArrayList<NamedEntityWrapper> getNamedEntities(String text) {
		KnowledgeEngine.getCurrentKnowledgeEngine();
		return NamedEntityWrapper.getNamedEntities(text);
	}

}
