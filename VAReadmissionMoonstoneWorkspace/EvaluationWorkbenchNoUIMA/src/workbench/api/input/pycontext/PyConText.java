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
package workbench.api.input.pycontext;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import tsl.documentanalysis.document.Document;
import tsl.utilities.JDomUtils;
import tsl.utilities.VUtils;
import workbench.api.Analysis;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.Annotator;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;

/*
 * PyConText XML information:  ConTextMarkup: nodes, edges.  
 * Node: category, tagObject (id, phrase, literal, category), modifies, 
 * modifiedBy (nodeid, category).  Edges: edge (edge: startnode, endnode).
 */

public class PyConText {
	Analysis analysis = null;
	Type type = null;
	Annotator annotator = null;
	public Vector<PCTDocument> pctdocs = null;
	int snippetCount = 0;
	static String typename = "category";
	static String classificationProperty = "category";
	static Vector<String> attributeNames = VUtils.arrayToVector(new String[] {
			"id", "literal", "phrase" });

	public PyConText(Analysis analysis, Annotator annotator,
			String dname) {
		this.analysis = analysis;
		this.annotator = annotator;
		extractTypeSystem();
		readDocuments(dname);
	}

	public Vector<AnnotationCollection> getAnnotationCollections() {
		Vector<AnnotationCollection> acs = null;
		if (this.pctdocs != null) {
			for (PCTDocument pct : this.pctdocs) {
				AnnotationCollection ac = pct.annotationCollection;
				acs = VUtils.add(acs, ac);
			}
		}
		return acs;
	}

	public TypeSystem extractTypeSystem() {
		TypeSystem ts = analysis.getTypeSystem();
		this.type = (Type) ts.getType(typename);
		if (this.type == null) {
			this.type = ts.getOrCreateType(null, typename);
			for (String aname : attributeNames) {
				this.type.addAttribute(aname);
			}
//			this.type = TypeSystem.addType(this.analysis, typename,
//					classificationProperty, attributeNames);
//			Classification pclass = this.type.getFirstClassification();
//			String astr = "category$category";
//			Attribute attribute = new Attribute(ts, astr, astr, null, null);
//			attribute.setParentTypeObject(pclass);
//			pclass.addAttribute(attribute);
		}
		return ts;
	}

	void readDocuments(String dname) {
		File file = new File(dname);
		if (file.exists() && file.isDirectory()) {
			File[] cfiles = file.listFiles();
			for (int i = 0; i < cfiles.length; i++) {
				File cfile = cfiles[i];
				if (cfile.isFile() && cfile.getName().endsWith(".xml")) {
					try {
						org.jdom.Document jdoc = new SAXBuilder()
								.build(cfiles[i].getAbsolutePath());
						Element root = jdoc.getRootElement();
						PCTDocument pct = readPCTDocument(root, cfile.getName());
						this.pctdocs = VUtils.add(this.pctdocs, pct);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	PCTDocument readPCTDocument(Element element, String fname) throws Exception {
		PCTDocument pct = this.new PCTDocument();
		pct.pyConText = this;
		pct.document = new Document();
		pct.document.analyzeSentencesNoHeader();
		pct.document.setName(fname);
		String text = element.getText().trim();
		pct.document.setText(text);
		AnnotationCollection ac = pct.annotationCollection = new AnnotationCollection();
		ac.setAnalysis(this.analysis);
		ac.setAnnotator(this.annotator);
//		ac.setDocument(pct.document);
		Vector<Element> ses = JDomUtils.getElementsByName(element, "section");
		if (ses != null) {
			for (Element se : ses) {
				Section section = readSection(se, pct);
				pct.sections = VUtils.add(pct.sections, section);
			}
		}
		if (pct.sections != null) {
			for (Section section : pct.sections) {
				if (section.sentences != null && section.conTextMarkups != null) {
					for (int i = 0; i < section.sentences.size(); i++) {
						Sentence sentence = section.sentences.elementAt(i);
						ConTextMarkup ctm = section.conTextMarkups.elementAt(i);
						if (ctm.nodes != null) {
							for (Node node : ctm.nodes) {
								node.tagObject.createSnippet(sentence);
							}
						}
					}
				}
			}
		}
		if (pct.sections != null) {
			for (Section section : pct.sections) {
				if (section.conTextMarkups != null) {
					for (ConTextMarkup ctm : section.conTextMarkups) {
						if (ctm.nodes != null) {
							for (Node node : ctm.nodes) {
								if (node.modifiesID != null) {
									node.modifiesNode = pct.nodeIDHash
											.get(node.modifiesID);
									if (node.modifiesNode != null) {
//										node.snippet.setRelation("modifies",
//												node.modifiesNode.snippet);
//										node.modifiesNode.snippet.setRelation(
//												"modifiedBy", node.snippet);
									}
								}
								if (node.modifiedByID != null) {
									node.modifiedByNode = pct.nodeIDHash
											.get(node.modifiedByID);
									if (node.modifiedByNode != null) {
//										node.snippet.setRelation("modifiedBy",
//												node.modifiedByNode.snippet);
//										node.modifiedByNode.snippet
//												.setRelation("modifies",
//														node.snippet);
									}
								}
							}
						}
						if (ctm.edges != null) {
							for (Edge edge : ctm.edges) {
								if (edge.startNodeID != null 
										&& edge.endNodeID != null) {
									edge.startNode = pct.nodeIDHash.get(edge.startNodeID);
									edge.endNode = pct.nodeIDHash.get(edge.endNodeID);
								}
								if (edge.startNode != null
										&& edge.endNode != null) {
//									edge.startNode.snippet.setRelation(
//											"EdgeTo", edge.endNode.snippet);
//									edge.endNode.snippet.setRelation(
//											"EdgeFrom", edge.startNode.snippet);
								}
							}
						}
					}
				}
			}
		}
//		ac.storeAnalysisIndices();
		return pct;
	}

	Section readSection(Element element, PCTDocument pct) {
		Section section = this.new Section();
		section.pctDocument = pct;
		section.sectionLabel = JDomUtils
				.getValueByName(element, "sectionLabel");
		if (section.sectionLabel != null) {
			section.sectionLabel = section.sectionLabel.trim();
		}
		Vector<Element> ses = JDomUtils.getElementsByName(element, "sentence");
		if (ses != null) {
			for (Element se : ses) {
				Sentence s = readSentence(se, section);
				section.sentences = VUtils.add(section.sentences, s);
			}
		}
		Vector<Element> ctmes = JDomUtils.getElementsByName(element,
				"ConTextMarkup");
		if (ctmes != null) {
			for (Element ctme : ctmes) {
				ConTextMarkup ctm = readConTextMarkup(ctme, section);
				section.conTextMarkups = VUtils
						.add(section.conTextMarkups, ctm);
			}
		}
		return section;
	}

	Sentence readSentence(Element element, Section section) {
		Sentence s = this.new Sentence();
		s.section = section;
		String nstr = JDomUtils.getValueByName(element, "sentenceNumber");
		if (nstr != null) {
			s.sentenceNumber = Integer.parseInt(nstr.trim());
		}
		nstr = JDomUtils.getValueByName(element, "sentenceOffset");
		if (nstr != null) {
			s.sentenceOffset = Integer.parseInt(nstr.trim());
		}
		return s;
	}

	ConTextMarkup readConTextMarkup(Element element, Section section) {
		ConTextMarkup ctm = this.new ConTextMarkup();
		ctm.section = section;
		ctm.rawText = JDomUtils.getValueByName(element, "rawText");
		if (ctm.rawText != null) {
			ctm.rawText = ctm.rawText.trim();
		}
		Vector<Element> nes = JDomUtils.getElementsByName(element, "node");
		if (nes != null) {
			for (Element ne : nes) {
				Node node = readNode(ne, ctm);
				ctm.nodes = VUtils.add(ctm.nodes, node);
			}
		}
		Vector<Element> ees = JDomUtils.getElementsByName(element, "edges");
		if (ees != null) {
			for (Element es : ees) {
				Edge edge = readEdge(es, ctm);
				ctm.edges = VUtils.add(ctm.edges, edge);
			}
		}
		return ctm;
	}

	Node readNode(Element element, ConTextMarkup ctm) {
		Node node = this.new Node();
		node.ctm = ctm;
		node.category = JDomUtils.getValueByName(element, "category");
		if (node.category != null) {
			node.category = node.category.trim();
		}
		Element ne = JDomUtils.getElementByName(element, "tagObject");
		node.tagObject = readTagObject(ne, node);
		node.modifiedByID = JDomUtils.getValueByName(element, "modifiedNode");
		node.modifiesID = JDomUtils.getValueByName(element, "modifiesNode");
		if (node.modifiedByID != null) {
			node.modifiedByID = node.modifiedByID.trim();
		}
		if (node.modifiesID != null) {
			node.modifiesID = node.modifiesID.trim();
		}
		return node;
	}

	Edge readEdge(Element element, ConTextMarkup ctm) {
		Edge edge = this.new Edge();
		edge.ctm = ctm;
		String str = JDomUtils.getValueByName(element, "startNode");
		if (str != null) {
			edge.startNodeID = str.trim();
		}
		str = JDomUtils.getValueByName(element, "endNode");
		if (str != null) {
			edge.endNodeID = str.trim();
		}
		return edge;
	}

	TagObject readTagObject(Element element, Node node) {
		TagObject to = this.new TagObject();
		to.node = node;
		to.cid = JDomUtils.getValueByName(element, "id");
		if (to.cid != null) {
			to.cid = to.cid.trim();
		}
		node.ctm.section.pctDocument.nodeIDHash.put(to.cid, node);
		to.phrase = JDomUtils.getValueByName(element, "phrase");
		if (to.phrase != null) {
			to.phrase = to.phrase.trim();
		}
		to.literal = JDomUtils.getValueByName(element, "literal");
		if (to.literal != null) {
			to.literal = to.literal.trim();
		}
		to.category = JDomUtils.getValueByName(element, "category");
		if (to.category != null) {
			to.category = to.category.trim();
		}
		String nstr = JDomUtils.getValueByName(element, "spanStart");
		if (nstr != null) {
			to.spanStart = Integer.parseInt(nstr.trim());
		}
		nstr = JDomUtils.getValueByName(element, "spanStop");
		if (nstr != null) {
			to.spanStop = Integer.parseInt(nstr.trim());
		}
		nstr = JDomUtils.getValueByName(element, "scopeStart");
		if (nstr != null) {
			to.scopeStart = Integer.parseInt(nstr.trim());
		}
		nstr = JDomUtils.getValueByName(element, "scopeStop");
		if (nstr != null) {
			to.scopeStop = Integer.parseInt(nstr.trim());
		}
		return to;
	}

	public class PCTDocument {
		PyConText pyConText = null;
		public Document document = null;
		public AnnotationCollection annotationCollection = null;
		Vector<Section> sections = null;
		Hashtable<String, Node> nodeIDHash = new Hashtable();
	}

	class Section {
		PCTDocument pctDocument = null;
		String sectionLabel = null;
		Vector<Sentence> sentences = null;
		Vector<ConTextMarkup> conTextMarkups = null;
	}

	class Sentence {
		Section section = null;
		int sentenceNumber = -1;
		int sentenceOffset = -1;
	}

	class ConTextMarkup {
		Section section = null;
		String rawText = null;
		Vector<Node> nodes = null;
		Vector<Edge> edges = null;
	}

	class Node {
		Annotation snippet = null;
		ConTextMarkup ctm = null;
		String category = null;
		TagObject tagObject = null;
		String modifiesID = null;
		String modifiedByID = null;
		Node modifiesNode = null;
		Node modifiedByNode = null;
	}

	class Edge {
		ConTextMarkup ctm = null;
		Node startNode = null;
		String startNodeID = null;
		Node endNode = null;
		String endNodeID = null;
	}

	class TagObject {
		Sentence sentence = null;
		Node node = null;
		String cid = null;
		String phrase = null;
		String literal = null;
		String category = null;
		int spanStart = -1;
		int spanStop = -1;
		int scopeStart = -1;
		int scopeStop = -1;

		Annotation createSnippet(Sentence sentence) {
			this.sentence = sentence;
			AnnotationCollection ac = this.node.ctm.section.pctDocument.annotationCollection;
			Annotation snippet = new Annotation(ac, type);
			this.node.snippet = snippet;
			snippet.setId("snippet_" + snippetCount++);
			snippet.setText(this.phrase);

			int start = this.spanStart + sentence.sentenceOffset;
			int end = this.spanStop + sentence.sentenceOffset - 1;
			snippet.addSpan(start, end);

//			String cname = type.getClassificationName();
//			TypeSystem ts = analysis.getArrTool().getTypeSystem();
//			Classification pclass = (Classification) ts
//					.getUimaClassification(cname);
//			String fullname = type.getName() + "$" + classificationProperty;
//			annotation.Classification c = new annotation.Classification(ac,
//					snippet, pclass, fullname, this.category, null);
//			snippet.setClassification(c);
			
			snippet.putAttributeValue("id", this.cid);
			snippet.putAttributeValue("literal", this.literal);
			snippet.putAttributeValue("phrase", this.phrase);
		
			return snippet;
		}

	}

}

