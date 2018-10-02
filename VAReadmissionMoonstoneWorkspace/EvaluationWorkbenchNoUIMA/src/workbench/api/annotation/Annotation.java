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
import java.util.Hashtable;
import java.util.Vector;

import annotation.EVAnnotation;
import annotation.RelationObject;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.variable.Variable;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.WorkbenchAPIObject;
import workbench.api.constraint.JavaFunctions;
import workbench.api.input.knowtator.KTAnnotation;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Relation;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class Annotation extends WorkbenchAPIObject {

	private Type type = null;
	private AnnotationCollection annotationCollection = null;
	private String id = null;
	private int level = -1;
	private String text = null;
	private Vector<Span> spans = null;
	private int start = -1;
	private int end = -1;
	private boolean isVisited = false;
	private Hashtable<Attribute, Object> attributeHash = new Hashtable();
	private Vector<Variable> variables = null;
	private Object classificationValue = null;
	private boolean isVerified = true;
	private boolean isVerifiedTrue = true;
	private Vector<OverlappingAnnotationPair> overlappingAnnotationPairs = null;
	private Vector<Annotation> matchingAnnotations = null;
	private KTAnnotation ktAnnotation = null;
	private Vector<Relation> relations = null;
	private Vector<Annotation> overlapping = null;

	// 11/10/2015
	private boolean containsClassificationMatch = false;
	
	// 2/15/2018
	public boolean isValidatedPrimaryAnnotation = false;

	public static int DocumentLevel = 0;
	public static int SnippetLevel = 1;

	// 10/13/2016:  Added "name" to parent type.  Not sure using type's name as
	// annotation name makes any sense...
	public Annotation(AnnotationCollection ac, Type type) {
		super(type.getName());
		this.annotationCollection = ac;
		this.type = type;
		this.addVariable("?annotation", this);
		if (type.isDocumentType()) {
			this.level = DocumentLevel;
		}
	}

	public Annotation(AnnotationCollection ac, Type type, EVAnnotation eva) {
		super(type.getName());
		this.type = type;
		this.annotationCollection = ac;
		ac.addAnnotation(this, null);
		this.getAnalysis().getOldToNewAnnotationHash().put(eva, this);
		this.addVariable("?annotation", this);
		if (eva.getClassification() != null) {
			annotation.Classification c = eva.getClassification();
			String aname = TypeSystem.eliminatePrepend(c.getName());
			Attribute attribute = type.getAttribute(aname);
			Object value = c.getValue();
			this.attributeHash.put(attribute, value);
			this.classificationValue = (String) value;
		}
		if (eva.getAttributes() != null) {
			for (String aname : eva.getAttributes()) {
				String value = (String) eva.getAttribute(aname);
				aname = TypeSystem.eliminatePrepend(aname);
				Attribute attribute = type.getAttribute(aname);
				if (attribute != null) {
					this.attributeHash.put(attribute, value);
				}
			}
		}
		if (eva.getSpans() != null) {
			for (annotation.Span evspan : eva.getSpans()) {
				this.addSpan(evspan.getTextStart(), evspan.getTextEnd());
			}
		}
	}

	public void putAttributeValue(Attribute attribute, Object value) {
		this.attributeHash.put(attribute, value);
	}

	public void putAttributeValue(String aname, Object value) {
		Attribute attr = this.getType().getAttribute(aname);
		if (attr != null) {
			this.attributeHash.put(attr, value);
		}

	}

	public Object getAttributeValue(Attribute attribute) {
		if (attribute != null) {
			return this.attributeHash.get(attribute);
		}
		return null;
	}

	public Object getAttributeValue(String aname) {
		Attribute attr = this.getType().getAttribute(aname);
		if (attr != null) {
			return this.attributeHash.get(attr);
		}
		return null;
	}

	public Vector<Attribute> getAllAttributes() {
		return HUtils.getKeys(this.attributeHash);
	}

	public void addSpan(int start, int end) {
		Span span = new Span(this, start, end);
		this.spans = VUtils.add(this.spans, span);
		Collections.sort(this.spans, new Span.PositionSorter());
	}

	public boolean isDocumentLevel() {
		return this.level == DocumentLevel;
	}

	public boolean isSnippetLevel() {
		return this.level == SnippetLevel;
	}

	public int getStart() {
		if (this.start < 0 && this.spans != null) {
			this.start = this.spans.firstElement().getStart();
		}
		return this.start;
	}

	public int getEnd() {
		if (this.end < 0 && this.spans != null) {
			this.end = this.spans.lastElement().getEnd();
		}
		return this.end;
	}

	public Type getType() {
		return type;
	}

	public TypeSystem getTypeSystem() {
		return this.getAnnotationCollection().getAnnotationEvent().getAnalysis().getTypeSystem();
	}

	public Analysis getAnalysis() {
		return this.getAnnotationCollection().getAnnotationEvent().getAnalysis();
	}

	public Vector<Span> getSpans() {
		return spans;
	}

	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	public static class PositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
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

	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	public String getText() {
		if (this.text == null) {
			Document doc = this.getAnnotationCollection().getAnnotationEvent().getDocument();
			if (doc.getText() != null && doc.getText().length() > this.getEnd() && this.getStart() >= 0
					&& this.getEnd() >= 0) {
				this.text = doc.getText().substring(this.getStart(), this.getEnd() + 1);
			}
		}
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String toString() {
		String typestr = (this.type != null ? this.type.getName() : "*");
		String textstr = (this.getText() != null ? this.getText() : "*");
		String spanstr = (this.spans != null ? this.spans.toString() : "*");
		String str = "<Annotation:Type=<" + typestr + ">,Text=\"" + textstr + "\",Spans=" + spanstr
				+ (this.isPrimary() ? "[Primary]" : "[Secondary]" + ">");
		return str;
	}

	public Vector<Variable> getVariables() {
		return variables;
	}

	public void addVariable(String vname, Object value) {
		if (Variable.find(this.variables, vname) == null) {
			Variable var = new Variable(vname);
			var.bind(value);
			this.variables = VUtils.add(this.variables, var);
		}
	}

	public Object getClassificationValue() {
		return this.classificationValue;
	}

	public void setClassificationValue(Object classificationValue) {
		this.classificationValue = classificationValue;
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
	}

	public boolean touchedByCursor(int position) {
		if (this.getSpans() != null) {
			for (workbench.api.annotation.Span span : this.getSpans()) {
				if (span.getStart() <= position && position <= span.getEnd()) {
					return true;
				}
			}
		}
		return false;
	}

	public Document getDocument() {
		return this.getAnnotationCollection().getAnnotationEvent().getDocument();
	}

	public String getDocumentName() {
		return this.getAnnotationCollection().getAnnotationEvent().getDocumentName();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vector<OverlappingAnnotationPair> getOverlappingAnnotationPairs() {
		return overlappingAnnotationPairs;
	}

	public OverlappingAnnotationPair getFirstOverlappingAnnotationPair() {
		if (this.overlappingAnnotationPairs != null) {
			return this.overlappingAnnotationPairs.firstElement();
		}
		return null;
	}

	public void addOverlappingAnnotationPair(OverlappingAnnotationPair opair) {
		this.overlappingAnnotationPairs = VUtils.add(this.overlappingAnnotationPairs, opair);
		if (this.overlappingAnnotationPairs.size() > 1) {
			Collections.sort(this.overlappingAnnotationPairs, new OverlappingAnnotationPair.LengthSorter());
		}
		this.matchingAnnotations = null;
		for (OverlappingAnnotationPair pair : this.overlappingAnnotationPairs) {
			Annotation matching = (this.isPrimary() ? pair.getSecondaryAnnotation() : pair.getPrimaryAnnotation());
			this.matchingAnnotations = VUtils.add(this.matchingAnnotations, matching);
		}
	}

	public boolean hasMatchingAnnotations() {
		return this.matchingAnnotations != null;
	}

	public Vector<Annotation> getMatchingAnnotations() {
		return this.matchingAnnotations;
	}

	public Annotation getFirstMatchingAnnotation() {
		if (this.matchingAnnotations != null) {
			return this.matchingAnnotations.firstElement();
		}
		return null;
	}

	// Wrong: Just returns largest
	public Annotation getMatchingPrimaryAnnotation() {
		if (this.isPrimary()) {
			return this;
		}
		if (this.overlappingAnnotationPairs != null) {
			return this.overlappingAnnotationPairs.firstElement().getPrimaryAnnotation();
		}
		return null;
	}

	public Annotation getMatchingSecondaryAnnotation() {
		if (!this.isPrimary()) {
			return this;
		}
		if (this.overlappingAnnotationPairs != null) {
			return this.overlappingAnnotationPairs.firstElement().getSecondaryAnnotation();
		}
		return null;
	}

	public boolean isPrimary() {
		return this.getAnnotationCollection().isPrimary();
	}

	public KTAnnotation getKtAnnotation() {
		return ktAnnotation;
	}

	public void setKtAnnotation(KTAnnotation ktAnnotation) {
		this.ktAnnotation = ktAnnotation;
	}

	public static class DocumentPositionSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Annotation a1 = (Annotation) o1;
			Annotation a2 = (Annotation) o2;
			String dname1 = a1.getDocumentName();
			String dname2 = a2.getDocumentName();
			if (dname1.compareTo(dname2) < 0) {
				return -1;
			}
			if (dname2.compareTo(dname1) < 0) {
				return 1;
			}
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

	public static void setVisited(Vector<Annotation> annotations, boolean visited) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				annotation.setVisited(visited);
			}
		}
	}

	// 9/27/2014: Temporary!
	public static Vector<Annotation> gatherSnippetAnnotations(Vector<Annotation> annotations) {
		Vector<Annotation> snippets = null;
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (annotation.getType() != null && !annotation.getType().isDocumentType()) {
					snippets = VUtils.add(snippets, annotation);
				}
			}
		}
		return snippets;
	}

	public Vector<Relation> getRelations() {
		return relations;
	}

	public void addRelation(String rname, Annotation subject, Annotation modifier) {
		Relation rel = new Relation(rname, subject, modifier);
		this.relations = VUtils.add(this.relations, rel);
	}

	public Annotator getAnnotator() {
		return this.annotationCollection.getAnnotator();
	}

	public boolean isContainsClassificationMatch() {
		return containsClassificationMatch;
	}

	public void setContainsClassificationMatch(boolean containsClassificationMatch) {
		this.containsClassificationMatch = containsClassificationMatch;
	}
	
	public static void resetContainsClassificationMatch(Vector<Annotation> annotations) {
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				annotation.setContainsClassificationMatch(false);
			}
		}
	}

	public Vector<Annotation> getOverlapping() {
		return overlapping;
	}

	public void addOverlapping(Annotation annotation) {
		this.overlapping = VUtils.add(this.overlapping, annotation);
		annotation.overlapping = VUtils.add(annotation.overlapping, this);
	}

	public void setOverlapping(Vector<Annotation> overlapping) {
		this.overlapping = overlapping;
	}

	public boolean overlappingSetContainsMatchedClassification() {
		if (this.containsClassificationMatch) {
			return true;
		}

		boolean hasClassValue = (this.getClassificationValue() != null);
		boolean otherHasClassValue = false;

		if (this.overlapping != null) {
			for (Annotation other : this.overlapping) {
				if (this.getType() == other.getType() && other.getClassificationValue() != null) {
					otherHasClassValue = true;
				}
				if (other.containsClassificationMatch) {
					return true;
				}
			}
		}
		// 7/20/2016: 
		if (!hasClassValue && !otherHasClassValue) {
			return true;
		}
		return false;
	}

	// Before 11/24/2015
	// public boolean overlappingContainsMatchingAnnotations() {
	// if (this.overlapping != null) {
	// for (Annotation other : this.overlapping) {
	// if (other.getMatchingAnnotations() != null) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	public Vector<String> gatherAllOverlappingClassifications() {
		return null;
	}

	public String getOverlappingClassifications() {
		String rv = "";
		Hashtable hash = new Hashtable();
		String cv = (String) this.getClassificationValue();
		if (cv != null) {
			hash.put(cv, cv);
		}
		if (this.overlapping != null) {
			for (Annotation other : this.overlapping) {
				cv = (String) other.getClassificationValue();
				if (cv != null) {
					hash.put(cv, cv);
				}
			}
		}
		if (!hash.isEmpty()) {
			for (Object key : HUtils.getKeys(hash)) {
				cv = (String) key;
				rv += cv + ":";
			}
		}
		return rv;
	}

	public static Vector<Annotation> getUniqueAnnotations(Vector<Annotation> annotations) {
		Vector<Annotation> unique = null;
		Hashtable<String, Annotation> hash = new Hashtable();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				hash.put(annotation.getText(), annotation);
			}
			unique = HUtils.getElements(hash);
		}
		return unique;
	}

	public boolean isValidatedPrimaryAnnotation() {
		return isValidatedPrimaryAnnotation;
	}

	public void setValidatedPrimaryAnnotation() {
		if (this.isPrimary()) {
			String aname = this.getKtAnnotation().getAnnotatorName()
					.toLowerCase();
			boolean isMoonstone = aname.contains("moonstone");
			boolean hasValidationCorrect = JavaFunctions.annotationHasAttributeValue(
					this, "validation", "correct");
			boolean hasValidationIncorrect = JavaFunctions.annotationHasAttributeValue(
					this, "validation", "incorrect");
			
			if (!isMoonstone || hasValidationCorrect) {
				this.isValidatedPrimaryAnnotation = true;
			}
		}
	}
	
	

}
