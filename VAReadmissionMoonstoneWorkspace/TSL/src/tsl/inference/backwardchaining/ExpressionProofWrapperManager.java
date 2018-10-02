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
import tsl.expression.term.variable.Variable;
import tsl.utilities.VUtils;

public class ExpressionProofWrapperManager {

	private Sentence querySentence = null;
	private int currentExpressionProofWrapperIndex = 0;
	private ExpressionProofWrapper[] expressionProofWrappers = null;
	private ExpressionProofWrapper currentExpressionProofWrapper = null;
	private Variable[][] provisionalBindings = null;
	private Variable[][] validatedBindings = null;
	private int provisionalBindingCount = 0;
	private int validatedBindingCount = 0;
	private static int MaxExpressionWrapperSize = 100;
	public static ExpressionProofWrapperManager EPWM = null;
	private static int provisionalArraySize = 10000;
	private static int provisionalBindingSize = 6;
	private static int validatedArraySize = 10000;
	private static int validatedBindingSize = 6;

	public ExpressionProofWrapperManager() {
		EPWM = this;
		this.expressionProofWrappers = new ExpressionProofWrapper[MaxExpressionWrapperSize];
		for (int i = 0; i < MaxExpressionWrapperSize; i++) {
			this.expressionProofWrappers[i] = new ExpressionProofWrapper(i);
		}
		this.provisionalBindings = new Variable[provisionalArraySize][provisionalBindingSize];
		for (int i = 0; i < provisionalArraySize; i++) {
			for (int j = 0; j < provisionalBindingSize; j++) {
				this.provisionalBindings[i][j] = new Variable();
			}
		}
		this.validatedBindings = new Variable[validatedArraySize][validatedBindingSize];
		for (int i = 0; i < validatedArraySize; i++) {
			for (int j = 0; j < validatedBindingSize; j++) {
				this.validatedBindings[i][j] = new Variable();
			}
		}
	}

	public static void initialize() {
		if (EPWM == null) {
			EPWM = new ExpressionProofWrapperManager();
		} else {
			EPWM.reset();
		}
	}

	public ExpressionProofWrapper pushExpressionProofWrapper(
			ExpressionProofWrapper parent, Expression e) {
		ExpressionProofWrapper epw = null;
		if (currentExpressionProofWrapperIndex < MaxExpressionWrapperSize) {
			epw = expressionProofWrappers[currentExpressionProofWrapperIndex++];
			epw.reset();
			epw.setExpression(e);
			epw.setParent(parent);
			epw.bindVariables();
			int depth = (parent != null ? parent.getProofDepth() + 1 : 0);
			epw.setProofDepth(depth);
			currentExpressionProofWrapper = epw;
		}
		return epw;
	}

	public void popExpressionProofWrapper() {
		if (this.currentExpressionProofWrapper != null) {
			// this.currentExpressionProofWrapper.reinitialize(); // Should not
			// be
			// // necessary.
			this.currentExpressionProofWrapper = this.currentExpressionProofWrapper
					.getParent();
			this.currentExpressionProofWrapperIndex--;
		}
	}

	public ExpressionProofWrapper getParentExpressionProofWrapper() {
		if (this.currentExpressionProofWrapper != null) {
			return this.currentExpressionProofWrapper.getParent();
		}
		return null;
	}

	public ExpressionProofWrapper getCurrentExpressionProofWrapper() {
		return this.currentExpressionProofWrapper;
	}

	public void storeProvisionalBindings() {
		Variable[] array = this.provisionalBindings[this.provisionalBindingCount++];
		ExpressionProofWrapper epw = this.querySentence
				.getLastExpressionProofWrapper();
		int vcount = this.querySentence.getContainingKBExpression()
				.getVariableCount();
		if (vcount != 2) {
			int x = 1;
			x = x;
		}
		for (int i = 0; i < vcount; i++) {
			Variable source = epw.getProofVariables()[i];
			Variable target = array[i];
			if (source.isBound()) {
				target.setName(source.getName()); // Not necessary
				target.bind(source.getValue());
				if (source.getValue() == null) {
					int x = 1;
					x = x;
				}
			} else {
				int x = 1;
				x = x;
			}
		}
	}

	public void clearProvisionalBindings() {
		// Not necessary. Take this out when it is clear that having earlier
		// bindings won't
		// hurt anything else.
		for (int i = 0; i < this.provisionalBindingCount; i++) {
			for (int j = 0; j < provisionalBindingSize; j++) {
				Variable var = this.provisionalBindings[i][j];
				var.setName(null);
				var.setValue(null);
			}
		}

		// All that is really needed.
		this.provisionalBindingCount = 0;
	}

	public void clearValidatedBindings() {
		this.validatedBindingCount = 0;
	}

	public void reset() {
		this.provisionalBindingCount = 0;
		this.validatedBindingCount = 0;
		this.currentExpressionProofWrapperIndex = 0;
	}

	// How to check whether a set of bindings already exists in the validated
	// set?
	public void storeValidatedBindings() {
		for (int i = 0; i < this.provisionalBindingCount; i++) {
			Variable[] prow = this.provisionalBindings[i];
			Variable[] vrow = this.validatedBindings[this.validatedBindingCount++];
			for (int j = 0; j < this.getQueryVariableCount(); j++) {
				Object value = prow[j].getValue();
				if (value == null) {
					int x = 1;
					x = x;
				}
				vrow[j].bind(value);
			}
		}
		this.provisionalBindingCount = 0;
	}

	public boolean hasValidatedBindings() {
		return this.validatedBindingCount > 0;
	}

	public Sentence getQuerySentence() {
		return querySentence;
	}

	public void setQuerySentence(Sentence querySentence) {
		this.querySentence = querySentence;
	}

	public Vector<Variable> getQueryVariables() {
		if (this.querySentence != null
				&& this.querySentence.getContainingKBExpression() != null) {
			return this.querySentence.getContainingKBExpression()
					.getVariables();
		}
		return null;
	}

	public int getQueryVariableCount() {
		if (this.querySentence != null
				&& this.querySentence.getContainingKBExpression() != null) {
			return this.querySentence.getContainingKBExpression()
					.getVariableCount();
		}
		return 0;
	}

	public String toString() {
		String str = "<EPWManager:CurrentWrapperIndex="
				+ this.currentExpressionProofWrapperIndex + ",CurrentEPW="
				+ this.getCurrentExpressionProofWrapper() + ">";
		return str;
	}

	public int getValidatedBindingCount() {
		return validatedBindingCount;
	}

	public Vector<Vector<Variable>> getValidatedBoundVariables() {
		Vector<Vector<Variable>> vars = null;
		for (int i = 0; i < this.validatedBindingCount; i++) {
			Variable[] row = this.validatedBindings[i];
			Vector<Variable> v = new Vector(0);
			for (int j = 0; j < this.getQueryVariableCount(); j++) {
				v = VUtils.add(v, row[j]);
			}
			vars = VUtils.add(vars, v);
		}
		return vars;
	}

}
