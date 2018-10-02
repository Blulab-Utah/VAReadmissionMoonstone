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
package tsl.tsllisp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Sexp extends TLObject {
	TLObject car = null;
	TLObject cdr = null;
	int length = -1;
	Object enumObject = null;
	public static Sexp NullSexp = new Sexp(Symbol.NIL, Symbol.NIL);
	public static Sexp TSexp = new Sexp(Symbol.T, Symbol.T);

	public Sexp() {
	}

	public Sexp(TLObject car, TLObject cdr) {
		this.car = car;
		this.cdr = cdr;
	}

	public TLObject getCar() {
		TLisp.setLastReferenceObject(this);
		return this.car;
	}

	public TLObject getCdr() {
		TLisp.setLastReferenceObject(this);
		return this.cdr;
	}

	public void setCar(TLObject o) {
		this.car = o;
	}

	public void setCdr(TLObject o) {
		this.cdr = o;
	}

	public TLObject getFirst() {
		TLisp.setLastReferenceObject(this);
		return this.car;
	}

	public TLObject getSecond() {
		return getNth(1);
	}

	public TLObject getThird() {
		return getNth(2);
	}

	public TLObject getFourth() {
		return getNth(3);
	}

	public TLObject getFifth() {
		return getNth(4);
	}

	public TLObject getSixth() {
		return getNth(5);
	}

	public TLObject getSeventh() {
		return getNth(6);
	}

	public TLObject getEighth() {
		return getNth(7);
	}

	public TLObject getNth(int num) {
		Sexp s = this;
		TLObject o = null;
		for (int i = 0; i < num; i++) {
			o = s.getCdr();
			if (!TLUtils.isCons(o)) {
				return TLUtils.getNIL();
			}
			s = (Sexp) o;
		}
		if (s != null) {
			TLisp.setLastReferenceObject(s);
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
			if (!this.isNil()) {
				for (Enumeration e = this.elements(); e.hasMoreElements();) {
					length++;
					e.nextElement();
				}
			}
		}
		return length;
	}

	public boolean countIsEvenP() {
		int length = this.getLength();
		return length % 2 == 0;
	}

	public String toNewlinedString() {
		return toNewlinedString(0);
	}
	
	public String toString() {
		return toNewlinedString(-1);
	}

	// 3/4/2016- Updated to no newline
	public String toNewlinedString(int depth) {
		StringBuffer sb = new StringBuffer();
		boolean withNewline = (depth >= 0);
		if (withNewline) {
			for (int i = 0; i < depth; i++) {
				sb.append("  ");
			}
		}
		sb.append("(");
		TLObject so = this;
		while (TLUtils.isCons(so)) {
			TLObject c = ((Sexp) so).getCar();
			if (c instanceof JavaObject) {
				JavaObject jo = (JavaObject) c;
				if (jo.getObject() instanceof String) {
					sb.append("\"" + c + "\"");
				} else {
					sb.append(jo.getObject());
				}
			} else if (c instanceof Sexp) {
				if (withNewline) {
					sb.append("\n");
				}
				Sexp csexp = (Sexp) c;
				sb.append(csexp.toNewlinedString(withNewline ? depth + 1 : -1));
			} else {
				sb.append(c.toString());
			}
			so = ((Sexp) so).getCdr();
			if (TLUtils.isCons(so)) {
				sb.append(" ");
			}
		}
		if (TLUtils.isAtom(so) && !TLUtils.isNil(so)) {
			sb.append(" . " + so);
		}
		sb.append(")");
		return sb.toString();
	}

	
//	public String toString() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("(");
//		TLObject so = this;
//		while (TLUtils.isCons(so)) {
//			Object c = ((Sexp) so).getCar();
//			// Clumsy -- since Lisp objects can include any regular Java
//			// classes, I use the Java class' toString() which may not
//			// give the correct result...
//			if (c instanceof String) {
//				sb.append("\"" + c + "\"");
//			} else {
//				sb.append(c);
//			}
//			so = ((Sexp) so).getCdr();
//			if (TLUtils.isCons(so)) {
//				sb.append(" ");
//			}
//		}
//		if (TLUtils.isAtom(so) && !TLUtils.isNil(so)) {
//			sb.append(" . " + so);
//		}
//		sb.append(")");
//		return sb.toString();
//	}

	public static Sexp doCons(TLObject car, TLObject cdr) {
		Sexp s = new Sexp();
		s.car = car;
		s.cdr = cdr;
		return s;
	}

	public static Sexp doList(TLObject o) {
		return doCons(o, TLUtils.getNIL());
	}

	// PROBLEM: This recreates the first sexp, but doesn't alter the second one.
	// Altering the new expression will also alter copies referenced elsewhere.
	public static TLObject doAppend(TLObject o1, TLObject o2) {
		if (TLUtils.isNil(o1)) {
			return o2;
		}
		if (TLUtils.isNil(o2)) {
			return o1;
		}
		Sexp s1 = (Sexp) o1;
		return Sexp.doCons(s1.getCar(), doAppend(s1.getCdr(), o2));
	}

	public static TLObject doCopy(TLObject to) {
		if (to.isAtom()) {
			return to;
		}
		Sexp sexp = (Sexp) to;
		return Sexp.doCons(sexp.getFirst(), Sexp.doCopy(sexp.getCdr()));
	}

	public boolean equals(Object o) {
		if (o instanceof Sexp) {
			if (this.isBoolean()) {
				return this == o;
			}
			if (this == o) {
				return true;
			}
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

	// 6/28/2013 -- Not tested...
	public static TLObject doAssocValue(Object s, Object key) {
		Sexp result = doAssoc(s, key);
		if (result != null) {
			return result.getSecond();
		}
		return null;
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

	public void extractProperties(Hashtable hash) {
		Vector v = TLUtils.convertSexpToJVector(this);
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
