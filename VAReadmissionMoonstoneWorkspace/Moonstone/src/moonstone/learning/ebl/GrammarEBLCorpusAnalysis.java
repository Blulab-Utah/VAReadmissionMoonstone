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
package moonstone.learning.ebl;

import moonstone.rulebuilder.MoonstoneRuleInterface;

public class GrammarEBLCorpusAnalysis implements Runnable {
	public void run() {
		MoonstoneRuleInterface msri = MoonstoneRuleInterface.RuleEditor;
		GrammarEBL gebl = msri.getGrammarEBL();
		gebl.getDocumentFilesFromTrainingDirectory();
		gebl.analyzeCorpusFiles();
		int x = 1;
	}
}
