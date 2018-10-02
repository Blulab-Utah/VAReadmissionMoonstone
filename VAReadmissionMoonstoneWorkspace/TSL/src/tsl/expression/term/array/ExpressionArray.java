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
package tsl.expression.term.array;

import tsl.expression.Expression;
import tsl.utilities.MathUtils;

// 7/7/2014:  Array of expressions and their corresponding weights, for use in
// machine learning applications.

public class ExpressionArray {
	
	private Expression[] expressions = null;
	private double[] weights = null;
	
	public ExpressionArray(Expression[] e) {
		this.expressions = e;
		this.weights = new double[e.length];
	}
	
	public ExpressionArray(Expression[] e, double[] w) {
		this.expressions = e;
		this.weights = w;
	}
	
	public double getCosineSimilarity(double[] v) {
		if (this.weights.length == v.length) {
			return MathUtils.cosineSimilarity(this.weights, v);
		}
		return -1;
	}
	
	public Expression getExpressionAt(int index) {
		return this.expressions[index];
	}
	
	public void setExpressionAt(Expression e, int index) {
		this.expressions[index] = e;
	}
	
	public double getWeightAt(int index) {
		return this.weights[index];
	}
	
	public void setWeightAt(double w, int index) {
		this.weights[index] = w;
	}

	public Expression[] getExpressions() {
		return expressions;
	}

	public void setExpressions(Expression[] expressions) {
		this.expressions = expressions;
	}

	public double[] getWeights() {
		return weights;
	}

	public void setWeights(double[] weights) {
		this.weights = weights;
	}

}
