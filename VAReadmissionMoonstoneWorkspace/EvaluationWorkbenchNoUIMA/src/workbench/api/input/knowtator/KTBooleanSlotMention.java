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


public class KTBooleanSlotMention extends KTSlotMention {
	
	private boolean booleanValue = false;

	public KTBooleanSlotMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		extractInformation();
	}

	public KTBooleanSlotMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
		extractInformation();
	}

	public void extractInformationXMLFormatSHARP() throws Exception {
		Element node = JDomUtils.getElementByName(this.node, "mentionSlot");
		this.mentionSlotID = node.getAttributeValue("id");
		node = JDomUtils.getElementByName(this.node, "booleanSlotMentionValue");
		this.stringValue = node.getAttributeValue("value");
		this.booleanValue = Boolean.valueOf(this.stringValue);
	}

	// 12/27/2012
	public void extractInformationLispFormat(Vector v)
			throws Exception {
		this.mentionSlotID = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot", v);
		this.stringValue = (String) VUtils.assocValueTopLevel(
				"knowtator_mention_slot_value", v);
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

	public String getValue() {
		return this.stringValue;
	}

	public String toString() {
		return "<KTStringSlotMention:" + this.name + ">";
	}

	public boolean isBooleanValue() {
		return booleanValue;
	}

}

