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

import java.util.Vector;

import tsl.expression.Expression;

public class JLUtils {

	public static boolean isCons(Object o) {
		return (o != null && o.getClass().equals(Sexp.class));
	}

	public static boolean isAtom(Object o) {
		return (o != null && !isCons(o));
	}

	public static boolean isNonNilAtom(Object o) {
		return (o != null && !isCons(o) && !isNil(o));
	}

	public static boolean isFloat(JLispObject o) {
		return (o != null && o instanceof JavaObject && ((JavaObject) o)
				.isFloat());
	}

	public static boolean isString(Object o) {
		return (o != null && o instanceof JavaObject && ((JavaObject) o)
				.isString());
	}

	public static boolean isSymbol(Object o) {
		return (o instanceof Symbol);
	}

	public static boolean isNil(Object o) {
		return Symbol.NIL.equals(o);
	}

	public static boolean isT(Object o) {
		return Symbol.T.equals(o);
	}

	public static Vector<JLispObject> convertSexpToLVector(Sexp s) {
		return toVector(s, false);
	}

	public static Vector convertSexpToJVector(Sexp s) {
		return toVector(s, true);
	}

	public static Object convertToJObject(Object s) {
		if (s instanceof Symbol) {
			Symbol sym = (Symbol) s;
			if (!JLisp.getJLisp().isPreserveSymbolCase()) {
				return sym.getName().toLowerCase();
			}
		}
		if (s instanceof Sexp) {
			return convertSexpToJVector((Sexp) s);
		}
		return s;
	}

	// THIS REALLY NEEDS TO BE REFACTORED!! EVERY JLISP OBJECT
	// NEEDS A toJava() method...

	public static Object getProperty(Object o, Object key) {
		Object value = null;
		if (JLUtils.isCons(o)) {
			Sexp s = (Sexp) o;
			Object car = s.getCar();
			if (key.equals(car) || key.equals(car.toString())) {
				if (s.getLength() > 2) {
					value = JLUtils.convertSexpToJVector((Sexp) s.getCdr());
				} else if (isCons(s.getSecond())) {
					value = JLUtils.convertSexpToJVector((Sexp) s.getSecond());
				} else {
					value = s.getSecond();
					if (isSymbol(value)) {
						value = ((Symbol) value).toString();
					}
				}
			} else if ((value = getProperty(car, key)) == null) {
				value = getProperty(s.getCdr(), key);
			}
		}
		return value;
	}

	public static Vector toVector(Sexp s, boolean isjava) {
		Vector v = new Vector(0);
		Object so = s;
		if (isCons(so)) {
			while (isCons(so)) {
				Object o = ((Sexp) so).getCar();
				if (isCons(o)) {
					o = toVector((Sexp) o, isjava);
				}
				Object co = o;
				if (isSymbol(co)) {
					Symbol csym = (Symbol) co;
					if (JLisp.getJLisp().isPreserveSymbolCase()) {
						co = csym.getName();
					} else {
						co = csym.getName().toLowerCase();
					}
				} else if (co instanceof JavaObject) {
					JavaObject jo = (JavaObject) co;
					co = jo.getObject();
				}
				v.add(co);
				so = ((Sexp) so).getCdr();
			}
		} else if (isNonNilAtom(so)) {
			Object co = so;
			if (isSymbol(so)) {
				Symbol csym = (Symbol) co;
				if (JLisp.getJLisp().isPreserveSymbolCase()) {
					co = csym.getName();
				} else {
					co = csym.getName().toLowerCase();
				}
			} else if (so instanceof JavaObject) {
				JavaObject jo = (JavaObject) so;
				co = jo.getObject();
			}
			v.add(co);
		}
		return v;
	}

	public static Sexp toSexp(JLispObject parent, Vector v) {
		JLispObject s = Symbol.NIL;
		for (int i = v.size() - 1; i >= 0; i--) {
			Object o = v.elementAt(i);
			JLispObject jlo = null;
			if (o instanceof Vector) {
				jlo = toSexp(parent, (Vector) o);
			} else if (o instanceof Expression) {
				String str = ((Expression) o).toLisp();
				jlo = new JavaObject(str);
			}
			s = Sexp.doCons(jlo, s);
		}
		return (Sexp) s;
	}

	public static Sexp toSexp(JLispObject parent, Object[] array) {
		JLispObject s = Symbol.NIL;
		for (int i = array.length - 1; i >= 0; i--) {
			Object o = array[i];
			JLispObject jlo = null;
			if (o instanceof Object[]) {
				jlo = toSexp(parent, (Object[]) o);
			} else if (o instanceof Expression) {
				String str = ((Expression) o).toLisp();
				jlo = new JavaObject(str);
			}
			s = Sexp.doCons(jlo, s);
		}
		return (Sexp) s;
	}

}
