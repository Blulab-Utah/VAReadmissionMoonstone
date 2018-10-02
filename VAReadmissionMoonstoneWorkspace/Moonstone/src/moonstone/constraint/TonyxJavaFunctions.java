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
package moonstone.constraint;

import moonstone.annotation.Annotation;

import tsl.documentanalysis.tokenizer.Token;

public class TonyxJavaFunctions extends
		tsl.expression.term.function.javafunction.JavaFunctions {

	public static TonyxJavaFunctions staticTonyxJavaFunctionsObject = new TonyxJavaFunctions();

	public static void initialize() {
		tsl.expression.term.function.javafunction.JavaFunctions.javaFunctionsObject = staticTonyxJavaFunctionsObject;
	}

	public static Boolean isConjunct(Object o) {
		Annotation annotation = (Annotation) o;
		return annotation.isConjunct();
	}

	public static Boolean isTagged(Object o) {
		Annotation annotation = (Annotation) o;
		return annotation.isTagged();
	}

	public static Boolean isSameSemanticType(Object o1, Object o2) {
		Annotation a1 = (Annotation) o1;
		Annotation a2 = (Annotation) o2;
		if (a1.isSemanticTypeCondition()) {
			return a2.isSemanticTypeCondition();
		}
		if (a1.isSemanticTypeLocation()) {
			return a2.isSemanticTypeLocation();
		}
		return (a1.hasCui() == a2.hasCui());
	}

	// Before 5/17/2013: I will try being stricter: Either both have cuis or
	// neither...
	// public static Boolean isSameSemanticType(Object o1, Object o2) {
	// Annotation a1 = (Annotation) o1;
	// Annotation a2 = (Annotation) o2;
	// if (a1.getCui() != null && a2.getCui() != null) {
	// if (a1.isSemanticTypeCondition()) {
	// return a2.isSemanticTypeCondition();
	// }
	// if (a1.isSemanticTypeLocation()) {
	// return a2.isSemanticTypeLocation();
	// }
	// return false;
	// }
	// return true;
	// }

	public static Boolean isCondition(Object o) {
		Annotation annotation = (Annotation) o;
		return annotation.isSemanticTypeCondition();
	}

	public static Boolean isLocation(Object o) {
		Annotation annotation = (Annotation) o;
		return annotation.isSemanticTypeLocation();
	}

	public static Boolean hasConcept(Object o) {
		Annotation annotation = (Annotation) o;
		return new Boolean(annotation.hasConcept());
	}

	public static Boolean hasCUI(Object o) {
		Annotation annotation = (Annotation) o;
		return annotation.hasCui();
	}

	public static Boolean isInterpreted(Object o) {
		Annotation annotation = (Annotation) o;
		boolean rv = annotation.isInterpreted();
		return rv;
	}

	public static Boolean wordsSupportCUI(Object o) {
		Annotation annotation = (Annotation) o;
		for (Token token : annotation.getTokens()) {
			if (token.getWord() != null
					&& token.getWord().getCUIPhrases() != null) {
				return true;
			}
		}
		return false;
	}

	public static Object lessThan(Object o1, Object o2) {
		if (o1 instanceof Float && o2 instanceof Float) {
			return (Float) o1 < (Float) o2;
		}
		return false;
	}

	public static Object lessThanOrEquals(Object o1, Object o2) {
		if (o1 instanceof Float && o2 instanceof Float) {
			return (Float) o1 <= (Float) o2;
		}
		return false;
	}

	public static Object greaterThan(Object o1, Object o2) {
		if (o1 instanceof Float && o2 instanceof Float) {
			return (Float) o1 > (Float) o2;
		}
		return false;
	}

	public static Object greaterThanOrEquals(Object o1, Object o2) {
		if (o1 instanceof Float && o2 instanceof Float) {
			return (Float) o1 >= (Float) o2;
		}
		return false;
	}

	public static Object argumentsAreEqual(Object o1, Object o2) {
		return o1.equals(o2);
	}

	public static Object argumentsAreUnequal(Object o1, Object o2) {
		return !o1.equals(o2);
	}

	public static Object ifThenElse(Object o1, Object o2, Object o3) {
		return ((Boolean) o1 ? o2 : o3);
	}

	public static Object andTest(Object o1, Object o2) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue());
	}

	public static Object andTest(Object o1, Object o2, Object o3) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue()
				&& arg3.booleanValue());
	}

	public static Object andTest(Object o1, Object o2, Object o3, Object o4) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		Boolean arg4 = (Boolean) o4;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue()
				&& arg3.booleanValue() && arg4.booleanValue());
	}

	public static Object andTest(Object o1, Object o2, Object o3, Object o4,
			Object o5) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		Boolean arg4 = (Boolean) o4;
		Boolean arg5 = (Boolean) o5;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue()
				&& arg3.booleanValue() && arg4.booleanValue()
				&& arg5.booleanValue());
	}

	public static Object andTest(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		Boolean arg4 = (Boolean) o4;
		Boolean arg5 = (Boolean) o5;
		Boolean arg6 = (Boolean) o6;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue()
				&& arg3.booleanValue() && arg4.booleanValue()
				&& arg5.booleanValue() && arg6.booleanValue());
	}

	public static Object andTest(Object o1, Object o2, Object o3, Object o4,
			Object o5, Object o6, Object o7) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		Boolean arg4 = (Boolean) o4;
		Boolean arg5 = (Boolean) o5;
		Boolean arg6 = (Boolean) o6;
		Boolean arg7 = (Boolean) o7;
		return new Boolean(arg1.booleanValue() && arg2.booleanValue()
				&& arg3.booleanValue() && arg4.booleanValue()
				&& arg5.booleanValue() && arg6.booleanValue()
				&& arg7.booleanValue());
	}

	public static Object orTest(Object o1, Object o2) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		return new Boolean(arg1.booleanValue() || arg2.booleanValue());
	}

	public static Object orTest(Object o1, Object o2, Object o3) {
		Boolean arg1 = (Boolean) o1;
		Boolean arg2 = (Boolean) o2;
		Boolean arg3 = (Boolean) o3;
		return new Boolean(arg1.booleanValue() || arg2.booleanValue()
				|| arg3.booleanValue());
	}

	public static Object notTest(Object o) {
		Boolean arg = (Boolean) o;
		return (arg.booleanValue() ? new Boolean("false") : new Boolean("true"));
	}

	static float cfraction = new Float(9).floatValue()
			/ new Float(5).floatValue();

	public static Float convertToFahrenheit(Object o) {
		if (o instanceof Float) {
			Float f = (Float) o;
			if (f < 70) {
				f = (f * cfraction) + 32f;
			}
			return f;
		}
		return null;
	}

	public static Object getAnnotationProperty(Object o1, Object o2) {
		Annotation annotation = (Annotation) o1;
		String attribute = (String) o2;
		Object result = annotation.getProperty(attribute);
		return result;
	}

	public static Object getRule(Object o1) {
		Annotation annotation = (Annotation) o1;
		return annotation.getRule();
	}

}
