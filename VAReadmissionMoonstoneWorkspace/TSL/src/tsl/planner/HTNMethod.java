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
package tsl.planner;

import java.util.Vector;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.rule.RuleSentence;
import tsl.utilities.VUtils;

public class HTNMethod extends RuleSentence {

	public HTNMethod(Vector v) {
		super();
		Vector<String> nv = (Vector) v.firstElement();
		this.setName(nv.elementAt(2));
		Vector rv = (Vector) VUtils.assocValueTopLevel("task", v);
		this.setConsequent(new HTNTaskSentence(rv));
		Vector<Vector> stvs = VUtils.assocTopLevel("subtasks", v);
		Vector<Sentence> subtasks = null;
		if (stvs != null) {
			for (Vector stv : stvs) {
				String tname = (String) stv.firstElement();
				Vector sv = (Vector) stv.elementAt(1);
				Vector pcv = (Vector) stv.elementAt(2);
				HTNTaskSentence stsent = new HTNTaskSentence(sv);
				stsent.setName(tname);
				stsent.setMethod(this);
				Sentence precond = Sentence.createSentence(pcv);
				stsent.setPrecondition(precond);
				subtasks = VUtils.add(subtasks, stsent);
			}
			this.setAntecedent(new AndSentence(subtasks));
		}
	}

	public static HTNMethod createHTNMethod(Vector v) {
		if (v != null
				&& v.firstElement() instanceof Vector
				&& "htnmethod".equals(((Vector) v.firstElement())
						.firstElement())) {
			return new HTNMethod(v);
		}
		return null;
	}

}
