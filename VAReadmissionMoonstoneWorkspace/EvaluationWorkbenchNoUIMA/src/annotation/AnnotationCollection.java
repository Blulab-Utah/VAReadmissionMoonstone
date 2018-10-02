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

import io.GrAF;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.VUtils;
import typesystem.TypeObject;
import typesystem.TypeSystem;
import workbench.arr.AnnotationAnalysis;
import workbench.arr.AnnotationEvent;
import workbench.arr.Annotator;

/**
 * The Class AnnotationCollection.
 */
public class AnnotationCollection {
	AnnotationAnalysis analysis = null;
	TypeSystem typeSystem = null;
	Document document = null;
	String xmlFileName = null;
	Annotator annotator = null;
	String annotatorID = null;
	String annotatorName = null;
	String sourceTextName = null;
	public Vector<EVAnnotation> annotations = null;
	public Vector<Classification> classifications = null;
	HashMap<String, EVAnnotation> annotationIDMap = new HashMap<String, EVAnnotation>();
	HashMap<String, Classification> classificationIDMap = new HashMap<String, Classification>();

	/**
	 * Map: Java Class representing an annotation level (Document, Snippet,
	 * Token) -> annotation objects at that level
	 */
	public Hashtable<typesystem.Annotation, Vector<EVAnnotation>> typeAnnotationMap = new Hashtable();

	/**
	 * Map: Classification key (cname, value) -> list of annotations with that
	 * classification
	 */
	Hashtable<String, Vector<EVAnnotation>> classificationAnnotationMap = new Hashtable();

	Hashtable<String, Vector<EVAnnotation>> allAVPairAnnotationMap = new Hashtable();

	/**
	 * The number of spans (used for generating a unique ID for each annotated
	 * text span
	 */
	int numberOfSpans = 0;
	boolean isPrimary = false;
	String annotatorType = "GrAF";
	boolean hasRelations = false;
	Object userObject = null;

	public AnnotationCollection() {
	}

	public AnnotationCollection(Document document) {
		this.setDocument(document);
	}

	public AnnotationCollection(AnnotationCollection ac) throws Exception {
		this.setDocument(ac.getDocument());
		this.setPrimary(ac.isPrimary());
		this.setAnalysis(ac.getAnalysis());
		this.setTypeSystem(ac.getTypeSystem());
	}

	public AnnotationCollection(AnnotationAnalysis analysis,
			String xmlFileName, String xmlString, Annotator annotator)
			throws Exception {
		this.analysis = analysis;
		this.xmlFileName = xmlFileName;
		this.typeSystem = analysis.getArrTool().getTypeSystem();
		if (xmlFileName != null) {
			GrAF.readXML(this, xmlString, annotator);
			wrapup();
			System.out.print(".");
		}
	}

	public void replace(AnnotationCollection newac) {
		if (newac != null) {
			AnnotationAnalysis analysis = this.getAnalysis();
			newac.setAnnotator(this.getAnnotator());
			Vector<AnnotationCollection> acs = this.getAnalysis()
					.getDocumentAnnotationCollectionMap()
					.get(this.getDocument());
			if (acs != null) {
				acs.remove(this);
				acs.add(newac);
				AnnotationEvent ae = analysis.getAnnotationEvent(this
						.getDocument());
				ae.resolveAnnotationCollections();
			}
		}
	}

	public int getNumberOfAnnotationsByType(TypeObject type) {
		Vector<EVAnnotation> annotations = typeAnnotationMap.get(type);
		if (annotations != null) {
			return annotations.size();
		}
		return 0;
	}

	public void storeAnalysisIndices() throws Exception {
		if (this.annotations != null) {
			Collections.sort(this.annotations,
					new EVAnnotation.PositionSorter());
			for (EVAnnotation annotation : this.annotations) {
				annotation.storeAnalysisIndices(this);
			}
		}
	}

	public void addAnnotation(String id, EVAnnotation annotation) {
		this.annotations = VUtils.add(this.annotations, annotation);
		annotationIDMap.put(id, annotation);
	}

	public EVAnnotation getAnnotation(String id) {
		return annotationIDMap.get(id);
	}

	public void addClassification(String id, Classification classification) {
		this.classifications = VUtils.add(this.classifications, classification);
		classificationIDMap.put(id, classification);
	}

	public Classification getClassification(String id) {
		return classificationIDMap.get(id);
	}

	public EVAnnotation createAnnotation(String name) throws Exception {
		EVAnnotation annotation = null;
		TypeSystem ts = this.getTypeSystem();
		TypeObject type = ts.getTypeObject(name);

		if (type == null && !ts.isUseOnlyTypeModel()) {
			ts.addToTypeSystem(name, null);
			type = ts.getTypeObject(name);
		}

		if (type instanceof typesystem.Annotation) {
			typesystem.Annotation annotationType = (typesystem.Annotation) type;
			String lname = annotationType.getJavaClass().getSimpleName();
			if ("CorpusAnnotation".equals(lname)) {
				annotation = new CorpusAnnotation(annotationType,
						this.getDocument());
			} else if ("DocumentAnnotation".equals(lname)) {
				annotation = new DocumentAnnotation(annotationType,
						this.getDocument());
			} else if ("GroupAnnotation".equals(lname)) {
				annotation = new GroupAnnotation(annotationType,
						this.getDocument());
			} else if ("UtteranceAnnotation".equals(lname)) {
				annotation = new UtteranceAnnotation(annotationType,
						this.getDocument());
			} else if ("SnippetAnnotation".equals(lname)) {
				annotation = new SnippetAnnotation(annotationType,
						this.getDocument());
			} else if ("TokenAnnotation".equals(lname)) {
				annotation = new TokenAnnotation(annotationType,
						this.getDocument());
			} else if ("WordAnnotation".equals(lname)) {
				annotation = new WordAnnotation(annotationType,
						this.getDocument());
			}
		}
		if (annotation != null) {
			annotation.setAnnotationCollection(this);
		}
		return annotation;
	}

	public void addAnnotationClassification(EVAnnotation annotation)
			throws Exception {
		Classification c = annotation.getClassification();
		if (c != null) {
			Hashtable chash = this.getAnalysis()
					.getAllAnnotationTypeClassificationMap();
			VUtils.pushIfNotHashVector(chash, annotation.getType(), c);
			Vector<String> keys = c.getClassificationAllValueKeys();
			if (keys != null) {
				for (String key : keys) {
					String annotatorName = (this.getAnnotator() != null ? this
							.getAnnotator().getName() : this.annotatorName);
					VUtils.pushIfNotHashVector(
							this.classificationAnnotationMap, key, annotation);
					Hashtable ahash = this.getAnalysis()
							.getAllClassificationAnnotationMap();
					VUtils.pushIfNotHashVector(ahash, key, annotation);
				}
			}
		}
	}

	// 8/15/2013
	public void removeAllAnnotationIndices() throws Exception {
		if (this.getAnnotations() != null) {
			for (EVAnnotation annotation : this.getAnnotations()) {
				removeAnnotationClassification(annotation);
			}
		}
	}

	public void removeAnnotationClassification(EVAnnotation annotation)
			throws Exception {
		Classification c = annotation.getClassification();
		if (c != null) {
			Vector<String> keys = c.getClassificationAllValueKeys();
			if (keys != null) {
				for (String key : keys) {
					Vector v = this.classificationAnnotationMap.get(key);
					if (v != null) {
						v.remove(annotation);
					}
					v = this.getAnalysis().getAllClassificationAnnotationMap()
							.get(key);
					if (v != null) {
						v.remove(annotation);
					}

					v = this.getAnalysis()
							.getAllAnnotationTypeClassificationMap()
							.get(annotation.getType());
					if (v != null) {
						v.remove(c);
					}
				}
			}
		}
	}

	public EVAnnotation getFirstAVPairAnnotation() throws Exception {
		if (this.analysis.isAttributeUserSelection()) {
			if (this.analysis.getSelectedAVPair() != null) {
				Vector<EVAnnotation> annotations = this.allAVPairAnnotationMap
						.get(this.analysis.getSelectedAVPair());
				if (annotations != null) {
					Collections.sort(annotations,
							new EVAnnotation.PositionSorter());
					return annotations.firstElement();
				}
			}
		}
		return null;
	}

	/**
	 * Given an avpair, get the first annotation listed for that pair.
	 */
	public EVAnnotation getFirstAnnotation(String attribute, String value)
			throws Exception {
		if (attribute != null && value != null) {
			String avpair = attribute + ":" + value;
			Vector<EVAnnotation> annotations = this.allAVPairAnnotationMap
					.get(avpair);
			if (annotations != null) {
				Collections
						.sort(annotations, new EVAnnotation.PositionSorter());
				return annotations.firstElement();
			}
		}
		return null;
	}

	/**
	 * Given a classification representing an annotation level (eg. Snippet,
	 * Document), get the first annotation listed for that level.
	 */

	public EVAnnotation getFirstAnnotation(Classification classification)
			throws Exception {
		String key = classification.getClassDisplayValueClassificationKey();
		if (key != null) {
			Vector<EVAnnotation> annotations = this.classificationAnnotationMap
					.get(key);
			if (annotations != null) {
				// 2/18/2013
				String error = null;
				EVAnnotation firstAnnotation = null;
				EVAnnotation firstWithSelectedError = null;
				EVAnnotation firstWithAnyError = null;
				for (EVAnnotation annotation : annotations) {
					if (firstAnnotation == null) {
						firstAnnotation = annotation;
					}
					if (error == null) {
						break;
					}
					if (error != null && annotation.getMismatchType() != null) {
						if (firstWithAnyError == null) {
							firstWithAnyError = annotation;
						}
						if (firstWithSelectedError == null
								&& annotation.getMismatchType().equals(error)) {
							firstWithSelectedError = annotation;
						}
					}
				}
				if (firstWithSelectedError != null) {
					return firstWithSelectedError;
				}
				if (firstWithAnyError != null) {
					return firstWithAnyError;
				}
				return firstAnnotation;
			}
		}
		return null;
	}

	public EVAnnotation findClosestAnnotation(EVAnnotation other)
			throws Exception {
		EVAnnotation closest = null;
		if (other != null) {
			String key = other.getClassification()
					.getClassDisplayValueClassificationKey();
			Vector<EVAnnotation> annotations = this.classificationAnnotationMap
					.get(key);
			if (annotations != null) {
				int otherStart = other.getStart();
				int mindist = 10000;
				for (EVAnnotation annotation : annotations) {
					int dist = Math.abs(annotation.getStart() - otherStart);
					if (dist < mindist) {
						mindist = dist;
						closest = annotation;
					} else if (closest != null) {
						break;
					}
				}
			}
		}
		if (closest == null) {
			Classification c = (other != null ? other.getClassification()
					: this.getAnalysis().getSelectedClassification());
			if (c != null) {
				closest = getFirstAnnotation(c);
			}
		}
		return closest;
	}

	// 9/5/2013: From VA
	public EVAnnotation findMatchingAnnotation(String cname, int start, int end)
			throws Exception {
		for (EVAnnotation annotation : annotations) {
			int startdist = Math.abs(annotation.getStart() - start);
			int enddist = Math.abs(annotation.getEnd() - end);
			if (startdist == 0 && enddist == 0) {
				annotation.Classification c = annotation.getClassification();
				if (c.containsProperty(cname)) {
					return annotation;
				}
			}
		}
		return null;
	}

	public static void processMatchingAnnotations(
			Vector<EVAnnotation> annotations1,
			Vector<EVAnnotation> annotations2, boolean strict) throws Exception {
		if (annotations1 != null) {
			for (EVAnnotation annotation1 : annotations1) {

				// 2/23/2013
				annotation1.matchedAnnotation = null;
				annotation1.allMatchingAnnotations = null;

				EVAnnotation matchedSecond = null;
				int maxOverlap = -1;
				if (annotations2 != null) {
					for (EVAnnotation annotation2 : annotations2) {
						if (annotation2.getSpans() == null
								|| annotation2.getEnd() < annotation1
										.getStart()) {
							continue;
						}

						// Too far: If a2's start > a1's end, break;
						if (annotation2.getStart() > annotation1.getEnd()) {
							break;
						}

						int overlap = getAnnotationOverlap(annotation1,
								annotation2);
						boolean hasStrictOverlap = hasStrictOverlap(
								annotation1, annotation2);
						Classification c1 = annotation1.getClassification();
						Classification c2 = annotation2.getClassification();
						if (c1 != null
								&& c2 != null
								&& c1.equals(c2)
								&& ((strict && hasStrictOverlap) || (!strict && overlap > 0))) {
							annotation1.allMatchingAnnotations = VUtils
									.addIfNot(
											annotation1.allMatchingAnnotations,
											annotation2);
							annotation2.allMatchingAnnotations = VUtils
									.addIfNot(
											annotation2.allMatchingAnnotations,
											annotation1);
							if (maxOverlap < overlap) {
								maxOverlap = overlap;
								annotation1.setMatchedAnnotation(annotation2);
								annotation2.setMatchedAnnotation(annotation1);
							} else if (maxOverlap == overlap) {
								float closeness1 = annotation1
										.attributeSimilarity(matchedSecond);
								float closeness2 = annotation1
										.attributeSimilarity(annotation2);
								if (closeness2 > closeness1) {
									annotation1
											.setMatchedAnnotation(annotation2);
									annotation2
											.setMatchedAnnotation(annotation1);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void discoverTouchingAnnotations(
			Vector<EVAnnotation> annotations) throws Exception {
		if (annotations != null) {
			EVAnnotation lastAnnotation = null;
			for (EVAnnotation annotation : annotations) {
				if (lastAnnotation != null) {
					typesystem.Classification lpc = lastAnnotation
							.getClassification().getParentClassification();
					typesystem.Classification pc = annotation
							.getClassification().getParentClassification();
					int overlap = getAnnotationOverlap(lastAnnotation,
							annotation);
					if (overlap > 0 && pc.equals(lpc)) {
						lastAnnotation.touchingAnnotations = VUtils.addIfNot(
								lastAnnotation.touchingAnnotations, annotation);
						annotation.touchingAnnotations = VUtils.addIfNot(
								annotation.touchingAnnotations, lastAnnotation);
					}
				}
				lastAnnotation = annotation;
			}
		}
	}

	// 2/5/2014: No match criteria. Filter the results out upstream.
	public static Vector<Vector> getMatchingAnnotations(
			typesystem.Annotation type, AnnotationCollection ac1,
			AnnotationCollection ac2) throws Exception {
		if (ac1 == null || ac2 == null) {
			return null;
		}
		Vector<EVAnnotation> annotations1 = ac1.typeAnnotationMap.get(type);
		Vector<EVAnnotation> annotations2 = ac2.typeAnnotationMap.get(type);
		Vector<Vector> matching = null;
		if (annotations1 != null) {
			for (EVAnnotation annotation1 : annotations1) {
				EVAnnotation matchedSecond = null;
				int maxOverlap = -1;
				if (annotations2 != null) {
					for (EVAnnotation annotation2 : annotations2) {
						int overlap = AnnotationCollection
								.getAnnotationOverlap(annotation1, annotation2);
						boolean hasStrictOverlap = AnnotationCollection
								.hasStrictOverlap(annotation1, annotation2);
						boolean requiresStrictOverlap = ac1.getAnalysis()
								.getArrTool().isStrictMatchCriterion();
						if ((requiresStrictOverlap && hasStrictOverlap)
								|| (!requiresStrictOverlap && overlap > 0)) {
							annotation2.setVisited();
							if (maxOverlap < overlap) {
								maxOverlap = overlap;
								matchedSecond = annotation2;
							} else if (maxOverlap == overlap) {
								float closeness1 = annotation1
										.attributeSimilarity(matchedSecond);
								float closeness2 = annotation1
										.attributeSimilarity(annotation2);
								if (closeness2 > closeness1) {
									matchedSecond = annotation2;
								}
							}
						} else if (matchedSecond != null) { // 2/5/2014
							break;
						}
					}
				}
				Vector v = VUtils.listify(annotation1);
				v.add(matchedSecond);
				matching = VUtils.add(matching, v);
			}
		}
		if (annotations2 != null) {
			for (EVAnnotation annotation2 : annotations2) {
				if (!annotation2.isVisited()) {
					Vector<EVAnnotation> v = VUtils.listify(null);
					v.add(annotation2);
					matching = VUtils.add(matching, v);
				}
				annotation2.resetVisited();
			}
		}
		return matching;
	}

	public static Vector<Vector> getMatchingAnnotations(
			typesystem.Annotation type, Classification classification,
			AnnotationCollection ac1, AnnotationCollection ac2)
			throws Exception {
		if (ac1 == null || ac2 == null) {
			return null;
		}
		String key = null;
		Vector<EVAnnotation> annotations1 = null;
		Vector<EVAnnotation> annotations2 = null;
		Vector<Vector> matching = null;
		if (classification != null) {
			key = classification.getClassDisplayValueClassificationKey();
			if (key != null) {
				annotations1 = ac1.getIndexedAnnotations(classification);
				annotations2 = ac2.getIndexedAnnotations(classification);
			}
		} else {
			annotations1 = ac1.typeAnnotationMap.get(type);
			annotations2 = ac2.typeAnnotationMap.get(type);
		}
		if (annotations1 != null) {
			for (EVAnnotation annotation1 : annotations1) {
				EVAnnotation matchedSecond = null;
				int maxOverlap = -1;
				if (annotations2 != null) {
					for (EVAnnotation annotation2 : annotations2) {
						int overlap = getAnnotationOverlap(annotation1,
								annotation2);
						boolean hasStrictOverlap = hasStrictOverlap(
								annotation1, annotation2);
						boolean requiresStrictOverlap = ac1.getAnalysis()
								.getArrTool().isStrictMatchCriterion();
						Classification c1 = annotation1.getClassification();
						Classification c2 = annotation2.getClassification();
						if (c1 != null
								&& c2 != null
								&& c1.equals(c2)
								&& ((requiresStrictOverlap && hasStrictOverlap) || (!requiresStrictOverlap && overlap > 0))) {
							annotation2.setVisited();
							if (maxOverlap < overlap) {
								maxOverlap = overlap;
								matchedSecond = annotation2;
							} else if (maxOverlap == overlap) {
								float closeness1 = annotation1
										.attributeSimilarity(matchedSecond);
								float closeness2 = annotation1
										.attributeSimilarity(annotation2);
								if (closeness2 > closeness1) {
									matchedSecond = annotation2;
								}
							}
						}
					}
				}
				Vector v = VUtils.listify(annotation1);
				v.add(matchedSecond);
				matching = VUtils.add(matching, v);
			}
		}
		if (annotations2 != null) {
			for (EVAnnotation annotation2 : annotations2) {
				if (!annotation2.isVisited()) {
					Vector<EVAnnotation> v = VUtils.listify(null);
					v.add(annotation2);
					matching = VUtils.add(matching, v);
				}
				annotation2.resetVisited();
			}
		}
		return matching;
	}

	public static Vector<Vector> getMatchingAnnotationsSimpleOverlap(
			typesystem.Annotation type, AnnotationCollection ac1,
			AnnotationCollection ac2) throws Exception {
		if (type == null || ac1 == null || ac2 == null) {
			return null;
		}
		Vector<EVAnnotation> annotations1 = ac1.typeAnnotationMap.get(type);
		Vector<EVAnnotation> annotations2 = ac2.typeAnnotationMap.get(type);
		if (annotations1 == null || annotations2 == null) {
			return null;
		}
		boolean isStrict = ac1.getAnalysis().getArrTool()
				.isStrictMatchCriterion();
		Vector<Vector> matching = getSimpleOverlapMatchingAnnotations(
				annotations1, annotations2, !isStrict);
		return matching;
	}

	public static Vector<Vector> getSimpleOverlapMatchingAnnotations(
			Vector<EVAnnotation> annotations1,
			Vector<EVAnnotation> annotations2, boolean allowRelaxed)
			throws Exception {
		Vector<Vector> matching = null;
		for (EVAnnotation annotation1 : annotations1) {
			if (annotation1.getSpans() == null) {
				continue;
			}
			EVAnnotation matchedSecond = null;
			int maxOverlap = -1;
			if (matchedSecond == null) {
				for (EVAnnotation annotation2 : annotations2) {

					// Catch up: If a2's end < a1's start, search forward.
					if (annotation2.getSpans() == null
							|| annotation2.getEnd() < annotation1.getStart()) {
						continue;
					}

					// Too far: If a2's start > a1's end, break;
					if (annotation2.getStart() > annotation1.getEnd()) {
						break;
					}
					boolean isMatched = false;
					boolean isStrictMatched = false;
					boolean isRelaxMatched = false;
					int overlap = 0;
					if (!allowRelaxed
							&& hasStrictOverlap(annotation1, annotation2)) {
						overlap = getAnnotationOverlap(annotation1, annotation2);
						isStrictMatched = true;
					} else {
						overlap = getAnnotationOverlap(annotation1, annotation2);
						if (allowRelaxed && overlap > 0) {
							isRelaxMatched = true;
						}
					}
					if (isStrictMatched || (allowRelaxed && isRelaxMatched)) {
						isMatched = true;
					}
					if (isMatched) {

						String v1 = annotation1.getClassification().getValue();
						String v2 = annotation2.getClassification().getValue();
						if (!v1.equals(v2)) {
							int x = 1;
							x = x;
						}

						annotation2.setVisited();
						if (overlap > maxOverlap) {
							maxOverlap = overlap;
							matchedSecond = annotation2;
						}
					}
				}
			}
			Vector<EVAnnotation> v = VUtils.listify(annotation1);
			v.add(matchedSecond);
			matching = VUtils.add(matching, v);
		}
		for (EVAnnotation annotation2 : annotations2) {
			if (!annotation2.isVisited()) {
				Vector<EVAnnotation> v = VUtils.listify(null);
				v.add(annotation2);
				matching = VUtils.add(matching, v);
			}
			annotation2.resetVisited();
		}
		return matching;
	}

	public Vector<EVAnnotation> getIndexedAnnotations(Classification c) {
		Vector<EVAnnotation> annotations = null;
		String key = c.getName() + ":" + c.getValue();
		annotations = this.classificationAnnotationMap.get(key);
		return annotations;
	}

	// 2/14/2013: INCORRECT!! COULD HAVE DIFFERENT # OF SPANS, ETC.
	static int getAnnotationOverlapOLD(EVAnnotation annotation1,
			EVAnnotation annotation2) {
		if (annotation1.getSpans() == null
				|| annotation2.getSpans() == null
				|| annotation1.getSpans().size() != annotation2.getSpans()
						.size()) {
			return 0;
		}
		int overlap = 0;
		for (Span s1 : annotation1.getSpans()) {
			for (Span s2 : annotation2.getSpans()) {
				overlap += Span.getOverlap(s1, s2);
			}
		}
		return overlap;
	}

	private static int[] SpanOverlapArray = new int[128000];

	// 2/13/2013
	public static int getAnnotationOverlap(EVAnnotation annotation1,
			EVAnnotation annotation2) {
		int overlap = 0;
		int start = 0;
		int end = 0;
		int length = 0;
		if (annotation1.getEnd() <= annotation2.getStart()
				|| annotation2.getEnd() <= annotation1.getStart()) {
			return 0;
		}
		if (annotation1.getStart() < annotation2.getStart()) {
			start = annotation1.getStart();
		} else {
			start = annotation2.getStart();
		}
		if (annotation1.getEnd() > annotation2.getEnd()) {
			end = annotation1.getEnd();
		} else {
			end = annotation2.getEnd();
		}
		length = end - start + 1;
		for (Span span : annotation1.getSpans()) {
			for (int i = span.getTextStart(); i <= span.getTextEnd(); i++) {
				int so = i - start;
				SpanOverlapArray[so]++;
			}
		}
		for (Span span : annotation2.getSpans()) {
			for (int i = span.getTextStart(); i <= span.getTextEnd(); i++) {
				int so = i - start;
				SpanOverlapArray[so]++;
			}
		}
		for (int i = 0; i < length; i++) {
			if (SpanOverlapArray[i] == 2) {
				overlap++;
			}
			SpanOverlapArray[i] = 0;
		}
		return overlap;
	}

	public static boolean hasStrictOverlap(EVAnnotation a1, EVAnnotation a2) {
		if (a1.getSpans() == null || a2.getSpans() == null
				|| a1.getSpans().size() != a2.getSpans().size()) {
			return false;
		}
		for (int i = 0; i < a1.getSpans().size(); i++) {
			Span s1 = a1.getSpans().elementAt(i);
			Span s2 = a2.getSpans().elementAt(i);
			if (s1.getTextStart() != s2.getTextStart()
					|| s1.getTextEnd() != s2.getTextEnd()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the document.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the document.
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * Gets the annotations.
	 */
	public Vector<EVAnnotation> getAnnotations() {
		return annotations;
	}

	public Vector<EVAnnotation> getNonDuplicateAnnotations() throws Exception {
		Vector<EVAnnotation> duplicates = this.getDuplicateAnnotations();
		Vector<EVAnnotation> nonduplicates = null;
		if (duplicates != null) {
			for (EVAnnotation duplicate : duplicates) {
				duplicate.setVisited();
			}
			for (EVAnnotation annotation : this.getAnnotations()) {
				if (!annotation.isVisited()) {
					nonduplicates = VUtils.add(nonduplicates, annotation);
				} else {
					annotation.resetVisited();
				}
			}
		} else {
			nonduplicates = this.getAnnotations();
		}
		return nonduplicates;
	}

	public Vector<EVAnnotation> getDuplicateAnnotations() throws Exception {
		Hashtable<String, Vector<EVAnnotation>> ahash = new Hashtable();
		Vector<EVAnnotation> duplicates = null;
		AnnotationCollection.discoverTouchingAnnotations(this.getAnnotations());
		if (this.getAnnotations() != null) {
			for (EVAnnotation annotation : this.getAnnotations()) {
				if (annotation.getTouchingAnnotations() != null) {
					String key = annotation.getStart() + ":"
							+ annotation.getEnd();
					VUtils.pushIfNotHashVector(ahash, key, annotation);
				}
			}
		}
		for (Enumeration<String> e = ahash.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			Vector<EVAnnotation> touching = ahash.get(key);
			if (touching.size() > 1) {
				Collections.sort(touching, new EVAnnotation.PositionSorter());
				for (int i = 1; i < touching.size(); i++) {
					EVAnnotation duplicate = touching.elementAt(i);
					duplicates = VUtils.add(duplicates, duplicate);
				}
			}
		}
		return duplicates;
	}

	public Vector<Vector> getAnnotationsBySelection() throws Exception {
		boolean showAttribute = false;
		boolean showLevel = false;
		Object selection = null;
		if (analysis.isAnnotationTypeUserSelection()) {
			showLevel = true;
			if (analysis.getSelectedAnnotation() != null) {
				selection = analysis.getSelectedAnnotation()
						.getClassification();
			}
		} else if (analysis.isAttributeUserSelection()) {
			showAttribute = true;
			selection = analysis.getSelectedAVPair();
		}
		Vector<EVAnnotation> sameClass = null;
		Vector<EVAnnotation> sameType = null;
		boolean doItAnyways = true;
		if ((selection != null || doItAnyways)
				&& this.annotations != null) {
			if (showLevel) {
				for (EVAnnotation annotation : annotations) {
					Classification c = annotation.getClassification();
					if (c.equals(selection)) {
						sameClass = VUtils.add(sameClass, annotation);
					} else if (annotation.getType().equals(
							analysis.getSelectedLevel())) {
						sameType = VUtils.add(sameType, annotation);
					}
				}
			} else if (showAttribute) {
				sameClass = this.getAVPairAnnotations((String) selection);
			}
		}
		Vector<Vector> result = new Vector(0);
		result.add(sameClass);
		result.add(sameType);
		return result;
	}

	public SnippetAnnotation addSnippet(typesystem.Classification pclass,
			String cname, String cvalue, int textStart, int textEnd)
			throws Exception {
		if (pclass == null) {
			return null;
		}
		SnippetAnnotation snippet = new SnippetAnnotation();
		snippet.setType(pclass.getParentTypeObject());
		snippet.setAnnotationCollection(this);
		snippet.setId(null);
		snippet.addSpan(textStart, textEnd);
		Classification classInstance = new Classification(this, snippet,
				pclass, cname, cvalue, null);
		snippet.setClassification(classInstance);
		this.addAnnotation(snippet.getId(), snippet);
		snippet.getSpans().firstElement().setAnnotationCollection(this);
		snippet.storeAnalysisIndices(this);
		return snippet;
	}

	/**
	 * Sets the annotations.
	 * 
	 */
	public void setAnnotations(Vector<EVAnnotation> annotations) {
		this.annotations = annotations;
	}

	/**
	 * Gets the annotator.
	 */
	public Annotator getAnnotator() {
		return annotator;
	}

	/**
	 * Sets the annotator.
	 * 
	 */
	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
	}

	/**
	 * Gets the analysis which contains this annotation collection.
	 */
	public AnnotationAnalysis getAnalysis() {
		return analysis;
	}

	/**
	 * Sets the analysis.
	 */
	public void setAnalysis(AnnotationAnalysis analysis) {
		this.analysis = analysis;
	}

	/**
	 * Gets the annotation id map.
	 */
	public HashMap<String, EVAnnotation> getAnnotationIDMap() {
		return annotationIDMap;
	}

	/**
	 * Gets the closest matching annotation to the provided offset, of the
	 * provided Class (i.e. annotation level)
	 */

	public EVAnnotation getClosestMatchingAnnotation(int offset)
			throws Exception {
		Vector<EVAnnotation> matches = getAnnotationsByOffset(offset);
		if (matches != null) {
			// 11/19/2013
			for (EVAnnotation match : matches) {
				match.clearTouchingAnnotation();
			}
			if (matches.size() == 1) {
				return matches.firstElement();
			}
			EVAnnotation closest = null;
			int smallestDistance = 1000000;
			for (EVAnnotation match : matches) {
				int dist = Math.abs(match.getStart() - offset)
						+ Math.abs(match.getEnd() - offset);
				if (dist < smallestDistance) {
					smallestDistance = dist;
					closest = match;
				}
			}
			for (EVAnnotation match : matches) {
				if (!closest.equals(match)) {
					closest.addTouchingAnnotation(match);
				}
			}
			return closest;
		}
		return null;
	}

	/**
	 * Returns all the annotations, belonging to the specified class, that cover
	 * the indicated offset.
	 */

	public Vector<EVAnnotation> getAnnotationsByOffset(int offset)
			throws Exception {
		Vector<EVAnnotation> annotations = this.getAnnotations();
		Vector<EVAnnotation> matches = null;
		if (annotations != null) {
			boolean foundMatch = false;
			for (EVAnnotation annotation : this.getAnnotations()) {
				if (annotation.touchedByCursor(offset)) {
					boolean levelIsNonterminal = this.analysis
							.getSelectedLevel().hasComponents();
					boolean annotationTypeIsNonterminal = annotation.getType()
							.hasComponents();
					// 8/30/2012: Shouldn't have to do this. Need separate
					// document / semantic distinctions.
					boolean sameTerminality = ((annotationTypeIsNonterminal && levelIsNonterminal) || (!annotationTypeIsNonterminal && !levelIsNonterminal));
					if (sameTerminality) {
						foundMatch = true;
						matches = VUtils.add(matches, annotation);
					}
				} else if (foundMatch) {
					break;
				}
			}
		}
		return matches;
	}

	public void addAVPairAnnotation(EVAnnotation annotation, String attribute,
			String value) throws Exception {
		if (attribute != null && value != null) {
			String avpair = attribute + ":" + value;
			VUtils.pushIfNotHashVector(this.allAVPairAnnotationMap, avpair,
					annotation);
			VUtils.pushIfNotHashVector(this.allAVPairAnnotationMap, attribute,
					annotation);
		}
	}

	public Vector<EVAnnotation> getAVPairAnnotations(String avstr)
			throws Exception {
		Vector<EVAnnotation> v = null;
		if (avstr != null) {
			v = this.allAVPairAnnotationMap.get(avstr);
			if (v != null) {
				Collections.sort(v, new EVAnnotation.PositionSorter());
			}
		}
		return v;
	}

	/**
	 * Gets the number of spans.
	 */
	public int getNumberOfSpans() {
		return numberOfSpans;
	}

	/**
	 * Sets the number of spans.
	 */
	public void setNumberOfSpans(int numberOfSpans) {
		this.numberOfSpans = numberOfSpans;
	}

	/**
	 * Gets the GrAF annotation file name.
	 */
	public String getXmlFileName() {
		return xmlFileName;
	}

	/**
	 * Sets the GrAF annotation file name.
	 * 
	 */
	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	/**
	 * After AnnotationCollection is created: sort all annotations by document
	 * position.
	 */
	public void wrapup() {
		Vector<EVAnnotation> annotations = this.getAnnotations();
		if (annotations != null) {
			Collections.sort(annotations, new PositionSorter());
		}
		for (Enumeration e = typeAnnotationMap.keys(); e.hasMoreElements();) {
			Object o = e.nextElement();
			annotations = (Vector<EVAnnotation>) typeAnnotationMap.get(o);
			if (annotations != null) {
				Collections.sort(annotations, new PositionSorter());
			}
		}
	}

	/**
	 * The Class PositionSorter.
	 */
	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			EVAnnotation a1 = (EVAnnotation) o1;
			EVAnnotation a2 = (EVAnnotation) o2;
			if (a1.getStart() < a2.getStart()) {
				return -1;
			}
			if (a1.getStart() > a2.getStart()) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * Checks whether this AnnotationCollection belongs to the first,
	 * "gold standard" annotator.
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * Sets whether this AnnotationCollection belongs to the first,
	 * "gold standard" annotator.
	 * 
	 */
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public TypeSystem getTypeSystem() throws Exception {
		if (this.typeSystem == null) {
			if (this.getAnalysis() != null) {
				this.typeSystem = this.getAnalysis().getArrTool()
						.getTypeSystem();
			}
		}
		return typeSystem;
	}

	public void setTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public String getAnnotorType() {
		return this.annotatorType;
	}

	public String getAnnotatorType() {
		return annotatorType;
	}

	public void setAnnotatorType(String annotatorType) {
		this.annotatorType = annotatorType;
	}

	public String getAnnotatorID() {
		return annotatorID;
	}

	public String getAnnotatorName() {
		return annotatorName;
	}

	public void setAnnotatorID(String id, String name) {
		this.annotatorID = id;
		this.annotatorName = name;
	}

	public String getSourceTextName() {
		return sourceTextName;
	}

	public void setSourceTextName(String sourceTextName) {
		this.sourceTextName = sourceTextName;
	}

	public Vector<Classification> getClassifications() {
		return classifications;
	}

	public boolean hasRelations() {
		return hasRelations;
	}

	public void setHasRelations(boolean hasRelations) {
		this.hasRelations = hasRelations;
	}

	public void printACInformation() {
		if (this.annotations != null) {
			System.out.println("Annotator=" + this.annotatorID + ",Document="
					+ this.document.getName());
			for (EVAnnotation annotation : this.annotations) {
				System.out.println("\tClassification: "
						+ annotation.getClassification());
				System.out.println("\tAttributes: "
						+ annotation.getAttributes());
			}
		}
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public String toString() {
		String str = "<AC: Document="
				+ (this.getDocument() != null ? this.getDocument().getName()
						: "*") + ",Annotator=" + this.annotatorName + ">";
		return str;
	}

}
