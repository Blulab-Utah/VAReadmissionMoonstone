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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.BindSentence;
import tsl.expression.form.sentence.ExistentialSentence;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.constant.ObjectConstant;
import tsl.expression.term.function.javafunction.JavaFunctionTerm;
import tsl.expression.term.function.logicfunction.LogicFunctionTerm;
import tsl.expression.term.relation.JavaRelationConstant;
import tsl.expression.term.relation.JavaRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypePredicate;
import tsl.expression.term.variable.Variable;
import tsl.inference.InferenceEngine;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.utilities.ListUtils;
import tsl.utilities.VUtils;

public class BackwardChainingInferenceEngineNEW extends InferenceEngine {

	public static int ProofCount = 0;

	/*
	 * Need one more full week to wring out the new BCIE. Need to test different
	 * sentence types. Need to find out why some verified bindings are null when
	 * they are stored. Fix proof depth.
	 */

	public BackwardChainingInferenceEngineNEW(KnowledgeBase kb) {
		super(kb);
	}

	public void initializeQuerySentence(KnowledgeBase kb, Sentence sentence,
			Vector binds) {
		kb.setQueryExpression(sentence);
		this.setQuerySentence(sentence);
		ExpressionProofWrapperManager.initialize();
		ExpressionProofWrapperManager.EPWM.setQuerySentence(sentence);
		sentence.pushExpressionProofWrapper();
		kb.getInferenceEngine().initialize();
	}

	public boolean prove(Sentence sentence) {
		return prove(sentence, null);
	}

	public boolean prove(Sentence sentence, Vector alsoProve) {
		if (atEnd()) {
			return true;
		}
		this.getKnowledgeBase().getInferenceEngine().incrementProofCount();
		if (sentence instanceof BindSentence) {
			return proveBindSentence((BindSentence) sentence, alsoProve);
		}
		if (sentence instanceof JavaRelationSentence) {
			return proveJavaRelationSentence((JavaRelationSentence) sentence,
					alsoProve);
		}
		if (sentence instanceof RelationSentence) {
			return proveRelationSentence((RelationSentence) sentence, alsoProve);
		}
		if (sentence instanceof AndSentence) {
			return proveAndSentence((AndSentence) sentence, alsoProve);
		}
		if (sentence instanceof OrSentence) {
			return proveOrSentence((OrSentence) sentence, alsoProve);
		}
		if (sentence instanceof NotSentence) {
			return proveNotSentence((NotSentence) sentence, alsoProve);
		}
		if (sentence instanceof TypePredicate) {
			return proveTypePredicate((TypePredicate) sentence, alsoProve);
		}
		if (sentence instanceof ExistentialSentence) {
			return proveExistentialSentence((ExistentialSentence) sentence,
					alsoProve);
		}
		return false;
	}

	/*
	 * Notes on Provisional and Validated bindings: At the tail end of a proof,
	 * the final bindings of query variables in the predicate get added to
	 * ProvisionalBindings. They have to be added at the tail end of the proof
	 * because those bindings are lost when I return to higher levels. At
	 * depth=1, if the proof succeeded I move provisional bindings to validated
	 * bindings. I only need to store validated bindings in RelationSentence,
	 * because af JavaRelationalSentence will never be at level 1 (it is never
	 * the RHS of a rule, or a query).
	 */

	public boolean proveRelationSentence(RelationSentence rs, Vector alsoProve) {
		Vector<RelationSentence> sentences = null;
		int originalBindStartIndex = 0;
		int originalBindVariableCount = 0;
		ExpressionProofWrapperManager epwm = ExpressionProofWrapperManager.EPWM;
		ExpressionProofWrapper pepw = rs.getLastExpressionProofWrapper();

		// 3/24/2014: The current sentence may point to one of the parent KBs. I
		// want to
		// use the currently selected KB. (May need to change this elsewhere...)
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		Object subject = null;
		if (rs.getSubject() != null) {
			subject = rs.getSubject().eval();
		}
		if (subject != null && subject instanceof Term) {
			Term ts = (Term) subject;
			sentences = ts.getSubjectSentences(rs.getRelation());
		}
		if (sentences == null) {
			sentences = kb.getStoredRelationSentences(rs.getRelation());
		}
		Vector<ImplicationSentence> isents = kb
				.getStoredImplicationSentences(rs.getRelation());
		sentences = VUtils.append(sentences, isents);
		if (sentences == null) {
			printWithIndent("proveRelationSentence:  No sentences to match...",
					rs, pepw.getProofDepth(), false);
			return false;
		}
		int depth = pepw.getProofDepth();
		printWithIndent("#RS:Proving=", rs, depth, false);
		originalBindStartIndex = pepw.getOriginalBindingIndex();
		originalBindVariableCount = pepw.storeOriginalBindings(alsoProve);
		boolean foundMatch = false;
		boolean firstProved = false;
		for (Sentence tomatch : sentences) {
			RelationSentence head = tomatch.getHead();
			ExpressionProofWrapper cepw = head.pushExpressionProofWrapper(pepw);
			if (match(rs, pepw, head, cepw)) {
				Vector ap = augmentAlsoProve(tomatch, alsoProve);
				if (proveAP(ap)) {
					firstProved = true;
					printWithIndent("#RS+AP: PROVED=", rs, depth, false);
					foundMatch = true;
					epwm.storeProvisionalBindings();
					if (pepw.isQueryWrapper()) {
						epwm.storeValidatedBindings();
					}
				} else if (pepw.isQueryWrapper()) {
					epwm.clearProvisionalBindings();
				}
			}
			head.popExpressionProofWrapper();
			pepw.restoreOriginalBindings(originalBindStartIndex,
					originalBindVariableCount);
			if (firstProved && KnowledgeEngine.isBreakAtFirstProof()) {
				break;
			}
		}
		printWithIndent((foundMatch ? "#RS:SUCCEEDED" : "#RS:FAILED"), null,
				depth, false);
		return foundMatch;
	}

	public boolean proveJavaRelationSentence(JavaRelationSentence jrs,
			Vector alsoProve) {
		ExpressionProofWrapper epw = jrs.getLastExpressionProofWrapper();
		ExpressionProofWrapperManager epwm = ExpressionProofWrapperManager.EPWM;
		printWithIndent("*JRS:Proving: ", jrs, epw.getProofDepth(), false);
		boolean result = false;
//		JavaRelationConstant jrc = (JavaRelationConstant) jrs.getRelation();
		result = jrs.eval();
		// Before 4/29/2015
//		result = jrc.invokeUsingProofVariables(jrs);
		if (result && alsoProve != null) {
			result = false;
			int originalBindStartIndex = epw.getOriginalBindingIndex();
			int originalBindVariableCount = epw
					.storeOriginalBindings(alsoProve);
			if (proveAP(alsoProve)) {
				printWithIndent("*JRS+AP: PROVED=", jrs, epw.getProofDepth(),
						false);
				result = true;
				epwm.storeProvisionalBindings();
			}
			epw.restoreOriginalBindings(originalBindStartIndex,
					originalBindVariableCount);
		}
		printWithIndent((result ? "*JRS: SUCCEEDED" : "*JRS: FAILED"), null,
				epw.getProofDepth(), false);
		return result;
	}

	public boolean match(RelationSentence rs, ExpressionProofWrapper pepw,
			RelationSentence child, ExpressionProofWrapper cepw) {
		String relname1 = null, relname2 = null;
		Vector terms1 = null, terms2 = null;
		relname1 = rs.getRelation().getName();
		terms1 = rs.getTerms();
		relname2 = child.getRelation().getName();
		terms2 = child.getTerms();

		// 6/5/2014
		if (terms1 == null && terms2 == null) {
			return true;
		}

		if (!(relname1.equals(relname2) && VUtils.sameLength(terms1, terms2))) {
			return false;
		}

		if (terms1 != null) {
			for (int i = 0; i < terms1.size(); i++) {
				Term term1 = (Term) terms1.elementAt(i);
				Term term2 = (Term) terms2.elementAt(i);
				if (term1 instanceof Variable) {
					term1 = pepw
							.getExpressionProofWrapperVariable((Variable) term1);
				}
				if (term2 instanceof Variable) {
					term2 = cepw
							.getExpressionProofWrapperVariable((Variable) term2);
				}
				if (!matchTerms(rs, term1, child, term2)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean matchTerms(RelationSentence rs, Term term1,
			RelationSentence child, Term term2) {
		Object o = null;
		Object val1 = null;
		Object val2 = null;
		Expression ckbe = null;

		// Reimplement this later...
		if (term1 instanceof JavaFunctionTerm
				|| term2 instanceof JavaFunctionTerm) {
			Term t1 = (Term) term1;
			Term t2 = (Term) term2;
			ckbe = t1.getContainingKBExpression();
			if (ckbe == null) {
				ckbe = t2.getContainingKBExpression();
			}
			if (ckbe != null) {
				ckbe.setSelectedExpressionProofWrapper(rs
						.getLastExpressionProofWrapper());
			}
		}

		if (!(term1 instanceof Variable)) {
			o = Term.evalObject(term1);
			if (o != null && !(o instanceof Term)) {
				term1 = new ObjectConstant(o);
			}
		}

		if (!(term2 instanceof Variable)) {
			o = Term.evalObject(term2);
			if (o != null && !(o instanceof Term)) {
				term2 = new ObjectConstant(o);
			}
		}

		if (ckbe != null) {
			ckbe.setSelectedExpressionProofWrapper(null);
		}

		if (!(term1 instanceof Variable) && !(term2 instanceof Variable)) {
			if (term1 != null && term2 != null) {
				if (term1.equals(term2)) {
					return true;
				}
			}
			if (term1 instanceof LogicFunctionTerm
					&& term2 instanceof LogicFunctionTerm) {
				LogicFunctionTerm lft1 = (LogicFunctionTerm) term1;
				LogicFunctionTerm lft2 = (LogicFunctionTerm) term2;
				return matchTerms(rs, lft1, child, lft2);
			}
		}

		if (term1 instanceof Variable
				&& (val1 = ((Variable) term1).eval()) != null) {
			return matchTerms(rs, (Term) val1, child, term2);
		}
		if (term2 instanceof Variable
				&& (val2 = ((Variable) term2).eval()) != null) {
			return matchTerms(rs, term1, child, (Term) val2);
		}

		if (term1 instanceof Variable && term1 != term2) {
			((Variable) term1).bind(term2);
			return true;
		}
		if (term2 instanceof Variable && term1 != term2) {
			((Variable) term2).bind(term1);
			return true;
		}
		return false;
	}

	public boolean proveBindSentence(BindSentence bs, Vector alsoProve) {
		boolean result = false;
		ExpressionProofWrapper pepw = bs.getLastExpressionProofWrapper();
		ExpressionProofWrapperManager epwm = ExpressionProofWrapperManager.EPWM;
		Variable var = (Variable) bs.getTerm(0);
		Variable pvar = pepw.getExpressionProofWrapperVariable(var);
		Object t = bs.getTerms().lastElement();
		Object value = Term.evalObject(t);
		if (value != null) {
			int originalBindStartIndex = pepw.getOriginalBindingIndex();
			int originalBindVariableCount = pepw
					.storeOriginalBindings(alsoProve);
			List vlist = (value instanceof ArrayList ? (ArrayList) value
					: ListUtils.listify(value));
			for (Object o : vlist) {
				pvar.bind(o);
				if (proveAP(alsoProve)) {
					result = true;
					epwm.storeProvisionalBindings();
				}
				pepw.restoreOriginalBindings(originalBindStartIndex,
						originalBindVariableCount);
				pvar.unbind();
			}
		}
		return result;
	}

	/*********************
	 * 1/27/2011: PROBLEM: In the rule below, the first 'bind was added at the
	 * top level so ?date would be visible at the end. However, after the rule
	 * succeeds, the value of the first binding, *null*, is stored
	 * provisionally, and both *null* and a real date appear as validated
	 * bindings in the final answer.
	 * 
	 * (-> (and (is_condition ?c) (bind ?date *null*) (or (and
	 * (report_contains_finding ?report ?c) (date_of ?report ?d) (bind ?date
	 * ?d)) (bind ?date ?*current_date*))) (date_of_finding ?c ?date))
	 *************************/

	public boolean proveTypePredicate(TypePredicate tp, Vector alsoProve) {
		Term t = (Term) tp.getArgument().eval();
		boolean result = t.getType().subsumedBy(tp.getType());
		ExpressionProofWrapper epw = tp.getLastExpressionProofWrapper();
		if (result && alsoProve != null) {
			result = false;
			int originalBindStartIndex = epw.getOriginalBindingIndex();
			int originalBindVariableCount = epw
					.storeOriginalBindings(alsoProve);
			if (proveAP(alsoProve)) {
				result = true;
			}
			epw.restoreOriginalBindings(originalBindStartIndex,
					originalBindVariableCount);
		}
		return result;
	}

	public boolean proveNotSentence(NotSentence ns, Vector alsoProve) {
		boolean provedSentence = prove(ns.getSentence(), null);
		boolean provedAP = false;
		if (!provedSentence) {
			provedAP = proveAP(alsoProve);
		}
		return !provedSentence && provedAP;
	}

	public boolean proveExistentialSentence(ExistentialSentence es,
			Vector alsoProve) {
		return prove(es.getSentence(), alsoProve);
	}

	public boolean proveAndSentence(AndSentence as, Vector alsoProve) {
		return proveAP(VUtils.appendNew(as.getSentences(), alsoProve));
	}

	public boolean proveOrSentence(OrSentence os, Vector alsoProve) {
		boolean proved = false;
		ExpressionProofWrapper epw = os.getLastExpressionProofWrapper();
		for (Sentence s : os.getSentences()) {
			int originalBindStartIndex = epw.getOriginalBindingIndex();
			int originalBindVariableCount = epw
					.storeOriginalBindings(alsoProve);
			epw.storeOriginalBindings(alsoProve);
			if (prove(s, alsoProve)) {
				proved = true;
			}
			epw.restoreOriginalBindings(originalBindStartIndex,
					originalBindVariableCount);
			if (proved) {
				break;
			}
		}
		return proved;
	}

	public boolean prove(Vector alsoProve) {
		return false;
	}

	public boolean proveAP(Vector alsoProve) {
		if (alsoProve != null) {
			Sentence sent = (Sentence) alsoProve.firstElement();
			// Expensive...
			Vector rest = VUtils.rest(alsoProve);
			if (!prove(sent, rest)) {
				return false;
			}
		}
		return true;
	}

	public Vector augmentAlsoProve(Sentence sentence, Vector ap) {
		if (sentence instanceof ImplicationSentence) {
			ap = (ap != null ? new Vector(ap) : new Vector(0));
			ap.insertElementAt(
					((ImplicationSentence) sentence).getAntecedent(), 0);
		}
		return ap;
	}

	static void printWithIndent(String prefix, RelationSentence rs, int depth,
			boolean force) {
		if (force || KnowledgeEngine.isDoQueryDebug()) {
			String rsstr = "";
			for (int i = 0; i < depth * 4; i++) {
				System.out.print(" ");
			}
			if (rs != null) {
				rsstr = rs.getRelation().getName() + "(";
				for (int i = 0; i < rs.getTermCount(); i++) {
					Term term = (Term) rs.getTerm(i);
					Object o = term;
					if (term instanceof Variable) {
						Variable var = (Variable) term;
						ExpressionProofWrapper epw = var
								.getContainingKBExpression()
								.getLastExpressionProofWrapper();
						o = epw.getValue(term);
					}
					rsstr += (o != null ? o : "*");
					if (i < rs.getTermCount() - 1) {
						rsstr += ",";
					}
				}
				rsstr += ")";
			}
			System.out.println("[" + depth + "] " + prefix + rsstr);
		}
	}

	public boolean atEnd() {
		return KnowledgeEngine.isBreakAtFirstProof()
				&& ExpressionProofWrapperManager.EPWM.hasValidatedBindings();
	}

	public Vector unpackValidatedBindings(boolean unpack) {
		Vector unpacked = null;
		Vector<Vector<Variable>> vars = ExpressionProofWrapperManager.EPWM
				.getValidatedBoundVariables();
		if (vars != null) {
			for (Vector<Variable> sv : vars) {
				Vector set = null;
				for (Variable var : sv) {
					Object o = var.getValue();
					set = VUtils.add(set, o);
				}
				unpacked = VUtils.add(unpacked, set);
			}
		}
		return unpacked;
	}

}