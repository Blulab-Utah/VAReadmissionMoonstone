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
package tsl.network;

import tsl.expression.term.relation.RelationConstant;

public class RelationEdge {
	private int sentenceInstanceID = 0;
	private RelationConstant relationConstant = null;
	private RelationNode subjectTerm = null;
	private RelationNode modifierTerm = null;

	// private Jung Edge

	public RelationEdge(int sid, RelationConstant rc, RelationNode subject,
			RelationNode modifier) {
		this.sentenceInstanceID = sid;
		this.relationConstant = rc;
		this.subjectTerm = subject;
		this.modifierTerm = modifier;
	}

	public int getSentenceInstanceID() {
		return sentenceInstanceID;
	}

	public RelationConstant getRelationConstant() {
		return relationConstant;
	}

	public RelationNode getSubjectTerm() {
		return subjectTerm;
	}

	public RelationNode getModifierTerm() {
		return modifierTerm;
	}

}
