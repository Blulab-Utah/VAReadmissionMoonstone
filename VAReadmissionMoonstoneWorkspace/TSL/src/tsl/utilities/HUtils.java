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

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class HUtils {
	public static HUtils staticHashUtils = new HUtils();

	HUtils() {

	}
	
	// 9/26/2015
	public static Hashtable clone(Hashtable h) {
		Hashtable newhash = null;
		if (h != null) {
			newhash = new Hashtable();
			for (Enumeration e = h.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Object value = h.get(key);
				if (value instanceof Hashtable) {
					value = HUtils.clone((Hashtable) value);
				}
				if (value != null) {
					newhash.put(key, value);
				}
			}
		}
		return newhash;
	}
	
	// 11/30/2014:  Create nested Hashmaps from a String[][] list.
	public static HashMap createTypePropertyValueMap(String[][] str) {
		HashMap map = new HashMap();
		for (String[] sstr : str) {
			String type = sstr[0];
			HashMap tmap = (HashMap) map.get(type);
			if (tmap == null) {
				tmap = new HashMap();
				map.put(type, tmap);
			}
			if (sstr.length > 1) {
				String property = sstr[1];
				for (int i = 2; i < sstr.length; i++) {
					VUtils.pushHashVector(tmap, property, sstr[i]);
				}
			}
		}
		return map;
	}
	
	public static HashMap createMap(Object[] keys, Object[] values) {
		HashMap map = null;
		if (keys != null && values != null && keys.length == values.length) {
			map = new HashMap();
			for (int i = 0; i < keys.length; i++) {
				map.put(keys[i], values[i]);
			}
		}
		return map;
	}

	public static Vector getKeys(Hashtable hash) {
		Vector v = new Vector(0);
		for (Enumeration e = hash.keys(); e.hasMoreElements();) {
			v.add(e.nextElement());
		}
		return (!v.isEmpty() ? v : null);
	}
	
	public static Vector getKeys(Map map) {
		Vector v = new Vector(0);
		for (Iterator li = map.keySet().iterator(); li.hasNext();) {
			v.add(li.next());
		}
		return (!v.isEmpty() ? v : null);
	}
	
	public static Vector getElements(Hashtable hash) {
		Vector v = new Vector(0);
		for (Enumeration e = hash.elements(); e.hasMoreElements();) {
			v.add(e.nextElement());
		}
		return (!v.isEmpty() ? v : null);
	}
	
	public static Vector getElements(Map map) {
		Vector v = new Vector(0);
		for (Iterator li = map.values().iterator(); li.hasNext();) {
			v.add(li.next());
		}
		return (!v.isEmpty() ? v : null);
	}
	
	public static Hashtable toHash(Vector v) {
		Hashtable hash = new Hashtable();
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				hash.put(o, o);
			}
		}
		return hash;
	}

	public static int incrementCount(Hashtable h, Object key) {
		Integer rv;
		if ((rv = (Integer) h.get(key)) != null) {
			rv = new Integer(rv.intValue() + 1);
			h.put(key, rv);
		} else {
			rv = new Integer(1);
			h.put(key, rv);
		}
		return rv.intValue();
	}
	
	public static Vector getCountWrappers(Hashtable h) {
		Vector v = new Vector(0);
		for (Enumeration e = h.keys(); e.hasMoreElements();) {
			Object o = e.nextElement();
			Integer count = (Integer) h.get(o);
			ObjectInfoWrapper w = new ObjectInfoWrapper(o, count.intValue());
			v.add(w);
		}
		Collections.sort(v, new ObjectInfoWrapper.CountSorter());
		return v;
	}
	
	public static int setCount(Hashtable h, Object key, int count) {
		h.put(key, new Integer(count));
		return count;
	}
	
	public static int getCount(Hashtable h, Object key) {
		Integer rv = (Integer) h.get(key);
		return (rv != null ? rv.intValue() : 0);
	}
	
	public static int getCount(Hashtable h, Object key1, Object key2) {
		Vector<ObjectInfoWrapper> wrappers = (Vector<ObjectInfoWrapper>) h.get(key1);
		if (wrappers != null) {
			return ObjectInfoWrapper.getCount(wrappers, key2);
		}
		return 0;
	}

	public static void print(Hashtable h, String msg) {
		System.out.println("\n" + msg + ":");
		for (Enumeration e = h.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = h.get(key);
			System.out.println("\tKey=" + key + ", Value=" + value);
		}
		System.out.flush();
	}
	
	public static Object getHighestFrequencyObject(Hashtable hash, Object key) {
		Vector<ObjectInfoWrapper> v = (Vector) hash.get(key);
		if (v != null) {
			Collections.sort(v, new FrequencySorter());
			return v.firstElement().object;
		}
		return null;
	}

	public static Object getHighestFrequencyObject(Hashtable hash) {
		int highestCount = 0;
		Object highestObject = null;
		if (!hash.isEmpty()) {
			for (Enumeration e = hash.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Integer value = (Integer) hash.get(key);
				if (value > highestCount) {
					highestCount = value.intValue();
					highestObject = key;
				}
			}
		}
		return highestObject;
	}

	public static void incrementHashObjectInfoWrapper(Hashtable hash,
			Object key, Object object) {
		Vector v = null;
		boolean found = false;
		if ((v = (Vector) hash.get(key)) != null) {
			for (Enumeration e = v.elements(); !found && e.hasMoreElements();) {
				ObjectInfoWrapper w = (ObjectInfoWrapper) e.nextElement();
				if (w.object.equals(object)) {
					w.count++;
					found = true;
				}
			}
			if (!found) {
				ObjectInfoWrapper w = new ObjectInfoWrapper(object, 1);
				v.add(w);
			}
		} else {
			ObjectInfoWrapper w = new ObjectInfoWrapper(object, 1);
			hash.put(key, VUtils.listify(w));
		}
	}

	public static void incrementHashObjectInfoWrapper(Hashtable hash,
			Object key, Object object, float value) {
		Vector v = null;
		boolean found = false;
		if ((v = (Vector) hash.get(key)) != null) {
			for (Enumeration e = v.elements(); !found && e.hasMoreElements();) {
				ObjectInfoWrapper w = (ObjectInfoWrapper) e.nextElement();
				if (w.object.equals(object)) {
					w.count++;
					w.value += value;
					found = true;
				}
			}
			if (!found) {
				ObjectInfoWrapper w = new ObjectInfoWrapper(object, 1, value);
				v.add(w);
			}
		} else {
			ObjectInfoWrapper w = new ObjectInfoWrapper(object, 1, value);
			hash.put(key, VUtils.listify(w));
		}
	}
	
	public static class FrequencySorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ObjectInfoWrapper w1 = (ObjectInfoWrapper) o1;
			ObjectInfoWrapper w2 = (ObjectInfoWrapper) o2;
			if (w1.count > w2.count) {
				return -1;
			}
			if (w2.count > w1.count) {
				return 1;
			}
			return 0;
		}
	}

	

}
