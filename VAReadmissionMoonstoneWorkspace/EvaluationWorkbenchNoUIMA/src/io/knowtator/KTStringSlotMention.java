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

// TODO: Auto-generated Javadoc
/**
 * The Class KTStringSlotMention.
 */
public class KTStringSlotMention extends KTSlotMention {

	public KTStringSlotMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		extractInformation();
	}

	public KTStringSlotMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
		extractInformation();
	}

	public void extractInformationXMLFormatSHARP() throws Exception {
		Element node = JDomUtils.getElementByName(this.node, "mentionSlot");
		this.mentionSlotID = node.getAttributeValue("id");
		node = JDomUtils.getElementByName(this.node, "stringSlotMentionValue");
		this.stringValue = node.getAttributeValue("value");
	}

	// 12/27/2012
	public void extractInformationLispFormat(Vector v)
			throws Exception {
		this.mentionSlotID = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot", v);
		this.stringValue = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot_value", v);
	}

	/**
	 * Extract information.
	 */
	public void extractInformationXMLFormatOriginal() throws Exception {
		Vector<Element> OSVNodes = JDomUtils.getElementsByName(this.node,
				"own_slot_value");
		if (OSVNodes != null) {
			for (Element osvnode : OSVNodes) {
				Element rnode = JDomUtils.getElementByName(osvnode,
						"slot_reference");
				String rvalue = rnode.getText();
				// String rvalue = rnode.getValue();
				Vector<Element> vnodes = JDomUtils.getElementsByName(osvnode,
						"value");
				String vvalue = vnodes.firstElement().getText();
				// String vvalue = vnodes.firstElement().getValue();
				if ("knowtator_mentioned_in".equals(rvalue)) {
					this.mentionedInID = vvalue;
				} else if ("knowtator_mention_slot".equals(rvalue)) {
					this.mentionSlotID = vvalue;
				} else if ("knowtator_mention_slot_value".equals(rvalue)) {
					this.stringValue = vvalue;
				}
			}
		}
	}

	public void resolveReferences() throws Exception {
		if (this.mentionedInID != null) {
			this.mentionedInInstance = (KTSimpleInstance) this.kt
					.getHashItem(this.mentionedInID);
		}
		if (this.mentionSlotID != null) {
			this.slotMention = (KTSlot) this.kt.getHashItem(this.mentionSlotID);
		}
	}

	public Object getValue() {
		return this.stringValue;
	}

	public String toString() {
		return "<KTStringSlotMention:" + this.name + ">";
	}

}
