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
package workbench.api.annotation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import annotation.EVAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.typesystem.ClassificationInstance;
import workbench.api.typesystem.Type;

public class AnnotationCollection {

	private AnnotationEvent annotationEvent = null;
	private Annotator annotator = null;
	private Vector<Annotation> annotations = new Vector(0);
	private Vector<Annotation> snippetAnnotations = null;
	private HashMap<String, Annotation> annotationIDMap = new HashMap();
	private HashMap<String, ClassificationInstance> classificationIDMap = new HashMap();
	private Analysis analysis = null;
	private String annotatorID = null;
	private String annotatorName = null;
	private String sourceTextName = null;
	private boolean isPrimary = false;

	public AnnotationCollection() {
		int x = 1;
	}

	public AnnotationCollection(annotation.AnnotationCollection ac,
			AnnotationEvent ae, boolean isPrimary) {
		this.annotationEvent = ae;
		this.isPrimary = isPrimary;
		if (ac != null && ac.getAnnotations() != null) {
			for (EVAnnotation eva : ac.getAnnotations()) {
				if (eva.isDocumentType()) {
					continue;
				}
				String tname = eva.getClassification().getParentAnnotationType()
						.getName();
				Type type = ae.getAnalysis().getTypeSystem().getType(tname);
				new Annotation(this, type, eva);
			}
			Collections.sort(this.annotations, new Annotation.PositionSorter());
			this.snippetAnnotations = Annotation
					.gatherSnippetAnnotations(this.annotations);
			Collections.sort(this.snippetAnnotations,
					new Annotation.PositionSorter());
		}
	}

	public Annotation getAnnotationByID(String id) {
		return this.annotationIDMap.get(id);
	}

	public void addAnnotation(Annotation annotation, String id) {
		this.annotations = VUtils.add(this.annotations, annotation);
		if (id != null) {
			this.annotationIDMap.put(id, annotation);
		}
	}

	public Object getClassificationInformationByID(String id) {
		return classificationIDMap.get(id);
	}

	public void storeClassificationInformationByID(String id,
			ClassificationInstance ci) {
		this.classificationIDMap.put(id, ci);
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public AnnotationEvent getAnnotationEvent() {
		return annotationEvent;
	}

	public Annotator getAnnotator() {
		return annotator;
	}

	public void setAnnotator(Annotator annotator) {
		this.annotator = annotator;
		this.isPrimary = (annotator.isPrimary() ? true : false);
	}

	public Vector<Annotation> getAnnotations() {
		return annotations;
	}

	public Vector<Annotation> getSnippetAnnotations() {
		if (this.snippetAnnotations == null && this.annotations != null) {
			this.snippetAnnotations = Annotation
					.gatherSnippetAnnotations(this.annotations);
			if (this.snippetAnnotations != null) {
				Collections.sort(this.snippetAnnotations,
						new Annotation.DocumentPositionSorter());
			}
		}
		return this.snippetAnnotations;
	}

	private static int[] SpanOverlapArray = new int[128000];

	public static int getAnnotationOverlap(Annotation annotation1,
			Annotation annotation2) {
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
			for (int i = span.getStart(); i <= span.getEnd(); i++) {
				int so = i - start;
				SpanOverlapArray[so]++;
			}
		}
		for (Span span : annotation2.getSpans()) {
			for (int i = span.getStart(); i <= span.getEnd(); i++) {
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

	public static boolean hasStrictOverlap(Annotation a1, Annotation a2) {
		if (a1.getSpans() == null || a2.getSpans() == null
				|| a1.getSpans().size() != a2.getSpans().size()) {
			return false;
		}
		for (int i = 0; i < a1.getSpans().size(); i++) {
			Span s1 = a1.getSpans().elementAt(i);
			Span s2 = a2.getSpans().elementAt(i);
			if (s1.getStart() != s2.getStart() || s1.getEnd() != s2.getEnd()) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasOverlap(Annotation a1, Annotation a2) {
		int overlap = getAnnotationOverlap(a1, a2);
		return overlap > 0;
	}

	public Annotation getClosestMatchingAnnotation(int offset) {
		Vector<Annotation> matches = getAnnotationsByOffset(offset);
		if (matches != null) {
			if (matches.size() == 1) {
				return matches.firstElement();
			}
			Annotation closest = null;
			int smallestDistance = 1000000;
			for (Annotation match : matches) {
				int dist = Math.abs(match.getStart() - offset)
						+ Math.abs(match.getEnd() - offset);
				if (dist < smallestDistance) {
					smallestDistance = dist;
					closest = match;
				}
			}
			return closest;
		}
		return null;
	}

	public Vector<Annotation> getAnnotationsByOffset(int offset) {
		Vector<Annotation> annotations = this.getAnnotations();
		Vector<Annotation> matches = null;
		if (annotations != null) {
			boolean foundMatch = false;
			for (Annotation annotation : this.getAnnotations()) {
				if (annotation.touchedByCursor(offset)) {
					foundMatch = true;
					matches = VUtils.add(matches, annotation);
				} else if (foundMatch) {
					break;
				}
			}
		}
		return matches;
	}
	
	//11/10/2015
	public void gatherOverlappingAnnotationGroups() {
		if (this.annotations != null) {
			Annotation start = null;
			for (Annotation current : this.annotations) {
				current.setOverlapping(null);
				if (start != null) {
					if (hasOverlap(start, current)) {
						if (start.getOverlapping() != null) {
							for (Annotation o : start.getOverlapping()) {
								o.addOverlapping(current);
							}
						}
						start.addOverlapping(current);
					} else {
						start = current;
					}
				} else {
					start = current;
				}
			}
		}
	}

	public Document getDocument() {
		if (this.annotationEvent != null) {
			return this.annotationEvent.getDocument();
		}
		return null;
	}

	public void setAnnotationEvent(AnnotationEvent annotationEvent) {
		this.annotationEvent = annotationEvent;
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}

	public void setAnnotatorID(String id, String name) {
		this.annotatorID = id;
		this.annotatorName = name;
	}

	public String getAnnotatorID() {
		return annotatorID;
	}

	public String getAnnotatorName() {
		return this.annotatorName;
	}

	public void setAnnotatorName(String annotatorName) {
		this.annotatorName = annotatorName;
	}

	public String getSourceTextName() {
		return sourceTextName;
	}

	public void setSourceTextName(String sourceTextName) {
		this.sourceTextName = sourceTextName;
	}

	public String toString() {
		String type = (this.isPrimary ? "Primary" : "Secondary");
		return "<Type=" + type + ",Doc=" + this.getDocument().getName() + ">";
	}
	
	public static class AnnotationCollectionTypeSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			AnnotationCollection ac1 = (AnnotationCollection) o1;
			AnnotationCollection ac2 = (AnnotationCollection) o2;
			if (ac1.isPrimary()) {
				return -1;
			}
			if (ac2.isPrimary()) {
				return 1;
			}
			return 0;
		}
	}

}
