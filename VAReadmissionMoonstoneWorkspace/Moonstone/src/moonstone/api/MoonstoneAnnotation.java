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
package moonstone.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.relation.RelationSentence;
import moonstone.annotation.Annotation;
import moonstone.semantic.Interpretation;

public class MoonstoneAnnotation {
	protected Annotation source = null;
	private String id = null;
	private String text = null;
	private int textStart = -1;
	private int textEnd = -1;
	private int textLength = -1;
	private String semanticType = null;
	private String concept = null;
	private String rule = null;
	private double score = 0;
	private ArrayList<MoonstoneAnnotationProperty> properties = new ArrayList();
	private ArrayList<MoonstoneAnnotationRelation> relations = new ArrayList();

	public MoonstoneAnnotation(Annotation annotation) {
		this.source = annotation;
		this.id = annotation.getAnnotationID();
		this.text = annotation.getText();
		this.textStart = annotation.getTextStart();
		this.textEnd = annotation.getTextEnd();
		this.textLength = annotation.getTextlength();
		this.score = annotation.getGoodness();
		if (annotation.isInterpreted()) {
			Interpretation si = annotation.getSemanticInterpretation();
			if (si.getType() != null) {
				this.semanticType = si.getType().getName();
			}
			if (si.getConcept() != null) {
				this.concept = si.getConcept().toString();
			}
		}
	}
	
	public static ArrayList<MoonstoneAnnotation> wrapMoonstoneAnnotations(
			Vector<Annotation> annotations) {
		Hashtable<Object, MoonstoneAnnotation> msahash = new Hashtable();
		ArrayList<MoonstoneAnnotation> msv = new ArrayList();
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				MoonstoneAnnotation msa = new MoonstoneAnnotation(annotation);
				msv.add(msa);
				msahash.put(msa.getId(), msa);
				msahash.put(annotation, msa);
			}
		}
		for (MoonstoneAnnotation msa : msv) {
			if (msa.source.isInterpreted()) {
				Interpretation si = msa.source.getSemanticInterpretation();
				for (String property : si.getPropertyNames()) {
					if (Annotation.PropertyIsRelevant(property)) {
						Object value = msa.source.getProperty(property);
						if (!(value instanceof Annotation)) {
							MoonstoneAnnotationProperty map = new MoonstoneAnnotationProperty(
									msa, property, value);
							msa.getProperties().add(map);
						}
					}
				}
				if (si.getRelationSentences() != null) {
					for (RelationSentence rs : si.getRelationSentences()) {
						if (rs.getArity() == 2
								&& rs.getSubject() instanceof Annotation
								&& rs.getModifier() instanceof Annotation) {
							MoonstoneAnnotation subject = msahash.get(rs
									.getSubject());
							MoonstoneAnnotation modifier = msahash.get(rs
									.getModifier());
							MoonstoneAnnotationRelation mar = new MoonstoneAnnotationRelation(
									msa, rs.getRelation().getName(), subject,
									modifier);
							msa.getRelations().add(mar);
						}
					}
				}
			}
		}
		Collections.sort(msv, new MoonstoneAnnotation.GoodnessSorter());
		Collections.sort(msv, new MoonstoneAnnotation.TextPositionSorter());
		return msv;
	}

	public String toString() {
		String str = "<Id=" + this.id + ",Position=("
				+ this.textStart + "-" + this.textEnd + ")";
		if (this.semanticType != null) {
			str += ",Type=" + this.semanticType;
		}
		if (this.concept != null) {
			str += ",Concept=" + this.concept;
		}
		str += ",Text=\"" + this.text + "\">";
		return str;
	}
	
	public String toXML() {
		return this.source.toXML();
	}
	
	public String toHTML() {
		return this.source.toHTML();
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public int getTextStart() {
		return textStart;
	}

	public int getTextEnd() {
		return textEnd;
	}

	public int getTextLength() {
		return textLength;
	}

	public String getSemanticType() {
		return semanticType;
	}

	public String getConcept() {
		return concept;
	}

	public String getRule() {
		return rule;
	}

	public ArrayList<MoonstoneAnnotationProperty> getProperties() {
		return properties;
	}

	public ArrayList<MoonstoneAnnotationRelation> getRelations() {
		return relations;
	}

	public double getScore() {
		return score;
	}
	
	public static class TextPositionSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			MoonstoneAnnotation a1 = (MoonstoneAnnotation) o1;
			MoonstoneAnnotation a2 = (MoonstoneAnnotation) o2;
			if (a1.getTextStart() < a2.getTextStart()) {
				return -1;
			}
			if (a2.getTextStart() < a1.getTextStart()) {
				return 1;
			}
			if (a1.getTextEnd() > a2.getTextEnd()) {
				return -1;
			}
			if (a2.getTextEnd() > a1.getTextEnd()) {
				return 1;
			}
			return 0;
		}
	}

	public static class GoodnessSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			MoonstoneAnnotation a1 = (MoonstoneAnnotation) o1;
			MoonstoneAnnotation a2 = (MoonstoneAnnotation) o2;
			if (a1.getScore() > a2.getScore()) {
				return -1;
			}
			if (a1.getScore() < a2.getScore()) {
				return 1;
			}
			return 0;
		}
	}

}
