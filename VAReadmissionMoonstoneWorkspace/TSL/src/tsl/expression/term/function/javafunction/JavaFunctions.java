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
package tsl.expression.term.function.javafunction;

import java.util.Vector;

public class JavaFunctions {

	public static JavaFunctions javaFunctionsObject = new JavaFunctions();
	
//	public static void testWeka() {
//		weka.classifiers.bayes.net.BayesNetGenerator();
//	}
	
	public static Float minusOne(Float x) {
		return x - 1;
	}
	
	public static Float minusOne(float x) {
		return new Float(x - 1);
	}
	
	public static Boolean memberp(Object o, Vector v) {
		return v != null && v.contains(o);
	}
	
	public static boolean objectsEqual(Object o1, Object o2) {
		return o1 != null && o1.equals(o2);
	}
	
	public static boolean objectsNotEqual(Object o1, Object o2) {
		return o1 != null && !o1.equals(o2);
	}
	
	public static boolean isInRange(Float low, Float high, Float number) {
		return number >= low && number <= high;
	}
	
	public static Float plus(Float x, Float y) {
		return new Float(x.floatValue() + y.floatValue());
	}
	
	public static float plusOne(float x) {
		return x + 1;
	}

	public static Float minus(Float x, Float y) {
		return new Float(x.floatValue() - y.floatValue());
	}

	public static Object lessThan(Float f1, Float f2) {
		return f1 < f2;
	}

	public static Object lessThanOrEqual(Float f1, Float f2) {
		return f1 <= f2;
	}

	public static Object greaterThan(Float f1, Float f2) {
		return f1 > f2;
	}
	
	public static Object greaterThanOrEqual(Float f1, Float f2) {
		return f1 >= f2;
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
		try {
			Boolean arg1 = (Boolean) o1;
			Boolean arg2 = (Boolean) o2;
			return new Boolean(arg1.booleanValue() && arg2.booleanValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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

}
