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

import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;

import tsl.utilities.JDomUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import typesystem.Attribute;
import typesystem.TypeSystem;
import workbench.api.annotation.Annotation;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.EVAnnotation;
import annotation.SnippetAnnotation;
import annotation.Span;

public class KTAnnotation extends KTSimpleInstance {

	String annotationSource = null;
	String textSource = null;
	String creationDate = null;
	int[] textStarts = null;
	int[] textEnds = null;
	String annotatedMentionID = null;
	KTClassMention annotatedMention = null;
	String annotatorID = null;
	String knowtatorAnnotatorName = null;
	String knowtatorSet = null;
	KTAnnotator annotator = null;
	String annotatorName = null;
	String text = null;
	SnippetAnnotation snippet = null;
	Vector<KTRelation> relations = null;
	boolean isLengthIndicatedPossibleRelation = false;
	Annotation annotation = null;  // 9/18/2014

	public KTAnnotation(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		extractInformation();
		Vector<String> key = new Vector(0);
		key.add(this.textSource);
		key.add(this.annotatorID);
		kt.annotations = VUtils.add(kt.annotations, this);
		VUtils.pushHashVector(kt.annotationHash, key, this);
	}

	public KTAnnotation(KnowtatorIO kt, Vector v, String fname)
			throws Exception {
		super(kt);
		int start = 0, end = 0;
		this.annotationSource = fname;
		// 2/20/2013: Am I de-allocating things like this when they are no
		// longer needed?
		this.ktvector = v;
		this.annotatedMentionID = (String) VUtils.assocValueTopLevel(
				"knowtator_annotated_mention", this.ktvector);
		this.annotatorID = (String) VUtils.assocValueTopLevel(
				"knowtator_annotation_annotator", this.ktvector);
		this.annotatorName = this.annotatorID;
		this.knowtatorAnnotatorName = this.annotatorName;
		this.knowtatorSet = (String) VUtils.assocValueTopLevel("knowtator_set",
				this.ktvector);

		if (this.kt.annotator != null) {
			this.annotatorID = this.kt.annotator.getName();
			this.annotatorName = this.kt.annotator.getName();
		}

		Vector sv = VUtils.assocTopLevel("knowtator_annotation_span",
				this.ktvector);
		if (sv != null) {
			sv = VUtils.rest(sv);
			this.textStarts = new int[sv.size()];
			this.textEnds = new int[sv.size()];
			for (int i = 0; i < sv.size(); i++) {
				String str = (String) sv.elementAt(i);
				Vector<String> strs = StrUtils.stringList(str, '|');
				this.textStarts[i] = Integer.parseInt(strs.elementAt(0));
				this.textEnds[i] = Integer.parseInt(strs.elementAt(1));
			}
		}
		this.text = (String) VUtils.assocValueTopLevel(
				"knowtator_annotation_text", this.ktvector);
		if (start > 0 && end - start <= 1) {
			this.isLengthIndicatedPossibleRelation = true;
		}
		this.textSource = (String) VUtils.assocValueTopLevel(
				"knowtator_annotation_text_source", this.ktvector);
		if (this.textSource != null && this.annotatorID != null) {
			Vector<String> key = new Vector(0);
			key.add(this.textSource);
			key.add(this.annotatorID);
			VUtils.pushHashVector(kt.annotationHash, key, this);
		}
		kt.annotations = VUtils.add(kt.annotations, this);
	}

	public void extractInformationXMLFormatSHARP() throws Exception {
		Element node = JDomUtils.getElementByName(this.node, "mention");
		this.annotatedMentionID = node.getAttributeValue("id");
		node = JDomUtils.getElementByName(this.node, "annotator");
		this.annotatorID = node.getAttributeValue("id");
		this.annotatorName = node.getText();
		node = JDomUtils.getElementByName(this.node, "span");
		Vector<Element> v = JDomUtils.getElementsByName(this.node, "span");
		if (v != null) {
			this.textStarts = new int[v.size()];
			this.textEnds = new int[v.size()];
			int i = 0;
			for (Element e : v) {
				String sstr = e.getAttributeValue("start");
				String estr = e.getAttributeValue("end");
				this.textStarts[i] = Integer.parseInt(sstr);
				this.textEnds[i] = Integer.parseInt(estr);
				i++;
			}
		}
		node = JDomUtils.getElementByName(this.node, "spannedText");
		if (node != null) {
			this.text = node.getText();
		}
		this.textSource = this.kt.textSource;
		if (this.kt.annotator != null) {
			this.annotatorID = this.kt.annotator.getName();
			this.annotatorName = this.kt.annotator.getName();
		}
	}

	public void extractInformationXMLFormatOriginal() throws Exception {
		Vector<Element> OSVNodes = JDomUtils.getElementsByName(this.node,
				"own_slot_value");
		if (OSVNodes != null) {
			for (Element osvnode : OSVNodes) {
				Element cnode = JDomUtils.getElementByName(osvnode,
						"slot_reference");
				String rvalue = cnode.getText();
				cnode = JDomUtils.getElementByName(osvnode, "value");
				String vvalue = cnode.getText();
				if ("knowtator_annotation_text_source".equals(rvalue)) {
					this.textSource = vvalue;
				} else if ("knowtator_annotation_creation_date".equals(rvalue)) {
					this.creationDate = vvalue;
				} else if ("knowtator_annotation_span".equals(rvalue)) {
					Vector<String> v = StrUtils.stringList(vvalue, '|');
					// 2/14/2013: Changed to allow disjoint spans.
					// this.textStart = Integer.parseInt(v.elementAt(0));
					// this.textEnd = Integer.parseInt(v.elementAt(1));
				} else if ("knowtator_annotated_mention".equals(rvalue)) {
					this.annotatedMentionID = vvalue;
				} else if ("knowtator_annotation_annotator".equals(rvalue)) {
					this.annotatorID = vvalue;
				} else if ("knowtator_annotation_text".equals(rvalue)) {
					this.text = vvalue;
				}
			}
		}
	}

	public void resolveReferences() throws Exception {
		if (this.annotatedMentionID != null) {
			Object o = this.kt.getHashItem(annotatedMentionID);
			if (!(o instanceof KTClassMention)) {
				return;
			}
			this.annotatedMention = (KTClassMention) this.kt
					.getHashItem(annotatedMentionID);
			this.annotatedMention.annotation = this;
		}
	}

	public String toString() {
		return "<KTAnnotation:" + this.name + ",Text=\"" + this.text
				+ "\",Spans=" + this.getSpanStrings();
	}

	public String getSpanStrings() {
		if (this.textStarts != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < this.textStarts.length; i++) {
				sb.append("<" + this.textStarts[i] + "|" + this.textEnds[i]
						+ ">");
			}
			return sb.toString();
		}
		return "";
	}

	SnippetAnnotation extractSnippet(AnnotationCollection ac,
			Hashtable<String, EVAnnotation> spanhash) throws Exception {
		SnippetAnnotation snippet = null;
		KTClassMention mention = this.annotatedMention;
		if (this.snippet == null && this.text != null
				&& this.textStarts != null && this.annotatedMention != null
				&& this.annotatedMention.mentionClass != null
				&& this.kt.getTypeSystem() != null
				&& mention.getSlotMentions() != null
				&& this.hasValidAnnotator()) {
			TypeSystem ts = this.kt.getTypeSystem();
			if (!ts.isUseOnlyTypeModel()) {
				this.annotatedMention.mentionClass.addToTypeSystem();
			}
			String tsClassName = typesystem.Annotation.getClassificationName(
					ts, this.annotatedMention.mentionClass.name);

			typesystem.Classification pclass = ts
					.getUimaClassification(tsClassName);
			if (pclass != null) {
				typesystem.Annotation type = (typesystem.Annotation) pclass
						.getParentTypeObject();
				String typeid = pclass.getParentTypeObject().getId();
				snippet = this.snippet = new SnippetAnnotation();
				snippet.setType(pclass.getParentTypeObject());
				this.kt.snippets = VUtils.add(this.kt.snippets, snippet);
				this.snippet.setAnnotationCollection(ac);
				snippet.setKtAnnotation(this);
				this.snippet.setId(this.getID());
				for (int i = 0; i < this.textStarts.length; i++) {
					int start = this.textStarts[i];
					int end = this.textEnds[i];
					Span span = new Span(this.snippet, start, end);
					String spanID = "SPAN:" + this.getID() + "_" + i;
					span.setId(spanID);
					this.snippet.addSpan(span);
					span.setAnnotationCollection(ac);
				}
				this.snippet.setType(pclass.getParentTypeObject());
				Classification c = new Classification(null, this.snippet,
						pclass, tsClassName, null, null);
				this.snippet.setClassification(c);

				for (KTSlotMention sm : mention.getSlotMentions()) {
					Object value = sm.getValue();
					if (value instanceof String) {
						ts.getRegularizedName((String) value);
					}
					if (value != null) {
						String astr = typeid + "$" + sm.mentionSlotID;
						astr = ts.getRegularizedName(astr);
						Attribute pattr = snippet.getClassification()
								.getParentClassification().getAttribute(astr);
						Attribute tattr = pclass.getParentTypeObject()
								.getAttribute(astr);
						if (pattr != null) {
							c.setProperty(astr, value);
						} else if (tattr != null) {
							snippet.setAttribute(astr, value);
						}
					}
				}
			}
		}
		if (snippet != null) {
			if (snippet.getClassification().isEmpty()) {
				this.snippet = null;
			}
		}

		if (this.snippet != null) {
			boolean eliminateDuplicates = false;
			String str = this.snippet.getStart() + ":" + this.snippet.getEnd();
			if (!eliminateDuplicates || spanhash.get(str) == null) {
				spanhash.put(str, this.snippet);
				ac.addAnnotation(this.snippet.getId(), this.snippet);
			}
		}

		// 3/13/2013: VINCI TEST
		boolean doVinciTest = true;
		if (doVinciTest && this.snippet != null) {
			TypeSystem ts = this.kt.getTypeSystem();
			annotation.Classification c = snippet.getClassification();
			if (c.getPropertyNumber() == 0) {
				typesystem.Annotation type = c.getParentAnnotationType();
				typesystem.Classification pclass = c.getParentClassification();
				String pcid = pclass.getParentTypeObject().getId();
				String astr = pcid + "$" + type.getName();
				astr = ts.getRegularizedName(astr);
				// String value = "annotated:" + pclass.getAnnotationCount();
				// pclass.incrementAnnotationCount();
				String value = "annotated";
				Attribute pattr = snippet.getClassification()
						.getParentClassification().getAttribute(astr);
				if (pattr != null) {
					c.setProperty(astr, value);
				}
			}
		}

		return this.snippet;
	}

	public boolean hasValidAnnotator() throws Exception {
		Vector<String> ktsets = this.kt.getSelectedAnnotationSets();
		Vector<String> ktanns = this.kt.getSelectedAnnotators();
		String aname = this.knowtatorAnnotatorName;
		String aset = this.getKnowtatorSet();
		if (ktanns != null) {
			return ktanns.contains(aname);
		}
		if (ktsets != null) {
			return ktsets.contains(aset);
		}
		return true;
	}

	public KTAnnotator getAnnotator() {
		return annotator;
	}

	public String getTextSource() {
		return textSource;
	}

	public int getTextStart() {
		if (this.textStarts != null) {
			return this.textStarts[0];
		}
		return 0;
	}

	public int getTextEnd() {
		if (this.textEnds != null) {
			return this.textEnds[this.textEnds.length - 1];
		}
		return 0;
	}

	public String getAnnotatorID() {
		return annotatorID;
	}

	public void setAnnotatorID(String annotatorID) {
		this.annotatorID = annotatorID;
	}

	public String getAnnotatorName() {
		return annotatorName;
	}

	public void setAnnotatorName(String annotatorName) {
		this.annotatorName = annotatorName;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTextSource(String textSource) {
		this.textSource = textSource;
	}

	public void setAnnotator(KTAnnotator annotator) {
		this.annotator = annotator;
	}

	public SnippetAnnotation getSnippet() {
		return this.snippet;
	}

	public KTClassMention getAnnotatedMention() {
		return annotatedMention;
	}

	public void setAnnotatedMention(KTClassMention annotatedMention) {
		this.annotatedMention = annotatedMention;
	}

	public void setSnippet(SnippetAnnotation snippet) {
		this.snippet = snippet;
	}

	public Vector<KTRelation> getRelations() {
		return relations;
	}

	public void addRelation(KTRelation relation) {
		this.relations = VUtils.add(this.relations, relation);
	}

	public boolean isLengthIndicatedPossibleRelation() {
		return isLengthIndicatedPossibleRelation;
	}

	public static boolean isNonOverlapping(KTAnnotation a1, KTAnnotation a2) {
		if (a1.getTextEnd() < a2.getTextStart()
				|| a2.getTextEnd() < a1.getTextStart()) {
			return true;
		}
		return false;
	}

	// 1/11/2012: Add 1?
	public int getTextLength() {
		return this.getTextEnd() - this.getTextStart();
	}

	public String getKnowtatorSet() {
		return this.knowtatorSet;
	}

	public String getAnnotationSource() {
		return this.annotationSource;
	}

	public String getAnnotatedMentionID() {
		return annotatedMentionID;
	}

	public String getSlotNames() {
		StringBuffer sb = new StringBuffer();
		getSlotNames(sb, 0);
		return sb.toString();
	}

	public void getSlotNames(StringBuffer sb, int depth) {
		if (this.isVisited()) {
			return;
		}
		this.setVisited(true);
		String str = "";
		for (int i = 0; i < depth; i++) {
			str += "  ";
		}
		KTClassMention cm = this.getAnnotatedMention();
		if (cm.getSlotMentions() != null) {
			for (KTSlotMention sm : cm.getSlotMentions()) {
				sb.append(str);
				sb.append(sm.mentionSlotID);
				sb.append("\n");
				if (sm instanceof KTComplexSlotMention) {
					KTComplexSlotMention csm = (KTComplexSlotMention) sm;
					KTAnnotation annotation = csm.complexSlotClassMention.annotation;
					annotation.getSlotNames(sb, depth + 1);
				}
			}
		}
		this.setVisited(false);
	}

	public KTSlotMention getSlotMention(String sname) {
		if (this.isVisited()) {
			return null;
		}
		this.setVisited(true);
		KTClassMention cm = this.getAnnotatedMention();
		if (cm.getSlotMentions() != null) {
			for (KTSlotMention sm : cm.getSlotMentions()) {
				if (sname.equals(sm.mentionSlotID)) {
					return sm;
				}
				if (sm instanceof KTComplexSlotMention) {
					KTComplexSlotMention csm = (KTComplexSlotMention) sm;
					csm.complexSlotClassMention.annotation
							.getSlotMention(sname);
				}
			}
		}
		this.setVisited(false);
		return null;
	}

	public Object getSlotValue(String[] slotNames) {
		return getSlotValue(slotNames, 0);
	}

	private Object getSlotValue(String[] slotNames, int sindex) {
		if (slotNames == null || sindex >= slotNames.length) {
			return null;
		}
		KTClassMention cm = this.getAnnotatedMention();
		String sname = slotNames[sindex];
		if (cm.getSlotMentions() != null) {
			for (KTSlotMention sm : cm.getSlotMentions()) {
				if (sname.equals(sm.mentionSlotID)) {
					if (sm instanceof KTComplexSlotMention) {
						KTComplexSlotMention csm = (KTComplexSlotMention) sm;
						KTAnnotation annotation = csm.complexSlotClassMention.annotation;
						return annotation.getSlotValue(slotNames, sindex + 1);
					} else if (sm instanceof KTStringSlotMention) {
						KTStringSlotMention ssm = (KTStringSlotMention) sm;
						return ssm.getValue();
					}
				}
			}
		}
		return null;
	}

	public String getModifierSpan(String[] slotNames) {
		KTAnnotation kta = this.getKTAnnotation(slotNames);
		if (kta != null && kta.textStarts != null) {
			String str = null;
			for (int i = 0; i < kta.textStarts.length; i++) {
				str = kta.textStarts[i] + "-" + kta.textEnds[i];
				if (i < kta.textStarts.length - 1) {
					str += ",";
				}
			}
			return str;
		}
		return null;
	}

	public String getText(String[] slotNames) {
		KTAnnotation annotation = this.getKTAnnotation(slotNames);
		if (annotation != null) {
			return annotation.getText();
		}
		return null;
	}

	public KTAnnotation getKTAnnotation() {
		return this;
	}

	public KTAnnotation getKTAnnotation(String[] slotNames) {
		return getKTAnnotation(slotNames, 0);
	}

	private KTAnnotation getKTAnnotation(String[] slotNames, int sindex) {
		if (slotNames == null || sindex >= slotNames.length) {
			return this;
		}
		KTClassMention cm = this.getAnnotatedMention();
		String sname = slotNames[sindex];
		for (KTSlotMention sm : cm.getSlotMentions()) {
			if (sname.equals(sm.mentionSlotID)) {
				if (sm instanceof KTComplexSlotMention) {
					KTComplexSlotMention csm = (KTComplexSlotMention) sm;
					KTAnnotation annotation = csm.complexSlotClassMention.annotation;
					return annotation.getKTAnnotation(slotNames, sindex + 1);
				}
			}
		}
		return null;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public int[] getTextStarts() {
		return textStarts;
	}

	public int[] getTextEnds() {
		return textEnds;
	}
	
	

}
