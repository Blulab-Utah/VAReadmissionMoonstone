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
package tsl.inference.forwardchaining;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.form.sentence.ImplicationSentence;
import tsl.expression.form.sentence.NotSentence;
import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.variable.Variable;
import tsl.inference.InferenceEngine;
import tsl.utilities.HUtils;
import tsl.utilities.VUtils;

public class ForwardChainingInferenceEngine extends InferenceEngine {

	private int proofID = 0;
	private boolean expandNegationSentences = false;

	// NS.name -> NS
	private Hashtable<String, NamedSentence> namedSentenceTable = new Hashtable();
	// RelationName -> [list of relation-level NSs]
	private Hashtable<String, Vector<NamedSentence>> namedRelationSentenceTable = new Hashtable();
	private Hashtable<NamedSentence, Vector<SentenceExpansion>> sentenceExpansionTable = new Hashtable();
	private Hashtable<Vector, RelationSentence> inferredSentenceHash = new Hashtable();

	// 9/30/2015
	// private Hashtable<RelationSentenceHashIndex, RelationSentence>
	// relationSentenceIndexHash = new Hashtable();

	// 4/30/2015: NamedSentence->[list of SentenceInstances]
	private Hashtable<NamedSentence, Vector<SentenceInstance>> namedSentenceInstanceTable = new Hashtable();
	private int newSentenceCount = 0;
	private int duplicateSentenceCount = 0;
	private boolean doDebug = false;

	public ForwardChainingInferenceEngine(boolean doDebug) {
		this.doDebug = doDebug;
	}

	public Vector<Sentence> getAllInferredSentences(
			Vector<RelationSentence> rsents) {
		Vector<Sentence> allInferredSentences = null;
		this.clearInferenceStructures();
		if (rsents != null) {
			this.proofCount++;
			while (rsents != null) {
				this.resetInferenceStructures();
				this.expandRelationSentences(rsents);
				this.expandNegationSentences = true;
				// expandNotSentences();
				rsents = null;
				if (this.newSentenceCount > 0) {
					rsents = this.getInferredSentences();
					allInferredSentences = VUtils.append(allInferredSentences,
							rsents);
				}
			}
		}
		this.clearInferenceStructures();
		return allInferredSentences;
	}

	public Vector<RelationSentence> getAllInferredRelationSentences(
			Vector<RelationSentence> rsents) {
		Vector<Sentence> isents = getAllInferredSentences(rsents);
		Vector<RelationSentence> newrsents = null;
		if (isents != null) {
			for (Sentence isent : isents) {
				if (isent instanceof RelationSentence) {
					newrsents = VUtils.add(newrsents, (RelationSentence) isent);
				}
			}
		}
		return newrsents;
	}

	// 5/1/2015: NOT TESTED
	public void expandNotSentences() {
		for (Enumeration<NamedSentence> e = this.namedSentenceTable.elements(); e
				.hasMoreElements();) {
			NamedSentence ns = e.nextElement();
			if (ns.getSentence() instanceof NotSentence) {
				Vector<SentenceInstance> nsis = this.namedSentenceInstanceTable
						.get(ns);
				if (nsis == null) {
					if (ns.getParent() != null) {
						Vector<SentenceExpansion> pses = this.sentenceExpansionTable
								.get(ns);
						if (pses != null) {
							for (SentenceExpansion pse : pses) {
								pse.createNotSentenceInstance(ns);
							}
						}
					}
				}
			}
		}
	}

	public void expandRelationSentences(Vector<RelationSentence> rsents) {
		for (RelationSentence rs : rsents) {
			processRelationSentence(rs);
		}
	}

	public void processRelationSentence(RelationSentence rs) {
		Vector<NamedSentence> nsv = namedRelationSentenceTable.get(rs
				.getRelation().getName());
		if (nsv != null) {
			for (NamedSentence ns : nsv) {
				if (canUnify(rs, (RelationSentence) ns.sentence)) {
					new SentenceInstance(rs, ns);
				}
			}
		}
	}

	public boolean canUnify(RelationSentence rs1, RelationSentence rs2) {
		if (!rs1.getRelation().equals(rs2.getRelation())) {
			return false;
		}
		for (int i = 0; i < rs1.getTermCount(); i++) {
			Term term1 = (Term) rs1.getTerm(i);
			Term term2 = (Term) rs2.getTerm(i);
			if (term1 == null || term2 == null) {
				return false;
			}
			if (!(term1 instanceof Variable) && !(term2 instanceof Variable)) {
				if (!term1.equals(term2)) {
					return false;
				}
			}
		}
		return true;
	}

	public NamedSentence createNamedSentence(Sentence sentence,
			NamedSentence parent, String name, int pindex) {
		NamedSentence ns = this.namedSentenceTable.get(name);
		if (ns == null) {
			ns = new NamedSentence(this, sentence, parent, name, pindex);
			if (ns.sentence instanceof NotSentence) {
				int x = 1;
				x = x;
			}
			namedSentenceTable.put(ns.name, ns);
			if (ns.sentence instanceof RelationSentence) {
				RelationSentence rs = (RelationSentence) ns.sentence;
				VUtils.pushHashVector(this.namedRelationSentenceTable, rs
						.getRelation().getName(), ns);
			}
		}
		return ns;
	}

	public void storeRules(Vector<ImplicationSentence> isents) {
		this.clearAllStructures();
		if (isents != null) {
			for (ImplicationSentence isent : isents) {
				if (isent.getName() != null) {
					NamedSentence ns = this.createNamedSentence(isent, null,
							isent.getName(), 0);
				}
			}
		}
		int x = 0;
	}

	public void addInferredSentence(RelationSentence rs) {
		Vector v = new Vector(0);
		v.add(rs.getRelation());
		v.add(rs.getTerms());
		if (this.inferredSentenceHash.get(v) == null) {
			this.inferredSentenceHash.put(v, rs);
			processRelationSentence(rs);
			this.newSentenceCount++;
			// System.out.println("INFERRED: " + rs);
		} else {
			this.duplicateSentenceCount++;
			// System.out.println("DUPLICATE: " + rs);
		}
	}

	public Vector<RelationSentence> getInferredSentences() {
		return HUtils.getElements(this.inferredSentenceHash);
	}

	public void resetInferenceStructures() {
		this.duplicateSentenceCount = 0;
		this.newSentenceCount = 0;
		this.expandNegationSentences = false;
	}

	public void clearInferenceStructures() {
		this.resetInferenceStructures();
		this.sentenceExpansionTable.clear();
		this.namedSentenceInstanceTable.clear();
		// this.relationSentenceIndexHash.clear();
		this.inferredSentenceHash.clear();
	}

	public void clearAllStructures() {
		this.namedRelationSentenceTable.clear();
		this.namedSentenceTable.clear();
		clearInferenceStructures();
	}

	public static String getBindingString(Object[] binds) {
		int numunbound = 0;
		String vstr = "[";
		for (int i = 0; i < binds.length; i++) {
			if (binds[i] == null) {
				numunbound++;
			}
		}
		if (numunbound == 0) {
			// vstr += "-FULL-";
			vstr += "(" + numunbound + ")";
		}
		for (int i = 0; i < binds.length; i++) {
			String bstr = null;
			if (binds[i] == null) {
				bstr = "?";
			} else {
				// bstr = binds[i].toString();
				bstr = "*";
			}
			vstr += bstr;
			if (i < binds.length - 1) {
				vstr += ",";
			}
		}
		vstr += "]";
		return vstr;
	}

	// 4/30/2015
	public Vector<Sentence> gatherSupportingSentences(RelationSentence rs) {
		return gatherSupportingSentences(rs.getDerivedFrom());
	}

	public static Vector<Sentence> gatherSupportingSentences(SentenceInstance si) {
		Vector<Sentence> derivedFrom = null;
		if (si != null) {
			if (si.getDerivedFrom() != null) {
				derivedFrom = VUtils.listify(si.getDerivedFrom());
			} else if (si.getChildSentenceInstances() != null) {
				for (SentenceInstance csi : si.getChildSentenceInstances()) {
					derivedFrom = VUtils.append(derivedFrom,
							gatherSupportingSentences(csi));
				}
			}
		}
		return derivedFrom;
	}

	public int getNewSentenceCount() {
		return newSentenceCount;
	}

	public int getDuplicateSentenceCount() {
		return duplicateSentenceCount;
	}

	public boolean isExpandNegationSentences() {
		return expandNegationSentences;
	}

	public void setExpandNegationSentences(boolean expandNegationSentences) {
		this.expandNegationSentences = expandNegationSentences;
	}

	public Hashtable<String, NamedSentence> getNamedSentenceTable() {
		return namedSentenceTable;
	}

	public Hashtable<String, Vector<NamedSentence>> getNamedRelationSentenceTable() {
		return namedRelationSentenceTable;
	}

	public Hashtable<NamedSentence, Vector<SentenceExpansion>> getSentenceExpansionTable() {
		return sentenceExpansionTable;
	}

	public Hashtable<Vector, RelationSentence> getInferredSentenceHash() {
		return inferredSentenceHash;
	}

	public Hashtable<NamedSentence, Vector<SentenceInstance>> getNamedSentenceInstanceTable() {
		return namedSentenceInstanceTable;
	}

	public boolean isDoDebug() {
		return doDebug;
	}

	public void setDoDebug(boolean doDebug) {
		this.doDebug = doDebug;
	}

}
