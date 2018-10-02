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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SetUtils {

	public static void main(String[] args) {
		Vector v1 = VUtils.arrayToVector(new Object[] { "a1", "a2" });
		Vector v2 = VUtils.arrayToVector(new Object[] { "b" });
		Vector v3 = VUtils.arrayToVector(new Object[] { "c1", "c2" });
		Vector v4 = VUtils.arrayToVector(new Object[] { "d1", "d2", "d3" });
		Vector all = VUtils.arrayToVector(new Object[] { v1, v2, v3, v4 });
		
		System.out.println("Cartesian Product:");
		Vector rv = cartesianProduct(all);
		for (Enumeration e = rv.elements(); e.hasMoreElements();) {
			System.out.println(e.nextElement());
		}
		
		System.out.println("Permutations:");
		for (Enumeration<Vector> e = rv.elements(); e.hasMoreElements();) {
			Vector v = e.nextElement();
			Vector pv = permutations(v);
			for (Enumeration<Vector> pe = pv.elements(); pe.hasMoreElements();) {
				Vector perm = pe.nextElement();
				System.out.println(perm);
			}
		}
		
		System.out.println("Ordered Powerset:");
		rv = orderedPowerset(all);
		for (Enumeration e = rv.elements(); e.hasMoreElements();) {
			System.out.println(e.nextElement());
		}

	}

	public static boolean same(Vector v1, Vector v2) {
		if (v1 == null || v2 == null || v1.size() != v2.size()) {
			return false;
		}
		for (Object o : v1) {
			if (!v2.contains(o)) {
				return false;
			}
		}
		return true;
	}

	public static Vector difference(Vector v1, Vector v2) {
		Vector rv = null;
		if (v2 == null) {
			rv = v1;
		} else if (v1 != null) {
			for (Enumeration e = v1.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (!v2.contains(o)) {
					rv = VUtils.add(rv, o);
				}
			}
		}
		return rv;
	}

	// 12/20/2008 -- Same as difference, but consider all the nodes that are in
	// one but not in the other.
	// Using hashtable -- this won't work unless the objects are different or
	// have useful hash function.
	public static Vector disjunction(Vector v1, Vector v2) {
		Vector rv = null;
		if (v1 == null) {
			rv = v2;
		} else if (v2 == null) {
			rv = v1;
		} else {
			Hashtable h1 = HUtils.toHash(v1);
			Hashtable h2 = HUtils.toHash(v2);
			for (Enumeration e = h1.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (h2.get(o) == null) {
					rv = VUtils.add(rv, o);
				}
			}
			for (Enumeration e = h2.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (h1.get(o) == null) {
					rv = VUtils.add(rv, o);
				}
			}
		}
		return rv;
	}

	public static Vector intersection(Vector v1, Vector v2) {
		Vector rv = null;
		if (v1 != null && v2 != null) {
			for (Enumeration e = v1.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (v2.contains(o)) {
					rv = VUtils.add(rv, o);
				}
			}
		}
		return rv;
	}

	public static boolean intersects(Vector v1, Vector v2) {
		if (v1 != null && v2 != null) {
			for (Enumeration e = v1.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (v2.contains(o)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Vector vectorIntersection(Vector v) {
		Vector intersection = null;
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Vector subv = (Vector) e.nextElement();
				intersection = (intersection == null ? subv : SetUtils
						.intersection(intersection, subv));
				if (intersection == null) {
					return null;
				}
			}
		}
		return intersection;
	}

	public static Vector union(Vector v1, Vector v2) {
		Vector rv = null;
		if (v1 == null) {
			rv = v2;
		} else if (v2 == null) {
			rv = v1;
		} else {
			rv = new Vector(0);
			for (Enumeration e = v1.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				rv.add(o);
			}
			for (Enumeration e = v2.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				if (!rv.contains(o)) {
					rv = VUtils.add(rv, o);
				}
			}
		}
		return rv;
	}

	public static Vector cartesianProduct(Vector v) {
		Vector rv = null;
		if (v != null) {
			if (v.size() == 1) {
				rv = VUtils.listifyVector((Vector) v.firstElement());
			} else {
				rv = new Vector(0);
				Vector rest = cartesianProduct(VUtils.rest(v));
				Vector first = (Vector) v.firstElement();
				for (Enumeration e1 = first.elements(); e1.hasMoreElements();) {
					Object o1 = e1.nextElement();
					for (Enumeration e2 = rest.elements(); e2.hasMoreElements();) {
						Vector v2 = VUtils.clone((Vector) e2.nextElement());
						v2.insertElementAt(o1, 0);
						rv.add(v2);
					}
				}
			}
		}
		return rv;
	}

	public static Vector orderedPowerset(Vector v, int min, int max) {
		Vector sets = orderedPowerset(v, max);
		if (sets != null) {
			for (Enumeration e = new Vector(sets).elements(); e
					.hasMoreElements();) {
				Vector set = (Vector) e.nextElement();
				if (set.size() < min) {
					sets.remove(set);
				}
			}
		}
		return sets;
	}

	public static Vector orderedPowerset(Vector v) {
		return orderedPowerset(v, 10);
	}

	public static Vector orderedPowerset(Vector v, int maxLength) {
		Vector rv = null;
		Vector op = null;
		if (v != null) {
			rv = new Vector(0);
			rv.add(VUtils.listify(v.firstElement()));
			Vector restv = VUtils.rest(v);
			if ((op = orderedPowerset(restv, maxLength)) != null) {
				for (Enumeration e = op.elements(); e.hasMoreElements();) {
					Vector opv = (Vector) e.nextElement();
					rv.add(opv);
					if (opv.size() < maxLength) {
						opv = new Vector(opv);
						opv.insertElementAt(v.firstElement(), 0);
						rv.add(opv);
					}
				}
			}
		}
		return rv;
	}

	// 9/7/2011
	public static Vector<Vector> allPermutations(Vector<Vector> v) {
		Vector<Vector> results = null;
		if (v != null) {
			for (Object o : v) {

			}
		}
		return results;
	}

	public static boolean isStrictSubset(Vector v1, Vector v2) {
		return isSubset(v1, v2) && v1.size() < v2.size();
	}

	// Why can't a subset be the same as a superset?
	public static boolean isSubset(Vector v1, Vector v2) {
		if (v1 == null || v2 == null || v1.equals(v2) || v1.size() >= v2.size()) {
			return false;
		}
		for (Object o1 : v1) {
			if (!v2.contains(o1)) {
				return false;
			}
		}
		return true;
	}

	public static Vector findSuperset(Vector sub, Vector v) {
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Vector sup = (Vector) e.nextElement();
				if (SetUtils.isSubset(sub, sup)) {
					return sup;
				}
			}
		}
		return null;
	}

	public static Vector eliminateSubsets(Vector sets) {
		Vector supersonly = null;
		if (sets != null) {
			for (Enumeration e = sets.elements(); e.hasMoreElements();) {
				Vector set = (Vector) e.nextElement();
				if (findSuperset(set, sets) == null) {
					supersonly = VUtils.add(supersonly, set);
				}
			}
		}
		return supersonly;
	}

	public static Vector allPairs(Vector v1, Vector v2) {
		Vector pairs = null;
		if (v1 != null && v2 != null) {
			for (Enumeration e1 = v1.elements(); e1.hasMoreElements();) {
				Object o1 = e1.nextElement();
				for (Enumeration e2 = v2.elements(); e2.hasMoreElements();) {
					Object o2 = e2.nextElement();
					Vector pair = new Vector(0);
					pair.add(o1);
					pair.add(o2);
					pairs = VUtils.add(pairs, pair);
				}
			}
		}
		return pairs;
	}

	public static Vector allPairs(Vector items) {
		Vector pairs = null;
		if (items != null && items.size() >= 2) {
			pairs = new Vector(0);
			for (int i = 0; i < items.size(); i++) {
				Object o1 = items.elementAt(i);
				for (int j = i + 1; j < items.size(); j++) {
					Object o2 = items.elementAt(j);
					Vector pair = new Vector(0);
					pair.add(o1);
					pair.add(o2);
					pairs.add(pair);
				}
			}
		}
		return pairs;
	}
	
	public static Vector<Vector> totalPermutations(Vector v) {
		Vector<Vector> all = null;
		if (v != null && v.firstElement() instanceof Vector) {
			Vector rv = cartesianProduct(v);
			for (Enumeration<Vector> ce = rv.elements(); ce.hasMoreElements();) {
				Vector cv = ce.nextElement();
				Vector pv = permutations(cv);
				for (Enumeration<Vector> pe = pv.elements(); pe
						.hasMoreElements();) {
					Vector perm = pe.nextElement();
					all = VUtils.add(all, perm);
				}
			}
		}
		return all;
	}

	public static Vector<Vector> permutations(Vector v) {
		if (v == null) {
			return null;
		}
		if (v.size() == 1) {
			return VUtils.listify(v);
		}
		Vector<Vector> results = null;
		for (Object o : v) {
			Vector restv = new Vector(v);
			restv.remove(o);
			Vector<Vector> prv = permutations(restv);
			for (Vector rv : prv) {
				rv.insertElementAt(o, 0);
				results = VUtils.add(results, rv);
			}
		}
		return results;
	}

}
