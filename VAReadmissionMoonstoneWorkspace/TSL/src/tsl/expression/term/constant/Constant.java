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
package tsl.expression.term.constant;

import java.lang.reflect.Method;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.term.Term;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.type.SyntacticTypeConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.StrUtils;

public abstract class Constant extends Term {

	private String userComment = null;
	protected String formalName = null;

	public Constant() {
		super();
	}

	public Constant(String name) {
		super();
		this.setName(name);
		this.getKnowledgeBase().getNameSpace().addConstant(this);
	}

	public Constant(String name, TypeConstant type) {
		super();
		this.setName(name);
		this.setType(type);
		this.getKnowledgeBase().getNameSpace().addConstant(this);
	}

	public static Constant createConstant(Vector v) {
		Constant c = null;
		if ((c = StringConstant.createStringConstant(v)) != null
				|| (c = TypeConstant.createTypeConstant(v)) != null
				|| (c = RelationConstant.createRelationConstant(v)) != null
				|| (c = FunctionConstant.createFunctionConstant(v)) != null
				|| (c = PropertyConstant.createPropertyConstant(v)) != null) {
			return c;
		}
		return null;
	}

	public String getUserComment() {
		return userComment;
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	public void resolve() {

	}

	public Expression copy() {
		return this;
	}

	public static boolean isQualifiedMethodPathname(String name) {
		return name != null && name.contains(".") && name.contains(":");
	}

	// 11/23/2013: For use in JavaRelationConstant and JavaFunctionConstant

	// E.g. "workbench.tsl.AnnotationTerm:evAnnotationWithAttributeValue"
	// => "workbench.tsl.AnnotationTerm"
	public static String getMethodSourceClassName(String cname) {
		int index = cname.lastIndexOf(':');
		if (index > 0) {
			return cname.substring(0, index);
		}
		return cname;
	}

	// E.g. "workbench.tsl.AnnotationTerm:evAnnotationWithAttributeValue"
	// => "evAnnotationWithAttributeValue"
	public static String getMethodName(String cname) {
		int index = cname.lastIndexOf(':');
		if (index > 0) {
			return cname.substring(index + 1);
		}
		return cname;
	}

	public static Method getMethod(String name) {
		if (isQualifiedMethodPathname(name)) {
			return getMethodUsingQualifiedPathname(name);
		}
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		String[] paths = ke.getJavaPathnames();
		if (paths != null) {
			for (String path : paths) {
				String pathname = path + ":" + name;
				Method m = getMethodUsingQualifiedPathname(pathname);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}

	// e.g. getMethod("a.b.c:methodname");
	// Finds a method without having to pre-specify parameter types.
	public static Method getMethodUsingQualifiedPathname(String fullname) {
		try {
			String cname = getMethodSourceClassName(fullname);
			String mname = getMethodName(fullname);
			if (cname != null && mname != null) {
				Class c = Class.forName(cname);
				Method[] methods = c.getMethods();
				if (methods != null) {
					for (Method m : methods) {
						if (mname.equals(m.getName())) {
							return m;
						}
					}
				}
			}
		} catch (Exception e) {
			// System.out.println("Java function/relation class not found: " +
			// fullname);
		}
		return null;
	}

	public static String generateFormalNameCore(String str) {
		StringBuffer sb = new StringBuffer();
		String tname = null;
		if (str != null && str.length() >= 2) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (Character.isLetter(c) || c == '_' || c == '-' || c == ' ' || c == ':' || c == '.' || c == '<'
						|| c == '>') {
					c = Character.toUpperCase(c);
					sb.append(c);
				}
			}
			tname = sb.toString();
		}
		return tname;
	}

	public String getFormalName() {
		if (this.formalName == null && this.name != null) {
			this.formalName = generateFormalNameCore(this.name);
		}
		return this.formalName;
	}

	public static boolean isConstantFormalName(Object token) {
		if (token instanceof String && ((String) token).length() > 2) {
			String name = (String) token;
			char start = name.charAt(0);
			char end = name.charAt(name.length() - 1);
			if ((start == '<' && end == '>') || (start == ':' && end == ':') || (start == '#' && end == '#')) {
				return true;
			}
		}
		return false;
	}

	public static Object extractConstant(KnowledgeBase kb, Object token) {
		Object o = token;
		if (Constant.isConstantFormalName(token)) {
			String tname = (String) token;
			Constant c = kb.getConstant(tname);
			if (c == null) {
				if (TypeConstant.isTypeConstantFormalName(tname)) {
					c = TypeConstant.createTypeConstant(tname);
				} else if (StringConstant.isStringConstantFormalName(tname)) {
					TypeConstant root = kb.getOntology().getRootType();
					c = new StringConstant(tname, root, false);
				} else if (SyntacticTypeConstant.isSyntacticTypeConstantFormalName(tname)) {
					c = SyntacticTypeConstant.createTypeConstant(tname);
				}
			}
			if (c != null) {
				o = c;
			} else {
				System.out.println("Undefined constant: " + token);
			}
		}
		return o;
	}

	public String getTuffyString() {
		String cname = StrUtils.removeNonAlphaDigitUnderlineCharacters(this.getName());
		return cname;
	}

	public static String getTuffyString(String str) {
		String cname = StrUtils.removeNonAlphaDigitUnderlineCharacters(str);
		return cname;
	}

	// public String getTuffyString() {
	// String cname =
	// StrUtils.removeNonAlphaDigitUnderlineCharacters(this.getName());
	// char fc = cname.charAt(0);
	// if (!Character.isLetter(fc)) {
	// cname = "X" + cname;
	// } else if (Character.isLowerCase(fc)){
	// cname = Character.toUpperCase(fc) + cname.substring(1);
	// }
	// return cname;
	// }

}
