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
package tsl.expression.term.relation;

import java.lang.reflect.Method;
import java.util.List;

import tsl.expression.term.Term;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.knowledge.knowledgebase.KnowledgeBase;

public class JavaRelationConstant extends RelationConstant {
	private Method method = null;
	private Class[] parameterTypes = null;
	private Object[] argumentArray = null;
	private boolean failedInvocation = false;

	public JavaRelationConstant(Method method, Object[] argumentArray) {
		super(method.getName());
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		this.method = method;
		this.parameterTypes = method.getParameterTypes();
		this.argumentArray = argumentArray;
		if (argumentArray == null) {
			this.argumentArray = new Object[this.parameterTypes.length];
		}
		kb.getNameSpace().addConstant(this);
	}
	
	// Before 4/29/2015
//	public boolean invokeUsingProofVariables(JavaRelationSentence jrs) {
//		boolean result = false;
//		try {
//			boolean didassign = this
//					.assignArgumentArrayFromProofVariables(jrs, false);
//			if (!didassign) {
//				return false;
//			}
//			Object rv = this.method.invoke(null, this.argumentArray);
//			if (rv instanceof Boolean) {
//				result = ((Boolean) rv).booleanValue();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return result;
//	}

	public static JavaRelationConstant createJavaRelationConstant(String mstr) {
		JavaRelationConstant jrc = null;
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		String mname = getMethodName(mstr);
		RelationConstant rc = (RelationConstant) kb.getNameSpace()
				.getRelationConstant(mname);
		if (rc == null) {
			Method m = getMethod(mstr);
			if (m != null) {
				jrc = new JavaRelationConstant(m, null);
			}
		} else if (rc instanceof JavaRelationConstant) {
			jrc = (JavaRelationConstant) rc;
		}
		return jrc;
	}

	public boolean assignArgumentArrayFromProofVariables(
			JavaRelationSentence jrs, boolean includeNulls) {
		if (this.argumentArray == null) {
			this.argumentArray = new Object[jrs.getTermCount()];
		}
		if (jrs.getTerms() != null) {
			List<ProofVariable> pbinds = (List<ProofVariable>) jrs
					.getProofVariables();
			for (int i = 0; i < jrs.getTermCount(); i++) {
				Object o = jrs.getTerm(i);
				if (o instanceof Variable) {
					o = ((Variable) o).getProofVariable(pbinds);
				}
				Object value = (o instanceof Term ? ((Term) o).eval() : o);
				if (value == null) {
					if (!includeNulls) {
						return false;
					}
					value = KnowledgeBase.getNullPlaceholderTerm();
				}
				this.argumentArray[i] = value;
			}
		}
		return true;
	}

	public Method getMethod() {
		return this.method;
	}

	public Class[] getParameterTypes() {
		return parameterTypes;
	}

	public Object[] getArgumentArray() {
		return argumentArray;
	}

	public void setArgumentArray(Object[] argumentArray) {
		this.argumentArray = argumentArray;
	}

	public boolean isFailedInvocation() {
		return failedInvocation;
	}

	public void setFailedInvocation(boolean failedInvocation) {
		this.failedInvocation = failedInvocation;
	}

}
