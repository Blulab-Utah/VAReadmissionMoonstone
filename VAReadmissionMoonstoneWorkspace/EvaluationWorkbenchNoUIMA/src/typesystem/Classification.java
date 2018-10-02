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

import java.util.Comparator;
import java.util.Vector;

public class Classification extends TypeObject {

	Attribute displayAttribute = null;
	int annotationCount = 0;
	static String[] cuiPropertyNames = { "cui", "code" };

	public Classification(TypeSystem ts, String name, String uima) {
		super(ts, name, uima);
	}

	public Classification(TypeSystem ts, String id, String name, String uima) {
		super(ts, id, name, uima);
	}

	public String toString() {
		return this.getName();
	}

	public String getShortUimaDisplayClassificationName() {
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				if (attribute.isDisplay() || this.attributes.size() == 1) {
					return attribute.getShortUima();
				}
			}
		}
		return null;
	}

	public boolean containsClassificationAttribute(String aname) {
		if (aname != null && this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				if (aname.equalsIgnoreCase(attribute.getShortUima())) {
					return true;
				}
			}
		}
		return false;
	}

	public String getDisplayClassification(String parent) {
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				if ((attribute.isDisplay() || this.attributes.size() == 1)
						&& parent.contentEquals(attribute.getParentTypeObject()
								.getName())) {
					return attribute.getName();
				}
			}
		}
		return null;
	}

	String toLisp(int depth) {
		StringBuffer sb = new StringBuffer();
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				sb.append(attribute.toLisp(depth + 6));
			}
		}
		return sb.toString();
	}

	public Attribute getDisplayAttribute() {
		if (this.displayAttribute == null && this.getAttributes() != null) {
			Attribute first = this.attributes.firstElement();
			this.setDisplayAttribute(first.getName());
		}
		return this.displayAttribute;
	}

	public void setDisplayAttribute(String aname) {
		if (this.getAttributes() != null) {
			for (Attribute attribute : this.getAttributes()) {
				attribute.setIsDisplay(false);
			}
		}
		this.displayAttribute = this.getAttribute(aname);
		if (this.displayAttribute != null) {
			this.displayAttribute.setIsDisplay(true);
		}
	}

	public Attribute getCUIProperty() {
		Vector<Attribute> properties = this.getAttributes();
		if (properties != null) {
			if (properties.size() == 1) {
				return properties.firstElement();
			}
			for (Attribute property : properties) {
				for (int i = 0; i < cuiPropertyNames.length; i++) {
					if (property.getName().toLowerCase()
							.contains(cuiPropertyNames[i])) {
						return property;
					}
				}
			}
		}
		return null;
	}

	public static boolean isCUIComprehensive(String cstr) {
		return (isCUI(cstr) || (cstr != null && cstr.toLowerCase().equals(
				"cui-less")));
	}

	public static boolean isCUI(String cstr) {
		if (cstr != null && cstr.length() > 2
				&& (cstr.charAt(0) == 'c' || cstr.charAt(0) == 'C')
				&& Character.isDigit(cstr.charAt(1))) {
			return true;
		}
		return false;
	}

	public int getAnnotationCount() {
		return annotationCount;
	}
	
	public void incrementAnnotationCount() {
		this.annotationCount++;
	}
	
	// 3/7/2014:
	public void addAttributeOrProperty(String sname) throws Exception {
		TypeSystem ts = this.getTypeSystem();
		Annotation type = (Annotation) this.getParentTypeObject();
		String aname = type.getName() + "$" + sname;
		Attribute attribute = null;
		if (attribute == null) {
			attribute = this.getAttribute(aname);
		}
		if (attribute == null) {
			attribute = type.getAttribute(aname);
		}
		if (attribute == null) {
			attribute = new Attribute(ts, aname, aname, null, null);
			if (ts.isDefaultClassificationProperty(sname)) {
				attribute.setParentTypeObject(this);
				this.addAttribute(attribute);
			} else {
				attribute.setParentTypeObject(type);
				type.addAttribute(attribute);
			}
			TypeSystem.addTypeObjectByID(attribute);
			TypeSystem.addTypeObjectByUima(attribute);
		}
	}
	
	public static class ClassificationSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Classification c1 = (Classification) o1;
			Classification c2 = (Classification) o2;
			String s1 = c1.getName();
			String s2 = c2.getName();
			if (s1 != null && s2 != null) {
				return s1.compareTo(s2);
			}
			return 0;
		}
	}

}
