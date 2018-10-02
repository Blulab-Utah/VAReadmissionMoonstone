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

import io.GrAF;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import annotation.AnnotationCollection;
import annotation.EVAnnotation;
import annotation.SnippetAnnotation;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import typesystem.Annotation;
import typesystem.Attribute;
import typesystem.Classification;
import typesystem.TypeObject;
import typesystem.TypeSystem;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.Annotator;
import workbench.arr.EvaluationWorkbench;

public class KnowtatorIO {

	TypeSystem typeSystem = null;
	public Vector<KTSimpleInstance> simpleInstances = null;
	public Vector<KTClassMention> classMentions = null;
	Vector<KTAnnotation> annotations = null;
	Vector<SnippetAnnotation> snippets = null;
	Hashtable<Vector<String>, Vector<KTAnnotation>> annotationHash = new Hashtable();
	Hashtable<String, KTAnnotator> annotatorHash = new Hashtable();
	Hashtable<String, Object> IDHash = new Hashtable();
	Hashtable annotatorSourceXMLHash = new Hashtable();
	public Vector<KTClass> classes = null;
	public Vector<KTSlot> slots = null;
	Vector<String> textSources = null;
	String textSource = null;
	Vector<String> annotators = null;
	String xml = null;
	String outputDirectory = "/Users/leechristensen/Desktop/LeeNLPFolder/Results KT";
	String xmlFormat = SHARPXMLFormat;
	public Vector<AnnotationCollection> annotationCollections = null;
	Annotator annotator = null;
	EvaluationWorkbench arrTool = null;
	Vector<String> selectedAnnotationSets = null;
	Vector<String> selectedAnnotators = null;
	public int numSnippets = 0;
	
	public static String OriginalXMLFormat = "original";
	public static String SHARPXMLFormat = "SHARP";
	public static String LispFormat = "LISP";
	
	public KnowtatorIO() {
		
	}

	public KnowtatorIO(EvaluationWorkbench arrTool, TypeSystem typesystem,
			String xmlFormat, Annotator annotator) {
		TLisp.getTLisp(true);
		this.annotator = annotator;
		this.arrTool = arrTool;
		this.typeSystem = typesystem;
		this.xmlFormat = xmlFormat;
	}

	public static KnowtatorIO create(String schemaFileName,
			String annotationDirectoryName, TypeSystem typeSystem,
			String xmlFormat) throws Exception {
		TLisp.getTLisp();
		KnowtatorIO kt = null;
		File file = new File(schemaFileName);
		if (file.exists()) {
			kt = new KnowtatorIO(null, typeSystem, xmlFormat, null);
			kt.extractSchema(schemaFileName);
			if (annotationDirectoryName != null) {
				File directory = new File(annotationDirectoryName);
				if (directory.exists() && directory.isDirectory()) {
					File[] files = directory.listFiles();
					for (int i = 0; i < files.length; i++) {
						if (files[i].getName().indexOf(".xml") > 0) {
							File xfile = files[i];
							kt.xmlFormat = KnowtatorIO.SHARPXMLFormat;
							kt.extractAnnotations(xfile.getAbsolutePath());
						}
					}
				}
			}
			kt.resolveReferences();
			kt.createWorkbenchAnnotations(null);
		}
		return kt;
	}

	public void extractSchema(String filename) throws Exception {
		if (filename != null) {
			if (filename.endsWith(".xml")) {
				this.xmlFormat = KnowtatorIO.SHARPXMLFormat;
				org.jdom.Document jdoc = new SAXBuilder().build(filename);
				Element root = jdoc.getRootElement();
				KTClass.extractClasses(root, this);
				KTSlot.extractSlots(root, this);
			} else if (filename.endsWith(".pont")) {
				this.xmlFormat = KnowtatorIO.LispFormat;
				TLisp tl = TLisp.getTLisp();
				Sexp sexp = (Sexp) tl.loadFile(filename, true);
				Vector ontv = TLUtils.convertSexpToJVector(sexp);
				KTClass.extractClasses(ontv, this);
			}
		}
	}
	
	// 9/15/2014:  For use with WEW
	public void extractSchema(String fstr, boolean isXML) throws Exception {
		if (isXML) {
			this.xmlFormat = KnowtatorIO.SHARPXMLFormat;
			InputSource is = new InputSource(new ByteArrayInputStream(
					fstr.getBytes("utf-8")));
			org.jdom.Document jdoc = new SAXBuilder().build(is);
			Element root = jdoc.getRootElement();
			KTClass.extractClasses(root, this);
			KTSlot.extractSlots(root, this);
		} else {
			this.xmlFormat = KnowtatorIO.LispFormat;
			TLisp tl = TLisp.getTLisp();
			Sexp sexp = (Sexp) tl.evalString(fstr);
			Vector ontv = TLUtils.convertSexpToJVector(sexp);
			KTClass.extractClasses(ontv, this);
		}
	}

	void extractLispAnnotations(String filename) throws Exception {
		TLisp tl = TLisp.getTLisp();
		Sexp sexp = (Sexp) tl.loadFile(filename, true);
		Vector instv = TLUtils.convertSexpToJVector(sexp);
		KTSimpleInstance.extractSimpleInstancesLisp(instv, this, filename);
	}

	void extractAnnotations(String filename) throws Exception {
		org.jdom.Document jdoc = new SAXBuilder().build(filename);
		Element root = jdoc.getRootElement();
		KTSimpleInstance.extractSimpleInstances(filename, root, this);
	}
	
	public void extractAnnotationsFromXMLString(String fstr) throws Exception {
		InputSource is = new InputSource(new ByteArrayInputStream(
				fstr.getBytes("utf-8")));
		org.jdom.Document jdoc = new SAXBuilder().build(is);
		Element root = jdoc.getRootElement();
		KTSimpleInstance.extractSimpleInstances(null, root, this);
	}

	public void extractAnnotationsFromDirectory(String fname) throws Exception {
		File f = new File(fname);
		if (f.exists()) {
			if (f.isDirectory()) {
				Vector<File> files = FUtils.readFilesFromDirectory(fname);
				if (files != null) {
					for (File file : files) {
						if (file.getName().indexOf(".xml") > 0) {
							this.xmlFormat = KnowtatorIO.SHARPXMLFormat;
							this.extractAnnotations(file.getAbsolutePath());
							this.resolveReferences();
							this.clearSimpleInstances();
						} else if (file.getName().indexOf(".pins") > 0) {
							extractAnnotationsFromPinsFile(file
									.getAbsolutePath());
						}
					}
				}
			} else if (fname.indexOf(".pins") > 0) {
				extractAnnotationsFromPinsFile(fname);
			}
		}
	}

	public void extractAnnotationsFromPinsFile(String fname) throws Exception {
		this.xmlFormat = KnowtatorIO.LispFormat;
		TLisp tl = TLisp.getTLisp();
		Sexp sexp = (Sexp) tl.loadFile(fname, true);
		Vector instv = TLUtils.convertSexpToJVector(sexp);
		KTSimpleInstance.extractSimpleInstancesLisp(instv, this, fname);
		this.resolveReferences();
		this.clearSimpleInstances();
	}
	
	// 9/15/2014
	public void extractAnnotationsFromPinsFileString(String str) throws Exception {
		this.xmlFormat = KnowtatorIO.LispFormat;
		TLisp tl = TLisp.getTLisp();
		Sexp sexp = (Sexp) tl.evalString(str);
		Vector instv = TLUtils.convertSexpToJVector(sexp);
		KTSimpleInstance.extractSimpleInstancesLisp(instv, this, "*");
		this.resolveReferences();
		this.clearSimpleInstances();
	}
	
	

	void extractConfigurationInfo(Vector v) {
		String id = (String) VUtils.assocValueTopLevel(
				"knowtator_selected_annotation_set", v);
		this.selectedAnnotationSets = VUtils.addIfNot(
				this.selectedAnnotationSets, id);
		id = (String) VUtils.assocValueTopLevel("knowtator_selected_annotator",
				v);
		this.selectedAnnotators = VUtils.addIfNot(this.selectedAnnotators, id);
	}

	public void resolveReferences() throws Exception {
		if (this.classes != null) {
			for (KTClass ktclass : this.classes) {
				ktclass.resolveReferences();
			}
			if (this.typeSystem != null) {
				for (KTClass c : this.classes) {
					String className = typesystem.Annotation
							.getClassificationName(this.typeSystem, c.getName());
					typesystem.Classification pc = this.getTypeSystem()
							.getUimaClassification(className);
					if (pc != null) {
						c.addToTypeSystem();
					}
				}
			}
		}
		if (this.slots != null) {
			for (KTSlot slot : this.slots) {
				slot.resolveReferences();
			}
		}
		if (this.simpleInstances != null) {
			for (KTSimpleInstance si : this.simpleInstances) {
				si.resolveReferences();
			}
		}
		if (this.classMentions != null && this.classes != null) {
			for (KTClassMention mention : this.classMentions) {
				if (mention.mentionClass != null) {
					mention.mentionClass.mentions = VUtils.add(
							mention.mentionClass.mentions, mention);
				}
			}
			for (KTClassMention mention : this.classMentions) {
				mention.addRelations();
			}
			for (KTClass c : this.classes) {
				if (c.typeObject == null && this.getTypeSystem() != null) {
					if (c.getSlotNames() != null) {
						for (String name : c.getSlotNames()) {
							if (this.getTypeSystem()
									.isDefaultClassificationProperty(name)) {
								c.addToTypeSystem();
								break;
							}
						}
					}
				}
				c.addTypeObjectAttributes();
			}
		}
	}

	public void createWorkbenchAnnotations(AnnotationAnalysis analysis)
			throws Exception {
		if (this.annotations != null) {
			for (KTAnnotation annotation : this.annotations) {
				if (annotation.annotatorID != null) {
					KTAnnotator annotator = this.annotatorHash
							.get(annotation.annotatorID);
					if (annotator == null) {
						annotator = new KTAnnotator(this,
								annotation.getAnnotatorName(),
								annotation.getAnnotatorID());
					}
				}
			}
			for (Enumeration<Vector<String>> e = this.annotationHash.keys(); e
					.hasMoreElements();) {
				Vector<String> key = e.nextElement();
				Vector<KTAnnotation> annotations = this.annotationHash.get(key);
				String source = key.firstElement();
				String annotatorID = key.lastElement();
				if (annotatorID != null) {
					KTAnnotator annotator = this.annotatorHash.get(annotatorID);
					AnnotationCollection ac = new AnnotationCollection();
					ac.setAnalysis(analysis);
					ac.setAnnotatorID(annotator.getId(), annotator.getName());
					ac.setSourceTextName(source);
					Hashtable<String, EVAnnotation> spanhash = new Hashtable();
					for (KTAnnotation annotation : annotations) {
						EVAnnotation snippet = annotation.extractSnippet(ac,
								spanhash);
					}
					extractRelations();
					ac.storeAnalysisIndices();
					this.annotationCollections = VUtils.add(
							this.annotationCollections, ac);
					ac.getAnalysis().appendKnowtatorAnnotations(
							ac.getAnnotations());
				}
			}
		}
	}

	void extractRelations() throws Exception {
		if (this.getSnippets() != null) {
			for (SnippetAnnotation snippet : this.getSnippets()) {
				if (snippet.getKtAnnotation().getRelations() != null) {
					snippet.getAnnotationCollection().setHasRelations(true);
					for (KTRelation ktr : snippet.getKtAnnotation()
							.getRelations()) {
						if (ktr.firstArgument.getSnippet() != null
								&& ktr.secondArgument.getSnippet() != null) {
							SnippetAnnotation s1 = ktr.firstArgument
									.getSnippet();
							SnippetAnnotation s2 = ktr.secondArgument
									.getSnippet();
							String rname = ktr.relation.getName();
							s1.setRelation(rname, s2);
							String irname = rname + "_inverse";
							s2.setRelation(irname, s1);
						}
					}
				}
			}
		}
	}

	public void writeGrAF(int id) throws Exception {
		this.outputDirectory = "/Users/leechristensen/Desktop/LeeNLPFolder/Results KT";
		for (Enumeration<Vector<String>> e = this.annotatorSourceXMLHash.keys(); e
				.hasMoreElements();) {
			Vector<String> key = e.nextElement();
			String source = key.firstElement();
			String annotatorID = key.lastElement();
			KTAnnotator annotator = this.annotatorHash.get(annotatorID);
			String xml = (String) this.annotatorSourceXMLHash.get(key);
			String filename = getOutputFileName(source,
					annotator.getFullName(), id);
			FUtils.writeFile(filename, xml);
		}
	}

	String getOutputFileName(String sourceText, String annotator, int id)
			throws Exception {
		int index = sourceText.indexOf('.');
		String shortname = new String(sourceText);
		if (index >= 0) {
			shortname = sourceText.substring(0, index);
		}
		String filename = this.outputDirectory + File.separatorChar + shortname
				+ "_" + annotator + "_" + id + ".xml";
		return filename;
	}

	String getGRafXML(AnnotationCollection ac, String textSource,
			String annotatorName) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<graph source_file=\""
				+ textSource
				+ "\" "
				+ "annotator_name=\"" + annotatorName + "\">\n");
		if (ac.getAnnotations() != null) {
			for (EVAnnotation annotation : ac.getAnnotations()) {
				SnippetAnnotation snippet = (SnippetAnnotation) annotation;
				String xml = GrAF.toXML(snippet);
				sb.append(xml);
			}
		}
		sb.append("</graph>\n");
		return sb.toString();
	}

	public TypeSystem extractTypeSystem() throws Exception {
		TypeSystem ts = null;
		ts = TypeSystem.getTypeSystem(null);
		Class c = Class.forName("annotation.DocumentAnnotation");
		Annotation root = new Annotation(ts, "KnowtatorRoot", c, null);
		TypeSystem.addTypeObjectByID(root);
		if (this.classes != null) {
			for (KTClass ktclass : this.classes) {
				String annName = TypeObject.getAnnotationName(true,
						ktclass.getName());
				Annotation childAnnotation = new Annotation(ts, annName,
						Class.forName("annotation.SnippetAnnotation"), null);
				root.addComponent(childAnnotation);
				childAnnotation.setParentTypeObject(root);
				TypeSystem.addTypeObjectByID(childAnnotation);
				String className = TypeObject.getClassName(true,
						ktclass.getName());
				String classID = ktclass.getName() + ":ID";
				Classification childClassification = new Classification(ts,
						className, null);
				childAnnotation.setClassification(childClassification);
				TypeSystem.addTypeObjectByID(childClassification);
				String attrName = TypeObject.getAttributeName(true,
						ktclass.getName());
				Attribute attribute = new Attribute(ts, classID, attrName, null);
				childClassification.addAttribute(attribute);
				if (ktclass.getSlots() != null) {
					for (KTSlot slot : ktclass.getSlots()) {
						// ... not using <<name>> format for slots...
						String sname = slot.getName();
						String sid = sname + ":ID";
						attribute = new Attribute(ts, sid, sname, null);
						childClassification.addAttribute(attribute);
						TypeSystem.addTypeObjectByID(childClassification);
					}
				}
			}
		}
		return ts;
	}

	void addHashItem(String name, Object item) throws Exception {
		String key = name;

		// System.out.println("addHashItem: Adding=" + item);

		Object o = this.IDHash.get(key);
		if (o != null) {
			// System.out.println("KnowtatorIO:addHashItem:  Duplicate key=" +
			// key
			// + ",Item=" + item);
		}
		this.IDHash.put(key, item);
	}

	Object getHashItem(String name) throws Exception {
		Object value = null;
		if (name != null) {
			value = this.IDHash.get(name);
		}
		if (value == null) {
			value = this.IDHash.get(name.toLowerCase());
		}
		if (value == null) {
			value = this.IDHash.get(name.toUpperCase());
		}
		return value;
	}

	public void clearSimpleInstances() {
		this.simpleInstances = null;
		this.classMentions = null;
		// this.annotations = null;
		// this.snippets = null;
		// this.annotationHash.clear();

		for (Enumeration<String> e = IDHash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			Object value = IDHash.get(key);
			if (value instanceof KTSimpleInstance) {
				// System.out.println("clearSimpleInstances: Removing=" +
				// value);
				IDHash.remove(key);
			}
		}

		for (Enumeration<String> e = IDHash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			Object value = IDHash.get(key);
			// System.out.println("clearSimpleInstances: Remaining=" + value);
		}

	}

	public Vector<KTAnnotation> getAnnotations(String source, String annotator)
			throws Exception {
		Vector<String> key = new Vector(0);
		key.add(source);
		key.add(annotator);
		return annotationHash.get(key);
	}

	public Vector<KTSimpleInstance> getSimpleInstances() {
		return simpleInstances;
	}

	public Vector<KTClass> getClasses() {
		return classes;
	}

	public Vector<String> getTextSources() {
		return textSources;
	}

	public Vector<String> getAnnotators() {
		return annotators;
	}

	public String getFirstAnnotator() {
		if (this.annotators != null) {
			return this.annotators.firstElement();
		}
		return null;
	}

	public Hashtable<Vector<String>, String> getAnnotatorSourceXMLHash() {
		return annotatorSourceXMLHash;
	}

	public String getXml() {
		return xml;
	}

	public Vector<String> getAllXmls(String annotator) {
		Vector<String> xmls = (Vector<String>) this.annotatorSourceXMLHash
				.get(annotator);
		return xmls;
	}

	public boolean isOriginalXMLFormat() {
		return OriginalXMLFormat.equals(this.xmlFormat);
	}

	public boolean isSHARPXMLFormat() {
		return SHARPXMLFormat.equals(this.xmlFormat);
	}

	public boolean isLispFormat() {
		return LispFormat.equals(this.xmlFormat);
	}

	public String getXMLFormat() {
		return this.xmlFormat;
	}

	public void setXMLFormat(String xmlFormat) {
		this.xmlFormat = xmlFormat;
	}

	public TypeSystem getTypeSystem() {
		return this.typeSystem;
	}

	public Vector<SnippetAnnotation> getSnippets() {
		return snippets;
	}

	public Vector<AnnotationCollection> getAnnotationCollections() {
		return annotationCollections;
	}

	public Vector<KTAnnotation> getAnnotations() {
		return annotations;
	}

	public Vector<String> getSelectedAnnotationSets() {
		return selectedAnnotationSets;
	}

	public Vector<String> getSelectedAnnotators() {
		return selectedAnnotators;
	}

	public Hashtable<String, KTAnnotator> getAnnotatorHash() {
		return annotatorHash;
	}

	public Hashtable<Vector<String>, Vector<KTAnnotation>> getAnnotationHash() {
		return annotationHash;
	}
	
	

}
