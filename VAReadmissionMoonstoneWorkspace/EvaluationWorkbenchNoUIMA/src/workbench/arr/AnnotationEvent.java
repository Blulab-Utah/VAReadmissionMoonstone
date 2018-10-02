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

import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Document;

import annotation.AnnotationCollection;
import annotation.EVAnnotation;

public class AnnotationEvent {

	/** The analysis. */
	AnnotationAnalysis analysis = null;

	/** The document. */
	Document document = null;

	/** The primary annotation collection. */
	AnnotationCollection primaryAnnotationCollection = null;

	/** The secondary annotation collection. */
	AnnotationCollection secondaryAnnotationCollection = null;

	/** The selected annotation collection. */
	AnnotationCollection selectedAnnotationCollection = null;

	/** The attributes. */
	Vector<String> attributes = null;

	/** The relations. */
	Vector<String> relations = null;

	/** The id annotation map. */
	Hashtable<String, EVAnnotation> idAnnotationMap = new Hashtable();

	/**
	 * Instantiates a new annotation event.
	 * 
	 * @param analysis
	 *            the analysis
	 * @param document
	 *            the document
	 */
	public AnnotationEvent(AnnotationAnalysis analysis, Document document) {
		this.setAnalysis(analysis);
		this.setDocument(document);
		resolveAnnotationCollections();
	}

	public void resolveAnnotationCollections() {
		Vector<AnnotationCollection> acs = this.analysis.documentAnnotationCollectionMap
				.get(document);
		for (AnnotationCollection ac : acs) {
			if (ac.getAnnotator() != null) {
				if (ac.getAnnotator().isPrimary()) {
					this.primaryAnnotationCollection = ac;
					this.primaryAnnotationCollection.setPrimary(true);
				} else {
					this.secondaryAnnotationCollection = ac;
					this.secondaryAnnotationCollection.setPrimary(false);
				}
			}
		}
		selectAnnotationCollection(true);
		resolveReferences();
	}

	/**
	 * Store annotation by id.
	 * 
	 * @param annotation
	 *            the annotation
	 */
	public void storeAnnotationByID(EVAnnotation annotation) {
		String key = annotation.getId() + ":"
				+ annotation.getAnnotationCollection().isPrimary();
		this.idAnnotationMap.put(key, annotation);
	}

	/**
	 * Gets the annotation by id.
	 * 
	 * @param id
	 *            the id
	 * @param isPrimary
	 *            the is primary
	 * @return the annotation by id
	 */
	public EVAnnotation getAnnotationByID(String id, boolean isPrimary) {
		String key = id + ":" + isPrimary;
		return this.idAnnotationMap.get(key);
	}

	/**
	 * Resolve references.
	 */
	public void resolveReferences() {
		if (this.primaryAnnotationCollection != null
				&& this.primaryAnnotationCollection.getAnnotations() != null
				&& this.secondaryAnnotationCollection != null
				&& this.secondaryAnnotationCollection.getAnnotations() != null) {
			for (EVAnnotation primary : this.primaryAnnotationCollection
					.getAnnotations()) {
				if (primary.getMatchedAnnotationID() != null
						&& primary.getMatchedAnnotation() == null) {
					primary.setMatchedAnnotation(this.getAnnotationByID(
							primary.getMatchedAnnotationID(), false));
				}
			}
			for (EVAnnotation secondary : this.secondaryAnnotationCollection
					.getAnnotations()) {
				if (secondary.getMatchedAnnotationID() != null
						&& secondary.getMatchedAnnotation() == null) {
					secondary.setMatchedAnnotation(this.getAnnotationByID(
							secondary.getMatchedAnnotationID(), true));
				}
			}
		}
	}

	/**
	 * Gets the analysis.
	 * 
	 * @return the analysis
	 */
	public AnnotationAnalysis getAnalysis() {
		return analysis;
	}

	/**
	 * Sets the analysis.
	 * 
	 * @param analysis
	 *            the new analysis
	 */
	public void setAnalysis(AnnotationAnalysis analysis) {
		this.analysis = analysis;
	}

	/**
	 * Gets the document.
	 * 
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the document.
	 * 
	 * @param document
	 *            the new document
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * Gets the primary annotation collection.
	 * 
	 * @return the primary annotation collection
	 */
	public AnnotationCollection getPrimaryAnnotationCollection() {
		return primaryAnnotationCollection;
	}

	/**
	 * Gets the secondary annotation collection.
	 * 
	 * @return the secondary annotation collection
	 */
	public AnnotationCollection getSecondaryAnnotationCollection() {
		return secondaryAnnotationCollection;
	}

	/**
	 * Gets the selected annotation collection.
	 * 
	 * @return the selected annotation collection
	 */
	public AnnotationCollection getSelectedAnnotationCollection() {
		return selectedAnnotationCollection;
	}
	
	public AnnotationCollection getNonselectedAnnotationCollection() {
		if (selectedAnnotationCollection.equals(primaryAnnotationCollection)) {
			return this.secondaryAnnotationCollection;
		}
		return this.primaryAnnotationCollection;
	}

	public void selectAnnotationCollection(int col) {
		selectAnnotationCollection(col <= 1);
	}

	/**
	 * Select annotation collection.
	 * 
	 * @param isPrimary
	 *            the is primary
	 */
	public void selectAnnotationCollection(boolean isPrimary) {
		if (isPrimary) {
			this.selectedAnnotationCollection = this
					.getPrimaryAnnotationCollection();
		} else {
			this.selectedAnnotationCollection = this
					.getSecondaryAnnotationCollection();
		}
	}

	public void toggleAnnotationCollection() {
		boolean isPrimary = this.getSelectedAnnotationCollection().isPrimary();
		this.selectAnnotationCollection(!isPrimary);
	}

	/**
	 * Gets the selected annotator.
	 * 
	 * @return the selected annotator
	 */
	public Annotator getSelectedAnnotator() {
		if (this.selectedAnnotationCollection != null) {
			return this.selectedAnnotationCollection.getAnnotator();
		}
		return null;
	}

}
