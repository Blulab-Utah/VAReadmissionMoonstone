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
package tsl.jlisp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Sexp extends JLispObject {
	JLispObject car = null;
	JLispObject cdr = null;
	int length = -1;
	Object enumObject = null;

	public Sexp() {
	}

	public Sexp(JLispObject car, JLispObject cdr) {
		this.car = car;
		this.cdr = cdr;
	}

	public JLispObject getCar() {
		JLisp.setLastReferenceObject(this);
		return this.car;
	}

	public JLispObject getCdr() {
		JLisp.setLastReferenceObject(this);
		return this.cdr;
	}

	public void setCar(JLispObject o) {
		this.car = o;
	}

	public void setCdr(JLispObject o) {
		this.cdr = o;
	}

	public JLispObject getFirst() {
		JLisp.setLastReferenceObject(this);
		return this.car;
	}

	public JLispObject getSecond() {
		return getNth(1);
	}

	public JLispObject getThird() {
		return getNth(2);
	}

	public JLispObject getFourth() {
		return getNth(3);
	}

	public JLispObject getFifth() {
		return getNth(4);
	}

	public JLispObject getSixth() {
		return getNth(5);
	}

	public JLispObject getSeventh() {
		return getNth(6);
	}

	public JLispObject getEighth() {
		return getNth(7);
	}

	public JLispObject getNth(int num) {
		Sexp s = this;
		JLispObject o = null;
		for (int i = 0; i < num; i++) {
			o = s.getCdr();
			if (!JLUtils.isCons(o)) {
				return Symbol.NIL;
			}
			s = (Sexp) o;
		}
		if (s != null) {
			JLisp.setLastReferenceObject(s);
			return s.getCar();
		}
		return null;
	}

	public Object getNthCdr(int num) {
		Sexp s = this;
		for (int i = 0; i < num; i++) {
			s = (Sexp) s.getCdr();
		}
		return s;
	}

	// 10/1/2007: If I make destructive changes, length will be invalid.
	// Should I count each time?
	public int getLength() {
		if (length < 0) {
			length = 0;
			for (Enumeration e = this.elements(); e.hasMoreElements();) {
				length++;
				e.nextElement();
			}
		}
		return length;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		Object so = this;
		while (JLUtils.isCons(so)) {
			Object c = ((Sexp) so).getCar();
			// Clumsy -- since Lisp objects can include any regular Java
			// classes, I use the Java class' toString() which may not
			// give the correct result...
			if (c.getClass().equals(String.class)) {
				sb.append("\"" + c + "\"");
			} else {
				sb.append(c);
			}
			so = ((Sexp) so).getCdr();
			if (JLUtils.isCons(so)) {
				sb.append(" ");
			}
		}
		if (JLUtils.isAtom(so) && !JLUtils.isNil(so)) {
			sb.append(" . " + so);
		}
		sb.append(")");
		return sb.toString();
	}

	public static Sexp doCons(JLispObject car, JLispObject cdr) {
		Sexp s = new Sexp();
		s.car = car;
		s.cdr = cdr;
		return s;
	}

	public static Sexp doList(JLispObject o) {
		return doCons(o, Symbol.NIL);
	}

	// Note: This recreates the first sexp, but doesn't alter the second one. Is
	// this the right approach??
	public static JLispObject doAppend(JLispObject o1,
			JLispObject o2) {
		if (JLUtils.isNil(o1)) {
			return o2;
		} else if (JLUtils.isNil(o2)) {
			return o1;
		} else {
			Sexp s1 = (Sexp) o1;
			return Sexp.doCons(s1.getCar(), doAppend(s1.getCdr(), o2));
		}
	}

	public boolean equals(Object o) {
		if (Sexp.class.equals(o.getClass())) {
			Sexp s = (Sexp) o;
			return (this.car.equals(s.car) && this.cdr.equals(s.cdr));
		}
		return false;
	}

	public int hashcode() {
		return this.car.hashCode() | this.cdr.hashCode();
	}

	public Enumeration elements() {
		return new SexpEnum(this);
	}

	public static Sexp doAssoc(Object s, Object key) {
		if (!(s instanceof Sexp)) {
			return null;
		}
		Sexp sexp = (Sexp) s;
		Object result = null;
		Object car = sexp.getCar();
		if (key.equals(sexp.getCar())) {
			result = sexp;
		}
		if (key instanceof String && car instanceof Symbol) {
			String kname = key.toString().toLowerCase();
			String cname = car.toString().toLowerCase();
			if (kname.equals(cname)) {
				result = sexp;
			}
		}
		if (result == null) {
			result = Sexp.doAssoc(sexp.getCar(), key);
		}
		if (result == null) {
			result = Sexp.doAssoc(sexp.getCdr(), key);
		}
		return (Sexp) result;
	}

	// 9/24/2007: Create key / value hash entries for each nested
	// predicate. (Note: Only stores atomic values, not vector values.)
	public void extractProperties(Hashtable hash) {
		Vector v = JLUtils.convertSexpToJVector(this);
		for (Enumeration e = v.elements(); e.hasMoreElements();) {
			Object o = e.nextElement();
			if (Vector.class.equals(o.getClass())) {
				Vector keyv = (Vector) e.nextElement();
				if (keyv.size() > 1) {
					Object key = keyv.elementAt(0);
					Object value = keyv.elementAt(1);
					hash.put(key, value);
				}
			}
		}
	}

}
