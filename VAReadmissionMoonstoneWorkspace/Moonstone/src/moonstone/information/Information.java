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
package moonstone.information;

import java.util.Vector;

import moonstone.grammar.Grammar;
import moonstone.grammar.GrammarModule;
import tsl.expression.term.Term;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.startup.StartupParameters;

public abstract class Information extends Term {

	// public GrammarModule grammarModule = null;
	public Grammar grammar = null;
	public boolean expandImmediately = false;

	public Information() {
		super();
	}

	public Information(Grammar grammar, Vector pattern) {
		super(pattern);
		this.grammar = grammar;
	}

	public static void initialize() {
		try {
			GrammarModule control = GrammarModule.CurrentGrammarModule;
			StartupParameters sp = control.getKnowledgeEngine()
					.getStartupParameters();
			String defaultFile = sp.getResourceFileName("defaults");
			Sexp sexp = (Sexp) JLisp.jLisp.loadFile(defaultFile);
			Vector v = JLUtils.convertSexpToJVector(sexp);
			setDefaults(v);
			String relevantFeatureFile = sp
					.getResourceFileName("relevantFeatures");
			sexp = (Sexp) JLisp.jLisp.loadFile(relevantFeatureFile);
			if (sexp != null) {
				v = JLUtils.convertSexpToJVector(sexp);
				setRelevantFeatures(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Grammar getGrammar() {
		return this.grammar;
	}

	public void setGrammar(Grammar grammar) {
		this.grammar = grammar;
	}

}
