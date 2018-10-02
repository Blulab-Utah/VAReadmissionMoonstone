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
package tsl.expression.form;

import java.util.Vector;
import tsl.expression.Expression;
import tsl.expression.form.assertion.Assertion;
import tsl.expression.form.definition.Definition;
import tsl.expression.form.sentence.Sentence;
import tsl.inference.backwardchaining.Query;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;


public abstract class Form extends Expression {

	public Form() {
		super();
	}

	public Form(String name) {
		super(name);
	}
	
	public Form(Vector pattern) {
		super(pattern);
	}

	public static Form createForm(Vector v) {
		Form f = null;
		if ((f = Query.createQuery(v)) != null
				|| (f = Sentence.createSentence(v)) != null
				|| (f = Assertion.createAssertion(v)) != null
				|| (f = Definition.createDefinition(v)) != null) {
			return f;
		}
		return null;
	}

	public static Form createForm(Sexp sexp) {
		Vector v = TLUtils.convertSexpToJVector(sexp);
		return createForm(v);
	}

}
