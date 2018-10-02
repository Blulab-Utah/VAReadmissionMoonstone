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

import java.util.HashMap;
import java.util.Map;
import annotation.AnnotationCollection;
import annotation.EVAnnotation;

// TODO: Auto-generated Javadoc
/**
 * The Class Annotator.
 */
public class Annotator {
	
	/** The analysis. */
	AnnotationAnalysis analysis = null;
	
	/** The name. */
	String name = null;
	
	/** The is primary. */
	boolean isPrimary = false;
	
	/** The annotation collection. */
	AnnotationCollection annotationCollection = null;
	
	/** The document annotation. */
	EVAnnotation documentAnnotation = null;
	
	/** The component annotation. */
	EVAnnotation componentAnnotation = null;
	
	/** The annotator map. */
	static Map<String, Annotator> annotatorMap = new HashMap<String, Annotator>();
	
	/**
	 * Instantiates a new annotator.
	 *
	 * @param analysis the analysis
	 * @param name the name
	 */
	public Annotator(AnnotationAnalysis analysis, String name) {
		this.analysis = analysis;
		this.name = name;
		annotatorMap.put(name, this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[" + this.name + "]";
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks if is primary.
	 *
	 * @return true, if is primary
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * Sets the primary.
	 *
	 * @param isPrimary the new primary
	 */
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	/**
	 * Gets the annotation collection.
	 *
	 * @return the annotation collection
	 */
	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	/**
	 * Sets the annotation collection.
	 *
	 * @param annotationCollection the new annotation collection
	 */
	public void setAnnotationCollection(AnnotationCollection annotationCollection) {
		this.annotationCollection = annotationCollection;
	}

	/**
	 * Gets the document annotation.
	 *
	 * @return the document annotation
	 */
	public EVAnnotation getDocumentAnnotation() {
		return documentAnnotation;
	}

	/**
	 * Sets the document annotation.
	 *
	 * @param documentAnnotation the new document annotation
	 */
	public void setDocumentAnnotation(EVAnnotation documentAnnotation) {
		this.documentAnnotation = documentAnnotation;
	}

	/**
	 * Gets the component annotation.
	 *
	 * @return the component annotation
	 */
	public EVAnnotation getComponentAnnotation() {
		return componentAnnotation;
	}

	/**
	 * Sets the component annotation.
	 *
	 * @param componentAnnotation the new component annotation
	 */
	public void setComponentAnnotation(EVAnnotation componentAnnotation) {
		this.componentAnnotation = componentAnnotation;
	}
	
	
	
}
