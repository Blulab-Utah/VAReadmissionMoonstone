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

import java.util.Enumeration;
import java.util.Vector;

import tsl.expression.Expression;
import tsl.expression.form.Form;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.tsllisp.Sexp;
import tsl.tsllisp.TLUtils;
import tsl.tsllisp.TLisp;
import tsl.utilities.VUtils;

public class Query extends Sentence {
	private Vector sourceVector = null;
	public static float RelSentProofCount = 0f;
	public static boolean firstAnswerOnly = false;

	// '(query (supports-pneumonia ?report))
	public Query(Vector v) {
		super(v);
		this.sourceVector = v;
	}

	public static Query createQuery(Vector v) {
		Query query = null;
		if ("query".equals(v.firstElement())) {
			query = new Query(v);
		}
		return query;
	}

	public Object eval(Vector binds) {
		return eval(binds, true);
	}

	public Object eval(KnowledgeBase kb, Vector binds,
			Vector<RelationSentence> localSentences, boolean unpackResults) {
		Vector v = (Vector) this.getSourceVector().elementAt(1);
		Sentence sentence = (Sentence) kb.initializeForm(v);
		return Query
				.doQuery(kb, sentence, binds, localSentences, unpackResults);
	}

	public static Vector doQuery(KnowledgeBase kb, Sentence sentence,
			Vector binds, Vector<RelationSentence> localSentences,
			boolean unpackResults) {
		kb.getInferenceEngine().initializeQuerySentence(kb, sentence, binds);
		boolean proved = kb.getInferenceEngine()
				.prove(sentence, localSentences);
		if (proved) {
			Vector v = kb.getInferenceEngine().unpackValidatedBindings(
					unpackResults);
			if (v == null) {
				return new Vector(0);
			}
			return v;
		}
		return null;
	}

	public static String getQueryResultsString(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences) {
		return getQueryResultsString(kb, qstr, binds, localSentences, true);
	}

	public static String getQueryResultsString(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences,
			boolean unpackResults) {
		String answer = null;
		Object rv = getQueryResults(kb, qstr, binds, localSentences,
				unpackResults);
		Expression query = KnowledgeBase.getCurrentKnowledgeBase()
				.getQueryExpression();
		answer = extractResultsString((Sentence) query, rv);
		return answer;
	}

	public static String extractResultsString(Expression exp, Object results) {
		String answer = null;
		if (results instanceof String) {
			answer = (String) results;
		} else if (results instanceof Vector) {
			Vector v = (Vector) results;
			StringBuffer sb = new StringBuffer();
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Vector values = (Vector) e.nextElement();
				for (int i = 0; i < exp.getVariableCount(); i++) {
					Variable var = (Variable) exp.getVariable(i);
					Object value = values.elementAt(i);
					sb.append(var.getName() + "=" + value);
					if (i < exp.getVariableCount() - 1) {
						sb.append("&");
					}
				}
				sb.append("\n");
			}
			answer = sb.toString();
		} else {
			answer = "[Not Proved]";
		}
		return answer;
	}

	public static Vector getQueryResultsVector(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences, int offset) {
		return getQueryResultsVector(kb, qstr, binds, localSentences, offset,
				true);
	}

	public static Vector getQueryResultsVector(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences, int offset,
			boolean unpackResults) {
		Object o = getQueryResults(kb, qstr, binds, localSentences,
				unpackResults);
		if (o instanceof Vector) {
			Vector v = (Vector) o;
			if (offset == -1) {
				return v;
			}
			return VUtils.gatherNthElements(v, offset);
		}
		return null;
	}

	public static Vector getQueryResultsVectorSingleVariable(KnowledgeBase kb,
			String qstr, Vector<RelationSentence> localSentences, Vector binds) {
		return getQueryResultsVector(kb, qstr, binds, localSentences, 0, true);
	}

	public static Vector getQueryResultsVectorSingleVariable(KnowledgeBase kb,
			String qstr, Vector binds, Vector<RelationSentence> localSentences,
			boolean unpackResults) {
		return getQueryResultsVector(kb, qstr, binds, localSentences, 0,
				unpackResults);
	}

	public static Object getQueryResults(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences) {
		return getQueryResults(kb, qstr, binds, localSentences, true);
	}

	public static Object getQueryResults(KnowledgeBase kb, String qstr,
			Vector binds, Vector<RelationSentence> localSentences,
			boolean unpackResults) {
		String answer = null;
		try {
			qstr = "'(query " + qstr + ")";
			Sexp sexp = (Sexp) TLisp.getTLisp().evalString(qstr);
			Vector v = TLUtils.convertSexpToJVector(sexp);
			Query query = (Query) Form.createForm(v);
			return query.eval(kb, binds, localSentences, unpackResults);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	public Vector getSourceVector() {
		return this.sourceVector;
	}

}
