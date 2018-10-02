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
package workbench.api;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import annotation.EVAnnotation;
import annotation.RelationObject;
import tsl.documentanalysis.document.Document;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import workbench.api.annotation.Annotation;
import workbench.api.annotation.AnnotationCollection;
import workbench.api.annotation.AnnotationEvent;
import workbench.api.annotation.Annotator;
import workbench.api.annotation.OverlappingAnnotationPair;
import workbench.api.annotation.Span;
import workbench.api.constraint.ConstraintMatch;
import workbench.api.constraint.ConstraintPacket;
import workbench.api.gui.WBGUI;
import workbench.api.input.FormatType;
import workbench.api.input.graf.GRAF;
import workbench.api.input.knowtator.Knowtator;
import workbench.api.input.knowtator.KnowtatorIO;
import workbench.api.typesystem.Attribute;
import workbench.api.typesystem.Classification;
import workbench.api.typesystem.Type;
import workbench.api.typesystem.TypeSystem;
import workbench.arr.EvaluationWorkbench;
import workbench.arr.StartupParameters;

public class Analysis {

	private WBGUI workbenchGUI = null;
	private TypeSystem typeSystem = null;
	private Vector<OverlappingAnnotationPair> allOverlappingAnnotationPairs = null;
	private Vector<Annotation> allPrimarySnippetAnnotations = null;
	private Vector<Annotation> allSecondarySnippetAnnotations = null;
	private Vector<Annotation> allSnippetAnnotations = null;
	private Vector<AnnotationEvent> allAnnotationEvents = null;
	private KnowledgeEngine knownledgeEngine = null;
	private Hashtable<String, ConstraintPacket> constraintPacketHash = new Hashtable();
	private Hashtable<Object, ConstraintMatch> matchedConstraintHash = new Hashtable();
	private Hashtable<String, Document> namedDocumentHash = new Hashtable();
	private Hashtable<String, AnnotationEvent> documentAnnotationEventHash = new Hashtable();
	private Annotator primaryAnnotator = null;
	private Annotator secondaryAnnotator = null;
	private ConstraintMatch selectedConstraintMatch = null;
	private ConstraintPacket selectedConstraintPacket = null;
	private Annotation selectedAnnotation = null;
	private Document selectedDocument = null;
	private Annotator selectedAnnotator = null;
	private Hashtable<String, Vector<AnnotationCollection>> documentAnnotationCollectionMap = new Hashtable();
	
	private Hashtable<String, String> completeDocumentNameMap = new Hashtable();
	
	private Vector<Annotation> knowtatorAnnotations = null;
	private KnowtatorIO knowtatorIO = null;
	private Vector<String> allClassificationValues = null;
	private Vector<Annotation> allAnnotations = null;
	private Hashtable<String, Vector<OverlappingAnnotationPair>> matchedClassificationOverlappingPairHash = new Hashtable();
	private WorkbenchAPIObject userSelectedWorkbenchAPIObject = null;
	private Vector<AnnotationCollection> allAnnotationCollections = null;
	private Hashtable<EVAnnotation, Annotation> oldToNewAnnotationHash = new Hashtable();
	private boolean isSingleAnnotationSet = false;
	private boolean readAnnotationCollectionFileIsPrimary = false;

	// 12/30/2015
	private Hashtable<String, String> FPToTPTextHash = new Hashtable();

	// 9/22/2014
	private Hashtable<String, String> namedDocumentTextHash = new Hashtable();

	public static Analysis CurrentAnalysis = null;

	public Analysis() throws Exception {
		this(null);
	}

	public Analysis(WBGUI wbgui, EvaluationWorkbench arrtool) throws Exception {
		CurrentAnalysis = this;
		this.workbenchGUI = wbgui;
		this.knownledgeEngine = KnowledgeEngine.getCurrentKnowledgeEngine(true);
		this.primaryAnnotator = new Annotator(arrtool.getAnalysis()
				.getPrimaryAnnotator().getName(), true);
		this.secondaryAnnotator = new Annotator(arrtool.getAnalysis()
				.getSecondaryAnnotator().getName(), false);
		this.createTypeSystem(arrtool);
		this.readAnnotationsFromOldWorkbench(arrtool);
		this.gatherOverlappingAnnotations();
		this.gatherAllClassifications();
		Constraint.initialize();
		this.initializeAllDefinedConstraintMatches();
	}

	// 8/7/2014: WEW
	public Analysis(TypeSystem ts) throws Exception {
		CurrentAnalysis = this;
		this.knownledgeEngine = KnowledgeEngine.getCurrentKnowledgeEngine(true);
		if (ts == null) {
			ts = new TypeSystem(this);
			ts.getRootType();
		}
		this.typeSystem = ts;
		if (this.primaryAnnotator == null) {
			String firstAnnotator = this.knownledgeEngine
					.getStartupParameters().getPropertyValue("firstAnnotator");
			String secondAnnotator = this.knownledgeEngine
					.getStartupParameters().getPropertyValue("secondAnnotator");

			this.primaryAnnotator = new Annotator(firstAnnotator, true);
			this.secondaryAnnotator = new Annotator(secondAnnotator, false);
		}
		// 3/9/2015: So that I can treat a single annotation set as both
		// primary and secondary.
		String primaryAnnotationDirectory = this.knownledgeEngine
				.getStartupParameters()
				.getPropertyValue(
						StartupParameters.AnnotationInputDirectoryFirstAnnotator);
		String secondaryAnnotationDirectory = this.knownledgeEngine
				.getStartupParameters()
				.getPropertyValue(
						StartupParameters.AnnotationInputDirectorySecondAnnotator);
		if (primaryAnnotationDirectory != null
				&& secondaryAnnotationDirectory != null
				&& primaryAnnotationDirectory
						.equals(secondaryAnnotationDirectory)) {
			this.isSingleAnnotationSet = true;
		}
		this.readFPToTPAnnotationTextsFromFile();
	}

	public void readAnnotationsFromOldWorkbench(EvaluationWorkbench arrtool) {
		for (workbench.arr.AnnotationEvent evae : arrtool.getAnalysis()
				.getAnnotationEvents()) {
			AnnotationEvent ae = new AnnotationEvent(this, evae);
			this.allAnnotationEvents = VUtils.add(this.allAnnotationEvents, ae);
			// this.namedDocumentHash.put(ae.getDocument().getName(),
			// ae.getDocument());
			this.putNamedDocumentHash(ae.getDocument().getName(),
					ae.getDocument());
			this.documentAnnotationEventHash
					.put(ae.getDocument().getName(), ae);

			AnnotationCollection primary = ae.getPrimaryAnnotationCollection();
			AnnotationCollection secondary = ae
					.getSecondaryAnnotationCollection();
			this.allAnnotations = VUtils.append(this.allAnnotations,
					primary.getAnnotations());
			this.allAnnotations = VUtils.append(this.allAnnotations,
					secondary.getAnnotations());
			this.allAnnotationCollections = VUtils.add(
					this.allAnnotationCollections, primary);
			this.allAnnotationCollections = VUtils.add(
					this.allAnnotationCollections, secondary);
		}
		resolveOldWorkbenchRelations();
	}

	public void resolveOldWorkbenchRelations() {
		for (Enumeration<EVAnnotation> e = this.oldToNewAnnotationHash.keys(); e
				.hasMoreElements();) {
			EVAnnotation eva = e.nextElement();
			if (eva.getRelationObjects() != null) {
				Annotation annotation = this.oldToNewAnnotationHash.get(eva);
				for (RelationObject ro : eva.getRelationObjects()) {
					Annotation relatum = this.oldToNewAnnotationHash.get(ro
							.getRelatum());
					annotation.addRelation(ro.getRelation(), annotation,
							relatum);
				}
			}
		}
	}

	public void createTypeSystem(EvaluationWorkbench arrtool) throws Exception {
		TypeSystem newts = new TypeSystem(this);
		typesystem.TypeSystem oldts = arrtool.getTypeSystem();
		if (oldts.getAllAnnotationTypes() != null) {
			for (typesystem.Annotation ta : oldts.getAllAnnotationTypes()) {
				newts.addType(ta);
			}
		}
		for (EVAnnotation ann : arrtool.getAnalysis().getAllAnnotations()) {
			annotation.Classification c = ann.getClassification();
			String tname = c.getParentAnnotationType().getName();
			Type type = (Type) newts.getObjectHash(tname);
			String cname = c.getName();
			String cvalue = c.getValue().toString();
			cname = TypeSystem.eliminatePrepend(cname);
			String fullaname = type.getFullname() + ":" + cname;
			Attribute attr = (Attribute) newts.getObjectHash(fullaname);
			if (attr == null) {
				attr = new Classification(type, cname);
			}
			attr.addValue(cvalue);
			if (ann.getAttributes() != null) {
				for (String aname : ann.getAttributes()) {
					Object value = ann.getAttribute(aname);
					int index = aname.indexOf('$');
					if (index > 0) {
						aname = aname.substring(index + 1);
					}
					fullaname = type.getFullname() + ":" + aname;
					attr = (Attribute) newts.getObjectHash(fullaname);
					if (attr == null) {
						attr = new Attribute(type, aname);
					}
					attr.addValue(value);
				}
			}
		}
		this.setTypeSystem(newts);
	}

	public void addAnnotationCollection(String docname, String afilename,
			String fstr, String format, boolean isPrimary, KnowtatorIO kio,
			String doctext) throws Exception {
		boolean storedIsPrimary = this.readAnnotationCollectionFileIsPrimary;
		this.readAnnotationCollectionFileIsPrimary = isPrimary;
		format = format.toLowerCase();
		if (format.equals(FormatType.GRAF.toString().toLowerCase())) {
			AnnotationCollection ac = new AnnotationCollection();
			ac.setPrimary(isPrimary);
			GRAF.readXML(ac, fstr, isPrimary);
			this.pushDocumentAnnotationCollectionMap(docname, ac);
//			VUtils.pushHashVector(this.documentAnnotationCollectionMap,
//					docname, ac);
		} else if (format.equals(FormatType.Knowtator.toString().toLowerCase())) {
			// 3/9/2015
			if (this.isSingleAnnotationSet()) {
				Annotator annotator = (isPrimary ? this.getPrimaryAnnotator()
						: this.getSecondaryAnnotator());
				kio.setAnnotator(annotator);
				Knowtator.readAnnotationFile(kio, docname, fstr, true);
				kio.setAnnotator(null);
			} else {
				Knowtator.readAnnotationFile(kio, docname, fstr, true);
			}
		}
		if (doctext != null) {
			this.putNamedDocumentTextHash(docname, doctext);
		}

		// 1/25/2016: Why did I do this?
		// this.readAnnotationCollectionFileIsPrimary = false;
		
		this.readAnnotationCollectionFileIsPrimary = storedIsPrimary;
	}
	
	public void postProcessAnnotationCollections() throws Exception {
		int x = 2;
		for (Enumeration<String> e = this.documentAnnotationCollectionMap
				.keys(); e.hasMoreElements();) {
			String dname = e.nextElement();
			Vector<AnnotationCollection> v = this.getDocumentAnnotationCollections(dname);
			if (v != null) {
				if (v.size() != 2) {
					x = 1;
				} else {
					x = 1;
				}
				AnnotationEvent ae = new AnnotationEvent(this, (Document) null,
						dname);
				
				// 3/20/2017:  PROBLEM: I'm now working with thousands of documents...
				String dtext = this.getNamedDocumentText(dname);
				if (dtext != null) {
					Document doc = new Document(dname, dtext);
					ae.setDocument(doc);
					this.putNamedDocumentHash(dname, doc);
				}
				AnnotationCollection primary = null;
				AnnotationCollection secondary = null;
				for (AnnotationCollection ac : v) {
					if (ac.isPrimary()) {
						primary = ac;
						if (ac.getAnnotations() != null && !ac.getAnnotations().isEmpty()) {
							x = 1;
						}
					} else {
						secondary = ac;
					}
				}
				if (primary == null) {
					primary = new AnnotationCollection(null, ae, true);
				}
				if (secondary == null) {
					secondary = new AnnotationCollection(null, ae, false);
				}

				if (secondary.isPrimary()) {
					System.out.println("Analysis: Botched primary/secondary AC assignment!!");
				}

				ae.setPrimaryAnnotationCollection(primary);
				ae.setSecondaryAnnotationCollection(secondary);

				this.allAnnotations = VUtils.append(this.allAnnotations,
						primary.getAnnotations());
				this.allAnnotations = VUtils.append(this.allAnnotations,
						secondary.getAnnotations());

				this.allAnnotationCollections = VUtils.add(
						this.allAnnotationCollections, primary);
				this.allAnnotationCollections = VUtils.add(
						this.allAnnotationCollections, secondary);

				this.documentAnnotationEventHash.put(dname, ae);
			}
		}
		this.allAnnotationEvents = HUtils
				.getElements(this.documentAnnotationEventHash);
		if (this.allAnnotationEvents != null) {
			Collections.sort(this.allAnnotationEvents,
					new AnnotationEvent.DocumentNameSorter());
			this.gatherOverlappingAnnotations();
			this.gatherAllClassifications();
		} else {
			System.out.println("WARNING:  Missing annotation events");
		}
	}

	public void updateStatistics() {

		// TEST: 10/20/2014
		this.clearMatchedConstraintHash();

		WorkbenchAPIObject wao = this.getUserSelectedWorkbenchAPIObject();
		ConstraintPacket cp = this.selectedConstraintPacket;
		if (wao != null && cp != null) {
			Vector key = ConstraintMatch.getHashKey(wao, cp);
			ConstraintMatch mca = this.matchedConstraintHash.get(key);
			if (mca == null) {
				mca = new ConstraintMatch(this, cp);
				mca.applyConstraintToMatchedPairs();
				this.matchedConstraintHash.put(mca.getHashKey(), mca);
			}
			this.setSelectedConstraintMatch(mca);
			if (this.getWorkbenchGUI() != null) {
				this.getWorkbenchGUI().getStatisticsPanel().getModel()
						.fireTableDataChanged();
			}
		}
	}

	public void initializeAllDefinedConstraintMatches() throws Exception {
		// for (int i = 0; i < ConstraintPanel.constraintDefinitions.length;
		// i++) {
		// String name = ConstraintPanel.constraintDefinitions[i][0];
		// String expr = ConstraintPanel.constraintDefinitions[i][1];
		// ConstraintPacket.createConstraintPacket(this.getKnownledgeBase(),
		// name, expr, this);
		// }
	}

	public void addConstraintPacket(ConstraintPacket cp) {
		this.constraintPacketHash.put(cp.getName(), cp);
	}

	public Vector<ConstraintMatch> getAllMatchedConstraints() {
		return HUtils.getElements(this.matchedConstraintHash);
	}

	// Updated 1/13/2016
	public void gatherAllClassifications() {
		// Could get this from Readmission table...
		String cstr = this.getKnownledgeEngine().getStartupParameters()
				.getPropertyValue("TargetWorkbenchConcepts");
		Vector<String> cv = null;
		if (cstr != null) {
			cv = VUtils.arrayToVector(cstr.split(","));
			this.allClassificationValues = cv;
		} else {
			Hashtable chash = new Hashtable();
			for (Annotation annotation : this.allSnippetAnnotations) {
				Object value = annotation.getClassificationValue();
				if (value instanceof String
						&& (cv == null || cv.contains(value))) {
					chash.put((String) value, value);
				}
			}
			this.allClassificationValues = HUtils.getKeys(chash);
		}
	}

	// 9/27/2014: I'm currently just gathering snippets. I need a more general
	// way
	// to handle levels.
	public void gatherOverlappingAnnotations() {
		clearAll();
		if (this.allAnnotationEvents != null) {
			this.allPrimarySnippetAnnotations = null;
			this.allSecondarySnippetAnnotations = null;
			for (AnnotationEvent ae : this.allAnnotationEvents) {
				AnnotationCollection pac = ae.getPrimaryAnnotationCollection();
				AnnotationCollection sac = ae
						.getSecondaryAnnotationCollection();
				pac.gatherOverlappingAnnotationGroups();
				sac.gatherOverlappingAnnotationGroups();
				this.allPrimarySnippetAnnotations = VUtils.append(
						this.allPrimarySnippetAnnotations,
						pac.getSnippetAnnotations());
				this.allSecondarySnippetAnnotations = VUtils.append(
						this.allSecondarySnippetAnnotations,
						sac.getSnippetAnnotations());
			}
			if (this.allPrimarySnippetAnnotations != null) {
				// 2/15/2018
				for (Annotation primary : this.allPrimarySnippetAnnotations) {
					primary.setValidatedPrimaryAnnotation();
				}
				Collections.sort(this.allPrimarySnippetAnnotations,
						new Annotation.DocumentPositionSorter());
			}
			if (this.allSecondarySnippetAnnotations != null) {
				Collections.sort(this.allSecondarySnippetAnnotations,
						new Annotation.DocumentPositionSorter());
			}
			this.allSnippetAnnotations = VUtils.appendNew(
					this.allPrimarySnippetAnnotations,
					this.allSecondarySnippetAnnotations);
			Collections.sort(this.allSnippetAnnotations,
					new Annotation.DocumentPositionSorter());
			for (AnnotationEvent ae : this.allAnnotationEvents) {
				gatherOverlappingAnnotations(ae);
			}
		}
	}

	public void gatherOverlappingAnnotations(AnnotationEvent ae) {
		AnnotationCollection pac = ae.getPrimaryAnnotationCollection();
		AnnotationCollection sac = ae.getSecondaryAnnotationCollection();
		if (pac.getSnippetAnnotations() == null
				|| sac.getSnippetAnnotations() == null) {
			return;
		}
		String key = ae.getDocumentName();
		for (Annotation a1 : pac.getSnippetAnnotations()) {
			for (Annotation a2 : sac.getSnippetAnnotations()) {
				if (a1.getEnd() < a2.getStart()) {
					continue;
				}
				if (a2.getStart() > a1.getEnd()) {
					break;
				}
				int overlap = AnnotationCollection.getAnnotationOverlap(a1, a2);
				boolean hasStrictOverlap = AnnotationCollection
						.hasStrictOverlap(a1, a2);
				if (overlap > 0) {
					OverlappingAnnotationPair pair = new OverlappingAnnotationPair(
							a1, a2, hasStrictOverlap);
					if (a1.getClassificationValue() != null) {
						VUtils.pushHashVector(
								matchedClassificationOverlappingPairHash,
								a1.getClassificationValue(), pair);
					} else {
						int x = 1;
					}
					this.allOverlappingAnnotationPairs = VUtils.add(
							this.allOverlappingAnnotationPairs, pair);
				}
			}
		}
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

	public void clearAll() {
		this.allOverlappingAnnotationPairs = null;
		this.allPrimarySnippetAnnotations = null;
		this.allSecondarySnippetAnnotations = null;
		this.allClassificationValues = null;
		this.matchedClassificationOverlappingPairHash = new Hashtable();
		this.matchedConstraintHash = new Hashtable();
	}

	public void clearMatchedConstraintHash() {
		this.matchedConstraintHash = new Hashtable();
	}

	public Vector<OverlappingAnnotationPair> getAllOverlappingAnnotationPairs() {
		return allOverlappingAnnotationPairs;
	}

	// public Vector<Annotation> getAllUnmatchedPrimarySingletonAnnotations() {
	// return allUnmatchedPrimarySingletonAnnotations;
	// }
	//
	// public Vector<Annotation> getAllUnmatchedSecondarySingletonAnnotations()
	// {
	// return allUnmatchedSecondarySingletonAnnotations;
	// }

	public TypeSystem getTypeSystem() {
		return typeSystem;
	}

	public void setTypeSystem(TypeSystem typeSystem) {
		this.typeSystem = typeSystem;
	}

	public Vector<Document> getAllDocuments() {
		Vector<Document> documents = HUtils.getElements(this.namedDocumentHash);
		Collections.sort(documents, new Document.NameSorter());
		return documents;
	}

	public Document getDocument(String dname) {
		Document doc = this.getNamedDocument(dname);
		return doc;
	}

	public AnnotationEvent getAnnotationEvent(String dname) {
		return this.documentAnnotationEventHash.get(dname);
	}

	public Vector<AnnotationEvent> getAllAnnotationEvents() {
		return this.allAnnotationEvents;
	}

	public Annotation getSelectedAnnotation() {
		return selectedAnnotation;
	}

	public void setSelectedAnnotation(Annotation selectedAnnotation)
			throws Exception {
		this.selectedAnnotation = selectedAnnotation;

		// 9/23/2014: For later...
		// if (this.getSelectedConstraintMatch() != null) {
		// int row = this.getSelectedConstraintMatch()
		// .getAnnotationClassificationRow(selectedAnnotation);
		// this.workbenchGUI.getStatisticsPanel()
		// .externallyChangeSelectedPosition(row);
		// }
	}

	public ConstraintMatch getSelectedConstraintMatch() {
		return selectedConstraintMatch;
	}

	public void setSelectedConstraintMatch(
			ConstraintMatch selectedConstraintMatch) {
		this.selectedConstraintMatch = selectedConstraintMatch;
	}

	public Document getSelectedDocument() {
		return selectedDocument;
	}

	public void setSelectedDocument(Document selectedDocument) {
		this.selectedDocument = selectedDocument;
	}

	public Annotator getPrimaryAnnotator() {
		return primaryAnnotator;
	}

	public void setPrimaryAnnotator(Annotator primaryAnnotator) {
		this.primaryAnnotator = primaryAnnotator;
	}

	public Annotator getSecondaryAnnotator() {
		return secondaryAnnotator;
	}

	public void setSecondaryAnnotator(Annotator secondaryAnnotator) {
		this.secondaryAnnotator = secondaryAnnotator;
	}

	public WBGUI getWorkbenchGUI() {
		return workbenchGUI;
	}

	public void setWorkbenchGUI(WBGUI workbenchGUI) {
		this.workbenchGUI = workbenchGUI;
	}

	public Annotator getSelectedAnnotator() {
		return selectedAnnotator;
	}

	public void setSelectedAnnotator(Annotator selectedAnnotator) {
		this.selectedAnnotator = selectedAnnotator;
	}

	public AnnotationEvent getSelectedAnnotationEvent() {
		if (this.selectedDocument != null) {
			return this.documentAnnotationEventHash.get(this.selectedDocument
					.getName());
		}
		return null;
	}

	public AnnotationCollection getSelectedAnnotationCollection() {
		AnnotationEvent ae = this.getSelectedAnnotationEvent();
		if (this.selectedAnnotator != null && ae != null) {
			return ae.getAnnotationCollection(this.selectedAnnotator);
		}
		return null;
	}

	public AnnotationCollection getNonselectedAnnotationCollection() {
		AnnotationEvent ae = this.getSelectedAnnotationEvent();
		if (this.selectedAnnotator != null && ae != null) {
			AnnotationCollection ac = ae
					.getAnnotationCollection(this.selectedAnnotator);
			if (ac.isPrimary()) {
				return ae.getSecondaryAnnotationCollection();
			}
			return ae.getPrimaryAnnotationCollection();
		}
		return null;
	}

	public KnowledgeEngine getKnownledgeEngine() {
		return knownledgeEngine;
	}

	public KnowledgeBase getKnownledgeBase() {
		return this.knownledgeEngine.getCurrentKnowledgeBase();
	}

	public ConstraintPacket getConstraintPacket(String name) {
		return this.constraintPacketHash.get(name);
	}

	public KnowtatorIO getKnowtatorIO() {
		return knowtatorIO;
	}

	public void setKnowtatorIO(KnowtatorIO knowtatorIO) {
		this.knowtatorIO = knowtatorIO;
	}

	public void appendKnowtatorAnnotations(Vector<Annotation> annotations) {
		this.knowtatorAnnotations = VUtils.append(this.knowtatorAnnotations,
				annotations);
	}

	public void attachDocumentsToKnowtatorAnnotationCollections(
			Annotator annotator) throws Exception {
		if (this.knowtatorIO.getAnnotationCollections() != null) {
			for (AnnotationCollection ac : this.knowtatorIO
					.getAnnotationCollections()) {

				if (annotator.getNames().contains(ac.getAnnotatorID())) {
					ac.setAnnotator(annotator);
				}

				// Before 11/10/2015
				// if (ac.getAnnotatorID().equals(annotator.getName())
				// || ac.getAnnotatorName().equals(annotator.getName())) {
				// ac.setAnnotator(annotator);
				// }
			}
		}
	}

	public void pushDocumentAnnotationCollectionMap(Object key,
			AnnotationCollection ac) {
		String str = StrUtils.removeNonAlphaDigitCharacters(key.toString());
		VUtils.pushIfNotHashVector(this.documentAnnotationCollectionMap, str,
				ac);
		this.completeDocumentNameMap.put(str, key.toString());
	}
	
	public String getNoncompressedDocumentName(String key) {
		return this.completeDocumentNameMap.get(key);
	}
	
	public Vector<AnnotationCollection> getDocumentAnnotationCollections(Object key) {
		// 12/18/2017:  Messed up text names in final set.
		String str = StrUtils.removeNonAlphaDigitCharacters(key.toString());
		return this.documentAnnotationCollectionMap.get(str);
	}

	public Vector<String> getAllClassificationValues() {
		return allClassificationValues;
	}

	public Vector<OverlappingAnnotationPair> getOverlappingAnnotationPairsByClassification(
			String classification) {
		return this.matchedClassificationOverlappingPairHash
				.get(classification);
	}

	public Vector<Annotation> getAllPrimarySnippetAnnotations() {
		return allPrimarySnippetAnnotations;
	}

	public Vector<Annotation> getAllSecondarySnippetAnnotations() {
		return allSecondarySnippetAnnotations;
	}

	public Vector<Annotation> getAllSnippetAnnotations() {
		return allSnippetAnnotations;
	}

	public Vector<String> getAllUserSelectionValues() {
		WorkbenchAPIObject wbo = this.getUserSelectedWorkbenchAPIObject();
		if (wbo instanceof Type) {
			Type type = (Type) wbo;
			if (type.isRoot()) {
				return this.getAllClassificationValues();
			}
			return type.getAlternativeValues();
		}
		if (wbo instanceof Attribute) {
			Attribute attr = (Attribute) wbo;
			return attr.getValues();
		}
		return null;
	}

	public WorkbenchAPIObject getUserSelectedWorkbenchAPIObject() {
		if (this.userSelectedWorkbenchAPIObject == null) {
			if (this.getTypeSystem() != null
					&& this.getTypeSystem().getRootType() != null) {
				this.setUserSelectedWorkbenchAPIObject(this.getTypeSystem()
						.getRootType());
			}
		}
		return userSelectedWorkbenchAPIObject;
	}

	public void setUserSelectedWorkbenchAPIObject(WorkbenchAPIObject wao) {
		this.userSelectedWorkbenchAPIObject = wao;
		this.setDefaultConstraintPacket(wao);
	}

	// 10/10/2014
	public void setDefaultConstraintPacket(WorkbenchAPIObject wao) {
		ConstraintPacket dcp = null;
		if (wao instanceof Type) {
			dcp = this
					.getConstraintPacket(ConstraintPacket.DefaultClassificationConstraintName);
		} else if (wao instanceof Attribute) {
			dcp = this
					.getConstraintPacket(ConstraintPacket.DefaultAttributeValueConstraintName);
		}
		this.setSelectedConstraintPacket(dcp);
	}

	public ConstraintPacket getSelectedConstraintPacket() {
		return selectedConstraintPacket;
	}

	public void setSelectedConstraintPacket(ConstraintPacket cp) {
		if (cp != null) {
			this.selectedConstraintPacket = cp;
		}
	}

	public void setSelectedConstraintPacket(String cpname) {
		this.selectedConstraintPacket = this.getConstraintPacket(cpname);
	}

	public ConstraintMatch getConstraintMatch(String cmname) {
		for (Enumeration<ConstraintMatch> e = this.matchedConstraintHash
				.elements(); e.hasMoreElements();) {
			ConstraintMatch cm = e.nextElement();
			if (cmname.equals(cm.getConstraintPacket().getName())) {
				return cm;
			}
		}
		return null;
	}

	public Vector<Annotation> getAllAnnotations() {
		return this.allAnnotations;
	}

	public Vector<AnnotationCollection> getAllAnnotationCollections() {
		return this.allAnnotationCollections;
	}

	public Hashtable<EVAnnotation, Annotation> getOldToNewAnnotationHash() {
		return oldToNewAnnotationHash;
	}

	public boolean isSingleAnnotationSet() {
		return isSingleAnnotationSet;
	}

	// public Hashtable<String, Document> getNamedDocumentHash() {
	// return namedDocumentHash;
	// }
	//
	// public Hashtable<String, String> getNamedDocumentTextHash() {
	// return namedDocumentTextHash;
	// }

	// 11/5/2015
	public String getNamedDocumentText(String dname) {
		String key = StrUtils.removeNonAlphaDigitCharacters(dname);
		return this.namedDocumentTextHash.get(key);
	}

	public Document getNamedDocument(String dname) {
		String key = StrUtils.removeNonAlphaDigitCharacters(dname);
		return this.namedDocumentHash.get(key);
	}

	public void putNamedDocumentTextHash(String dname, String text) {
		String key = StrUtils.removeNonAlphaDigitCharacters(dname);
		this.namedDocumentTextHash.put(key, text);
	}

	public void putNamedDocumentHash(String dname, Document doc) {
		String key = StrUtils.removeNonAlphaDigitCharacters(dname);
		this.namedDocumentHash.put(key, doc);
	}

	// Only return "primary" if file is in primary directory and contains
	// primary
	// annotator name.
	public String getNormalizedAnnotatorName(String aname) {
		if (this.knowtatorIO.getAnalysis()
				.readAnnotationCollectionFileIsPrimary()
				&& this.getPrimaryAnnotator().containsName(aname)) {
			return "primary";
		} else if (!this.knowtatorIO.getAnalysis()
				.readAnnotationCollectionFileIsPrimary()
				&& this.getSecondaryAnnotator().containsName(aname)) {
			return "secondary";
		}
		return null;
	}

	public boolean isAnnotationFPtoTPMatch(Annotation annotation) {
		String key = (annotation.isPrimary() ? "FN" : "FP");
		if (annotation != null && annotation.getClassificationValue() != null) {
			String text = annotation.getText().toLowerCase();
			String cstr = annotation.getClassificationValue().toString();
			String value = key + ":" + cstr + "==" + text;
			return this.FPToTPTextHash.get(value) != null;
		}
		return false;
	}

	public void recordAnnotationFPtoTPText() {
		Annotation a = this.getSelectedAnnotation();
		String key = (a.isPrimary() ? "FN" : "FP");
		if (a != null && a.getClassificationValue() != null) {
			String text = a.getText().toLowerCase();
			String cstr = a.getClassificationValue().toString();
			String value = key + ":" + cstr + "==" + text;
			this.FPToTPTextHash.put(value, value);
			this.storeAnnotationFPtoTPTextsToFile();
		}
	}

	public void readFPToTPAnnotationTextsFromFile() {
		String rdir = this.knownledgeEngine.getStartupParameters()
				.getResourceDirectory();
		String tfile = rdir + File.separatorChar + "FPToTPTexts";
		String tstr = FUtils.readFile(tfile);
		if (tstr != null) {
			String[] values = tstr.split("&&");
			for (String value : values) {
				if (value != null && value.length() > 2
//						&& !value.toLowerCase().contains("margin")
						) {
					this.FPToTPTextHash.put(value, value);
				}
			}
		}
	}

	public void storeAnnotationFPtoTPTextsToFile() {
		if (this.FPToTPTextHash != null && !this.FPToTPTextHash.isEmpty()) {
			String rdir = this.knownledgeEngine.getStartupParameters()
					.getResourceDirectory();
			String idfile = rdir + File.separatorChar + "FPToTPTexts";
			FUtils.deleteFileIfExists(idfile);
			StringBuffer sb = new StringBuffer();
			for (String text : (Vector<String>) HUtils
					.getKeys(this.FPToTPTextHash)) {
				if (text != null && text.length() > 2) {
					text = text.toLowerCase();
					
					text = text.replace("fp:", "FP:");
					text = text.replace("fn:", "FN:");
					
//					if (!(text.contains("FP:") || text.contains("FN:"))) {
//						text = "FP:" + text;
//					}
					
					sb.append(text);
					sb.append("&&");
				}
			}
			FUtils.writeFile(idfile, sb.toString());
		}
	}

	public void clearAnnotationFPToTPTextHash() {
		this.FPToTPTextHash = new Hashtable();
	}

	public boolean readAnnotationCollectionFileIsPrimary() {
		return readAnnotationCollectionFileIsPrimary;
	}

}
