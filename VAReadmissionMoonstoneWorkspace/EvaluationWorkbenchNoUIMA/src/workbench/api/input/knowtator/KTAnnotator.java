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
package workbench.api.input.knowtator;

import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class KTAnnotator.
 */
public class KTAnnotator extends KTSimpleInstance {

	/** The first name. */
	String firstName = null;

	/** The last name. */
	String lastName = null;

	/** The full name. */
	String fullName = null;

	/** The affiliation. */
	String affiliation = null;

	/** The annotator id. */
	String annotatorID = null;

	public KTAnnotator(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		extractInformation();
	}

	public KTAnnotator(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
		extractInformation(v);
	}

	public KTAnnotator(KnowtatorIO kt, String name, String id) throws Exception {
		super(kt, name);
		this.id = id;
		this.kt.annotatorHash.put(this.name, this);
		this.kt.annotatorHash.put(this.id, this);
	}

	public void extractInformation() throws Exception {
		Vector<Element> OSVNodes = JDomUtils.getElementsByName(this.node,
				"own_slot_value");
		if (OSVNodes != null) {
			for (Element osvnode : OSVNodes) {
				Element cnode = JDomUtils.getElementByName(osvnode,
						"slot_reference");
				String rvalue = cnode.getText();
				cnode = JDomUtils.getElementByName(osvnode, "value");
				String vvalue = cnode.getText();
				if ("knowtator_annotation_annotator_lastname".equals(rvalue)) {
					this.lastName = vvalue;
				} else if ("knowtator_annotation_annotator_firstname"
						.equals(rvalue)) {
					this.firstName = vvalue;
				} else if ("knowtator_annotation_annotator_affiliation"
						.equals(rvalue)) {
					this.affiliation = vvalue;
				} else if ("knowtator_annotator_id".equals(rvalue)) {
					this.annotatorID = vvalue;
				}
			}
			this.fullName = this.firstName;
			if (this.lastName != null) {
				this.fullName += "_" + this.lastName;
			}
			this.kt.annotatorHash.put(this.fullName, this);
			if (this.annotatorID != null) {
				this.kt.annotatorHash.put(this.annotatorID, this);
			}
			this.kt.annotatorHash.put(this.name, this);
		}
	}

	public void extractInformation(Vector v) throws Exception {
		String teamname = (String) VUtils.assocValueTopLevel(
				"knowtator_annotator_team_name", v);
		if (teamname != null) {
			this.fullName = teamname;
		}
		this.firstName = (String) VUtils.assocValueTopLevel(
				"knowtator_annotation_annotator_firstname", v);
		this.affiliation = (String) VUtils.assocValueTopLevel(
				"knowtator_annotation_annotator_affiliation", v);
		if (this.fullName == null && this.firstName != null) {
			this.fullName = this.firstName;
			if (this.lastName != null) {
				this.fullName += "_" + this.lastName;
			}
		}
		this.name = this.fullName;
		this.annotatorID = this.id = v.firstElement().toString();
		this.kt.annotatorHash.put(this.fullName, this);
		if (this.annotatorID != null) {
			this.kt.annotatorHash.put(this.annotatorID, this);
		}
		this.kt.annotatorHash.put(this.name, this);
	}
	
	// Before 2/19/2013
//	public void extractInformation(Vector v) throws Exception {
//		this.firstName = (String) VUtils.assocValueTopLevel(
//				"knowtator_annotation_annotator_firstname", v);
//		this.affiliation = (String) VUtils.assocValueTopLevel(
//				"knowtator_annotation_annotator_affiliation", v);
//		this.fullName = this.firstName;
//		if (this.lastName != null) {
//			this.fullName += "_" + this.lastName;
//		}
//		this.name = this.fullName;
//		this.annotatorID = this.id = v.firstElement().toString();
//		this.kt.annotatorHash.put(this.fullName, this);
//		if (this.annotatorID != null) {
//			this.kt.annotatorHash.put(this.annotatorID, this);
//		}
//		this.kt.annotatorHash.put(this.name, this);
//	}

	public String getFullName() {
		if (this.fullName == null) {
			this.fullName = this.name;
		}
		return this.fullName;
	}

}
