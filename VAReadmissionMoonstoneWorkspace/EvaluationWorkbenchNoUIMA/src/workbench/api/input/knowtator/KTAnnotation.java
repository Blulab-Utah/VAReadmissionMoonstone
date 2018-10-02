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
package workbench.api.input.knowtator;

import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;

import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.JDomUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import utility.UnixFormat;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

public class KTAnnotation extends KTSimpleInstance {

	public String annotationSource = null;
	public String textSource = null;
	public String creationDate = null;
	public int[] textStarts = null;
	public int[] textEnds = null;
	public String annotatedMentionID = null;
	public KTClassMention annotatedMention = null;
	public String annotatorID = null;
	public String knowtatorAnnotatorName = null;
	public String knowtatorSet = null;
	public KTAnnotator annotator = null;
	public String annotatorName = null;
	public String text = null;
	public Annotation snippet = null;
	public Vector<KTRelation> relations = null;
	public boolean isLengthIndicatedPossibleRelation = false;
	public String documentName = null;
	private boolean isValid = true;

	// 10/7/2015: For use with Moonstone
	public KTAnnotation(String id, String text, int tstart, int tend,
			String datestr) {
		this.id = id;
		this.annotatorID = "Moonstone";
		this.text = text;
		this.textStarts = new int[1];
		this.textEnds = new int[1];
		this.textStarts[0] = tstart;
		this.textEnds[0] = tend;

		if (tend - tstart > 40) {
			int x = 1;
		}
		this.creationDate = datestr;
	}

	public KTAnnotation(KnowtatorIO kt, String name, Element node)
			throws Exception {
		super(kt, name, node);
		extractInformation();
		
		if (this.textSource.startsWith("22")) {
			int x = 1;
		}
		
		if (!this.isValid()) {
			return;
		}
		Vector<String> key = KnowtatorIO.getAnnotationHashKey(this.textSource,
				this.annotatorID, kt.getAnalysis()
						.readAnnotationCollectionFileIsPrimary());
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
		
		if (this.textSource.startsWith("22")) {
			int x = 1;
		}
		
		if (this.textSource != null && this.annotatorID != null) {

			// 3/3/2016
			Vector<String> key = KnowtatorIO.getAnnotationHashKey(
					this.textSource, this.annotatorID, kt.getAnalysis()
							.readAnnotationCollectionFileIsPrimary());

			// Vector<String> key = new Vector(0);
			// key.add(this.textSource);
			// key.add(this.annotatorID);

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
		
		// 2/28/2018:  EHost files may contain multiple annotator IDs, and sometimes
		// I need to consider them all as "primary".
		if (!KnowledgeEngine.currentKnowledgeEngine.getStartupParameters().isPropertyTrue("UseAllKTAnnotationIDs")) {
			this.annotatorID = this.annotatorName;
		}

		// 7/26/2016: Should not have primary moonstone annotations
		// 3/20/2017:  PROBLEM:  I originally added this code to catch eHOST adjudications
		// that included Moonstone annotations.  Now, however, the annotators are using
		// "moonstone" as the annotator ID.  I'm going to unhook this for the moment, and
		// hopefully work out annotator naming conventions in the future...
		if (this.annotatorName != null
				&& this.kt.getAnalysis()
						.readAnnotationCollectionFileIsPrimary()
//				&& !this.kt.getAnalysis().isSingleAnnotationSet()
				&& this.annotatorName.toLowerCase().contains("moonstone")) {
			int x = 1;
//			this.isValid = false;
//			return;
		}

		/****
		 * // 3/10/2016 TEST
		 *3/29/2017:  In handling adjudication files, the name is important.  I need
		 * to come up with a permanent solution for this.
		String psstr = (this.kt.getAnalysis()
				.readAnnotationCollectionFileIsPrimary() ? "primary"
				: "secondary");
		if (this.annotatorID != null) {
			this.annotatorID = this.annotatorID + "-" + psstr;
			this.annotatorID = this.annotatorID.toLowerCase();
		}

		// this.annotatorID = this.getKt().getAnalysis()
		// .getNormalizedAnnotatorName(this.annotatorID);
	****/
		if (this.annotatorID == null) {
			this.isValid = false;
			return;
		}
		
		// 3/29/2017:  Temporarily removed...
//		this.annotatorName = this.annotatorID;

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
		
		/****
		 * 3/29/2017:  Removed temporarily.  Annotator name as recorded in the
		 * annotation files is important for adjudication tasks in particular.
		if (this.kt.annotator != null) {
			this.annotatorID = this.kt.annotator.getName();
			this.annotatorName = this.kt.annotator.getName();
		}
		*****/
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

	public Annotation extractSnippet(AnnotationCollection ac,
			Hashtable<String, Annotation> spanhash) throws Exception {
		int x = 2;
		
		try {
			boolean isPrimary = ac.isPrimary();
			if (ac.isPrimary()) {
				x = 1;
			} else {
				x = 1;
			}
			Annotation snippet = null;
			TypeSystem ts = ac.getAnalysis().getTypeSystem();
			KTClassMention mention = this.getAnnotatedMention();

			if (this.getSnippet() == null
					&& this.getText() != null
					&& this.getTextStarts() != null
					&& mention != null
					// 1/26/2015 -- EHost project schema doesn't include KTClass
					// analogs
					// && mention.getMentionClass() != null
					&& this.getKt().getTypeSystem() != null
					&& mention.getSlotMentions() != null
					&& this.hasValidAnnotator()) {
				KnowtatorIO kt = (KnowtatorIO) this.getKt();
				Type type = null;
				if (this.getAnnotatedMention().getMentionClass() != null) {
					type = ts.getType(this.getAnnotatedMention()
							.getMentionClass().getName());
				} else if (mention.getMentionClassID() != null) {
					type = ts.getType(mention.getMentionClassID());
				}
				if (type != null) {
					snippet = new Annotation(ac, type);
					snippet.setId(this.getID());
					snippet.setKtAnnotation(this);
					kt.snippets = VUtils.add(kt.snippets, snippet);

					// 10/13/2015: Offsets are getting messed up for some
					// reason...
					String text = this.getText();
					snippet.setText(text);

					for (int i = 0; i < this.getTextStarts().length; i++) {
						int start = this.getTextStarts()[i];
						int end = this.getTextEnds()[i];
						snippet.addSpan(start, end);
					}

					// Removed 4/6/2016
					// String dtext = ac.getAnalysis().getNamedDocumentText(
					// ac.getSourceTextName());
					// int snippetstart = snippet.getStart();
					// int snippetend = snippet.getEnd();

					// String dstr = dtext.substring(snippetstart, snippetend);
					// String sstr = snippet.getText();

					for (KTSlotMention sm : mention.getSlotMentions()) {
						if (sm instanceof KTComplexSlotMention) {
							x = 1;
						}
						Object value = sm.getValue();
						String mentionSlotID = sm.mentionSlotID;

						if (value == null) {
							// 10/12/2015: I wasn't storing sub-annotations as
							// attribute values before.
							KTComplexSlotMention csm = (KTComplexSlotMention) sm;
							KTClassMention cm = csm.complexSlotClassMention;
							KTAnnotation child = cm.annotation;
							String mcid = cm.mentionClassID;
							value = child;
						}

						if (value != null) {
							
							if (value.toString().toLowerCase().contains("facil")) {
								x = 1;
							}
							String aname = sm.mentionSlotID;
							String astr = type.getFullname() + ":" + aname;
							Attribute attr = type.getAttribute(astr);

							// 10/12/2015: Attributes may be independent of
							// types.
							// Create a new Attribute of the same name for the
							// current type.
							if (attr == null) {
								Type root = ts.getRootType();
								String rastr = ts.getRootType().getFullname()
										+ ":" + aname;
								attr = ts.getRootType().getAttribute(rastr);
								if (attr != null) {
									if (ts.isClassificationName(aname)) {
										new Classification(type, aname);
									} else {
										new Attribute(type, aname);
									}
								}
							}

							if (attr != null) {
								snippet.putAttributeValue(attr, value);
								if (attr.isClassification()) {
									snippet.setClassificationValue(value);
								}
								attr.addValue(value);
							}
						}
					}
				}
			}

			// 12/16/2014: Removed until I can find out why no snippets are
			// classifications.
			if (snippet != null && snippet.getClassificationValue() == null) {
				// snippet = null;
			}

			if (snippet != null) {
				if (snippet.getClassificationValue() == null) {
					x = 1;
				}
				String str = snippet.getStart() + ":" + snippet.getEnd();
				if (snippet.getClassificationValue() != null) {
					str = snippet.getClassificationValue() + ":" + str;
				}
				boolean flag = false; // 11/22/2015: permit multiple snippets at
										// a
										// span.
				if (spanhash.get(str) == null || flag) {
					spanhash.put(str, snippet);
					this.setSnippet(snippet);
					ac.addAnnotation(snippet, snippet.getId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.getSnippet();
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

	public String getAnnotatorName() {
		return annotatorName;
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

	public Annotation getSnippet() {
		return this.snippet;
	}

	public KTClassMention getAnnotatedMention() {
		return annotatedMention;
	}

	public void setAnnotatedMention(KTClassMention annotatedMention) {
		this.annotatedMention = annotatedMention;
	}

	public void setSnippet(Annotation snippet) {
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

	public void setAnnotatedMentionID(String annotatedMentionID) {
		this.annotatedMentionID = annotatedMentionID;
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
		// 10/20/2015
		if (sindex == slotNames.length) {
			return this;
		}
		if (slotNames == null || sindex > slotNames.length) {
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
					} else if (sm instanceof KTBooleanSlotMention) {
						KTBooleanSlotMention bsm = (KTBooleanSlotMention) sm;
						return bsm.getValue();
					}
				}
			}
		}
		return null;
	}

	// 19/16/2015
	public KTAnnotation getSlotValuedAnnotation(String[] slotNames) {
		return getSlotValuedAnnotation(slotNames, 0);
	}

	private KTAnnotation getSlotValuedAnnotation(String[] slotNames, int sindex) {
		if (sindex >= slotNames.length) {
			return this;
		}
		KTClassMention cm = this.getAnnotatedMention();
		String sname = slotNames[sindex];
		if (cm.getSlotMentions() != null) {
			for (KTSlotMention sm : cm.getSlotMentions()) {
				if (sname.equals(sm.mentionSlotID)) {
					if (sm instanceof KTComplexSlotMention) {
						KTComplexSlotMention csm = (KTComplexSlotMention) sm;
						KTAnnotation annotation = csm.complexSlotClassMention.annotation;
						return annotation.getSlotValuedAnnotation(slotNames,
								sindex + 1);
					}
				}
			}
		}
		return null;
	}

	public boolean hasValue(String attribute, Object value) {
		Object tval = this.getSlotValue(new String[] { attribute });
		return value.equals(tval);
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

	public int[] getTextStarts() {
		return textStarts;
	}

	public int[] getTextEnds() {
		return textEnds;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	// 10/7/2015
	public String toXML() {
		String xml = "    <annotation>\n";
		xml += "        <mention id=\"" + this.annotatedMentionID + "\" />\n";
		xml += "        <annotator id=\"" + this.annotatorID + "\">"
				+ this.annotatorID + "</annotator>\n";
		xml += "        <span start=\"" + this.textStarts[0] + "\" end=\""
				+ this.textEnds[0] + "\" />\n";
		String htmltext = "NOTEXT";
		if (this.getText() != null) {
			htmltext = StrUtils.replaceNonAlphaNumericCharactersWithSpaces(this
					.getText());
		}
		xml += "        <spannedText>" + htmltext + "</spannedText>\n";
		xml += "        <creationDate>" + this.creationDate
				+ "</creationDate>\n";
		xml += "    </annotation>";
		return xml;
	}

	public boolean isValid() {
		return isValid;
	}

}
