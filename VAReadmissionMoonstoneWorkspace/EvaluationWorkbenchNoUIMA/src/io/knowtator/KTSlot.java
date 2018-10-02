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

public class KTSlot extends KTTypeObject {

	KTClass sclass = null;

	String sclassName = null;

	String type = null;
	Vector<String> allowedValues = null;
	String defaultValue = null;
	Vector<Integer> cardinality = null;
	boolean allowMultiples = false;

	public KTSlot() {
	}

	// 1/3/2012
	public KTSlot(Vector v, KnowtatorIO kt) throws Exception {
		this.kt = kt;
		kt.slots = VUtils.add(kt.slots, this);
		this.name = v.elementAt(1).toString();
		this.type = (String) VUtils.assocValueTopLevel("type", v);
		this.allowedValues = VUtils.rest(VUtils.assoc("allowed-values", v));
		this.defaultValue = (String) VUtils.assocValueTopLevel("default", v);
		String str = v.elementAt(0).toString();
		this.allowMultiples = str.contains("mult");
		if (this.getName() != null) {
			kt.addHashItem(this.getName(), this);
		}

	}

	public static void extractSlots(Element root, KnowtatorIO kt)
			throws Exception {
		Vector<Element> nodes = JDomUtils.getElementsByName(root, "slot");
		if (nodes != null) {
			for (Element node : nodes) {
				KTSlot slot = new KTSlot();
				slot.kt = kt;
				kt.slots = VUtils.add(kt.slots, slot);
				Element cnode = JDomUtils.getElementByName(node, "name");
				String sname = cnode.getText().trim();
				slot.setName(sname);
				Vector<Element> snodes = JDomUtils.getElementsByName(node,
						"own_slot_value");
				if (snodes != null) {
					for (Element snode : snodes) {
						Vector<Element> vnodes = JDomUtils.getElementsByName(
								snode, "value");
						if (vnodes != null) {
							for (Element vnode : vnodes) {
								String vtype = vnode.getAttribute("value_type")
										.getValue();
								String value = vnode.getText().trim();
								if ("class".equals(vtype)) {
									slot.setSclassName(value);
								}
							}
						}
					}
				}
				if (slot.getName() != null) {
					kt.addHashItem(slot.getName(), slot);
				}
			}
		}
	}

	public void resolveReferences() throws Exception {
		String cname = this.sclassName;
		if (cname != null) {
			if (cname.charAt(0) == ':') {
				cname = cname.substring(1);
			}
			this.sclass = (KTClass) this.kt.getHashItem(cname);
		}
	}

	public String toString() {
		return "<KTSlot:" + this.getName() + ">";
	}

	public KTClass getSclass() {
		return sclass;
	}

	public void setSclass(KTClass sclass) {
		this.sclass = sclass;
	}

	public String getSclassName() {
		return sclassName;
	}

	public void setSclassName(String sclassName) {
		this.sclassName = sclassName;
	}

}
