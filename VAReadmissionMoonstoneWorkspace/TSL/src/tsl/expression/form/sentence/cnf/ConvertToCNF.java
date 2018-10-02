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
package tsl.expression.form.sentence.cnf;

import java.util.Vector;

import tsl.expression.form.sentence.AndSentence;
import tsl.expression.form.sentence.BiconditionalSentence;
import tsl.expression.form.sentence.ComplexSentence;
import tsl.expression.form.sentence.ExistentialSentence;
import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.OrSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.form.sentence.UniversalSentence;
import tsl.expression.term.constant.SkolemConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.utilities.VUtils;

// 7/10/2013. Not yet tested...

public class ConvertToCNF {

	public static Sentence convertCNF(Sentence s) {
		Sentence converted = null;
		 // ??? How to apply changes in order, tail-recursively?  
		return s;
	}

	public static AndSentence distributeOrSentences(OrSentence os) {
		AndSentence newas = distributeOrSentences(os.getSentences().elementAt(0), os.getSentences().elementAt(1));
		for (int i = 2; i < os.getSentences().size() - 1; i++) {
			Sentence cs = os.getSentences().elementAt(i);
			newas = distributeOrSentences(newas, cs);
		}
		return newas;
	}

	public static AndSentence distributeOrSentences(Sentence s1, Sentence s2) {
		AndSentence newas = new AndSentence();
		Vector<Sentence> children1 = (s1 instanceof ComplexSentence ? ((ComplexSentence) s1).getSentences()
				: VUtils.listify(s1));
		Vector<Sentence> children2 = (s2 instanceof ComplexSentence ? ((ComplexSentence) s2).getSentences()
				: VUtils.listify(s2));
		for (Sentence cs : children1) {
			Sentence flat = flatten(cs);
			newas.addSentence(flat);
		}
		for (Sentence cs : children2) {
			Sentence flat = flatten(cs);
			newas.addSentence(flat);
		}
		return newas;
	}

	public static Sentence convertBiconditionals(BiconditionalSentence bsent) {
		AndSentence newas = new AndSentence();
		ImplicationSentence is1 = new ImplicationSentence();
		ImplicationSentence is2 = new ImplicationSentence();
		is1.setAntecedent(bsent.getAntecedent());
		is1.setConsequent(bsent.getConsequent());
		is2.setAntecedent(bsent.getConsequent());
		is2.setConsequent(bsent.getAntecedent());
		newas.addSentence(is1);
		newas.addSentence(is2);
		return newas;
	}

	public static Sentence convertImplications(ImplicationSentence isent) {
		OrSentence newos = new OrSentence();
		newos.addSentence(isent.getAntecedent());
		newos.addSentence(new NotSentence(isent.getConsequent()));
		return newos;
	}

	public static Sentence moveNegations(Sentence ns) {
		Sentence converted = null;
		if (ns instanceof NotSentence) {
			converted = moveNegations((NotSentence) ns);
		}
		// How to apply recursively?
		return converted;
	}

	public static Sentence moveNegations(NotSentence ns) {
		Sentence cs = ns.getSentence();
		Sentence converted = null;
		if (cs instanceof UniversalSentence) {
			Sentence ccs = ((UniversalSentence) cs).getSentence();
			ExistentialSentence newes = new ExistentialSentence();
			NotSentence newns = new NotSentence();
			newns.setSentence(ccs);
			newes.setSentence(newns);
			converted = newes;
		} else if (cs instanceof ExistentialSentence) {
			Sentence ccs = ((ExistentialSentence) cs).getSentence();
			UniversalSentence newus = new UniversalSentence();
			NotSentence newns = new NotSentence();
			newns.setSentence(ccs);
			newus.setSentence(newns);
			converted = newus;
		} else if (cs instanceof OrSentence) {
			OrSentence os = (OrSentence) cs;
			AndSentence newas = new AndSentence();
			for (Sentence ccs : os.getSentences()) {
				NotSentence newns = new NotSentence();
				ns.setSentence(ccs);
				newas.addSentence(ns);
			}
			converted = newas;
		} else if (cs instanceof AndSentence) {
			AndSentence as = (AndSentence) cs;
			OrSentence newos = new OrSentence();
			for (Sentence ccs : as.getSentences()) {
				NotSentence newns = new NotSentence();
				ns.setSentence(ccs);
				newos.addSentence(ns);
			}
			converted = newos;
		} else if (cs instanceof NotSentence) {
			NotSentence cns = (NotSentence) cs;
			converted = cns.getSentence();
		}
		return converted;
	}

	public static Sentence dropUniversals(Sentence s) {
		Sentence result = s;
		if (s instanceof UniversalSentence) {
			result = ((UniversalSentence) s).getSentence();
		}
		return result;
	}

	public static Sentence skolemize(ExistentialSentence es) {
		RelationSentence rs = (RelationSentence) es.getSentence();
		RelationSentence newrs = new RelationSentence(rs.getRelation());
		SkolemConstant sc = new SkolemConstant(rs);
		newrs.addTerm(sc);
		return newrs;
	}

	public static Sentence flatten(Sentence s) {
		return s;
	}

	public static Sentence flatten(NotSentence ns) {
		NotSentence newns = new NotSentence();
		Sentence flat = flatten(ns.getSentence());
		newns.setSentence(flat);
		return newns;
	}

	public static Sentence flatten(ComplexSentence cs) {
		ComplexSentence newcs = (cs instanceof AndSentence ? new AndSentence() : new OrSentence());
		Vector<Sentence> children = null;
		for (Sentence css : cs.getSentences()) {
			Sentence flat = flatten(css);
			if (flat instanceof ComplexSentence) {
				ComplexSentence cflat = (ComplexSentence) flat;
				children = VUtils.append(children, cflat.getSentences());
			} else {
				children = VUtils.add(children, flat);
			}
		}
		newcs.setSentences(children);
		return newcs;
	}

	public static Sentence flatten(OrSentence os) {
		AndSentence newos = new AndSentence();
		Vector<Sentence> children = null;
		for (Sentence cs : os.getSentences()) {
			Sentence flat = flatten(cs);
			if (flat instanceof ComplexSentence) {
				ComplexSentence cflat = (ComplexSentence) flat;
				children = VUtils.append(children, cflat.getSentences());
			} else {
				children = VUtils.add(children, flat);
			}
		}
		newos.setSentences(children);
		return newos;
	}

}
