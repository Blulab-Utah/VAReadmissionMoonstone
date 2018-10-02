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
package tsl.information;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import tsl.expression.term.relation.RelationConstant;
import tsl.tsllisp.TLObject;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public abstract class TSLInformation extends TLObject implements Serializable {

	protected Hashtable<String, Object> properties = null;

	private Vector pattern = null;
	private Hashtable<RelationConstant, Vector<TSLInformation>> relations = null;
	private String propertyType = null;

	private static Vector<String> attributeVector = null;
	private static Hashtable attributeTypeHash = new Hashtable();
	private static Hashtable defaultAttributeValueHash = new Hashtable();
	private static Hashtable<String, Integer> defaultExcelAttributeIndexHash = new Hashtable();
	private static Hashtable<Integer, Vector<String>> defaultExcelIndexAttributeHash = new Hashtable(0);
	private static int defaultExcelAttributeMaxIndex = -1;
	public static String[] relevantFeatures = null;

	public TSLInformation() {
	}

	public TSLInformation(Vector pattern) {
		this.pattern = pattern;
		this.setPropertiesFromPattern(pattern);
	}

	protected void initializeProperties() {
		if (this.properties == null) {
			this.properties = new Hashtable();
		}
	}

	public boolean hasProperties() {
		return this.properties != null && !this.properties.isEmpty();
	}

	public boolean propertiesIntersect(TSLInformation other) {
		this.initializeProperties();
		other.initializeProperties();
		TSLInformation shortest = (this.getPropertyNumber() < other.getPropertyNumber() ? this : other);
		TSLInformation longest = (shortest == this ? other : this);
		if (shortest.getPropertyNumber() == 0) {
			return false;
		}
		boolean hasCommonProperties = false;
		for (String key : shortest.getPropertyNames()) {
			Object v1 = shortest.getProperty(key);
			Object v2 = longest.getProperty(key);
			if (v2 != null) {
				hasCommonProperties = true;
				if (!v1.equals(v2)) {
					return false;
				}
			}
		}
		return hasCommonProperties;
	}

	public Object getProperty(String attribute) {
		initializeProperties();
		Object value = properties.get(attribute);
		return value;
	}

	// 3/26/2015: Depth-first search for property value.
	public Object getPropertyRecursive(String attribute) {
		Object value = this.getProperty(attribute);
		if (value != null) {
			return value;
		}
		for (Enumeration e = this.getProperties().elements(); e.hasMoreElements();) {
			Object o = e.nextElement();
			if (!o.equals(this) && o instanceof TSLInformation && ((TSLInformation) o).getProperties() != null) {
				TSLInformation tsli = (TSLInformation) o;
				value = tsli.getPropertyRecursive(attribute);
				if (value != null) {
					return value;
				}
			}
		}
		return null;
	}

	public Object getPropertyWithDefault(String attribute) {
		initializeProperties();
		Object value = properties.get(attribute);
		if (value == null) {
			value = getDefault(attribute);
		}
		return value;
	}

	public static Object getProperty(Vector v, String attribute) {
		Vector subv = VUtils.assoc(attribute, v);
		Object value = null;
		if (subv != null) {
			value = subv.elementAt(1);
		}
		return value;
	}

	public String getAllPropertyString() {
		String pstr = null;
		if (!this.getProperties().isEmpty()) {
			pstr = "";
			for (Enumeration e = this.getProperties().keys(); e.hasMoreElements();) {
				Object property = e.nextElement();
				Object value = this.getProperties().get(property);
				pstr += property.toString() + "=" + value.toString();
				if (e.hasMoreElements()) {
					pstr += "&";
				}
			}
		}
		return pstr;
	}

	public String getStringProperty(String attribute) {
		Object o = getPropertyWithDefault(attribute);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	public int getIntProperty(String attribute) {
		Object value = getPropertyWithDefault(attribute);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		} else if (value instanceof Float) {
			return ((Float) value).intValue();
		}
		return -1;
	}

	public float getFloatProperty(String attribute) {
		Object value = getPropertyWithDefault(attribute);
		if (value instanceof Float) {
			return ((Float) value).floatValue();
		}
		return -1f;
	}

	public boolean getBooleanProperty(String attribute) {
		Object value = getPropertyWithDefault(attribute);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	public void setProperty(String attribute, Object value) {
		initializeProperties();
		if (attribute != null && value != null) {
			this.properties.put(attribute, value);
		}
	}

	public static void setProperty(Hashtable hash, String attribute, Object value) {
		if (hash != null && attribute != null && attribute.length() > 0 && value != null
				&& value.toString().length() > 0) {
			hash.put(attribute, value);
		}
	}

	public void setProperty(String attribute, int value) {
		initializeProperties();
		if (attribute != null) {
			this.properties.put(attribute, new Integer(value));
		}
	}

	public void setProperty(String attribute, float value) {
		initializeProperties();
		if (attribute != null) {
			this.properties.put(attribute, new Float(value));
		}
	}

	public void setProperty(String attribute, boolean value) {
		initializeProperties();
		if (attribute != null) {
			this.properties.put(attribute, new Boolean(value));
		}
	}

	public static Vector<String> gatherDistributableProperties(TSLInformation source, Vector<TSLInformation> targets) {
		Hashtable<String, Object> sprops = source.getProperties();
		Vector<String> keys = null;
		for (Enumeration<String> e = sprops.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			boolean found = false;
			for (TSLInformation target : targets) {
				if (target.getProperties().get(key) != null) {
					found = true;
				}
			}
			if (!found) {
				keys = VUtils.add(keys, key);
			}
		}
		return keys;
	}

	public static boolean propertiesAreUnifiable(TSLInformation i1, TSLInformation i2) {
		for (Enumeration<String> e1 = i1.getProperties().keys(); e1.hasMoreElements();) {
			String pname1 = e1.nextElement();
			Object value1 = i1.getProperty(pname1);
			Object value2 = i2.getProperty(pname1);
			if (!value1.equals(value2)) {
				return false;
			}
		}
		for (Enumeration<String> e2 = i2.getProperties().keys(); e2.hasMoreElements();) {
			String pname2 = e2.nextElement();
			Object value2 = i2.getProperty(pname2);
			Object value1 = i1.getProperty(pname2);
			if (!value2.equals(value1)) {
				return false;
			}
		}
		return true;
	}

	public static Hashtable<String, Object> unifyProperties(TSLInformation i1, TSLInformation i2) {
		Hashtable<String, Object> properties = null;
		if (propertiesAreUnifiable(i1, i2)) {
			properties = new Hashtable();
			for (Enumeration<String> e1 = i1.getProperties().keys(); e1.hasMoreElements();) {
				String pname1 = e1.nextElement();
				Object value1 = i1.getProperty(pname1);
				properties.put(pname1, value1);
			}
			for (Enumeration<String> e2 = i2.getProperties().keys(); e2.hasMoreElements();) {
				String pname2 = e2.nextElement();
				Object value2 = i2.getProperty(pname2);
				properties.put(pname2, value2);
			}
		}
		return properties;
	}

	public static void distributeProperties(TSLInformation source, Vector<TSLInformation> targets) {
		Vector<String> keys = gatherDistributableProperties(source, targets);
		if (keys != null) {
			for (String key : keys) {
				Object value = source.getProperty(key);
				for (TSLInformation target : targets) {
					target.setProperty(key, value);
				}
			}
		}
	}

	public static void copyProperties(Hashtable<String, Object> p1, Hashtable<String, Object> p2) {
		if (p1 != null && p2 != null) {
			for (Enumeration<String> e = p1.keys(); e.hasMoreElements();) {
				String key = e.nextElement();
				Object value = p1.get(key);
				if (p2.get(key) == null) {
					p2.put(key, value);
				}
			}
		}
	}

	public static void copyProperties(TSLInformation i1, TSLInformation i2) {
		if (i1 != null && i2 != null) {
			copyProperties(i1.getProperties(), i2.getProperties());
		}
	}

	// //////////// 4/24/2013

	// ////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////

	public static String[] getRelevantFeatures() {
		return relevantFeatures;
	}

	void initializeRelations() {
		if (this.relations == null) {
			this.relations = new Hashtable();
		}
	}

	public void setRelevantProperties(Hashtable<String, Object> hash) {
		initializeProperties();
		String[] features = getRelevantFeatures();
		if (features != null) {
			for (int i = 0; i < features.length; i++) {
				String feature = features[i];
				Object value = hash.get(feature);
				if (value != null) {
					this.properties.put(feature, value);
				}
			}
		}
	}

	public void setRelevantProperties(Vector v) {
		initializeProperties();
		String[] features = getRelevantFeatures();
		if (v != null && features != null) {
			for (int i = 0; i < features.length; i++) {
				String feature = features[i];
				Vector pair = VUtils.assoc(feature, v);
				if (pair != null) {
					this.properties.put(feature, pair.elementAt(1));
				}
			}
		}
	}

	public void setPropertiesFromPattern(Vector pattern) {
		initializeProperties();
		Vector avpairs = getAVPairs(pattern);
		if (avpairs != null) {
			for (ListIterator it = avpairs.listIterator(); it.hasNext();) {
				setProperty((Vector) it.next());
			}
		}
	}

	public void setProperty(Vector v) {
		initializeProperties();
		if (v != null) {
			String attribute = (String) v.firstElement();
			Object value = v.lastElement();
			String type = (String) TSLInformation.getPropertyType(attribute);
			if (type != null) {
				if ("int".equals(type) && value instanceof Float) {
					value = ((Float) value).intValue();
				} else if ("boolean".equals(type) && value instanceof String) {
					value = Boolean.parseBoolean((String) value);
				} else if ("vlist".equals(type) && value instanceof Vector) {
					value = VUtils.assocValue(attribute, v);
				}
			}
			this.properties.put(attribute, value);
		}
	}

	public void setProperties(Hashtable properties) {
		this.properties = properties;
	}

	public static Vector getAVPairs(Vector v) {
		Vector<Vector> avpairs = null;
		if (v != null) {
			if (v.firstElement() instanceof String
					&& (attributeVector == null || attributeVector.contains(v.firstElement()))) {
				Vector avpair = wrapAVPair(v);
				if (avpair != null) {
					avpairs = VUtils.add(avpairs, avpair);
				}
			} else {
				for (Object o : v) {
					if (o instanceof Vector) {
						avpairs = VUtils.append(avpairs, getAVPairs((Vector) o));
					}
				}
			}
		}
		return avpairs;
	}

	public static Vector wrapAVPair(Vector v) {
		Vector newv = null;
		if (v.firstElement() instanceof String) {
			String attribute = (String) v.firstElement();
			if (isVList(attribute)) {
				newv = new Vector(0);
				newv.add(v.firstElement());
				newv.add(VUtils.rest(v));
			} else if (isList(attribute)) {
				// 1/7/2012
				if (v.elementAt(1) instanceof Vector) {
					newv = v;
				} else {
					// Before 1/7/2013
					newv = new Vector(0);
					newv.add(v.firstElement());
					newv.add(VUtils.rest(v));
				}
			} else {
				newv = v;
			}
		}
		return newv;
	}

	public void setRelation(RelationConstant relation, TSLInformation relatum) {
		initializeRelations();
		VUtils.pushIfNotHashVector(this.relations, relation, relatum);
	}

	public TSLInformation getRelation(RelationConstant relation) {
		initializeRelations();
		Vector<TSLInformation> relata = this.relations.get(relation);
		if (relata != null) {
			return relata.firstElement();
		}
		return null;
	}

	public Vector<TSLInformation> getRelations(RelationConstant relation) {
		initializeRelations();
		return this.relations.get(relation);
	}

	public static Object getDefault(String attribute) {
		return defaultAttributeValueHash.get(attribute);
		// return defaultAttributeValueHash.get(attribute.toLowerCase());
	}

	public static Object getPropertyType(String attribute) {
		return attributeTypeHash.get(attribute);
		// return attributeTypeHash.get(attribute.toLowerCase());
	}

	public Hashtable getProperties() {
		initializeProperties();
		return properties;
	}

	public Vector<String> getPropertyNames() {
		initializeProperties();
		return (Vector<String>) HUtils.getKeys(properties);
	}

	public static Vector<String> getPropertyNames(Vector<TSLInformation> items) {
		if (items != null) {
			Hashtable<String, String> phash = new Hashtable();
			for (TSLInformation item : items) {
				if (item != null && item.properties != null && !item.properties.isEmpty()) {
					for (String property : item.getPropertyNames()) {
						phash.put(property, property);
					}
				}
			}
			return HUtils.getKeys(phash);
		}
		return null;
	}

	public Vector getPropertyValues() {
		Vector<String> properties = this.getPropertyNames();
		Vector values = null;
		if (properties != null) {
			Collections.sort(properties);
			for (String property : properties) {
				Object value = this.getProperty(property);
				values = VUtils.add(values, value);
			}
		}
		return values;
	}

	public int getPropertyNumber() {
		int size = 0;
		if (properties != null) {
			size = properties.size();
		}
		return size;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String type) {
		this.propertyType = type;
	}

	public Vector getPattern() {
		return pattern;
	}

	public void setPattern(Vector pattern) {
		this.pattern = pattern;
	}

	public static boolean isSexp(String attribute) {
		return "sexp".equals(getPropertyType(attribute));
	}

	public static boolean isSentence(String attribute) {
		return "sentence".equals(getPropertyType(attribute));
	}

	public static boolean isList(String attribute) {
		return "list".equals(getPropertyType(attribute));
	}

	public static boolean isVList(String attribute) {
		Object o = getPropertyType(attribute);
		return "vlist".equals(o);
	}

	public static boolean isString(String attribute) {
		return "string".equals(getPropertyType(attribute));
	}

	public static boolean isBoolean(String attribute) {
		return "boolean".equals(getPropertyType(attribute));
	}

	public static boolean isNumber(String attribute) {
		return "number".equals(getPropertyType(attribute));
	}

	public static void setRelevantFeatures(Vector<String> rf) {
		relevantFeatures = new String[rf.size()];
		for (int i = 0; i < rf.size(); i++) {
			relevantFeatures[i] = rf.elementAt(i);
		}
	}

	public static void setDefaults(Vector defaults) {
		try {
			for (ListIterator it = defaults.listIterator(); it.hasNext();) {
				Vector subv = (Vector) it.next();
				String attribute = (String) subv.elementAt(0);
				String type = (String) subv.elementAt(1);
				Object value = null;
				if ("number".equals(type)) {
					value = (Float) subv.elementAt(2);
				} else {
					String def = (String) subv.elementAt(2);
					if (!def.equals("null")) {
						if ("string".equals(type)) {
							value = def;
						} else if ("boolean".equals(type)) {
							value = Boolean.valueOf(def);
						} else if ("number".equals(type)) {
							value = Float.valueOf(def);
						}
					}
				}
				attributeTypeHash.put(attribute, type);
				attributeVector = VUtils.addIfNot(attributeVector, attribute);
				if (value != null) {
					defaultAttributeValueHash.put(attribute, value);
				}
				if (subv.size() >= 4 && subv.elementAt(3) instanceof Float) {
					Integer index = new Integer(((Float) subv.elementAt(3)).intValue());
					defaultExcelAttributeIndexHash.put(attribute, index);
					VUtils.pushHashVector(defaultExcelIndexAttributeHash, index, attribute);
					if (index > defaultExcelAttributeMaxIndex) {
						defaultExcelAttributeMaxIndex = index;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 5/11/2016
	public static int getExcelAttributeIndex(String attribute) {
		if (attribute != null) {
			Integer index = defaultExcelAttributeIndexHash.get(attribute);
			if (index != null) {
				return index.intValue();
			}
		}
		return -1;
	}

	public static Vector<String> getExcelIndexAttributes(int index) {
		return defaultExcelIndexAttributeHash.get(index);
	}

	public static int getExcelAttributeMaxIndex() {
		return defaultExcelAttributeMaxIndex;
	}

	public static Vector<TSLInformation> wrapInfos(Vector infos) {
		Vector<TSLInformation> v = null;
		for (Object o : infos) {
			TSLInformation info = (TSLInformation) o;
			v = VUtils.add(v, (TSLInformation) info);
		}
		return v;
	}

	// 12/18/2013
	public static boolean validateProperty(String property, Object value) {
		if (property == null || value == null) {
			return false;
		}
		Object type = getPropertyType(property);
		if (type == null) {
			return false;
		}
		if ("number".equals(type)) {
			return (value instanceof Float);
		}
		if ("string".equals(type)) {
			return (value instanceof String);
		}
		if ("boolean".equals(type)) {
			if (value instanceof Boolean) {
				return true;
			}
			return "true".equals(value) || "false".equals(value);
		}
		if ("vlist".equals(type)) {
			if (!(value instanceof Vector)) {
				return false;
			}
			for (Object o : (Vector) value) {
				if (!(o instanceof Vector)) {
					return false;
				}
			}
			return true;
		}
		if ("list".equals(type)) {
			if (!(value instanceof Vector)) {
				return false;
			}
			for (Object o : (Vector) value) {
				if (o instanceof Vector) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// 10/2/2014: Moving these things from Moonstone.Annotation, so I can
	// refer to them in Expression's patternEval() method.
	public static boolean isConceptString(String token) {
		return (token != null && token.length() > 2 && token.charAt(0) == ':' && Character.isUpperCase(token.charAt(1))
				&& token.charAt(token.length() - 1) == ':');
	}

	public static boolean isTypeString(String token) {
		return (token != null && token.length() > 2 && token.charAt(0) == '<' && Character.isUpperCase(token.charAt(1))
				&& token.charAt(token.length() - 1) == '>');
	}

	public static boolean isMacroString(String token) {
		return (token != null && token.length() > 2 && token.charAt(0) == '_' && Character.isUpperCase(token.charAt(1))
				&& token.charAt(token.length() - 1) == '_');
	}

	public static boolean isCUIString(String token) {
		return (token != null && token.length() > 2 && Character.isLetter(token.charAt(0))
				&& (token.charAt(1) == '_' || Character.isDigit(token.charAt(1))));
	}

	public static boolean isPhraseTypeString(String token) {
		return (token != null && token.length() > 2 && token.charAt(0) == '#' && Character.isUpperCase(token.charAt(1))
				&& token.charAt(token.length() - 1) == '#');
	}

	public static boolean isLowerCaseWordString(Object token) {
		if (token != null) {
			String tstr = token.toString();
			for (int i = 0; i < tstr.length(); i++) {
				char c = tstr.charAt(i);
				if (!Character.isLowerCase(c)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
