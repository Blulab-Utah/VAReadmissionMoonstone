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

// TODO: Auto-generated Javadoc
/**
 * The Class KTSlotMention.
 */
public class KTSlotMention extends KTSimpleInstance {

	/** The knowtator mentioned in id. */
	String mentionedInID = null;

	/** The knowtator mentioned in instance. */
	KTSimpleInstance mentionedInInstance = null;

	/** The slot mention. */
	KTSlot slotMention = null;

	/** The knowtator mention slot i ds. */
	Vector<String> slotIDs = null;

	/** The knowtator mention slot values. */
	Vector<KTClassMention> slotValues = null;

	/** The mention slot id. */
	String mentionSlotID = null;

	String stringValue = null;

	public KTSlotMention(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
	}

	public KTSlotMention(KnowtatorIO kt, String name, Vector v)
			throws Exception {
		super(kt, name, v);
	}

	public Object getValue() throws Exception {
		return null;
	}

}
