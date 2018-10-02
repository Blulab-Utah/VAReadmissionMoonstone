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
import java.util.Hashtable;
import java.util.Vector;

public class ObjectInfoWrapper {
	public Object object = null;
	public int count = 0;
	public float value = 0f;
	boolean visited = false;
	
	public ObjectInfoWrapper() {
	}
	
	public ObjectInfoWrapper(Object object, int count) {
		this.object = object;
		this.count = count;
	}
	
	public ObjectInfoWrapper(Object object, int count, float value) {
		this.object = object;
		this.count = 1;
		this.value = value;
	}
	
	public String toString() {
		return "<" + this.object + ":" + this.count + ":" + this.value + ">";
	}
	
	public static void sortPerValue(Vector wrappers) {
		if (wrappers != null) {
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
		}
	}
	
	public static void sortPerValue(Hashtable hash) {
		for (Enumeration e = hash.elements(); e.hasMoreElements();) {
			Vector wrappers = (Vector) e.nextElement();
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
		}
	}
	
	public static void sortAndTrimPerValue(Hashtable hash, int maxLen) {
		for (Enumeration e = hash.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Vector wrappers = (Vector) hash.get(key);
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
			wrappers = VUtils.subVector(wrappers, 0, maxLen);
			hash.put(key, wrappers);
		}
	}
	
	public static void populateInverseHashAndSortPerValue(Hashtable h1,
			Hashtable h2) {
		for (Enumeration e = h1.keys(); e.hasMoreElements();) {
			Object key1 = e.nextElement();
			Vector<ObjectInfoWrapper> wrappers = (Vector) h1.get(key1);
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
			for (ObjectInfoWrapper wrapper : wrappers) {
				Object key2 = wrapper.object;
				wrapper = new ObjectInfoWrapper(key1, 1, wrapper.value);
				VUtils.pushHashVector(h2, key2, wrapper);
			}
		}
		for (Enumeration e = h2.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Vector wrappers = (Vector) h2.get(key);
			Collections.sort(wrappers, new ObjectInfoWrapper.ValueSorter());
		}
	}
	
	public static Vector getObjects(Vector wrappers) {
		return VUtils.gatherFields(wrappers, "object");
	}
	
	public static Vector getObjects(Hashtable hash, Object key) {
		Vector<ObjectInfoWrapper> wrappers = (Vector<ObjectInfoWrapper>) hash.get(key);
		if (wrappers != null) {
			return VUtils.gatherFields(wrappers, "object");
		}
		return null;
	}
	
	public static ObjectInfoWrapper find(Object object, Vector wrappers) {
		if (wrappers != null) {
			for (Enumeration e = wrappers.elements(); e.hasMoreElements();) {
				ObjectInfoWrapper wrapper = (ObjectInfoWrapper) e.nextElement();
				if (wrapper.object.equals(object)) {
					return wrapper;
				}
			}
		}
		return null;
	}
	
	public static int total(Vector v) {
		int total = 0;
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			ObjectInfoWrapper w = (ObjectInfoWrapper) e.nextElement();
			total += w.count;
		}
		return total;
	}
	
	public static int getCount(Vector v, Object object) {
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			ObjectInfoWrapper w = (ObjectInfoWrapper) e.nextElement();
			if (w.object.equals(object)) {
				return w.count;
			}
		}
		return 0;
	}
	
	public static Vector increment(Vector v, Object object) {
		boolean found = false;
		if (v == null) {
			v = new Vector(0);
		}
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
		return v;
	}
	
	public static Vector increment(Vector v,
			Object object, float value) {
		boolean found = false;
		if (v == null) {
			v = new Vector(0);
		}
		for (Enumeration e = v.elements(); !found && e.hasMoreElements();) {
			ObjectInfoWrapper w = (ObjectInfoWrapper) e.nextElement();
			if (w.object.equals(object)) {
				w.value += value;
				found = true;
			}
		}
		if (!found) {
			ObjectInfoWrapper w = new ObjectInfoWrapper(object, 1, value);
			v.add(w);
		}
		return v;
	}
	
	public static Vector getUpperHalf(Vector wrappers) {
		Vector best = null;
		if (wrappers != null) {
			Collections.sort(wrappers, new ValueSorter());
			ObjectInfoWrapper first = (ObjectInfoWrapper) wrappers
					.firstElement();
			ObjectInfoWrapper last = (ObjectInfoWrapper) wrappers.lastElement();
			float midvalue = (first.value + last.value) / 2f;
			for (Enumeration we = wrappers.elements(); we.hasMoreElements();) {
				ObjectInfoWrapper wrapper = (ObjectInfoWrapper) we
						.nextElement();
				if (wrapper.value >= midvalue) {
					best = VUtils.add(best, wrapper);
				} else {
					break;
				}
			}
		}
		return best;
	}
	
	public static class ValueSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ObjectInfoWrapper w1 = (ObjectInfoWrapper) o1;
			ObjectInfoWrapper w2 = (ObjectInfoWrapper) o2;
			if (w1.value > w2.value) {
				return -1;
			}
			if (w1.value < w2.value) {
				return 1;
			}
			return 0;
		}
	}
	
	public static class CountSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			ObjectInfoWrapper w1 = (ObjectInfoWrapper) o1;
			ObjectInfoWrapper w2 = (ObjectInfoWrapper) o2;
			if (w1.count > w2.count) {
				return -1;
			}
			if (w1.count < w2.count) {
				return 1;
			}
			return 0;
		}
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public static void setVisited(Vector<ObjectInfoWrapper> wrappers, boolean visited) {
		if (wrappers != null) {
			for (ObjectInfoWrapper wrapper : wrappers) {
				wrapper.setVisited(visited);
			}
		}
	}
	
	
}