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
package io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import typesystem.TypeSystem;
import workbench.arr.Annotator;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;
import annotation.Span;

// TODO: Auto-generated Javadoc
/**
 * Code related to formatting/reading annotation information to/from GrAF
 * format.
 */
public class GrAF {

	/**
	 * Maps classification names to annotations (used in generating annotations
	 * from
	 */
	Hashtable<String, Vector<EVAnnotation>> classificationAnnotationHash = new Hashtable();

	/** The annotation collection. */
	AnnotationCollection annotationCollection = new AnnotationCollection();

	/** The UIMA jcas. */
	// JCas jcas = null;

	/** The type system. */
	TypeSystem typeSystem = null;

	/** List of all annotations. */
	Vector<EVAnnotation> allAnnotations = null;

	/** The annotator (human or otherwise). */
	String annotator = null;

	/** Name of GrAF file. */
	String fileName = null;

	/** Text, if file is not used **/
	String text = null;

	/** The output directory containing GrAF file. */
	String outputDirectory = null;

	static Vector<String> IrrelevantUIMAFeatureNames = VUtils
			.arrayToVector(new String[] { "begin", "end", "sofa", "language" });

	// public GrAF(String filename, String annotator, String outputDirectory) {
	// this.fileName = filename;
	// this.annotator = annotator;
	// this.outputDirectory = outputDirectory;
	// }

	/**
	 * Instantiates a new GrAF object.
	 * 
	 * @param jcas
	 *            the jcas
	 * @param typeSystem
	 *            the type system
	 * @param filename
	 *            the filename
	 * @param annotator
	 *            the annotator
	 * @param outputDirectory
	 *            the output directory
	 */

	public void addAnnotations(AnnotationCollection ac) {
		if (this.allAnnotations != null) {
			for (EVAnnotation annotation : this.allAnnotations) {
				ac.addAnnotation(annotation.getId(), annotation);
			}
		}
	}

	public void writeGrAFAnnotations(Vector<EVAnnotation> allAnnotations,
			String fileName, String annotator) {
		this.allAnnotations = allAnnotations;
		this.fileName = fileName;
		this.annotator = annotator;
		writeGrAFAnnotations();
	}

	public void writeGrAFAnnotations() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<graph source_file=\""
				+ this.fileName
				+ "\" "
				+ "annotator_name=\"" + annotator + "\">\n");
		if (this.annotationCollection != null) {
			this.annotationCollection.annotations = this.allAnnotations;
		}
		if (this.allAnnotations != null) {
			Collections.sort(this.allAnnotations,
					new EVAnnotation.ClassificationSorter());
			for (EVAnnotation annotation : this.allAnnotations) {
				String str = GrAF.toXML(annotation);
				sb.append(str);
			}
		}
		sb.append("</graph>\n");
		String shortName = this.fileName;
		int index = shortName.lastIndexOf(".");
		if (index > 0) {
			shortName = shortName.substring(0, index);
		}
		String fname = this.outputDirectory + File.separatorChar
				+ this.annotator + File.separatorChar + shortName + ".xml";
		FUtils.writeFile(fname, sb.toString());
	}

	public void writeGrAFText(String outputDir) {
		if (this.fileName != null) {
			String fname = null;
			if (outputDir != null) {
				fname = this.outputDirectory + File.separatorChar
						+ this.fileName;
			} else {
				fname = this.outputDirectory + File.separatorChar
						+ this.annotator + File.separatorChar + this.fileName;
			}
			String text = null;
			if (this.getText() != null) {
				text = this.getText();
			}
			if (text != null) {
				FUtils.writeFile(fname, text);
			}
		}
	}

	/**
	 * Read GrAF file; populate AnnotationCollection with workbench annotations.
	 * 
	 * @param ac
	 *            the AnnotationCollection to populate.
	 */
	public static void readXML(AnnotationCollection ac, String xml,
			Annotator annotator) throws Exception {
		Hashtable<String, Span> hash = new Hashtable();
		org.jdom.Document jdoc = null;
		if (xml != null) {
			InputSource is = new InputSource(new ByteArrayInputStream(
					xml.getBytes("utf-8")));
			jdoc = new SAXBuilder().build(is);
		} else {
			String fname = ac.getXmlFileName();
			jdoc = new SAXBuilder().build(fname);
		}
		Element root = jdoc.getRootElement();
		String fname = root.getAttributeValue("source_file");
		ac.setDocument(ac.getAnalysis().getDocument(fname, annotator));
		ac.setAnnotator(annotator);
		List l = root.getChildren("node");
		String annotatorType = root.getAttributeValue("attribute_type");
		if (annotatorType != null) {
			ac.setAnnotatorType(annotatorType);
		}
		for (ListIterator i = l.listIterator(); i.hasNext();) {
			Element node = (Element) i.next();
			readNode(ac, node);
		}
		l = root.getChildren("sink");
		for (ListIterator i = l.listIterator(); i.hasNext();) {
			Element sink = (Element) i.next();
			String id = sink.getAttribute("id").getValue();
			int start = sink.getAttribute("start").getIntValue();
			int end = sink.getAttribute("end").getIntValue();
			Span span = new Span(start, end);
			hash.put(id, span);
		}
		l = root.getChildren("edge");
		for (ListIterator i = l.listIterator(); i.hasNext();) {
			Element edge = (Element) i.next();
			String fromID = edge.getAttributeValue("from");
			String toID = edge.getAttributeValue("to");
			String relationName = edge.getAttributeValue("relation_name");
			EVAnnotation fromAnnotation = ac.getAnnotation(fromID);
			if (fromAnnotation != null) {
				EVAnnotation toAnnotation = ac.getAnnotation(toID);
				Classification toClassification = ac.getClassification(toID);
				Span span = hash.get(toID);
				if (relationName != null && toAnnotation != null) {
					fromAnnotation.setRelation(relationName, toAnnotation);
					if (EVAnnotation.isComponentRelation(relationName)) {
						fromAnnotation.addComponent(toAnnotation);
						toAnnotation.setRelation("parent", fromAnnotation);
					}
					toAnnotation.setRelation("parent", fromAnnotation);
				} else if (toAnnotation != null) {
					fromAnnotation.setRelation("component", toAnnotation);
					fromAnnotation.addComponent(toAnnotation);
					toAnnotation.setRelation("parent", fromAnnotation);
				} else if (toClassification != null) {
					fromAnnotation.setClassification(toClassification);
					toClassification.setAnnotation(fromAnnotation);
				} else if (span != null) {
					fromAnnotation.addSpan(span);
				}
			}
		}
		ac.storeAnalysisIndices();
	}

	/**
	 * Read GrAF node from XML; create EVAnnotation (Annotation or
	 * Classification)
	 * 
	 * @param ac
	 *            the AnnotationCollection
	 * @param node
	 *            the XML node
	 */
	public static void readNode(AnnotationCollection ac, Element node)
			throws Exception {
		String type = node.getAttributeValue("type");
		if ("annotation".equals(type)) {
			EVAnnotation annotation = readAnnotation(ac, node);
			if (annotation != null) {
				ac.addAnnotation(annotation.getId(), annotation);
			}
		} else if ("classification".equals(type)) {
			Classification c = readClassification(ac, node);
			if (c != null) {
				ac.addClassification(c.getId(), c);
			}
		}
	}

	/**
	 * Create workbench annotation from GrAF element.
	 * 
	 * @param ac
	 *            the AnnotationCollection
	 * @param node
	 *            the node
	 * @return the new EVAnnotation
	 */
	static EVAnnotation readAnnotation(AnnotationCollection ac, Element node)
			throws Exception {
		EVAnnotation annotation = null;
		String id = node.getAttributeValue("id");
		List cl = node.getChildren("f");
		String level = null;
		String matchedID = null;
		boolean verified = false;
		boolean verifiedTrue = false;
		Vector<String> attributes = new Vector(0);
		Vector<String> values = new Vector(0);
		TypeSystem ts = TypeSystem.getTypeSystem();
		for (ListIterator ci = cl.listIterator(); ci.hasNext();) {
			Element feature = (Element) ci.next();
			String name = feature.getAttributeValue("name");
			String value = feature.getAttributeValue("value");
			name = ts.getRegularizedName(name);
			value = ts.getRegularizedName(value);
			if ("level".equals(name)) {
				level = value;
			} else if ("verified".equals(name)) {
				verified = true;
				verifiedTrue = Boolean.parseBoolean(value);
			} else if ("matchid".equals(name)) {
				matchedID = value;
			} else {
				attributes.add(name);
				values.add(value);
			}
		}
		annotation = ac.createAnnotation(level);
		if (annotation != null) {
			annotation.setId(id);
			if (verified) {
				annotation.setVerified(true);
				annotation.setVerifiedTrue(verifiedTrue);
			}
			annotation.setVerified(verified);
			annotation.setMatchedAnnotationID(matchedID);
			for (int i = 0; i < attributes.size(); i++) {
				String attribute = attributes.elementAt(i);
				String value = values.elementAt(i);
				attribute = ts.getRegularizedName(attribute);
				value = ts.getRegularizedName(value);
				if (attribute != null && value != null) {
					if (!ts.isUseOnlyTypeModel()) {
						annotation.getType().addAttribute(attribute);
					}
					annotation.setAttribute(attribute, value);
				}
			}
		}
		return annotation;
	}

	/**
	 * Create workbench Classification object from GrAF XML element
	 * 
	 * @param ac
	 *            the AnnotationCollection
	 * @param node
	 *            the node
	 * @return the classification
	 */
	static Classification readClassification(AnnotationCollection ac,
			Element node) throws Exception {
		TypeSystem ts = TypeSystem.getTypeSystem();
		String id = node.getAttributeValue("id");
		String[] sa = id.split("_");
		int nid = Integer.parseInt(sa[1]);
		String annotationType = node.getAttributeValue("annotationtype");
		List cl = node.getChildren("f");
		Vector<String> attributes = new Vector(0);
		Vector<String> values = new Vector(0);
		String cname = null;
		String cvalue = null;
		for (ListIterator ci = cl.listIterator(); ci.hasNext();) {
			Element feature = (Element) ci.next();
			cname = feature.getAttributeValue("name");
			cvalue = feature.getAttributeValue("value");

			// 9/24/2012
			cname = ts.getRegularizedName(cname);
			cvalue = ts.getRegularizedName(cvalue);

			attributes.add(cname);
			values.add(cvalue);
		}
		Classification c = new Classification(ac, null, id, nid,
				annotationType, cname, cvalue);
		for (int i = 0; i < attributes.size(); i++) {
			String attribute = attributes.elementAt(i);
			String value = values.elementAt(i);

			// 10/17/2012
			if (!ts.isUseOnlyTypeModel()) {
				typesystem.Classification tsc = c.getParentClassification();
				tsc.addAttribute(attribute);
			}
			c.setProperty(attribute, value);
		}
		return c;
	}

	/**
	 * Create XML string from workbench annotation.
	 * 
	 * @param annotation
	 *            the annotation
	 * @return the string
	 */
	public static String toXML(EVAnnotation annotation) {
		StringBuffer sb = new StringBuffer();
		sb.append("<node id=\"" + annotation.getId()
				+ "\" type=\"annotation\">\n");
		sb.append("<f name=\"level\" value=\"" + annotation.getType().getName()
				+ "\"/>\n");
		if (annotation.getMatchedAnnotation() != null) {
			sb.append("<f name=\"matchid\" value=\""
					+ annotation.getMatchedAnnotation().getId() + "\"/>\n");
		}
		if (annotation.isVerified()) {
			sb.append("<f name=\"verified\" value=\""
					+ annotation.isVerifiedTrue() + "\"/>\n");
		}
		if (annotation.getAttributeValues() != null) {
			for (Enumeration<String> e = annotation.getAttributeMap().keys(); e
					.hasMoreElements();) {
				String attribute = e.nextElement();
				for (Object value : annotation.getAttributeMap().get(attribute)) {
					sb.append("<f name=\"" + attribute + "\" value=\"" + value
							+ "\"/>\n");
				}
			}
		}
		sb.append("</node>\n");
		Classification c = annotation.getClassification();
		if (c != null && c.getPropertyNames() != null) {
			sb.append("<node id=\"" + c.getId()
					+ "\" type=\"classification\" annotationtype=\""
					+ c.getAnnotationType() + "\">\n");
			for (String name : c.getPropertyNames()) {
				String value = c.getStringProperty(name);
				sb.append("<f name=\"" + name + "\" value=\"" + value
						+ "\" />\n");
			}
			sb.append("</node>\n");
			sb.append("<edge from=\"" + annotation.getId() + "\" to=\""
					+ c.getId() + "\"/>\n");
		}
		if (annotation.getRelations() != null) {
			for (Enumeration<String> e = annotation.getRelationMap().keys(); e
					.hasMoreElements();) {
				String rname = e.nextElement();
				Vector<EVAnnotation> relata = annotation.getRelationMap().get(
						rname);
				for (EVAnnotation relatum : relata) {
					sb.append("<edge from=\"" + annotation.getId() + "\" to=\""
							+ relatum.getId() + "\" relation_name=\"" + rname
							+ "\"/>\n");
				}
			}
		}

		if (annotation.getComponents() == null && annotation.getSpans() != null) {
			for (Span span : annotation.getSpans()) {
				sb.append("<sink id=\"" + span.getId() + "\" start=\""
						+ span.getTextStart() + "\" end=\"" + span.getTextEnd()
						+ "\"/>\n");
				sb.append("<edge from=\"" + annotation.getId() + "\" to=\""
						+ span.getId() + "\"/>\n");
			}
		}
		return sb.toString();
	}

	public Hashtable<String, Vector<EVAnnotation>> getClassificationAnnotationHash() {
		return classificationAnnotationHash;
	}

	public void setClassificationAnnotationHash(
			Hashtable<String, Vector<EVAnnotation>> classificationAnnotationHash) {
		this.classificationAnnotationHash = classificationAnnotationHash;
	}

	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	public void setAnnotationCollection(
			AnnotationCollection annotationCollection) {
		this.annotationCollection = annotationCollection;
	}

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public void setTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public Vector<EVAnnotation> getAllAnnotations() {
		return allAnnotations;
	}

	public void setAllAnnotations(Vector<EVAnnotation> allAnnotations) {
		this.allAnnotations = allAnnotations;
	}

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
