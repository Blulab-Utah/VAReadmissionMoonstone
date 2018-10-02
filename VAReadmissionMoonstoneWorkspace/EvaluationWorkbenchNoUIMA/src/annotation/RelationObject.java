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
package annotation;

// TODO: Auto-generated Javadoc
/**
 * Objects representing relations, with relation name ("relation") and
 * EVAnnotation relatum ("relatum").
 */
public class RelationObject {

	/** The relation. */
	String relation = null;

	/** The relatum. */
	EVAnnotation relatum = null;

	/**
	 * Instantiates a new relation object.
	 * 
	 * @param relation
	 *            the relation
	 * @param relatum
	 *            the relatum
	 */
	public RelationObject(String relation, EVAnnotation relatum) {
		this.relation = relation;
		this.relatum = relatum;
	}

	/**
	 * Gets the relation.
	 * 
	 * @return the relation
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * Sets the relation.
	 * 
	 * @param relation
	 *            the new relation
	 */
	public void setRelation(String relation) {
		this.relation = relation;
	}

	/**
	 * Gets the relatum.
	 * 
	 * @return the relatum
	 */
	public EVAnnotation getRelatum() {
		return relatum;
	}

	/**
	 * Sets the relatum.
	 * 
	 * @param relatum
	 *            the new relatum
	 */
	public void setRelatum(EVAnnotation relatum) {
		this.relatum = relatum;
	}
	
	public String toString() {
		String str = "<RelationObject:Relation=" + relation + ",Relatum=" + relatum + ">";
		return str;
	}

}
