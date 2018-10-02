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

import io.knowtator.KTClass;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.VUtils;
import annotation.EVAnnotation;

public class Annotation extends TypeObject {

	TypeObject annotationType = null;

	/** The java class. */
	Class javaClass = null;

	/** The constructor. */
	Constructor constructor = null;

	/** The classifications. */
	// Vector<Classification> classifications = null;

	/** The attributes. */
	Vector<Attribute> attributes = null;

	/** The components. */
	// Vector<Annotation> components = null;

	/** The relation hash. */
	Hashtable<String, Vector<Annotation>> relationHash = new Hashtable();

	KTClass ktClass = null;

	/** The class annotation hash. */
	static Hashtable<Class, Annotation> classAnnotationHash = new Hashtable();

	static Hashtable levelHash = new Hashtable();

	static Vector<Annotation> allAnnotationTypes = null;

	public Annotation(TypeSystem ts, String name, Class c, String uima)
			throws Exception {
		super(ts, name, uima);
		this.javaClass = c;
		if (c != null) {
			Class constructorClass = Class.forName(c.getName());
			this.constructor = constructorClass.getConstructor();
			classAnnotationHash.put(c, this);
		}
		initialize();
	}

	public Annotation(TypeSystem ts, String id, String name, Class c,
			String uima) throws Exception {
		super(ts, id, name, uima);
		this.javaClass = c;
		if (c != null) {
			Class constructorClass = Class.forName(c.getName());
			this.constructor = constructorClass.getConstructor();
			classAnnotationHash.put(c, this);
		}
		initialize();
	}

	private void initialize() {
		levelHash.put(this.getName(), this);
		levelHash.put(this.getJavaClass(), this);
		levelHash.put(this.getJavaClass().getSimpleName(), this);
		allAnnotationTypes = VUtils.addIfNot(allAnnotationTypes, this);
	}

	public EVAnnotation createEVAnnotation(TypeObject type) throws Exception {
		EVAnnotation ev = null;
		ev = (EVAnnotation) this.constructor.newInstance();
		ev.setType(type);
		return ev;
	}

	public static Annotation getAnnotationByClass(Class c) {
		return (Annotation) levelHash.get(c);
	}

	public Class getAnnotationClass() {
		return javaClass;
	}

	public void setAnnotationClass(Class c) {
		this.javaClass = c;
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
		// 3/3/2014
		// Collections.sort(this.classifications, new
		// Classification.ClassificationSorter());
	}

	public Classification getClassification(String cname) {
		if (this.classifications != null) {
			for (Classification c : this.classifications) {
				if (cname.equals(c.getName())) {
					return c;
				}
			}
		}
		return null;
	}

	public Vector<Attribute> getAttributes() {
		return attributes;
	}

	public Vector<String> getAttributeStrings() {
		Vector<String> strings = null;
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				strings = VUtils.add(strings, attribute.getName());
			}
			// Before 10/27/2012
			// Collections.sort(strings);
		}
		return strings;
	}

	public int getNumberOfAttributes() {
		if (this.attributes != null) {
			return this.attributes.size();
		}
		return 0;
	}

	public void setAttributes(Vector<Attribute> attributes) {
		this.attributes = attributes;
	}

	// public Vector<Annotation> getComponents() {
	// return components;
	// }
	//
	// public void addComponent(Annotation component) {
	// this.components = VUtils.add(this.components, component);
	// }
	//
	// public boolean hasComponents() {
	// return this.components != null;
	// }

	public void addClassification(TypeSystem ts, Annotation type, String cname) throws Exception {
		if (this.getClassification(cname) == null) {
			Classification cclass = new Classification(ts, cname, cname);
			cclass.setId(cname);
			type.setClassification(cclass);
			cclass.setParentTypeObject(type);
			TypeSystem.addTypeObjectByID(cclass);
		}
	}

	public void addAttribute(Attribute attribute) {
		if (getAttribute(attribute.getName()) == null) {
			this.attributes = VUtils.add(this.attributes, attribute);

			// 3/3/2014
			Collections.sort(this.attributes, new TypeObject.NameSorter());
		}
	}

	public Object getValue(String name) {
		return VUtils.findIfMatchingField(attributes, "name", name);
	}

	public Attribute getAttribute(String aname) {
		if (this.attributes != null) {
			for (Attribute attribute : this.attributes) {
				if (aname.equalsIgnoreCase(attribute.getName())
						|| (attribute.getShortUima() != null && aname
								.equalsIgnoreCase(attribute.getShortUima()))
						|| (attribute.getUima() != null && aname
								.equalsIgnoreCase(attribute.getUima()))) {
					return attribute;
				}
			}
		}
		return null;
	}

	public Vector<Annotation> getRelata(String rname) {
		return this.relationHash.get(rname);
	}

	public void addRelation(String rname, Annotation relatum) {
		VUtils.pushIfNotHashVector(this.relationHash, rname, relatum);
	}

	public Vector<String> getRelations() {
		return new Vector(this.relationHash.keySet());
	}

	public Vector<TypeObject> getChildren() {
		Vector<TypeObject> children = null;
		children = VUtils.append(children, this.getClassifications());
		children = VUtils.append(children, this.getAttributes());
		children = VUtils.append(children, this.getComponents());
		return children;
	}

	public String toLisp(int depth) {
		StringBuffer sb = new StringBuffer();
		addSpaces(sb, depth);
		sb.append("(\"" + this.name + "\"");
		addSpaces(sb, depth + 2);
		sb.append("(level \"" + this.javaClass.getSimpleName() + "\")");
		addSpaces(sb, depth + 2);
		// String jclass = UIMATypeSystemJTree.levelToWorkbenchMap
		// .get(this.javaClass.getSimpleName());
		String jclass = "*";
		sb.append("(workbench \"" + jclass + "\")");
		if (this.classifications != null) {
			addSpaces(sb, depth + 2);
			sb.append("(classifications ");
			for (Classification c : this.classifications) {
				sb.append(c.toLisp(depth + 6));
			}
			sb.append(")");
		}
		if (this.attributes != null) {
			addSpaces(sb, depth + 2);
			sb.append("(attributes ");
			for (Attribute a : this.attributes) {
				sb.append(a.toLisp(depth + 6));
			}
			sb.append(")");
		}
		if (this.components != null) {
			addSpaces(sb, depth + 2);
			sb.append("(components ");
			for (Annotation a : this.components) {
				sb.append(a.toLisp(depth + 6));
			}
			sb.append(")");
		}
		sb.append(")");
		return sb.toString();
	}

	public boolean equals(Object o) {
		if (o instanceof Annotation) {
			Annotation annotation = (Annotation) o;
			if (this.name == annotation.name
					&& this.javaClass == annotation.javaClass) {
				return true;
			}
		}
		return false;
	}

	public Class getJavaClass() {
		return this.javaClass;
	}

	public TypeObject getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(TypeObject annotationType) {
		this.annotationType = annotationType;
	}

	public static Vector<Annotation> getAllAnnotationTypes() {
		return allAnnotationTypes;
	}

	public String getClassificationName() {
		return this.getName() + "_class";
	}

	public static String getClassificationName(TypeSystem ts,
			String annotationName) {
		if (annotationName != null) {
			String str = ts.getRegularizedName(annotationName);
			return str + "_class";
		}
		return "*";
	}

	// 9/18/2014
	public static String getClassificationName(workbench.api.typesystem.TypeSystem ts,
			String annotationName) {
		if (annotationName != null) {
			return annotationName + "_class";
		}
		return "*";
	}

	public boolean isRoot() {
		return getClassifications() == null && getParentTypeObject() == null
				&& getAttributes() == null;
	}

	public KTClass getKtClass() {
		return ktClass;
	}

	public void setKtClass(KTClass ktClass) {
		this.ktClass = ktClass;
	}

}
