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
package annotation;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import tsl.information.TSLInformation;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;
import typesystem.Annotation;
import typesystem.TypeObject;
import typesystem.TypeSystem;

/**
 * An element of class annotation.Classification is created for each annotation,
 * which has a classification, set of attributes and text span.
 */
public class Classification extends TSLInformation {
	String id = null;
	int numericID = 0;
	int displayIndex = 0;
	String annotationType = null;
	EVAnnotation annotation = null;
	typesystem.Annotation parentAnnotationType = null;
	typesystem.Classification parentClassification = null;
	AnnotationCollection annotationCollection = null;
	boolean usedInAnnotation = false;
	boolean testedNumberStrings = false;
	Vector<String> numberStrings = null;
	boolean isPrimary = false;
	Classification equivalentClassification = null;
	static int classificationCounter = 0;
	static String NonVisibleName = "*";

	public Classification() {
		super();
	}

	public Classification(AnnotationCollection ac, EVAnnotation annotation,
			String id, int numericID, String annotationType, String name,
			String value) throws Exception {
		this.annotationCollection = ac;
		this.annotation = annotation;
		this.id = id;
		TypeSystem ts = ac.getAnalysis().getArrTool().getTypeSystem();
		TypeObject to = ts.getTypeObject(annotationType);
		if (to instanceof typesystem.Classification) {
			to = to.getParentTypeObject();
		}
		this.parentAnnotationType = (typesystem.Annotation) to;
		if (this.parentAnnotationType != null) {
			this.parentClassification = this.parentAnnotationType
					.getFirstClassification();
		}
		this.numericID = numericID;
		this.setProperty(name, value);
	}

	/**
	 * Instantiates a new classification.
	 */
	public Classification(AnnotationCollection ac, EVAnnotation annotation,
			typesystem.Classification classification, String name,
			String value, String annotationType) {
		this.annotationCollection = ac;
		this.annotation = annotation;
		this.numericID = classificationCounter++;
		this.id = "Classification_" + this.numericID;
		this.parentClassification = classification;
		if (annotationType == null) {
			this.annotationType = classification.getParentTypeObject()
					.getName();
		} else {
			this.annotationType = annotationType;
		}

		// 9/21/2012: Not sure if this is correct, or why I have these two
		// redundant fields:
		if (this.parentAnnotationType == null && this.annotationType != null) {
			this.parentAnnotationType = (Annotation) classification
					.getParentTypeObject();
		}

		if (name != null && value != null) {
			this.setProperty(name, value);
		}
	}

	// 11/6/2013: Two classifications are the same if they have same parent
	// classification,
	// and have intersecting properties.
	public boolean equals(Object o) {
		try {
			if (o instanceof Classification) {
				Classification c = (Classification) o;
				if (this.isEmpty() || c.isEmpty()) {
					return false;
				}
				if (this.getParentClassification().equals(
						c.getParentClassification())
						&& this.propertiesIntersect(c)) {
					return true;
				}
				Object v1 = this.getValue();
				Object v2 = c.getValue();
				if (NonVisibleName.equals(v1) || NonVisibleName.equals(v2)) {
					return false;
				}
				if (v1.equals(v2)) {
					return true;
				}
				if (v1 instanceof String && v2 instanceof String) {
					String s1 = (String) v1;
					String s2 = (String) v2;
					if (s1.equalsIgnoreCase(s2) || this.stringsContainSameNumber(c)) {
						return true;
					}
				}
			} else if (o instanceof String) {
				Object v1 = this.getValue();
				return (v1 != null && this.getValue().equals(o));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean stringsContainSameNumber(Classification other) {
		Vector<String> v1 = this.getNumberStrings();
		Vector<String> v2 = other.getNumberStrings();
		if (v1 != null && v2 != null) {
			for (String nstr1 : v1) {
				if (nstr1.length() > 3 && v2.contains(nstr1)) {
					return true;
				}
			}
		}
		return false;
	}

	public int hashCode() {
		Object value = this.getValue();
		int hc = value.hashCode();
		return hc;
	}

	/**
	 * Gets the id.
	 */
	public String getId() {
		return id;
	}

	public String getName() {
		if (this.getParentClassification() != null
				&& this.getParentClassification().getDisplayAttribute() != null) {
			return this.getParentClassification().getDisplayAttribute()
					.getName();
		}
		return NonVisibleName;
	}

	public String getValue() {
		return getValue(true);
	}

	public String getValue(boolean regularized) {
		String cname = this.getName();
		String cvalue = this.getStringProperty(cname);
		if (regularized) {
			try {
				cvalue = TypeSystem.getTypeSystem().getRegularizedName(cvalue);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (cvalue == null) {
			cvalue = NonVisibleName;
		}
		if ("NULL".equals(cvalue)) {
			int x = 1;
			x = x;
		}
		return cvalue;
	}

	// 9/5/2013: From VA
	public boolean isNonDisplayable() {
		String cname = this.getName();
		String cvalue = this.getStringProperty(cname);
		return cvalue == null;
	}

	public String getFirstDisplayableValue() {
		String cname = this.getName();
		String cvalue = this.getStringProperty(cname);
		if (cvalue == null) {
			for (Enumeration<String> e = this.getProperties().keys(); e
					.hasMoreElements();) {
				String pname = e.nextElement();
				String pvalue = (String) this.getProperty(pname);
				if (pvalue != null) {
					return pvalue;
				}
			}
		}
		return cvalue;
	}

	public boolean hasDisplayableName() {
		String cvalue = getValue(false);
		return (!NonVisibleName.equals(cvalue));
	}

	/**
	 * Stores a name / value pair.
	 */
	public void addValue(String name, String value) {
		this.setProperty(name, value);
	}

	// 5/4/2013
	public void addValue(String value) {
		String name = this.getParentClassification().getDisplayAttribute()
				.getName();
		if (name != null && value != null) {
			this.setProperty(name, value);
		}
	}

	/**
	 * Returns a string displaying all name/value pairs. (Used as an internal
	 * hashtable key.)
	 */
	public String getAttributeString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		Vector<String> pnames = this.getPropertyNames();
		if (pnames != null) {
			for (Enumeration<String> e = pnames.elements(); e.hasMoreElements();) {
				String attribute = e.nextElement();
				sb.append(attribute);
				sb.append('=');
				String pstr = this.getProperty(attribute).toString();
				pstr = this.parentAnnotationType.getTypeSystem()
						.getRegularizedName(pstr);
				sb.append(pstr);
				if (e.hasMoreElements()) {
					sb.append(',');
				}
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public String toString() {
		return getValue();
	}

	public String getCompositeValueString() {
		Vector values = this.getPropertyValues();
		String key = "";
		if (values != null) {
			for (Enumeration<String> e = values.elements(); e.hasMoreElements();) {
				key += e.nextElement().toString();
				if (e.hasMoreElements()) {
					key += ":";
				}
			}
		}
		return key;
	}

	/**
	 * Gets the annotation type.
	 */
	public String getAnnotationType() {
		return annotationType;
	}

	/**
	 * Sets the annotation type.
	 */
	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}

	/**
	 * Creates vector with current (display) name and value; used as internal
	 * key.
	 */
	public String getClassDisplayValueClassificationKey() {
		String key = this.getName() + ":" + this.getValue();
		return key;
	}

	/**
	 * Creates list of vectors representing name/value and type/value keys. Used
	 * to create and store all hashmap indices for this Classification.
	 */
	Vector<String> getClassificationAllValueKeys() {
		Vector<String> keys = null;
		Vector<String> nvkeys = this.getClassificationNameValueKeys();
		// Vector<String> tvkeys = this.getClassificationTypeValueKeys();
		// keys = VUtils.appendIfNot(nvkeys, tvkeys);
		keys = nvkeys;
		return keys;
	}

	/**
	 * Creates key signatures based on annotation type.
	 */
	public Vector<String> getClassificationTypeValueKeys() {
		Vector<String> keys = null;
		if (this.annotationType == null) {
			typesystem.Annotation parent = this.getParentAnnotationType();
			if (parent != null) {
				this.annotationType = parent.getName();
			}
		}
		if (this.annotationType != null) {
			Vector<String> pnames = this.getPropertyNames();
			if (pnames != null) {
				for (String name : pnames) {
					String value = this.getStringProperty(name);
					try {
						value = TypeSystem.getTypeSystem()
								.getRegularizedName(value);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (value != null) {
						String key = this.annotationType + ":"
								+ this.getStringProperty(name);
						keys = VUtils.add(keys, key);
					}
				}
			}
		}
		return keys;
	}

	/**
	 * Gets the classification name value keys.
	 */
	public Vector<String> getClassificationNameValueKeys() {
		Vector<String> keys = null;
		Vector<String> pnames = this.getPropertyNames();
		if (pnames != null) {
			for (String name : pnames) {
				String value = this.getStringProperty(name);
				try {
					value = TypeSystem.getTypeSystem().getRegularizedName(value);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (value != null) {
					String key = name + ":" + value;
					keys = VUtils.add(keys, key);
				}

				// 2/7/2013 -- Use generic classification so I can get stats on
				// single line
				// String key = name + ":GENERIC_CLASSIFICATION";
				// keys = VUtils.add(keys, key);
			}
		}
		return keys;
	}

	// Before 9/23/2012
	// public Vector<String> getClassificationNameValueKeys() {
	// Vector<String> keys = null;
	// Vector<String> pnames = this.getPropertyNames();
	// if (pnames != null) {
	// for (String name : pnames) {
	// String value = this.getStringProperty(name);
	// String key = name + ":" + value;
	// keys = VUtils.add(keys, key);
	// }
	// }
	// return keys;
	// }

	/**
	 * Sorts by Classification name.
	 */
	public static class ClassificationSorter implements Comparator {

		public int compare(Object o1, Object o2) {
			Classification c1 = (Classification) o1;
			Classification c2 = (Classification) o2;
			String s1 = c1.getValue();
			String s2 = c2.getValue();
			if (s1 != null && s2 != null) {
				return s1.compareTo(s2);
			}
			return 0;
		}
	}

	/**
	 * Gets the parent annotation.
	 */
	public typesystem.Annotation getParentAnnotationType() {
		return parentAnnotationType;
	}

	/**
	 * Gets the parent classification.
	 */
	public typesystem.Classification getParentClassification() {
		return parentClassification;
	}

	public int getDisplayIndex() {
		return displayIndex;
	}

	public void setDisplayIndex(int displayIndex) {
		this.displayIndex = displayIndex;
	}

	public boolean isUsedInAnnotation() {
		return usedInAnnotation;
	}

	public void setUsedInAnnotation(boolean usedInAnnotation) {
		this.usedInAnnotation = usedInAnnotation;
	}

	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	public boolean isEmpty() {
		return (this.getProperties() == null || this.getProperties().isEmpty());
	}

	public Vector<String> getNumberStrings() {
		if (this.testedNumberStrings) {
			return this.numberStrings;
		}
		this.testedNumberStrings = true;
		if (this.getValue() instanceof String) {
			this.numberStrings = StrUtils.getNumberSnippets((String) this
					.getValue());
		}
		return this.numberStrings;
	}

	public EVAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(EVAnnotation annotation) {
		this.annotation = annotation;
	}

	public String getDisplayString() {
		String text = "";
		if (this.isPrimary()) {
			text = this.getValue(true);
			if (this.getEquivalentClassification() != null) {
				text += "="
						+ this.getEquivalentClassification().getValue(false);
			}
		} else {
			if (this.getEquivalentClassification() != null) {
				text = this.getEquivalentClassification().getValue(true) + "="
						+ this.getValue(false);
			} else {
				text = "=" + this.getValue(false);
			}
		}
		return text;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public Classification getEquivalentClassification() {
		return equivalentClassification;
	}

	public String getRegularizedValue() {
		if (!this.isPrimary() && this.getEquivalentClassification() != null) {
			return this.getEquivalentClassification().getValue();
		}
		return this.getValue();
	}

	public void setEquivalentClassification(
			Classification equivalentClassification) {
		this.equivalentClassification = equivalentClassification;
	}

	public String getCUI() {
		Vector<String> anames = this.getPropertyNames();
		if (anames != null) {
			for (String aname : anames) {
				String vstr = this.getProperty(aname).toString();
				if ("cui-less".equals(vstr.toLowerCase())) {
					return vstr;
				}
				if (typesystem.Classification.isCUI(vstr)) {
					return vstr.toUpperCase();
				}
			}
		}
		return null;
	}

	// 9/11/2013, for use in creating annotations.
	public String getConceptName() {
		Vector<String> anames = this.getPropertyNames();
		if (anames != null) {
			for (String aname : anames) {
				String value = this.getProperty(aname).toString();
				if ("concept".equals(aname) || "classification".equals(aname)) {
					return value;
				}
			}
		}
		return null;
	}

	// 9/5/2013: From VA
	public boolean containsProperty(String p) {
		String lc = p.toLowerCase();
		if (this.getProperties() != null) {
			for (Enumeration e = this.getProperties().elements(); e
					.hasMoreElements();) {
				String value = (String) e.nextElement();
				if (value.toLowerCase().contains(lc)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String extractCUIFromLabel(String label) {
		String cui = null;
		if (label != null) {
			String lstr = label.toUpperCase();
			for (int i = 0; i < lstr.length() - 1; i++) {
				char c = lstr.charAt(i);
				char next = lstr.charAt(i + 1);
				if (c == 'C' && Character.isDigit(next)) {
					String cstr = "";
					cstr += c;
					int j = i + 1;
					while (j < lstr.length()
							&& Character.isDigit(lstr.charAt(j))) {
						cstr += lstr.charAt(j++);
					}
					cui = cstr;
					break;
				}
			}
		}
		return cui;
	}

	public static String extractCUIFromLabelNEW(String label) {
		String cui = null;
		if (label != null) {
			String lstr = label.toUpperCase();
			for (int i = 0; i < lstr.length() - 1; i++) {
				char c = lstr.charAt(i);
				char next = lstr.charAt(i + 1);
				if (c == 'C' && Character.isDigit(next)) {
					String cstr = "";
					cstr += c;
					int j = i + 1;
					while (j < lstr.length()) {
						char cc = lstr.charAt(j++);
						if (Character.isWhitespace(cc)) {
							continue;
						}
						if (Character.isDigit(cc)) {
							cstr += cc;
						} else {
							break;
						}
					}

					while (j < lstr.length()
							&& Character.isDigit(lstr.charAt(j))) {
						cstr += lstr.charAt(j++);
					}
					cui = cstr;
					break;
				}
			}
		}
		return cui;
	}
	
	public static Vector<Classification> getDisplayableClassifications(
			Vector<Classification> classifications) {
		Vector<Classification> displayable = null;
		if (classifications != null) {
			for (Classification c : classifications) {
				String str = c.getValue();
				if (str != null && !NonVisibleName.equals(str)) {
					displayable = VUtils.addIfNot(displayable, c);
//					displayable = VUtils.add(displayable, c);
				}
			}
		}
		return displayable;
	}

	// Before 3/5/2014
//	public static Vector<Classification> getDisplayableClassifications(
//			Vector<Classification> classifications) {
//		Vector<Classification> displayable = null;
//		if (classifications != null) {
//			for (Classification c : classifications) {
//				String str = c.getValue();
//				if (str != null && !NonVisibleName.equals(str)) {
//					displayable = VUtils.add(displayable, c);
//				}
//			}
//		}
//		return displayable;
//	}

}
