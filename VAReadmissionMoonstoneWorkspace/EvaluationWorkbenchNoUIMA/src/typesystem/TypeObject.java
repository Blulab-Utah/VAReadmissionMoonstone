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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import tsl.utilities.VUtils;

public abstract class TypeObject {
	TypeSystem typeSystem = null;

	/** The java type, denoting the type model level (e.g. Snippet, Document...) */
	String javaType = null;

	/** The id. */
	String id = null;

	/** The name. */
	String name = null;

	/** The UIMA name. */
	String uima = null;

	/** The shortened UIMA name. */
	String shortUima = null;

	/** The UIMA type. */
	String uimaType = null;

	/** The parent type object. */
	TypeObject parenTypeObject = null;

	/** The classifications. */
	Vector<Classification> classifications = null;

	/** The attributes. */
	Vector<Attribute> attributes = null;

	/** The components. */
	Vector<Annotation> components = null;
	
	Vector<String> synonyms = null;

	/** The row. */
	int row = 0;

	public TypeObject() {
		this.javaType = this.getClass().getSimpleName();
	}

	public TypeObject(TypeSystem ts, String name) {
		this.typeSystem = ts;
		this.name = name;
	}

	public TypeObject(TypeSystem ts, String name, String uima) {
		typeSystem = ts;
		this.id = this.getClass().getSimpleName() + "_"
				+ typeSystem.typeObjectCount++;
		this.name = name;
		this.uima = uima;
		this.shortUima = getShortUimaName(uima);
		this.javaType = this.getClass().getSimpleName();
	}

	public TypeObject(TypeSystem ts, String id, String name, String uima) {
		this(ts, id, name, uima, null);
	}

	public TypeObject(TypeSystem ts, String id, String name, String uima,
			String uimaType) {
		typeSystem = ts;
		this.id = id;
		this.name = name;
		this.uima = uima;
		this.shortUima = getShortUimaName(uima);
		this.javaType = this.getClass().getSimpleName();
		this.uimaType = uimaType;
	}

	public void clearComponentChild() {
		if (this.components != null) {
			components.clear();
		}
	}

	public Object getValue(String name) {
		return VUtils.findIfMatchingField(attributes, "name", name);
	}

	void assignRowNumber() {
		this.row = typeSystem.rowNumber++;
		Vector<TypeObject> children = getChildren();
		if (children != null) {
			for (TypeObject child : children) {
				child.assignRowNumber();
			}
		}
	}

	public int getRowNumber() {
		return this.row;
	}

	public String toString() {
		return "<[" + this.getClass().getSimpleName() + "],Id=" + this.getId()
				+ ",Name=" + this.getName() + ",UIMA=" + this.getUima()
				+ ",Type=" + this.getClass().getSimpleName() + ">";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vector<Classification> getClassifications() {
		return classifications;
	}

	public Classification getFirstClassification() {
		if (classifications != null) {
			return classifications.firstElement();
		}
		return null;
	}

	public void setClassification(Classification classification) {
		this.classifications = VUtils.add(this.classifications, classification);
	}

	public Vector<Attribute> getAttributes() {
		return attributes;
	}
	
	public Vector<String> getAttributeStrings() {
		Vector<String> astrs = null;
		if (this.attributes != null) {
			for (Attribute attr : this.attributes) {
				astrs = VUtils.add(astrs, attr.getName());
			}
		}
		// 3/3/2014
		Collections.sort(astrs);
		return astrs;
	}

	public void setAttributes(Vector<Attribute> attributes) {
		this.attributes = attributes;
	}

	public Vector<Annotation> getComponents() {
		return components;
	}

	public void addComponent(Annotation component) {
		this.components = VUtils.add(this.components, component);
	}

	public boolean hasComponents() {
		return this.components != null;
	}

	public void addAttribute(Attribute attribute) {
		this.attributes = VUtils.add(this.attributes, attribute);
	}

	public TypeObject getParentTypeObject() {
		return parenTypeObject;
	}

	public Attribute getAttribute(String aname) {
		aname = this.getTypeSystem().getRegularizedName(aname);
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				if (aname.equalsIgnoreCase(attribute.getName())) {
					return attribute;
				}
			}
		}
		return null;
	}

	public String getUimaType() {
		return this.uimaType;
	}

	public void setParentTypeObject(TypeObject parent) {
		this.parenTypeObject = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector<TypeObject> getChildren() {
		Vector<TypeObject> children = null;
		children = VUtils.append(children, this.getClassifications());
		children = VUtils.append(children, this.getAttributes());
		children = VUtils.append(children, this.getComponents());
		return children;
	}

	public String getUima() {
		return uima;
	}

	public boolean isChildOf(TypeObject c) {
		return (this.getParentTypeObject() != null && this
				.getParentTypeObject().equals(c));
	}

	public Class getAnnotationClass() {
		if (this instanceof Annotation) {
			return ((Annotation) this).getAnnotationClass();
		}
		return null;
	}

	public String getParentUIMA() {
		if (this.getParentTypeObject() != null) {
			if (this.getParentTypeObject().getUima() != null) {
				return this.getParentTypeObject().getUima();
			}
			return this.getParentTypeObject().getParentUIMA();
		}
		return null;
	}

	String getShortUima() {
		return this.shortUima;
	}
	static char[] UimaDelimiters = new char[] {':', '.', '$'};
	static String getShortUimaName(String uname) {
		if (uname != null) {
			for (int i = 0; i < UimaDelimiters.length; i++) {
				char delim = UimaDelimiters[i];
				int cindex = uname.lastIndexOf(delim);
				if (cindex > 0) {
					return uname.substring(cindex+1);
				}
			}
		}
		return uname;
	}


//	static String getShortUimaName(String umlsName) {
//		String str = null;
//		if (umlsName != null) {
//			int cindex = umlsName.lastIndexOf(':');
//			int pindex = umlsName.lastIndexOf('.');
//			if (cindex > 0) {
//				str = umlsName.substring(cindex + 1);
//			} else if (pindex > 0) {
//				str = umlsName.substring(pindex + 1);
//			}
//		}
//		return str;
//	}

	String toLisp(int depth) {
		return "";
	}

	void addSpaces(StringBuffer sb, int num) {
		sb.append("\n");
		for (int i = 0; i < num; i++) {
			sb.append(' ');
		}
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public static String getAnnotationName(boolean isKnowtator, String name) {
		return getObjectName(isKnowtator, name, "", "");
	}

	public static String getClassName(boolean isKnowtator, String name) {
		return getObjectName(isKnowtator, name, "[", "]");
	}

	public static String getAttributeName(boolean isKnowtator, String name) {
		return getObjectName(isKnowtator, name, "[[", "]]");
	}

	public static String getObjectName(boolean isKnowtator, String name,
			String startDelim, String endDelim) {
		return name;
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public void setTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public static class NameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			TypeObject to1 = (TypeObject) o1;
			TypeObject to2 = (TypeObject) o2;
			return to1.getName().toLowerCase()
					.compareTo(to2.getName().toLowerCase());
		}
	}
	
	public void addAttribute(String aname) throws Exception {
		if (this.getAttribute(aname) == null) {
			Attribute attribute = new Attribute(this.getTypeSystem(), aname, aname,
					aname, null);
			attribute.setParentTypeObject(this);
			this.addAttribute(attribute);
			TypeSystem.addTypeObjectByID(attribute);
			TypeSystem.addTypeObjectByUima(attribute);
		}
	}

	public Vector<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(Vector<String> synonyms) {
		this.synonyms = synonyms;
	}


}
