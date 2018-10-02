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
package io.knowtator;

import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;

public class KTComplexSlotMention extends KTSlotMention {

	// 6/25/2012
	String complexSlotMentionValue = null;
	KTClassMention complexSlotClassMention = null;

	public KTComplexSlotMention(KnowtatorIO kt, String name, Element node) throws Exception {
		super(kt, name, node);
		extractInformation();
	}
	
	// 1/1/2013
	public KTComplexSlotMention(KnowtatorIO kt, String name, Vector v) throws Exception {
		super(kt, name, v);
		extractInformation();
	}
	
	public void extractInformationLispFormat(Vector v) throws Exception {
		this.mentionSlotID = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot", this.ktvector);
		this.complexSlotMentionValue = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot_value", this.ktvector);
		this.mentionedInID = (String) VUtils.assocValueTopLevel(
				"knowtator_mentioned_in", this.ktvector);
	}

	public void extractInformationXMLFormatSHARP() throws Exception {
		Element node = JDomUtils.getElementByName(this.node, "mentionSlot");
		this.mentionSlotID = node.getAttributeValue("id");
		node = JDomUtils.getElementByName(this.node, "complexSlotMentionValue");
		this.complexSlotMentionValue = node.getAttributeValue("value");
	}

	public void resolveReferences() throws Exception {
		Object o = null;
		if (this.mentionedInID != null) {
			this.mentionedInInstance = (KTSimpleInstance) this.kt
					.getHashItem(this.mentionedInID);
		}
		if (this.slotIDs != null) {
			for (String sid : this.slotIDs) {
				KTClassMention cm = (KTClassMention) this.kt.getHashItem(sid);
				if (cm != null) {
					this.slotValues = VUtils.add(this.slotValues, cm);
				}
			}
		}
		if (this.mentionSlotID != null) {
			this.slotMention = (KTSlot) this.kt.getHashItem(this.mentionSlotID);
		}
		if (this.complexSlotMentionValue != null) {
			if (this.complexSlotClassMention != null) {
				int x = 1;
				x = x;
			}
			o = this.kt.getHashItem(this.complexSlotMentionValue);
			if (o != null && !(o instanceof KTClassMention)) {
				System.out.println("\tObject=" + o);
				int x = 1;
				x = x;
			}
			this.complexSlotClassMention = (KTClassMention) o;
		}
	}

	public Object getValue() throws Exception {
		if (this.isVisited()
				|| this.complexSlotClassMention == null
				|| this.complexSlotClassMention.slotMentions == null
				|| this.complexSlotClassMention.slotMentions.size() > 1) {
			return null;
		}
		this.setVisited(true);
		for (KTSlotMention sm : this.complexSlotClassMention.slotMentions) {
			if (!sm.isVisited()) {
				Object value = sm.getValue();
				if (value != null) {
					this.setVisited(false);
					return value;
				}
			}
		}
		this.setVisited(false);
		return null;
	}

	public void extractInformationXMLFormatOriginal() throws Exception {
		Vector<Element> OSVNodes = JDomUtils.getElementsByName(this.node,
				"own_slot_value");
		if (OSVNodes != null) {
			for (Element osvnode : OSVNodes) {
				Element rnode = JDomUtils.getElementByName(osvnode,
						"slot_reference");
				String rvalue = rnode.getText();
				Vector<Element> vnodes = JDomUtils.getElementsByName(osvnode,
						"value");
				String vvalue = vnodes.firstElement().getText();
				if ("knowtator_mentioned_in".equals(rvalue)) {
					this.mentionedInID = vvalue;
				} else if ("knowtator_mention_slot".equals(rvalue)) {
					this.mentionSlotID = vvalue;
				} else if ("knowtator_mention_slot_value".equals(rvalue)) {
					for (Element vnode : vnodes) {
						this.slotIDs = VUtils
								.add(this.slotIDs, vnode.getText());
					}
				}
			}
		}
	}

	public String toString() {
		return "<KTComplexSlotMention:" + this.name + ">";
	}

}
