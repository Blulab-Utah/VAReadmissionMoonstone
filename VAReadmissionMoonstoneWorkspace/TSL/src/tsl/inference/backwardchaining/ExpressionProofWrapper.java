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
package tsl.inference.backwardchaining;

import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.variable.Variable;

public class ExpressionProofWrapper {
	private Expression expression = null;
	private ExpressionProofWrapper parent = null;
	private Variable[] proofVariables = new Variable[10];
	private Variable[] originalBindingVariables = new Variable[100];
	private Object[] originalBindingValues = new Object[100];
	private int originalBindingIndex = 0;
	private int proofDepth = 0;
	private int wrapperIndex = 0;
	private static int ProofVariableCount = 10;

	private static int MaxExpressionWrapperSize = 10;
	private static ExpressionProofWrapper[] ExpressionProofWrappers = new ExpressionProofWrapper[MaxExpressionWrapperSize];

	public ExpressionProofWrapper(int windex) {
		for (int i = 0; i < ProofVariableCount; i++) {
			this.proofVariables[i] = new Variable();
		}
//		for (int i = 0; i < ProofVariableCount; i++) {
//			this.originalBindingVariables[i] = new Variable();
//		}
		this.wrapperIndex = windex;
	}

	public Variable getExpressionProofWrapperVariable(Variable var) {
		return this.proofVariables[var.getContainingKBExpressionIndex()];
	}

	public void bind(Variable var, Object value) {
		Variable pv = this.getExpressionProofWrapperVariable(var);
		pv.bind(value);
	}

	public void bind(Variable var) {
		Variable pv = this.getExpressionProofWrapperVariable(var);
		pv.setName(var.getName()); // Not necessary...
		pv.bind(var.getValue());
	}

	public void bindVariables() {
		Expression container = this.expression.getContainingKBExpression();
		if (container.getVariableCount() > 0) {
			for (int i = 0; i < container.getVariableCount(); i++) {
				Variable var = container.getVariables().elementAt(i);
				this.bind(var);
			}
		}
	}

	// If a variable is bound to a variable, recursively return that value.
	public Object getValue(Term term) {
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			Variable pv = this.getExpressionProofWrapperVariable(var);
			return pv.getValue();
		}
		return term;
	}

	public int storeOriginalBindings(Vector<Sentence> alsoProve) {
		int vcount = 0;
		for (int i = 0; i < this.getVariableCount(); i++) {
			Variable var = this.proofVariables[i];
			this.originalBindingVariables[this.originalBindingIndex] = var;
			this.originalBindingValues[this.originalBindingIndex] = var.getValue();
			this.originalBindingIndex++;
			vcount++;
		}
		if (alsoProve != null) {
			for (Sentence s : alsoProve) {
				ExpressionProofWrapper epw = s.getLastExpressionProofWrapper();
				for (int i = 0; i < epw.getVariableCount(); i++) {
					Variable var = epw.proofVariables[i];
					this.originalBindingVariables[this.originalBindingIndex] = var;
					this.originalBindingValues[this.originalBindingIndex] = var.getValue();
					this.originalBindingIndex++;
					vcount++;
				}
			}
		}
		return vcount;
	}

	public void restoreOriginalBindings(int start, int vcount) {
		for (int i = 0; i < vcount; i++) {
			int index = start + i;
			Variable var = this.originalBindingVariables[index];
			Object value = this.originalBindingValues[index];
			var.bind(value);
		}
		this.originalBindingIndex = start;
	}
	
	// Before 5/30/2014
//	public void restoreOriginalBindings(int index) {
//		for (int i = 0; i < this.originalBindingIndex; i++) {
//			Variable var = this.originalBindingVariables[i];
//			Object value = this.originalBindingValues[i];
//			var.bind(value);
//		}
//		this.originalBindingIndex = 0;
//	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Variable[] getProofVariables() {
		return proofVariables;
	}

	public int getProofVariableCount() {
		return this.expression.getContainingKBExpression().getVariableCount();
	}

	public void reinitialize() {
		for (int i = 0; i < this.expression.getContainingKBExpression()
				.getVariableCount(); i++) {
			proofVariables[i].setName(null);
			proofVariables[i].unbind();
		}
		if (this.proofDepth == 1) {
			int x = 1;
			x = x;
		}
		this.expression = null;
	}

	public ExpressionProofWrapper getParent() {
		return this.parent;
	}

	public boolean isQueryWrapper() {
		return this.parent == null;
	}

	public void setParent(ExpressionProofWrapper parent) {
		this.parent = parent;
	}

	public int getProofDepth() {
		return proofDepth;
	}

	public void setProofDepth(int proofDepth) {
		this.proofDepth = proofDepth;
	}

	public int getVariableCount() {
		if (this.expression == null) {
			int x = 1;
			x = x;
		}
		return this.expression.getContainingKBExpression().getVariableCount();
	}

	public String toString() {
		String str = "<EPW: Expression=" + this.expression + ",Depth="
				+ this.proofDepth + ">";
		return str;
	}

	public int getWrapperIndex() {
		return wrapperIndex;
	}
	
	public void reset() {
		this.originalBindingIndex = 0;
	}

	public int getOriginalBindingIndex() {
		return originalBindingIndex;
	}

}
