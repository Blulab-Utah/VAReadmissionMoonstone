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
package workbench.arr;

import io.knowtator.KnowtatorIO;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;
import tsl.utilities.FUtils;
import tsl.utilities.VUtils;
import typesystem.Annotation;
import typesystem.TypeObject;
import annotation.AnnotationCollection;
import annotation.Classification;
import annotation.DocumentAnnotation;
import annotation.EVAnnotation;
import annotation.SnippetAnnotation;

/**
 * The Class AnnotationAnalysis.
 */
public class AnnotationAnalysis {

	// String directoryFirstAnnotator = null;
	//
	// String directorySecondAnnotator = null;

	/** The primary annotator. */
	Annotator primaryAnnotator = null;

	/** The secondary annotator. */
	Annotator secondaryAnnotator = null;

	Annotator pipelineAnnotator = null;

	/** The arr tool. */
	EvaluationWorkbench arrTool = null;

	/** The selected document. */
	Document selectedDocument = null;

	/** The selected annotation event. */
	AnnotationEvent selectedAnnotationEvent = null;

	/** The classifications. */
	Vector<Classification> classifications = null;

	Hashtable<Object, Integer> classificationRowHash = new Hashtable();

	/** The selected classification. */
	Classification selectedClassification = null;

	String selectedAttributeName = null;

	String selectedAttributeValueName = null;

	Vector<String> selectedAttributeValueNames = null;

	/** The document annotation collection map. */
	public Hashtable<Document, Vector<AnnotationCollection>> documentAnnotationCollectionMap = new Hashtable();

	/** The annotation event map. */
	Hashtable<Document, AnnotationEvent> annotationEventMap = new Hashtable();

	/** The document hash. */
	public Hashtable<String, Document> documentHash = new Hashtable();

	/** The annotator hash. */
	Hashtable<String, Annotator> annotatorHash = new Hashtable();

	/** The all classification annotation map. */
	Hashtable<Classification, Vector<EVAnnotation>> allClassificationAnnotationMap = new Hashtable();

	/** The all annotation type classification map. */
	Hashtable<String, Vector<Classification>> allAnnotationTypeClassificationMap = new Hashtable();

	/** The all class attribute map. */
	Hashtable<typesystem.Annotation, Vector<String>> allClassAttributeMap = new Hashtable();

	/** The all class attribute value map. */
	Hashtable<String, Vector> allClassAttributeValueMap = new Hashtable();

	/** The all class relation map. */
	Hashtable<typesystem.Annotation, Vector<String>> allClassRelationMap = new Hashtable();

	/** The selected annotation. */
	EVAnnotation selectedAnnotation = null;

	EVAnnotation lastSelectedAnnotation = null;

	/** The primary selected annotation. */
	EVAnnotation primarySelectedAnnotation = null;

	/** The secondary selected annotation. */
	EVAnnotation secondarySelectedAnnotation = null;

	/** The selected level. */
	typesystem.Annotation selectedLevel = null;

	/** The refresh classifications. */
	boolean refreshClassifications = false;

	KnowtatorIO knowtatorIO = null;

	Hashtable<String, String> equivalentClassificationNameHash = null;

	Hashtable<String, String> secondaryClassificationNameHash = null;

	Vector<EVAnnotation> KnowtatorAnnotations = null;

	StartupParameters startupParameters = null;

	public AnnotationAnalysis(EvaluationWorkbench arrTool) {
		this.arrTool = arrTool;
		arrTool.analysis = this;
		this.startupParameters = arrTool.getStartupParameters();
	}

	public void initializeAnnotators() {
		this.primaryAnnotator = this.startupParameters.getFirstAnnotator();
		this.primaryAnnotator.setPrimary(true);
		this.secondaryAnnotator = this.startupParameters.getSecondAnnotator();
		this.secondaryAnnotator.setPrimary(false);
		this.annotatorHash.put(this.getPrimaryAnnotator().getName(),
				this.getPrimaryAnnotator());
		this.annotatorHash.put(this.getSecondaryAnnotator().getName(),
				this.getSecondaryAnnotator());
	}

	public void addDocumentAnnotationCollectionMap(AnnotationCollection ac,
			Document document) {
		ac.setDocument(document);
		if (this.getPipelineAnnotator() != null) {
			ac.setAnnotator(this.getPipelineAnnotator());
			documentHash.put(document.getName(), document);
			VUtils.pushHashVector(this.documentAnnotationCollectionMap,
					ac.getDocument(), ac);
		}
	}

	// 1/15/2013
	public void addDocumentAnnotationCollectionMap(AnnotationCollection ac) {
		if (ac != null) {
			Document document = ac.getDocument();
			documentHash.put(document.getName(), document);
			VUtils.pushIfNotHashVector(this.documentAnnotationCollectionMap,
					document, ac);
		}
	}

	public void readAnnotationCollections() throws Exception {
		if (startupParameters.isInputTypeFirstAnnotatorGrAF()) {
			readAnnotationCollectionsGrAF(this.primaryAnnotator);
		} else if (startupParameters.isInputTypeFirstAnnotatorKnowtator()) {
			readAnnotationCollectionsKnowtator(this.primaryAnnotator);
		}
		if (startupParameters.isInputTypeSecondAnnotatorGrAF()) {
			readAnnotationCollectionsGrAF(this.secondaryAnnotator);
		} else if (startupParameters.isInputTypeSecondAnnotatorKnowtator()) {
			readAnnotationCollectionsKnowtator(this.secondaryAnnotator);
		}
	}

	public void readAnnotationCollectionsGrAF(Annotator annotator)
			throws Exception {
		String dstr = null;
		if (annotator.isPrimary()) {
			dstr = startupParameters
					.getAnnotationInputDirectoryFirstAnnotator();
		} else {
			dstr = startupParameters
					.getAnnotationInputDirectorySecondAnnotator();
		}
		readGrAFAnnotationFiles(dstr, annotator);

	}

	public void readAnnotationCollectionsKnowtator(Annotator annotator)
			throws Exception {
		String format = null;
		String fname = null;
		if (annotator.isPrimary()) {
			format = startupParameters.getKnowtatorFormatFirstAnnotator();
			fname = startupParameters.getKnowtatorPinsFileFirstAnnotator();
			if (fname == null) {
				fname = startupParameters
						.getAnnotationInputDirectoryFirstAnnotator();
			}
		} else {
			format = startupParameters.getKnowtatorFormatSecondAnnotator();
			fname = startupParameters.getKnowtatorPinsFileSecondAnnotator();
			if (fname == null) {
				fname = startupParameters
						.getAnnotationInputDirectorySecondAnnotator();
			}
		}
		this.knowtatorIO = new KnowtatorIO(arrTool, arrTool.getTypeSystem(),
				format, annotator);
		this.knowtatorIO.extractSchema(startupParameters
				.getKnowtatorSchemaFile());
		this.knowtatorIO.extractAnnotationsFromDirectory(fname);
		this.knowtatorIO.createWorkbenchAnnotations(this);
		attachDocumentsToKnowtatorAnnotationCollections(annotator);
		this.knowtatorIO = null;
	}

	public void postProcessAnnotationCollections() throws Exception {
		Vector<Document> toRemove = new Vector(0);
		for (Enumeration<Document> e = this.documentAnnotationCollectionMap
				.keys(); e.hasMoreElements();) {
			Document document = e.nextElement();
			Vector<AnnotationCollection> v = this.documentAnnotationCollectionMap
					.get(document);
			if (v.size() == 2) {
				AnnotationEvent annotationEvent = new AnnotationEvent(this,
						document);
				AnnotationCollection primary = v.elementAt(0);
				AnnotationCollection secondary = v.elementAt(1);
				if (this.selectedAnnotationEvent == null) {
					this.selectedAnnotationEvent = annotationEvent;
				}
				this.annotationEventMap.put(document, annotationEvent);
				primary.wrapup();
				secondary.wrapup();
			} else {
				toRemove.add(document);
			}
		}
		if (!toRemove.isEmpty()) {
			for (Document document : toRemove) {
				this.documentAnnotationCollectionMap.remove(document);
				this.documentHash.remove(document.getName());
			}
		}
		if (startupParameters.permitEquivalentClassificationNames()) {
			populateEquivalentClassificationHash();
		}

		typesystem.Annotation type = typesystem.Annotation
				.getAnnotationByClass(SnippetAnnotation.class);
		this.setSelectedLevel(type, false);
		// if (startupParameters.isUseTSL()) {
		// this.tslInterface = new TSLInterface(this);
		// }

		// 9/5/2013: From VA
		Validation.readValidations(this);

	}

	// 2/14/2013: THIS PRODUCES ERRORS ON THE CLEF DATA SETS!!
	public void populateEquivalentClassificationHash() throws Exception {
		Hashtable<String, String> ehash = new Hashtable();
		Hashtable<String, String> shash = new Hashtable();
		Hashtable<String, Vector<EVAnnotation>> pchash = new Hashtable();
		Hashtable<String, Vector<EVAnnotation>> schash = new Hashtable();
		for (Enumeration<Document> e = this.annotationEventMap.keys(); e
				.hasMoreElements();) {
			Document document = e.nextElement();
			AnnotationEvent ae = this.annotationEventMap.get(document);
			if (ae.getPrimaryAnnotationCollection().getAnnotations() != null) {
				for (EVAnnotation primary : ae.getPrimaryAnnotationCollection()
						.getAnnotations()) {
					Classification pc = primary.getClassification();
					pc.setPrimary(true);
					if (!pc.isEmpty()
							&& pc.getEquivalentClassification() == null) {
						String pcvalue = pc.getValue();
						VUtils.pushHashVector(pchash, pcvalue, primary);
					}
				}
			}
			if (ae.getSecondaryAnnotationCollection().getAnnotations() != null) {
				for (EVAnnotation secondary : ae
						.getSecondaryAnnotationCollection().getAnnotations()) {
					Classification sc = secondary.getClassification();
					sc.setPrimary(false);
					if (!sc.isEmpty()
							&& sc.getEquivalentClassification() == null) {
						String scvalue = sc.getValue();
						VUtils.pushHashVector(schash, scvalue, secondary);
					}
				}
			}
		}
		this.equivalentClassificationNameHash = ehash;
		this.secondaryClassificationNameHash = shash;
		Hashtable<Classification, Classification> echash = new Hashtable();
		for (Enumeration<String> se = schash.keys(); se.hasMoreElements();) {
			String scstr = se.nextElement();
			Vector<EVAnnotation> sv = schash.get(scstr);
			for (Enumeration<String> pe = pchash.keys(); pe.hasMoreElements();) {
				boolean found = false;
				String pcstr = pe.nextElement();
				Vector<EVAnnotation> pv = pchash.get(pcstr);
				if (pv.firstElement().getClassification()
						.getEquivalentClassification() == null) {
					for (Enumeration<EVAnnotation> sae = sv.elements(); !found
							&& sae.hasMoreElements();) {
						EVAnnotation secondary = sae.nextElement();
						Classification sc = secondary.getClassification();
						for (Enumeration<EVAnnotation> pae = pv.elements(); !found
								&& pae.hasMoreElements();) {
							EVAnnotation primary = pae.nextElement();
							Classification pc = primary.getClassification();
							if (found
									|| secondary.getStart() == primary
											.getStart()
									|| pc.stringsContainSameNumber(sc)) {
								found = true;
								echash.put(pc, sc);
								pc.setEquivalentClassification(sc);
								sc.setEquivalentClassification(pc);
								secondary.removeAnalysisIndices();
								ehash.put(scstr, pcstr);
								shash.put(pcstr, scstr);
								secondary.storeAnalysisIndices(secondary
										.getAnnotationCollection());
							}
						}
					}
				}
			}
		}

		this.equivalentClassificationNameHash = ehash;
		this.secondaryClassificationNameHash = shash;
	}

	public String getEquivalentClassificationName(String name) {
		if (name != null && this.equivalentClassificationNameHash != null) {
			String estr = this.equivalentClassificationNameHash.get(name);
			return estr;
		}
		return null;
	}

	public String getSecondaryClassificationName(String name) {
		if (name != null && this.secondaryClassificationNameHash != null) {
			return this.secondaryClassificationNameHash.get(name);
		}
		return null;
	}

	public void attachDocumentsToKnowtatorAnnotationCollections(
			Annotator annotator) throws Exception {
		if (this.knowtatorIO.getAnnotationCollections() != null) {
			for (AnnotationCollection ac : this.knowtatorIO
					.getAnnotationCollections()) {
				if (ac.getAnnotatorID().equals(annotator.getName())
						|| ac.getAnnotatorName().equals(annotator.getName())) {
					ac.setAnnotator(annotator);
					Document doc = this.getDocument(ac.getSourceTextName(),
							annotator);
					ac.setDocument(doc);
					VUtils.pushHashVector(this.documentAnnotationCollectionMap,
							ac.getDocument(), ac);
				}
			}
		}
	}

	public int readGrAFAnnotationFiles(String dname, Annotator annotator)
			throws Exception {
		int count = 0;
		File dir = new File(dname);
		if (!dir.exists()) {
//			JOptionPane.showMessageDialog(new JFrame(),
//					"Unable to locate directory " + dname);
			String msg = "Unable to locate directory " + dname;
			throw new Exception(msg);
		}
		File[] cfiles = dir.listFiles();
		for (int i = 0; i < cfiles.length; i++) {
			File file = cfiles[i];
			if (file.isFile() && file.getName().indexOf(".xml") > 0) {
				String filename = dname + File.separator + file.getName();
				AnnotationCollection ac = new AnnotationCollection(this,
						filename, null, annotator);
				VUtils.pushHashVector(this.documentAnnotationCollectionMap,
						ac.getDocument(), ac);
			}
		}
		return count;
	}

	public void writeGrAFAnnotations() throws Exception {
		for (Enumeration<Document> e = this.documentAnnotationCollectionMap
				.keys(); e.hasMoreElements();) {
			Document document = e.nextElement();
			Vector<AnnotationCollection> collections = this.documentAnnotationCollectionMap
					.get(document);
//			for (AnnotationCollection ac : collections) {
//				ac.writeGrAFAnnotations();
//			}
		}
	}

	public void writeCSVFile() {
		StringBuffer sb = new StringBuffer();
		for (Enumeration<Document> e = this.documentAnnotationCollectionMap
				.keys(); e.hasMoreElements();) {
			Document document = e.nextElement();
			for (AnnotationCollection ac : this.documentAnnotationCollectionMap
					.get(document)) {
				for (EVAnnotation annotation : ac.getAnnotations()) {
					if (annotation.getClassification() != null
							&& !"*".equals(annotation.getClassification()
									.getName()) && annotation.isVerifiedTrue()) {

						sb.append(document.getName());
						sb.append('\t');
						sb.append(ac.getAnnotator().getName());
						sb.append('\t');
						sb.append(annotation.getClassification().getName());
						sb.append('=');
						sb.append(annotation.getClassification().getValue());
						sb.append('\t');
						if (annotation.getSpans() == null) {
							sb.append("*");
						} else {
							sb.append(annotation.getSpans());
						}
						sb.append("\n");
					}
				}
			}
		}
//		JFileChooser chooser = new JFileChooser();
//		int rv = chooser.showSaveDialog(new JFrame());
//		if (rv == JFileChooser.APPROVE_OPTION) {
//			File file = chooser.getSelectedFile();
//			FUtils.writeFile(file.getAbsolutePath(), sb.toString());
//		}
	}

	public void verifySelectedAnnotation() throws Exception {
		EVAnnotation selected = this.getSelectedAnnotation();
		EVAnnotation other = null;
		if (selected != null) {
			if (selected.getAnnotationCollection().isPrimary()) {
				other = this.getSecondarySelectedAnnotation();
			} else {
				other = this.getPrimarySelectedAnnotation();
			}
			selected.setVerifiedTrue(true);
			if (other != null) {
				boolean matches = EVAnnotation.isSameClassificationState(
						selected, other, selected.getClassification());
				other.setVerifiedTrue(matches);
			}
//			arrTool.documentPane.highlightSentences(true);
		}
	}

	public void falsifySelectedAnnotation() throws Exception {
		EVAnnotation selected = this.getSelectedAnnotation();
		if (selected != null) {
			selected.setVerifiedTrue(false);
//			arrTool.documentPane.highlightSentences(true);
		}
	}

	public void unverifySelectedAnnotation() throws Exception {
		EVAnnotation primary = this.getPrimarySelectedAnnotation();
		EVAnnotation secondary = this.getSecondarySelectedAnnotation();
		if (primary != null) {
			primary.setVerifiedTrue(false);
			primary.setVerified(false);
			if (secondary != null) {
				secondary.setVerifiedTrue(false);
				secondary.setVerified(false);
			}
//			arrTool.documentPane.highlightSentences(true);
		}
	}

	public void verifyAllAnnotations() throws Exception {
		AnnotationEvent ae = this.arrTool.getAnalysis()
				.getSelectedAnnotationEvent();
		for (EVAnnotation annotation : ae.getPrimaryAnnotationCollection()
				.getAnnotations()) {
			if (annotation.getClassification() != null) {
				annotation.setVerifiedTrue(true);
			}
		}
		for (EVAnnotation annotation : ae.getSecondaryAnnotationCollection()
				.getAnnotations()) {
			annotation.setVerifiedTrue(true);
		}
//		arrTool.documentPane.highlightSentences(true);
	}

	public void unverifyAllAnnotations() throws Exception {
		AnnotationEvent ae = this.arrTool.getAnalysis()
				.getSelectedAnnotationEvent();
		for (EVAnnotation annotation : ae.getPrimaryAnnotationCollection()
				.getAnnotations()) {
			annotation.setUnverified();
		}
		for (EVAnnotation annotation : ae.getSecondaryAnnotationCollection()
				.getAnnotations()) {
			annotation.setUnverified();
		}
//		arrTool.documentPane.highlightSentences(true);
	}

	// 9/5/2013: From VA
	public AnnotationCollection getAnnotationCollection(Document doc,
			Annotator annotator) {
		if (doc != null && annotator != null) {
			AnnotationEvent ae = this.getAnnotationEvent(doc);
			if (annotator.isPrimary()) {
				return ae.getPrimaryAnnotationCollection();
			}
			return ae.getSecondaryAnnotationCollection();
		}
		return null;
	}

	public Annotator getAnnotator(String aname) {
		if (aname.equals(this.getPrimaryAnnotator().getName())) {
			return this.getPrimaryAnnotator();
		}
		return this.getSecondaryAnnotator();
	}

	public Document getDocument(String filename, Annotator annotator)
			throws Exception {
		Document document = documentHash.get(filename);
		if (document == null) {
			String dname = startupParameters.getTextInputDirectory();
			String fullname = dname + File.separator + filename;
			String text = FUtils.readFile(fullname);
			if (text != null) {
				document = new Document();
				document.setName(filename);
				document.setAbsoluteFilePath(fullname);
				document.setFullName(fullname);
				document.setText(text);
				// 3/27/2013 -- Don't need all the extra data
				// document.analyzeSentences();
				documentHash.put(filename, document);
			}
		}
		return document;
	}

	public Vector<Document> getAllDocuments() {
		if (!documentHash.values().isEmpty()) {
			return new Vector(documentHash.values());
		}
		return null;
	}

	public AnnotationEvent getAnnotationEvent(Document document) {
		return this.annotationEventMap.get(document);
	}

	public Vector<AnnotationEvent> getAnnotationEvents() {
		Vector<AnnotationEvent> events = null;
		for (Enumeration<Document> e = this.annotationEventMap.keys(); e
				.hasMoreElements();) {
			Document doc = e.nextElement();
			AnnotationEvent ae = this.annotationEventMap.get(doc);
			events = VUtils.add(events, ae);
		}
		return events;
	}

	public Vector<AnnotationCollection> getAllAnnotationCollections() {
		Vector<AnnotationEvent> events = getAnnotationEvents();
		Vector<AnnotationCollection> acs = null;
		if (events != null) {
			for (AnnotationEvent ae : events) {
				acs = VUtils.add(acs, ae.getPrimaryAnnotationCollection());
				acs = VUtils.add(acs, ae.getSecondaryAnnotationCollection());
			}
		}
		return acs;
	}

	public Vector<EVAnnotation> getAllAnnotations() {
		Vector<AnnotationCollection> acs = getAllAnnotationCollections();
		Vector<EVAnnotation> annotations = null;
		if (acs != null) {
			for (AnnotationCollection ac : acs) {
				annotations = VUtils.append(annotations, ac.getAnnotations());
			}
		}
		return annotations;
	}

	public void selectAnnotationCollection(AnnotationCollection ac)
			throws Exception {
		AnnotationEvent e = this.getSelectedAnnotationEvent();
		if (e != null) {
			e.selectAnnotationCollection(ac.isPrimary());
			setSelectedAnnotation();
			arrTool.setTitle();
		}
	}

	public void selectAnnotationCollection(int col) throws Exception {
		if (this.getSelectedAnnotationEvent() != null) {
			this.getSelectedAnnotationEvent().selectAnnotationCollection(
					col < 2 ? true : false);
			setSelectedAnnotation();
			arrTool.setTitle();
		}
	}

	public Annotator getSelectedAnnotator() {
		if (this.getSelectedAnnotationEvent() != null) {
			return this.getSelectedAnnotationEvent().getSelectedAnnotator();
		}
		return null;
	}

	public String getDirectory(boolean isPrimary) {
		if (startupParameters.getRewriteDirectory() != null) {
			String dir = startupParameters.getRewriteDirectory()
					+ File.separatorChar
					+ this.getSelectedAnnotator().getName();
			return dir;
		}
		if (isPrimary) {
			return this.startupParameters
					.getAnnotationInputDirectoryFirstAnnotator();
		} else {
			return this.startupParameters
					.getAnnotationInputDirectorySecondAnnotator();
		}
	}

	/**
	 * Gets the primary annotator.
	 * 
	 * @return the primary annotator
	 */
	public Annotator getPrimaryAnnotator() {
		return primaryAnnotator;
	}

	/**
	 * Sets the primary annotator.
	 * 
	 * @param primaryAnnotator
	 *            the new primary annotator
	 */
	public void setPrimaryAnnotator(Annotator primaryAnnotator) {
		this.primaryAnnotator = primaryAnnotator;
	}

	/**
	 * Gets the secondary annotator.
	 * 
	 * @return the secondary annotator
	 */
	public Annotator getSecondaryAnnotator() {
		return secondaryAnnotator;
	}

	/**
	 * Sets the secondary annotator.
	 * 
	 * @param secondaryAnnotator
	 *            the new secondary annotator
	 */
	public void setSecondaryAnnotator(Annotator secondaryAnnotator) {
		this.secondaryAnnotator = secondaryAnnotator;
	}

	/**
	 * Gets the arr tool.
	 * 
	 * @return the arr tool
	 */
	public EvaluationWorkbench getArrTool() {
		return arrTool;
	}

	/**
	 * Sets the arr tool.
	 * 
	 * @param arrTool
	 *            the new arr tool
	 */
	public void setArrTool(EvaluationWorkbench arrTool) {
		this.arrTool = arrTool;
	}

	/**
	 * Gets the classifications.
	 * 
	 * @return the classifications
	 */
	public Vector<Classification> getClassifications() {
		return getClassifications(this.getSelectedLevel(), false);
	}

	public Vector<Classification> getClassifications(TypeObject level,
			boolean reset) {
		if (reset) {
			this.classifications = null;
		}
		if ((this.doRefreshClassifications() || this.classifications == null)
				&& level != null) {
			Vector<Classification> classifications = (Vector<Classification>) this.allAnnotationTypeClassificationMap
					.get(level);
			classifications = Classification
					.getDisplayableClassifications(classifications);
			if (classifications != null) {
				Collections.sort(classifications,
						new Classification.ClassificationSorter());
				this.classifications = classifications;
				int index = 0;
				for (Classification c : this.classifications) {
					String fullname = c.getCompositeValueString();
					this.classificationRowHash.put(c, new Integer(index));
					c.setDisplayIndex(index++);
				}
			}
		}
		return this.classifications;
	}

	// Before 3/4/2014
	// public Vector<Classification> getClassifications(TypeObject level,
	// boolean reset) {
	// if (reset) {
	// this.classifications = null;
	// }
	// if ((this.doRefreshClassifications() || this.classifications == null)
	// && level != null) {
	// Vector<Classification> classifications = (Vector<Classification>)
	// this.allAnnotationTypeClassificationMap
	// .get(level);
	// if (classifications != null) {
	// for (Classification c : classifications) {
	// }
	// Collections.sort(classifications,
	// new Classification.ClassificationSorter());
	// this.classifications = classifications;
	// int index = 0;
	// for (Classification c : this.classifications) {
	// String fullname = c.getCompositeValueString();
	// this.classificationRowHash.put(c, new Integer(index));
	// c.setDisplayIndex(index++);
	// }
	// }
	// }
	// return this.classifications;
	// }

	public Vector<Classification> getCurrentClassifications() {
		return this.classifications;
	}

	public boolean doRefreshClassifications() {
		return this.refreshClassifications;
	}

	public void setDoRefreshClassifications() {
		this.refreshClassifications = true;
	}

	public int getClassificationRow() {
		if (this.selectedClassification != null
				&& this.getClassifications() != null) {
			Vector<Classification> v = this.getClassifications();
			return v.indexOf(selectedClassification);
		}
		return -1;
	}

	public Classification getClassificationByRow(int row) {
		if (this.getClassifications() != null
				&& this.getClassifications().size() > row + 1) {
			// 3/5/2012 -- Summary row offset
			return this.getClassifications().elementAt(row + 1);
		}
		return null;
	}

	public int findClassificationRow(Classification c) {
		Integer i = null;
		String fullname = c.getCompositeValueString();
		// i = this.classificationRowHash.get(fullname);
		// Before 11/13/2013
		i = this.classificationRowHash.get(c);
		if (i != null) {
			int value = i.intValue() + 1;
			return value;

		}
		return -1;
	}

	public int findAttributeValueRow(String vname) {
		if (this.getSelectedAttributeValueNames() != null) {
			int row = this.getSelectedAttributeValueNames().indexOf(vname);
			return row + 1;
		}
		return -1;
	}

	public void setSelectedAnnotationEvent(String filename) {
		if (filename != null) {
			this.selectedDocument = this.documentHash.get(filename);
		}
		if (this.selectedDocument != null) {
			AnnotationEvent ae = this.annotationEventMap
					.get(this.selectedDocument);
			this.setSelectedAnnotationEvent(ae);
		}
	}

	public AnnotationEvent getSelectedAnnotationEvent() {
		return selectedAnnotationEvent;
	}

	public AnnotationCollection getSelectedAnnotationCollection() {
		if (this.selectedAnnotationEvent != null) {
			return this.selectedAnnotationEvent
					.getSelectedAnnotationCollection();
		}
		return null;
	}

	public void setSelectedAnnotationEvent(
			AnnotationEvent selectedAnnotationEvent) {
		this.selectedAnnotationEvent = selectedAnnotationEvent;
	}

	public EVAnnotation getSelectedAnnotation() {
		return this.selectedAnnotation;
	}

	public EVAnnotation setSelectedAnnotation(EVAnnotation annotation)
			throws Exception {
		this.selectedAnnotation = annotation;
		if (annotation != null) {
			if (!annotation.getType().equals(this.selectedLevel)) {
				setSelectedLevel((typesystem.Annotation) annotation.getType());
			}
			this.setSelectedClassification(annotation.getClassification());
			this.selectedAnnotation = annotation;
		}
		setPrimarySecondarySelectedAnnotations();
		return this.selectedAnnotation;
	}

	public void nullifySelectedAnnotation() {
		this.selectedAnnotation = null;
	}

	public EVAnnotation setSelectedAnnotation() throws Exception {
		EVAnnotation annotation = null;
		if (this.selectedClassification != null
				&& this.selectedAnnotationEvent != null
				&& this.selectedAnnotationEvent.selectedAnnotationCollection != null) {
			AnnotationCollection ac = this.selectedAnnotationEvent.selectedAnnotationCollection;
			if (this.isAnnotationTypeUserSelection()) {
				annotation = ac.findClosestAnnotation(this
						.getSelectedAnnotation());
			} else {
				annotation = ac.getFirstAVPairAnnotation();
			}
		}
		this.lastSelectedAnnotation = this.selectedAnnotation;
		this.selectedAnnotation = annotation;
		setPrimarySecondarySelectedAnnotations();
		return annotation;
	}

	// 2/21/2013 TOOK OUT!!
	void setPrimarySecondarySelectedAnnotations() throws Exception {
		boolean dothis = true;
		if (!dothis) {
			return;
		}
		if (this.selectedAnnotation != null) {
			AnnotationCollection primaryAC = this.selectedAnnotationEvent.primaryAnnotationCollection;
			AnnotationCollection secondaryAC = this.selectedAnnotationEvent.secondaryAnnotationCollection;
			boolean isPrimary = this.selectedAnnotationEvent.selectedAnnotationCollection
					.isPrimary();
			Vector<Vector> matching = AnnotationCollection
					.getMatchingAnnotations(this.selectedLevel,
							this.selectedAnnotation.getClassification(),
							primaryAC, secondaryAC);
			if (matching != null) {
				for (Vector<EVAnnotation> pair : matching) {
					EVAnnotation primary = pair.elementAt(0);
					EVAnnotation secondary = pair.elementAt(1);
					if (this.selectedAnnotation.equals(primary)
							|| this.selectedAnnotation.equals(secondary)) {
						this.primarySelectedAnnotation = primary;
						this.secondarySelectedAnnotation = secondary;
						break;
					}
				}
			} else if (isPrimary) {
				this.primarySelectedAnnotation = this.selectedAnnotation;
				this.secondarySelectedAnnotation = null;
			} else {
				this.primarySelectedAnnotation = null;
				this.secondarySelectedAnnotation = this.selectedAnnotation;
			}
		}
	}

	// 9/11/2013: Not tested...
	public void removeSelectedAnnotation() throws Exception {
		if (this.selectedAnnotation != null) {
			this.selectedAnnotation.removeAnalysisIndices();
			if (this.lastSelectedAnnotation != null) {
				this.setSelectedAnnotation(this.lastSelectedAnnotation);
			} else {
				this.setSelectedAnnotation(null);
			}
			arrTool.getGeneralStatistics(null, true);
			arrTool.fireAllTableDataChanged();
		}
	}

	public Classification getSelectedClassification() {
		return selectedClassification;
	}

	public void setSelectedClassification(Classification c) {
		this.nullifySelectedAnnotation();
		this.selectedClassification = c;
	}

	public void makeClassificationsEquivalent() throws Exception {
		if (this.lastSelectedAnnotation != null
				&& this.selectedAnnotation != null
				&& this.lastSelectedAnnotation.getClassification() != null
				&& this.selectedAnnotation.getClassification() != null
				&& this.lastSelectedAnnotation != this.selectedAnnotation) {
			String lvalue = this.lastSelectedAnnotation.getClassification()
					.getValue();
			String cvalue = this.selectedAnnotation.getClassification()
					.getValue();
			this.equivalentClassificationNameHash.put(lvalue, cvalue);
			this.secondaryClassificationNameHash.put(cvalue, lvalue);
			Classification lastClassification = this.lastSelectedAnnotation
					.getClassification();
			Vector<Classification> v = this
					.getAllAnnotationTypeClassificationMap().get(
							this.lastSelectedAnnotation.getType());
			v.remove(lastClassification);
			this.lastSelectedAnnotation
					.storeAnalysisIndices(this.lastSelectedAnnotation
							.getAnnotationCollection());
			arrTool.getGeneralStatistics(null, true);
			arrTool.fireAllTableDataChanged();
		}
	}

	public Hashtable<Document, AnnotationEvent> getAnnotationEventMap() {
		return annotationEventMap;
	}

	public Hashtable<String, Document> getDocumentHash() {
		return documentHash;
	}

	public Hashtable<String, Annotator> getAnnotatorHash() {
		return annotatorHash;
	}

	public Hashtable<Classification, Vector<EVAnnotation>> getAllClassificationAnnotationMap() {
		return allClassificationAnnotationMap;
	}

	public Vector<Classification> getTypesystemClassifications(Annotation type) {
		return this.allAnnotationTypeClassificationMap.get(type);
	}

	public void addClassAttribute(Class c, String attribute, Object value) {
		VUtils.pushIfNotHashVector(this.allClassAttributeMap, c, attribute);
		VUtils.pushIfNotHashVector(this.allClassAttributeValueMap, attribute,
				value);
	}

	public void addClassRelation(Class c, String relation) {
		VUtils.pushIfNotHashVector(this.allClassRelationMap, c, relation);
	}

	public Vector<String> getClassAttributes(Class c) {
		return this.allClassAttributeMap.get(c);
	}

	public Vector<String> getClassAttributeValues(String attribute) {
		if (attribute != null) {
			return this.allClassAttributeValueMap.get(attribute);
		}
		return null;
	}

	public Vector<String> getClassRelations(Class c) {
		return this.allClassRelationMap.get(c);
	}

	public Vector<String> getClassRelations() {
		return this.allClassRelationMap.get(this.getSelectedLevel());
	}

	public Vector<typesystem.Annotation> getAllLevels() {
		return new Vector(this.allClassAttributeMap.keySet());
	}

	public Document getSelectedDocument() {
		return selectedDocument;
	}

	public void setSelectedDocument(Document selectedDocument) {
		this.selectedDocument = selectedDocument;
	}

	public typesystem.Annotation getSelectedLevel() {
		return selectedLevel;
	}

	public boolean isDocumentLevel() {
		return selectedLevel.getJavaClass() == DocumentAnnotation.class;
	}

	public boolean isSnippetLevel() {
		return selectedLevel.getJavaClass() == SnippetAnnotation.class;
	}

	public void setSelectedLevel(typesystem.Annotation selectedLevel)
			throws Exception {
		setSelectedLevel(selectedLevel, true);
	}

	public void setSelectedLevel(typesystem.Annotation selectedLevel,
			boolean reinitialize) throws Exception {
		if (selectedLevel != null) {
			// 2/1/2014
			this.arrTool.clearGeneralStatisticsTable();

			getClassifications(selectedLevel, true);

			if (reinitialize) {
				this.selectedLevel = selectedLevel;
				// 2/1/2014: Temporarily taking this out. This line ensures that
				// the last
				// invocation of getGeneralStatistics() assumes that the
				// selected item is an
				// Annotation, causing Type-level display to be incorrect.
				// arrTool.getGeneralStatistics(null, false);
//				arrTool.accuracyPane.populate();
//				this.arrTool.graphPane.setSelectedLevel(selectedLevel);
//				this.arrTool.fireAllTableDataChanged();
			}

			this.selectedLevel = selectedLevel;
		}
	}

	public Hashtable<String, Vector<Classification>> getAllAnnotationTypeClassificationMap() {
		return allAnnotationTypeClassificationMap;
	}

	public EVAnnotation getPrimarySelectedAnnotation() {
		return primarySelectedAnnotation;
	}

	public EVAnnotation getSecondarySelectedAnnotation() {
		return secondarySelectedAnnotation;
	}

	public void toggleAnnotators() throws Exception {
		if (this.getSelectedAnnotationEvent() != null) {
			this.getSelectedAnnotationEvent().toggleAnnotationCollection();
//			getArrTool().fireAllTableDataChanged(true);
//			getArrTool().attributesPane.toggleColumn();
		}
	}

	public KnowtatorIO getKnowtatorIO() {
		return knowtatorIO;
	}

	public EvaluationWorkbench getWorkbench() {
		return this.arrTool;
	}

	public void setSelectedAttributeName(String aname) {
		this.selectedAttributeName = aname;
	}

	public void setSelectedAttributeValueName(String aname, String vname) {
		this.selectedAttributeName = aname;
		this.selectedAttributeValueName = vname;
	}

	public String getSelectedAttributeValueName() {
		return this.selectedAttributeValueName;
	}

	public Vector<String> getSelectedAttributeValueNames() {
		return this.selectedAttributeValueNames;
	}

	public String getSelectedAttributeName() {
		return this.selectedAttributeName;
	}

	public void setSelectedAttributeValueNames(Vector<String> names) {
		this.selectedAttributeValueNames = names;
	}

	public String getSelectedAVPair() {
		String str = null;
		if (this.selectedAttributeName != null
				&& this.selectedAttributeValueName != null) {
			str = this.selectedAttributeName + ":"
					+ this.selectedAttributeValueName;
		}
		return str;
	}

	public boolean isAnnotationTypeUserSelection() {
		return this.arrTool.statistics != null
				&& this.arrTool.statistics.selectedItemIsAnnotation();
	}

	public boolean isAttributeUserSelection() {
		return this.arrTool.statistics != null
				&& this.arrTool.statistics.selectedItemIsAttributeOrAVPair();
	}

	public Annotator getPipelineAnnotator() {
		if (this.pipelineAnnotator == null) {
			if (startupParameters.isInputTypeFirstAnnotatorPIPELINE()) {
				this.pipelineAnnotator = this.getPrimaryAnnotator();
			} else if (startupParameters.isInputTypeSecondAnnotatorPIPELINE()) {
				this.pipelineAnnotator = this.getSecondaryAnnotator();
			}
		}
		return this.pipelineAnnotator;
	}

	public Vector<EVAnnotation> getKnowtatorAnnotations() {
		return KnowtatorAnnotations;
	}

	public void appendKnowtatorAnnotations(Vector<EVAnnotation> annotations) {
		this.KnowtatorAnnotations = VUtils.append(this.KnowtatorAnnotations,
				annotations);
	}

	public void releaseKnowtatorAnnotations() {
		if (this.KnowtatorAnnotations != null) {
			for (EVAnnotation annotation : this.KnowtatorAnnotations) {
				annotation.setKtAnnotation(null);
			}
		}
	}

	public Hashtable<Document, Vector<AnnotationCollection>> getDocumentAnnotationCollectionMap() {
		return documentAnnotationCollectionMap;
	}

	public void printAllDuplicateAnnotations(Annotator annotator)
			throws Exception {
		int linenum = 0;
		StringBuffer sb = new StringBuffer();
		for (AnnotationCollection ac : this.getAllAnnotationCollections()) {
			if (annotator.equals(ac.getAnnotator())
					&& ac.getAnnotations() != null) {
				Vector<EVAnnotation> duplicates = ac.getDuplicateAnnotations();
				if (duplicates != null) {
					for (EVAnnotation duplicate : duplicates) {
						String sstr = duplicate.getStart() + ":"
								+ duplicate.getEnd();
						String str = ++linenum + ": Report= "
								+ ac.getDocument().getName() + ", Span=" + sstr
								+ "\n";
						sb.append(str);
					}
				}
			}
		}
//		InformationDisplay.displayContent(sb.toString());
	}

}
