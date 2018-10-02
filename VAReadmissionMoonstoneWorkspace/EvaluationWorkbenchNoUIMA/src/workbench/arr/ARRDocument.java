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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import tsl.utilities.HUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class ARRDocument.
 */
public class ARRDocument {
	
	/** The analysis. */
	public AnnotationAnalysis analysis = null;
	
	/** The filename. */
	public String filename = null;
	
	/** The text. */
	public String text = null;
	
	/** The document map. */
	public static Map<String, ARRDocument> documentMap = new HashMap<String, ARRDocument>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[" + this.filename + "]";
	}
	
	/**
	 * Gets the document.
	 *
	 * @param filename the filename
	 * @return the document
	 */
	public static ARRDocument getDocument(String filename) {
		return documentMap.get(filename);
	}
	
	/**
	 * Gets the documents.
	 *
	 * @return the documents
	 */
	public static Vector getDocuments() {
		Vector v = HUtils.getElements(documentMap);
		Collections.sort(v, new NameSorter());
		return v;
	}
	
	/**
	 * Gets the document names.
	 *
	 * @return the document names
	 */
	public static Vector getDocumentNames() {
		Vector v = HUtils.getKeys(documentMap);
		Collections.sort(v);
		return v;
	}
	
	/**
	 * The Class NameSorter.
	 */
	public static class NameSorter implements Comparator {
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			ARRDocument d1 = (ARRDocument) o1;
			ARRDocument d2 = (ARRDocument) o2;
			return d1.filename.compareTo(d2.filename);
		}
	}

}
