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
package annotation;

import io.knowtator.KTAnnotation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import typesystem.Attribute;
import typesystem.TypeObject;
import workbench.arr.EvaluationWorkbench;

/**
 * Base class for all Annotation objects. There is a subclass for each
 * annotation level, e.g. Document, Snippet, Token, Group, etc.
 */
public abstract class EVAnnotation {
	public AnnotationCollection annotationCollection = null;
	Hashtable<String, Vector> attributeMap = new Hashtable();
	Vector<String> attributeNames = new Vector(0);
	Hashtable<String, Vector> relationMap = new Hashtable();
	Vector<RelationObject> relationObjects = null;
	EVAnnotation parentAnnotation = null;
	Vector<EVAnnotation> allMatchingAnnotations = null;
	Vector<EVAnnotation> touchingAnnotations = null;
	TypeObject type = null;
	boolean hasMismatch = false;
	String mismatchType = null;
	String state = null;
	String id = null;
	int numericID = 0;
	String text = null;
	Document document = null;
	Vector<Span> spans = null;
	Classification classification = null;
	Vector<EVAnnotation> components = null;
	int UIMAStart = 0;
	int UIMAEnd = 0;
	boolean isVerified = false;
	boolean isVerifiedTrue = false;
	int spanStart = -1;
	int spanEnd = -1;
	int spanLength = -1;
	EVAnnotation matchedAnnotation = null;
	String matchedAnnotationID = null;
	boolean visited = false;
	boolean isVisible = false;
	KTAnnotation ktAnnotation = null;
	Object userObject = null;

	/**
	 * The currently known state attribute names (NEED TO FIND BETTER SOLUTION).
	 */
	static String[] stateAttributeNames = { "state", "status", "directionality" };

	/**
	 * The currently known present attribute names (NEED TO FIND BETTER
	 * SOLUTION).
	 */
	static String[] presentNames = { "present", "acute", "affirmed", "chronic" };

	/**
	 * The currently known absence attribute names (NEED TO FIND BETTER
	 * SOLUTION)..
	 */
	static String[] absentNames = { "absent", "negated", "negation_present" };

	/**
	 * The currently known missing attribute names (NEED TO FIND BETTER
	 * SOLUTION).
	 */
	static String[] missingNames = { "missing" };
	static Vector<TypeObject> annotationTypes = null;

	public EVAnnotation() {
	}

	public EVAnnotation(TypeObject type, Document document) throws Exception {
		this.document = document;
		this.setType(type);
		annotationTypes = VUtils.addIfNot(annotationTypes, this.getType());
	}

	// 7/6/2014
	public boolean isDocumentType() {
		if (this.getType() != null
				&& this.getType().getName().toLowerCase().contains("document")) {
			return true;
		}
		return false;
	}

	public void storeAnalysisIndices(AnnotationCollection ac) throws Exception {
		ac.addAnnotationClassification(this);
		Vector<String> attributes = this.getAttributes();
		if (attributes != null) {
			for (String aname : attributes) {
				Object value = this.getAttribute(aname);
				if (value != null) {
					ac.getAnalysis().addClassAttribute(this.getClass(), aname,
							value);
					ac.addAVPairAnnotation(this, aname, value.toString());
				}
			}
		}
		if (this.getRelations() != null) {
			for (String relation : this.getRelations()) {
				ac.getAnalysis().addClassRelation(this.getClass(), relation);
			}
		}
	}

	public void removeAnalysisIndices() throws Exception {
		this.getAnnotationCollection().removeAnnotationClassification(this);
	}

	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<annotation id=\"" + this.getId() + "\" level=\""
				+ this.getType() + "\">\n");
		if (this.getClassification() != null) {
			sb.append("\t<classification name=\"" + this.getClassification()
					+ "\"/>\n");
		}
		if (this.getAttributeValues() != null) {
			sb.append("\t<attributes>\n");
			for (Enumeration<String> e = attributeMap.keys(); e
					.hasMoreElements();) {
				String aname = e.nextElement();
				for (Object attribute : attributeMap.get(aname)) {
					sb.append("\t\t<attribute name=\"" + aname + "\" value=\""
							+ attribute + "\"/>\n");
				}
			}
			sb.append("\t</attributes>\n");
		}
		if (this.getRelations() != null) {
			sb.append("\t<relations>\n");
			for (Enumeration<String> e = relationMap.keys(); e
					.hasMoreElements();) {
				String rname = e.nextElement();
				Vector<EVAnnotation> relata = relationMap.get(rname);
				for (EVAnnotation relatum : relata) {
					sb.append("\t\t<relation name=\"" + rname + "\" value=\""
							+ relatum.getId() + "\"/>\n");
				}
			}
			sb.append("\t</relations>\n");
		}
		if (this.getSpans() != null) {
			sb.append("\t<spans>\n");
			for (Span span : this.getSpans()) {
				sb.append("\t\t<span start=\"" + span.getTextStart()
						+ "\" end=\"" + span.getTextEnd() + "\" text=\"");
				sb.append(span.getText() + "\"/>\n");
			}
			sb.append("\t</spans>\n");
		}
		sb.append("</annotation>\n");
		return sb.toString();
	}

	/**
	 * Checks whether two annotations have the same level (e.g. Document,
	 * Snippet), and the same state (e.g. present, negated)
	 * 
	 */
	public static boolean isSameLevelState(EVAnnotation annotation1,
			EVAnnotation annotation2, Class level) throws Exception {
		if ((isPresentLevel(annotation1, level) && isPresentLevel(annotation2,
				level))
				|| (!isPresentLevel(annotation1, level) && !isPresentLevel(
						annotation2, level))) {
			return true;
		}
		return false;
	}

	/**
	 * Tests whether two annotations exist at the same level, and are marked as
	 * "present" (or equivalent)
	 */
	public static boolean isPresentLevel(EVAnnotation annotation, Class level)
			throws Exception {
		if (annotation != null && level != null
				&& level.equals(annotation.getClass())) {
			String state = annotation.getState();
			if (StrUtils.getMatchingStringIndex(presentNames, state) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests whether two annotations exist at the same level, and are marked as
	 * "absent" (or equivalent)
	 */
	public static boolean isAbsentLevel(EVAnnotation annotation, Class level) {
		if (annotation == null || !level.equals(annotation.getClass())) {
			return true;
		}
		if (level.equals(annotation.getClass())) {
			String state = annotation.getState();
			if (StrUtils.getMatchingStringIndex(absentNames, state) >= 0
					|| StrUtils.getMatchingStringIndex(missingNames, state) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests whether two annotations exist at the same level, and are marked as
	 * "present" (or equivalent)
	 */
	public static boolean isPresentSimpleOverlap(EVAnnotation annotation) {
		if (annotation != null) {
			String state = annotation.getState();
			if (StrUtils.getMatchingStringIndex(presentNames, state) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests whether two annotations exist at the same level, and are marked as
	 * "absent" (or equivalent)
	 */
	public static boolean isAbsentSimpleOverlap(EVAnnotation annotation) {
		if (annotation == null) {
			return true;
		}
		String state = annotation.getState();
		if (StrUtils.getMatchingStringIndex(absentNames, state) >= 0
				|| StrUtils.getMatchingStringIndex(missingNames, state) >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Tests whether two annotations have the same classification, and are both
	 * marked "present"
	 */
	public static boolean isSameClassificationState(EVAnnotation annotation1,
			EVAnnotation annotation2, Classification classification) {
		if ((isPresentClassification(annotation1, classification) && isPresentClassification(
				annotation2, classification))
				|| (!isPresentClassification(annotation1, classification) && !isPresentClassification(
						annotation2, classification))) {
			return true;
		}
		return false;
	}

	/**
	 * Tests whether an annotation has a specified classification, and is
	 * present.
	 */
	public static boolean isPresentClassification(EVAnnotation annotation,
			Classification classification) {
		if (annotation != null && classification != null
				&& classification.equals(annotation.getClassification())
				&& !isAbsentClassification(annotation, classification)) {
			String state = annotation.getState();
			if (StrUtils.getMatchingStringIndex(presentNames,
					state.toLowerCase()) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests whether an annotation has a specified classification, and is
	 * absent.
	 */
	public static boolean isAbsentClassification(EVAnnotation annotation,
			Classification classification) {
		if (annotation == null
				|| !classification.equals(annotation.getClassification())) {
			return true;
		}
		if (classification.equals(annotation.getClassification())) {
			String state = annotation.getState();
			if (StrUtils.getMatchingStringIndex(absentNames,
					state.toLowerCase()) >= 0
					|| StrUtils.getMatchingStringIndex(missingNames, state) >= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the annotation has a specified attribute/value pair.
	 * 
	 */
	public static boolean isPresentAttributeValue(EVAnnotation annotation,
			String attribute, String value) {
		if (annotation != null) {
			Object o = annotation.getAttribute(attribute);
			if (value.equals(annotation.getAttribute(attribute))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether two annotations have the same attribute value (or both
	 * null).
	 */
	public static boolean isSameAttributeValue(EVAnnotation annotation1,
			EVAnnotation annotation2, String attribute) {
		Object v1 = annotation1.getAttribute(attribute);
		Object v2 = annotation2.getAttribute(attribute);
		return ((v1 == null && v2 == null) || (v1 != null && v1.equals(v2)));
	}

	public String getText() {
		if (this.text == null
				&& this.getAnnotationCollection().getDocument() != null
				&& this.getAnnotationCollection().getDocument().getText() != null) {
			String dtext = this.getAnnotationCollection().getDocument()
					.getText();
			if (dtext != null && this.getStart() >= 0
					&& this.getEnd() > this.getStart()
					&& dtext.length() > this.getEnd()) {
				this.text = dtext.substring(this.getStart(), this.getEnd() + 1);
			}
		}
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Document getDocument() {
		if (this.document == null && this.getAnnotationCollection() != null) {
			this.document = this.getAnnotationCollection().getDocument();
		}
		return document;
	}

	public void setDocument(Document doc) {
		this.document = doc;
	}

	public boolean isSnippet() {
		return this instanceof SnippetAnnotation;
	}

	public boolean isValid() {
		if (this.getClassification() == null) {
			return false;
		}
		Vector attributes = this.getAttributeValues();
		if (attributes != null && this.isSnippet()) {
			for (Object attribute : attributes) {
				if ("missing".equals(attribute.toString())) {
					return false;
				}
			}
		}
		return true;
	}

	public void setSpans(Vector<Span> spans) {
		this.spans = spans;
	}

	public Vector<Span> getSpans() {
		Vector<Span> allSpans = null;
		if (this.spans == null && this.getComponents() != null) {
			for (EVAnnotation component : this.getComponents()) {
				allSpans = VUtils.appendIfNot(allSpans, component.getSpans());
			}
			if (allSpans != null) {
				Collections.sort(allSpans, new Span.PositionSorter());
				this.setSpans(allSpans);
			}
		}
		return this.spans;
	}

	public Span getFirstSpan() {
		if (spans != null) {
			return spans.firstElement();
		}
		return null;
	}

	public void addSpan(int start, int end) {
		if (start >= 0 && end >= start) {
			Span span = new Span(this, start, end);
			this.spans = VUtils.add(this.spans, span);
			Collections.sort(this.spans, new Span.PositionSorter());
		}
	}

	public static Vector<Span> getSpans(String str) throws Exception {
		Vector<Span> spans = null;
		String[] spanstrs = str.split(",");
		for (int i = 0; i < spanstrs.length; i++) {
			String[] sestrs = spanstrs[i].split("-");
			if (sestrs.length != 2) {
				return null;
			}
			int start = Integer.valueOf(sestrs[0]).intValue();
			int end = Integer.valueOf(sestrs[1]).intValue();
			Span span = new Span(null, start, end);
			spans = VUtils.add(spans, span);
		}
		return spans;
	}

	public void addSpan(Span span) {
		span.annotation = this;
		span.setId(span.getId());
		this.spans = VUtils.add(this.spans, span);
		Collections.sort(this.spans, new Span.PositionSorter());
	}

	public int getStart() {
		if (this.spanStart < 0 && this.getSpans() != null) {
			this.spanStart = this.getSpans().firstElement().getTextStart();
		}
		return this.spanStart;
	}

	public int getEnd() {
		if (this.spanEnd < 0 && this.getSpans() != null) {
			this.spanEnd = this.getSpans().lastElement().getTextEnd();
		}
		return this.spanEnd;
	}

	public int getLength() {
		if (this.spanLength < 0) {
			int length = 0;
			for (Span span : this.getSpans()) {
				length += span.getLength();
			}
			this.spanLength = length;
		}
		return this.spanLength;
	}

	public boolean coversPosition(int position) {
		for (Span span : this.getSpans()) {
			if (span.coversPosition(position)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns subannotations related to the current annotation via a
	 * "component_of" relation. Stores them with "components" field.
	 */
	public Vector<EVAnnotation> getComponents() {
		if (this.components == null) {
			Vector<EVAnnotation> relata = this.getRelata("component_of");
			if (relata == null) {
				relata = this.getRelata("component");
			}
			if (relata != null) {
				this.setComponents(relata);
			}
		}
		return components;
	}

	public void setComponents(Vector<EVAnnotation> components) {
		this.components = components;
	}

	public void addComponent(EVAnnotation component) {
		this.components = VUtils.addIfNot(this.components, component);
	}

	public boolean isFamily(EVAnnotation annotation) {
		if (this.components != null && this.components.contains(annotation)) {
			return true;
		}
		if (annotation != null && annotation.components != null
				&& annotation.components.contains(this)) {
			return true;
		}
		return false;
	}

	public Classification getClassification() {
		return classification;
	}

	public void setClassification(Classification classification) {
		this.classification = classification;
		classification.setAnnotation(this);
		if (this.type == null
				&& classification.getParentClassification() != null) {
			this.setType(classification.getParentClassification()
					.getParentTypeObject());
		}
	}

	// 3/7/2014: Find & replace all code that looks like this...
	public void setPropertyOrAttributeValue(String cname, String cvalue) {
		if (this.getClassification() != null) {
			typesystem.Annotation type = this.getClassification()
					.getParentAnnotationType();
			Classification c = this.getClassification();
			typesystem.Classification pclass = c.getParentClassification();
			String astr = type.getName() + "$" + cname;
			Attribute pattr = pclass.getAttribute(astr);
			Attribute tattr = type.getAttribute(astr);
			if (pattr != null) {
				if (c.getProperty(astr) == null) {
					c.setProperty(astr, cvalue);
				}
			} else if (tattr != null) {
				if (this.getAttribute(astr) == null) {
					this.setAttribute(astr, cvalue);
				}
			}
		}
	}

	public Object getAttribute(String aname) {
		if (aname != null) {
			Vector v = (Vector) attributeMap.get(aname);
			if (v != null) {
				return v.firstElement();
			}
		}
		return null;
	}

	public Vector<Vector> getAVPairs() {
		Vector<Vector> avpairs = null;
		for (Enumeration<String> e = attributeMap.keys(); e.hasMoreElements();) {
			String aname = e.nextElement();
			Object value = attributeMap.get(aname);
			if (value instanceof Vector) {
				value = ((Vector) value).firstElement();
			}
			Vector pair = new Vector(0);
			pair.add(aname);
			pair.add(value);
			avpairs = VUtils.add(avpairs, pair);
		}
		return avpairs;
	}

	public void setAttribute(String aname, Object value) {
		if (aname != null) {
			if (value == null) {
				value = "EMPTY";
			}
			this.attributeMap.remove(aname);
			VUtils.pushHashVector(this.attributeMap, aname, value);
			this.attributeNames = VUtils.addIfNot(this.attributeNames, aname);
			Attribute atype = this.getType().getAttribute(aname);
			if (atype != null) {
				atype.addValue(value);
			}
		}
	}

	public void removeAttribute(String aname) {
		this.attributeMap.remove(aname);
	}

	public int getNumberOfAttributes() {
		return this.attributeMap.keySet().size();
	}

	public Vector<String> getAttributes() {
		return this.attributeNames;
	}

	public Vector getAttributeValues() {
		Vector values = null;
		if (!this.attributeMap.isEmpty()) {
			for (Enumeration e = this.attributeMap.elements(); e
					.hasMoreElements();) {
				values = VUtils.append(values, (Vector) e.nextElement());
			}
		}
		return values;
	}

	public float attributeSimilarity(EVAnnotation other) {
		if (other != null) {
			Classification c1 = this.getClassification();
			Classification c2 = this.getClassification();
			if (c1.equals(c2)) {
				return VUtils.degreeOverlap(this.getAttributeValues(),
						other.getAttributeValues());
			}
		}
		return 0f;
	}

	public Vector getRelata(String rname) {
		return relationMap.get(rname);
	}

	public void setRelation(String relation, Object value) {
		if (relation != null && value != null) {
			String rname = relation;
			if (this.relationMap.get(rname) == null
					|| isComponentRelation(relation)) {
				VUtils.pushHashVector(this.relationMap, rname, value);
				RelationObject ro = new RelationObject(rname,
						(EVAnnotation) value);
				this.relationObjects = VUtils.add(this.relationObjects, ro);

				if (value instanceof EVAnnotation
						&& isComponentRelation(relation)) {
					EVAnnotation to = (EVAnnotation) value;
					to.parentAnnotation = this;
				}
			}
		}
	}

	public static boolean isComponentRelation(String relation) {
		return (relation != null && relation.indexOf("component") >= 0);
	}

	public Vector<String> getRelations() {
		Vector v = new Vector(this.relationMap.keySet());
		return v;
	}

	public Vector<RelationObject> getRelationObjects() {
		if (this.relationObjects == null) {
			for (Enumeration<String> e = this.relationMap.keys(); e
					.hasMoreElements();) {
				String relation = e.nextElement();
				Vector<EVAnnotation> relata = this.relationMap.get(relation);
				for (EVAnnotation relatum : relata) {
					RelationObject ro = new RelationObject(relation, relatum);
					this.relationObjects = VUtils.add(this.relationObjects, ro);
				}
			}
		}
		return this.relationObjects;
	}

	public EVAnnotation getParentAnnotation() {
		return this.parentAnnotation;
	}

	public void setParentAnnotation(EVAnnotation parentAnnotation) {
		this.parentAnnotation = parentAnnotation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id == null) {
			int num = this.getAnnotationCollection()
					.getNumberOfAnnotationsByType(this.getType());
			id = this.getType().getName() + "_" + num;
		}
		this.id = id;
		this.getAnnotationCollection().getAnnotationIDMap().put(id, this);
	}

	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	public void setAnnotationCollection(AnnotationCollection ac) {
		this.annotationCollection = ac;
		VUtils.pushHashVector(ac.typeAnnotationMap, this.getType(), this);
	}

	public String getState() {
		if (this.state == null) {
			EvaluationWorkbench workbench = EvaluationWorkbench
					.getEvaluationWorkbench();
			String[] names = (workbench.getStateAttributeNames() != null ? workbench
					.getStateAttributeNames() : stateAttributeNames);
			for (int i = 0; i < names.length; i++) {
				String sname = names[i];
				for (Enumeration<String> e = this.attributeMap.keys(); this.state == null
						&& e.hasMoreElements();) {
					String key = e.nextElement();
					if (key.equals(sname) || key.indexOf(sname) >= 0) {
						this.state = (String) this.attributeMap.get(key)
								.firstElement();
						break;
					}
				}
			}
			if (this.state == null) {
				this.state = "present";
			}
		}
		return this.state;
	}

	public String toString() {
		String str = "<" + this.getLevelPrefix() + ":Class="
				+ this.getClassification();
		if (this.getSpans() != null) {
			str += this.getSpans();
		}
		str += ">";
		return str;
	}

	public boolean equals(Object o) {
		if (o instanceof EVAnnotation) {
			EVAnnotation other = (EVAnnotation) o;
			if (this.getAnnotationCollection() != null
					&& other.getAnnotationCollection() != null
					&& this.getAnnotationCollection().equals(
							other.getAnnotationCollection())
					&& this.getType().equals(other.getType())
					&& this.getClassification() != null
					&& other.getClassification() != null

					&& this.getClassification().equals(
							other.getClassification())

					// Before 11/6/2013
					// && this.getClassification().getValue()
					// .equals(other.getClassification().getValue())

					&& this.getStart() == other.getStart()
					&& this.getEnd() == other.getEnd()) {
				return true;
			}
		}
		return false;
	}

	public String getLevelPrefix() {
		return "?";
	}

	public int getNumericID() {
		return this.numericID;
	}

	public void setNumericID(int id) {
		this.numericID = id;
	}

	public Hashtable<String, Vector> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Hashtable<String, Vector> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public Hashtable<String, Vector> getRelationMap() {
		return relationMap;
	}

	public void setRelationMap(Hashtable<String, Vector> relationMap) {
		this.relationMap = relationMap;
	}

	public void setRelationObjects(Vector<RelationObject> relationObjects) {
		this.relationObjects = relationObjects;
	}

	public static class ClassificationSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			EVAnnotation a1 = (EVAnnotation) o1;
			EVAnnotation a2 = (EVAnnotation) o2;
			return a1.getClassification().getValue()
					.compareTo(a2.getClassification().getValue());
		}
	}

	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			EVAnnotation a1 = (EVAnnotation) o1;
			EVAnnotation a2 = (EVAnnotation) o2;
			if (a1.getStart() < a2.getStart()) {
				return -1;
			}
			if (a2.getStart() < a1.getStart()) {
				return 1;
			}
			if (a1.getEnd() > a2.getEnd()) {
				return -1;
			}
			if (a2.getEnd() > a1.getEnd()) {
				return 1;
			}
			return 0;
		}
	}

	public boolean touchedByCursor(int position) {
		if (this.getSpans() != null) {
			for (Span span : this.getSpans()) {
				if (span.getTextStart() <= position
						&& position <= span.getTextEnd()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getUIMAStart() {
		return UIMAStart;
	}

	public void setUIMAStart(int uIMAStart) {
		UIMAStart = uIMAStart;
	}

	public int getUIMAEnd() {
		return UIMAEnd;
	}

	public void setUIMAEnd(int uIMAEnd) {
		UIMAEnd = uIMAEnd;
	}

	public TypeObject getType() {
		return type;
	}

	public void setType(TypeObject type) {
		this.type = type;
	}

	public boolean isStartEndAssigned() {
		return this.getStart() >= 0;
	}

	public boolean isHasMismatch() {
		return this.mismatchType != null;
	}

	public void setHasMismatch(String type) {
		this.mismatchType = type;
	}

	public String getMismatchType() {
		return this.mismatchType;
	}

	public void setUnverified() {
		this.isVerifiedTrue = false;
		this.isVerified = false;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public boolean isVerifiedTrue() {
		return isVerifiedTrue;
	}

	public void setVerifiedTrue(boolean isVerifiedTrue) {
		this.isVerifiedTrue = isVerifiedTrue;
		this.isVerified = true;
	}

	public EVAnnotation getIndirectMatchedAnnotation() {
		if (this.getMatchedAnnotation() != null) {
			return this.getMatchedAnnotation();
		}
		// 3/7/2013: Turning this function off in response to Wendy's need to
		// find
		// duplicates.
		boolean flag = true;
		if (flag && this.touchingAnnotations != null) {
			for (EVAnnotation touching : this.touchingAnnotations) {
				if (touching.getMatchedAnnotation() != null
						&& AnnotationCollection
								.hasStrictOverlap(this, touching)) {
					return touching.getMatchedAnnotation();
				}
			}
		}
		return null;
	}

	public EVAnnotation getMatchedAnnotation() {
		return matchedAnnotation;
	}

	public void setMatchedAnnotation(EVAnnotation matchedAnnotation) {
		this.matchedAnnotation = matchedAnnotation;
	}

	public String getMatchedAnnotationID() {
		return matchedAnnotationID;
	}

	public void setMatchedAnnotationID(String matchedAnnotationID) {
		this.matchedAnnotationID = matchedAnnotationID;
	}

	public String getAnnotatorType() {
		return this.getAnnotationCollection().getAnnotatorType();
	}

	public void setVisited() {
		this.visited = true;
	}

	public void resetVisited() {
		this.visited = false;
	}

	public boolean isVisited() {
		return this.visited;
	}

	public static void resetVisited(Vector<EVAnnotation> annotations) {
		if (annotations != null) {
			for (EVAnnotation annotation : annotations) {
				annotation.resetVisited();
			}
		}
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public void setIsVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public KTAnnotation getKtAnnotation() {
		return ktAnnotation;
	}

	public void setKtAnnotation(KTAnnotation ktAnnotation) {
		this.ktAnnotation = ktAnnotation;
	}

	public String getToolTip(boolean isAltDown) {
		this.setVisited();
		StringBuffer sb = new StringBuffer();
		sb.append("\"" + this.getText() + "\":");
		sb.append(this.getClassification().getValue());
		if (!this.attributeMap.isEmpty()) {
			sb.append("[");
			for (Enumeration<String> e = this.attributeMap.keys(); e
					.hasMoreElements();) {
				String attr = e.nextElement();
				Vector<Object> v = this.attributeMap.get(attr);
				String value = v.firstElement().toString();
				String aname = attr;
				int index = attr.lastIndexOf("$");
				if (index > 0) {
					aname = attr.substring(index + 1);
				}
				sb.append(aname + "=" + value);
				if (e.hasMoreElements()) {
					sb.append(",");
				}
			}
			String rule = (String) this.getAttribute("rule");
			if (rule != null && rule.length() > 2) {
				sb.append(",Rule=" + rule);
			}
			sb.append("]");
		}
		if (this.getSpans() != null) {
			for (Span span : this.getSpans()) {
				sb.append("<" + span.getTextStart() + "-" + span.getTextEnd()
						+ ">");
			}
		}
		if (this.getMismatchType() != null) {
			sb.append("*" + this.getMismatchType() + "*");
		}
		if (this.getTouchingAnnotations() != null) {
			for (EVAnnotation touching : this.getTouchingAnnotations()) {
				if (!touching.isVisited()) {
					sb.append("->(TOUCHES: ");
					sb.append(touching.getToolTip(false));
					sb.append(")");
				}
			}
		}
		this.resetVisited();
		return sb.toString();
	}

	public boolean hasNonEmptyClassification() {
		return this.getClassification() != null
				&& !this.getClassification().isEmpty();
	}

	public Vector<String> getAttributeNames() {
		return attributeNames;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public Vector<EVAnnotation> getTouchingAnnotations() {
		return touchingAnnotations;
	}

	public void addTouchingAnnotation(EVAnnotation touching) {
		this.touchingAnnotations = VUtils.addIfNot(this.touchingAnnotations,
				touching);
	}

	public void clearTouchingAnnotation() {
		this.touchingAnnotations = null;
	}

	public boolean belongsTo(AnnotationCollection ac) {
		return this.getAnnotationCollection().equals(ac);
	}

	public Vector<EVAnnotation> getAllMatchingAnnotations() {
		return allMatchingAnnotations;
	}

}
