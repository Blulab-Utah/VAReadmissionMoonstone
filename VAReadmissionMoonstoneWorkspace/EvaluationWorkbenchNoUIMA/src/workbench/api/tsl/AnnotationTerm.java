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
package workbench.api.tsl;

import java.util.Vector;

import annotation.Classification;
import annotation.EVAnnotation;
import annotation.SnippetAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.property.PropertySentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class AnnotationTerm extends ObjectConstant {
	private TSLInterface tsl = null;
	// private DocumentTerm documentTerm = null;
	private Document document = null;

	public AnnotationTerm(TSLInterface tsl, EVAnnotation annotation,
			Document document) {
		super(annotation);
		this.setName(annotation.getText());
		this.document = document;
		this.tsl = tsl;
	}

	public void addToKB() {
		KnowledgeBase kb = this.tsl.getKb();
		EVAnnotation annotation = (EVAnnotation) this.getObject();
		RelationSentence rs = RelationSentence.createRelationSentence(
				"contains-snippet", this.document, this);
		kb.initializeAndAddForm(rs);
		Classification c = annotation.getClassification();
		if (c.getValue() != null) {
			ObjectConstant oc = new ObjectConstant(c.getValue());
			rs = RelationSentence.createRelationSentence("classification-of",
					this, oc);
			kb.initializeAndAddForm(rs);
		}
		Vector<String> anames = annotation.getAttributeNames();
		if (anames != null) {
			for (String aname : anames) {
				String shortname = getShortAttributeName(aname);
				Object value = annotation.getAttribute(aname);
				ObjectConstant oc = new ObjectConstant(value);
				TypeConstant type = TypeConstant.createTypeConstant(annotation
						.getType().getName());
				this.setType(type);

				// 7/12/2013: E.g. (has-property <annotationTerm> "age" 30)
				ObjectConstant ac = new ObjectConstant(shortname);
				ObjectConstant vc = new ObjectConstant(value);
				Vector arguments = new Vector(0);
				arguments.add(this);
				arguments.add(ac);
				arguments.add(vc);
				rs = RelationSentence.createRelationSentence("has-property",
						arguments);
				this.addSubjectSentence(rs);
				kb.initializeAndAddForm(rs); // no modifier

				// E.g. (age <annotationTerm> 30)
				PropertyConstant pc = PropertyConstant.createPropertyConstant(
						shortname, type, value);
				PropertySentence ps = PropertySentence.createPropertySentence(
						pc, this, oc);
				this.addPropertySentence(ps);
				kb.initializeAndAddForm(ps); // no subject or modifier
			}
		}
		Vector<String> rnames = annotation.getRelations();
		if (rnames != null) {
			for (String rname : rnames) {
				Vector<EVAnnotation> relata = annotation.getRelata(rname);
				for (EVAnnotation relatum : relata) {
					AnnotationTerm other = (AnnotationTerm) this.tsl
							.getMapObject(relatum);
					if (other != null) {

						// 7/12/2013: E.g. (has-relation <annotationTerm1>
						// "located-at" <annotationTerm2>)
						Vector arguments = new Vector(0);
						arguments.add(this);
						arguments.add(new ObjectConstant(rname));
						arguments.add(other);
						rs = RelationSentence.createRelationSentence(
								"has-relation", arguments);
						this.addSubjectSentence(rs);
						kb.initializeAndAddForm(rs);

						// E.g. (located-at <annotationTerm1> <annotationTerm2>)
						rs = RelationSentence.createRelationSentence(rname,
								this, other);
						this.addSubjectSentence(rs);
						kb.initializeAndAddForm(rs);
					}
				}
			}
		}
	}

	private String getShortAttributeName(String aname) {
		int index = aname.indexOf('$');
		if (index > 0) {
			aname = aname.substring(index + 1);
		}
		return aname;
	}

	// public DocumentTerm getDocumentTerm() {
	// return documentTerm;
	// }

	public Document getDocument() {
		return this.document;
	}

	// I can nest a call to this method inside a JRelationSentence, e.g.
	// e.g. ("workbench.tsl.AnnotationTerm:evAnnotationWithAttributeValue" ?x
	// "side" "right")

	public static Boolean evAnnotationWithAttributeValue(
			SnippetAnnotation annotation, String astr, String vstr) {
		Vector<String> anames = annotation.getAttributeNames();
		if (anames != null) {
			for (String aname : anames) {
				if (aname.contains(astr)) {
					String value = (String) annotation.getAttribute(aname);
					if (value.contains(vstr)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// 9/17/2013
	public boolean equals(Object o) {
		if (o instanceof String) {
			String str = o.toString().toLowerCase();
			return this.getName() != null && this.getName().toLowerCase().contains(str);
		}
		return this == o;
	}

}

