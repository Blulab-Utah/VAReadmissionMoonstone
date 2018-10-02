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
package tsl.knowledge.constraint.temporal;

import java.util.Vector;

import tsl.expression.term.constant.Constant;
import tsl.expression.term.relation.RelationSentence;
import tsl.knowledge.constraint.ConstraintGraph;

public class TemporalConstraintGraph extends ConstraintGraph {

	private static String[] temporalRelations = {};
	private static String[][] consistentTemporalRelations = {};
	
	private void enforceConsistency(Constant startEvent) {

	}

	public Vector<RelationSentence> extractTemporalRelations() {
		Vector<RelationSentence> relations = null;
		return relations;
	}

}
