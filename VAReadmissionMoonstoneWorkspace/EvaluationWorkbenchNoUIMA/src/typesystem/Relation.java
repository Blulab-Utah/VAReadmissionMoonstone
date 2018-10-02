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
package typesystem;

// TODO: Auto-generated Javadoc
/**
 * The Class Relation.
 */
public class Relation extends TypeObject {

	/** The subject. */
	TypeObject subject = null;
	
	/** The modifier. */
	TypeObject modifier = null;

	/**
	 * Instantiates a new relation.
	 *
	 * @param ts the ts
	 * @param name the name
	 * @param subject the subject
	 * @param modifier the modifier
	 */
	public Relation(TypeSystem ts, String name, TypeObject subject,
			TypeObject modifier) {
		super(ts, name);
		this.subject = subject;
		this.modifier = modifier;
	}

}
