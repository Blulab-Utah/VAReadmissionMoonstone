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
package tsl.utilities;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class VUtils {
	
	public static int size(Vector v) {
		return (v != null ? v.size() : 0);
	}

	public static boolean isOneDeepVector(Vector v) {
		return (v != null && !v.isEmpty() && !(v.firstElement() instanceof Vector));
	}

	public static boolean isMultiDeepVector(Vector v) {
		return (v != null && !v.isEmpty() && !isOneDeepVector(v));
	}

	// 4/16/2015
	public static Vector collectIfNotClass(Vector v, Class c) {
		Vector clean = null;
		if (v != null && c != null) {
			for (Object o : v) {
				if (!c.equals(o.getClass())) {
					clean = VUtils.add(clean, o);
				}
			}
		}
		return clean;
	}

	public static Vector collectIfClass(Vector v, Class c) {
		Vector clean = null;
		if (v != null && c != null) {
			for (Object o : v) {
				if (c.equals(o.getClass())) {
					clean = VUtils.add(clean, o);
				}
			}
		}
		return clean;
	}

	public static float degreeOverlap(Vector v1, Vector v2) {
		if (v1 != null && v2 != null) {
			float countAlike = 0;
			Vector shortest = (v1.size() < v2.size() ? v1 : v2);
			for (int i = 0; i < shortest.size(); i++) {
				if (v1.elementAt(i).equals(v2.elementAt(i))) {
					countAlike++;
				}
			}
			return countAlike / (v1.size() + v2.size());
		}
		return 0;
	}

	public static Vector castTypes(Vector v, Class c) {
		Vector newv = null;
		if (v != null) {
			for (Object o : v) {
				if (c.isAssignableFrom(o.getClass())) {
					newv = VUtils.add(newv, c.cast(o));
				}
			}
		}
		return newv;
	}

	public static Object firstElement(Vector v) {
		if (v != null && !v.isEmpty()) {
			return v.firstElement();
		}
		return null;
	}

	public static Object lastElement(Vector v) {
		if (v != null && !v.isEmpty()) {
			return v.lastElement();
		}
		return null;
	}

	public static boolean containsNull(Vector v) {
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				if (e.nextElement() == null) {
					return true;
				}
			}
		}
		return false;
	}

	// 10/24/2008 -- This only works for objects that have defined hashcodes. I
	// don't want to rely on that.
	public static Vector removeDuplicates(Vector v) {
		Hashtable hash = new Hashtable();
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				hash.put(o, o);
			}
			return new Vector(hash.values());
		}
		return null;
	}

	public static boolean containsDuplicates(Vector v) {
		if (v != null) {
			for (int i = 0; i < v.size() - 1; i++) {
				for (int j = i + 1; j < v.size(); j++) {
					if (v.elementAt(i).equals(v.elementAt(j))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Vector create2DVector(int size) {
		Vector v = new Vector(0);
		for (int i = 0; i < size; i++) {
			v.add(new Vector(0));
		}
		return v;
	}

	public static boolean isVector(Object o) {
		return (o != null && o.getClass().equals(Vector.class));
	}

	public static boolean containsAny(Vector v, Object[] array) {
		for (int i = 0; i < array.length; i++) {
			if (v.contains(array[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean containedIn(Object o, Object[] array) {
		for (int i = 0; i < array.length; i++) {
			if (o.equals(array[i])) {
				return true;
			}
		}
		return false;
	}

	public static Vector arrayToVector(Object[] array) {
		Vector v = null;
		if (array != null) {
			v = new Vector(0);
			for (int i = 0; i < array.length; i++) {
				Object o = array[i];
				if (o != null && o instanceof Object[]) {
					o = arrayToVector((Object[]) o);
				}
				v.add(o);
			}
		}
		return v;
	}

	public static Vector appendArrayToVector(Vector v, Object[] a) {
		if (v == null) {
			v = new Vector(0);
		}
		Object[] array = new Object[] { a };
		for (int i = 0; i < array.length; i++) {
			Object o = array[i];
			if (o.getClass().equals(Object[].class)) {
				o = arrayToVector((Object[]) o);
			}
			v.add(o);
		}
		return v;
	}

	public static Object[] vectorToArray(Vector v) {
		if (v != null) {
			Object[] array = new Object[v.size()];
			v.toArray(array);
			return array;
		}
		return null;
	}

	public static void copyVectorToArray(Vector v, Object[] array) {
		if (v != null && v.size() == array.length) {
			for (int i = 0; i < v.size(); i++) {
				array[i] = v.elementAt(i);
			}
		}
	}

	public static String[] vectorToStringArray(Vector<String> v) {
		if (v != null) {
			String[] array = new String[v.size()];
			v.toArray(array);
			return array;
		}
		return null;
	}
	
	public static Vector listToVector(List l) {
		Vector v = null;
		if (l != null) {
			v = new Vector(0);
			for (Object o : l) {
				v.add(o);
			}
		}
		return v;
	}

	// 12/27/2012
	public static Object gatherVectorsWithNthMatchingFields(Vector<Vector> v,
			Object key, int index) {
		Vector rv = null;
		if (v != null) {
			for (Vector<Vector> sv : v) {
				Object o = sv.elementAt(index);
				if (o != null && o.equals(key)) {
					rv = VUtils.add(rv, sv);
				}
			}
		}
		return rv;
	}

	public static Vector gatherObjectsOfClass(Vector v, Class c) {
		Vector rv = null;
		if (v != null) {
			for (Object o : v) {
				if (o.getClass().equals(c)) {
					rv = VUtils.add(rv, o);
				}
			}
		}
		return rv;
	}

	public static Vector gatherFieldsUnique(Vector v, String fieldname) {
		Vector newv = null;
		if (v != null && v.size() > 0) {
			Object o = v.firstElement();
			Class c = o.getClass();
			newv = gatherFields(v, c, fieldname, true);
		}
		return newv;
	}

	public static Vector gatherFields(Vector v, String fieldname) {
		Vector newv = null;
		if (v != null && v.size() > 0) {
			Object o = v.firstElement();
			Class c = o.getClass();
			newv = gatherFields(v, c, fieldname, false);
		}
		return newv;
	}

	public static Vector gatherFields(Vector v, Class c, String fieldname,
			boolean unique) {
		Vector newv = null;
		try {
			if (v != null && v.size() > 0) {
				Field field = c.getField(fieldname);
				for (Enumeration e = v.elements(); e.hasMoreElements();) {
					Object o = e.nextElement();
					Object fieldobject = field.get(o);
					if (fieldobject != null) {
						if (unique) {
							newv = VUtils.addIfNot(newv, fieldobject);
						} else {
							newv = VUtils.add(newv, fieldobject);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newv;
	}

	public static Vector gatherByClass(Vector v, Class c) {
		Vector matching = null;
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (c.isAssignableFrom(o.getClass())) {
					matching = VUtils.add(matching, o);
				}
			}
		}
		return matching;
	}

	public static Object findIfMatchingFieldFirst(Vector v, String fieldname,
			Object other) {
		Vector rv = null;
		if (v != null && other != null) {
			rv = findIfMatchingField(v, fieldname, other);
		}
		return (rv != null ? rv.firstElement() : null);
	}

	public static Vector findIfMatchingField(Vector v, String fieldname,
			Object other) {
		Vector matching = null;
		try {
			if (v != null && other != null) {
				Class c = v.firstElement().getClass();
				Field field = c.getField(fieldname);
				for (Enumeration e = v.elements(); e.hasMoreElements();) {
					Object o = e.nextElement();
					Object fieldobject = field.get(o);
					if (fieldobject != null && fieldobject.equals(other)) {
						matching = VUtils.add(matching, o);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matching;
	}

	public static Vector findIfContainingField(Vector v, String fieldname,
			Vector others) {
		Vector matching = null;
		try {
			if (v == null || others == null) {
				matching = v;
			} else {
				Class c = v.firstElement().getClass();
				Field field = c.getField(fieldname);
				for (Enumeration e = v.elements(); e.hasMoreElements();) {
					Object o = e.nextElement();
					Object fieldobject = field.get(o);
					if (fieldobject != null && others.contains(fieldobject)) {
						matching = VUtils.add(matching, o);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matching;
	}

	public static Vector appendFieldVectorsUnique(Vector v, String fieldname) {
		Vector rv = null;
		try {
			if (v != null && !v.isEmpty()) {
				Class c = v.firstElement().getClass();
				Field field = c.getField(fieldname);
				for (Enumeration e = v.elements(); e.hasMoreElements();) {
					Object o = e.nextElement();
					Object fieldobject = field.get(o);
					if (fieldobject != null && fieldobject instanceof Vector) {
						rv = VUtils.appendIfNot(rv, (Vector) fieldobject);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

	// Recursive clone
	public static Vector clone(Vector v) {
		Vector newv = null;
		if (v != null) {
			newv = new Vector(0);
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				Object rv = (o instanceof Vector ? clone((Vector) o) : o);
				newv.add(rv);
			}
		}
		return newv;
	}

	public static Vector flatten(Vector v) {
		Vector newv = null;
		if (v != null) {
			newv = new Vector(0);
			flattenAux(v, newv);
		}
		return newv;
	}

	private static void flattenAux(Vector v, Vector newv) {
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			Object o = e.nextElement();
			if (o != null) {
				if (isVector(o)) {
					flattenAux((Vector) o, newv);
				} else {
					newv.add(o);
				}
			}
		}
	}

	public static Vector remove(Vector v, Vector items) {
		Vector newv = null;
		if (v != null && items != null) {
			for (Object item : items) {
				newv = remove(v, item);
			}
		}
		return newv;
	}

	public static Vector remove(Vector v, Object item) {
		if (v != null && item != null) {
			v.remove(item);
			if (v.isEmpty()) {
				v = null;
			}
		}
		return v;
	}

	public static Vector removeNulls(Vector v) {
		Vector newv = null;
		if (v != null) {
			for (Object o : v) {
				if (o != null) {
					newv = VUtils.add(newv, o);
				}
			}
		}
		return newv;
	}

	// Appends contents of one vector to earlier vector
	public static Vector append(Vector v1, Vector v2) {
		if (v2 != null && v1 != v2) {
			if (v1 == null) {
				v1 = new Vector(0);
			}
			for (Enumeration e = v2.elements(); e.hasMoreElements();) {
				v1.add(e.nextElement());
			}
		}
		return v1;
	}

	public static Vector appendIfNot(Vector v1, Vector v2) {
		if (v2 != null && v1 != v2) {
			if (v1 == null) {
				v1 = new Vector(0);
			}
			for (Enumeration e = v2.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (!v1.contains(o)) {
					v1.add(o);
				}
			}
		}
		return v1;
	}

	public static Vector appendIfNot(Vector<Vector> v) {
		Vector results = null;
		if (v != null) {
			for (Vector subv : v) {
				results = VUtils.appendIfNot(results, subv);
			}
		}
		return results;
	}

	// Creates new vector containing contents of two input vectors
	public static Vector appendNew(Vector v1, Vector v2) {
		Vector rv = null;
		if (v1 != null) {
			for (Enumeration e = v1.elements(); e.hasMoreElements();) {
				rv = add(rv, e.nextElement());
			}
		}
		if (v2 != null) {
			for (Enumeration e = v2.elements(); e.hasMoreElements();) {
				rv = add(rv, e.nextElement());
			}
		}
		return rv;
	}

	// Saves having to create a vector
	public static Vector add(Vector v, Object value) {
		if (value != null) {
			if (v == null) {
				v = new Vector(0);
			}
			v.add(value);
		}
		return v;
	}

	public static Vector addNew(Vector v, Object value) {
		if (value != null) {
			if (v == null) {
				v = new Vector(0);
			} else {
				v = (Vector) v.clone();
			}
			v.add(value);
		}
		return v;
	}

	public static Vector addIfNot(Vector v, Object value) {
		if (value != null) {
			if (v == null) {
				v = new Vector(0);
			}
			if (value != null && !v.contains(value)) {
				v.add(value);
			}
		}
		return v;
	}

	// PROFILE: THIS 40% OF THE TIME
	public static void pushHashVector(Hashtable hash, Object key, Object value) {
		if (key != null && value != null) {
			Vector v = (Vector) hash.get(key);
			if (v != null) {
				v.add(value);
			} else {
				v = listify(value);
				hash.put(key, v);
			}
		}
	}

	public static void pushHashVector(Map map, Object key, Object value) {
		if (key != null && value != null) {
			Vector v = (Vector) map.get(key);
			if (v != null) {
				v.add(value);
			} else {
				v = listify(value);
				map.put(key, v);
			}
		}
	}

	public static void popHashVector(Hashtable hash, Object key, Object value) {
		if (key != null) {
			Vector v = (Vector) hash.get(key);
			if (v != null) {
				v.remove(value);
				if (v.isEmpty()) {
					hash.remove(key);
				}
			}
		}
	}

	public static void pushIfNotHashVector(Hashtable hash, Object key,
			Object value) {
		if (key != null && value != null) {
			Vector v = (Vector) hash.get(key);
			if (v != null) {
				if (!v.contains(value)) {
					v.add(value);
				}
			} else {
				v = listify(value);
				hash.put(key, v);
			}
		}
	}

	public static void pushIfNotHashVector(Map map, Object key, Object value) {
		if (key != null) {
			Vector v = (Vector) map.get(key);
			if (v != null) {
				if (!v.contains(value)) {
					v.add(value);
				}
			} else {
				v = listify(value);
				map.put(key, v);
			}
		}
	}

	public static Vector gatherVectors(Hashtable hash, Vector keys) {
		Vector rv = new Vector(0);
		if (keys != null) {
			for (Enumeration e = keys.elements(); e.hasMoreElements();) {
				Object o = hash.get(e.nextElement());
				if (o != null && o instanceof Vector) {
					rv.add(o);
				}
			}
		}
		return (!rv.isEmpty() ? rv : null);
	}

	public static Vector listify(Object o1, Object o2) {
		Vector v = new Vector(0);
		v.add(o1);
		v.add(o2);
		return v;
	}

	public static Vector listify(Object o1, Object o2, Object o3) {
		Vector v = new Vector(0);
		v.add(o1);
		v.add(o2);
		v.add(o3);
		return v;
	}

	public static Vector listify(Object o1, Object o2, Object o3, Object o4) {
		Vector v = new Vector(0);
		v.add(o1);
		v.add(o2);
		v.add(o3);
		v.add(o4);
		return v;
	}

	public static Vector listify(Object o1, Object o2, Object o3, Object o4,
			Object o5) {
		Vector v = new Vector(0);
		v.add(o1);
		v.add(o2);
		v.add(o3);
		v.add(o4);
		v.add(o5);
		return v;
	}
	
	public static Vector listify(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6) {
		Vector v = new Vector(0);
		v.add(o1);
		v.add(o2);
		v.add(o3);
		v.add(o4);
		v.add(o5);
		v.add(o6);
		return v;
	}

	public static Vector listify(Object o) {
		return listify(o, 0);
	}

	public static Vector listify(Object o, int initialSize) {
		Vector rv = new Vector(initialSize);
		rv.add(o);
		return rv;
	}

	public static Vector listifyVector(Vector v) {
		Vector rv = new Vector(0);
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			rv.add(listify(e.nextElement()));
		}
		return rv;
	}

	// 12/27/2012
	public static Vector assocAll(Object key, Vector v) {
		Vector rv = null;
		if (key != null && v != null) {
			for (Object o : v) {
				if (o instanceof Vector) {
					Vector sv = (Vector) o;
					if (key.equals(sv.firstElement())) {
						rv = VUtils.add(rv, sv);
					} else {
						rv = VUtils.append(rv, assocAll(key, sv));
					}
				}
			}
		}
		return rv;
	}

	// Returns vector beginning with key. Does depth-first search.
	public static Vector assoc(Object key, Vector v) {
		Vector rv = null;
		if (v != null) {
			if (key.equals(v.firstElement())) {
				return v;
			}
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (o instanceof Vector
						&& (rv = assoc(key, (Vector) o)) != null) {
					return rv;
				}
			}
		}
		return null;
	}

	public static Vector assoc(String[] keys, Vector v) {
		if (keys != null && v != null) {
			v = new Vector(v);
			for (int i = 0; v != null && i < keys.length; i++) {
				v = assoc(keys[i], v);
			}
			return v;
		}
		return null;
	}

	// 9/24/2007: Like 'assoc, but returns the single object following
	// the key.
	public static Object assocValue(Object key, Vector v) {
		Object value = null;
		Vector keyv = assoc(key, v);
		if (keyv != null && keyv.size() > 1) {
			value = keyv.elementAt(1);
		}
		return value;
	}

	// Returns vector beginning with key at top level
	public static Vector assocTopLevel(Object key, Vector v) {
		if (v != null) {
			for (Object o : v) {
				if (o instanceof Vector) {
					Vector subv = (Vector) o;
					if (key.equals(subv.firstElement())) {
						return subv;
					}
				}
			}
		}
		return null;
	}

	public static Object assocValueTopLevel(Object key, Vector v) {
		Vector keyv = assocTopLevel(key, v);
		if (keyv != null && keyv.size() > 1) {
			return keyv.elementAt(1);
		}
		return null;
	}

	public static Vector gatherNthElements(Vector v, int n) {
		Vector elements = null;
		if (v != null) {
			elements = new Vector(0);
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Vector subv = (Vector) e.nextElement();
				elements.add(subv.elementAt(n));
			}
		}
		return elements;
	}

	public static Vector rest(Vector v) {
		Vector rv = null;
		if (v != null && v.size() > 1) {
			for (int i = 1; i < v.size(); i++) {
				rv = VUtils.add(rv, v.elementAt(i));
			}
		}
		return rv;
	}

	public static Vector restNEW(Vector v) {
		Vector rv = null;
		if (v != null && v.size() > 1) {
			for (int i = 1; i < v.size(); i++) {
				rv = VUtils.add(rv, v.elementAt(i));
			}
		}
		return rv;
	}

	public static Vector<Vector> restVectors(Vector v) {
		Vector<Vector> rv = null;
		if (v != null && v.size() > 1) {
			for (int i = 1; i < v.size(); i++) {
				Vector sub = (Vector) v.elementAt(i);
				rv = VUtils.add(rv, sub);
			}
		}
		return rv;
	}

	public static Vector butLast(Vector v) {
		Vector rv = null;
		if (v != null) {
			for (int i = 0; i < v.size() - 1; i++) {
				rv = VUtils.add(rv, v.elementAt(i));
			}
		}
		return rv;
	}

	public static Vector subVector(Vector v, int start) {
		return subVector(v, start, v.size());
	}

	public static Vector subVector(Vector v, int start, int end) {
		Vector rv = new Vector(0);
		if (end > v.size()) {
			end = v.size();
		}
		for (int i = start; i < end; i++) {
			rv.add(v.elementAt(i));
		}
		return rv;
	}

	public static int maxCardinality(Vector v) {
		if (v != null) {
			v = (Vector) v.clone();
			Collections.sort(v, new InverseLengthSorter());
			Vector first = (Vector) v.firstElement();
			return first.size();
		}
		return 0;
	}

	public static Vector gatherLargest(Vector v) {
		int lastsize = 0;
		Vector largest = null;
		if (v != null) {
			v = (Vector) v.clone();
			Collections.sort(v, new InverseLengthSorter());
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Vector sub = (Vector) e.nextElement();
				if (sub.size() >= lastsize) {
					largest = add(largest, sub);
					lastsize = sub.size();
				} else {
					break;
				}
			}
		}
		return largest;
	}

	public static Vector gatherByLength(Vector lists, int length) {
		Vector v = null;
		if (lists != null) {
			for (Enumeration e = lists.elements(); e.hasMoreElements();) {
				Vector list = (Vector) e.nextElement();
				if (list.size() == length) {
					v = add(v, list);
				}
			}
		}
		return v;
	}

	public static class LengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			int s1 = ((Vector) o1).size();
			int s2 = ((Vector) o2).size();
			return (s1 < s2 ? 1 : (s1 > s2 ? -1 : 0));
		}
	}

	public static class InverseLengthSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			int s1 = ((Vector) o1).size();
			int s2 = ((Vector) o2).size();
			return (s1 > s2 ? 1 : (s1 < s2 ? -1 : 0));
		}
	}

	public static Vector replaceWith(Vector v, Object o1, Object o2) {
		Vector newv = null;
		if (v != null && o1 != null && o2 != null) {
			newv = new Vector(0);
			for (Object o : v) {
				if (o != null && o.equals(o1)) {
					newv.add(o2);
				} else {
					if (o instanceof Vector) {
						newv.add(replaceWith((Vector) o, o1, o2));
					} else {
						newv.add(o);
					}
				}
			}
		}
		return newv;
	}

	// Before 6/5/2015
	// public static Vector replaceWith(Vector v, Object o1, Object o2) {
	// Vector newv = null;
	// if (v != null && o1 != null && o2 != null) {
	// newv = new Vector(0);
	// for (Object o : v) {
	// if (o != null && o.equals(o1)) {
	// newv.add(o2);
	// } else {
	// newv.add(o);
	// }
	// }
	// }
	// return newv;
	// }

	public static boolean sameLength(Vector v1, Vector v2) {
		if (v1 == v2) {
			return true;
		}
		if (v1 != null && v2 != null) {
			return v1.size() == v2.size();
		}
		return false;
	}

}
