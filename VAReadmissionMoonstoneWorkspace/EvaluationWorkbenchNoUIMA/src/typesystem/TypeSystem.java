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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.EvaluationWorkbench;

public class TypeSystem {

	/** The type object count. */
	int typeObjectCount = 0;

	/** Map: ID to type object */
	Hashtable<String, TypeObject> typeObjectIDHash = new Hashtable();

	Vector<Annotation> allAnnotationTypes = null;

	/** List of all type objects. */
	Vector<TypeObject> allTypeObjects = null;

	/** The index of the type object in a delineated tree. */
	int rowNumber = 0;

	/** Map: Type objects to Java classes represent type levels; vice versa. */
	Hashtable workbenchAnnotationJavaClassMap = new Hashtable();

	Vector<String> defaultClassificationProperties = null;

	Vector<String> defaultAttributes = null;

	Vector<String> hiddenTypes = null;

	boolean useOnlyTypeModel = true;

	Hashtable<String, String> synonymHash = new Hashtable();

	/** The current type system. */
	public static TypeSystem currentTypeSystem = null;

	/** The type system filename. (Not currently used.) */
	public static String typeSystemFilename = null;

	/** The type system filename; Lisp format. */
	static String typeSystemLispFilename = null;

	/** The default type system filename. */
	static String defaultTypeSystemFilename = typeSystemLispFilename;

	public static void createTypeSystem() {

	}

	public static TypeSystem createTypeSystem(String filename) throws Exception {
		if (currentTypeSystem == null) {
			typeSystemFilename = filename;
			currentTypeSystem = new TypeSystem();
			if (filename != null) {
				currentTypeSystem.readLisp(filename);
			}
		}
		return currentTypeSystem;
	}

	public static TypeSystem getTypeSystem(String filename) throws Exception {
		createTypeSystem(filename);
		return currentTypeSystem;
	}

	public static TypeSystem getTypeSystem() throws Exception {
		return getTypeSystem(typeSystemFilename);
	}

	// 1/16/2013: Given a typesystem with root type, this will add a type and
	// add classification + attributes to that type.
	// NOTE: Need to merge this method with the one following (addToTypeSystem).
	public static Annotation addType(AnnotationAnalysis analysis,
			String typename, String classPropertyName, Vector<String> attrnames)
			throws Exception {
		Annotation typeAnnotation = null;
		TypeSystem ts = analysis.getArrTool().getTypeSystem();
		typeAnnotation = (Annotation) ts.getTypeObject(typename);
		if (typeAnnotation == null) {
			String fname = "annotation.SnippetAnnotation";
			Class c = Class.forName(fname);
			Annotation root = (Annotation) ts.getRoot();
			if (root == null) {
				root = new Annotation(ts, "root", c, null);
			}
			typeAnnotation = new Annotation(ts, typename, c, null);
			typeAnnotation.setParentTypeObject(root);
			root.addComponent(typeAnnotation);
			TypeSystem.addTypeObjectByID(typeAnnotation);
			String cname = typename + "_class";
			Classification pclass = new Classification(ts, cname, null);
			pclass.setId(cname);
			typeAnnotation.setClassification(pclass);
			pclass.setParentTypeObject(typeAnnotation);
			TypeSystem.addTypeObjectByID(pclass);
			// 2/7/2013: Why did I ever need this?
			// String pattrname = typename + "$" + classPropertyName;
			// pclass.addAttribute(pattrname);
			if (attrnames != null) {
				for (String aname : attrnames) {
					String fullname = typename + "$" + aname;
					typeAnnotation.addAttribute(fullname);
				}
			}
		}
		return typeAnnotation;
	}

	// 8/29/2012: For adding a new type / classification
	public Annotation addToTypeSystem(String tname,
			Vector<String> classificationNames) throws Exception {
		Annotation type = null;
		type = (Annotation) this.getTypeObject(tname);
		if (type == null) {
			String fname = "annotation.SnippetAnnotation";
			Class c = Class.forName(fname);
			type = new Annotation(this, tname, tname, c, tname);
			TypeSystem.addTypeObjectByID(type);
			Annotation parent = (Annotation) this.getRoot();
			type.setParentTypeObject(parent);
			parent.addComponent(type);
			String cname = tname + "_class";
			Classification cclass = new Classification(this, cname, cname);
			cclass.setId(cname);
			type.setClassification(cclass);
			cclass.setParentTypeObject(type);
			TypeSystem.addTypeObjectByID(cclass);
			if (classificationNames != null) {
				for (String cattr : classificationNames) {
					type.addClassification(this, type, cattr);
				}
			}
		}
		return type;
	}

	public static TypeSystem extractFromDirectory(String dname)
			throws Exception {
		TypeSystem ts = null;
		Class c = Class.forName("annotation.DocumentAnnotation");
		currentTypeSystem = ts = new TypeSystem();
		Annotation root = new Annotation(ts, "DirectoryTypeSystemRoot", c, null);
		TypeSystem.addTypeObjectByID(root);
		Vector<File> lfiles = FUtils.readFilesFromDirectory(dname);
		if (lfiles != null) {
			for (File f : lfiles) {
				if (f.exists()) {
					ts.addTypeFromFile(f.getName());
				}
			}
		}
		return ts;
	}

	public static void addTypeObjectByID(TypeObject to) throws Exception {
		TypeSystem ts = getTypeSystem();
		if (to instanceof Annotation) {
			Annotation annotation = (Annotation) to;
			ts.allAnnotationTypes = VUtils.addIfNot(ts.allAnnotationTypes,
					annotation);
		}
		String rname = ts.getRegularizedName(to.getName());
		ts.typeObjectIDHash.put(rname, to);
		ts.typeObjectIDHash.put(to.getId(), to);
		ts.typeObjectIDHash.put(to.getId().toLowerCase(), to);
		ts.typeObjectIDHash.put(to.getName(), to);
		ts.typeObjectIDHash.put(to.getName().toLowerCase(), to);
		if (to instanceof Annotation) {
			Annotation annotation = (Annotation) to;
			ts.typeObjectIDHash.put(annotation.javaClass.getSimpleName(), to);
			ts.typeObjectIDHash.put(annotation.javaClass.getSimpleName()
					.toLowerCase(), to);
		}
		if (to.getUima() != null) {
			ts.typeObjectIDHash.put(to.getUima(), to);
			ts.typeObjectIDHash.put(to.getUima().toLowerCase(), to);
		}
	}

	// Before 9/14/2012
	// public static void addTypeObjectByID(TypeObject to) {
	// TypeSystem ts = getTypeSystem();
	// if (to instanceof Annotation) {
	// Annotation annotation = (Annotation) to;
	// ts.allAnnotationTypes = VUtils.addIfNot(ts.allAnnotationTypes,
	// annotation);
	// }
	// ts.typeObjectIDHash.put(to.getId(), to);
	// ts.typeObjectIDHash.put(to.getId().toLowerCase(), to);
	// ts.typeObjectIDHash.put(to.getName(), to);
	// ts.typeObjectIDHash.put(to.getName().toLowerCase(), to);
	// if (to instanceof Annotation) {
	// Annotation annotation = (Annotation) to;
	// ts.typeObjectIDHash.put(annotation.javaClass.getSimpleName(), to);
	// ts.typeObjectIDHash.put(annotation.javaClass.getSimpleName()
	// .toLowerCase(), to);
	// }
	// if (to.getUima() != null) {
	// ts.typeObjectIDHash.put(to.getUima(), to);
	// ts.typeObjectIDHash.put(to.getUima().toLowerCase(), to);
	// }
	// }

	public static void addTypeObjectByUima(TypeObject to) throws Exception {
		if (to.getUima() != null && to.getParentUIMA() != null) {
			TypeSystem ts = getTypeSystem();
			String puima = to.getParentUIMA();
			String uima = to.getShortUima();
			String key = puima + "=" + uima;
			ts.typeObjectIDHash.put(key, to);
			puima = TypeSystem.removeClassSuffix(puima);
			key = puima + "=" + uima;
			ts.typeObjectIDHash.put(key, to);
			ts.typeObjectIDHash.put(key, to);
		}
	}

	public static void removeChildComponent(TypeObject to) {
		to.clearComponentChild();
	}

	void readLisp(String filename) throws Exception {
		JLisp jlisp = JLisp.getJLisp();
		Sexp sexp = (Sexp) jlisp.loadFile(filename);
		Vector v = JLUtils.toVector(sexp, true);
		readLispAnnotation(v, null);
	}

	void readLispAnnotation(Vector v, Annotation parent) throws Exception {
		String name = v.firstElement().toString();
		String uima = (String) VUtils.assocValueTopLevel("uima", v);
		String workbench = (String) VUtils.assocValueTopLevel("workbench", v);
		Class c = null;
		if (workbench != null) {
			String fname = "annotation." + workbench;
			c = Class.forName(fname);
			workbenchAnnotationJavaClassMap.put(c, workbench);
			workbenchAnnotationJavaClassMap.put(workbench, c);
		}
		Annotation annotation = new Annotation(this, name, name, c, uima);
		TypeSystem.addTypeObjectByID(annotation);
		if (parent != null) {
			annotation.setParentTypeObject(parent);
			parent.addComponent(annotation);
		}
		Vector rv = VUtils.assocTopLevel("classifications", v);
		if (rv != null) {
			readLispClassification(rv, annotation);
		}
		rv = VUtils.assocTopLevel("attributes", v);
		if (rv != null) {
			Vector<Vector> attributes = VUtils.rest(rv);
			for (Vector av : attributes) {
				readLispAttribute(av, annotation);
			}
		}
		rv = VUtils.assocTopLevel("components", v);
		if (rv != null) {
			Vector<Vector> components = VUtils.rest(rv);
			for (Vector cv : components) {
				readLispAnnotation(cv, annotation);
			}
		}
		rv = VUtils.assocTopLevel("relations", v);
		if (rv != null) {
			Vector<Vector> relations = VUtils.rest(rv);
			for (Vector cv : relations) {
				String rname = (String) cv.elementAt(0);
				String rid = (String) cv.elementAt(2);
				Object o = this.getUimaAnnotation(rid);
				if (o instanceof TypeObject) {
					TypeObject relatum = (TypeObject) o;
					if (relatum instanceof Classification) {
						relatum = ((Classification) relatum)
								.getParentTypeObject();
					}
					annotation.addRelation(rname, (Annotation) relatum);
				}
			}
		}
		TypeSystem.addTypeObjectByUima(annotation);
	}

	void readLispClassification(Vector<Vector> v, TypeObject parent) throws Exception {
		String uima = (String) VUtils.assocValueTopLevel("uima", v);
		String cname = (parent instanceof Annotation ? ((Annotation) parent)
				.getClassificationName() : parent.getName());
		Classification parentClassification = new Classification(this, cname,
				cname, uima);
		parentClassification.setParentTypeObject(parent);
		parent.setClassification(parentClassification);
		TypeSystem.addTypeObjectByID(parentClassification);
		for (Enumeration e = VUtils.rest(v).elements(); e.hasMoreElements();) {
			Vector av = (Vector) e.nextElement();
			readLispAttribute(av, parentClassification);
		}
		TypeSystem.addTypeObjectByUima(parentClassification);
	}

	void readLispAttribute(Vector v, TypeObject parent) throws Exception {
		String attributeName = v.firstElement().toString();
		String uima = (String) VUtils.assocValueTopLevel("uima", v);
		String uimatype = (String) VUtils.assocValueTopLevel("type", v);
		Attribute attribute = new Attribute(this, attributeName, attributeName,
				uima, uimatype);
		if ("true".equals(VUtils.assocValueTopLevel("display", v))) {
			attribute.isDisplay = true;
		}
		attribute.setParentTypeObject(parent);
		parent.addAttribute(attribute);
		TypeSystem.addTypeObjectByID(attribute);
		TypeSystem.addTypeObjectByUima(attribute);
	}

	void readXML(String filename) throws Exception {
		org.jdom.Document jdoc = new SAXBuilder().build(new File(filename));
		Element root = jdoc.getRootElement();
		List l = root.getChildren("node");
		for (ListIterator li = l.listIterator(); li.hasNext();) {
			Element node = (Element) li.next();
			String id = node.getAttributeValue("id");
			String type = node.getAttributeValue("type");
			String name = node.getAttributeValue("name");
			String uima = node.getAttributeValue("uima");
			TypeObject typeObject = null;
			if ("annotation".equals(type)) {
				String cname = node.getAttributeValue("workbench");
				Class c = null;
				if (cname != null) {
					cname = "annotation." + cname;
					c = Class.forName(cname);
				}
				typeObject = new Annotation(this, id, name, c, uima);
			} else if ("classification".equals(type)) {
				typeObject = new Classification(this, id, name, uima);
			} else if ("attribute".equals(type)) {
				typeObject = new Attribute(this, id, name, uima);
			}
			TypeSystem.addTypeObjectByID(typeObject);
		}
		l = root.getChildren("edge");
		for (ListIterator i = l.listIterator(); i.hasNext();) {
			Element edge = (Element) i.next();
			String fromID = edge.getAttributeValue("from");
			String toID = edge.getAttributeValue("to");
			TypeObject from = getTypeObject(fromID);
			TypeObject to = getTypeObject(toID);
			to.setParentTypeObject(from);
			if (to instanceof Classification) {
				from.setClassification((Classification) to);
			} else if (to instanceof Attribute) {
				from.addAttribute((Attribute) to);
			} else if (to instanceof Annotation) {
				from.addComponent((Annotation) to);
			}
			TypeSystem.addTypeObjectByUima(to);
		}
		assignRowNumbers();
	}

	void assignRowNumbers() {
		rowNumber = 0;
		getRoot().assignRowNumber();
	}

	public Vector<TypeObject> getAllTypeObjects() {
		return new Vector(typeObjectIDHash.entrySet());
	}

	public Annotation getUimaAnnotation(String name) {
		Object o = this.getTypeObjectIDHash(name);
		if (o != null && o instanceof Annotation) {
			return (Annotation) o;
		}
		return null;
	}

	public Attribute getUimaAttribute(String aname, String atname) {
		String key = aname + "=" + atname;
		return getUimaAttribute(key);
	}

	public Attribute getUimaAttribute(String key) {
		TypeObject to = this.getTypeObjectIDHash(key);
		if (to == null) {
			to = typeObjectIDHash.get(key.toLowerCase());
		}
		if (to instanceof Attribute) {
			return (Attribute) to;
		}
		return null;
	}

	public Classification getUimaClassification(String aname, String cname) {
		String key = aname + "=" + cname;
		Classification c = getUimaClassification(key);
		return c;
	}

	// Before 10/16/2012
	public Classification getUimaClassification(String key) {
		TypeObject to = this.getTypeObjectIDHash(key);
		if (to == null) {
			to = this.getTypeObjectIDHash(key.toLowerCase());
		}
		if (to instanceof Attribute && to.getParentTypeObject() != null
				&& to.getParentTypeObject() instanceof Classification) {
			return (Classification) to.getParentTypeObject();
		}
		if (to instanceof Classification) {
			return (Classification) to;
		}
		if (key.endsWith("_class")) {
			int index = key.indexOf("_class");
			String substr = key.substring(0, index);
			to = this.getTypeObjectIDHash(substr);
			if (to instanceof Annotation) {
				Annotation annotation = (Annotation) to;
				return annotation.getFirstClassification();
			}
		}
		return null;

	}

	// public Classification getUimaClassification(String key) {
	// TypeObject to = this.getTypeObjectIDHash(key);
	// if (to == null) {
	// to = this.getTypeObjectIDHash(key.toLowerCase());
	// }
	// if (to instanceof Attribute && to.getParentTypeObject() != null
	// && to.getParentTypeObject() instanceof Classification) {
	// return (Classification) to.getParentTypeObject();
	// }
	// if (to instanceof Classification) {
	// return (Classification) to;
	// }
	//
	// // 9/13/2012
	// int index = key.indexOf("_class");
	// if (index > 0) {
	// String substr = key.substring(0, index);
	// to = this.getTypeObjectIDHash(substr);
	// if (to instanceof Annotation) {
	// Annotation annotation = (Annotation) to;
	// return annotation.getFirstClassification();
	// }
	// }
	//
	// return null;
	//
	// }

	public TypeObject getRoot() {
		if (this.allAnnotationTypes != null) {
			return this.allAnnotationTypes.firstElement();
		}
		return null;
	}

	public void addDefaultRoot() throws Exception {
		if (this.getRoot() == null) {
			Class c = Class.forName("annotation.DocumentAnnotation");
			Annotation root = new Annotation(this, "ROOT", c, "ROOT");
			TypeSystem.addTypeObjectByID(root);
		}
	}

	public Annotation addTypeFromFile(String filename) throws Exception {
		Annotation type = null;
		String tname = getTypeNameFromFileName(filename);
		type = (Annotation) this.getTypeObject(tname);
		if (type == null) {
			Annotation root = (Annotation) this.getRoot();
			type = new Annotation(this, tname,
					Class.forName("annotation.SnippetAnnotation"), null);
			type.setId(tname);
			root.addComponent(type);
			type.setParentTypeObject(root);
			TypeSystem.addTypeObjectByID(type);
			String cname = tname + "_class";
			Classification cclass = new Classification(this, cname, null);
			Attribute attribute = new Attribute(this, cname + ":ID", tname,
					null);
			cclass.addAttribute(attribute);
			cclass.setDisplayAttribute(attribute.getName());
			cclass.setId(cname);
			type.setClassification(cclass);
			cclass.setParentTypeObject(type);
			TypeSystem.addTypeObjectByID(cclass);
		}
		return type;
	}

	public String getTypeNameFromFileName(String fname) {
		int index = fname.indexOf('.');
		String str = fname;
		if (index > 0) {
			str = fname.substring(0, index);
		}
		return str;
	}

	public TypeObject getTypeObject(String id) {
		TypeObject to = this.getTypeObjectIDHash(id);
		return to;
	}

	public int getTypeObjectCount() {
		return typeObjectCount;
	}

	public Class mapAnnotationTypeToJavaClass(String annotationType) {
		return (Class) workbenchAnnotationJavaClassMap.get(annotationType);
	}

	public String mapJavaClassToAnnotationType(Class c) {
		return (String) workbenchAnnotationJavaClassMap.get(c);
	}

	public Vector<Annotation> getAllAnnotationTypes() {
		return this.allAnnotationTypes;
	}

	public TypeObject getTypeObjectIDHash(String key) {
		key = getRegularizedName(key);
		TypeObject to = typeObjectIDHash.get(key);
		return to;
	}

	public String getDelimitedDefaultClassificationPropertyString() {
		if (this.defaultClassificationProperties != null) {
			return StrUtils.stringListConcat(
					this.defaultClassificationProperties, ",");
		}
		return null;
	}

	public Vector<String> getDefaultClassificationProperties() {
		return defaultClassificationProperties;
	}

	public void setDefaultClassificationProperties(
			Vector<String> defaultClassificationProperties) {
		this.defaultClassificationProperties = defaultClassificationProperties;
	}

	public boolean isDefaultClassificationProperty(String pname) {
		EvaluationWorkbench workbench = EvaluationWorkbench
				.getEvaluationWorkbench();
		if (workbench.getStartupParameters()
				.isPermitAllDefaultClassicationNames()
				|| (this.defaultClassificationProperties != null && this.defaultClassificationProperties
						.contains(pname))) {
			return true;
		}
		return false;
	}

	public void setDefaultAttributes(Vector<String> defaultAttributes) {
		this.defaultAttributes = defaultAttributes;
	}

	public boolean isDefaultAttribute(String aname) {
		return this.defaultAttributes != null
				&& this.defaultAttributes.contains(aname);
	}

	public boolean isUseOnlyTypeModel() {
		return useOnlyTypeModel;
	}

	public void setUseOnlyTypeModel(boolean useOnlyTypeModel) {
		this.useOnlyTypeModel = useOnlyTypeModel;
	}

	public Vector<String> getHiddenTypes() {
		return hiddenTypes;
	}

	public void setHiddenTypes(Vector<String> hiddenTypes) {
		this.hiddenTypes = hiddenTypes;
	}

	public boolean typeIsHidden(String tname) {
		return (this.hiddenTypes != null && (this.hiddenTypes.contains(tname) || this.hiddenTypes
				.contains(tname.toLowerCase())));
	}

	public void readSynonymsFromFile(String filename) throws Exception {
		FileInputStream fstream = new FileInputStream(filename);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.length() > 0 && line.charAt(0) != '#') {
				Vector<String> words = StrUtils.stringList(line, ',');
				if (words != null && words.size() > 1) {
					String key = words.firstElement();
					for (Object o : VUtils.rest(words)) {
						String synonym = (String) o;
						this.synonymHash.put(synonym, key);
					}
				}
			}
		}
	}

	public String getRegularizedName(String str) {
		String result = null;

		if (str == null) {
			return null;
		}
		EvaluationWorkbench workbench = EvaluationWorkbench
				.getEvaluationWorkbench();
		if (workbench != null
				&& workbench.getStartupParameters() != null
				&& workbench.getStartupParameters()
						.permitEquivalentClassificationNames()) {
			result = workbench.getAnalysis().getEquivalentClassificationName(
					str);
			if (result != null) {
				return result;
			}
		}
		result = this.synonymHash.get(str);
		if (result != null) {
			return result;
		}
		TypeObject to = typeObjectIDHash.get(str);
		if (to != null) {
			return to.getName();
		}
		if (str.endsWith("_class")) {
			int index = str.indexOf("_class");
			String substr = str.substring(0, index);
			result = this.synonymHash.get(substr);
			if (result != null) {
				String newkey = result + "_class";
				this.synonymHash.put(str, newkey);
				return newkey;
			}
			to = typeObjectIDHash.get(substr);
			if (to instanceof Annotation) {
				Annotation annotation = (Annotation) to;
				Classification c = annotation.getFirstClassification();
				if (c != null) {
					this.synonymHash.put(str, c.getName());
					return c.getName();
				}
			}
		}

		// 3/8/2013: Knowtator wantonly replaces "+" in slotnames with " "
		// in xml export files...
		String newstr = "";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			newstr += (c == '+' ? " " : c);
		}
		if (!str.equals(newstr)) {
			str = newstr;
		}

		return str;
	}

	// 10/17/2012
	public static String removeClassSuffix(String name) {
		String str = name;
		if (name != null) {
			if (str.endsWith("_class")) {
				int index = str.indexOf("_class");
				str = str.substring(0, index);
			}
		}
		return str;
	}

}
