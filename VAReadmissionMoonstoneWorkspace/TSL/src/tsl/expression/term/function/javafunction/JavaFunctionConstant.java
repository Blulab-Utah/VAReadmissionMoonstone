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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import tsl.expression.term.function.FunctionConstant;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.VUtils;

public class JavaFunctionConstant extends FunctionConstant {
	private Method method = null;
	private Class source = null;
	private Class[] parameterTypes = null;

	public JavaFunctionConstant(Method method, Class source) {
		super(method.getName());
		this.method = method;
		this.source = source;
		this.parameterTypes = method.getParameterTypes();
	}
	
	// 11/23/2013
	public static JavaFunctionConstant createJavaFunctionConstant(String fname) {
		JavaFunctionConstant jfc = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		String mname = getMethodName(fname);
		FunctionConstant fc = kb.getNameSpace().getFunctionConstant(
				mname);
		if (fc == null) {
			Method m = getMethod(fname);
			if (m != null) {
				jfc = new JavaFunctionConstant(m, null);
			}
		} else if (fc instanceof JavaFunctionConstant) {
			jfc = (JavaFunctionConstant) fc;
		}
		return jfc;
	}

	// Called from load-static-methods()
	public static Vector<JavaFunctionConstant> loadStaticMethods(
			String className) {
		Vector<JavaFunctionConstant> constants = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		try {
			Class c = Class.forName(className);
			Method[] allMethods = c.getDeclaredMethods();
			for (int i = 0; i < allMethods.length; i++) {
				Method method = allMethods[i];
				JavaFunctionConstant jfc = (JavaFunctionConstant) kb
						.getNameSpace().getFunctionConstant(method.getName());
				if (jfc == null) {
					jfc = new JavaFunctionConstant(method, c);
					constants = VUtils.add(constants, jfc);
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to extract JavaRelationConstants from "
					+ className);
			constants = null;
		}
		return constants;
	}
	
	public Object apply(Object[] arguments) {
		Object result = null;
		try {
			Class source = arguments[0].getClass();
			result = this.method.invoke(source, arguments);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
//	public Object apply(Object[] arguments) {
//		Object result = null;
//		try {
//			Class source = arguments[0].getClass();
//			result = this.method.invoke(source, arguments);
//		} catch (Exception e) {
//			System.out.println("JavaFunctionConstant application error: "
//					+ e.getMessage());
//		}
//		return result;
//	}

	public Method getMethod() {
		return this.method;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}
	
	public Class getParameterType(int index) {
		if (index >= 0 && index < this.parameterTypes.length) {
			return this.parameterTypes[index];
		}
		return null;
	}

}