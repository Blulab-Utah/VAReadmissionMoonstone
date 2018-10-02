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
package tsl.expression;

// Expression -> Term, Form
// Term -> Constant, Function, Variable,Template.
// Function->LogicFunction, JavaFunction
// Constant -> ObjConstant, RelConstant, TypeConstant, FuncConstant.
// Form -> N-ary relational sentences, and-sentences, or-sentences, quantifier sentences, 
//		equality sentence, reified sentence, user instruction, Assertion.

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import tsl.expression.form.Form;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.constraint.Constraint;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.inference.backwardchaining.ExpressionProofWrapper;
import tsl.inference.backwardchaining.ExpressionProofWrapperManager;
import tsl.inference.backwardchaining.ProofVariable;
import tsl.information.TSLInformation;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.ontology.Ontology;
import tsl.tsllisp.Sexp;
import tsl.utilities.ListUtils;
import tsl.utilities.VUtils;

public abstract class Expression extends TSLInformation {

	/*
	 * NOTES: When I store expressions to MySQL, each term / form can have both
	 * termID and KBID. I can create a table with KB info (e.g. name).
	 */

	/* empty comment for svn purposes */

	private Expression containingKBExpression = null;
	protected KnowledgeBase knowledgeBase = null;
	public Ontology ontology = null;
	private Vector terms = null;
	private Vector<Variable> variables = null;
	private Vector<Vector<ProofVariable>> proofVariableStack = null;
	private List<ProofVariable> selectedProofVariableList = null;
	private boolean isVisited = false;
	private long numericID = -1;
	private short KBID = -1;
	private boolean isValid = true;
	private String stringID = null;
	private Sexp sexp = null;
	private static long lastNumericID = 0;
	private Expression containedBy = null;
	private Vector<Object[]> variableBindings = null;
	public Object[] argumentArray = null;

	// 6/20/2013: Moved up from Term. I want to be able to name Sentences, and
	// add 2nd-order sentences.
	public String name = null;
	public String fullName = null;

	// 4/22/2014: ExpressionProofWrappers
	private ExpressionProofWrapper selectedExpressionProofWrapper = null;
	private Vector<ExpressionProofWrapper> expressionProofWrappers = null;

	public Expression() {
		if (!(this instanceof Variable)) {
			this.numericID = lastNumericID++;
		}
		this.knowledgeBase = KnowledgeBase.getCurrentKnowledgeBase();
	}

	public Expression(short kBID) {
		super();
		this.setKBID(kBID);
	}

	public Expression(Vector pattern) {
		super(pattern);
	}

	public Expression(String name) {
		this.name = name;
	}

	public Expression(Sexp sexp) {
		this();
		this.sexp = sexp;
		this.name = sexp.getCar().toString().toLowerCase();
	}

	public static Expression createExpression(Vector v) {
		Expression e = null;
		if ((e = Form.createForm(v)) != null
				|| (e = Term.createTerm(v)) != null) {
			return e;
		}
		return null;
	}

	public Object eval() {
		return null;
	}

	public Object eval(Vector binds) {
		return eval(binds, true);
	}

	public Object eval(Vector binds, boolean unpackResults) {
		return null;
	}

	public void resolveReferences() {

	}

	public Expression getContainingKBExpression() {
		return containingKBExpression;
	}

	public void setContainingKBExpression(Expression containingKBExpression) {
		this.containingKBExpression = containingKBExpression;
	}

	public KnowledgeBase getKnowledgeBase() {
		if (this.knowledgeBase == null) {
			this.knowledgeBase = KnowledgeBase.getCurrentKnowledgeBase();
		}
		return knowledgeBase;
	}

	public void setKnowledgeBase(KnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void assignContainingKBExpression(KnowledgeBase kb,
			Expression containingKBExpression) {
		this.containingKBExpression = containingKBExpression;
		this.knowledgeBase = kb;
		// if (this.terms != null) {
		// for (Enumeration e = this.terms.elements(); e.hasMoreElements();) {
		// Term term = (Term) e.nextElement();
		// term.assignContainingKBExpression(kb, containingKBExpression);
		// }
		// }
	}

	// Creates a list of proof variables corresponding to the variables in the
	// containingKBExpression. When
	// I evaluate the variables, I get the corresponding proof variable from the
	// containing expression and
	// get its value. That's why the debug window shows the sentence with its
	// variables bound, even though
	// the variable objects aren't actually bound -- it's through the proof
	// variables!

	public void pushProofVariables(Vector binds) {
		if (this.containingKBExpression != null) {
			List<ProofVariable> pvars = ProofVariable
					.wrapVariables(this.containingKBExpression.variables);
			if (pvars != null) {
				this.containingKBExpression.proofVariableStack = VUtils.add(
						this.containingKBExpression.proofVariableStack, pvars);
				if (binds != null) {
					for (int i = 0; i < binds.size(); i++) {
						ProofVariable pvar = (ProofVariable) pvars.get(i);
						Object value = binds.elementAt(i);
						if (value != null) {
							pvar.bind(value);
						}
					}
				}
			}
		}
	}

	public void popProofVariables() {
		if (this.containingKBExpression != null
				&& this.containingKBExpression.proofVariableStack != null) {
			this.containingKBExpression.proofVariableStack
					.remove(this.containingKBExpression.proofVariableStack
							.size() - 1);
			if (this.containingKBExpression.proofVariableStack.isEmpty()) {
				this.containingKBExpression.proofVariableStack = null;
			}
		}
	}

	public List<ProofVariable> getProofVariables() {
		List<ProofVariable> pvars = null;
		if (this.containingKBExpression != null
				&& this.containingKBExpression.proofVariableStack != null) {
			pvars = this.getSelectedProofVariableList();
			if (pvars == null) {
				pvars = this.containingKBExpression.proofVariableStack
						.lastElement();
			}
		}
		return pvars;
	}

	public static List<ProofVariable> getProofVariables(List<Sentence> sentences) {
		List<ProofVariable> pvars = null;
		if (sentences != null) {
			for (Sentence sentence : sentences) {
				pvars = ListUtils.appendIfNot(pvars,
						sentence.getProofVariables());
			}
		}
		return pvars;
	}

	public Expression copy() {
		return this;
	}

	public String toLisp() {
		return null;
	}

	public Vector getTerms() {
		return terms;
	}

	public Object getTerm(int index) {
		if (index < this.getTermCount()) {
			return this.terms.elementAt(index);
		}
		return null;
	}

	public void setTerms(Vector terms) {
		this.terms = terms;
	}

	// Changed 5/19/2014: In OWL ontologies there are identical
	// subjects/modifiers,
	// e.g. (affects Element Element).
	// 12/7/2014: What if I *want* to permit identical terms?
	public void addTerm(Object term) {
		this.terms = VUtils.add(this.terms, term);
		// this.terms = VUtils.addIfNot(this.terms, term);
	}

	public void addTermForce(Object term) {
		this.terms = VUtils.add(this.terms, term);
	}

	public int getTermCount() {
		return (this.terms != null ? this.terms.size() : 0);
	}

	public Vector<Variable> getVariables() {
		return variables;
	}

	public void setVariables(Vector<Variable> variables) {
		this.variables = variables;
	}

	public void setProofVariableStack(
			Vector<Vector<ProofVariable>> proofVariableStack) {
		this.proofVariableStack = proofVariableStack;
	}

	public void setVariableBindings(Vector<Object[]> variableBindings) {
		this.variableBindings = variableBindings;
	}

	public void setExpressionProofWrappers(
			Vector<ExpressionProofWrapper> expressionProofWrappers) {
		this.expressionProofWrappers = expressionProofWrappers;
	}

	public void addVariable(Variable var) {
		this.variables = VUtils.add(this.variables, var);
	}

	public void addVariables(Vector<Variable> vars) {
		this.variables = VUtils.append(this.variables, vars);
	}

	public int getVariableCount() {
		return (this.variables != null ? this.variables.size() : 0);
	}

	public Variable getVariable(int index) {
		return this.variables.elementAt(index);
	}

	public void setVisited(boolean visited) {
		this.isVisited = visited;
	}

	public boolean isVisited() {
		return this.isVisited;
	}

	// Need a better way to do this... What about when I
	public long getNumericID() {
		return numericID;
	}

	public static void setlastNumericID(long id) {
		lastNumericID = id;
	}

	public short getKBID() {
		return KBID;
	}

	public void setKBID(short kBID) {
		KBID = kBID;
	}

	public String getClassNumericID() {
		String id = this.getClass().getSimpleName() + ":" + this.getNumericID();
		return id;
	}

	// Validate() returns null if the validation succeeded, else
	// may return an error message or some other relevant item.
	public Object validate() {
		setValid(true);
		return null;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getName() {
		if (this.name != null) {
			return this.name;
		}
		return this.toString();
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Vector<String> getNames(Vector<Expression> expressions) {
		Vector<String> names = null;
		if (expressions != null) {
			for (Expression e : expressions) {
				names = VUtils.add(names, e.getName());
			}
		}
		return names;
	}

	public String getLabel() {
		return (this.getName() != null ? this.getName() : this.toString());
	}

	public boolean preProofValidate() {
		return true;
	}

	public Expression getContainedBy() {
		return containedBy;
	}

	public void setContainedBy(Expression containedBy) {
		this.containedBy = containedBy;
	}

	public String getStringID() {
		return (this.stringID != null ? this.stringID : "*");
	}

	public void setStringID(String stringID) {
		this.stringID = stringID;
	}

	// 10/2/2013
	public Vector<TypeConstant> gatherSupportingTypes() {
		return null;
	}

	public Sexp getSexp() {
		return sexp;
	}

	public void setSexp(Sexp sexp) {
		this.sexp = sexp;
	}

	// 4/5/2014
	public List<ProofVariable> getSelectedProofVariableList() {
		return selectedProofVariableList;
	}

	public void setSelectedProofVariableList(
			List<ProofVariable> selectedProofVariableList) {
		this.selectedProofVariableList = selectedProofVariableList;
	}

	public static class NameSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Expression e1 = (Expression) o1;
			Expression e2 = (Expression) o2;
			String name1 = e1.getName();
			String name2 = e2.getName();
			return name1.compareTo(name2);
		}
	}

	// 5/16/2014
	public ExpressionProofWrapper getLastExpressionProofWrapper() {
		Expression container = this.getContainingKBExpression();
		if (container.expressionProofWrappers != null) {
			return container.expressionProofWrappers.lastElement();
		}
		return null;
	}

	public void pushExpressionProofWrapper() {
		pushExpressionProofWrapper(null);
	}

	public ExpressionProofWrapper pushExpressionProofWrapper(
			ExpressionProofWrapper parent) {
		Expression container = this.getContainingKBExpression();
		ExpressionProofWrapper epw = ExpressionProofWrapperManager.EPWM
				.pushExpressionProofWrapper(parent, this);
		container.expressionProofWrappers = VUtils.add(
				container.expressionProofWrappers, epw);
		return epw;
	}

	public void popExpressionProofWrapper() {
		ExpressionProofWrapperManager.EPWM.popExpressionProofWrapper();
		Expression container = this.getContainingKBExpression();
		if (container.expressionProofWrappers != null) {
			container.expressionProofWrappers
					.remove(container.expressionProofWrappers.size() - 1);
			if (container.expressionProofWrappers.isEmpty()) {
				container.expressionProofWrappers = null;
			}
		}
	}

	// Equivalent to selectedProofList, for when an RS calls itself recursively.
	public ExpressionProofWrapper getSelectedExpressionProofWrapper() {
		return selectedExpressionProofWrapper;
	}

	public void setSelectedExpressionProofWrapper(
			ExpressionProofWrapper selectedExpressionProofWrapper) {
		this.selectedExpressionProofWrapper = selectedExpressionProofWrapper;
	}

	public Object evalPattern(Object arg, Vector<Variable> vars) {
		return evalPattern(this, arg, vars, false);
	}

	public Object evalPatternRecursive(Object arg, Vector<Variable> vars) {
		return evalPattern(this, arg, vars, true);
	}

	private static Object evalPattern(Expression expr, Object arg,
			Vector<Variable> vars, boolean recursive) {
		if (vars == null) {
			vars = expr.getVariables();
		}
		if (arg instanceof ObjectConstant) {
			Object value = ((ObjectConstant) arg).getObject();
			return evalPattern(expr, value, vars, recursive);
		}
		if (arg instanceof Constraint) {
			Constraint c = (Constraint) arg;
			return c.evalConstraint(vars);
		}
		if (Variable.isVariable(arg)) {
			Variable var = Variable.find(vars, arg);
			if (var != null && var.isBound()) {
				return evalPattern(expr, var.getValue(), vars, recursive);
			} 
			return expr.evalVariable(arg);
		}
		if (arg instanceof Vector) {
			Vector v = (Vector) arg;
			if (v.size() == 2 && v.firstElement() instanceof String) {
				String attr = (String) v.elementAt(0);
				Object rv = evalPattern(expr, v.elementAt(1), vars, recursive);
				if (rv instanceof Expression) {
					Expression e = (Expression) rv;
					return (recursive ? e.getPropertyRecursive(attr) : e
							.getProperty(attr));
				}
			}
			return null;
		}
		if (arg instanceof String) {
			String argname = (String) arg;
			Object co = Constant.extractConstant(expr.getKnowledgeBase(), argname);
			if (co instanceof Constant) {
				return co;
			}

			
//			if (isTypeString(argname)) {
//				return TypeConstant.getType(argname);
//			}
		}
		return arg;
	}

	// 11/16/2015: ELIMINATE THIS.
	public static Object evalPattern(Object arg, Vector<Variable> vars,
			boolean recursive) {
		if (arg instanceof ObjectConstant) {
			Object value = ((ObjectConstant) arg).getObject();
			return evalPattern(value, vars, recursive);
		}
		if (arg instanceof Constraint) {
			Constraint c = (Constraint) arg;
			return c.evalConstraint(vars);
		}
		if (Variable.isVariable(arg)) {
			Variable var = Variable.find(vars, arg);
			if (var != null && var.isBound()) {
				return evalPattern(var.getValue(), vars, recursive);
			}
		}
		if (arg instanceof Vector) {
			Vector v = (Vector) arg;
			if (v.size() == 2 && v.firstElement() instanceof String) {
				String attr = (String) v.elementAt(0);
				Object rv = evalPattern(v.elementAt(1), vars, recursive);
				if (rv instanceof Expression) {
					Expression e = (Expression) rv;
					return (recursive ? e.getPropertyRecursive(attr) : e
							.getProperty(attr));
				}
				// 5/6/2016, e.g. (list "concept1" "concept2" ...)
			} else if ("list".equals(v.firstElement())) {
				return VUtils.rest(v);
			}
			
			
			// System.out
			// .println("Would be returning from Expression.evalPattern() with non-property vector; arg="
			// + arg);
			// int x = 1;
			return null;
		}
		// Handle definitions, e.g. type, lambda, property, grammar rule.
		// Apply a function (including lambda function) to a set of arguments.
		// Apply equivalent of evlist() to a set of objects.
		// Apply a TSL question to the RelSents associated with an Expression.
		if (arg instanceof String) {
			String astr = (String) arg;
			Object value = astr;
			
			KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
			Object co = Constant.extractConstant(kb, astr);
			if (co instanceof Constant) {
				return co;
			}
			
			if (isTypeString(astr)) {
				value = TypeConstant.getType(astr);
			}
			return value;
		}
		return arg;
	}

	public Object[] getVariableBindings() {
		if (this.variableBindings != null) {
			return this.variableBindings.lastElement();
		}
		return null;
	}

	public void pushVariableBindings(Object[] binds) {
		this.variableBindings = VUtils.add(this.variableBindings, binds);
	}

	public void popVariableBindings() {
		if (this.variableBindings != null) {
			this.variableBindings = VUtils.butLast(this.variableBindings);
		}
	}

	public Object evalVariable(Object o) {
		return null;
	}
	
	// 7/22/2016
	public String getTuffyString() {
		String idstr = "E" + String.valueOf(this.getNumericID());
		return idstr;
	}
	

}
