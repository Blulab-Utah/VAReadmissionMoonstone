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

import java.util.Comparator;
import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class KTClass extends KTTypeObject {

	KTClass superclass = null;

	String superclassName = null;

	Vector<KTSlot> slots = null;

	Vector<String> slotNames = null;

	Vector<KTClass> subclasses = null;

//	Classification typeModelClass = null;

	Vector<KTClassMention> mentions = null;

	Type type = null;

	static Vector<String> slotLabels = VUtils.arrayToVector(new String[] {
			"single-slot", "multislot" });

	static void extractClasses(Element root, KnowtatorIO kt) throws Exception {
		Vector<Element> nodes = JDomUtils.getElementsByName(root, "class");
		if (nodes != null) {
			for (Element node : nodes) {
				KTClass kc = new KTClass();
				kc.kt = kt;
				Element cnode = JDomUtils.getElementByName(node, "name");
				kc.setName(cnode.getText());
				cnode = JDomUtils.getElementByName(node, "superclass");
				if (cnode != null) {
					kc.setSuperclassName(cnode.getText());
				}
				Vector<Element> snodes = JDomUtils.getElementsByName(node,
						"template_slot");
				if (snodes != null) {
					for (Element snode : snodes) {
						String sname = snode.getText();
						kc.addSlotNames(sname);
					}
				}
				if (!kc.isSystemClass()) {
					kt.classes = VUtils.add(kt.classes, kc);
				}
				if (kc.getName() != null) {
					kt.addHashItem(kc.getName(), kc);
				}
			}
		}
	}

	// 12/27/2012
	static void extractClasses(Vector v, KnowtatorIO kt) throws Exception {
		Vector<Vector> cvs = VUtils.assocAll("defclass", v);
		if (cvs != null) {
			for (Vector cv : cvs) {
				String cname = (String) cv.elementAt(1);
				KTClass kc = new KTClass();
				kc.kt = kt;
				kc.setName(cname);
				if (!kc.isSystemClass()) {
					kt.classes = VUtils.add(kt.classes, kc);
					for (String slabel : slotLabels) {
						Vector<Vector> svs = VUtils.assocAll(slabel, cv);
						if (svs != null) {
							for (Vector sv : svs) {
								String sname = (String) sv.elementAt(1);
								kc.addSlotNames(sname);
								KTSlot slot = new KTSlot(sv, kt);
								slot.setSclassName(cname);
							}
						}
						if (kc.getName() != null) {
							kt.addHashItem(kc.getName(), kc);
						}
					}
				}
			}
		}
	}

	public void resolveReferences() throws Exception {
		if (this.slots == null && this.slotNames != null) {
			for (String name : this.slotNames) {
				KTSlot slot = (KTSlot) this.kt.getHashItem(name);
				if (slot != null) {
					this.slots = VUtils.add(this.slots, slot);
				}
			}
		}
		if (this.superclass == null && this.superclassName != null) {
			this.superclass = (KTClass) this.kt
					.getHashItem(this.superclassName);
			if (this.superclass != null) {
				this.superclass.subclasses = VUtils.add(
						this.superclass.subclasses, this);
			}
		}
	}

	public void addTypeObjectAttributes() throws Exception {
		TypeSystem ts = this.kt.typeSystem;
		Type type = this.type;
		if (ts != null && type != null) {
			if (this.slotNames != null) {
				for (String sname : this.slotNames) {
					Attribute attr = null;
					if (ts.isClassificationName(sname)) {
						attr = new Classification(type, sname);
					} else {
						attr = new Attribute(type, sname);
					}
					type.addAttribute(attr);
					
					// Temporary:  Add this to startup parameters
//					if (sname.contains("assoc")) {
//						attr = new Classification(type, sname);
//					} else {
//						attr = new Attribute(type, sname);
//					}
//					type.addAttribute(attr);
				}
			}

			// 9/18/2014:  Removed temporarily...
//			String cname = type.getClassificationName();
//			Classification pclass = (Classification) ts.getTypeObject(cname);
//			if (pclass != null && this.slotNames != null) {
//				for (String sname : this.slotNames) {
//					pclass.addAttributeOrProperty(sname);
//				}
//			}

			// 9/18/2014:  Removed temporarily...
//			if (pclass != null
//					&& (pclass.getAttributes() == null || pclass
//							.getAttributes().size() == 0)) {
//				pclass.addAttributeOrProperty(type.getName());
//			}
		}
	}

	public void addToTypeSystem() throws Exception {
		TypeSystem ts = this.kt.typeSystem;
		if (ts != null) {
			Type type = ts.getType(this.name);
			if (type == null) {
				Type parent = ts.getRootType();
				type = new Type(ts, parent, this.name, null);
			}
			type.setKtClass(this);
			this.setType(type);
		}
	}

	public boolean isSystemClass() {
		if (this.name.charAt(0) == ':' || this.name.indexOf("knowtator") >= 0
				|| this.name.indexOf("file") >= 0
				|| this.name.indexOf("source") >= 0
				|| this.name.indexOf("coref") >= 0
				|| this.name.indexOf("%") >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * To lisp.
	 * 
	 * @param depth
	 *            the depth
	 * @return the string
	 */
	String toLisp(int depth) {
		StringBuffer sb = new StringBuffer();
		addSpaces(sb, depth);
		sb.append("(\"" + this.name + "\"");
		addSpaces(sb, depth + 2);
		sb.append("(level \"snippet\")");
		addSpaces(sb, depth + 2);
		sb.append("(workbench \"SnippetAnnotation\")\n");
		addSpaces(sb, depth + 2);
		sb.append("(classifications\n");
		addSpaces(sb, depth + 4);
		sb.append("(Class:" + this.name + "\"");
		sb.append(")");
		return sb.toString();
	}

	void addSpaces(StringBuffer sb, int num) {
		sb.append("\n");
		for (int i = 0; i < num; i++) {
			sb.append(' ');
		}
	}

	public String toString() {
		return "<C:" + this.getName() + ">";
	}

	public KTClass getSuperclass() {
		return superclass;
	}

	public String getSuperclassName() {
		return superclassName;
	}

	public void setSuperclassName(String superclassName) {
		this.superclassName = superclassName;
	}

	public Vector<KTSlot> getSlots() {
		return slots;
	}

	public void setSlots(Vector<KTSlot> slots) {
		this.slots = slots;
	}

	public Vector<String> getSlotNames() {
		return slotNames;
	}

	public void addSlotNames(String sname) {
		this.slotNames = VUtils.add(this.slotNames, sname);
	}

	public Vector<KTClass> getSubclasses() {
		return subclasses;
	}

	public Vector<KTClassMention> getMentions() {
		return mentions;
	}

	public void addMention(KTClassMention mention) {
		this.mentions = VUtils.add(this.mentions, mention);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public static class NameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			KTClass s1 = (KTClass) o1;
			KTClass s2 = (KTClass) o2;
			return s1.getName().compareTo(s2.getName());
		}
	}

}
