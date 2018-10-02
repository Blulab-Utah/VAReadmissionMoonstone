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
package tsl.expression.term.relation;

import tsl.expression.term.Term;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.NameSpace;

public class BinaryRelationSentence extends RelationSentence {

	public BinaryRelationSentence() {
		super();
	}

	public BinaryRelationSentence(RelationConstant rc) {
		super(rc);
	}

	public BinaryRelationSentence(RelationConstant rc, Term subject,
			Term modifier) {
		super(rc);
		this.setSubject(subject);
		this.setModifier(modifier);
	}
	
	public static BinaryRelationSentence createBinaryRelationSentence(
			Object relation, Term subject, Term modifier) {
		RelationConstant rc = null;
		if (relation instanceof RelationConstant) {
			rc = (RelationConstant) relation;
		} else if (relation instanceof String) {
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			NameSpace ns = kb.getNameSpace();
			rc = ns.getRelationConstant((String) relation);
		}
		if (rc != null && rc.testAssignments(subject, modifier)) {
			BinaryRelationSentence rs = new BinaryRelationSentence(rc, subject,
					modifier);
			return rs;
		}
		return null;
	}

}
