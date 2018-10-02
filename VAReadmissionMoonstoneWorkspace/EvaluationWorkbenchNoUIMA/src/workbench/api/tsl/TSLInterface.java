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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import annotation.EVAnnotation;
import annotation.SnippetAnnotation;
import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.document.DocumentTerm;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.inference.backwardchaining.Query;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.Sexp;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.gui.KnowledgeBasePanel;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;
import workbench.arr.AnnotationAnalysis;

public class TSLInterface {

	private AnnotationAnalysis analysis = null;
	private KnowledgeBase kb = null;
	private HashMap termMap = new HashMap();
	private Hashtable<String, Vector<EVAnnotation>> documentAnnotationMap = new Hashtable();
	private KnowledgeBasePanel knowledgeBasePanel = null;

	public TSLInterface(AnnotationAnalysis analysis) {
		this.analysis = analysis;
		this.kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.knowledgeBasePanel = new KnowledgeBasePanel(this.kb, false);
		this.knowledgeBasePanel.setVisible(false);
		Vector<Document> documents = analysis.getAllDocuments();
		Hashtable<Document, DocumentTerm> dtermHash = new Hashtable();
		if (documents != null) {
			for (Document document : documents) {
				DocumentTerm dterm = new DocumentTerm(document);
				dtermHash.put(document, dterm);
				ObjectConstant nterm = new ObjectConstant(document.getName());
				RelationSentence rs = RelationSentence.createRelationSentence(
						"document", dterm, nterm);
				kb.initializeAndAddForm(rs);
				termMap.put(document, dterm);
				termMap.put(dterm, document);
			}
		}
		if (analysis.getAllAnnotations() != null) {
			Vector<AnnotationTerm> terms = null;
			for (EVAnnotation annotation : analysis.getAllAnnotations()) {
				if (annotation.isSnippet()) {
					Document document = annotation.annotationCollection
							.getDocument();
					AnnotationTerm aterm = new AnnotationTerm(this, annotation,
							document);
					termMap.put(annotation, aterm);
					termMap.put(aterm, annotation);
					terms = VUtils.add(terms, aterm);
				}
			}
			if (terms != null) {
				for (AnnotationTerm term : terms) {
					term.addToKB();
				}
			}
		}
	}
	
//	public void doQueryFromTSLJTreePanel() throws Exception {
//		TSLSentenceGUI tjp = analysis.getArrTool().getTslJTreePanel();
//		if (tjp != null && tjp.getRootSentence() != null) {
//			Vector results = tjp.doQuery();
//			displayQueryAnnotations(results);
//		}
//	}

	public void doQueryFromString(String str) throws Exception {
		KnowledgeEngine.setDoQueryDebug(analysis.getArrTool()
				.getStartupParameters().isDoTSLQueryDebug());
		this.documentAnnotationMap.clear();
		String qstr = "'(query " + str + ")";
		Sexp s = (Sexp) JLisp.getJLisp().evalString(qstr);
		Vector qv = JLUtils.convertSexpToJVector(s);
		Query q = Query.createQuery(qv);
		Vector rv = (Vector) q.eval(null, true);
		System.out.println("Query Finished");
		displayQueryAnnotations(rv);
	}

	public void displayQueryAnnotations(Vector results) throws Exception {
		Hashtable<EVAnnotation, EVAnnotation> ahash = new Hashtable();
		this.documentAnnotationMap.clear();
		if (results != null) {
			for (Object o : results) {
				if (o instanceof Vector) {
					Vector v = (Vector) o;
					for (Object value : v) {
						if (value instanceof SnippetAnnotation) {
							SnippetAnnotation annotation = (SnippetAnnotation) value;
							if (annotation.getAnnotationCollection()
									.getAnnotator() == this.analysis
									.getSelectedAnnotator()) {
								ahash.put(annotation, annotation);
								AnnotationTerm aterm = (AnnotationTerm) termMap
										.get(annotation);
								Document doc = (Document) aterm.getDocument();
								VUtils.pushHashVector(
										this.documentAnnotationMap,
										doc.getName(), annotation);
							}
						}
					}
				}
			}
		}
		// 11/2/2014:  Deactivated.
//		if (!ahash.isEmpty()) {
//			this.analysis.getArrTool().getDocumentPane().setDisplayTSLQuery();
//			Vector<EVAnnotation> allAnnotations = HUtils.getKeys(ahash);
//			this.analysis.getArrTool().omDocumentPane.documentReportPane.reportListPane.annotationJTreePanel
//					.updateTree(allAnnotations);
//			this.analysis.getArrTool().getDocumentPane()
//					.highlightSentences(true);
//		}
	}
	
	public Object getMapObject(Object key) {
		return termMap.get(key);
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public AnnotationAnalysis getAnalysis() {
		return analysis;
	}

	public Vector<EVAnnotation> getQueryAnnotations(Document document) {
		return documentAnnotationMap.get(document.getName());
	}

	public KnowledgeBasePanel getKnowledgeBasePanel() {
		return knowledgeBasePanel;
	}

}

