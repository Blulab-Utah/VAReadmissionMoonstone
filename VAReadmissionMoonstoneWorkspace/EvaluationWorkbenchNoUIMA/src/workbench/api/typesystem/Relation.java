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
package workbench.api.typesystem;

import workbench.api.annotation.Annotation;

public class Relation {
	private String relation = null;
	private Annotation subject = null;
	private Annotation modifier = null;

	public Relation(String relation, Annotation subject, Annotation modifier) {
		this.relation = relation;
		this.subject = subject;
		this.modifier = modifier;
	}

	public String getRelation() {
		return relation;
	}

	public Annotation getSubject() {
		return subject;
	}

	public Annotation getModifier() {
		return modifier;
	}
	
}
