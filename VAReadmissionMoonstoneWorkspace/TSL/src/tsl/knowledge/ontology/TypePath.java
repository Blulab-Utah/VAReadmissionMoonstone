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
package tsl.knowledge.ontology;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.expression.term.Term;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.VUtils;

public class TypePath {
	private Vector<TypeRelationSentence> sentences = null;

	private Vector<TypeConstant> types = null;

	private Variable startVar = null;

	private Vector<Variable> intermediateVars = null;

	private Variable endVar = null;

	private Term root = null;

	private int length = 0;

	Ontology ontology = null;

	public TypePath(Vector relSents, TypeConstant startType,
			TypeConstant endType) {
		this.ontology = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getCurrentOntology();
		this.sentences = relSents;
		for (TypeRelationSentence rs : this.sentences) {
			for (int i = 0; i < rs.getArity(); i++) {
				Term term = (Term) rs.getTerm(i);
				types = VUtils.addIfNot(types, term);
				if (term.equals(startType)) {
					this.startVar = new Variable(term);
				} else if (term.equals(endType)) {
					this.endVar = new Variable(term);
				} else {
					this.intermediateVars = VUtils.addIfNot(
							this.intermediateVars, new Variable(term));
				}
			}
		}
		this.root = TypeRelationSentence.findRootTerm(this.sentences);
		this.length = sentences.size();
		Collections.sort(this.types, new TypeConstant.NameSorter());
		this.ontology.addTypePath(this);
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof TypePath) {
			TypePath other = (TypePath) o;
			if (this.getSentences().size() == other.getSentences().size()
					&& this.getTypes().equals(other.getTypes())) {
				for (int i = 0; i < this.getSentences().size(); i++) {
					if (!this.getSentences().elementAt(i)
							.equals(other.getSentences().elementAt(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public static void setupTypePaths(Vector typeConstants,
			Hashtable typePathHash) {
		if (typeConstants != null) {
			for (int i = 0; i < typeConstants.size(); i++) {
				TypeConstant t1 = (TypeConstant) typeConstants.elementAt(i);
				for (int j = 0; j < typeConstants.size(); j++) {
					if (i != j) {
						TypeConstant t2 = (TypeConstant) typeConstants
								.elementAt(j);
						Vector paths = getConnectingPaths(t1, t2);
						if (paths != null) {
							Vector typepaths = new Vector(0);
							Collections.sort(paths,
									new VUtils.InverseLengthSorter());
							String key = getSortedTermKey(t1, t2);
							for (Enumeration e = paths.elements(); e
									.hasMoreElements();) {
								Vector v = (Vector) e.nextElement();
								typepaths.add(new TypePath(v, t1, t2));
							}
							typePathHash.put(key, typepaths);
							for (Enumeration e = typepaths.elements(); e
									.hasMoreElements();) {
								TypePath path = (TypePath) e.nextElement();
								key = getSortedTermKey(path.getSentences());
								typePathHash.put(key, path);
							}
						}
					}
				}
			}
		}
	}

	public boolean isSubsumedBy(TypePath path) {
		if (this.types.size() == path.types.size()) {
			for (int i = 0; i < this.types.size(); i++) {
				TypeConstant tc1 = (TypeConstant) this.types.elementAt(i);
				TypeConstant tc2 = (TypeConstant) path.types.elementAt(i);
				if (!(tc1 == tc2 || tc1.subsumedBy(tc2))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	// Removed methods for getting typepaths

	public static Vector getConnectingPaths(TypeConstant tc1, TypeConstant tc2) {
		Vector validpaths = null;
		if (!tc1.isAbstract() && !tc2.isAbstract()) {
			Vector paths = getConnectingPaths(tc1, tc2, VUtils.listify(tc1));
			if (paths != null) {
				for (Enumeration e = paths.elements(); e.hasMoreElements();) {
					Vector path = (Vector) e.nextElement();
					if (validPathSentences(path)) {
						validpaths = VUtils.addIfNot(validpaths, path);
					}
				}
			}
		}
		return validpaths;
	}

	public static Vector getConnectingPaths(TypeConstant tc1, TypeConstant tc2,
			Vector sofar) {
		Vector paths = null;
		Vector path = null;
		if (!tc1.isAbstract() && sofar.size() > 1) {
			return null;
		}
		if (tc1.getConnectedTypes() != null) {
			if (tc1.getConnectedTypes().contains(tc2)) {
				// Problem with Vector typecasting...
				Vector<RelationSentence> rss = new Vector(tc1.getRelations());
				Vector v = TypeRelationSentence.findConnecting(rss, tc1, tc2);
				if (v != null) {
					for (Enumeration e = v.elements(); e.hasMoreElements();) {
						TypeRelationSentence trs = (TypeRelationSentence) e
								.nextElement();
						path = VUtils.listify(trs);
						paths = VUtils.addIfNot(paths, path);
					}
				}
			}
			if (sofar.size() == 1 || paths == null) {
				Vector connectedtypes = TypeConstant
						.removeSubsumed(tc1.getConnectedTypes());
				for (Enumeration e = connectedtypes.elements(); e
						.hasMoreElements();) {
					TypeConstant otc = (TypeConstant) e.nextElement();
					if (!sofar.contains(otc) && otc != tc2) {
						Vector newsofar = new Vector(sofar);
						newsofar.add(otc);
						Vector newpaths = getConnectingPaths(otc, tc2, newsofar);
						if (newpaths != null) {
							Vector adjustedpaths = null;
							Vector<RelationSentence> rss = new Vector(
									tc1.getRelations());
							Vector v = TypeRelationSentence.findConnecting(rss,
									tc1, otc);
							if (v != null) {
								for (Enumeration te = v.elements(); te
										.hasMoreElements();) {
									TypeRelationSentence trs = (TypeRelationSentence) te
											.nextElement();
									for (Enumeration pe = newpaths.elements(); pe
											.hasMoreElements();) {
										Vector newpath = (Vector) pe
												.nextElement();
										TypeRelationSentence newrs = (TypeRelationSentence) newpath
												.firstElement();
										if (!newrs.getModifier().equals(
												trs.getModifier())
												&& !trs.canUnify(newrs)
												&& !newrs.canUnify(trs)) {
											newpath.add(0, trs);
											adjustedpaths = VUtils.addIfNot(
													adjustedpaths, newpath);
										}
									}
								}
							}
							paths = VUtils.appendIfNot(paths, adjustedpaths);
						}
					}
				}
			}
		}
		return paths;
	}

	static boolean validPathSentences(Vector sents) {
		for (int i = 0; i < sents.size() - 1; i++) {
			TypeRelationSentence trs1 = (TypeRelationSentence) sents
					.elementAt(i);
			for (int j = i + 1; j < sents.size(); j++) {
				TypeRelationSentence trs2 = (TypeRelationSentence) sents
						.elementAt(j);
				if (trs1.sameType(trs2)) {
					return false;
				}
			}
		}
		return true;
	}

	public String toString() {
		return this.sentences.toString();
	}

	public Vector<TypeRelationSentence> getSentences() {
		return sentences;
	}

	public void setSentences(Vector<TypeRelationSentence> sentences) {
		this.sentences = sentences;
	}

	public Vector<TypeConstant> getTypes() {
		return types;
	}

	public void setTypes(Vector<TypeConstant> types) {
		this.types = types;
	}

	public Variable getStartVar() {
		return startVar;
	}

	public void setStartVar(Variable startVar) {
		this.startVar = startVar;
	}

	public Vector<Variable> getIntermediateVars() {
		return intermediateVars;
	}

	public void setIntermediateVars(Vector<Variable> intermediateVars) {
		this.intermediateVars = intermediateVars;
	}

	public Variable getEndVar() {
		return endVar;
	}

	public void setEndVar(Variable endVar) {
		this.endVar = endVar;
	}

	public Term getRoot() {
		return root;
	}

	public void setRoot(Term root) {
		this.root = root;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public static String getSortedTermKey(TypeConstant t1, TypeConstant t2) {
		String key = "";
		Vector<Term> v = VUtils.listify(t1, t2);
		Collections.sort(v, new TypeConstant.NameSorter());
		key = v.elementAt(0).getName() + ":" + v.elementAt(1).getName();
		return key;
	}

	public static String getSortedTermKey(Vector<TypeRelationSentence> sentences) {
		String key = "";
		Vector<TypeRelationSentence> v = new Vector(sentences);
		Collections.sort(v, new TypeRelationSentence.RelationSorter());
		for (Enumeration<TypeRelationSentence> e = v.elements(); e
				.hasMoreElements();) {
			key += e.nextElement().getRelation().getName();
			if (e.hasMoreElements()) {
				key += ":";
			}
		}
		return key;
	}

}
