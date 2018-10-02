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

import tsl.expression.Expression;
import tsl.expression.form.definition.Definition;
import tsl.expression.term.Term;
import tsl.expression.term.constant.Constant;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.function.FunctionConstant;
import tsl.expression.term.graph.TermGraph;
import tsl.expression.term.property.PropertyConstant;
import tsl.expression.term.relation.RelationConstant;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.jlisp.JLUtils;
import tsl.jlisp.JLisp;
import tsl.jlisp.JLispException;
import tsl.jlisp.Sexp;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.knowledge.knowledgebase.KnowledgeBase;
import tsl.knowledge.knowledgebase.SymbolTable;
import tsl.utilities.SetUtils;
import tsl.utilities.VUtils;

public class Ontology extends TermGraph {

	private Hashtable typePathHash = new Hashtable();
	private Vector<TypeConstant> allTypeConstants = new Vector(0);
	private TypeConstant rootType = null;
	private TermGraph typeGraph = null;
	private Vector<TypePath> allTypePaths = new Vector(0);
	private Vector<TypeConstant> allStringConstants = new Vector(0);

	public Ontology() {

	}

	public Ontology(String name) {
		this.setName(name);
		System.out.println("Creating ontology (" + name + ")");
	}

	public static Ontology createFromLisp(String ostr) throws JLispException {
		JLisp jLisp = JLisp.getJLisp();
		Sexp sexp = (Sexp) jLisp.evalString(ostr);
		Vector elements = JLUtils.convertSexpToJVector(sexp);
		String name = "onyx";
		if (elements.firstElement() instanceof String) {
			name = elements.firstElement().toString();
			elements = VUtils.rest(elements);
		}
		KnowledgeEngine ke = KnowledgeEngine.getCurrentKnowledgeEngine();
		Ontology ontology = ke.findOrCreateOntology(name);
		ke.setCurrentOntology(ontology);
		ontology.addTypesAndRelations(elements);
		KnowledgeBase.getCurrentKnowledgeBase().resolveConstants();
		ontology.setupTypeConnectionsWithExpandedRelations();
		ke.resetCurrentOntology();
		return ontology;
	}

	public void addTypesAndRelations(Vector predicates) {
		Vector<TypeConstant> types = new Vector(0);
		if (predicates != null) {
			for (Enumeration e = predicates.elements(); e.hasMoreElements();) {
				try {
					Object o = e.nextElement();
					Vector v = (Vector) o;
					if (Definition.isDefinition(v)) {
						Constant.createConstant(v);
					} else {
						// TypeRelSent sentences, from olden days...
						TypeConstant subject = null;
						TypeConstant modifier = null;
						String relname = (String) v.elementAt(0);
						String sname = (String) v.elementAt(1);
						String mname = (String) v.elementAt(2);
						RelationConstant rc = (RelationConstant) RelationConstant
								.createRelationConstant(relname);
						subject = (TypeConstant) TypeConstant
								.createTypeConstant(sname);
						modifier = (TypeConstant) TypeConstant
								.createTypeConstant(mname);
						types = VUtils.addIfNot(types, subject);
						types = VUtils.addIfNot(types, modifier);
						if ("isa".equals(rc.getName())) {
							subject.addParent(modifier);
						} else {
							TypeRelationSentence trs = new TypeRelationSentence(rc,
									subject, modifier);
							this.addSentence(trs);
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			// Before 11/22/2013: With TSL, some of the TypeRelSents will be
			// coming
			// from type definition statements. I want to expand the relations
			// after resolving the constant definitions.
			// setupTypeConnectionsWithExpandedRelations(types);
		}
	}

	public void setupTypeConnections(Vector types) {
		TypeConstant.gatherUnifiables(types);
		TypeConstant.inheritRelations(getSentences());
		setupTypePaths(types);
	}

	public void setupTypeConnectionsWithExpandedRelations() {
		Vector<TypeConstant> types = this.getAllTypeConstants();
		TypeConstant.gatherUnifiables(types);
		expandRelationsPerSubtype(getSentences());
		TypeConstant.inheritRelations(getSentences());

		// 4/8/2015: Not currently using expanded type paths.
		// setupTypePaths(types);
	}

	public void expandRelationsPerSubtype(Vector<RelationSentence> relSents) {
		if (relSents != null) {
			Vector<RelationSentence> rsv = new Vector(relSents);
			for (RelationSentence rs : rsv) {
				TypeRelationSentence ancestor = (TypeRelationSentence) rs;
				ancestor.setType(ancestor);
				if ("isa".equals(ancestor.getRelation().getName())) {
					continue;
				}
				TypeConstant stype = (TypeConstant) ancestor.getSubject();
				TypeConstant mtype = (TypeConstant) ancestor.getModifier();
				Vector stypes = new Vector(0);
				stypes.add(stype);
				stypes = VUtils.append(stypes, stype.getChildren());
				Vector mtypes = new Vector(0);
				mtypes.add(mtype);
				mtypes = VUtils.append(mtypes, mtype.getChildren());
				Vector v = new Vector(0);
				v.add(stypes);
				v.add(mtypes);
				Vector cp = SetUtils.cartesianProduct(v);
				for (Enumeration pe = cp.elements(); pe.hasMoreElements();) {
					v = (Vector) pe.nextElement();
					stype = (TypeConstant) v.elementAt(0);
					mtype = (TypeConstant) v.elementAt(1);
					if (stype != mtype) {
						TypeRelationSentence trs = getSentence(ancestor
								.getRelation().getName(), stype.getName(),
								mtype.getName());
						if (trs == null) {
							TypeRelationSentence newtrs = new TypeRelationSentence(
									ancestor.getRelation(), stype, mtype);
							newtrs.setType(ancestor);
							addSentence(newtrs);
						}
					}
				}
			}
		}
	}

	public void setupTypePaths(Vector types) {
		if (types != null) {
			Collections.sort(types, new TypeConstant.NameSorter());
			int count = 0;
			for (int i = 0; i < types.size(); i++) {
				TypeConstant tc1 = (TypeConstant) types.elementAt(i);
				for (int j = i + 1; j < types.size(); j++) {
					TypeConstant tc2 = (TypeConstant) types.elementAt(j);
					if (!tc1.isAbstract() && !tc2.isAbstract()) {
						Vector<Vector> paths = getConnectingPaths(tc1, tc2);
						if (paths == null) {
							paths = getConnectingPaths(tc2, tc1);
						}
						if (paths != null) {
							Vector<TypePath> typepaths = new Vector(0);
							Collections.sort(paths,
									new VUtils.InverseLengthSorter());
							String key = RelationSentence.toString(tc1, tc2);
							Vector vkey = VUtils.listify(tc1, tc2);
							for (Vector v : paths) {
								TypePath path = new TypePath(v, tc1, tc2);
								typepaths.add(path);
							}
							typePathHash.put(key, typepaths);
							// 8/30/2013 -- To make it a little faster.
							typePathHash.put(vkey, typepaths);
							for (TypePath path : typepaths) {
								Vector<TypeRelationSentence> sorted = new Vector(
										path.getSentences());
								Collections.sort(sorted,
										new RelationSentence.RelationSorter());
								key = "";
								for (Enumeration<TypeRelationSentence> e = sorted
										.elements(); e.hasMoreElements();) {
									key += e.nextElement().getRelation()
											.getName();
									if (e.hasMoreElements()) {
										key += ":";
									}
								}
								typePathHash.put(key, path);
								count++;
							}
						}
					}
				}
			}
			System.out.println("Total connecting type paths (" + this.getName()
					+ ") = " + count);
		}
	}

	public Vector getTypePaths(TypeConstant t1, TypeConstant t2) {
		Vector paths = null;
		if (t1 != null && t2 != null) {
			Vector<TypeConstant> vkey = VUtils.listify(t1, t2);
			paths = (Vector<TypePath>) this.typePathHash.get(vkey);
			// Before 8/30/2013
			// String key = RelationSentence.toString(t1, t2);
			// paths = (Vector<TypePath>) this.typePathHash.get(key);
		}
		return paths;
	}

	// 8/30/2013
	public TypeRelationSentence getFirstRelation(TypeConstant t1,
			TypeConstant t2) {
		Vector<TypePath> paths = getTypePaths(t1, t2);
		if (paths != null) {
			for (TypePath path : paths) {
				if (path.getSentences().size() == 1) {
					return path.getSentences().firstElement();
				}
			}
		}
		return null;
	}

	public String toLispString() {
		StringBuffer sb = new StringBuffer();
		Hashtable<RelationSentence, RelationSentence> trshash = new Hashtable();
		sb.append("\'(\"" + this.getName() + "\"\n");
		if (this.getAllTypeConstants() != null) {
			for (TypeConstant type : this.getAllTypeConstants()) {
				String str = "(domain \"" + type.getName() + "\")\n";
				sb.append(str);
			}
			for (TypeConstant type : this.getAllTypeConstants()) {
				if (type.getParents() != null) {
					for (Term parent : type.getParents()) {
						String pstr = "(isa \"" + type.getName() + "\" \""
								+ parent.getName() + "\")\n";
						sb.append(pstr);
					}
				}
			}
		}
		if (this.getSentences() != null) {
			for (RelationSentence trs : this.getSentences()) {
				trshash.put(trs, trs);
			}
			for (Enumeration e = trshash.keys(); e.hasMoreElements();) {
				TypeRelationSentence trs = (TypeRelationSentence) e
						.nextElement();
				String str = "(" + trs.getRelation().getName() + " \""
						+ trs.getSubject().getName() + "\" \""
						+ trs.getModifier().getName() + "\")";
				sb.append(str + "\n");
			}
		}
		sb.append("\n)");
		return sb.toString();
	}

	public TypeConstant getRootType() {
		if (this.rootType == null && this.allTypeConstants != null) {
			for (TypeConstant type : this.allTypeConstants) {
				if (type.isRoot()) {
					this.rootType = type;
					break;
				}
			}
		}
		return this.rootType;
	}
	
	// 4/12/2015:  Move all getConstant()-type functions here eventually.
	public Constant getConstant(String name) {
		return this.getKnowledgeBase().getConstant(name);
	}

	public Vector<Constant> getAllTypedConstants(TypeConstant type) {
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		return st.getAllTypedConstants(type);
	}

	public Vector<StringConstant> getAllTypedStringConstants(TypeConstant type) {
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		Vector<Constant> cs = st.getAllTypedConstants(type);
		Vector<StringConstant> scs = null;
		if (cs != null) {
			for (Constant c : cs) {
				if (c instanceof StringConstant) {
					scs = VUtils.add(scs, c);
				}
			}
		}
		return scs;
	}

	public Vector<StringConstant> getAllStringConstants() {
		Vector<StringConstant> rv = null;
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		int x = 1;
		Vector<StringConstant> strconstants = st.getAllStringConstants();
		if (strconstants != null) {
			for (StringConstant sc : strconstants) {
				if (!sc.isComplex()) {
					rv = VUtils.add(rv, sc);
				}
			}
		}
		return rv;
	}
	
	
	public Vector<String> getAllStringConstantNames() {
		Vector<StringConstant> scs = this.getAllStringConstants();
		Vector<String> cnames = new Vector(0);
		for (StringConstant sc : scs) {
			cnames.add(sc.getName());
		}
		Collections.sort(cnames);
		return cnames;
	}

	public Vector<TypeConstant> getAllTypeConstants() {
		if (this.allTypeConstants.isEmpty()) {
			SymbolTable st = this.getKnowledgeBase().getNameSpace()
					.getCurrentSymbolTable();
			this.allTypeConstants = st.getAllTypeConstants();
			Collections
					.sort(this.allTypeConstants, new Expression.NameSorter());
		}
		return this.allTypeConstants;
	}

	public Vector<RelationConstant> getAllRelationConstants() {
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		return st.getAllRelationConstants();
	}

	public Vector<FunctionConstant> getAllFunctionConstants() {
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		return st.getAllFunctionConstants();
	}

	public Vector<PropertyConstant> getAllPropertyConstants() {
		SymbolTable st = this.getKnowledgeBase().getNameSpace()
				.getCurrentSymbolTable();
		return st.getAllPropertyConstants();
	}

	public void addTypeConstant(TypeConstant tc) {
		this.allTypeConstants = VUtils.add(this.allTypeConstants, tc);
	}

	public Hashtable getTypePathHash() {
		return typePathHash;
	}

	public void setTypePathHash(Hashtable typePathHash) {
		this.typePathHash = typePathHash;
	}

	public TermGraph getTypeGraph() {
		return typeGraph;
	}

	public void setTypeGraph(TermGraph typeGraph) {
		this.typeGraph = typeGraph;
	}

	public Vector getAllTypePaths() {
		return allTypePaths;
	}

	public void setAllTypePaths(Vector allTypePaths) {
		this.allTypePaths = allTypePaths;
	}

	public void addTypePath(TypePath path) {
		this.allTypePaths = VUtils.add(this.allTypePaths, path);
	}

	public boolean isOnyx() {
		return "onyx".equals(this.getName());
	}

	public String toString() {
		return "<Ontology=" + this.getName() + ">";
	}
	
}
