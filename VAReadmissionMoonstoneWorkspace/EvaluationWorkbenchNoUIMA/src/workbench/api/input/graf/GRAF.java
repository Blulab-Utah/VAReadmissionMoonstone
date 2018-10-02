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
package workbench.api.input.graf;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.Span;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;
import workbench.api.typesystem.ClassificationInstance;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class GRAF {

	private static Hashtable<String, Object> IDMap = null;

	public static void readXML(AnnotationCollection ac, String xml,
			boolean isPrimary) throws Exception {
		Hashtable<String, Span> hash = new Hashtable();
		org.jdom.Document jdoc = null;
		InputSource is = new InputSource(new ByteArrayInputStream(
				xml.getBytes("utf-8")));
		jdoc = new SAXBuilder().build(is);
		Element root = jdoc.getRootElement();
		String fname = root.getAttributeValue("source_file");
		List l = root.getChildren("node");
		String tname = root.getAttributeValue("attribute_type");
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
			Span span = new Span(null, start, end);
			hash.put(id, span);
		}
		l = root.getChildren("edge");
		for (ListIterator i = l.listIterator(); i.hasNext();) {
			Element edge = (Element) i.next();
			String fromID = edge.getAttributeValue("from");
			String toID = edge.getAttributeValue("to");
			String relationName = edge.getAttributeValue("relation_name");
			Annotation fromAnnotation = ac.getAnnotationByID(fromID);
			if (fromAnnotation != null) {
				Annotation toAnnotation = ac.getAnnotationByID(toID);
				ClassificationInstance ci = (ClassificationInstance) ac
						.getClassificationInformationByID(toID);
				Span span = hash.get(toID);
				if (ci != null) {
					// 10/10/2014:  Need a way for user to select which classification
					// to view.  OR, could just use attribute classifications.
					String firstcvalue = ci.getValues().firstElement();
					fromAnnotation.setClassificationValue(firstcvalue);
					for (int j = 0; j < ci.getAttributes().size(); j++) {
						String cname = ci.getAttributes().elementAt(j);
						String cvalue = ci.getValues().elementAt(j);
						fromAnnotation.putAttributeValue(cname, cvalue);
					}
				} else if (span != null) {
					fromAnnotation.addSpan(span.getStart(), span.getEnd());
				}

				// Before New WB
				// if (relationName != null && toAnnotation != null) {
				// fromAnnotation.setRelation(relationName, toAnnotation);
				// if (Annotation.isComponentRelation(relationName)) {
				// fromAnnotation.addComponent(toAnnotation);
				// toAnnotation.setRelation("parent", fromAnnotation);
				// }
				// toAnnotation.setRelation("parent", fromAnnotation);
				// } else if (toAnnotation != null) {
				// fromAnnotation.setRelation("component", toAnnotation);
				// fromAnnotation.addComponent(toAnnotation);
				// toAnnotation.setRelation("parent", fromAnnotation);
				// } else if (toClassification != null) {
				// fromAnnotation.setClassification(toClassification);
				// toClassification.setAnnotation(fromAnnotation);
				// } else if (span != null) {
				// fromAnnotation.addSpan(span);
				// }
			}
		}
		// ac.storeAnalysisIndices();
	}

	public static void readNode(AnnotationCollection ac, Element node)
			throws Exception {
		String type = node.getAttributeValue("type");
		if ("annotation".equals(type)) {
			readAnnotation(ac, node);
		} else if ("classification".equals(type)) {
			readClassification(ac, node);
		}
	}

	static Annotation readAnnotation(AnnotationCollection ac, Element node)
			throws Exception {
		Annotation annotation = null;
		String id = node.getAttributeValue("id");
		List cl = node.getChildren("f");
		String level = null;
		String matchedID = null;
		boolean verified = false;
		boolean verifiedTrue = false;
		Vector<String> attributes = new Vector(0);
		Vector<String> values = new Vector(0);
		TypeSystem ts = Analysis.CurrentAnalysis.getTypeSystem();
		for (ListIterator ci = cl.listIterator(); ci.hasNext();) {
			Element feature = (Element) ci.next();
			String name = feature.getAttributeValue("name");
			String value = feature.getAttributeValue("value");
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
		Type type = ts.getOrCreateType(null, level);
		annotation = new Annotation(ac, type);
		annotation.setId(id);
		ac.addAnnotation(annotation, id);

		for (int i = 0; i < attributes.size(); i++) {
			String aname = attributes.elementAt(i);
			String value = values.elementAt(i);
			if (aname != null && value != null) {
				Attribute attr = Attribute.createAttribute(type, aname);
				type.addAttribute(attr);
				attr.addValue(value);

				// 9/27/2014
				annotation.putAttributeValue(attr, value);

				// if (!ts.isUseOnlyTypeModel()) {
				// annotation.getType().addAttribute(attribute);
				// }
				// annotation.setAttribute(attribute, value);
			}
		}
		return annotation;
	}

	static void readClassification(AnnotationCollection ac, Element node) {
		TypeSystem ts = Analysis.CurrentAnalysis.getTypeSystem();
		String id = node.getAttributeValue("id");
		String[] sa = id.split("_");
		int nid = Integer.parseInt(sa[1]);
		String tname = node.getAttributeValue("annotationtype");
		Type type = (Type) ts.getObjectHash(tname);
		List cl = node.getChildren("f");
		Vector<String> attributes = new Vector(0);
		Vector<String> values = new Vector(0);
		String cname = null;
		String cvalue = null;
		for (ListIterator ci = cl.listIterator(); ci.hasNext();) {
			Element feature = (Element) ci.next();
			cname = feature.getAttributeValue("name");
			cvalue = feature.getAttributeValue("value");
			attributes.add(cname);
			values.add(cvalue);
		}
		ClassificationInstance ci = new ClassificationInstance(attributes,
				values);
		ac.storeClassificationInformationByID(id, ci);
		for (int i = 0; i < attributes.size(); i++) {
			String aname = attributes.elementAt(i);
			String value = values.elementAt(i);
			Classification c = (Classification) type.getAttribute(aname);
			if (c == null) {
				c = new Classification(type, aname);
			}
			c.addValue(value);
		}
	}

	// Before 10/10/2014
	static void readClassification_BEFORE_10_10_2014(AnnotationCollection ac,
			Element node) {
		TypeSystem ts = Analysis.CurrentAnalysis.getTypeSystem();
		String id = node.getAttributeValue("id");
		String[] sa = id.split("_");
		int nid = Integer.parseInt(sa[1]);
		String tname = node.getAttributeValue("annotationtype");
		Type type = (Type) ts.getObjectHash(tname);
		List cl = node.getChildren("f");
		Vector<String> attributes = new Vector(0);
		Vector<String> values = new Vector(0);
		String cname = null;
		String cvalue = null;
		for (ListIterator ci = cl.listIterator(); ci.hasNext();) {
			Element feature = (Element) ci.next();
			cname = feature.getAttributeValue("name");
			cvalue = feature.getAttributeValue("value");
			attributes.add(cname);
			values.add(cvalue);
		}
		cname = attributes.firstElement();
		cvalue = values.firstElement();
		Classification c = (Classification) type.getAttribute(cname);
		if (c == null) {
			c = new Classification(type, cname);
		}
//		c.addValue(cvalue);
//		ac.storeClassificationInformationByID(id, cvalue);
		for (int i = 0; i < attributes.size(); i++) {
			String aname = attributes.elementAt(i);
			String value = values.elementAt(i);
			if (!aname.equals(cname)) {
				Attribute attr = Attribute.createAttribute(type, aname);
				attr.addValue(value);
			}
		}
	}

}
