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
package tsl.expression.term.graph;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.form.sentence.Sentence;
import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.knowledge.ontology.TypeRelationSentence;
import tsl.utilities.SetUtils;
import tsl.utilities.VUtils;

public class TermGraph extends Term {
	public Vector<Term> terms = new Vector(0);

	public Vector topTerminals = null;

	public Vector bottomTerminals = null;

	public Hashtable subjectHash = new Hashtable();

	public Hashtable modifierHash = new Hashtable();

	public Vector<RelationSentence> sentences = null;

	public Hashtable subjectModifierHash = new Hashtable();

	public Hashtable relationSubjectModifierHash = new Hashtable();

	public TermGraph() {

	}

	public boolean addSentence(RelationSentence rs) {
		String rname = rs.getRelation().getName();
		TypeConstant subject = (TypeConstant) rs.getSubject();
		TypeConstant modifier = (TypeConstant) rs.getModifier();
		String sname = subject.getName();
		String mname = modifier.getName();
		Sentence s = getSentence(rname, sname, mname);
		if (s != null) {
			return false;
		}
		this.sentences = VUtils.addIfNot(this.sentences, rs);
		this.terms = VUtils.addIfNot(this.terms, rs.getSubject());
		this.terms = VUtils.addIfNot(this.terms, rs.getModifier());
		VUtils.pushIfNotHashVector(subjectHash, rs.getSubject(), rs);
		VUtils.pushIfNotHashVector(modifierHash, rs.getModifier(), rs);
		if (rs instanceof TypeRelationSentence) {
			TypeRelationSentence trs = (TypeRelationSentence) rs;
			trs.setType(trs);
			subject = (TypeConstant) trs.getSubject();
			modifier = (TypeConstant) trs.getModifier();

			// I create relations when I resolve TypeConstants. I don't add
			// relations
			// elsewhere...
			// subject.addRelation(trs);
			// modifier.addRelation(trs);

			String key = RelationSentence.toString(subject, modifier);
			VUtils.pushHashVector(subjectModifierHash, key, trs);
			key = trs.toString();
			relationSubjectModifierHash.put(key, trs);
		}
		return true;
	}

	public void removeTerm(Term term) {
		if (this.terms.contains(term)) {
			terms.remove(term);
			Vector sents = (Vector) subjectHash.get(term);
			if (sents != null) {
				subjectHash.remove(term);
				for (Enumeration e = sents.elements(); e.hasMoreElements();) {
					RelationSentence rs = (RelationSentence) e.nextElement();
					this.sentences.remove(rs);
				}
			}
			sents = (Vector) modifierHash.get(term);
			if (sents != null) {
				modifierHash.remove(term);
				for (Enumeration e = sents.elements(); e.hasMoreElements();) {
					RelationSentence rs = (RelationSentence) e.nextElement();
					this.sentences.remove(rs);
				}
			}
		}
	}

	public void removeSentence(RelationSentence rs) {
		this.sentences.remove(rs);
		Vector sents = (Vector) subjectHash.get(rs.getSubject());
		if (sents != null) {
			sents.remove(rs);
			if (sents.isEmpty()) {
				subjectHash.remove(rs.getSubject());
			}
		}
		sents = (Vector) modifierHash.get(rs.getSubject());
		if (sents != null) {
			sents.remove(rs);
			if (sents.isEmpty()) {
				modifierHash.remove(rs.getSubject());
			}
		}
		sents = (Vector) subjectHash.get(rs.getModifier());
		if (sents != null) {
			sents.remove(rs);
			if (sents.isEmpty()) {
				subjectHash.remove(rs.getModifier());
			}
		}
		sents = (Vector) modifierHash.get(rs.getModifier());
		if (sents != null) {
			sents.remove(rs);
			if (sents.isEmpty()) {
				modifierHash.remove(rs.getModifier());
			}
		}

		if (subjectHash.get(rs.getSubject()) == null
				&& modifierHash.get(rs.getSubject()) == null) {
			removeTerm(rs.getSubject());
		}
		if (subjectHash.get(rs.getModifier()) == null
				&& modifierHash.get(rs.getModifier()) == null) {
			removeTerm(rs.getModifier());
		}
	}

	public boolean isFullyConnected() {
		if (terms.size() <= 1) {
			return true;
		}
		Vector pairs = SetUtils.allPairs(terms);
		Vector newpairs = pairs;
		pairs = newpairs;
		for (Enumeration e = pairs.elements(); e.hasMoreElements();) {
			Vector pair = (Vector) e.nextElement();
			Term t1 = (Term) pair.elementAt(0);
			Term t2 = (Term) pair.elementAt(1);
			if (!isConnected(t1, t2)) {
				return false;
			}
		}
		return true;
	}

	public boolean isConnected(Term t1, Term t2) {
		return isSubjectConnected(t1, t2) || isSubjectConnected(t2, t1);
	}

	public Vector getConnectingPaths(Term t1, Term t2) {
		Vector paths = getConnectingPaths(t1, t2, false, 0);
		return paths;
	}

	// 6/8/2010: Constraints: 1- Concrete endpoints. 2- No local minima. 3-
	// Intermediate
	// points are as abstract as possible.
	public Vector getConnectingPaths(Term startType, Term endType,
			boolean startTypeWasSubject, int depth) {
		if (depth > 3) {
			return null;
		}
		if (startType instanceof TypeConstant) {
			TypeConstant tc1 = (TypeConstant) startType;
			if (depth > 0 && tc1.getParents() != null) {
				return null;
			}
		}
		Vector paths = null;
		boolean startTypeIsSubject = true;
		Vector sentences = (Vector) subjectHash.get(startType);
		if (sentences == null) {
			// No local minima
			if (depth > 0 && !startTypeWasSubject) {
				return null;
			}
			startTypeIsSubject = false;
			sentences = (Vector) modifierHash.get(startType);
		}
		if (sentences != null) {
			for (Enumeration e = sentences.elements(); paths == null
					&& e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				Term matchType = (startTypeIsSubject ? rs.getModifier() : rs
						.getSubject());
				if (matchType.equals(endType)) {
					Vector path = VUtils.listify(rs);
					paths = VUtils.add(paths, path);
				}
			}
			if (paths == null || depth == 0) {
				Vector failed = new Vector(0);
				for (Enumeration e = sentences.elements(); e.hasMoreElements();) {
					RelationSentence rs = (RelationSentence) e.nextElement();
					Term startArg, endArg;
					if (startTypeIsSubject) {
						startArg = rs.getSubject();
						endArg = rs.getModifier();
					} else {
						startArg = rs.getModifier();
						endArg = rs.getSubject();
					}
					if (startArg instanceof TypeConstant
							&& (((TypeConstant) startArg).getConnectedTypes() == null || !((TypeConstant) startArg)
									.getConnectedTypes().contains(endArg))) {
						continue;
					}
					if (!endArg.isVisited() && !rs.isVisited()
							&& !failed.contains(endArg)) {
						endArg.setVisited(true);
						rs.setVisited(true);
						Vector v = getConnectingPaths(endArg, endType,
								!startTypeIsSubject, depth + 1);
						if (v != null) {
							Collections.sort(v,
									new VUtils.InverseLengthSorter());
							int minlen = ((Vector) v.firstElement()).size();
							for (Enumeration ve = v.elements(); ve
									.hasMoreElements();) {
								Vector path = (Vector) ve.nextElement();
								if (path.size() > minlen) {
									break;
								}
								Vector newpath = new Vector(path);
								newpath.insertElementAt(rs, 0);

								/****
								 * 6/8/2010: There should never be more than one
								 * with relation with a given name in a path
								 ******/
								Vector relations = VUtils.gatherFields(newpath,
										"relation");
								Vector rnames = VUtils.gatherFields(relations,
										"name");
								Vector dedups = VUtils.removeDuplicates(rnames);
								if (rnames.size() == dedups.size()) {
									paths = VUtils.add(paths, newpath);
								}
							}
						} else {
							failed.add(endArg);
						}
						rs.setVisited(false);
						endArg.setVisited(false);
					}
				}
			}
		}
		return paths;
	}

	public boolean isSubjectConnected(Term t1, Term t2) {
		Vector sentences = (Vector) subjectHash.get(t1);
		if (sentences != null) {
			for (Enumeration e = sentences.elements(); e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				if (rs.getModifier().equals(t2)) {
					return true;
				}
			}
			for (Enumeration e = sentences.elements(); e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				if (isSubjectConnected(rs.getModifier(), t2)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isModifierConnected(Term t1, Term t2) {
		Vector sentences = (Vector) modifierHash.get(t1);
		if (sentences != null) {
			for (Enumeration e = sentences.elements(); e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				if (rs.getSubject().equals(t2)) {
					return true;
				}
			}
			for (Enumeration e = sentences.elements(); e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				if (isModifierConnected(rs.getSubject(), t2)) {
					return true;
				}
			}
		}
		return false;
	}

	public Term getRoot() {
		Vector roots = getRoots();
		return (Term) roots.firstElement();
	}

	public Vector getRoots() {
		Vector tops = new Vector(0);
		for (Enumeration e = this.terms.elements(); e.hasMoreElements();) {
			Term term = (Term) e.nextElement();
			if (modifierHash.get(term) == null) {
				tops.add(term);
			}
		}
		return tops;
	}

	public Vector getTerminals() {
		Vector terminals = new Vector(0);
		for (Enumeration e = this.terms.elements(); e.hasMoreElements();) {
			Term term = (Term) e.nextElement();
			if (subjectHash.get(term) == null) {
				terminals.add(term);
			}
		}
		return terminals;
	}

	public Vector getSubjectSentences(Term term) {
		return (Vector) subjectHash.get(term);
	}

	public Vector getModifierSentences(Term term) {
		return (Vector) modifierHash.get(term);
	}

	public TypeRelationSentence getSentence(RelationConstant relation,
			TypeConstant subject, TypeConstant modifier) {
		String key = RelationSentence.toString(relation, subject, modifier);
		TypeRelationSentence trs = (TypeRelationSentence) relationSubjectModifierHash
				.get(key);
		return trs;
	}

	public TypeRelationSentence getSentence(String relation, String subject,
			String modifier) {
		String key = RelationSentence.toString(relation, subject, modifier);
		return (TypeRelationSentence) relationSubjectModifierHash.get(key);
	}

	public Vector outEdges(Term term) {
		return (Vector) subjectHash.get(term);
	}

	public Vector inEdges(Term term) {
		return (Vector) modifierHash.get(term);
	}

	public Vector getAllTrees() {
		Vector roots = getRoots();
		Vector trees = null;
		if (roots != null) {
			for (Enumeration e = roots.elements(); e.hasMoreElements();) {
				Term root = (Term) e.nextElement();
				trees = VUtils.add(trees, getTree(root));
			}
		}
		return trees;
	}

	public Vector getTree(Term root) {
		Vector allsents = new Vector(0);
		getTree(root, allsents);
		return allsents;
	}

	public void getTree(Term root, Vector allsents) {
		Vector sents = (Vector) subjectHash.get(root);
		if (sents != null) {
			for (Enumeration e = sents.elements(); e.hasMoreElements();) {
				RelationSentence rs = (RelationSentence) e.nextElement();
				allsents = VUtils.add(allsents, rs);
				getTree(rs.getModifier(), allsents);
			}
		}
	}

	public Vector<RelationSentence> getSentences() {
		return this.sentences;
	}

	public String toString() {
		return "<TermGraph:" + terms + ">";
	}

}
