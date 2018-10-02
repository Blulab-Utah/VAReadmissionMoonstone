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

public class BackwardChainingInferenceEngine extends InferenceEngine {

	public BackwardChainingInferenceEngine(KnowledgeBase kb) {
		super(kb);
	}

	public void initializeQuerySentence(KnowledgeBase kb, Sentence sentence,
			Vector binds) {
		kb.setQueryExpression(sentence);
		this.setQuerySentence(sentence);
		kb.initializeProof();
		sentence.pushProofVariables(binds);
		initialize();
	}

	public boolean prove(Sentence sentence,
			Vector<RelationSentence> localSentences) {
		return prove(sentence, null, localSentences);
	}

	public boolean prove(Sentence sentence, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		if (atEnd()) {
			return true;
		}
		this.getKnowledgeBase().getInferenceEngine().incrementProofCount();
		if (!sentence.preProofValidate()) {
			return false;
		}
		if (sentence instanceof BindSentence) {
			return proveBindSentence((BindSentence) sentence, alsoProve,
					localSentences);
		}
		if (sentence instanceof JavaRelationSentence) {
			return proveJavaRelationSentence((JavaRelationSentence) sentence,
					alsoProve, localSentences);
		}
		if (sentence instanceof RelationSentence) {
			return proveRelationSentence((RelationSentence) sentence,
					alsoProve, localSentences);
		}
		if (sentence instanceof AndSentence) {
			return proveAndSentence((AndSentence) sentence, alsoProve,
					localSentences);
		}
		if (sentence instanceof OrSentence) {
			return proveOrSentence((OrSentence) sentence, alsoProve,
					localSentences);
		}
		if (sentence instanceof NotSentence) {
			return proveNotSentence((NotSentence) sentence, alsoProve,
					localSentences);
		}
		if (sentence instanceof TypePredicate) {
			return proveTypePredicate((TypePredicate) sentence, alsoProve,
					localSentences);
		}
		if (sentence instanceof ExistentialSentence) {
			return proveExistentialSentence((ExistentialSentence) sentence,
					alsoProve, localSentences);
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

	/*
	 * 10/17/2014 note: Need a way to test whether the current rs is the same as
	 * the previous rs in the proof tree, with the same bindings. If so, I am in
	 * an infinite loop.
	 * 
	 * 4/7/2015: Adding localSentences to possible matches for the target.
	 * Specifically adding this so I can pass in sentences from Moonstone
	 * annotations, for use inside the MS parser.
	 */

	public boolean proveRelationSentence(RelationSentence rs, Vector alsoProve,
			Vector<RelationSentence> localSentences) {

		Vector<RelationSentence> sentences = null;

		// 3/24/2014: The current sentence may point to one of the parent KBs. 
		// I want to use the currently selected KB. (May need to change this 
		// elsewhere...)
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();

		Object subject = ProofVariable.getValue(rs.getSubject());
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

		// 4/7/2015
		sentences = VUtils.append(sentences, localSentences);

		if (sentences == null) {
			printWithIndent("proveRelationSentence:  No sentences to match...",
					rs, kb.getProofDepth(), false);
			return false;
		}
		if (kb.getProofDepth() > 100) {
			printWithIndent(
					"proveRelationSentence:  Maximum depth exceeded...", rs, 0,
					true);
			return false;
		}
		int depth = kb.getProofDepth();
		printWithIndent("#RS:Proving=", rs, depth, false);

		int pvsindex = ProofVariable.PVSIndex;
		rs.resetValidBindingStack();
		kb.incrementProofDepth();
		List<ProofVariable> pvars = rs.getProofVariables();
		List<ProofVariable> apvars = Expression.getProofVariables(alsoProve);
		List<ProofVariable> origbinds = null, copybinds = null;
		origbinds = ListUtils.appendNew(pvars, apvars);
		copybinds = ProofVariable.clone(origbinds);
		boolean foundMatch = false;
		boolean proofSuccess = false;
		for (Sentence tomatch : sentences) {
			if (proofSuccess && KnowledgeEngine.isBreakAtFirstProof()) {
				break;
			}
			RelationSentence head = tomatch.getHead();

			// 4/6/2015: If a localSentence RS doesn't match the target
			// relation.
			if (!rs.getRelation().getName()
					.equals(head.getRelation().getName())) {
				continue;
			}

			List<ProofVariable> pbinds = (List<ProofVariable>) rs
					.getProofVariables();
			head.pushProofVariables(null);
			List<ProofVariable> cbinds = head.getProofVariables();
			if (match(rs, pbinds, head, cbinds)) {
				Vector ap = augmentAlsoProve(tomatch, alsoProve);
				if (proveAP(ap, localSentences) && postProofValidation(rs)) {
					proofSuccess = true;
					printWithIndent("#RS+AP: PROVED=", rs, depth, false);
					foundMatch = true;
					ProofVariable.storeProvisionalBindings();
					if (depth <= 1) {
						kb.indexContainingExpression(pbinds,
								rs.getContainingKBExpression());
						ProofVariable.storeValidatedBindings();
					}
				} else if (depth <= 1) {
					ProofVariable.clearProvisionalBindings();
				}
			}
			head.popProofVariables();
			if (origbinds != null) {
				ProofVariable.copyBinds(copybinds, origbinds);
			}
		}
		ProofVariable.resetPVSIndex(pvsindex);
		kb.decrementProofDepth();
		if (depth != kb.getProofDepth()) {
			System.out.println("ProveRelationSentence:  Bad Proof Depth: "
					+ depth + "," + kb.getProofDepth());
		}
		printWithIndent((foundMatch ? "#RS:SUCCEEDED" : "#RS:FAILED"), null,
				depth, false);
		return foundMatch;
	}

	public boolean proveJavaRelationSentence(JavaRelationSentence jrs,
			Vector alsoProve, Vector<RelationSentence> localSentences) {
		KnowledgeBase kb = KnowledgeBase.getCurrentKnowledgeBase();
		int depth = kb.getProofDepth();
		kb.incrementProofDepth();
		printWithIndent("*JRS:Proving: ", jrs, depth, false);
		boolean result = false;
		JavaRelationConstant jrc = (JavaRelationConstant) jrs.getRelation();
		result = jrs.eval();
		if (result && alsoProve != null) {
			result = false;
			int pvsindex = ProofVariable.PVSIndex;
			List apvars = Expression.getProofVariables(alsoProve);
			List origbinds = new ArrayList(apvars);
			List copybinds = ProofVariable.clone(origbinds);
			if (proveAP(alsoProve, null) && postProofValidation(jrs)) {
				printWithIndent("*JRS+AP: PROVED=", jrs, depth, false);
				result = true;
				ProofVariable.storeProvisionalBindings();
			}
			ProofVariable.copyBinds(copybinds, origbinds);
			ProofVariable.resetPVSIndex(pvsindex);
		}
		printWithIndent((result ? "*JRS: SUCCEEDED" : "*JRS: FAILED"), null,
				depth, false);
		kb.decrementProofDepth();
		return result;
	}

	public boolean match(RelationSentence rs, List<ProofVariable> pbinds,
			RelationSentence child, List<ProofVariable> cbinds) {
		String relname1 = null, relname2 = null;
		Vector terms1 = null, terms2 = null;
		relname1 = rs.getRelation().getName();
		terms1 = rs.getTerms();
		relname2 = child.getRelation().getName();
		terms2 = child.getTerms();

		// if (pbinds == cbinds) {
		// int z = 1;
		// z = z;
		// for (int i = 0; i < rs.getTermCount(); i++) {
		// try {
		// Term term = (Term) rs.getTerm(i);
		// ProofVariable pvar = pbinds.get(i);
		// if (!(term instanceof Variable)) {
		// Object value = term.eval();
		// if (value != null) {
		// int x = 1;
		// x = x;
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }

		if (terms1 == null && terms2 == null) {
			return true;
		}

		if (!(relname1.equals(relname2) && VUtils.sameLength(terms1, terms2))) {
			return false;
		}

		if (terms1 != null) {
			for (int i = 0; i < terms1.size(); i++) {
				Object term1 = terms1.elementAt(i);
				Object term2 = terms2.elementAt(i);
				if (term1 instanceof Variable) {
					term1 = ((Variable) term1).getProofVariable(pbinds);
				}
				if (term2 instanceof Variable) {
					term2 = ((Variable) term2).getProofVariable(cbinds);
				}
				if (!matchTerms(term1, pbinds, term2, cbinds)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// 4/5/2014: Contains Klooges...
	public static boolean matchTerms(Object term1, List<ProofVariable> pbinds,
			Object term2, List<ProofVariable> cbinds) {
		Object o = null;
		Object val1 = null;
		Object val2 = null;
		Expression ckbe = null;

		if (term1 instanceof JavaFunctionTerm
				|| term2 instanceof JavaFunctionTerm) {
			Term t1 = (Term) term1;
			Term t2 = (Term) term2;
			ckbe = t1.getContainingKBExpression();
			if (ckbe == null) {
				ckbe = t2.getContainingKBExpression();
			}
			if (ckbe != null) {
				ckbe.setSelectedProofVariableList(pbinds);
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
			ckbe.setSelectedProofVariableList(null);
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
				return matchTerms(lft1, pbinds, lft2, cbinds);
			}
		}

		// Why do I need this?
		if (term1 instanceof ProofVariable
				&& (val1 = ((ProofVariable) term1).eval()) != null) {
			return matchTerms(val1, pbinds, term2, cbinds);
		}
		if (term2 instanceof ProofVariable
				&& (val2 = ((ProofVariable) term2).eval()) != null) {
			return matchTerms(term1, pbinds, val2, cbinds);
		}

		if (term1 instanceof ProofVariable && term1 != term2) {
			((ProofVariable) term1).bind(term2);
			return true;
		}
		if (term2 instanceof ProofVariable && term1 != term2) {
			((ProofVariable) term2).bind(term1);
			return true;
		}
		return false;
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
	public boolean proveBindSentence(BindSentence bs, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		boolean result = false;
		ArrayList pbinds = (ArrayList) Expression.getProofVariables(alsoProve);
		Variable var = (Variable) bs.getTerm(0);
		ProofVariable pvar = var.getProofVariable();

		Object t = bs.getTerms().lastElement();
		Object value = Term.evalObject(t);

		if (value != null) {
			List origbinds = null, copybinds = null;
			if (pbinds != null) {
				origbinds = new ArrayList(pbinds);
				copybinds = ProofVariable.clone(origbinds);
			}
			List vlist = (value instanceof ArrayList ? (ArrayList) value
					: ListUtils.listify(value));
			for (Object o : vlist) {
				pvar.bind(o);
				if (proveAP(alsoProve, null) && postProofValidation(bs)) {
					result = true;
					ProofVariable.storeProvisionalBindings();
				}
				if (copybinds != null) {
					ProofVariable.copyBinds(copybinds, origbinds);
				}
				pvar.unbind();
			}
		}
		return result;
	}

	public boolean proveTypePredicate(TypePredicate tp, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		Term t = (Term) tp.getArgument().eval();
		boolean result = t.getType().subsumedBy(tp.getType());
		if (result && alsoProve != null) {
			result = false;
			Vector apvars = (Vector) Expression.getProofVariables(alsoProve);
			Vector origbinds = new Vector(apvars);
			List copybinds = ProofVariable.clone(origbinds);
			if (proveAP(alsoProve, null) && postProofValidation(tp)) {
				result = true;
			}
			ProofVariable.copyBinds(copybinds, origbinds);
		}
		return result;
	}

	public boolean proveNotSentence(NotSentence ns, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		boolean provedSentence = prove(ns.getSentence(), null);
		boolean provedAP = false;
		if (!provedSentence) {
			provedAP = proveAP(alsoProve, localSentences);
		}
		return !provedSentence && provedAP;
	}

	public boolean proveExistentialSentence(ExistentialSentence es,
			Vector alsoProve, Vector<RelationSentence> localSentences) {
		return prove(es.getSentence(), alsoProve);
	}

	public boolean proveAndSentence(AndSentence as, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		return proveAP(VUtils.appendNew(as.getSentences(), alsoProve),
				localSentences);
	}

	public boolean proveOrSentence(OrSentence os, Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		boolean proved = false;
		int pvsindex = ProofVariable.PVSIndex;
		for (Sentence s : os.getSentences()) {
			List<ProofVariable> pvars = s.getProofVariables();
			List<ProofVariable> apvars = Expression
					.getProofVariables(alsoProve);
			List<ProofVariable> origbinds = ListUtils.appendNew(pvars, apvars);
			List<ProofVariable> copybinds = ProofVariable.clone(origbinds);
			if (prove(s, alsoProve, localSentences)) {
				proved = true;
			}
			if (origbinds != null) {
				ProofVariable.copyBinds(copybinds, origbinds);
			}

			// 4/4/2014
			if (proved) {
				break;
			}
		}
		ProofVariable.resetPVSIndex(pvsindex);
		return proved;
	}

	public boolean prove(Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		return false;
	}

	public boolean proveAP(Vector alsoProve,
			Vector<RelationSentence> localSentences) {
		if (alsoProve != null) {
			Sentence sent = (Sentence) alsoProve.firstElement();
			// Expensive...
			Vector rest = VUtils.rest(alsoProve);
			if (!prove(sent, rest, localSentences)) {
				return false;
			}
		}
		return true;
	}

	public boolean postProofValidation(Sentence sentence) {
		// if (!SeqSent.isValidSequence()) {
		// return false;
		// }
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
					Object o = term.eval();
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
		if (KnowledgeEngine.isBreakAtFirstProof()
				&& ProofVariable.validatedBindings != null) {
			return true;
		}
		return false;
	}

	public Vector unpackValidatedBindings(boolean unpack) {
		if (unpack) {
			return ProofVariable.unpackValidatedBindings();
		} else {
			return ProofVariable.getValidatedBindings();
		}
	}
}
