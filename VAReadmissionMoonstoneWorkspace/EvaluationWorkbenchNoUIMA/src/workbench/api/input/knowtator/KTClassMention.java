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
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class KTClassMention extends KTSimpleInstance {

	public Vector<String> slotMentionIDs = null;
	public Vector<KTSlotMention> slotMentions = null;
	public String mentionClassID = null;
	public KTClass mentionClass = null;
	public String annotationID = null;
	public Vector<KTRelation> relations = null;
	public Vector<KTAttribute> attributes = null;
	public KTAnnotation annotation = null;
	public boolean isRelation = false;
	public String mentionClassValue = null;

	// 10/7/2015: For Moonstone
	public KTClassMention(String ehostid, String mcid, String mcv,
			KTAnnotation kta) {
		this.id = ehostid;
		this.name = ehostid;
		this.mentionClassID = mcid;
		this.annotation = kta;
		this.mentionClassValue = mcv;
		kta.annotatedMention = this;
	}

	public KTClassMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		kt.classMentions = VUtils.add(kt.classMentions, this);
		extractInformation();
	}

	public KTClassMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
		kt.classMentions = VUtils.add(kt.classMentions, this);
		extractInformationLispFormat(v);
	}

	public void extractInformationXMLFormatSHARP() throws Exception {
		Element node = JDomUtils.getElementByName(this.node, "mentionClass");
		this.mentionClassID = node.getAttributeValue("id");
		Vector<Element> nodes = JDomUtils.getElementsByName(this.node,
				"hasSlotMention");
		if (nodes != null) {
			for (Element n : nodes) {
				String sid = n.getAttributeValue("id");
				this.slotMentionIDs = VUtils.add(this.slotMentionIDs, sid);
			}
		}
	}

	// 12/27/2012
	public void extractInformationLispFormat(Vector v) throws Exception {
		this.mentionClassID = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_class", v);
		this.slotMentionIDs = VUtils.rest(VUtils.assocTopLevel(
				"knowtator_slot_mention", v));
	}

	public void extractInformationXMLFormatOriginal() throws Exception {
		Vector<Element> OSVNodes = JDomUtils.getElementsByName(this.node,
				"own_slot_value");
		if (OSVNodes != null) {
			for (Element osvnode : OSVNodes) {
				Element rnode = JDomUtils.getElementByName(osvnode,
						"slot_reference");
				String rvalue = rnode.getText();
				Vector<Element> children = JDomUtils.getElementsByName(osvnode,
						"value");
				String firstValue = children.firstElement().getText();
				if ("knowtator_slot_mention".equals(rvalue)) {
					for (Element child : children) {
						String sid = child.getText();
						this.slotMentionIDs = VUtils.add(this.slotMentionIDs,
								sid);
					}
				} else if ("knowtator_mention_class".equals(rvalue)) {
					this.mentionClassID = firstValue;
				} else if ("knowtator_mention_annotation".equals(rvalue)) {
					this.annotationID = firstValue;
				}
			}
		}
	}

	public void resolveReferences() throws Exception {
		if (this.slotMentionIDs != null) {
			for (String sid : this.slotMentionIDs) {
				Object o = this.kt.getHashItem(sid);
				if (o instanceof KTSlotMention) {
					KTSlotMention sm = (KTSlotMention) this.kt.getHashItem(sid);
					if (sm != null) {
						this.slotMentions = VUtils.add(this.slotMentions, sm);
						if (sm.mentionSlotID.toLowerCase().contains("neg")) {
							int x = 1;
							x = x;
						}
					}
				} else {
					// System.out.println("KTClassMention Wrong Class; Sid=" +
					// sid + ",Object="
					// + o);
				}
			}
		}
		if (this.mentionClassID != null) {
			KTClass ktc = (KTClass) this.kt.getHashItem(this.mentionClassID);
			this.mentionClass = ktc;
		}
		if (this.annotationID != null) {
			this.annotation = (KTAnnotation) this.kt
					.getHashItem(this.annotationID);
		}
	}

	public void addRelations() throws Exception {
		TypeSystem ts = this.kt.getTypeSystem();
		if (ts != null && this.mentionClass != null
		// 4/2/2013: Changed from size > 2 to size >= 2; not filtering out
		// relations now.
				&& this.slotMentions != null && this.slotMentions.size() >= 2) {
			KTComplexSlotMention slot1 = null;
			KTComplexSlotMention slot2 = null;
			KTAnnotation annotation1 = null;
			KTAnnotation annotation2 = null;
			Vector<KTAnnotation> annotations = null;
			KTClass relclass = null;
			for (KTSlotMention sm : this.slotMentions) {
				if (sm instanceof KTComplexSlotMention) {
					KTComplexSlotMention csm = (KTComplexSlotMention) sm;
					if (csm.complexSlotClassMention != null
							&& csm.complexSlotClassMention.getAnnotation() != null) {
						KTAnnotation annotation = csm.complexSlotClassMention
								.getAnnotation();
						if (annotation.getTextLength() > 0) {
							if (slot1 == null) {
								slot1 = csm;
								annotation1 = annotation;
							} else if (slot2 == null && csm != slot1) {
								slot2 = csm;
								annotation2 = annotation;
							}
						} else {
							relclass = csm.complexSlotClassMention.mentionClass;
						}
						annotations = VUtils.add(annotations, annotation);
					}
				}
			}
			if (slot1 != null && slot2 != null) {
				// Before 12/19/2014
				// Annotation type = ts.getUimaAnnotation(this.mentionClassID);
				// if (type != null || !ts.isUseOnlyTypeModel()) {

				Type type = ts.getType(this.mentionClassID);
				if (type != null) {
					this.setRelation(true);
					KTClass c1 = slot1.complexSlotClassMention.mentionClass;
					KTClass c2 = slot2.complexSlotClassMention.mentionClass;
					if (c1 != null && c2 != null) {
						c1.addToTypeSystem();
						c2.addToTypeSystem();
						if (relclass == null) {
							relclass = this.mentionClass;
						}
						new KTRelation(relclass, annotation1, annotation2);
					}
				}
			}
		}
	}

	public void addClassifications() throws Exception {
		// 9/14/2014
		// TypeSystem ts = this.kt.getTypeSystem();
		// if (ts != null && this.getMentionClass() != null
		// && this.mentionClass.getTypeObject() != null
		// && this.getSlotMentions() != null) {
		// if (!ts.isUseOnlyTypeModel()) {
		// this.mentionClass.addToTypeSystem();
		// }
		// Annotation type = (Annotation) this.mentionClass.getTypeObject();
		// String pclassName = type.getName() + "_class";
		// Classification pclass = (Classification) ts
		// .getTypeObject(pclassName);
		// for (KTSlotMention sm : this.getSlotMentions()) {
		// if (sm instanceof KTStringSlotMention) {
		// KTStringSlotMention ssm = (KTStringSlotMention) sm;
		// String aname = type.getId() + "$" + ssm.mentionSlotID;
		// Attribute attribute = null;
		// if (ssm.mentionSlotID != null
		// && ts.isDefaultClassificationProperty(ssm.mentionSlotID)
		// && pclass != null
		// && pclass.getAttribute(aname) == null) {
		// attribute = new Attribute(ts, aname, aname, null, null);
		// attribute.setParentTypeObject(pclass);
		// pclass.addAttribute(attribute);
		// TypeSystem.addTypeObjectByID(attribute);
		// TypeSystem.addTypeObjectByUima(attribute);
		// }
		// }
		// } else if (sm instanceof KTBooleanSlotMention) {
		// KTBooleanSlotMention bsm = (KTBooleanSlotMention) sm;
		// String aname = type.getId() + "$" + bsm.mentionSlotID;
		// Attribute attribute = null;
		// if (bsm.mentionSlotID != null
		// && ts.isDefaultClassificationProperty(bsm.mentionSlotID)
		// && pclass != null
		// && pclass.getAttribute(aname) == null) {
		// attribute = new Attribute(ts, aname, aname, null, null);
		// attribute.setParentTypeObject(pclass);
		// pclass.addAttribute(attribute);
		// TypeSystem.addTypeObjectByID(attribute);
		// TypeSystem.addTypeObjectByUima(attribute);
		// }
		// }
		// }
	}

	public String getValue() throws Exception {
		TypeSystem ts = this.kt.getTypeSystem();
		if (ts != null && this.getMentionClass() != null
				&& this.mentionClass.getType() != null
				&& this.getSlotMentions() != null) {

		}
		return null;
	}

	public String toString() {
		return "<KTClassMention:" + this.name + "(\""
				+ this.annotation.getText() + "\">";
	}

	public String getClassName() {
		return this.mentionClass.name;
	}

	public Vector<KTRelation> getRelations() {
		return relations;
	}

	public Vector<KTAttribute> getAttributes() {
		return attributes;
	}

	public Vector<KTSlotMention> getSlotMentions() {
		return slotMentions;
	}

	public Vector<String> getSlotMentionIDs() {
		return slotMentionIDs;
	}

	public void setSlotMentionIDs(Vector<String> slotMentionIDs) {
		this.slotMentionIDs = slotMentionIDs;
	}

	public KTClass getMentionClass() {
		return mentionClass;
	}

	public KTAnnotation getAnnotation() {
		return annotation;
	}

	public boolean isRelation() {
		return isRelation;
	}

	public void setRelation(boolean isRelation) {
		this.isRelation = isRelation;
	}

	public String getMentionClassID() {
		return mentionClassID;
	}

	public void setMentionClassID(String mentionClassID) {
		this.mentionClassID = mentionClassID;
	}

	public String toXML() {
		String xml = "    <classMention id=\"" + this.name + "\">\n";
		xml += "        <mentionClass id=\"" + this.mentionClassID + "\">"
				+ this.mentionClassValue + "</mentionClass>\n";
		if (this.slotMentionIDs != null) {
			for (String smid : this.slotMentionIDs) {
				xml += "        <hasSlotMention id=\"" + smid + "\" />\n";
			}
		}
		xml += "    </classMention>";
		return xml;
	}

}
