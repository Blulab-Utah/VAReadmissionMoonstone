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
import java.util.Vector;

import tsl.expression.Expression;
import tsl.information.TSLInformation;
import tsl.utilities.VUtils;

public class TLUtils {

	public static boolean isNil(TLObject to) {
		return Sexp.NullSexp == to || Symbol.NIL == to;
	}

	public static boolean isT(TLObject to) {
		return Sexp.TSexp == to || Symbol.T == to;
	}

	public static boolean isBoolean(TLObject to) {
		return isT(to) || isNil(to);
	}

	public static TLObject getNIL() {
		return Sexp.NullSexp;
	}

	public static TLObject getT() {
		return Sexp.TSexp;
	}

	public static boolean isCons(Object o) {
		return o instanceof Sexp && !(TLUtils.isNil((TLObject) o));
	}

	public static boolean isCons(TLObject o) {
		return o instanceof Sexp && !(TLUtils.isNil(o));
	}

	public static boolean isAtom(TLObject o) {
		return (o != null && !isCons(o));
	}

	public static boolean isAtomList(TLObject o) {
		if (isCons(o)) {
			Sexp sexp = (Sexp) o;
			for (Enumeration<TLObject> e = sexp.elements(); e.hasMoreElements();) {
				TLObject tlo = e.nextElement();
				if (!TLUtils.isAtom(tlo)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isJavaObjectList(TLObject o) {
		if (isCons(o)) {
			Sexp sexp = (Sexp) o;
			for (Enumeration<TLObject> e = sexp.elements(); e.hasMoreElements();) {
				TLObject tlo = e.nextElement();
				if (!(tlo instanceof JavaObject)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isSexpList(TLObject o) {
		if (isCons(o)) {
			Sexp sexp = (Sexp) o;
			for (Enumeration<TLObject> e = sexp.elements(); e.hasMoreElements();) {
				TLObject tlo = e.nextElement();
				if (!TLUtils.isCons(tlo)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isParamValueList(TLObject o) {
		if (isCons(o)) {
			Sexp sexp = (Sexp) o;
			for (Enumeration<TLObject> e = sexp.elements(); e.hasMoreElements();) {
				TLObject to = e.nextElement();
				if (!to.isParameterPair()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isConsList(TLObject o) {
		if (isCons(o)) {
			Sexp sexp = (Sexp) o;
			for (Enumeration<TLObject> e = sexp.elements(); e.hasMoreElements();) {
				TLObject to = e.nextElement();
				if (!to.isCons()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isNonNilAtom(TLObject o) {
		return (o != null && !isCons(o) && !isNil(o));
	}

	public static boolean isConstantLiteral(TLObject o) {
		return (TLUtils.isFloat(o) || TLUtils.isString(o) || TLUtils
				.isTSLInformation(o));
	}

	public static boolean isFloat(TLObject o) {
		return (o != null && o instanceof JavaObject && ((JavaObject) o)
				.isFloat());
	}

	public static boolean isString(TLObject o) {
		return (o != null && o instanceof JavaObject && ((JavaObject) o)
				.isString());
	}

	public static boolean isTSLInformation(TLObject o) {
		return o instanceof TSLInformation;
	}

	public static boolean isSymbol(Object o) {
		return (o instanceof Symbol);
	}

	public static boolean isSymbol(TLObject o) {
		return (o instanceof Symbol);
	}

	public static boolean isDefClass(TLObject o) {
		if (isCons(o)) {
			o = ((Sexp) o).getFirst();
			if (isSymbol(o)) {
				return "defclass".equals(((Symbol) o).getName());
			}
		}
		return false;
	}

	public static boolean isFalse(TLObject o) {
		return isNil(o);
	}

	public static boolean isTrue(TLObject o) {
		return !isNil(o);
	}

	public static Object doAssoc(Object s, Object key) {
		Sexp result = Sexp.doAssoc(s, key);
		if (result != null) {
			return convertToJObject(result.getCdr());
		}
		return null;
	}

	public static Object doAssocValue(Object s, Object key) {
		Sexp result = Sexp.doAssoc(s, key);
		if (result != null) {
			return convertToJObject(result.getSecond());
		}
		return null;
	}

	public static Vector<TLObject> convertSexpToLVector(Sexp s) {
		return toVector(s, false);
	}

	public static TLObject convertLVectorToSexp(Object o) {
		if (o instanceof Vector) {
			Vector v = (Vector) o;
			return Sexp.doCons((TLObject) v.firstElement(),
					convertLVectorToSexp(VUtils.rest(v)));
		}
		return TLUtils.getNIL();
	}

	public static Object[] convertSexpToArray(Sexp s) {
		Vector v = convertSexpToJVector(s);
		return VUtils.vectorToArray(v);
	}

	public static Vector convertSexpToJVector(Sexp s) {
		return toVector(s, true);
	}

	public static Object convertToJObject(Object s) {
		if (s instanceof Symbol) {
			Symbol sym = (Symbol) s;
			if (!TLisp.tLisp.isPreserveSymbolCase()) {
				return sym.getName().toLowerCase();
			}
		}
		if (s instanceof Sexp) {
			return convertSexpToJVector((Sexp) s);
		}
		if (s instanceof JavaObject) {
			JavaObject jo = (JavaObject) s;
			return jo.getObject();
		}
		return s;
	}

	public static Vector toVector(Sexp s, boolean isjava) {
		if (s == null) {
			return null;
		}
		Vector v = new Vector(0);
		TLObject so = s;
		if (isCons(so)) {
			while (isCons(so)) {
				Object o = ((Sexp) so).getCar();
				if (isCons(o)) {
					o = toVector((Sexp) o, isjava);
				}
				Object co = o;
				if (isjava) {
					if (isSymbol(co)) {
						Symbol csym = (Symbol) co;
						co = (TLisp.tLisp.isPreserveSymbolCase() ? csym
								.getName() : csym.getName().toLowerCase());
					} else if (co instanceof JavaObject) {
						JavaObject jo = (JavaObject) co;
						co = jo.getObject();
					}
				}
				v.add(co);
				so = ((Sexp) so).getCdr();
			}
		} else if (isNonNilAtom(so)) {
			Object co = so;
			if (isSymbol(so)) {
				Symbol csym = (Symbol) co;
				if (TLisp.tLisp.isPreserveSymbolCase()) {
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

	public static Sexp toSexp(TLObject parent, Vector v) {
		TLObject s = TLUtils.getNIL();
		for (int i = v.size() - 1; i >= 0; i--) {
			Object o = v.elementAt(i);
			TLObject jlo = null;
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

	public static Sexp toSexp(TLObject parent, Object[] array) {
		TLObject s = TLUtils.getNIL();
		for (int i = array.length - 1; i >= 0; i--) {
			Object o = array[i];
			TLObject jlo = null;
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
